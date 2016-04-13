package com.aol.cyclops.control.transformers.seq;

import java.util.Optional;

import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class OptionalTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        
        return OptionalT.fromIterable(ListX.of(elements).map(Optional::of));
    }

    @Override
    public <T> Traversable<T> empty() {
        return OptionalT.emptyList();
    }

}
