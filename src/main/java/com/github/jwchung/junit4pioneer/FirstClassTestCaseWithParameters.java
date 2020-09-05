package com.github.jwchung.junit4pioneer;

@FunctionalInterface
public interface FirstClassTestCaseWithParameters<ParametersT> {
    void run(ParametersT parameters);
}
