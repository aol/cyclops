package cyclops.companion;

import cyclops.companion.reactor.Fluxs;
import cyclops.companion.reactor.Monos;
import cyclops.control.Future;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class FluxsTest {

    Flux<Integer> just;
    Flux<Integer> none;
    Flux<Integer> active;
    Flux<Integer> just2;

    @Before
    public void setup(){
        just = Flux.just(10);
        none = Flux.error(new Exception("boo"));
        active = Flux.from(Future.future());
        just2 = Flux.just(20);
    }

    @Test
    public void testSequenceError() throws InterruptedException {
        Flux<Mono<Integer>> maybes = Fluxs.sequence(Flux.just(just,none));
        AtomicBoolean error = new AtomicBoolean(false);
        maybes.subscribe(m->{
            System.out.println(m);
        },t->{
            error.set(true);
        },()->{
            System.out.println("Done");
        });

        assertThat(error.get(),equalTo(true));
    }
    @Test
    public void testSequenceErrorAsync() {
        AtomicBoolean done = new AtomicBoolean(false);
        Flux<Mono<Integer>> maybes =Fluxs.sequence(Flux.just(just.doOnNext(e->done.set(true)),active));

       assertThat(done.get(),equalTo(false));
    }
    @Test
    public void testSequenceTwo() {
        Flux<Mono<Integer>> maybes =Fluxs.sequence(Flux.just(just,just2));
        assertThat(maybes.map(Mono::block).toStream().collect(Collectors.toList()),equalTo(Arrays.asList(10,20)));
    }

    @Test
    public void testSequenceOneFlux() {
        Flux<Mono<Integer>> maybes =Fluxs.sequence(Mono.just(Flux.just(10,20)));
        assertThat(maybes.map(Mono::block).collect(Collectors.toList()).block(),equalTo(Arrays.asList(10,20)));
    }

}
