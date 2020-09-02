package com.github.jwchung.junit4pioneer;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FirstClassTestCases {
    public static <T> FirstClassTestCaseBuilder<T> with(Iterable<T> testData) {
        return with(StreamSupport.stream(testData.spliterator(), false));
    }

    public static <T> FirstClassTestCaseBuilder<T> with(T[] testData) {
        return with(Arrays.stream(testData));
    }

    public static <T> FirstClassTestCaseBuilder<T> with(Stream<T> testData) {
        return new FirstClassTestCaseBuilder<>(testData);
    }
}
