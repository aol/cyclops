package cyclops.collectionx.standard;

import com.aol.cyclops2.types.foldable.Folds;


import cyclops.collectionx.mutable.ListX;
import com.aol.cyclops2.react.lazy.sequence.CollectableTest;

public class ListXCollectableTest extends CollectableTest {

    @Override
    public <T> Folds<T> of(T... values) {
       return  ListX.of(values);
    }

}
