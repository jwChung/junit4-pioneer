package com.github.jwchung.junit4pioneer;

import java.util.stream.Stream;

public class FirstClassTestCasesWithParameters<T> {
    private final Stream<? extends T> testData;

    FirstClassTestCasesWithParameters(Stream<? extends T> testData) {
        this.testData = testData;
    }

    public FirstClassTestCasesWithDisplayer<T> displayParameters(
            ParametersDisplayer<? super T> displayer) {
        return new FirstClassTestCasesWithDisplayer<>(testData, displayer);
    }

    public Stream<FirstClassTestCase> run(FirstClassTestCaseWithParameters<? super T> testCase) {
        return displayParameters(ParametersDisplayer.getEmpty()).run(testCase);
    }
}
