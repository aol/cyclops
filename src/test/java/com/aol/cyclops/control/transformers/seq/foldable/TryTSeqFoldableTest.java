package com.aol.cyclops.control.transformers.seq.foldable;

import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.Foldable;


public class TryTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> Foldable<T> of(T... elements) {
        
        return TryT.fromIterable(ListX.of(elements).map(Try::success));
    }

    

}
