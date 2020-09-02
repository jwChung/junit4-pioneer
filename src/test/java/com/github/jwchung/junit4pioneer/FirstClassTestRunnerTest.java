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
    public static class IterableTestCases {
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
    public static class NormalTestCases {
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
    public static class StreamTestCases {
        @Test
        public Stream<FirstClassTestCase> createTestCases() {
            int[] testData = {
                    1, 1
            };

            return Arrays.stream(testData).mapToObj(x -> () ->
                    assertEquals(x, 1));
        }
    }

    @Test
    public void sutCorrectlyRunsIterableFirstClassTestCases() {
        Result result = JUnitCore.runClasses(IterableTestCases.class);
        assertEquals(5, result.getRunCount());
        assertEquals(0, result.getFailureCount());
    }

    @Test
    public void sutCorrectlyRunsVoidTestCases() {
        NormalTestCases.isExecutedRecorder = false;

        Result result = JUnitCore.runClasses(NormalTestCases.class);

        assertEquals(3, result.getRunCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(NormalTestCases.isExecutedRecorder);
    }

    @Test
    public void sutCorrectlyRunsStreamFirstClassTestCases() {
        Result result = JUnitCore.runClasses(StreamTestCases.class);
        assertEquals(2, result.getRunCount());
        assertEquals(0, result.getFailureCount());
    }

    @Test
    public void sutRunsOnlySelectedFirstClassTestCases() {
        JUnitCore junitCore = new JUnitCore();
        Result result = junitCore.run(
                Request.method(IterableTestCases.class, "createIterableTestCases"));
        assertEquals(3, result.getRunCount());
    }
}
