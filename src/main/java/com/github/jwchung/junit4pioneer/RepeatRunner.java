package com.github.jwchung.junit4pioneer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class RepeatRunner extends Runner implements Filterable {
    private final ParentRunner<?> realRunner;

    public RepeatRunner(Class<?> klass) throws InitializationError {
        realRunner = new RealRunner(klass);
    }

    @Override
    public Description getDescription() {
        return realRunner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        realRunner.run(notifier);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        realRunner.filter(filter);
    }

    private static class RealRunner extends BlockJUnit4ClassRunner {
        public RealRunner(Class<?> klass) throws InitializationError {
            super(klass);
        }

        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            return computeRepeatMethods();
        }

        private List<FrameworkMethod> computeRepeatMethods() {
            List<FrameworkMethod> repeatMethods = new ArrayList<>();

            for (FrameworkMethod method : super.computeTestMethods()) {
                Repeat repeat = method.getAnnotation(Repeat.class);

                int repeatValue = repeat == null ? 1 : repeat.value();

                for (int i = 0; i < repeatValue; i++) {
                    repeatMethods.add(method);
                }
            }

            return Collections.unmodifiableList(repeatMethods);
        }
    }
}
