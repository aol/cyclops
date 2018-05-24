package cyclops.monads.transformers.observables;

import com.oath.cyclops.types.AbstractTraversableTest;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.companion.rx2.Observables;
import cyclops.monads.AnyMs;
import cyclops.monads.ObservableAnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.transformers.StreamT;
import cyclops.reactive.ObservableReactiveSeq;
import cyclops.reactive.collections.mutable.ListX;
import io.reactivex.Observable;
import org.junit.Test;



public class StreamTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return AnyMs.liftM(ObservableReactiveSeq.of(elements),Witness.reactiveSeq.ITERATIVE);
    }

    @Override
    public <T> Traversable<T> empty() {

        return AnyMs.liftM(ObservableReactiveSeq.<T>empty(),Witness.reactiveSeq.ITERATIVE);
    }

    @Test
    public void conversion(){
        StreamT<list,Integer> trans = AnyMs.liftM(ObservableReactiveSeq.just(1,2,3),list.INSTANCE);

        ListX<Observable<Integer>> listObs = Witness.list(trans.unwrapTo(ObservableAnyM::fromStream));

    }

}
