package cyclops.reactive.collections.standard;

import com.oath.cyclops.types.foldable.Folds;


import cyclops.reactive.collections.mutable.ListX;
import cyclops.streams.CollectableTest;


public class ListXCollectableTest extends CollectableTest {

    @Override
    public <T> Folds<T> of(T... values) {
       return  ListX.of(values);
    }

}
