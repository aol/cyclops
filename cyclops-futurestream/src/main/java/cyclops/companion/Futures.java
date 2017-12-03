package cyclops.companion;

import com.oath.cyclops.react.collectors.lazy.Blocker;
import com.oath.cyclops.react.threads.SequentialElasticPools;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.async.Future;
import cyclops.async.SimpleReact;
import cyclops.collections.mutable.ListX;
import cyclops.control.Either;
import com.oath.cyclops.react.Status;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Futures {
  public static  <T,R> Future<R> tailRec(T initial, Function<? super T, ? extends Future<? extends Either<T, R>>> fn){
    SimpleReact sr = SequentialElasticPools.simpleReact.nextReactor();
    return Future.of(()->{
      Future<? extends Either<T, R>> next[] = new Future[1];
      next[0]=Future.ofResult(Either.left(initial));
      boolean cont = true;
      do {
        cont = next[0].visit(p ->  p.visit(s -> {
          next[0] = Future.narrowK(fn.apply(s));
          return true;
        }, pr -> false), () -> false);
      }while(cont);
      return next[0].map(x->x.orElse(null));
    }, sr.getExecutor()).flatMap(i->i)
      .peek(e->SequentialElasticPools.simpleReact.populate(sr)).recover(t->{
        SequentialElasticPools.simpleReact.populate(sr);
        throw ExceptionSoftener.throwSoftenedException(t);
      });
  }

  /**
   * Block until a Quorum of results have returned as determined by the provided Predicate
   *
   * <pre>
   * {@code
   *
   * Future<ListX<Integer>> strings = Future.quorum(status -> status.getCompleted() >0, Future.of(()->1),Future.future(),Future.future());


  strings.getValue().size()
  //1
   *
   * }
   * </pre>
   *
   *
   * @param breakout Predicate that determines whether the block should be
   *            continued or removed
   * @param fts Futures to  wait on results from
   * @return Future which will be populated with a Quorum of results
   */
  @SafeVarargs
  public static <T> Future<ListX<T>> quorum(Predicate<Status<T>> breakout, Future<T>... fts) {

    List<CompletableFuture<?>> list = Stream.of(fts)
      .map(Future::getFuture)
      .collect(Collectors.toList());

    return Future.of(new Blocker<T>(list, Optional.empty()).nonBlocking(breakout));


  }
  /**
   * Block until a Quorum of results have returned as determined by the provided Predicate
   *
   * <pre>
   * {@code
   *
   * Future<ListX<Integer>> strings = Future.quorum(status -> status.getCompleted() >0, Future.of(()->1),Future.future(),Future.future());


  strings.getValue().size()
  //1
   *
   * }
   * </pre>
   *
   *
   * @param breakout Predicate that determines whether the block should be
   *            continued or removed
   * @param fts Futures to  wait on results from
   * @param errorHandler Consumer to handle any exceptions thrown
   * @return Future which will be populated with a Quorum of results
   */
  @SafeVarargs
  public static <T> Future<ListX<T>> quorum(Predicate<Status<T>> breakout, Consumer<Throwable> errorHandler, Future<T>... fts) {

    List<CompletableFuture<?>> list = Stream.of(fts)
      .map(Future::getFuture)
      .collect(Collectors.toList());

    return Future.of(new Blocker<T>(list, Optional.of(errorHandler)).nonBlocking(breakout));


  }

}
