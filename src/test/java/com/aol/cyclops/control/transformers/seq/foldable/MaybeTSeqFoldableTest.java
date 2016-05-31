package com.aol.cyclops.control.transformers.seq.foldable;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.MaybeT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.IterableFoldable;


public class MaybeTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> IterableFoldable<T> of(T... elements) {
        
        return MaybeT.fromIterable(ListX.of(elements).map(Maybe::just));
    }


}
