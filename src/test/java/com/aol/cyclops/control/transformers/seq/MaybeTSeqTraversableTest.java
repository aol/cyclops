package com.aol.cyclops.control.transformers.seq;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.MaybeT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class MaybeTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        
        return MaybeT.fromIterable(ListX.of(elements).map(Maybe::just));
    }

    @Override
    public <T> Traversable<T> empty() {
        return MaybeT.emptyList();
    }

}
