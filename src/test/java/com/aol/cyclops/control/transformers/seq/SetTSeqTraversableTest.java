package com.aol.cyclops.control.transformers.seq;

import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class SetTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return SetT.fromIterable(ListX.of(SetX.of(elements)));
    }

    @Override
    public <T> Traversable<T> empty() {
        return SetT.emptySet();
    }

}
