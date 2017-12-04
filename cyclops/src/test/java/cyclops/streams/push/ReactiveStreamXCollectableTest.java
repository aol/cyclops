package cyclops.streams.push;

import com.oath.cyclops.react.lazy.sequence.CollectableTest;
import com.oath.cyclops.types.foldable.Folds;
import cyclops.reactive.Spouts;


public class ReactiveStreamXCollectableTest extends CollectableTest {

    @Override
    public <T> Folds<T> of(T... values) {
       return Spouts.of(values);
    }

}
