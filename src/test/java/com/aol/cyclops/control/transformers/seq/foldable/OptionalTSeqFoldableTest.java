package com.aol.cyclops.control.transformers.seq.foldable;

import java.util.Optional;

import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.IterableFoldable;


public class OptionalTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> IterableFoldable<T> of(T... elements) {
        
        return OptionalT.fromIterable(ListX.of(elements).map(Optional::of));
    }

  

}
