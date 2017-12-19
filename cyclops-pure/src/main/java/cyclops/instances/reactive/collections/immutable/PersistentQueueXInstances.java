package cyclops.instances.reactive.collections.immutable;

import com.oath.cyclops.hkt.DataWitness.persistentQueueX;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentQueue;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.collections.immutable.PersistentQueueX;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static cyclops.collections.immutable.PersistentQueueX.narrowK;

/**
 * Companion class for creating Type Class instances for working with PersistentQueues
 * @author johnmcclean
 *
 */
@UtilityClass
public class PersistentQueueXInstances {

  public static  <T> Cokleisli<persistentQueueX,T,PersistentQueueX<T>> kindCokleisli(){
    return Cokleisli.of(PersistentQueueX::narrowK);
  }
  public static  <T> Kleisli<persistentQueueX,PersistentQueueX<T>,T> kindKleisli(){
    return Kleisli.of(PersistentQueueXInstances.monad(), PersistentQueueX::widen);
  }
  public static <W1,T> Nested<persistentQueueX,W1,T> nested(PersistentQueueX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, PersistentQueueXInstances.definitions(),def2);
  }

  public static <W1,T> Product< persistentQueueX,W1,T> product(PersistentQueueX<T> q, Active<W1,T> active){
    return Product.of(allTypeclasses(q),active);
  }
  public static <W1,T> Coproduct<W1, persistentQueueX,T> coproduct(PersistentQueueX<T> q, InstanceDefinitions<W1> def2){
    return Coproduct.right(q,def2, PersistentQueueXInstances.definitions());
  }
  public static <T> Active<persistentQueueX,T> allTypeclasses(PersistentQueueX<T> q){
    return Active.of(q, PersistentQueueXInstances.definitions());
  }
  public static <W2,R,T> Nested< persistentQueueX,W2,R> mapM(PersistentQueueX<T> q,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(q.map(fn), PersistentQueueXInstances.definitions(), defs);
  }

  public static InstanceDefinitions<persistentQueueX> definitions(){

    return new InstanceDefinitions<persistentQueueX>() {
      @Override
      public <T, R> Functor<persistentQueueX> functor() {
        return PersistentQueueXInstances.functor();
      }

      @Override
      public <T> Pure<persistentQueueX> unit() {
        return PersistentQueueXInstances.unit();
      }

      @Override
      public <T, R> Applicative<persistentQueueX> applicative() {
        return PersistentQueueXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<persistentQueueX> monad() {
        return PersistentQueueXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<persistentQueueX>> monadZero() {
        return Option.some(PersistentQueueXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<persistentQueueX>> monadPlus() {
        return Option.some(PersistentQueueXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<persistentQueueX> monadRec() {
        return PersistentQueueXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<persistentQueueX>> monadPlus(MonoidK<persistentQueueX> m) {
        return Option.some(PersistentQueueXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<persistentQueueX> traverse() {
        return PersistentQueueXInstances.traverse();
      }

      @Override
      public <T> Foldable<persistentQueueX> foldable() {
        return PersistentQueueXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<persistentQueueX>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<persistentQueueX>> unfoldable() {
        return Option.some(PersistentQueueXInstances.unfoldable());
      }
    };
  }

  public static Unfoldable<persistentQueueX> unfoldable(){
    return new Unfoldable<persistentQueueX>() {
      @Override
      public <R, T> Higher<persistentQueueX, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return PersistentQueueX.unfold(b,fn);
      }
    };
  }

  /**
   *
   * Transform a list, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  PersistentQueueX<Integer> list = PQueues.functor().map(i->i*2, Arrays.asPQueue(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with PQueues
   * <pre>
   * {@code
   *   PersistentQueueX<Integer> list = PQueues.unit()
  .unit("hello")
  .applyHKT(h->PQueues.functor().map((String v) ->v.length(), h))
  .convert(PersistentQueueX::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for PQueues
   */
  public static <T,R>Functor<persistentQueueX> functor(){
    BiFunction<PersistentQueueX<T>,Function<? super T, ? extends R>,PersistentQueueX<R>> map = PersistentQueueXInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * PersistentQueueX<String> list = PQueues.unit()
  .unit("hello")
  .convert(PersistentQueueX::narrowK3);

  //Arrays.asPQueue("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for PQueues
   */
  public static <T> Pure<persistentQueueX> unit(){
    return General.<persistentQueueX,T>unit(PersistentQueueXInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.PersistentQueueX.widen;
   * import static com.aol.cyclops.util.function.Lambda.l1;
   * import static java.util.Arrays.asPQueue;
   *
  PQueues.zippingApplicative()
  .ap(widen(asPQueue(l1(this::multiplyByTwo))),widen(asPQueue(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * PersistentQueueX<Function<Integer,Integer>> listFn =PQueues.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(PersistentQueueX::narrowK3);

  PersistentQueueX<Integer> list = PQueues.unit()
  .unit("hello")
  .applyHKT(h->PQueues.functor().map((String v) ->v.length(), h))
  .applyHKT(h->PQueues.zippingApplicative().ap(listFn, h))
  .convert(PersistentQueueX::narrowK3);

  //Arrays.asPQueue("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for PQueues
   */
  public static <T,R> Applicative<persistentQueueX> zippingApplicative(){
    BiFunction<PersistentQueueX< Function<T, R>>,PersistentQueueX<T>,PersistentQueueX<R>> ap = PersistentQueueXInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.PersistentQueueX.widen;
   * PersistentQueueX<Integer> list  = PQueues.monad()
  .flatMap(i->widen(PersistentQueueX.range(0,i)), widen(Arrays.asPQueue(1,2,3)))
  .convert(PersistentQueueX::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    PersistentQueueX<Integer> list = PQueues.unit()
  .unit("hello")
  .applyHKT(h->PQueues.monad().flatMap((String v) ->PQueues.unit().unit(v.length()), h))
  .convert(PersistentQueueX::narrowK3);

  //Arrays.asPQueue("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad arrow for PQueues
   */
  public static <T,R> Monad<persistentQueueX> monad(){

    BiFunction<Higher<persistentQueueX,T>,Function<? super T, ? extends Higher<persistentQueueX,R>>,Higher<persistentQueueX,R>> flatMap = PersistentQueueXInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  PersistentQueueX<String> list = PQueues.unit()
  .unit("hello")
  .applyHKT(h->PQueues.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(PersistentQueueX::narrowK3);

  //Arrays.asPQueue("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<persistentQueueX> monadZero(){

    return General.monadZero(monad(), PersistentQueueX.empty());
  }
  /**
   * <pre>
   * {@code
   *  PersistentQueueX<Integer> list = PQueues.<Integer>monadPlus()
  .plus(Arrays.asPQueue()), Arrays.asPQueue(10)))
  .convert(PersistentQueueX::narrowK3);
  //Arrays.asPQueue(10))
   *
   * }
   * </pre>
   * @return Type class for combining PQueues by concatenation
   */
  public static <T> MonadPlus<persistentQueueX> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.persistentQueueXConcat());
  }
  public static <T,R> MonadRec<persistentQueueX> monadRec(){

    return new MonadRec<persistentQueueX>(){
      @Override
      public <T, R> Higher<persistentQueueX, R> tailRec(T initial, Function<? super T, ? extends Higher<persistentQueueX,? extends Either<T, R>>> fn) {
        return PersistentQueueX.tailRec(initial,fn.andThen(PersistentQueueX::narrowK));
      }
    };
  }

  public static MonadPlus<persistentQueueX> monadPlus(MonoidK<persistentQueueX> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<persistentQueueX> traverse(){
    BiFunction<Applicative<C2>,PersistentQueueX<Higher<C2, T>>,Higher<C2, PersistentQueueX<T>>> sequenceFn = (ap, list) -> {

      Higher<C2,PersistentQueueX<T>> identity = ap.unit(PersistentQueueX.empty());

      BiFunction<Higher<C2,PersistentQueueX<T>>,Higher<C2,T>,Higher<C2,PersistentQueueX<T>>> combineToPQueue =   (acc, next) -> ap.apBiFn(ap.unit((a, b) ->a.plus(b)),acc,next);

      BinaryOperator<Higher<C2,PersistentQueueX<T>>> combinePQueues = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> l1.plusAll(l2)),a,b); ;

      return list.stream()
        .reduce(identity,
          combineToPQueue,
          combinePQueues);


    };
    BiFunction<Applicative<C2>,Higher<persistentQueueX,Higher<C2, T>>,Higher<C2, Higher<persistentQueueX,T>>> sequenceNarrow  =
      (a,b) -> PersistentQueueX.widen2(sequenceFn.apply(a, narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = PQueues.foldable()
  .foldLeft(0, (a,b)->a+b, Arrays.asPQueue(1,2,3,4)));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T,R> Foldable<persistentQueueX> foldable(){
    BiFunction<Monoid<T>,Higher<persistentQueueX,T>,T> foldRightFn =  (m, l)-> narrowK(l).foldRight(m);
    BiFunction<Monoid<T>,Higher<persistentQueueX,T>,T> foldLeftFn = (m, l)-> narrowK(l).reduce(m);
    Function3<Monoid<R>, Function<T, R>, Higher<persistentQueueX, T>, R> foldMapFn = (m, f, l)->narrowK(l).map(f).foldLeft(m);
    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }

  private static  <T> PersistentQueueX<T> concat(PersistentQueue<T> l1, PersistentQueue<T> l2){

    return PersistentQueueX.fromIterable(l1.plusAll(l2));
  }
  private <T> PersistentQueueX<T> of(T value){
    return PersistentQueueX.of(value);
  }
  private static <T,R> PersistentQueueX<R> ap(PersistentQueueX<Function< T, R>> lt, PersistentQueueX<T> list){
    return PersistentQueueX.fromIterable(lt).zip(list,(a, b)->a.apply(b));
  }
  private static <T,R> Higher<persistentQueueX,R> flatMap(Higher<persistentQueueX,T> lt, Function<? super T, ? extends  Higher<persistentQueueX,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(PersistentQueueX::narrowK));
  }
  private static <T,R> PersistentQueueX<R> map(PersistentQueueX<T> lt, Function<? super T, ? extends R> fn){
    return lt.map(fn);
  }
}
