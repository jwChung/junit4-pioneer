package org.jwchung.junit4pioneer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

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
        private static final List<String> executedTestNames = new ArrayList<>();

        public static List<String> getExecutedTestNames() {
            return executedTestNames;
        }

        public static void clearExecutedTestNames() {
            getExecutedTestNames().clear();
        }

        private static String getCurrentMethod() {
            return Thread
                    .currentThread()
                    .getStackTrace()[1 + 1]
                    .getMethodName();
        }

        @Test
        @Repeat(3)
        public void testMyCode3Times() {
            getExecutedTestNames().add(getCurrentMethod());
        }

        @Test
        @Repeat(5)
        public void testMyCode5Times() {
            getExecutedTestNames().add(getCurrentMethod());
        }

        @Test
        @Repeat(7)
        public void testMyCode7Times() {
            getExecutedTestNames().add(getCurrentMethod());
        }
    }

    @Test
    public void sutCorrectlyRepeatsSeveralRepeatTestMethods() {
        SeveralRepeatTestMethodsCase.clearExecutedTestNames();

        JUnitCore.runClasses(SeveralRepeatTestMethodsCase.class);

        List<String> executedTestNames = SeveralRepeatTestMethodsCase.getExecutedTestNames();
        assertEquals(15, executedTestNames.size());
        assertEquals(3, getRepeat(executedTestNames, getTargetTestName(3)));
        assertEquals(5, getRepeat(executedTestNames, getTargetTestName(5)));
        assertEquals(7, getRepeat(executedTestNames, getTargetTestName(7)));
    }

    private String getTargetTestName(int repeat) {
        return String.format("testMyCode%sTimes", repeat);
    }

    @RunWith(RepeatRunner.class)
    public static class RepeatTestMethodWithNormalCase {
        private static final List<String> executedTestNames = new ArrayList<>();

        public static List<String> getExecutedTestNames() {
            return executedTestNames;
        }

        public static void clearExecutedTestNames() {
            getExecutedTestNames().clear();
        }

        private static String getCurrentMethod() {
            return Thread
                    .currentThread()
                    .getStackTrace()[1 + 1]
                    .getMethodName();
        }

        @Test
        @Repeat(3)
        public void testMyCode3Times() {
            getExecutedTestNames().add(getCurrentMethod());
        }

        @Test
        public void normalTestMethod() {
            getExecutedTestNames().add(getCurrentMethod());
        }
    }

    @Test
    public void sutCorrectlyRunsNormalTestMethod() {
        RepeatTestMethodWithNormalCase.clearExecutedTestNames();

        JUnitCore.runClasses(RepeatTestMethodWithNormalCase.class);

        List<String> executedTestNames = RepeatTestMethodWithNormalCase.getExecutedTestNames();
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
