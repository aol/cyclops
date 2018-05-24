package cyclops.monads.transformers.observables;


import com.oath.cyclops.types.foldable.AbstractConvertableSequenceTest;
import com.oath.cyclops.types.foldable.ConvertableSequence;
import cyclops.companion.rx2.Observables;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.reactive.ObservableReactiveSeq;


public class StreamTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {

        return AnyMs.liftM(ObservableReactiveSeq.of(elements),Witness.list.INSTANCE).to();
    }

    @Override
    public <T> ConvertableSequence<T> empty() {

        return AnyMs.liftM(ObservableReactiveSeq.<T>empty(),Witness.list.INSTANCE).to();
    }

}
