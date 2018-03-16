package cyclops.streams.syncflux;


import cyclops.companion.reactor.Fluxs;
import cyclops.reactive.ReactiveSeq;
import cyclops.streams.CollectableTest;

import reactor.core.publisher.Flux;

public class SyncRSCollectableTest extends CollectableTest {


    public <T> ReactiveSeq<T> of(T... values){
        return Fluxs.reactiveSeq(Flux.just(values));
    }

}
