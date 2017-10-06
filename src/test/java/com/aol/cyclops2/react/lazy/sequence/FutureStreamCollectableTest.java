package com.aol.cyclops2.react.lazy.sequence;

import com.aol.cyclops2.types.foldable.Folds;
import cyclops.async.LazyReact;


public class FutureStreamCollectableTest extends CollectableTest {
    @Override
    public <T> Folds<T> of(T... values) {
       return LazyReact.sequentialBuilder().of(values);
    }

}
