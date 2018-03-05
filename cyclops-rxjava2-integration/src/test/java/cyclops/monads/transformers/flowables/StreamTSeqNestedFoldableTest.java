package cyclops.monads.transformers.flowables;


import com.oath.cyclops.types.anyM.transformers.FoldableTransformerSeq;
import cyclops.companion.rx2.Flowables;
import cyclops.companion.rx2.Observables;
import cyclops.monads.Witness;
import cyclops.monads.transformers.AbstractNestedFoldableTest;


public class StreamTSeqNestedFoldableTest extends AbstractNestedFoldableTest<Witness.list> {

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> of(T... elements) {
        return  Flowables.just(elements).liftM(Witness.list.INSTANCE);
    }

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> empty() {
        return  Flowables.<T>empty().liftM(Witness.list.INSTANCE);
    }

}
