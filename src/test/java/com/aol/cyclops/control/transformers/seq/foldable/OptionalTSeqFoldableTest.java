package com.aol.cyclops.control.transformers.seq.foldable;

import java.util.Optional;

import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.Foldable;


public class OptionalTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> Foldable<T> of(T... elements) {
        
        return OptionalT.fromIterable(ListX.of(elements).map(Optional::of));
    }

  

}
