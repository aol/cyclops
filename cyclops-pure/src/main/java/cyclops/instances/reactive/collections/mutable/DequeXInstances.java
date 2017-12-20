package cyclops.instances.reactive.collections.mutable;


import com.oath.cyclops.hkt.DataWitness.deque;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.reactive.collections.mutable.DequeX;
import cyclops.companion.CyclopsCollectors;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
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

import java.util.Deque;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static cyclops.reactive.collections.mutable.DequeX.narrowK;

@UtilityClass
public class DequeXInstances {
  public static  <T> Kleisli<deque,DequeX<T>,T> kindKleisli(){
    return Kleisli.of(DequeXInstances.monad(), DequeX::widen);
  }

  public static  <T> Cokleisli<deque,T,DequeX<T>> kindCokleisli(){
    return Cokleisli.of(DequeX::narrowK);
  }
  public static <W1,T> Nested<deque,W1,T> nested(DequeX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, DequeXInstances.definitions(),def2);
  }
  public static  <W1,T> Product<deque,W1,T> product(DequeX<T> d, Active<W1,T> active){
    return Product.of(allTypeclasses(d),active);
  }
  public static  <W1,T> Coproduct<W1,deque,T> coproduct(DequeX<T> d, InstanceDefinitions<W1> def2){
    return Coproduct.right(d,def2, DequeXInstances.definitions());
  }
  public static  <T> Active<deque,T> allTypeclasses(DequeX<T> d){
    return Active.of(d, DequeXInstances.definitions());
  }

  public static  <W2,R,T> Nested<deque,W2,R> mapM(DequeX<T> d,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(d.map(fn), DequeXInstances.definitions(), defs);
  }
  public static InstanceDefinitions<deque> definitions(){
    return new InstanceDefinitions<deque>() {
      @Override
      public <T, R> Functor<deque> functor() {
        return DequeXInstances.functor();
      }

      @Override
      public <T> Pure<deque> unit() {
        return DequeXInstances.unit();
      }

      @Override
      public <T, R> Applicative<deque> applicative() {
        return DequeXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<deque> monad() {
        return DequeXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<deque>> monadZero() {
        return Option.some(DequeXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<deque>> monadPlus() {
        return Option.some(DequeXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<deque> monadRec() {
        return DequeXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<deque>> monadPlus(MonoidK<deque> m) {
        return Option.some(DequeXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<deque> traverse() {
        return DequeXInstances.traverse();
      }

      @Override
      public <T> Foldable<deque> foldable() {
        return DequeXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<deque>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<deque>> unfoldable() {
        return Option.some(DequeXInstances.unfoldable());
      }
    };
  }
  public static Unfoldable<deque> unfoldable(){
    return new Unfoldable<deque>() {
      @Override
      public <R, T> Higher<deque, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return DequeX.unfold(b,fn);
      }
    };
  }
  /**
   *
   * Transform a list, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  DequeX<Integer> list = Deques.functor().map(i->i*2, DequeX.of(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with Deques
   * <pre>
   * {@code
   *   DequeX<Integer> list = Deques.unit()
  .unit("hello")
  .applyHKT(h->Deques.functor().map((String v) ->v.length(), h))
  .convert(DequeX::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Deques
   */
  public static <T,R>Functor<deque> functor(){
    BiFunction<DequeX<T>,Function<? super T, ? extends R>,DequeX<R>> map = DequeXInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * DequeX<String> list = Deques.unit()
  .unit("hello")
  .convert(DequeX::narrowK3);

  //DequeX.of("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Deques
   */
  public static <T> Pure<deque> unit(){
    return General.<deque,T>unit(DequeXInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.DequeX.widen;
   * import static com.aol.cyclops.util.function.Lambda.l1;
   * import static java.util.DequeX.of;
   *
  Deques.zippingApplicative()
  .ap(widen(asDeque(l1(this::multiplyByTwo))),widen(asDeque(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * DequeX<Function<Integer,Integer>> listFn =Deques.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(DequeX::narrowK3);

  DequeX<Integer> list = Deques.unit()
  .unit("hello")
  .applyHKT(h->Deques.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Deques.zippingApplicative().ap(listFn, h))
  .convert(DequeX::narrowK3);

  //DequeX.of("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Deques
   */
  public static <T,R> Applicative<deque> zippingApplicative(){
    BiFunction<DequeX< Function<T, R>>,DequeX<T>,DequeX<R>> ap = DequeXInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.DequeX.widen;
   * DequeX<Integer> list  = Deques.monad()
  .flatMap(i->widen(DequeX.range(0,i)), widen(DequeX.of(1,2,3)))
  .convert(DequeX::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    DequeX<Integer> list = Deques.unit()
  .unit("hello")
  .applyHKT(h->Deques.monad().flatMap((String v) ->Deques.unit().unit(v.length()), h))
  .convert(DequeX::narrowK3);

  //DequeX.of("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad arrow for Deques
   */
  public static <T,R> Monad<deque> monad(){

    BiFunction<Higher<deque,T>,Function<? super T, ? extends Higher<deque,R>>,Higher<deque,R>> flatMap = DequeXInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  DequeX<String> list = Deques.unit()
  .unit("hello")
  .applyHKT(h->Deques.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(DequeX::narrowK3);

  //DequeX.of("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<deque> monadZero(){

    return General.monadZero(monad(), DequeX.empty());
  }
  /**
   * <pre>
   * {@code
   *  DequeX<Integer> list = Deques.<Integer>monadPlus()
  .plus(DequeX.of()), DequeX.of(10)))
  .convert(DequeX::narrowK3);
  //DequeX.of(10))
   *
   * }
   * </pre>
   * @return Type class for combining Deques by concatenation
   */
  public static <T> MonadPlus<deque> monadPlus(){
    Monoid<DequeX<T>> m = Monoid.of(DequeX.empty(), DequeXInstances::concat);
    Monoid<Higher<deque,T>> m2= (Monoid)m;
    return General.monadPlus(monadZero(), MonoidKs.dequeXConcat());
  }
  public static <T,R> MonadRec<deque> monadRec(){

    return new MonadRec<deque>(){
      @Override
      public <T, R> Higher<deque, R> tailRec(T initial, Function<? super T, ? extends Higher<deque,? extends Either<T, R>>> fn) {
        return DequeX.tailRec(initial,fn.andThen(DequeX::narrowK));
      }
    };
  }

  public static <T> MonadPlus<deque> monadPlus(MonoidK<deque> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<deque> traverse(){
    BiFunction<Applicative<C2>,DequeX<Higher<C2, T>>,Higher<C2, DequeX<T>>> sequenceFn = (ap,list) -> {

      Higher<C2,DequeX<T>> identity = ap.unit(DequeX.of());

      BiFunction<Higher<C2,DequeX<T>>,Higher<C2,T>,Higher<C2,DequeX<T>>> combineToDeque =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

      BinaryOperator<Higher<C2,DequeX<T>>> combineDeques = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> { l1.addAll(l2); return l1;}),a,b); ;

      return list.stream()
        .reduce(identity,
          combineToDeque,
          combineDeques);


    };
    BiFunction<Applicative<C2>,Higher<deque,Higher<C2, T>>,Higher<C2, Higher<deque,T>>> sequenceNarrow  =
      (a,b) -> DequeX.widen2(sequenceFn.apply(a, narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = Deques.foldable()
  .foldLeft(0, (a,b)->a+b, DequeX.of(1,2,3,4)));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T> Foldable<deque> foldable(){
    return new Foldable<deque>() {
      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<deque, T> ds) {
        return  DequeX.fromIterable(narrowK(ds)).foldRight(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<deque, T> ds) {
        return  DequeX.fromIterable(narrowK(ds)).foldLeft(monoid);
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<deque, T> nestedA) {
        return narrowK(nestedA).<R>map(fn).foldLeft(mb);
      }
    };

  }

  private static  <T> DequeX<T> concat(Deque<T> l1, Deque<T> l2){
    return Stream.concat(l1.stream(),l2.stream()).collect(CyclopsCollectors.toDequeX());
  }
  private <T> DequeX<T> of(T value){
    return DequeX.of(value);
  }
  private static <T,R> DequeX<R> ap(DequeX<Function< T, R>> lt,  DequeX<T> list){
    return lt.zip(list,(a,b)->a.apply(b));
  }
  private static <T,R> Higher<deque,R> flatMap(Higher<deque,T> lt, Function<? super T, ? extends  Higher<deque,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(DequeX::narrowK));
  }
  private static <T,R> DequeX<R> map(DequeX<T> lt, Function<? super T, ? extends R> fn){
    return lt.map(fn);
  }
}
