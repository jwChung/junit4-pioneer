package org.jwchung.junit4pioneer;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepeatRunner extends BlockJUnit4ClassRunner {

    public RepeatRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        return computeRepeatMethods();
    }

    private List<FrameworkMethod> computeRepeatMethods() {
        List<FrameworkMethod> repeatMethods = new ArrayList<>();

        for (FrameworkMethod method: super.computeTestMethods()) {
            Repeat repeat = method.getAnnotation(Repeat.class);
            for (int i = 0; i < repeat.value(); i++) {
                repeatMethods.add(method);
            }
        }

        return Collections.unmodifiableList(repeatMethods);
    }
}
