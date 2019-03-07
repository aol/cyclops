package cyclops.streams.push.syncflux;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.streams.AbstractReactiveSeqTest;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ForkJoinPool;

public class SyncRSReactiveSeqTest extends AbstractReactiveSeqTest {

    @Override
    public ReactiveSeq<Integer> of(Integer... values) {
        return Spouts.of(values);
    }

    @Override
    public ReactiveSeq<Integer> empty() {
        return Spouts.empty();
    }





}
