package com.aol.cyclops.control.transformers.seq.foldable;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.Foldable;


public class FutureWTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> Foldable<T> of(T... elements) {
        
        return FutureWT.fromIterable(ListX.of(elements).map(FutureW::ofResult));
    }

   
}
