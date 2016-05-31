package com.aol.cyclops.control.transformers.seq.foldable;

import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.IterableFoldable;


public class TryTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> IterableFoldable<T> of(T... elements) {
        
        return TryT.fromIterable(ListX.of(elements).map(Try::success));
    }

    

}
