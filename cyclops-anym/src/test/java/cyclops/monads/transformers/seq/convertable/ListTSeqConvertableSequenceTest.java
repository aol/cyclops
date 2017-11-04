package cyclops.monads.transformers.seq.convertable;

import com.oath.cyclops.types.AbstractConvertableSequenceTest;
import com.oath.cyclops.types.foldable.ConvertableSequence;
import cyclops.collections.mutable.ListX;


public class ListTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        return ListX.of(elements).liftM(Witness.list.INSTANCE).to();
    }

    @Override
    public <T> ConvertableSequence<T> empty() {

        return ListX.<T>empty().liftM(Witness.list.INSTANCE).to();
    }

}
