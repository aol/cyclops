package cyclops.streams.syncflux;


import cyclops.companion.reactor.Fluxs;
import cyclops.reactive.FluxReactiveSeq;
import cyclops.reactive.ReactiveSeq;
import cyclops.streams.CollectableTest;

import reactor.core.publisher.Flux;

public class SyncRSCollectableTest extends CollectableTest {


    public <T> ReactiveSeq<T> of(T... values){
        return FluxReactiveSeq.reactiveSeq(Flux.just(values));
    }

}
