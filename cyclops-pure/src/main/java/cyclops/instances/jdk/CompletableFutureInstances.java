package cyclops.instances.jdk;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.Higher;
import cyclops.companion.Optionals;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.kinds.CompletableFutureKind;
import cyclops.kinds.OptionalKind;
import cyclops.typeclasses.functions.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.tuple1;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple1;
import cyclops.function.Monoid;
import cyclops.typeclasses.Cokleisli;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Kleisli;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.Higher;
import cyclops.companion.CompletableFutures;
import cyclops.control.Either;
import cyclops.control.Future;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.typeclasses.functor.Functor;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Companion class for creating Type Class instances for working with CompletableFutures
 * @author johnmcclean
 *
 */
@UtilityClass
public class CompletableFutureInstances {
  public static InstanceDefinitions<DataWitness.completableFuture> definitions(){
    return new InstanceDefinitions<DataWitness.completableFuture>() {
      @Override
      public <T, R> Functor<DataWitness.completableFuture> functor() {
        return CompletableFutureInstances.functor();
      }

      @Override
      public <T> Pure<DataWitness.completableFuture> unit() {
        return CompletableFutureInstances.unit();
      }

      @Override
      public <T, R> Applicative<DataWitness.completableFuture> applicative() {
        return CompletableFutureInstances.applicative();
      }

      @Override
      public <T, R> Monad<DataWitness.completableFuture> monad() {
        return CompletableFutureInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<DataWitness.completableFuture>> monadZero() {
        return Option.some(CompletableFutureInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<DataWitness.completableFuture>> monadPlus() {
        return Option.some(CompletableFutureInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<DataWitness.completableFuture> monadRec() {
        return CompletableFutureInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<DataWitness.completableFuture>> monadPlus(MonoidK<DataWitness.completableFuture> m) {
        return Option.some(CompletableFutureInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<DataWitness.completableFuture> traverse() {
        return CompletableFutureInstances.traverse();
      }

      @Override
      public <T> Foldable<DataWitness.completableFuture> foldable() {
        return CompletableFutureInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<DataWitness.completableFuture>> comonad() {
        return Maybe.just(CompletableFutureInstances.comonad());
      }

      @Override
      public <T> Option<Unfoldable<DataWitness.completableFuture>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }

  /**
   *
   * Transform a future, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  CompletableFutureKind<Integer> future = CompletableFutures.functor().map(i->i*2, CompletableFutureKind.widen(CompletableFuture.completedFuture(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with CompletableFutures
   * <pre>
   * {@code
   *   CompletableFutureKind<Integer> future = CompletableFutures.unit()
  .unit("hello")
  .applyHKT(h->CompletableFutures.functor().map((String v) ->v.length(), h))
  .convert(CompletableFutureKind::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for CompletableFutures
   */
  public static <T,R>Functor<DataWitness.completableFuture> functor(){
    BiFunction<CompletableFutureKind<T>,Function<? super T, ? extends R>,CompletableFutureKind<R>> map = CompletableFutureInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * CompletableFutureKind<String> future = CompletableFutures.unit()
  .unit("hello")
  .convert(CompletableFutureKind::narrowK3);

  //CompletableFuture.completedFuture("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for CompletableFutures
   */
  public static <T> Pure<DataWitness.completableFuture> unit(){
    return General.<DataWitness.completableFuture,T>unit(CompletableFutureInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.CompletableFutureKind.widen;
   * import static com.aol.cyclops.util.function.Lambda.l1;
   *
  CompletableFutures.applicative()
  .ap(widen(asCompletableFuture(l1(this::multiplyByTwo))),widen(asCompletableFuture(3)));
   *
   * //[6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * CompletableFutureKind<Function<Integer,Integer>> futureFn =CompletableFutures.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(CompletableFutureKind::narrowK3);

  CompletableFutureKind<Integer> future = CompletableFutures.unit()
  .unit("hello")
  .applyHKT(h->CompletableFutures.functor().map((String v) ->v.length(), h))
  .applyHKT(h->CompletableFutures.applicative().ap(futureFn, h))
  .convert(CompletableFutureKind::narrowK3);

  //CompletableFuture.completedFuture("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for CompletableFutures
   */
  public static <T,R> Applicative<DataWitness.completableFuture> applicative(){
    BiFunction<CompletableFutureKind< Function<T, R>>,CompletableFutureKind<T>,CompletableFutureKind<R>> ap = CompletableFutureInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.CompletableFutureKind.widen;
   * CompletableFutureKind<Integer> future  = CompletableFutures.monad()
  .flatMap(i->widen(CompletableFutureX.range(0,i)), widen(CompletableFuture.completedFuture(3)))
  .convert(CompletableFutureKind::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    CompletableFutureKind<Integer> future = CompletableFutures.unit()
  .unit("hello")
  .applyHKT(h->CompletableFutures.monad().flatMap((String v) ->CompletableFutures.unit().unit(v.length()), h))
  .convert(CompletableFutureKind::narrowK3);

  //CompletableFuture.completedFuture("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad functions for CompletableFutures
   */
  public static <T,R> Monad<DataWitness.completableFuture> monad(){

    BiFunction<Higher<DataWitness.completableFuture,T>,Function<? super T, ? extends Higher<DataWitness.completableFuture,R>>,Higher<DataWitness.completableFuture,R>> flatMap = CompletableFutureInstances::flatMap;
    return General.monad(applicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  CompletableFutureKind<String> future = CompletableFutures.unit()
  .unit("hello")
  .applyHKT(h->CompletableFutures.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(CompletableFutureKind::narrowK3);

  //CompletableFuture.completedFuture("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<DataWitness.completableFuture> monadZero(){

    return General.monadZero(monad(), CompletableFutureKind.widen(new CompletableFuture<T>()));
  }

  public static <T> MonadPlus<DataWitness.completableFuture> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.firstCompleteCompletableFuture());
  }

  public static  <T> MonadPlus<DataWitness.completableFuture> monadPlus(MonoidK<DataWitness.completableFuture> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<DataWitness.completableFuture> traverse(){

    return General.traverseByTraverse(applicative(), CompletableFutureInstances::traverseA);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = CompletableFutures.foldable()
  .foldLeft(0, (a,b)->a+b, CompletableFutureKind.widen(CompletableFuture.completedFuture(3)));

  //3
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T,R> Foldable<DataWitness.completableFuture> foldable(){
    BiFunction<Monoid<T>,Higher<DataWitness.completableFuture,T>,T> foldRightFn =  (m, l)-> m.apply(m.zero(), CompletableFutureKind.narrowK(l).join());
    BiFunction<Monoid<T>,Higher<DataWitness.completableFuture,T>,T> foldLeftFn = (m, l)->  m.apply(m.zero(), CompletableFutureKind.narrowK(l).join());
    Function3<Monoid<R>, Function<T, R>, Higher<DataWitness.completableFuture, T>, R> foldMapFn = (m, f, l)-> Future.of(CompletableFutureKind.narrowK(l).thenApply(f)).fold(m);
    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }
  public static <T> Comonad<DataWitness.completableFuture> comonad(){
    Function<? super Higher<DataWitness.completableFuture, T>, ? extends T> extractFn = maybe -> maybe.convert(CompletableFutureKind::narrowK).join();
    return General.comonad(functor(), unit(), extractFn);
  }

  private <T> CompletableFutureKind<T> of(T value){
    return CompletableFutureKind.widen(CompletableFuture.completedFuture(value));
  }
  private static <T,R> CompletableFutureKind<R> ap(CompletableFutureKind<Function< T, R>> lt, CompletableFutureKind<T> future){
    return CompletableFutureKind.widen(lt.thenCombine(future, (a, b)->a.apply(b)));

  }
  private static <T,R> Higher<DataWitness.completableFuture,R> flatMap(Higher<DataWitness.completableFuture,T> lt, Function<? super T, ? extends  Higher<DataWitness.completableFuture,R>> fn){
    return CompletableFutureKind.widen(CompletableFutureKind.narrow(lt).thenCompose(fn.andThen(CompletableFutureKind::narrowK)));
  }
  private static <T,R> CompletableFutureKind<R> map(CompletableFutureKind<T> lt, Function<? super T, ? extends R> fn){
    return CompletableFutureKind.widen(lt.thenApply(fn));
  }


  private static <C2,T,R> Higher<C2, Higher<DataWitness.completableFuture, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                                         Higher<DataWitness.completableFuture, T> ds){
    CompletableFuture<T> future = CompletableFutureKind.narrowK(ds);
    return applicative.map(CompletableFutureKind::completedFuture, fn.apply(future.join()));
  }
  public static <T,R> MonadRec<DataWitness.completableFuture> monadRec(){

    return new  MonadRec<DataWitness.completableFuture>(){

      @Override
      public <T, R> Higher<DataWitness.completableFuture, R> tailRec(T initial, Function<? super T, ? extends Higher<DataWitness.completableFuture, ? extends Either<T, R>>> fn) {
        Higher<DataWitness.future, R> x = Future.Instances.monadRec().tailRec(initial, fn.andThen(CompletableFutureKind::narrowK).andThen(Future::of));
        return CompletableFutureKind.narrowFuture(x);
      }
    };
  }

}
