package cyclops.streams.push.syncflux;

import com.aol.cyclops2.react.lazy.sequence.CollectableTest;
import com.aol.cyclops2.types.foldable.Folds;
import cyclops.stream.Spouts;
import reactor.core.publisher.Flux;

public class SyncRSCollectableTest extends CollectableTest {


    public <T> Folds<T> of(T... values){
        return Spouts.from(Flux.just(values));
    }

}
