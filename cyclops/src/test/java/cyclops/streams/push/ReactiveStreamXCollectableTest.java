package cyclops.streams.push;

import com.oath.cyclops.types.foldable.Folds;
import cyclops.reactive.Spouts;
import cyclops.streams.CollectableTest;


public class ReactiveStreamXCollectableTest extends CollectableTest {

    @Override
    public <T> Folds<T> of(T... values) {
       return Spouts.of(values);
    }

}
