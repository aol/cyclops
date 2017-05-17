package com.aol.cyclops2.react.lazy.sequence;

import cyclops.async.LazyReact;
import org.jooq.lambda.Collectable;

public class FutureStreamCollectableTest extends CollectableTest {
    @Override
    public <T> Collectable<T> of(T... values) {
       return LazyReact.sequentialBuilder().of(values).statisticalOperations();
    }

}
