package cyclops.reactive;


import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.function.checked.CheckedFunction;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class IOCheckedTest {
  Executor ex = Executors.newFixedThreadPool(1);
  RuntimeException re = new RuntimeException();

  @Test
  public void withCatch(){
      IO<Integer> x = IO.withCatch(() -> {
          throw new IOException();
      });

      assertTrue(x.run().isFailure());

  }

  @Test
  public void foldForEach(){

  }

  public int addOne(int i) throws Exception{
      return i+1;
  }
    public IO<Integer> addOneIO(int i) throws Exception{
        return IO.of(i+1);
    }
  @Test
  public void sync(){
    assertThat(IO.of(()->10)
                 .checkedMap(this::addOne)
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
      IO.of(()->10)
          .checkedBracket(this::createCloseable)
          .run();

      assertTrue(closed);
  }

    private  MyCloseable createCloseable(int i) throws Exception {
        return  new MyCloseable();
    }

    @Test
    public void bracketCons(){
        assertFalse(closed);
        IO.of(()->10)
            .checkedBracket(this::createCloseable,b->{
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
        IO.of(()->10)
            .checkedBracket(this::createCloseable)
            .map(x->100)
            .run();

        assertTrue(closed);
    }
  @Test
  public void async(){
    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .checkedMap(this::addOne)
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
    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .checkedFlatMap(this::addOneIO)
      .run().orElse(-1),equalTo(11));
  }

  @Test
  public void asyncAttempt(){
    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .mapTry(i->{throw re;})
      .checkedMap(t->t.fold(i->i, e->-1))
      .run(),equalTo(Try.success(-1)));

    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .mapTry(i->i*2)
      .map(t->t.fold(i->i, e->-1))
      .run(),equalTo(Try.success(20)));
  }
  @Test
  public void asyncAttemptSpecific(){
    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .mapTry(i->{throw re;}, IOException.class)
      .map(t->t.fold(i->i, e->-1))
      .run(),equalTo(Try.failure(re)));

    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .mapTry(i->{throw re;},RuntimeException.class)
      .map(t->t.fold(i->i, e->-1))
      .run(),equalTo(Try.success(-1)));

    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .mapTry(i->i*2,RuntimeException.class)
      .map(t->t.fold(i->i, e->-1))
      .run(),equalTo(Try.success(20)));
  }

    @Mock
    CheckedFunction<Integer, String> serviceMock;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

    }
    @Test
    public void shouldSucceedAfterFewAsynchronousRetries() throws Throwable {


        BDDMockito.given(serviceMock.apply(Matchers.anyInt())).willThrow(
            new RuntimeException(new SocketException("First")),
            new RuntimeException(new IOException("Second"))).willReturn(
            "42");

        String result = IO.fromPublisher( Spouts.of(1,  2, 3))
            .checkedRetry(serviceMock)
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
        throws Throwable {




        BDDMockito.given(serviceMock.apply(Matchers.anyInt())).willThrow(
            new RuntimeException("DONT PANIC"));


        String result = IO.fromPublisher( Spouts.of(1))

            .checkedRetry(serviceMock,2, 100l,TimeUnit.MILLISECONDS).toString();


        System.out.println(result);
        assertThat(result,equalTo("IO[Failure[java.lang.RuntimeException: DONT PANIC]]"));



    }


}
