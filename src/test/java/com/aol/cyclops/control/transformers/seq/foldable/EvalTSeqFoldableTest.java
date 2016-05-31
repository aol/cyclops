package com.aol.cyclops.control.transformers.seq.foldable;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.IterableFoldable;


public class EvalTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> IterableFoldable<T> of(T... elements) {
        
        return EvalT.fromIterable(ListX.of(elements).map(Eval::now));
    }

   
   
}
