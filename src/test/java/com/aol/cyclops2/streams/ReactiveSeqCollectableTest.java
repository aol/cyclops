package com.aol.cyclops2.streams;

import com.aol.cyclops2.types.foldable.Folds;


import cyclops.reactive.ReactiveSeq;
import com.aol.cyclops2.react.lazy.sequence.CollectableTest;

public class ReactiveSeqCollectableTest extends CollectableTest {

    @Override
    public <T> Folds<T> of(T... values) {
       return ReactiveSeq.of(values);
    }

}
