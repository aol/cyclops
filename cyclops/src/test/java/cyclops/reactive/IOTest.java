package cyclops.reactive;


import com.oath.cyclops.util.ExceptionSoftener;

import cyclops.companion.Semigroups;
import cyclops.control.Future;
import cyclops.control.Try;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.oath.cyclops.util.ExceptionSoftener.softenFunction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.is;



public class IOTest extends AbstractIOTestBase{
  Executor ex = Executors.newFixedThreadPool(1);
  RuntimeException re = new RuntimeException();

    @Override
    public IO<Integer> of(Integer... values) {
        return IO.fromPublisher(Spouts.of(values));
    }

    @Override
    public IO<Integer> empty() {
        return IO.fromPublisher(Spouts.empty());
    }

    static class My{
      public String mayThrowCheckedException() throws Exception{
            throw new RuntimeException("Erorr thrown");
      }
  }

  @Test
  public void getHost() throws MalformedURLException {
      URL u = new URL("http://test.yahoo.com");
      System.out.println(u.getHost());
  }
 @Test
 public void errorHandling(){
     List<My> myList = Arrays.asList(new My(), new My(), new My());
     ReactiveSeq.fromIterable(myList);


     IO.sync(myList)
       .checkedMap(My::mayThrowCheckedException)
       .forEach(System.out::println,System.err::println,()->System.out.println("Complete"));


     String errorMessage = IO.sync(myList)
                             .mapTry(softenFunction(My::mayThrowCheckedException))
                             .map(Try::toEither)
                             .map(e->e.mapLeft(Throwable::getMessage))
                             .map(e->e.leftOrElse("No Exception"))
                             .stream()
                             .collect(Collectors.joining(","));

     System.out.println(errorMessage);

    
 }

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
  @Test
  public void sync(){
    assertThat(IO.of(()->10)
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
      IO.of(()->10)
          .checkedBracket(i-> new MyCloseable())
          .run();

      assertTrue(closed);
  }
    @Test
    public void bracketCons(){
        assertFalse(closed);
        IO.of(()->10)
            .checkedBracket(i-> new MyCloseable(),b->{
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
            .bracket(i-> new MyCloseable())
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
    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .flatMap(i->IO.of(()->i+1))
      .run().orElse(-1),equalTo(11));
  }

  @Test
  public void asyncAttempt(){
    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .mapTry(i->{throw re;})
      .map(t->t.fold(i->i, e->-1))
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

        String result = IO.fromPublisher( Spouts.of(1,  2, 3))
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


        String result = IO.fromPublisher( Spouts.of(1))

            .retry(serviceMock,2, 100l,TimeUnit.MILLISECONDS).toString();


        System.out.println(result);
        assertThat(result,equalTo("IO[Failure[java.lang.RuntimeException: DONT PANIC]]"));



    }


}
