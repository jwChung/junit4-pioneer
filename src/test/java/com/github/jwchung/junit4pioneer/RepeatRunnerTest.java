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

    @Test
    public void sutCorrectlyRepeatsTestRunForSpecifiedNumberOfTimes() {
        Result result = JUnitCore.runClasses(OneRepeatTestMethodCase.class);
        assertEquals(10, result.getRunCount());
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

    @Test
    public void sutCorrectlyRepeatsSeveralRepeatTestMethods() {
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });

        junitCore.run(SeveralRepeatTestMethodsCase.class);

        assertEquals(15, executedTestNames.size());
        assertEquals(3, getRepeat(executedTestNames, getTargetTestName(3)));
        assertEquals(5, getRepeat(executedTestNames, getTargetTestName(5)));
        assertEquals(7, getRepeat(executedTestNames, getTargetTestName(7)));
    }

    @Test
    public void sutRunsOnlySelectedRepeatMethods() {
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });
        String targetTestName = getTargetTestName(3);

        junitCore.run(Request.method(SeveralRepeatTestMethodsCase.class, targetTestName));

        assertEquals(3, executedTestNames.size());
        assertEquals(3, getRepeat(executedTestNames, targetTestName));
    }

    private String getTargetTestName(int repeat) {
        return String.format("testMyCode%sTimes", repeat);
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
    public void sutCorrectlyRunsNormalTestMethod() {
        List<String> executedTestNames = new ArrayList<>();
        JUnitCore junitCore = new JUnitCore();
        junitCore.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                executedTestNames.add(description.getMethodName());
            }
        });

        junitCore.run(RepeatTestMethodWithNormalCase.class);

        assertEquals(4, executedTestNames.size());
        assertEquals(1, getRepeat(executedTestNames, "normalTestMethod"));
    }

    private long getRepeat(List<String> executedTestNames, String targetTestName) {
        return executedTestNames
                .stream()
                .filter(x -> x.equals(targetTestName))
                .count();
    }
}
