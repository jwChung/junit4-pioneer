package com.github.jwchung.junit4pioneer;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FirstClassTestCases {
    public static <U> WithTestDataBuilder<U> with(Iterable<? extends U> testData) {
        return with(StreamSupport.stream(testData.spliterator(), false));
    }

    public static <U> WithTestDataBuilder<U> with(U[] testData) {
        return with(Arrays.stream(testData));
    }

    public static <U> WithTestDataBuilder<U> with(Stream<? extends U> testData) {
        return new WithTestDataBuilder<>(testData);
    }
}
