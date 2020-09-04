package com.github.jwchung.junit4pioneer;

import java.util.stream.Stream;

public class WithParametersDisplayerBuilder<T> {
    private final Stream<? extends T> testData;
    private final ParametersDisplayer<? super T> displayer;

    WithParametersDisplayerBuilder(
            Stream<? extends T> testData, ParametersDisplayer<? super T> displayer) {
        this.testData = testData;
        this.displayer = displayer;
    }

    /**
     * Builds first-class test cases with the given test data.
     *
     * @param testCase A test case with parameters to be run
     *
     * @return The first-class test cases
     */
    public Stream<FirstClassTestCase> run(
            FirstClassTestCaseWithParameters<? super T> testCase) {
        return displayer == ParametersDisplayer.getEmpty()
                ? runWithoutPhrase(testCase)
                : runWithPhrase(testCase);
    }

    private Stream<FirstClassTestCase> runWithoutPhrase(
            FirstClassTestCaseWithParameters<? super T> testCase) {
        return testData.map(parameters -> () -> testCase.run(parameters));
    }

    private Stream<FirstClassTestCase> runWithPhrase(
            FirstClassTestCaseWithParameters<? super T> testCase) {
        return testData.map(parameters -> {
            String phrase = displayer.display(parameters);
            return new ParametersDisplayableTestCase(phrase) {
                @Override
                public void run() {
                    testCase.run(parameters);
                }
            };
        });
    }

    private abstract static class ParametersDisplayableTestCase
            implements FirstClassTestCase, ParametersDisplayable {
        private final String phrase;

        public ParametersDisplayableTestCase(String phrase) {
            this.phrase = phrase;
        }

        @Override
        public String getPhrase() {
            return phrase;
        }

        @Override
        public abstract void run();
    }
}
