package cyclops.streams.push.asyncreactivestreams;

import com.aol.cyclops2.react.lazy.sequence.CollectableTest;
import com.aol.cyclops2.types.foldable.Folds;
import cyclops.reactive.Spouts;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ForkJoinPool;

public class AsyncRSCollectableTest extends CollectableTest {


    public <T> Folds<T> of(T... values){

        return Spouts.from(Flux.just(values)
                .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));
    }

}
