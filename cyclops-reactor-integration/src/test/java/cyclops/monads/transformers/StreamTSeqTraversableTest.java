package cyclops.monads.transformers;

import com.oath.cyclops.types.AbstractTraversableTest;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.companion.reactor.Fluxs;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.reactive.FluxReactiveSeq;


public class StreamTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return AnyMs.liftM(FluxReactiveSeq.of(elements), Witness.reactiveSeq.ITERATIVE);
    }

    @Override
    public <T> Traversable<T> empty() {
        return AnyMs.liftM(FluxReactiveSeq.empty(), Witness.reactiveSeq.ITERATIVE);

    }

}
