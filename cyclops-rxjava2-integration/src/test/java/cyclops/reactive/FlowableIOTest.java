package cyclops.reactive;

import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.data.Vector;
import io.reactivex.schedulers.Schedulers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class FlowableIOTest extends AbstractIOTestBase {

    Executor ex = Executors.newFixedThreadPool(1);
    RuntimeException re = new RuntimeException();

    @Override
    public IO<Integer> of(Integer... values) {
        return FlowableIO.fromPublisher(Flux.just(values));
    }

    @Override
    public IO<Integer> empty() {
        return FlowableIO.fromPublisher(Flux.empty());
    }
    @Test
    public void sync(){
        assertThat(FlowableIO.of(()->10)
            .map(i->i+1)
            .run().orElse(-1),equalTo(11));
    }

    boolean closed = false;
    class MyCloseable implements AutoCloseable{

        @Override
        public void close() throws Exception {
            closed = true;
        }
    }

    @Test
    public void bracket(){
        assertFalse(closed);
        FlowableIO.of(()->10)
            .bracket(i-> new MyCloseable())
            .run();

        assertTrue(closed);
    }
    @Test
    public void bracketCons(){
        assertFalse(closed);
        FlowableIO.of(()->10)
            .bracket(i-> new MyCloseable(), b->{
                try {
                    b.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            })
            .run();

        assertTrue(closed);
    }
    @Test
    public void bracketThenMap(){
        assertFalse(closed);
        FlowableIO.of(()->10)
            .bracket(i-> new MyCloseable())
            .map(x->100)
            .run();

        assertTrue(closed);
    }
    @Test
    public void async(){
        assertThat(FlowableIO.fromPublisher(Future.of(()->10, ex))
            .map(i->i+1)
            .run().orElse(-1),equalTo(11));
    }

    @Test
    public void asyncError(){
        MatcherAssert.assertThat(FlowableIO.fromPublisher(Future.of(()->10, ex))
            .map(i->{throw re;})
            .run(),equalTo(Try.failure(re)));
    }
    @Test
    public void flatMap(){
        assertThat(FlowableIO.fromPublisher(Future.of(()->10, ex))
            .flatMap(i->FlowableIO.of(()->i+1))
            .run().orElse(-1),equalTo(11));
    }

    @Test
    public void asyncAttempt(){
        assertThat(FlowableIO.fromPublisher(Future.of(()->10, ex))
            .mapTry(i->{throw re;})
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(-1)));

        assertThat(FlowableIO.fromPublisher(Future.of(()->10, ex))
            .mapTry(i->i*2)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(20)));
    }
    @Test
    public void asyncAttemptRx(){
        assertThat(FlowableIO.of(()->10, Schedulers.io())
            .mapTry(i->{throw re;})
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(-1)));

        assertThat(FlowableIO.of(()->10, Schedulers.io())
            .mapTry(i->i*2)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(20)));
    }
    @Test
    public void asyncAttemptSpecific(){
        assertThat(FlowableIO.fromPublisher(Future.of(()->10, ex))
            .mapTry(i->{throw re;}, IOException.class)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.failure(re)));

        assertThat(FlowableIO.fromPublisher(Future.of(()->10, ex))
            .mapTry(i->{throw re;},RuntimeException.class)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(-1)));

        assertThat(FlowableIO.fromPublisher(Future.of(()->10, ex))
            .mapTry(i->i*2,RuntimeException.class)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(20)));
    }

    @Test
    public void onError(){
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> result = new AtomicReference<>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);



        of(1, 2, 3).<Integer>map(i -> {
            throw new RuntimeException();
        })
            .onError(e -> count.incrementAndGet())
            .forEach(n -> {
                result.updateAndGet(v->v.plus(n));
                data.set(true);
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });

        while(error.get()==null){
            LockSupport.parkNanos(10l);
        }
        Assert.assertThat(data.get(), equalTo(false));
        Assert.assertThat(complete.get(), equalTo(false));
        Assert.assertThat(error.get(), instanceOf(RuntimeException.class));
        Assert.assertThat(result.get(),equalTo(Vector.empty()));




        Assert.assertThat(count.get(),equalTo(1));

    }

    @Override @Test @Ignore
    public void onErrorList() {

    }

    @Override @Test @Ignore
    public void onErrorIterator() {

    }

    @Override @Test @Ignore
    public void onErrorIncremental() throws InterruptedException {

    }

    @Override @Test @Ignore
    public void onErrorEmptyList() {

    }

    @Override @Test @Ignore
    public void onErrorEmptyIterator() {

    }

    @Override @Test @Ignore
    public void onErrorEmpty() {

    }

    @Override @Test @Ignore
    public void onErrorEmptyIncremental() {

    }

}
