package cyclops.streams.push.asyncreactivestreams;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.streams.AbstractReactiveSeqTest;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Ignore
public class AsyncRSReactiveSeqTest extends AbstractReactiveSeqTest {

    @Override
    public ReactiveSeq<Integer> of(Integer... values) {
        return Spouts.from(Flux.just(values)
            .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));
    }

    @Override
    public ReactiveSeq<Integer> empty() {
        return Spouts.from(Flux.<Integer>empty()
            .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));
    }





}
