package cyclops.futurestream;

import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.reactive.AbstractIOTestBase;
import cyclops.reactive.IO;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class FutureStreamIOTest extends AbstractIOTestBase {
    Executor ex = Executors.newFixedThreadPool(1);
    RuntimeException re = new RuntimeException();
    @Test
    public void sync(){
        assertThat(FutureStreamIO.of(()->10)
            .map(i->i+1)
            .run().orElse(-1),equalTo(11));
    }

    boolean closed = false;

    @Override
    public IO<Integer> of(Integer... values) {
        return FutureStreamIO.of(new LazyReact().sequentialBuilder().of(values));
    }

    @Override
    public IO<Integer> empty() {
        return FutureStreamIO.of(FutureStream.builder().of());
    }

    class MyCloseable implements AutoCloseable{

        @Override
        public void close() throws Exception {
            closed = true;
        }
    }

    @Test
    public void bracket(){
        assertFalse(closed);
        FutureStreamIO.of(()->10)
            .bracket(i-> new MyCloseable())
            .run();

        assertTrue(closed);
    }
    @Test
    public void bracketCons(){
        assertFalse(closed);
        FutureStreamIO.of(()->10)
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
        FutureStreamIO.of(()->10)
            .bracket(i-> new MyCloseable())
            .map(x->100)
            .run();

        assertTrue(closed);
    }
    @Test
    public void bracketThenMap2(){
        assertFalse(closed);
        FutureStreamIO.of(()->10)
            .bracket(i-> new MyCloseable())
            .map(x->100)
            .run();

        assertTrue(closed);
    }
    @Test
    public void async(){
        assertThat(FutureStreamIO.of(Future.of(()->10, ex))
            .map(i->i+1)
            .run().orElse(-1),equalTo(11));
    }

    @Test
    public void asyncError(){
        MatcherAssert.assertThat(FutureStreamIO.of(Future.of(()->10, ex))
            .map(i->{throw re;})
            .run(),equalTo(Try.failure(re)));
    }
    @Test
    public void flatMap(){
        assertThat(FutureStreamIO.of(Future.of(()->10, ex))
            .flatMap(i->FutureStreamIO.of(()->i+1))
            .run().orElse(-1),equalTo(11));
    }

    @Test
    public void asyncAttempt(){
        assertThat(FutureStreamIO.of(Future.of(()->10, ex))
            .mapTry(i->{throw re;})
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(-1)));

        assertThat(FutureStreamIO.of(Future.of(()->10, ex))
            .mapTry(i->i*2)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(20)));
    }
    @Test
    public void asyncAttemptRx(){
        assertThat(FutureStreamIO.of(()->10, Executors.newSingleThreadExecutor())
            .mapTry(i->{throw re;})
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(-1)));

        assertThat(FutureStreamIO.of(()->10,Executors.newSingleThreadExecutor())
            .mapTry(i->i*2)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(20)));
    }
    @Test
    public void asyncAttemptSpecific(){
        assertThat(FutureStreamIO.of(Future.of(()->10, ex))
            .mapTry(i->{throw re;}, IOException.class)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.failure(re)));

        assertThat(FutureStreamIO.of(Future.of(()->10, ex))
            .mapTry(i->{throw re;},RuntimeException.class)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(-1)));

        assertThat(FutureStreamIO.of(Future.of(()->10, ex))
            .mapTry(i->i*2,RuntimeException.class)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(20)));
    }

}
