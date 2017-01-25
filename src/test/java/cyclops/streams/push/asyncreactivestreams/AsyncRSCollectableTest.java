package cyclops.streams.push.asyncreactivestreams;

import com.aol.cyclops2.react.lazy.sequence.CollectableTest;
import cyclops.stream.Spouts;
import org.jooq.lambda.Collectable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

public class AsyncRSCollectableTest extends CollectableTest {


    public <T> Collectable<T> of(T... values){

        return Spouts.from(Flux.just(values)
                .subscribeOn(Schedulers.fromExecutor(Executors.newFixedThreadPool(1))))
                .collectable();
    }

}
