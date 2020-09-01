package com.github.jwchung.junit4pioneer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
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
        assertNumberOfTimesToRun(executedTestNames, 3);
        assertNumberOfTimesToRun(executedTestNames, 5);
        assertNumberOfTimesToRun(executedTestNames, 7);
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

        // Exercise system
        junitCore.run(RepeatTestMethodWithNormalCase.class);

        // Verify outcome
        assertEquals(4, executedTestNames.size());
        assertEquals(1, getNumberOfTimes(executedTestNames, "normalTestMethod"));
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
        assertNumberOfTimesToRun(executedTestNames, 3);
    }

    private void assertNumberOfTimesToRun(
            List<String> executedTestNames, int expectedNumberOfTimes) {
        long actual = getNumberOfTimes(
                executedTestNames, getTargetTestName(expectedNumberOfTimes));

        assertEquals(expectedNumberOfTimes, actual);
    }

    private String getTargetTestName(int repeat) {
        return String.format("testMyCode%sTimes", repeat);
    }

    private long getNumberOfTimes(List<String> executedTestNames, String targetTestName) {
        return executedTestNames
                .stream()
                .filter(x -> x.equals(targetTestName))
                .count();
    }
}
