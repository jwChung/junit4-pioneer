package com.github.jwchung.junit4pioneer;

import java.util.stream.Stream;

public class WithTestDataBuilder<T> {
    private final Stream<? extends T> testData;

    WithTestDataBuilder(Stream<? extends T> testData) {
        this.testData = testData;
    }

    public WithParametersDisplayerBuilder<T> displayParameters(
            ParametersDisplayer<? super T> displayer) {
        return new WithParametersDisplayerBuilder<>(testData, displayer);
    }

    public Stream<FirstClassTestCase> run(FirstClassTestCaseWithParameters<? super T> testCase) {
        return displayParameters(ParametersDisplayer.getEmpty()).run(testCase);
    }
}
