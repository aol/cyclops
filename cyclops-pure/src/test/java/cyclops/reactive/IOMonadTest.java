package cyclops.reactive;

import com.oath.cyclops.hkt.DataWitness.reactiveSeq;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.instances.reactive.PublisherInstances;
import cyclops.reactive.IOMonad.FromPublsher;
import cyclops.reactive.IOMonad.ToPublsher;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static cyclops.reactive.IOMonad.futureConverter;
import static cyclops.reactive.IOMonad.ioMonad;
import static cyclops.reactive.IOMonad.reactiveSeqConverter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class IOMonadTest {
    Executor ex = Executors.newFixedThreadPool(1);
    RuntimeException re = new RuntimeException();


    @Test
    public void sync(){
        assertThat(ioMonad(PublisherInstances.monad(), ReactiveSeq.of(10), new ToPublsher<reactiveSeq>() {
            @Override
            public <T> Function<Higher<reactiveSeq, T>, Publisher<T>> toPublisherFn() {
                return a -> ReactiveSeq.narrowK(a);
            }
        }, new FromPublsher<reactiveSeq>() {
            @Override
            public <T> Function<? super Publisher<? extends T>, ? extends Higher<reactiveSeq, T>> fromPublisherFn() {
                return p->Spouts.from(p);
            }
        })
            .map(i->i+1)
            .run().orElse(-1),equalTo(11));
    }
    public void sync2(){
        assertThat(ioMonad(reactiveSeqConverter, ReactiveSeq.of(10))
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
        ioMonad(reactiveSeqConverter,ReactiveSeq.of(10))
            .bracket(i-> new IOMonadTest.MyCloseable())
            .run();

        assertTrue(closed);
    }
    @Test
    public void bracketCons(){
        assertFalse(closed);
        ioMonad(reactiveSeqConverter,ReactiveSeq.of(10))
            .bracket(i-> new IOMonadTest.MyCloseable(), b->{
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
        ioMonad(reactiveSeqConverter,ReactiveSeq.of(10))
            .bracket(i-> new IOMonadTest.MyCloseable())
            .map(x->100)
            .run();

        assertTrue(closed);
    }
    @Test
    public void async(){
        assertThat(IO.fromPublisher(Future.of(()->10, ex))
            .map(i->i+1)
            .run().orElse(-1),equalTo(11));
    }



    @Test
    public void asyncError(){
        MatcherAssert.assertThat(IO.fromPublisher(Future.of(()->10, ex))
            .map(i->{throw re;})
            .run(),equalTo(Try.failure(re)));
    }
    @Test
    public void flatMap(){
        assertThat(ioMonad(futureConverter,Future.of(()->10, ex))
            .flatMap(i->IO.of(()->i+1))
            .run().orElse(-1),equalTo(11));
    }

    @Test
    public void asyncAttempt(){
        assertThat(ioMonad(futureConverter,Future.of(()->10, ex))
            .mapTry(i->{throw re;})
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(-1)));

        assertThat(ioMonad(futureConverter,Future.of(()->10, ex))
            .mapTry(i->i*2)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(20)));
    }
    @Test
    public void asyncAttemptSpecific(){
        assertThat(ioMonad(futureConverter,Future.of(()->10, ex))
            .mapTry(i->{throw re;}, IOException.class)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.failure(re)));

        assertThat(ioMonad(futureConverter,Future.of(()->10, ex))
            .mapTry(i->{throw re;},RuntimeException.class)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(-1)));

        assertThat(ioMonad(futureConverter,Future.of(()->10, ex))
            .mapTry(i->i*2,RuntimeException.class)
            .map(t->t.fold(i->i, e->-1))
            .run(),equalTo(Try.success(20)));
    }

    @Mock
    Function<Integer, String> serviceMock;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

    }
    @Test
    public void shouldSucceedAfterFewAsynchronousRetries() throws Exception {


        BDDMockito.given(serviceMock.apply(Matchers.anyInt())).willThrow(
            new RuntimeException(new SocketException("First")),
            new RuntimeException(new IOException("Second"))).willReturn(
            "42");

        String result = ioMonad(reactiveSeqConverter,Spouts.of(1,  2, 3))
            .retry(serviceMock)
            .run().mkString();

        Assert.assertThat(result, is("Success[42]"));
    }

    private CompletableFuture<String> failedAsync(Throwable throwable) {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }




    @Test
    public void shouldRethrowOriginalExceptionFromUserFutureCompletion()
        throws Exception {




        BDDMockito.given(serviceMock.apply(Matchers.anyInt())).willThrow(
            new RuntimeException("DONT PANIC"));


        String result = ioMonad(reactiveSeqConverter, Spouts.of(1))

            .retry(serviceMock,2, 100l, TimeUnit.MILLISECONDS).toString();


        System.out.println(result);
        assertThat(result,equalTo("IO[Failure[java.lang.RuntimeException: DONT PANIC]]"));



    }



}
