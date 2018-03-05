package cyclops.monads.transformers.flowables;


import com.oath.anym.transformers.FoldableTransformerSeq;
import cyclops.companion.rx2.Flowables;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.monads.transformers.AbstractNestedFoldableTest;


public class StreamTSeqNestedFoldableTest extends AbstractNestedFoldableTest<Witness.list> {

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> of(T... elements) {
        return AnyMs.liftM(Flowables.just(elements),Witness.list.INSTANCE);
    }

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> empty() {
        return  AnyMs.liftM(Flowables.<T>empty(),Witness.list.INSTANCE);
    }

}
