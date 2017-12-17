package cyclops.instances.reactive.collections.mutable;

import com.oath.cyclops.hkt.DataWitness.set;
import com.oath.cyclops.hkt.Higher;
import cyclops.collections.mutable.SetX;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
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

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

@UtilityClass
public class SetXInstances {
  public static <W1,T> Nested<set,W1,T> nested(SetX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, SetXInstances.definitions(),def2);
  }
  public static  <W1,T> Product<set,W1,T> product(SetX<T> s,Active<W1,T> active){
    return Product.of(allTypeclasses(s),active);
  }
  public static  <W1,T> Coproduct<W1,set,T> coproduct(SetX<T> s,InstanceDefinitions<W1> def2){
    return Coproduct.right(s,def2, SetXInstances.definitions());
  }
  public static  <T>  Active<set,T> allTypeclasses(SetX<T> s){
    return Active.of(s, SetXInstances.definitions());
  }
  public static  <W2,R,T> Nested<set,W2,R> mapM(SetX<T> s,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(s.map(fn), SetXInstances.definitions(), defs);
  }
  public static  <T> Kleisli<set,SetX<T>,T> kindKleisli(){
    return Kleisli.of(SetXInstances.monad(), SetX::widen);
  }

  public static  <T> Cokleisli<set,T,SetX<T>> kindCokleisli(){
    return Cokleisli.of(SetX::narrowK);
  }

  public static InstanceDefinitions<set> definitions(){

    return new InstanceDefinitions<set>() {
      @Override
      public <T, R> Functor<set> functor() {
        return SetXInstances.functor();
      }

      @Override
      public <T> Pure<set> unit() {
        return SetXInstances.unit();
      }

      @Override
      public <T, R> Applicative<set> applicative() {
        return SetXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<set> monad() {
        return SetXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<set>> monadZero() {
        return Option.some(SetXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<set>> monadPlus() {
        return Option.some(SetXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<set> monadRec() {
        return SetXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<set>> monadPlus(MonoidK<set> m) {
        return Option.some(SetXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<set> traverse() {
        return  SetXInstances.traverse();
      }

      @Override
      public <T> Foldable<set> foldable() {
        return SetXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<set>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<set>> unfoldable() {
        return Option.some(SetXInstances.unfoldable());
      }
    };

  }
  public static Unfoldable<set> unfoldable(){
    return new Unfoldable<set>() {
      @Override
      public <R, T> Higher<set, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return SetX.unfold(b,fn);
      }
    };
  }
  /**
   *
   * Transform a set, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  SetX<Integer> set = Sets.functor().map(i->i*2, SetX.widen(Arrays.asSet(1,2,3));
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
   *   SetX<Integer> set = Sets.unit()
  .unit("hello")
  .applyHKT(h->Sets.functor().map((String v) ->v.length(), h))
  .convert(SetX::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Sets
   */
  public static <T,R>Functor<set> functor(){
    BiFunction<SetX<T>,Function<? super T, ? extends R>,SetX<R>> map = SetXInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * SetX<String> set = Sets.unit()
  .unit("hello")
  .convert(SetX::narrowK3);

  //Arrays.asSet("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Sets
   */
  public static <T> Pure<set> unit(){
    return General.<set,T>unit(SetXInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.oath.cyclops.hkt.jdk.SetX.widen;
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
   * SetX<Function<Integer,Integer>> setFn =Sets.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(SetX::narrowK3);

  SetX<Integer> set = Sets.unit()
  .unit("hello")
  .applyHKT(h->Sets.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Sets.zippingApplicative().ap(setFn, h))
  .convert(SetX::narrowK3);

  //Arrays.asSet("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Sets
   */
  public static <T,R> Applicative<set> zippingApplicative(){
    BiFunction<SetX< Function<T, R>>,SetX<T>,SetX<R>> ap = SetXInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.oath.cyclops.hkt.jdk.SetX.widen;
   * SetX<Integer> set  = Sets.monad()
  .flatMap(i->widen(SetX.range(0,i)), widen(Arrays.asSet(1,2,3)))
  .convert(SetX::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    SetX<Integer> set = Sets.unit()
  .unit("hello")
  .applyHKT(h->Sets.monad().flatMap((String v) ->Sets.unit().unit(v.length()), h))
  .convert(SetX::narrowK3);

  //Arrays.asSet("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad functions for Sets
   */
  public static <T,R> Monad<set> monad(){

    BiFunction<Higher<set,T>,Function<? super T, ? extends Higher<set,R>>,Higher<set,R>> flatMap = SetXInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }
  public static <T,R> MonadRec<set> monadRec(){

    return new MonadRec<set>(){
      @Override
      public <T, R> Higher<set, R> tailRec(T initial, Function<? super T, ? extends Higher<set,? extends Either<T, R>>> fn) {
        return SetX.tailRec(initial,fn.andThen(SetX::narrowK));
      }
    };
  }
  /**
   *
   * <pre>
   * {@code
   *  SetX<String> set = Sets.unit()
  .unit("hello")
  .applyHKT(h->Sets.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(SetX::narrowK3);

  //Arrays.asSet("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<set> monadZero(){

    return General.monadZero(monad(), SetX.empty());
  }

  public static <T> MonadPlus<set> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.setXConcat());
  }

  public static <T> MonadPlus<set> monadPlus(MonoidK<set> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<set> traverse(){
    BiFunction<Applicative<C2>,SetX<Higher<C2, T>>,Higher<C2, SetX<T>>> sequenceFn = (ap, set) -> {

      Higher<C2,SetX<T>> identity = ap.unit(SetX.empty());

      BiFunction<Higher<C2,SetX<T>>,Higher<C2,T>,Higher<C2,SetX<T>>> combineToSet =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

      BinaryOperator<Higher<C2,SetX<T>>> combineSets = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> { l1.addAll(l2); return l1;}),a,b); ;

      return set.stream()
        .reduce(identity,
          combineToSet,
          combineSets);


    };
    BiFunction<Applicative<C2>,Higher<set,Higher<C2, T>>,Higher<C2, Higher<set,T>>> sequenceNarrow  =
      (a,b) -> SetXInstances.widen2(sequenceFn.apply(a, SetXInstances.narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = Sets.foldable()
  .foldLeft(0, (a,b)->a+b, SetX.widen(Arrays.asSet(1,2,3,4)));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T> Foldable<set> foldable(){
    return new Foldable<set>() {
      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<set, T> ds) {
        return  SetX.fromIterable(narrowK(ds)).foldRight(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<set, T> ds) {
        return  SetX.fromIterable(narrowK(ds)).foldLeft(monoid);
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<set, T> nestedA) {
        return narrowK(nestedA).<R>map(fn).foldLeft(mb);
      }
    };
  }

  private static  <T> SetX<T> concat(Set<T> l1, Set<T> l2){
    return SetX.setX(ReactiveSeq.fromStream(Stream.concat(l1.stream(),l2.stream())));
  }
  private static <T> SetX<T> of(T value){
    return SetX.of(value);
  }
  private static <T,R> SetX<R> ap(SetX<Function< T, R>> lt,  SetX<T> set){
    return SetX.fromIterable(lt).zip(set,(a,b)->a.apply(b));
  }
  private static <T,R> Higher<set,R> flatMap(Higher<set,T> lt, Function<? super T, ? extends  Higher<set,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(SetXInstances::narrowK));
  }
  private static <T,R> SetX<R> map(SetX<T> lt, Function<? super T, ? extends R> fn){
    return SetX.fromIterable(lt).map(fn);
  }



  /**
   * Widen a SetType nest inside another HKT encoded type
   *
   * @param flux HTK encoded type containing  a Set to widen
   * @return HKT encoded type with a widened Set
   */
  public static <C2, T> Higher<C2, Higher<set, T>> widen2(Higher<C2, SetX<T>> flux) {
    // a functor could be used (if C2 is a functor / one exists for C2 type)
    // instead of casting
    // cast seems safer as Higher<set,T> must be a SetX
    return (Higher) flux;
  }



  /**
   * Convert the raw Higher Kinded Type for SetType types into the SetType type definition class
   *
   * @param future HKT encoded set into a SetType
   * @return SetType
   */
  public static <T> SetX<T> narrowK(final Higher<set, T> future) {
    return (SetX<T>) future;
  }

  /**
   * Convert the HigherKindedType definition for a Set into
   *
   * @param completableSet Type Constructor to convert back into narrowed type
   * @return Set from Higher Kinded Type
   */
  public static <T> SetX<T> narrow(final Higher<set, T> completableSet) {

    return ((SetX<T>) completableSet);

  }
}
