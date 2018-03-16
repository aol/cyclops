package cyclops.monads.transformers.seq.nestedfoldable;

import cyclops.reactive.collections.mutable.ListX;

import com.oath.cyclops.anym.transformers.FoldableTransformerSeq;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.monads.transformers.AbstractNestedFoldableTest;


public class ListTSeqNestedFoldableTest extends AbstractNestedFoldableTest<Witness.list> {

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> of(T... elements) {
      return ListX.of(elements)
                  .to(AnyMs::<Witness.list,T>liftM)
                 .apply(Witness.list.INSTANCE);
    }

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> empty() {
      return ListX.<T>empty()
                  .to(AnyMs::<Witness.list,T>liftM)
                  .apply(Witness.list.INSTANCE);
    }

}
