package cyclops.futurestream.react;


import cyclops.control.Future;
import cyclops.data.HashSet;
import cyclops.data.ImmutableList;
import cyclops.data.ImmutableSet;
import cyclops.data.Vector;
import cyclops.futurestream.LazyReact;
import cyclops.futurestream.SimpleReact;
import cyclops.companion.Futures;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

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

    Future<ImmutableList<Integer>> strings = Futures.quorum(status -> status.getCompleted() > 1, Future.of(()->1), Future.of(()->1), Future.of(()->1));


    assertThat(strings.toCompletableFuture().join().size(), is(greaterThan(1)));
  }
  @Test
  public void testBreakoutAll(){

    Future<ImmutableList<Integer>> strings = Futures.quorum(status -> status.getCompleted() > 2, Future.of(()->1), Future.of(()->1), Future.of(()->1));


    assertThat(strings.toCompletableFuture().join().size(), is(equalTo(3)));
  }
  @Test
  public void testBreakoutOne(){

    Future<ImmutableList<Integer>> strings = Futures.quorum(status -> status.getCompleted() >0, Future.of(()->1), Future.future(), Future.future());


    assertThat(strings.toCompletableFuture().join().size(), is(equalTo(1)));
  }
  static Supplier<Integer> countGen(AtomicInteger i) {
    return (()-> i.getAndIncrement());
  }
  @Test
  public void test(){
    final Supplier<Integer> count = countGen(new AtomicInteger(1));
    final Optional<Integer> sum = new LazyReact(100,100).generate(count).limit(10).reduce((a, b) -> a + b);
    assertThat(sum.get(),equalTo(55));
  }
  @Test
  public void pVectorX(){



    ReactiveSeq<String> seq = Spouts.from(Vector.of(1, 2, 3, 4)
      .plus(5)
      .map(i -> "connect toNested Akka, RxJava and more with reactiveBuffer-streams" + i));

    ImmutableSet<String> setX =  seq.to(s->new LazyReact().fromStream(s))
      .map(data->"fan out across threads with futureStreams" + data)
      .to(HashSet::fromIterable);





  }
}
