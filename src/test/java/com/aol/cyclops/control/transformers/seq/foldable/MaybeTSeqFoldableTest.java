package com.aol.cyclops.control.transformers.seq.foldable;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.MaybeT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.Foldable;


public class MaybeTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> Foldable<T> of(T... elements) {
        
        return MaybeT.fromIterable(ListX.of(elements).map(Maybe::just));
    }


}
