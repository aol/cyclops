package cyclops.instances.reactive.collections.immutable;

import com.oath.cyclops.hkt.DataWitness.persistentSetX;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentSet;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.reactive.collections.immutable.PersistentSetX;
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
import cyclops.reactive.ReactiveSeq;
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
import java.util.stream.Stream;

@UtilityClass
public class PersistentSetXInstances {

  public static  <T> Kleisli<persistentSetX,PersistentSetX<T>,T> kindKleisli(){
    return Kleisli.of(PersistentSetXInstances.monad(), PersistentSetX::widen);
  }
  public static  <T> Cokleisli<persistentSetX,T,PersistentSetX<T>> kindCokleisli(){
    return Cokleisli.of(PersistentSetX::narrowK);
  }
  public static <W1,T> Nested<persistentSetX,W1,T> nested(PersistentSetX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, PersistentSetXInstances.definitions(),def2);
  }
  public static  <W1,T> Product<persistentSetX,W1,T> product(PersistentSetX<T> s, Active<W1,T> active){
    return Product.of(allTypeclasses(s),active);
  }
  public static  <W1,T> Coproduct<W1,persistentSetX,T> coproduct(PersistentSetX<T> s, InstanceDefinitions<W1> def2){
    return Coproduct.right(s,def2, PersistentSetXInstances.definitions());
  }
  public static  <T> Active<persistentSetX,T> allTypeclasses(PersistentSetX<T> s){
    return Active.of(s, PersistentSetXInstances.definitions());
  }
  public static  <W2,R,T> Nested<persistentSetX,W2,R> mapM(PersistentSetX<T> s,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(s.map(fn), PersistentSetXInstances.definitions(), defs);
  }


  public static InstanceDefinitions<persistentSetX> definitions(){
    return new InstanceDefinitions<persistentSetX>() {
      @Override
      public <T, R> Functor<persistentSetX> functor() {
        return PersistentSetXInstances.functor();
      }

      @Override
      public <T> Pure<persistentSetX> unit() {
        return PersistentSetXInstances.unit();
      }

      @Override
      public <T, R> Applicative<persistentSetX> applicative() {
        return PersistentSetXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<persistentSetX> monad() {
        return PersistentSetXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<persistentSetX>> monadZero() {
        return Option.some(PersistentSetXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<persistentSetX>> monadPlus() {
        return Option.some(PersistentSetXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<persistentSetX> monadRec() {
        return PersistentSetXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<persistentSetX>> monadPlus(MonoidK<persistentSetX> m) {
        return Option.some(PersistentSetXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<persistentSetX> traverse() {
        return PersistentSetXInstances.traverse();
      }

      @Override
      public <T> Foldable<persistentSetX> foldable() {
        return PersistentSetXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<persistentSetX>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<persistentSetX>> unfoldable() {
        return Option.some(PersistentSetXInstances.unfoldable());
      }
    };

  }
  public static Unfoldable<persistentSetX> unfoldable(){
    return new Unfoldable<persistentSetX>() {
      @Override
      public <R, T> Higher<persistentSetX, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return PersistentSetX.unfold(b,fn);
      }
    };
  }
  /**
   *
   * Transform a persistentSetX, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  PersistentSetX<Integer> persistentSetX = Sets.functor().map(i->i*2, PersistentSetX.widen(Arrays.asSet(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with Sets
   * <pre>
   * {@code
   *   PersistentSetX<Integer> persistentSetX = Sets.unit()
  .unit("hello")
  .applyHKT(h->Sets.functor().map((String v) ->v.length(), h))
  .convert(PersistentSetX::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Sets
   */
  public static <T,R>Functor<persistentSetX> functor(){
    BiFunction<PersistentSetX<T>,Function<? super T, ? extends R>,PersistentSetX<R>> map = PersistentSetXInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * PersistentSetX<String> persistentSetX = Sets.unit()
  .unit("hello")
  .convert(PersistentSetX::narrowK3);

  //Arrays.asSet("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Sets
   */
  public static <T> Pure<persistentSetX> unit(){
    return General.<persistentSetX,T>unit(PersistentSetXInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.oath.cyclops.hkt.jdk.PersistentSetX.widen;
   * import static com.oath.cyclops.util.function.Lambda.l1;
   * import static java.util.Arrays.asSet;
   *
  Sets.zippingApplicative()
  .ap(widen(asSet(l1(this::multiplyByTwo))),widen(asSet(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * PersistentSetX<Function<Integer,Integer>> persistentSetXFn =Sets.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(PersistentSetX::narrowK3);

  PersistentSetX<Integer> persistentSetX = Sets.unit()
  .unit("hello")
  .applyHKT(h->Sets.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Sets.zippingApplicative().ap(persistentSetXFn, h))
  .convert(PersistentSetX::narrowK3);

  //Arrays.asSet("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Sets
   */
  public static <T,R> Applicative<persistentSetX> zippingApplicative(){
    BiFunction<PersistentSetX< Function<T, R>>,PersistentSetX<T>,PersistentSetX<R>> ap = PersistentSetXInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.oath.cyclops.hkt.jdk.PersistentSetX.widen;
   * PersistentSetX<Integer> persistentSetX  = Sets.monad()
  .flatMap(i->widen(PersistentSetX.range(0,i)), widen(Arrays.asSet(1,2,3)))
  .convert(PersistentSetX::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    PersistentSetX<Integer> persistentSetX = Sets.unit()
  .unit("hello")
  .applyHKT(h->Sets.monad().flatMap((String v) ->Sets.unit().unit(v.length()), h))
  .convert(PersistentSetX::narrowK3);

  //Arrays.asSet("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad arrow for Sets
   */
  public static <T,R> Monad<persistentSetX> monad(){

    BiFunction<Higher<persistentSetX,T>,Function<? super T, ? extends Higher<persistentSetX,R>>,Higher<persistentSetX,R>> flatMap = PersistentSetXInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }
  public static <T,R> MonadRec<persistentSetX> monadRec(){

    return new MonadRec<persistentSetX>(){
      @Override
      public <T, R> Higher<persistentSetX, R> tailRec(T initial, Function<? super T, ? extends Higher<persistentSetX,? extends Either<T, R>>> fn) {
        return PersistentSetX.tailRec(initial,fn.andThen(PersistentSetX::narrowK));
      }
    };
  }
  /**
   *
   * <pre>
   * {@code
   *  PersistentSetX<String> persistentSetX = Sets.unit()
  .unit("hello")
  .applyHKT(h->Sets.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(PersistentSetX::narrowK3);

  //Arrays.asSet("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<persistentSetX> monadZero(){

    return General.monadZero(monad(), PersistentSetX.empty());
  }

  public static <T> MonadPlus<persistentSetX> monadPlus(){
    Monoid<PersistentSetX<T>> m = Monoid.of(PersistentSetX.empty(), PersistentSetXInstances::concat);
    Monoid<Higher<persistentSetX,T>> m2= (Monoid)m;
    return General.monadPlus(monadZero(), MonoidKs.persistentSetXConcat());
  }

  public static <T> MonadPlus<persistentSetX> monadPlus(MonoidK<persistentSetX> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<persistentSetX> traverse(){
    BiFunction<Applicative<C2>,PersistentSetX<Higher<C2, T>>,Higher<C2, PersistentSetX<T>>> sequenceFn = (ap, persistentSetX) -> {

      Higher<C2,PersistentSetX<T>> identity = ap.unit(PersistentSetX.empty());

      BiFunction<Higher<C2,PersistentSetX<T>>,Higher<C2,T>,Higher<C2,PersistentSetX<T>>> combineToSet =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

      BinaryOperator<Higher<C2,PersistentSetX<T>>> combineSets = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> { l1.addAll(l2); return l1;}),a,b); ;

      return persistentSetX.stream()
        .reduce(identity,
          combineToSet,
          combineSets);


    };
    BiFunction<Applicative<C2>,Higher<persistentSetX,Higher<C2, T>>,Higher<C2, Higher<persistentSetX,T>>> sequenceNarrow  =
      (a,b) -> PersistentSetXInstances.widen2(sequenceFn.apply(a, PersistentSetXInstances.narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = Sets.foldable()
  .foldLeft(0, (a,b)->a+b, PersistentSetX.widen(Arrays.asSet(1,2,3,4)));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T,R> Foldable<persistentSetX> foldable(){
    BiFunction<Monoid<T>,Higher<persistentSetX,T>,T> foldRightFn =  (m, l)-> PersistentSetX.fromIterable(narrow(l)).foldRight(m);
    BiFunction<Monoid<T>,Higher<persistentSetX,T>,T> foldLeftFn = (m, l)-> PersistentSetX.fromIterable(narrow(l)).reduce(m);
    Function3<Monoid<R>, Function<T, R>, Higher<persistentSetX, T>, R> foldMapFn = (m, f, l)->narrowK(l).map(f).foldLeft(m);
    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }

  private static  <T> PersistentSetX<T> concat(PersistentSet<T> l1, PersistentSet<T> l2){
    return PersistentSetX.persistentSetX(ReactiveSeq.fromStream(Stream.concat(l1.stream(),l2.stream())));
  }
  private static <T> PersistentSetX<T> of(T value){
    return PersistentSetX.of(value);
  }
  private static <T,R> PersistentSetX<R> ap(PersistentSetX<Function< T, R>> lt,  PersistentSetX<T> persistentSetX){
    return PersistentSetX.fromIterable(lt).zip(persistentSetX,(a,b)->a.apply(b));
  }
  private static <T,R> Higher<persistentSetX,R> flatMap(Higher<persistentSetX,T> lt, Function<? super T, ? extends  Higher<persistentSetX,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(PersistentSetXInstances::narrowK));
  }
  private static <T,R> PersistentSetX<R> map(PersistentSetX<T> lt, Function<? super T, ? extends R> fn){
    return PersistentSetX.fromIterable(lt).map(fn);
  }



  /**
   * Widen a SetType nest inside another HKT encoded type
   *
   * @param flux HTK encoded type containing  a Set to widen
   * @return HKT encoded type with a widened Set
   */
  public static <C2, T> Higher<C2, Higher<persistentSetX, T>> widen2(Higher<C2, PersistentSetX<T>> flux) {
    // a functor could be used (if C2 is a functor / one exists for C2 type)
    // instead of casting
    // cast seems safer as Higher<persistentSetX,T> must be a PersistentSetX
    return (Higher) flux;
  }



  /**
   * Convert the raw Higher Kinded Type for SetType types into the SetType type definition class
   *
   * @param future HKT encoded persistentSetX into a SetType
   * @return SetType
   */
  public static <T> PersistentSetX<T> narrowK(final Higher<persistentSetX, T> future) {
    return (PersistentSetX<T>) future;
  }

  /**
   * Convert the HigherKindedType definition for a Set into
   *
   * @param completableSet Type Constructor to convert back into narrowed type
   * @return Set from Higher Kinded Type
   */
  public static <T> PersistentSetX<T> narrow(final Higher<persistentSetX, T> completableSet) {

    return ((PersistentSetX<T>) completableSet);//.narrow();

  }
}
