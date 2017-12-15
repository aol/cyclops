package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness.future;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Either;
import cyclops.control.Future;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Monoid;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functions.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Companion class for creating Type Class instances for working with Futures
 * @author johnmcclean
 *
 */
@UtilityClass
public  class FutureInstances {
  public static  <T> Kleisli<future,Future<T>,T> kindKleisli(){
    return Kleisli.of(FutureInstances.monad(), Future::widen);
  }

  public static  <T> Cokleisli<future,T,Future<T>> kindCokleisli(){
    return Cokleisli.of(Future::narrowK);
  }
  public static <W1,T> Nested<future,W1,T> nested(Future<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, FutureInstances.definitions(),def2);
  }
  public static <W1,T> Product<future,W1,T> product(Future<T> f,Active<W1,T> active){
    return Product.of(allTypeclasses(f),active);
  }
  public static <W1,T> Coproduct<W1,future,T> coproduct(Future<T> f,InstanceDefinitions<W1> def2){
    return Coproduct.right(f,def2, FutureInstances.definitions());
  }
  public static <T>Active<future,T> allTypeclasses(Future<T> f){
    return Active.of(f, FutureInstances.definitions());
  }
  public static <W2,R,T> Nested<future,W2,R> mapM(Future<T> f,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(f.map(fn), FutureInstances.definitions(), defs);
  }

  public static InstanceDefinitions<future> definitions(){
    return new InstanceDefinitions<future>() {
      @Override
      public <T, R> Functor<future> functor() {
        return FutureInstances.functor();
      }

      @Override
      public <T> Pure<future> unit() {
        return FutureInstances.unit();
      }

      @Override
      public <T, R> Applicative<future> applicative() {
        return FutureInstances.applicative();
      }

      @Override
      public <T, R> Monad<future> monad() {
        return FutureInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<future>> monadZero() {
        return Option.some(FutureInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<future>> monadPlus() {
        return Option.some(FutureInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<future> monadRec() {
        return FutureInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<future>> monadPlus(MonoidK<future> m) {
        return Option.some(FutureInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<future> traverse() {
        return FutureInstances.traverse();
      }

      @Override
      public <T> Foldable<future> foldable() {
        return FutureInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<future>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<future>> unfoldable() {
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
   *  Future<Integer> future = Futures.functor().map(i->i*2, Future.widen(Future.ofResult(2));
   *
   *  //[4]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with Futures
   * <pre>
   * {@code
   *   Future<Integer> future = Futures.unit()
  .unit("hello")
  .applyHKT(h->Futures.functor().map((String v) ->v.length(), h))
  .convert(Future::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Futures
   */
  public static <T,R> Functor<future> functor(){
    BiFunction<Future<T>,Function<? super T, ? extends R>,Future<R>> map = FutureInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * Future<String> future = Futures.unit()
  .unit("hello")
  .convert(Future::narrowK3);

  //Future("hello")
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Futures
   */
  public static <T> Pure<future> unit(){
    return General.<future,T>unit(FutureInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.Future.widen;
   * import static com.aol.cyclops.util.function.Lambda.l1;
   * import static java.util.Arrays.asFuture;
   *
  Futures.zippingApplicative()
  .ap(widen(asFuture(l1(this::multiplyByTwo))),widen(asFuture(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * Future<Function<Integer,Integer>> futureFn =Futures.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(Future::narrowK3);

  Future<Integer> future = Futures.unit()
  .unit("hello")
  .applyHKT(h->Futures.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Futures.applicative().ap(futureFn, h))
  .convert(Future::narrowK3);

  //Future("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Futures
   */
  public static <T,R> Applicative<future> applicative(){
    BiFunction<Future< Function<T, R>>,Future<T>,Future<R>> ap = FutureInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.Future.widen;
   * Future<Integer> future  = Futures.monad()
  .flatMap(i->widen(Future.ofResult(0)), widen(Future.ofResult(2)))
  .convert(Future::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    Future<Integer> future = Futures.unit()
  .unit("hello")
  .applyHKT(h->Futures.monad().flatMap((String v) ->Futures.unit().unit(v.length()), h))
  .convert(Future::narrowK3);

  //Future("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad functions for Futures
   */
  public static <T,R> Monad<future> monad(){

    BiFunction<Higher<future,T>,Function<? super T, ? extends Higher<future,R>>,Higher<future,R>> flatMap = FutureInstances::flatMap;
    return General.monad(applicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  Future<String> future = Futures.unit()
  .unit("hello")
  .applyHKT(h->Futures.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(Future::narrowK3);

  //Future["hello"]
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<future> monadZero(){

    return General.monadZero(monad(), Future.future());
  }

  public static <T,R> MonadRec<future> monadRec(){


    return new MonadRec<future>(){

      @Override
      public <T, R> Higher<future, R> tailRec(T initial, Function<? super T, ? extends Higher<future, ? extends Either<T, R>>> fn) {
        return Future.tailRec(initial,fn.andThen(Future::narrowK));
      }
    };
  }


  public static <T> MonadPlus<future> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.firstSuccessfulFuture());
  }

  public static <T> MonadPlus<future> monadPlus(MonoidK<future> m){

    return General.monadPlus(monadZero(),m);
  }


  public static <L> Traverse<future> traverse() {
    return new Traverse<future>() {

      @Override
      public <T> Higher<future, T> unit(T value) {
        return FutureInstances.<T>unit().unit(value);
      }

      @Override
      public <C2, T, R> Higher<C2, Higher<future, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<future, T> ds) {
        Future<T> maybe = Future.narrowK(ds);
        return maybe.visit(right->applicative.map(m->Future.ofResult(m), fn.apply(right)),left->  applicative.unit(Future.ofError(left)));
      }

      @Override
      public <C2, T> Higher<C2, Higher<future, T>> sequenceA(Applicative<C2> applicative, Higher<future, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }



      @Override
      public <T, R> Higher<future, R> ap(Higher<future, ? extends Function<T, R>> fn, Higher<future, T> apply) {
        return applicative().ap(fn,apply);

      }

      @Override
      public <T, R> Higher<future, R> map(Function<? super T, ? extends R> fn, Higher<future, T> ds) {
        return functor().map(fn,ds);
      }

    };
  }
  public static <L> Foldable<future> foldable() {
    return new Foldable<future>() {


      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<future, T> ds) {
        Future<T> ft = Future.narrowK(ds);
        return ft.fold(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<future, T> ds) {
        Future<T> ft = Future.narrowK(ds);
        return ft.fold(monoid);
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<future, T> nestedA) {
        return foldLeft(mb,Future.narrowK(nestedA).<R>map(fn));
      }
    };
  }



  private <T> Future<T> of(T value){
    return Future.ofResult(value);
  }
  private static <T,R> Future<R> ap(Future<Function< T, R>> lt,  Future<T> future){
    return lt.zip(future, (a,b)->a.apply(b));

  }
  private static <T,R> Higher<future,R> flatMap(Higher<future,T> lt, Function<? super T, ? extends  Higher<future,R>> fn){
    return Future.narrowK(lt).flatMap(fn.andThen(Future::narrowK));
  }
  private static <T,R> Future<R> map(Future<T> lt, Function<? super T, ? extends R> fn){
    return lt.map(fn);
  }



}
