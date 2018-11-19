package cyclops.companion;

import com.sun.tools.javac.comp.Flow;
import cyclops.companion.rx2.Singles;
import cyclops.control.Future;
import cyclops.reactive.Spouts;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class SinglesTest {

    Single<Integer> just;
    Single<Integer> none;
    Single<Integer> active;
    Single<Integer> just2;

    @Before
    public void setup(){
        just = Single.just(10);
        none = Single.error(new Exception("boo"));
        active = Single.fromPublisher(Future.future());
        just2 = Single.just(20);
    }

    @Test
    public void testSequenceError() throws InterruptedException {
        Single<Flowable<Integer>> maybes = Singles.sequence(Flux.just(just,none));



        assertThat(Future.fromPublisher(maybes.toFlowable()).isFailed(),equalTo(true));
    }
    @Test
    public void testSequenceErrorAsync() {
        Single<Flowable<Integer>> maybes =Singles.sequence(Flux.just(just,active));
        assertThat(Future.fromPublisher(maybes.toFlowable()).isDone(),equalTo(false));
    }
    @Test
    public void testSequenceTwo() {
        Single<Flowable<Integer>> maybes =Singles.sequence(Flux.just(just,just2));
        assertThat(Spouts.from(maybes.toFlowable()).mergeMap(i->i).toList(),equalTo(Arrays.asList(10,20)));
    }

}
