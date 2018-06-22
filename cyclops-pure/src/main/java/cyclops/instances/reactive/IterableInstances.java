package cyclops.instances.reactive;

import com.oath.cyclops.hkt.DataWitness.reactiveSeq;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.react.ThreadPools;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
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

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

//typeclass instances for iterable sequences
//via ReactiveSeq's iterable factory methods (pull based synchronous stream)
public class IterableInstances {

  public static <T> Higher<reactiveSeq,T> connectHKT(Iterable<T> p){
    return ReactiveSeq.fromIterable(p);
  }
  public static <T,R extends Iterable<T>> R convertHKT(Higher<reactiveSeq,T> hkt, Function<? super Iterable<T>,? extends R> fn){
    return fn.apply(ReactiveSeq.narrowK(hkt));
  }
  public static  <T> Kleisli<reactiveSeq,ReactiveSeq<T>,T> kindKleisli(){
    return Kleisli.of(IterableInstances.monad(), ReactiveSeq::widen);
  }

  public static  <T> Cokleisli<reactiveSeq,T,ReactiveSeq<T>> kindCokleisli(){
    return Cokleisli.of(ReactiveSeq::narrowK);
  }

  public static <W1,T> Nested<reactiveSeq,W1,T> nested(ReactiveSeq<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, IterableInstances.definitions(),def2);
  }
  public static  <W1,T> Product<reactiveSeq,W1,T> product(Iterable<T> it, Active<W1,T> active){
    return Product.of(allTypeclasses(it),active);
  }
  public static  <W1,T> Product<reactiveSeq,W1,T> product(Iterable<T> it,Active<W1,T> active, Executor ex){
    return Product.of(allTypeclasses(it,ex),active);
  }
  public static  <W1,T> Coproduct<W1,reactiveSeq,T> coproduct(Iterable<T> it, InstanceDefinitions<W1> def2){
    ReactiveSeq<T> r = ReactiveSeq.fromIterable(it);
    return Coproduct.right(r,def2, IterableInstances.definitions());
  }
  public static  <T> Active<reactiveSeq,T> allTypeclasses(Iterable<T> it,Executor ex){
    ReactiveSeq<T> r = ReactiveSeq.fromIterable(it);
    return Active.of(r, r.fold(sync-> IterableInstances.definitions(), rs-> PublisherInstances.definitions(ex), ac-> PublisherInstances.definitions(ex)));
  }
  public static  <T> Active<reactiveSeq,T> allTypeclasses(Iterable<T> it){
    ReactiveSeq<T> r = ReactiveSeq.fromIterable(it);
    return Active.of(r, r.fold(sync-> IterableInstances.definitions(), rs-> PublisherInstances.definitions(ThreadPools.getCurrentThreadExecutor()), ac-> PublisherInstances.definitions(ThreadPools.getCurrentThreadExecutor())));
  }
  public static  <W2,R,T> Nested<reactiveSeq,W2,R> mapM(Iterable<T> it,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    ReactiveSeq<T> r = ReactiveSeq.fromIterable(it);
    return Nested.of(r.map(fn), IterableInstances.definitions(), defs);
  }

  public static InstanceDefinitions<reactiveSeq> definitions(){
    return new InstanceDefinitions<reactiveSeq>() {
      @Override
      public <T, R> Functor<reactiveSeq> functor() {
        return IterableInstances.functor();
      }

      @Override
      public <T> Pure<reactiveSeq> unit() {
        return IterableInstances.unit();
      }

      @Override
      public <T, R> Applicative<reactiveSeq> applicative() {
        return IterableInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<reactiveSeq> monad() {
        return IterableInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<reactiveSeq>> monadZero() {
        return Option.some(IterableInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<reactiveSeq>> monadPlus() {
        return Option.some(IterableInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<reactiveSeq> monadRec() {
        return IterableInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<reactiveSeq>> monadPlus(MonoidK<reactiveSeq> m) {
        return Option.some(IterableInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<reactiveSeq> traverse() {
        return  IterableInstances.traverse();
      }

      @Override
      public <T> Foldable<reactiveSeq> foldable() {
        return IterableInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<reactiveSeq>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<reactiveSeq>> unfoldable() {
        return Option.some(IterableInstances.unfoldable());
      }
    };
  }
  public static Unfoldable<reactiveSeq> unfoldable(){
    return new Unfoldable<reactiveSeq>() {
      @Override
      public <R, T> Higher<reactiveSeq, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return ReactiveSeq.unfold(b,fn);
      }
    };
  }
  /**
   *
   * Transform a list, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  ReactiveSeq<Integer> list = Lists.functor().map(i->i*2, ReactiveSeq.widen(Arrays.asList(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with Lists
   * <pre>
   * {@code
   *   ReactiveSeq<Integer> list = ReactiveSeq.Instances.unit()
  .unit("hello")
  .applyHKT(h->Lists.functor().map((String v) ->v.length(), h))
  .convert(ReactiveSeq::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Lists
   */
  public static <T,R>Functor<reactiveSeq> functor(){
    BiFunction<ReactiveSeq<T>,Function<? super T, ? extends R>,ReactiveSeq<R>> map = IterableInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * ReactiveSeq<String> list = Lists.unit()
  .unit("hello")
  .convert(ReactiveSeq::narrowK3);

  //Arrays.asList("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Lists
   */
  public static <T> Pure<reactiveSeq> unit(){
    return General.<reactiveSeq,T>unit(IterableInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.oath.cyclops.hkt.jdk.ReactiveSeq.widen;
   * import static com.oath.cyclops.util.function.Lambda.l1;
   * import static java.util.Arrays.asList;
   *
  Lists.zippingApplicative()
  .ap(widen(asList(l1(this::multiplyByTwo))),widen(asList(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * ReactiveSeq<Function<Integer,Integer>> listFn =Lists.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(ReactiveSeq::narrowK3);

  ReactiveSeq<Integer> list = Lists.unit()
  .unit("hello")
  .applyHKT(h->Lists.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Lists.zippingApplicative().ap(listFn, h))
  .convert(ReactiveSeq::narrowK3);

  //Arrays.asList("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Lists
   */
  public static <T,R> Applicative<reactiveSeq> zippingApplicative(){
    BiFunction<ReactiveSeq< Function<T, R>>,ReactiveSeq<T>,ReactiveSeq<R>> ap = IterableInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.oath.cyclops.hkt.jdk.ReactiveSeq.widen;
   * ReactiveSeq<Integer> list  = Lists.monad()
  .flatMap(i->widen(ReactiveSeq.range(0,i)), widen(Arrays.asList(1,2,3)))
  .convert(ReactiveSeq::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    ReactiveSeq<Integer> list = Lists.unit()
  .unit("hello")
  .applyHKT(h->Lists.monad().flatMap((String v) ->Lists.unit().unit(v.length()), h))
  .convert(ReactiveSeq::narrowK3);

  //Arrays.asList("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad arrow for Lists
   */
  public static <T,R> Monad<reactiveSeq> monad(){

    BiFunction<Higher<reactiveSeq,T>,Function<? super T, ? extends Higher<reactiveSeq,R>>,Higher<reactiveSeq,R>> flatMap = IterableInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  ReactiveSeq<String> list = Lists.unit()
  .unit("hello")
  .applyHKT(h->Lists.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(ReactiveSeq::narrowK3);

  //Arrays.asList("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<reactiveSeq> monadZero(){

    return General.monadZero(monad(), ReactiveSeq.empty());
  }

  public static <T> MonadPlus<reactiveSeq> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.combineReactiveSeq());
  }
  public static <T,R> MonadRec<reactiveSeq> monadRec(){

    return new MonadRec<reactiveSeq>(){
      @Override
      public <T, R> Higher<reactiveSeq, R> tailRec(T initial, Function<? super T, ? extends Higher<reactiveSeq,? extends Either<T, R>>> fn) {
        return ReactiveSeq.tailRec(initial,fn.andThen(ReactiveSeq::narrowK));
      }
    };
  }

  public static <T> MonadPlus<reactiveSeq> monadPlus(MonoidK<reactiveSeq> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<reactiveSeq> traverse(){
    BiFunction<Applicative<C2>,ReactiveSeq<Higher<C2, T>>,Higher<C2, ReactiveSeq<T>>> sequenceFn = (ap,list) -> {

      Higher<C2,ReactiveSeq<T>> identity = ap.unit(ReactiveSeq.empty());

      BiFunction<Higher<C2,ReactiveSeq<T>>,Higher<C2,T>,Higher<C2,ReactiveSeq<T>>> combineToList =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.append(b); return a;}),acc,next);

      BinaryOperator<Higher<C2,ReactiveSeq<T>>> combineLists = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> { l1.appendStream(l2); return l1;}),a,b); ;

      return list.stream()
        .reduce(identity,
          combineToList,
          combineLists);


    };
    BiFunction<Applicative<C2>,Higher<reactiveSeq,Higher<C2, T>>,Higher<C2, Higher<reactiveSeq,T>>> sequenceNarrow  =
      (a,b) -> IterableInstances.widen2(sequenceFn.apply(a, ReactiveSeq.narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = Lists.foldable()
  .foldLeft(0, (a,b)->a+b, ReactiveSeq.of(1,2,3,4));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T,R> Foldable<reactiveSeq> foldable(){
    BiFunction<Monoid<T>,Higher<reactiveSeq,T>,T> foldRightFn =  (m, l)-> narrow(l).foldRight(m);
    BiFunction<Monoid<T>,Higher<reactiveSeq,T>,T> foldLeftFn = (m, l)-> narrow(l).reduce(m);
    Function3<Monoid<R>, Function<T, R>, Higher<reactiveSeq, T>, R> foldMapFn = (m, f, l)->ReactiveSeq.narrowK(l).map(f).foldLeft(m);
    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }

  private static  <T> ReactiveSeq<T> concat(ReactiveSeq<T> l1, ReactiveSeq<T> l2){
    return ReactiveSeq.concat(l1.stream(),l2.stream());
  }
  private static <T> ReactiveSeq<T> of(T value){
    return ReactiveSeq.of(value);
  }
  private static <T,R> ReactiveSeq<R> ap(ReactiveSeq<Function< T, R>> lt,  ReactiveSeq<T> list){
    return lt.zip(list,(a,b)->a.apply(b));
  }
  private static <T,R> Higher<reactiveSeq,R> flatMap(Higher<reactiveSeq,T> lt, Function<? super T, ? extends  Higher<reactiveSeq,R>> fn){
    return ReactiveSeq.narrowK(lt).flatMap(fn.andThen(ReactiveSeq::narrowK));
  }
  private static <T,R> ReactiveSeq<R> map(ReactiveSeq<T> lt, Function<? super T, ? extends R> fn){
    return lt.map(fn);
  }




  public static <T>  Higher<reactiveSeq, T> widen(ReactiveSeq<T> stream) {
    return stream;
  }

  /**
   * Widen a ReactiveSeq nest inside another HKT encoded type
   *
   * @param flux HTK encoded type containing  a List to widen
   * @return HKT encoded type with a widened List
   */
  public static <C2, T> Higher<C2, Higher<reactiveSeq, T>> widen2(Higher<C2, ReactiveSeq<T>> flux) {
    // a functor could be used (if C2 is a functor / one exists for C2 type)
    // instead of casting
    // cast seems safer as Higher<reactiveSeq,T> must be a ReactiveSeq
    return (Higher) flux;
  }



  /**
   * Convert the HigherKindedType definition for a List into
   *
   * @param completableList Type Constructor to convert back into narrowed type
   * @return List from Higher Kinded Type
   */
  public static <T> ReactiveSeq<T> narrow(final Higher<reactiveSeq, T> completableList) {

    return ((ReactiveSeq<T>) completableList);

  }
}
