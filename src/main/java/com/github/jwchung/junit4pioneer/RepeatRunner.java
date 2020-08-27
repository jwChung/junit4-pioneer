package com.github.jwchung.junit4pioneer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class RepeatRunner extends Runner {
    private final Class<?> klass;

    public RepeatRunner(Class<?> klass) {
        this.klass = klass;
    }

    @Override
    public Description getDescription() {
        return createProxyRunner().getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        createProxyRunner().run(notifier);
    }

    private Runner createProxyRunner() {
        try {
            return createProxyRunnerUnsafely();
        } catch (InitializationError initializationError) {
            String message = "Error occurred when initializing an instance of "
                    + "the BlockJUnit4ClassRunner type.";

            throw new RuntimeException(message, initializationError);
        }
    }

    private BlockJUnit4ClassRunner createProxyRunnerUnsafely() throws InitializationError {
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
