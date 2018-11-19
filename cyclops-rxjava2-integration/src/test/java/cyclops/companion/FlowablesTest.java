package cyclops.companion;

import cyclops.companion.rx2.Flowables;
import cyclops.control.Future;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class FlowablesTest {

    Flowable<Integer> just;
    Flowable<Integer> none;
    Flowable<Integer> active;
    Flowable<Integer> just2;

    @Before
    public void setup(){
        just = Flowable.just(10);
        none = Flowable.error(new Exception("boo"));
        active = Flowable.fromPublisher(Future.future());
        just2 = Flowable.just(20);
    }

    @Test
    public void testSequenceError() throws InterruptedException {
        Flowable<Single<Integer>> maybes = Flowables.sequence(Flux.just(just,none));
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
        Flowable<Single<Integer>> maybes =Flowables.sequence(Flowable.just(just.doOnNext(e->done.set(true)),active));

       assertThat(done.get(),equalTo(false));
    }
    @Test
    public void testSequenceTwo() {
        Flowable<Single<Integer>> maybes =Flowables.sequence(Flux.just(just,just2));
        assertThat(maybes.map(Single::blockingGet).toList().blockingGet(),equalTo(Arrays.asList(10,20)));
    }

    @Test
    public void testSequenceOneFlux() {
        Flowable<Single<Integer>> maybes =Flowables.sequence(Single.just(Flowable.just(10,20)).toFlowable());
        assertThat(maybes.map(Single::blockingGet).toList().blockingGet(),equalTo(Arrays.asList(10,20)));
    }

}
