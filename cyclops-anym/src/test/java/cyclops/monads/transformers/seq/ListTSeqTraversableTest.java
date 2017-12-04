package cyclops.monads.transformers.seq;

import com.oath.cyclops.types.AbstractTraversableTest;
import cyclops.collections.mutable.ListX;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.reactive.ReactiveSeq;


public class ListTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return ListX.of(elements)
                    .to(AnyMs::<list,T>liftM)
                     .apply(list.INSTANCE);
    }

    @Override
    public <T> Traversable<T> empty() {
      return ListX.<T>empty()
                  .to(AnyMs::<list,T>liftM)
                  .apply(list.INSTANCE);

    }

}
