package cyclops.monads.transformers.seq.nestedfoldable;


import com.oath.cyclops.anym.transformers.FoldableTransformerSeq;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.monads.transformers.AbstractNestedFoldableTest;
import cyclops.reactive.ReactiveSeq;


public class StreamTSeqNestedFoldableTest extends AbstractNestedFoldableTest<Witness.list> {

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> of(T... elements) {
        return  ReactiveSeq.of(elements)
                          .to(AnyMs::<Witness.list,T>liftM)
                          .apply(Witness.list.INSTANCE);
    }

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> empty() {
        return  ReactiveSeq.<T>empty()
                          .to(AnyMs::<Witness.list,T>liftM)
                          .apply(Witness.list.INSTANCE);
    }

}
