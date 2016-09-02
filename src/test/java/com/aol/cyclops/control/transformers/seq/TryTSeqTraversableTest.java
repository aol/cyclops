package com.aol.cyclops.control.transformers.seq;

import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class TryTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        ListX<Try<T,Throwable>> list = ListX.<T>of(elements).map(Try::success);
        return TryT.fromIterable(list);
    }

    @Override
    public <T> Traversable<T> empty() {
        return TryT.emptyList();
    }

}
