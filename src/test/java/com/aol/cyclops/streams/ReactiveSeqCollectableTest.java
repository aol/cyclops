package com.aol.cyclops.streams;

import org.jooq.lambda.Collectable;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.react.lazy.sequence.CollectableTest;

public class ReactiveSeqCollectableTest extends CollectableTest {

    @Override
    public <T> Collectable<T> of(T... values) {
       return ReactiveSeq.of(values);
    }

}
