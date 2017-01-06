package com.aol.cyclops2.react.lazy.sequence;

import cyclops.stream.FutureStream;
import org.jooq.lambda.Collectable;

public class FutureStreamCollectableTest extends CollectableTest {
    @Override
    public <T> Collectable<T> of(T... values) {
       return FutureStream.of(values).collectable();
    }

}
