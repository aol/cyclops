package cyclops.monads.transformers.flowables;


import com.oath.cyclops.types.foldable.ConvertableSequence;
import cyclops.companion.rx2.Flowables;
import cyclops.companion.rx2.Observables;
import cyclops.monads.Witness;
import cyclops.monads.transformers.AbstractConvertableSequenceTest;


public class StreamTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {

        return Flowables.of(elements).liftM(Witness.list.INSTANCE).to();
    }

    @Override
    public <T> ConvertableSequence<T> empty() {

        return Flowables.<T>empty().liftM(Witness.list.INSTANCE).to();
    }

}
