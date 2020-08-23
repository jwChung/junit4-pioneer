package org.jwchung.junit4pioneer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

public class RepeatRunnerTest {

    @RunWith(RepeatRunner.class)
    public static class MyTestClass {
        @Test
        @Repeat(10)
        public void testMyCode10Times() {
        }
    }

    @Test
    public void sutCorrectlyRepeatsTestRunForSpecifiedNumberOfTimes() {
        Result result = JUnitCore.runClasses(MyTestClass.class);
        assertEquals(10, result.getRunCount());
    }
}
