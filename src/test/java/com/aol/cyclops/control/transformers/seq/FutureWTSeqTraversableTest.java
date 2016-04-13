package com.aol.cyclops.control.transformers.seq;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.control.monads.transformers.MaybeT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class FutureWTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        
        return FutureWT.fromIterable(ListX.of(elements).map(FutureW::ofResult));
    }

    @Override
    public <T> Traversable<T> empty() {
        return FutureWT.emptyList();
    }

}
