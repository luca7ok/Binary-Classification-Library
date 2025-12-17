package com.domain;

import java.util.List;

public class Instance<F, L> {
    private final List<F> input;
    private final L output;

    public Instance(List<F> input, L output) {
        this.input = input;
        this.output = output;
    }

    public L getOutput() {
        return output;
    }

    public List<F> getInput() {
        return input;
    }

    @Override
    public String toString() {
        return "Instance{input=" + input + ", output=" + output + "}";
    }
}
