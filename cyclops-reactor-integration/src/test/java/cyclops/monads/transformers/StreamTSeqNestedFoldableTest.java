package cyclops.monads.transformers;


import com.oath.cyclops.anym.transformers.FoldableTransformerSeq;
import cyclops.companion.reactor.Fluxs;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness.list;


public class StreamTSeqNestedFoldableTest extends AbstractNestedFoldableTest<list> {

    @Override
    public <T> FoldableTransformerSeq<list,T> of(T... elements) {
        return AnyMs.liftM(Fluxs.of(elements), list.INSTANCE);
    }

    @Override
    public <T> FoldableTransformerSeq<list,T> empty() {
        return AnyMs.liftM(Fluxs.empty(), list.INSTANCE);
    }

}
