package cyclops.streams.push.syncflux;

import com.oath.cyclops.types.foldable.Folds;
import cyclops.reactive.Spouts;
import cyclops.streams.CollectableTest;
import reactor.core.publisher.Flux;

public class SyncRSCollectableTest extends CollectableTest {


    public <T> Folds<T> of(T... values){
        return Spouts.from(Flux.just(values));
    }

}
