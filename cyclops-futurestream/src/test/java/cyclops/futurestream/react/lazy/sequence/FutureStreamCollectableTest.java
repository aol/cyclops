package cyclops.futurestream.react.lazy.sequence;

import com.oath.cyclops.types.foldable.Folds;
import cyclops.futurestream.LazyReact;
import cyclops.streams.CollectableTest;


public class FutureStreamCollectableTest extends CollectableTest {
    @Override
    public <T> Folds<T> of(T... values) {
       return LazyReact.sequentialBuilder().of(values);
    }

}
