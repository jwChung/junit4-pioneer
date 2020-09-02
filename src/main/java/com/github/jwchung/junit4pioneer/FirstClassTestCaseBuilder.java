package com.github.jwchung.junit4pioneer;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class FirstClassTestCaseBuilder<T> {
    private final Stream<T> testData;

    FirstClassTestCaseBuilder(Stream<T> testData) {
        this.testData = testData;
    }

    public Stream<FirstClassTestCase> build(Consumer<? super T> testCase) {
        return testData.map(x -> () -> testCase.accept(x));
    }
}
