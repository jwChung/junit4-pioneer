package com.github.jwchung.junit4pioneer;

import java.util.stream.Stream;

public class WithTestDataBuilder<ParametersT> {
    private final Stream<? extends ParametersT> testData;

    WithTestDataBuilder(Stream<? extends ParametersT> testData) {
        this.testData = testData;
    }

    public WithParametersDisplayerBuilder<ParametersT> displayParameters(
            ParametersDisplayer<? super ParametersT> displayer) {
        return new WithParametersDisplayerBuilder<>(testData, displayer);
    }

    public Stream<FirstClassTestCase> run(
            FirstClassTestCaseWithParameters<? super ParametersT> testCase) {
        return displayParameters(ParametersDisplayer.getEmpty()).run(testCase);
    }
}
