package cyclops.monads.transformers.flowables;


import com.oath.cyclops.anym.transformers.FoldableTransformerSeq;
import cyclops.companion.rx2.Flowables;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.monads.transformers.AbstractNestedFoldableTest;
import cyclops.reactive.FlowableReactiveSeq;


public class StreamTSeqNestedFoldableTest extends AbstractNestedFoldableTest<Witness.list> {

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> of(T... elements) {
        return AnyMs.liftM(FlowableReactiveSeq.just(elements),Witness.list.INSTANCE);
    }

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> empty() {
        return  AnyMs.liftM(FlowableReactiveSeq.<T>empty(),Witness.list.INSTANCE);
    }

}
