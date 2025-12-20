package com.models;

import com.domain.Instance;

import java.io.Serializable;
import java.util.List;

public interface Model<F, L> extends Serializable {
    void train(List<Instance<F, L>> instances);

    List<Double> test(List<Instance<F, L>> instances);
}
