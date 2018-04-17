package cyclops.reactive;


import cyclops.control.Future;
import cyclops.control.Try;
import cyclops.reactive.IO;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class IOTest {
  Executor ex = Executors.newFixedThreadPool(1);
  RuntimeException re = new RuntimeException();
  @Test
  public void sync(){
    assertThat(IO.of(()->10)
                 .map(i->i+1)
                 .run().orElse(-1),equalTo(11));
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
      .map(t->t.visit(i->i,e->-1))
      .run(),equalTo(Try.success(-1)));

    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .mapTry(i->i*2)
      .map(t->t.visit(i->i,e->-1))
      .run(),equalTo(Try.success(20)));
  }
  @Test
  public void asyncAttemptSpecific(){
    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .mapTry(i->{throw re;}, IOException.class)
      .map(t->t.visit(i->i,e->-1))
      .run(),equalTo(Try.failure(re)));

    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .mapTry(i->{throw re;},RuntimeException.class)
      .map(t->t.visit(i->i,e->-1))
      .run(),equalTo(Try.success(-1)));

    assertThat(IO.fromPublisher(Future.of(()->10, ex))
      .mapTry(i->i*2,RuntimeException.class)
      .map(t->t.visit(i->i,e->-1))
      .run(),equalTo(Try.success(20)));
  }
}
