package com.oath.cyclops.streams;

import com.oath.cyclops.types.foldable.Folds;


import cyclops.reactive.ReactiveSeq;
import cyclops.streams.CollectableTest;


public class ReactiveSeqCollectableTest extends CollectableTest {

    @Override
    public <T> Folds<T> of(T... values) {
       return ReactiveSeq.of(values);
    }

}
