package com.github.jwchung.junit4pioneer;

@FunctionalInterface
public interface ParametersDisplayer<T> {
    String display(T parameters);

    /**
     * Represents the empty displayer.
     *
     * @param <U> The type of parameters
     *
     * @return The empty displayer
     */
    static <U> ParametersDisplayer<U> getEmpty() {
        return parameters -> {
            throw new UnsupportedOperationException();
        };
    }
}