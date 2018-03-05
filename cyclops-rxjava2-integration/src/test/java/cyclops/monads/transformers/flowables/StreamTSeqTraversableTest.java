package cyclops.monads.transformers.flowables;

import com.oath.cyclops.types.AbstractTraversableTest;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.companion.rx2.Flowables;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.transformers.StreamT;
import cyclops.reactive.collections.mutable.ListX;
import io.reactivex.Flowable;
import org.junit.Test;


public class StreamTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return AnyMs.liftM(Flowables.of(elements),Witness.reactiveSeq.ITERATIVE);
    }

    @Override
    public <T> Traversable<T> empty() {

        return AnyMs.liftM(Flowables.<T>empty(),Witness.reactiveSeq.ITERATIVE);
    }

    @Test
    public void conversion(){
        StreamT<list,Integer> trans = AnyMs.liftM(Flowables.just(1,2,3),list.INSTANCE);

        ListX<Flowable<Integer>> listObs = Witness.list(trans.unwrapTo(Flowables::fromStream));

    }

}
