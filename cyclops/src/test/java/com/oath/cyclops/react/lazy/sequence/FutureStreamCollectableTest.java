package com.oath.cyclops.react.lazy.sequence;

import com.oath.cyclops.types.foldable.Folds;
import cyclops.async.LazyReact;


public class FutureStreamCollectableTest extends CollectableTest {
    @Override
    public <T> Folds<T> of(T... values) {
       return LazyReact.sequentialBuilder().of(values);
    }

}
