package com.github.jwchung.junit4pioneer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
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
                    .run(x -> assertTrue(x <= 3));
        }

        @Test
        public Stream<FirstClassTestCase> createTestCasesWithStream() {
            Stream<Integer> testData = Stream.of(1, 2);

            return FirstClassTestCases
                    .with(testData)
                    .run(x -> assertTrue(x <= 2));
        }

        @Test
        public Stream<FirstClassTestCase> createTestCasesWithArray() {
            Integer[] testData = {
                    1, 2, 3, 4, 5
            };

            return FirstClassTestCases
                    .with(testData)
                    .run(x -> assertTrue(x <= 5));
        }
    }

    @RunWith(FirstClassTestRunner.class)
    public static class FirstClassTestCasesFormattingTestClass {
        @Test
        public Stream<FirstClassTestCase> createTestCasesWithSimpleDisplayer() {
            Iterable<Integer> testData = Arrays.asList(1, 2, 3);

            return FirstClassTestCases
                    .with(testData)
                    .displayParameters(x -> String.format("value=%s", x))
                    .run(x -> assertTrue(x <= 3));
        }

        @Test
        public Stream<FirstClassTestCase> createTestCasesWithComplexDisplayer() {
            return FirstClassTestCases
                    .with(Arrays.asList(new Object[]{
                            13,
                            "a",
                            new Object() {
                                @Override
                                public String toString() {
                                    return "hello";
                                }
                            }
                    }, new Object[]{
                            23, "b"
                    }))
                    .displayParameters(x -> Arrays.stream(x)
                            .map(Object::toString)
                            .reduce((str1, str2) -> str1 + ", " + str2)
                            .orElse(""))
                    .run(x -> {
                    });
        }

        @Test
        public Stream<FirstClassTestCase> createTestCasesWithoutDisplayer() {
            return FirstClassTestCases
                    .with(new Integer[]{
                            1, 2, 3
                    })
                    .run(x -> assertTrue(x <= 3));
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

    @Test
    public void sutCorrectlyRepresentsSimpleParametersPhrase() {
        // Fixture setup
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });
        String targetTestName = "createTestCasesWithSimpleDisplayer";
        List<String> expected = Arrays.asList(
                "createTestCasesWithSimpleDisplayer[value=1]",
                "createTestCasesWithSimpleDisplayer[value=2]",
                "createTestCasesWithSimpleDisplayer[value=3]");

        // Exercise system
        junitCore.run(Request.method(FirstClassTestCasesFormattingTestClass.class, targetTestName));

        // Verify outcome
        assertThat(executedTestNames, is(expected));
    }

    @Test
    public void sutCorrectlyRepresentsComplexParametersPhrase() {
        // Fixture setup
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });
        String targetTestName = "createTestCasesWithComplexDisplayer";
        List<String> expected = Arrays.asList(
                "createTestCasesWithComplexDisplayer[13, a, hello]",
                "createTestCasesWithComplexDisplayer[23, b]");

        // Exercise system
        junitCore.run(Request.method(
                FirstClassTestCasesFormattingTestClass.class, targetTestName));

        // Verify outcome
        assertThat(executedTestNames, is(expected));
    }

    @Test
    public void sutDoesNotRepresentParametersPhraseWithoutDisplayer() {
        // Fixture setup
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });
        String targetTestName = "createTestCasesWithoutDisplayer";
        List<String> expected = Arrays.asList(
                "createTestCasesWithoutDisplayer",
                "createTestCasesWithoutDisplayer",
                "createTestCasesWithoutDisplayer");

        // Exercise system
        junitCore.run(Request.method(
                FirstClassTestCasesFormattingTestClass.class, targetTestName));

        // Verify outcome
        assertThat(executedTestNames, is(expected));
    }
}
