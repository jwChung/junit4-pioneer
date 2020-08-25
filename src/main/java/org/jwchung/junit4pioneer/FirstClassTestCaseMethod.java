package org.jwchung.junit4pioneer;

import org.junit.runners.model.FrameworkMethod;

/**
 * Represents a first-class test case.
 */
public class FirstClassTestCaseMethod {
    private final FrameworkMethod declaringMethod;
    private final FirstClassTestCase testCase;

    public FirstClassTestCaseMethod(FrameworkMethod declaringMethod, FirstClassTestCase testCase) {
        this.declaringMethod = declaringMethod;
        this.testCase = testCase;
    }

    public FrameworkMethod getDeclaringMethod() {
        return declaringMethod;
    }

    public void invoke() {
        testCase.invoke();
    }
}
