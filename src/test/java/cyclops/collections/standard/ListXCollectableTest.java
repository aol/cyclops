package cyclops.collections.standard;

import com.oath.cyclops.types.foldable.Folds;


import cyclops.collections.mutable.ListX;
import com.oath.cyclops.react.lazy.sequence.CollectableTest;

public class ListXCollectableTest extends CollectableTest {

    @Override
    public <T> Folds<T> of(T... values) {
       return  ListX.of(values);
    }

}
