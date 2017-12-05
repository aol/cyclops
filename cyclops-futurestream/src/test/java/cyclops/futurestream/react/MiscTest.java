package cyclops.futurestream.react;

import cyclops.async.Future;
import cyclops.async.LazyReact;
import cyclops.async.SimpleReact;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Futures;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MiscTest {
  @Test
  public void fromFluxLazyFutureStream(){
    assertThat( new LazyReact().fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
      Arrays.asList(1,2,3)));
  }
  @Test
  public void fromFluxSimpleReactStream(){
    assertThat( new SimpleReact().fromPublisher(Flux.just(1,2,3)).block(),equalTo(
      Arrays.asList(1,2,3)));
  }
  @Test
  public void testBreakout(){

    Future<ListX<Integer>> strings = Futures.quorum(status -> status.getCompleted() > 1, Future.of(()->1), Future.of(()->1), Future.of(()->1));


    assertThat(strings.toCompletableFuture().join().size(), is(greaterThan(1)));
  }
  @Test
  public void testBreakoutAll(){

    Future<ListX<Integer>> strings = Futures.quorum(status -> status.getCompleted() > 2, Future.of(()->1), Future.of(()->1), Future.of(()->1));


    assertThat(strings.toCompletableFuture().join().size(), is(equalTo(3)));
  }
  @Test
  public void testBreakoutOne(){

    Future<ListX<Integer>> strings = Futures.quorum(status -> status.getCompleted() >0, Future.of(()->1), Future.future(), Future.future());


    assertThat(strings.toCompletableFuture().join().size(), is(equalTo(1)));
  }
}
