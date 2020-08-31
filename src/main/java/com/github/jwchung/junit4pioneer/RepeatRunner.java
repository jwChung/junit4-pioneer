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
    private final ParentRunner<?> proxyRunner;

    public RepeatRunner(Class<?> klass) throws InitializationError {
        proxyRunner = createProxyRunner(klass);
    }

    @Override
    public Description getDescription() {
        return proxyRunner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        proxyRunner.run(notifier);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        proxyRunner.filter(filter);
    }

    private static ParentRunner<?> createProxyRunner(final Class<?> klass)
            throws InitializationError {
        return new BlockJUnit4ClassRunner(klass) {
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
        };
    }
}
