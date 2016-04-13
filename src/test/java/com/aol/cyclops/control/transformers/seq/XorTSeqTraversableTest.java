package com.aol.cyclops.control.transformers.seq;

import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.XorT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class XorTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        
        return XorT.fromIterable(ListX.of(elements).map(Xor::primary));
    }

    @Override
    public <T> Traversable<T> empty() {
        return XorT.emptyList();
    }

}
