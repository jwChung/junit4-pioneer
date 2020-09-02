package com.github.jwchung.junit4pioneer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class FirstClassTestRunnerTest {
    @RunWith(FirstClassTestRunner.class)
    public static class IterableTestClass {
        @Test
        public Iterable<FirstClassTestCase> createIterableTestCases() {
            int[] testData = {
                    1, 1, 1
            };

            return Arrays.stream(testData).mapToObj(x -> (FirstClassTestCase) () ->
                    assertEquals(x, 1)).collect(Collectors.toList());
        }

        @Test
        public List<FirstClassTestCase> createListTestCases() {
            int[] testData = {
                    2, 2
            };

            return Arrays.stream(testData).mapToObj(x -> (FirstClassTestCase) () ->
                    assertEquals(x, 2)).collect(Collectors.toList());
        }
    }

    @RunWith(FirstClassTestRunner.class)
    public static class NormalTestClass {
        private static Boolean isExecutedRecorder = false;

        @Test
        public Iterable<FirstClassTestCase> createTestCases() {
            int[] testData = {
                    1, 1
            };

            return Arrays.stream(testData).mapToObj(x -> (FirstClassTestCase) () ->
                    assertEquals(x, 1)).collect(Collectors.toList());
        }

        @Test
        public void voidTestCase() {
            isExecutedRecorder = true;
        }
    }

    @RunWith(FirstClassTestRunner.class)
    public static class StreamTestClass {
        @Test
        public Stream<FirstClassTestCase> createTestCases() {
            int[] testData = {
                    1, 1
            };

            return Arrays.stream(testData).mapToObj(x -> () ->
                    assertEquals(x, 1));
        }
    }

    @RunWith(FirstClassTestRunner.class)
    public static class FirstClassTestCasesTestClass {
        @Test
        public Stream<FirstClassTestCase> createTestCasesWithIterable() {
            Iterable<Integer> testData = Arrays.asList(1, 2, 3);

            return FirstClassTestCases
                    .with(testData)
                    .build(x -> assertTrue(x <= 3));
        }

        @Test
        public Stream<FirstClassTestCase> createTestCasesWithStream() {
            Stream<Integer> testData = Stream.of(1, 2);

            return FirstClassTestCases
                    .with(testData)
                    .build(x -> assertTrue(x <= 2));
        }

        @Test
        public Stream<FirstClassTestCase> createTestCasesWithArray() {
            Integer[] testData = {
                    1, 2, 3, 4, 5
            };

            return FirstClassTestCases
                    .with(testData)
                    .build(x -> assertTrue(x <= 5));
        }
    }

    @Test
    public void sutCorrectlyRunsIterableTestCases() {
        Result result = JUnitCore.runClasses(IterableTestClass.class);
        assertEquals(5, result.getRunCount());
        assertEquals(0, result.getFailureCount());
    }

    @Test
    public void sutCorrectlyRunsVoidTestCases() {
        NormalTestClass.isExecutedRecorder = false;

        Result result = JUnitCore.runClasses(NormalTestClass.class);

        assertEquals(3, result.getRunCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(NormalTestClass.isExecutedRecorder);
    }

    @Test
    public void sutCorrectlyRunsStreamTestCases() {
        Result result = JUnitCore.runClasses(StreamTestClass.class);
        assertEquals(2, result.getRunCount());
        assertEquals(0, result.getFailureCount());
    }

    @Test
    public void sutRunsOnlySelectedTestCases() {
        JUnitCore junitCore = new JUnitCore();
        Result result = junitCore.run(
                Request.method(IterableTestClass.class, "createIterableTestCases"));
        assertEquals(3, result.getRunCount());
    }

    @Test
    public void sutCorrectlyRunsTestCasesInitializedByFirstClassTestCases() {
        Result result = JUnitCore.runClasses(FirstClassTestCasesTestClass.class);
        assertEquals(10, result.getRunCount());
        assertEquals(0, result.getFailureCount());
    }
}
