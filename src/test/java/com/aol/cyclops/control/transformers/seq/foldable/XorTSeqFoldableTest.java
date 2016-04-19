package com.aol.cyclops.control.transformers.seq.foldable;

import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.XorT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.Foldable;


public class XorTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> Foldable<T> of(T... elements) {
        
        return XorT.fromIterable(ListX.of(elements).map(Xor::primary));
    }

    

}
