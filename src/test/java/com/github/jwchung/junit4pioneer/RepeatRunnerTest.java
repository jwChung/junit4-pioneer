package com.github.jwchung.junit4pioneer;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;

public class RepeatRunnerTest {
    @RunWith(RepeatRunner.class)
    public static class OneRepeatTestMethodCase {
        @Test
        @Repeat(10)
        public void testMyCode10Times() {
        }
    }

    @RunWith(RepeatRunner.class)
    public static class SeveralRepeatTestMethodsCase {
        @Test
        @Repeat(3)
        public void testMyCode3Times() {
        }

        @Test
        @Repeat(5)
        public void testMyCode5Times() {
        }

        @Test
        @Repeat(7)
        public void testMyCode7Times() {
        }
    }

    @RunWith(RepeatRunner.class)
    public static class RepeatTestMethodWithNormalCase {
        @Test
        @Repeat(3)
        public void testMyCode3Times() {
        }

        @Test
        public void normalTestMethod() {
        }
    }

    @RunWith(RepeatRunner.class)
    public static class ManyTimesRepeatTestCase {
        @Test
        @Repeat(100)
        public void testMyCode100Times() {
        }

        @Test
        @Repeat(110)
        public void testMyCode110Times() {
        }
    }

    @Test
    public void sutSupportsSeveralRepeatTestMethods() {
        // Fixture setup
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });

        // Exercise system
        junitCore.run(SeveralRepeatTestMethodsCase.class);

        // Verify outcome
        assertEquals(15, executedTestNames.size());
        assertNumberOfRepeatTimes(executedTestNames, 3);
        assertNumberOfRepeatTimes(executedTestNames, 5);
        assertNumberOfRepeatTimes(executedTestNames, 7);
    }

    @Test
    public void sutCorrectlyRepeatsTestRunForSpecifiedNumberOfTimes() {
        Result result = JUnitCore.runClasses(OneRepeatTestMethodCase.class);
        assertEquals(10, result.getRunCount());
    }

    @Test
    public void sutCorrectlyRunsNormalTestMethod() {
        // Fixture setup
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });
        String targetTestName = "normalTestMethod";

        // Exercise system
        junitCore.run(Request.method(RepeatTestMethodWithNormalCase.class, targetTestName));

        // Verify outcome
        assertEquals(1, executedTestNames.size());
        assertEquals(targetTestName, executedTestNames.get(0));
    }

    @Test
    public void sutRunsOnlySelectedRepeatMethods() {
        // Fixture setup
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });
        String targetTestName = getTargetTestName(3);

        // Exercise system
        junitCore.run(Request.method(SeveralRepeatTestMethodsCase.class, targetTestName));

        // Verify outcome
        assertEquals(3, executedTestNames.size());
        assertNumberOfRepeatTimes(executedTestNames, 3);
    }

    @Test
    public void sutCorrectlyRepresentsRepeatNumberPhrase() {
        // Fixture setup
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });
        String targetTestName = getTargetTestName(3);
        List<String> expected = Arrays.asList(
                "testMyCode3Times[0]",
                "testMyCode3Times[1]",
                "testMyCode3Times[2]");

        // Exercise system
        junitCore.run(Request.method(SeveralRepeatTestMethodsCase.class, targetTestName));

        // Verify outcome
        assertThat(executedTestNames, is(expected));
    }

    @Test
    public void sutCorrectlyRepresentsRepeatNumbersAlignedRight_Example1() {
        // Fixture setup
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });
        String targetTestName = "testMyCode100Times";
        String[] expected = {
                "testMyCode100Times[01]",
                "testMyCode100Times[99]"
        };

        // Exercise system
        junitCore.run(Request.method(ManyTimesRepeatTestCase.class, targetTestName));

        // Verify outcome
        assertThat(executedTestNames, hasItems(expected));
        assertThat(executedTestNames, not(hasItem("testMyCode100Times[100]")));
    }

    @Test
    public void sutCorrectlyRepresentsRepeatNumbersAlignedRight_Example2() {
        // Fixture setup
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });
        String targetTestName = "testMyCode110Times";
        String[] expected = {
                "testMyCode110Times[000]",
                "testMyCode110Times[109]"
        };

        // Exercise system
        junitCore.run(Request.method(ManyTimesRepeatTestCase.class, targetTestName));

        // Verify outcome
        assertThat(executedTestNames, hasItems(expected));
    }

    private void assertNumberOfRepeatTimes(
            List<String> executedTestNames, int expectedNumberOfRepeatTimes) {
        long actual = getNumberOfRepeatTimes(
                executedTestNames, getTargetTestName(expectedNumberOfRepeatTimes));

        assertEquals(expectedNumberOfRepeatTimes, actual);
    }

    private String getTargetTestName(int repeat) {
        return String.format("testMyCode%sTimes", repeat);
    }

    private long getNumberOfRepeatTimes(List<String> executedTestNames, String targetTestName) {
        return executedTestNames
                .stream()
                .filter(x -> x.startsWith(targetTestName))
                .count();
    }
}
