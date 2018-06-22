package cyclops.instances.reactive.collections.immutable;


import static com.oath.cyclops.data.ReactiveWitness.vectorX;

import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.reactive.collections.immutable.VectorX;
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

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.oath.cyclops.types.foldable.Evaluation.LAZY;
import static cyclops.reactive.collections.immutable.VectorX.narrowK;

/**
 * Companion class for creating Type Class instances for working with PVectors
 * @author johnmcclean
 *
 */
@UtilityClass
public class VectorXInstances {

  public static  <T> Kleisli<vectorX,VectorX<T>,T> kindKleisli(){
    return Kleisli.of(VectorXInstances.monad(), VectorX::widen);
  }

  public static  <T> Cokleisli<vectorX,T,VectorX<T>> kindCokleisli(){
    return Cokleisli.of(VectorX::narrowK);
  }
  public static <W1,T> Nested<vectorX,W1,T> nested(VectorX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, VectorXInstances.definitions(),def2);
  }
  public static <W1,T> Product<vectorX,W1,T> product(VectorX<T> vec, Active<W1,T> active){
    return Product.of(allTypeclasses(vec),active);
  }
  public static <W1,T> Coproduct<W1,vectorX,T> coproduct(VectorX<T> vec, InstanceDefinitions<W1> def2){
    return Coproduct.right(vec,def2, VectorXInstances.definitions());
  }
  public static <T> Active<vectorX,T> allTypeclasses(VectorX<T> vec){
    return Active.of(vec, VectorXInstances.definitions());
  }
  public static <W2,R,T> Nested<vectorX,W2,R> mapM(VectorX<T> vec,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(vec.map(fn), VectorXInstances.definitions(), defs);
  }
  public static InstanceDefinitions<vectorX> definitions(){
    return new InstanceDefinitions<vectorX>() {
      @Override
      public <T, R> Functor<vectorX> functor() {
        return VectorXInstances.functor();
      }

      @Override
      public <T> Pure<vectorX> unit() {
        return VectorXInstances.unit();
      }

      @Override
      public <T, R> Applicative<vectorX> applicative() {
        return VectorXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<vectorX> monad() {
        return VectorXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<vectorX>> monadZero() {
        return Option.some(VectorXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<vectorX>> monadPlus() {
        return Option.some(VectorXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<vectorX> monadRec() {
        return VectorXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<vectorX>> monadPlus(MonoidK<vectorX> m) {
        return Option.some(VectorXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<vectorX> traverse() {
        return VectorXInstances.traverse();
      }

      @Override
      public <T> Foldable<vectorX> foldable() {
        return VectorXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<vectorX>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<vectorX>> unfoldable() {
        return Option.some(VectorXInstances.unfoldable());
      }
    };
  }
  public static Unfoldable<vectorX> unfoldable(){
    return new Unfoldable<vectorX>() {
      @Override
      public <R, T> Higher<vectorX, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return VectorX.unfold(b,fn);
      }
    };
  }
  /**
   *
   * Transform a list, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  VectorX<Integer> list = PVectors.functor().map(i->i*2, Arrays.asPVector(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with PVectors
   * <pre>
   * {@code
   *   VectorX<Integer> list = PVectors.unit()
  .unit("hello")
  .applyHKT(h->PVectors.functor().map((String v) ->v.length(), h))
  .convert(VectorX::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for PVectors
   */
  public static <T,R>Functor<vectorX> functor(){
    BiFunction<VectorX<T>,Function<? super T, ? extends R>,VectorX<R>> map = VectorXInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * VectorX<String> list = PVectors.unit()
  .unit("hello")
  .convert(VectorX::narrowK3);

  //Arrays.asPVector("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for PVectors
   */
  public static  <T> Pure<vectorX> unit(){
    return General.<vectorX,T>unit(VectorXInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.VectorX.widen;
   * import static com.aol.cyclops.util.function.Lambda.l1;
   * import static java.util.Arrays.asPVector;
   *
  PVectors.zippingApplicative()
  .ap(widen(asPVector(l1(this::multiplyByTwo))),widen(asPVector(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * VectorX<Function<Integer,Integer>> listFn =PVectors.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(VectorX::narrowK3);

  VectorX<Integer> list = PVectors.unit()
  .unit("hello")
  .applyHKT(h->PVectors.functor().map((String v) ->v.length(), h))
  .applyHKT(h->PVectors.zippingApplicative().ap(listFn, h))
  .convert(VectorX::narrowK3);

  //Arrays.asPVector("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for PVectors
   */
  public static <T,R> Applicative<vectorX> zippingApplicative(){
    BiFunction<VectorX< Function<T, R>>,VectorX<T>,VectorX<R>> ap = VectorXInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.VectorX.widen;
   * VectorX<Integer> list  = PVectors.monad()
  .flatMap(i->widen(VectorX.range(0,i)), widen(Arrays.asPVector(1,2,3)))
  .convert(VectorX::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    VectorX<Integer> list = PVectors.unit()
  .unit("hello")
  .applyHKT(h->PVectors.monad().flatMap((String v) ->PVectors.unit().unit(v.length()), h))
  .convert(VectorX::narrowK3);

  //Arrays.asPVector("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad arrow for PVectors
   */
  public static <T,R> Monad<vectorX> monad(){

    BiFunction<Higher<vectorX,T>,Function<? super T, ? extends Higher<vectorX,R>>,Higher<vectorX,R>> flatMap = VectorXInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  VectorX<String> list = PVectors.unit()
  .unit("hello")
  .applyHKT(h->PVectors.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(VectorX::narrowK3);

  //Arrays.asPVector("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<vectorX> monadZero(){

    return General.monadZero(monad(), VectorX.empty());
  }
  public static <T,R> MonadRec<vectorX> monadRec(){

    return new MonadRec<vectorX>(){
      @Override
      public <T, R> Higher<vectorX, R> tailRec(T initial, Function<? super T, ? extends Higher<vectorX,? extends Either<T, R>>> fn) {
        VectorX<Either<T, R>> next = VectorX.of(Either.left(initial));
        boolean newValue[] = {false};
        for(;;){
          next = next.concatMap(e -> e.fold(s -> { newValue[0]=true; return narrowK(fn.apply(s)); }, p -> VectorX.of(e)));
          if(!newValue[0])
            break;
        }
        return Either.sequenceRight(next).map(l -> l.to(ReactiveConvertableSequence::converter)
                                                    .vectorX(LAZY))
                                                    .orElse(VectorX.empty());

      }
    };
  }

  public static <T> MonadPlus<vectorX> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.vectorXConcat());
  }

  public static  MonadPlus<vectorX> monadPlus(MonoidK<vectorX> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<vectorX> traverse(){
    BiFunction<Applicative<C2>,VectorX<Higher<C2, T>>,Higher<C2, VectorX<T>>> sequenceFn = (ap, list) -> {

      Higher<C2,VectorX<T>> identity = ap.unit(VectorX.empty());

      BiFunction<Higher<C2,VectorX<T>>,Higher<C2,T>,Higher<C2,VectorX<T>>> combineToPVector =   (acc, next) -> ap.apBiFn(ap.unit((a, b) ->a.plus(b)),acc,next);

      BinaryOperator<Higher<C2,VectorX<T>>> combinePVectors = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> l1.plusAll(l2)),a,b); ;

      return list.stream()
        .reduce(identity,
          combineToPVector,
          combinePVectors);


    };
    BiFunction<Applicative<C2>,Higher<vectorX,Higher<C2, T>>,Higher<C2, Higher<vectorX,T>>> sequenceNarrow  =
      (a,b) -> VectorX.widen2(sequenceFn.apply(a, narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = PVectors.foldable()
  .foldLeft(0, (a,b)->a+b, Arrays.asPVector(1,2,3,4)));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T> Foldable<vectorX> foldable(){
    return new Foldable<vectorX>() {
      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<vectorX, T> ds) {
        return  VectorX.fromIterable(narrowK(ds)).foldRight(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<vectorX, T> ds) {
        return  VectorX.fromIterable(narrowK(ds)).foldLeft(monoid);
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<vectorX, T> nestedA) {
        Monoid<? extends R> m = mb;
        Object x = narrowK(nestedA).map(fn).foldLeft((Monoid) m);
        return (R)x;

      }
    };

  }

  private static  <T> VectorX<T> concat(PersistentList<T> l1, PersistentList<T> l2){

    return VectorX.fromIterable(l1.plusAll(l2));
  }
  private <T> VectorX<T> of(T value){
    return VectorX.of(value);
  }
  private static <T,R> VectorX<R> ap(VectorX<Function< T, R>> lt, VectorX<T> list){
    return VectorX.fromIterable(lt).zip(list,(a, b)->a.apply(b));
  }
  private static <T,R> Higher<vectorX,R> flatMap(Higher<vectorX,T> lt, Function<? super T, ? extends  Higher<vectorX,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(VectorX::narrowK));
  }
  private static <T,R> VectorX<R> map(VectorX<T> lt, Function<? super T, ? extends R> fn){
    return lt.map(fn);
  }
}
