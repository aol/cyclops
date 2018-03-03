package cyclops.monads.transformers;


import com.oath.cyclops.types.foldable.ConvertableSequence;
import cyclops.companion.reactor.Fluxs;
import cyclops.monads.Witness;
import cyclops.reactive.ReactiveSeq;


public class StreamTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {

        return Fluxs.of(elements).liftM(Witness.list.INSTANCE).to();
    }

    @Override
    public <T> ConvertableSequence<T> empty() {

        return Fluxs.<T>empty().liftM(Witness.list.INSTANCE).to();
    }

}
