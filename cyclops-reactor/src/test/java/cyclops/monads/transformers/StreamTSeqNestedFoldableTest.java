package cyclops.monads.transformers;


import com.oath.cyclops.types.anyM.transformers.FoldableTransformerSeq;
import cyclops.companion.reactor.Fluxs;
import cyclops.monads.Witness;
import cyclops.reactive.ReactiveSeq;


public class StreamTSeqNestedFoldableTest extends AbstractNestedFoldableTest<Witness.list> {

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> of(T... elements) {
        return  Fluxs.of(elements).liftM(Witness.list.INSTANCE);
    }

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> empty() {
        return  Fluxs.<T>empty().liftM(Witness.list.INSTANCE);
    }

}
