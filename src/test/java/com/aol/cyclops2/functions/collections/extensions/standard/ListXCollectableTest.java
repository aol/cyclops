package com.aol.cyclops2.functions.collections.extensions.standard;

import org.jooq.lambda.Collectable;

import cyclops.collections.ListX;
import com.aol.cyclops2.react.lazy.sequence.CollectableTest;

public class ListXCollectableTest extends CollectableTest {

    @Override
    public <T> Collectable<T> of(T... values) {
       return  ListX.of(values).collectionOperations();
    }

}
