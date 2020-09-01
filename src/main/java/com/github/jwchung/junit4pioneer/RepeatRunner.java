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
    private final RepeatInnerRunner innerRunner;

    public RepeatRunner(Class<?> klass) throws InitializationError {
        innerRunner = new RepeatInnerRunner(klass);
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

    private static class RepeatInnerRunner extends BlockJUnit4ClassRunner {
        private final ConcurrentHashMap<RepeatMethod, Description> methodDescriptions
                = new ConcurrentHashMap<>();

        public RepeatInnerRunner(Class<?> klass) throws InitializationError {
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
            super.filter(new RepeatFilter(filter));
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
                    NumberFormat maxRepeatNumberFormat = getMaxRepeatNumberFormat(repeatValue);

                    for (int i = 0; i < repeatValue; i++) {
                        repeatMethods.add(new RepeatMethod(method, i, maxRepeatNumberFormat));
                    }
                }
            }

            return Collections.unmodifiableList(repeatMethods);
        }

        private boolean hasTestAnnotation(Method method) {
            return method.getAnnotation(Test.class) != null;
        }

        private NumberFormat getMaxRepeatNumberFormat(int maxRepeatNumber) {
            int maxRepeatNumberDigits =
                    Integer.valueOf(maxRepeatNumber - 1).toString().length();

            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumIntegerDigits(maxRepeatNumberDigits);
            numberFormat.setMinimumIntegerDigits(maxRepeatNumberDigits);
            numberFormat.setGroupingUsed(false);

            return numberFormat;
        }
    }

    private static class RepeatFilter extends Filter {
        private final Filter innerFilter;

        public RepeatFilter(Filter innerFilter) {
            this.innerFilter = innerFilter;
        }

        @Override
        public boolean shouldRun(Description description) {
            return innerFilter.shouldRun(
                    getDescriptionWithoutRepeatNumberPhrase(description));
        }

        @Override
        public String describe() {
            return innerFilter.describe();
        }

        private Description getDescriptionWithoutRepeatNumberPhrase(Description description) {
            return Description.createTestDescription(
                    description.getTestClass(),
                    getMethodNameWithoutRepeatNumberPhrase(description));
        }

        private String getMethodNameWithoutRepeatNumberPhrase(Description description) {
            int phraseStartIndex = description.getMethodName().indexOf('[');

            if (phraseStartIndex < 0) {
                return description.getMethodName();
            }

            return description.getMethodName().substring(0, phraseStartIndex);
        }
    }

    private static class RepeatMethod extends FrameworkMethod {
        private final int repeatNumber;
        private final NumberFormat maxRepeatNumberFormat;

        public RepeatMethod(Method method) {
            this(method, 0, null);
        }

        public RepeatMethod(Method method, int repeatNumber, NumberFormat maxRepeatNumberFormat) {
            super(method);
            this.repeatNumber = repeatNumber;
            this.maxRepeatNumberFormat = maxRepeatNumberFormat;
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

            return repeatNumber == that.repeatNumber
                    && getMethod().equals(that.getMethod());
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), getMethod(), repeatNumber);
        }

        public Description createTestDescription() {
            if (maxRepeatNumberFormat == null) {
                return Description.createTestDescription(
                        getDeclaringClass(),
                        getName());
            } else {
                String repeatNumberAlignedRight = maxRepeatNumberFormat.format(repeatNumber);

                String displayName = String.format(
                        "%s[%s]",
                        getName(),
                        repeatNumberAlignedRight);

                return Description.createTestDescription(
                        getDeclaringClass(),
                        displayName);
            }
        }
    }
}
