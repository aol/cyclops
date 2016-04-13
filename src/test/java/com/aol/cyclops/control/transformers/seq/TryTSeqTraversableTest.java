package com.aol.cyclops.control.transformers.seq;

import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class TryTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        
        return TryT.fromIterable(ListX.of(elements).map(Try::success));
    }

    @Override
    public <T> Traversable<T> empty() {
        return TryT.emptyList();
    }

}
