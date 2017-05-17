package com.aol.cyclops2.streams;

import org.jooq.lambda.Collectable;

import cyclops.stream.ReactiveSeq;
import com.aol.cyclops2.react.lazy.sequence.CollectableTest;

public class ReactiveSeqCollectableTest extends CollectableTest {

    @Override
    public <T> Collectable<T> of(T... values) {
       return ReactiveSeq.of(values).collectionOperations();
    }

}
