package com.github.jwchung.junit4pioneer;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FirstClassTestCases {
    public static <ParametersT> WithTestDataBuilder<ParametersT> with(
            Iterable<? extends ParametersT> testData) {
        return with(StreamSupport.stream(testData.spliterator(), false));
    }

    public static <ParametersT> WithTestDataBuilder<ParametersT> with(
            ParametersT[] testData) {
        return with(Arrays.stream(testData));
    }

    public static <ParametersT> WithTestDataBuilder<ParametersT> with(
            Stream<? extends ParametersT> testData) {
        return new WithTestDataBuilder<>(testData);
    }
}
