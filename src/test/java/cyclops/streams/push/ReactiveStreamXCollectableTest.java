package cyclops.streams.push;

import com.aol.cyclops2.react.lazy.sequence.CollectableTest;
import com.aol.cyclops2.types.foldable.Folds;
import cyclops.stream.Spouts;


public class ReactiveStreamXCollectableTest extends CollectableTest {

    @Override
    public <T> Folds<T> of(T... values) {
       return Spouts.of(values);
    }

}
