package com.github.jwchung.junit4pioneer;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public class FirstClassTestRunner extends Runner implements Filterable {
    private final InnerFirstClassTestRunner innerRunner;

    public FirstClassTestRunner(Class<?> klass) throws InitializationError {
        innerRunner = new InnerFirstClassTestRunner(klass);
    }

    @Override
    public Description getDescription() {
        return innerRunner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        innerRunner.run(notifier);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        innerRunner.filter(filter);
    }

    private static class InnerFirstClassTestRunner
            extends ParentRunner<FirstClassTestCaseMethod> {
        private final ConcurrentHashMap<FirstClassTestCaseMethod, Description> methodDescriptions =
                new ConcurrentHashMap<>();

        public InnerFirstClassTestRunner(Class<?> declaringClass) throws InitializationError {
            super(declaringClass);
        }

        @Override
        protected List<FirstClassTestCaseMethod> getChildren() {
            return getTestClass()
                    .getAnnotatedMethods(Test.class)
                    .stream()
                    .flatMap(declaringMethod -> {
                        try {
                            return new FirstClassTestCaseMethodComposer(
                                    declaringMethod, getDeclaringClass())
                                    .compose();
                        } catch (RuntimeException exception) {
                            throw exception;
                        } catch (Throwable throwable) {
                            // do nothing to throw an exception as no tests found.
                        }
                        return Stream.empty();
                    }).collect(Collectors.toList());
        }

        @Override
        protected Description describeChild(FirstClassTestCaseMethod child) {
            Description description = methodDescriptions.get(child);

            if (description == null) {
                description = child.createTestDescription();

                methodDescriptions.putIfAbsent(child, description);
            }
            return description;
        }

        @Override
        protected void runChild(FirstClassTestCaseMethod child, RunNotifier notifier) {
            Description description = describeChild(child);
            runLeaf(testCaseMethodBlock(child), description, notifier);
        }

        @Override
        public void filter(Filter filter) throws NoTestsRemainException {
            super.filter(new NoPhraseFilter(filter));
        }

        private TestClass getDeclaringClass() {
            return super.getTestClass();
        }

        private Statement testCaseMethodBlock(FirstClassTestCaseMethod testCaseMethod) {
            return new Statement() {
                @Override
                public void evaluate() {
                    testCaseMethod.run();
                }
            };
        }

        private static class FirstClassTestCaseMethodComposer {
            private final FrameworkMethod declaringMethod;
            private final TestClass declaringClass;

            public FirstClassTestCaseMethodComposer(
                    FrameworkMethod declaringMethod, TestClass declaringClass) {

                this.declaringMethod = declaringMethod;
                this.declaringClass = declaringClass;
            }

            public Stream<FirstClassTestCaseMethod> compose() throws Throwable {
                Class<?> returnType = declaringMethod.getReturnType();

                if (void.class.isAssignableFrom(returnType)) {
                    return composeNormalTestCaseMethod();
                }

                if (Iterable.class.isAssignableFrom(returnType)) {
                    return composeIterableTestCaseMethods();
                }

                if (Stream.class.isAssignableFrom(returnType)) {
                    return composeStreamTestCaseMethods();
                }

                String message = String.format(
                        "The returned type '%s' isn't supported"
                                + " on FirstClassTestRunner. Void, Iterable<FirstClassTestCase> or"
                                + "  Stream<FirstClassTestCase> types are only supported.",
                        returnType);

                throw new ClassCastException(message);
            }

            private Stream<FirstClassTestCaseMethod> composeNormalTestCaseMethod() {
                return Stream.of(new FirstClassTestCaseMethod(
                        declaringMethod,
                        () -> {
                            try {
                                declaringMethod.invokeExplosively(createDeclaringClassObject());
                            } catch (Throwable throwable) {
                                String message = "Thrown while creating an instance of"
                                        + " the test class.";
                                throw new RuntimeException(
                                        message,
                                        throwable);
                            }
                        }));
            }

            private Stream<FirstClassTestCaseMethod> composeIterableTestCaseMethods()
                    throws Throwable {
                Object obj = declaringMethod
                        .invokeExplosively(createDeclaringClassObject());

                Iterable<?> testCases = (Iterable<?>) obj;

                return StreamSupport
                        .stream(testCases.spliterator(), false)
                        .map(testCase -> {
                            if (testCase instanceof FirstClassTestCase) {
                                return new FirstClassTestCaseMethod(
                                        declaringMethod, (FirstClassTestCase) testCase);
                            } else {
                                return throwClassCastException(
                                        testCases.getClass(), testCase.getClass());
                            }
                        });
            }

            private Stream<FirstClassTestCaseMethod> composeStreamTestCaseMethods()
                    throws Throwable {
                Object obj = declaringMethod
                        .invokeExplosively(createDeclaringClassObject());

                Stream<?> testCases = (Stream<?>) obj;

                return testCases
                        .map(testCase -> {
                            if (testCase instanceof FirstClassTestCase) {
                                return new FirstClassTestCaseMethod(
                                        declaringMethod, (FirstClassTestCase) testCase);
                            } else {
                                return throwClassCastException(
                                        testCases.getClass(), testCase.getClass());
                            }
                        });
            }

            private Object createDeclaringClassObject() throws Exception {
                return declaringClass.getOnlyConstructor().newInstance();
            }

            private FirstClassTestCaseMethod throwClassCastException(
                    Class<?> genericType, Class<?> argumentType) {
                String message = String.format(
                        "The returned type '%s[%s]' isn't supported"
                                + " on FirstClassTestRunner. Void, Iterable<FirstClassTestCase> or"
                                + "  Stream<FirstClassTestCase> types are only supported.",
                        genericType,
                        argumentType);

                throw new ClassCastException(message);
            }
        }
    }

    private static class FirstClassTestCaseMethod {
        private final FrameworkMethod declaringMethod;
        private final FirstClassTestCase testCase;

        public FirstClassTestCaseMethod(
                FrameworkMethod declaringMethod, FirstClassTestCase testCase) {
            this.declaringMethod = declaringMethod;
            this.testCase = testCase;
        }

        public void run() {
            testCase.run();
        }

        public Description createTestDescription() {
            if (testCase instanceof ParametersDisplayable) {
                String displayName = String.format(
                        "%s[%s]",
                        declaringMethod.getName(),
                        ((ParametersDisplayable) testCase).getPhrase());

                return Description.createTestDescription(
                        declaringMethod.getDeclaringClass(),
                        displayName);
            } else {
                return Description.createTestDescription(
                        declaringMethod.getDeclaringClass(),
                        declaringMethod.getName());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FirstClassTestCaseMethod that = (FirstClassTestCaseMethod) o;
            return testCase.equals(that.testCase);
        }

        @Override
        public int hashCode() {
            return Objects.hash(testCase);
        }
    }
}
