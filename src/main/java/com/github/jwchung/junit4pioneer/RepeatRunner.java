package com.github.jwchung.junit4pioneer;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class RepeatRunner extends Runner implements Filterable {
    private final InnerRepeatRunner innerRunner;

    public RepeatRunner(Class<?> klass) throws InitializationError {
        innerRunner = new InnerRepeatRunner(klass);
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

    private static class InnerRepeatRunner extends BlockJUnit4ClassRunner {
        private final ConcurrentHashMap<RepeatMethod, Description> methodDescriptions
                = new ConcurrentHashMap<>();

        public InnerRepeatRunner(Class<?> klass) throws InitializationError {
            super(klass);
        }

        @Override
        protected Description describeChild(FrameworkMethod method) {
            return describeChild((RepeatMethod)method);
        }

        private Description describeChild(RepeatMethod repeatMethod) {
            Description description = methodDescriptions.get(repeatMethod);

            if (description == null) {
                description = repeatMethod.createTestDescription();
                methodDescriptions.putIfAbsent(repeatMethod, description);
            }

            return description;
        }

        @Override
        public void filter(Filter filter) throws NoTestsRemainException {
            super.filter(new NoPhraseFilter(filter));
        }

        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            return computeRepeatMethods();
        }

        private List<FrameworkMethod> computeRepeatMethods() {
            List<FrameworkMethod> repeatMethods = new ArrayList<>();

            Method[] methods = getTestClass().getJavaClass().getDeclaredMethods();

            for (Method method : methods) {
                if (!hasTestAnnotation(method)) {
                    break;
                }

                Repeat repeat = method.getAnnotation(Repeat.class);
                if (repeat == null) {
                    repeatMethods.add(new RepeatMethod(method));
                } else {
                    int repeatValue = repeat.value();
                    NumberFormat repeatNumberFormat = getRepeatNumberFormat(repeatValue);

                    for (int i = 0; i < repeatValue; i++) {
                        repeatMethods.add(new RepeatMethod(method, repeatNumberFormat.format(i)));
                    }
                }
            }

            return Collections.unmodifiableList(repeatMethods);
        }

        private boolean hasTestAnnotation(Method method) {
            return method.getAnnotation(Test.class) != null;
        }

        private NumberFormat getRepeatNumberFormat(int maxRepeatNumber) {
            int maxRepeatNumberDigits =
                    Integer.valueOf(maxRepeatNumber - 1).toString().length();

            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumIntegerDigits(maxRepeatNumberDigits);
            numberFormat.setMinimumIntegerDigits(maxRepeatNumberDigits);
            numberFormat.setGroupingUsed(false);

            return numberFormat;
        }
    }

    private static class RepeatMethod extends FrameworkMethod {
        private static final String EMPTY_REPEAT_NUMBER = "";
        private final Method method;
        private final String repeatNumber;

        public RepeatMethod(Method method) {
            this(method, EMPTY_REPEAT_NUMBER);
        }

        public RepeatMethod(Method method, String repeatNumber) {
            super(method);
            this.method = method;
            this.repeatNumber = repeatNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            if (!super.equals(o)) {
                return false;
            }

            RepeatMethod that = (RepeatMethod) o;

            return method.equals(that.method)
                    && repeatNumber.equals(that.repeatNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), method, repeatNumber);
        }

        public Description createTestDescription() {
            if (repeatNumber.equals(EMPTY_REPEAT_NUMBER)) {
                return Description.createTestDescription(
                        getDeclaringClass(),
                        getName());
            } else {
                String displayName = String.format(
                        "%s[%s]",
                        getName(),
                        repeatNumber);

                return Description.createTestDescription(
                        getDeclaringClass(),
                        displayName);
            }
        }
    }
}
