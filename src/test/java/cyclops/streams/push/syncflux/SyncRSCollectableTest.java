package cyclops.streams.push.syncflux;

import com.aol.cyclops2.react.lazy.sequence.CollectableTest;
import cyclops.stream.Spouts;
import org.jooq.lambda.Collectable;
import reactor.core.publisher.Flux;

public class SyncRSCollectableTest extends CollectableTest {


    public <T> Collectable<T> of(T... values){
        return Spouts.from(Flux.just(values)).collectionOperations();
    }

}
