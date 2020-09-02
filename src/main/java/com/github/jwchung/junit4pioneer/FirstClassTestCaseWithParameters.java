package com.github.jwchung.junit4pioneer;

@FunctionalInterface
public interface FirstClassTestCaseWithParameters<T> {
    void run(T parameters);
}
