package com.github.jwchung.junit4pioneer;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

class NoPhraseFilter extends Filter {
    private final Filter innerFilter;

    public NoPhraseFilter(Filter innerFilter) {
        this.innerFilter = innerFilter;
    }

    @Override
    public boolean shouldRun(Description description) {
        return innerFilter.shouldRun(
                getDescriptionWithoutPhrase(description));
    }

    @Override
    public String describe() {
        return innerFilter.describe();
    }

    private Description getDescriptionWithoutPhrase(Description description) {
        return Description.createTestDescription(
                description.getTestClass(),
                getMethodNameWithoutPhrase(description));
    }

    private String getMethodNameWithoutPhrase(Description description) {
        int phraseStartIndex = description.getMethodName().indexOf('[');

        if (phraseStartIndex < 0) {
            return description.getMethodName();
        }

        return description.getMethodName().substring(0, phraseStartIndex);
    }
}
