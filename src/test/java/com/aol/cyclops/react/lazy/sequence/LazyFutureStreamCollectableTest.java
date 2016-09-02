package com.aol.cyclops.react.lazy.sequence;

import org.jooq.lambda.Collectable;

import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class LazyFutureStreamCollectableTest extends CollectableTest {
    @Override
    public <T> Collectable<T> of(T... values) {
       return LazyFutureStream.of(values).collectable();
    }

}
