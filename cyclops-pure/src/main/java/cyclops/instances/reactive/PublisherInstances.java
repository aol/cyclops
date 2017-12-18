package cyclops.instances.reactive;

import com.oath.cyclops.hkt.DataWitness.reactiveSeq;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.react.ThreadPools;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functions.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import org.reactivestreams.Publisher;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

//typeclass instances for reactive-streams publishers
//via ReactiveSeq Spouts (push based async reactive-streams)
public class PublisherInstances {
  public static <T> Higher<reactiveSeq,T> connectHKT(Publisher<T> p){
    return Spouts.from(p);
  }
  public static <T,R extends Publisher<T>> R convertHKT(Higher<reactiveSeq,T> hkt, Function<? super Publisher<T>,? extends R> fn){
    return fn.apply(Spouts.narrowK(hkt));
  }
  public static InstanceDefinitions<reactiveSeq> definitions(Executor ex){
    return new InstanceDefinitions<reactiveSeq>() {
      @Override
      public <T, R> Functor<reactiveSeq> functor() {
        return PublisherInstances.functor();
      }

      @Override
      public <T> Pure<reactiveSeq> unit() {
        return PublisherInstances.unit();
      }

      @Override
      public <T, R> Applicative<reactiveSeq> applicative() {
        return PublisherInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<reactiveSeq> monad() {
        return PublisherInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<reactiveSeq>> monadZero() {
        return Option.some(PublisherInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<reactiveSeq>> monadPlus() {
        return Option.some(PublisherInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<reactiveSeq> monadRec() {
        return PublisherInstances.monadRec(ex);
      }

      @Override
      public <T> Option<MonadPlus<reactiveSeq>> monadPlus(MonoidK<reactiveSeq> m) {
        return Option.some(PublisherInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<reactiveSeq> traverse() {
        return PublisherInstances.traverse();
      }

      @Override
      public <T> Foldable<reactiveSeq> foldable() {
        return PublisherInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<reactiveSeq>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<reactiveSeq>> unfoldable() {
        return Maybe.just(PublisherInstances.unfoldable(ex));
      }
    };
  }
  public static InstanceDefinitions<reactiveSeq> definitions(){
    return definitions(ThreadPools.getCurrentThreadExecutor());
  }
  public static Unfoldable<reactiveSeq> unfoldable(Executor ex){
    return new Unfoldable<reactiveSeq>() {
      @Override
      public <R, T> Higher<reactiveSeq, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return Spouts.reactive(Spouts.unfold(b,fn),ex);
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
  .map(h->Lists.functor().map((String v) ->v.length(), h))
  .convert(ReactiveSeq::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Lists
   */
  public static <T,R>Functor<reactiveSeq> functor(){
    BiFunction<ReactiveSeq<T>,Function<? super T, ? extends R>,ReactiveSeq<R>> map = PublisherInstances::map;
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
    return General.<reactiveSeq,T>unit(PublisherInstances::of);
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
  .map(h->Lists.functor().map((String v) ->v.length(), h))
  .map(h->Lists.zippingApplicative().ap(listFn, h))
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
    BiFunction<ReactiveSeq< Function<T, R>>,ReactiveSeq<T>,ReactiveSeq<R>> ap = PublisherInstances::ap;
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
  .map(h->Lists.monad().flatMap((String v) ->Lists.unit().unit(v.length()), h))
  .convert(ReactiveSeq::narrowK3);

  //Arrays.asList("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad functions for Lists
   */
  public static <T,R> Monad<reactiveSeq> monad(){

    BiFunction<Higher<reactiveSeq,T>,Function<? super T, ? extends Higher<reactiveSeq,R>>,Higher<reactiveSeq,R>> flatMap = PublisherInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  ReactiveSeq<String> list = Lists.unit()
  .unit("hello")
  .map(h->Lists.monadZero().filter((String t)->t.startsWith("he"), h))
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
  /**
   * <pre>
   * {@code
   *  ReactiveSeq<Integer> list = Lists.<Integer>monadPlus()
  .plus(ReactiveSeq.widen(Arrays.asList()), ReactiveSeq.widen(Arrays.asList(10)))
  .convert(ReactiveSeq::narrowK3);
  //Arrays.asList(10))
   *
   * }
   * </pre>
   * @return Type class for combining Lists by concatenation
   */
  public static <T> MonadPlus<reactiveSeq> monadPlus(){
    return General.monadPlus(monadZero(), MonoidKs.combineReactiveSeq());
  }
  /**
   *
   * <pre>
   * {@code
   *  Monoid<ReactiveSeq<Integer>> m = Monoid.of(ReactiveSeq.widen(Arrays.asList()), (a,b)->a.isEmpty() ? b : a);
  ReactiveSeq<Integer> list = Lists.<Integer>monadPlus(m)
  .plus(ReactiveSeq.widen(Arrays.asList(5)), ReactiveSeq.widen(Arrays.asList(10)))
  .convert(ReactiveSeq::narrowK3);
  //Arrays.asList(5))
   *
   * }
   * </pre>
   *
   * @param m Monoid to use for combining Lists
   * @return Type class for combining Lists
   */
  public static <T> MonadPlus<reactiveSeq> monadPlus(MonoidK<reactiveSeq> m){

    return General.monadPlus(monadZero(),m);
  }
  public static <T,R> MonadRec<reactiveSeq> monadRec(Executor ex){

    return new MonadRec<reactiveSeq>(){
      @Override
      public <T, R> Higher<reactiveSeq, R> tailRec(T initial, Function<? super T, ? extends Higher<reactiveSeq,? extends Either<T, R>>> fn) {
        return  Spouts.reactive(ReactiveSeq.deferFromStream( ()-> ReactiveSeq.tailRec(initial, fn.andThen(ReactiveSeq::narrowK))),ex);

      }
    };
  }
  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<reactiveSeq> traverse(){
    BiFunction<Applicative<C2>,ReactiveSeq<Higher<C2, T>>,Higher<C2, ReactiveSeq<T>>> sequenceFn = (ap,list) -> {

      Higher<C2,ReactiveSeq<T>> identity = ap.unit(Spouts.empty());

      BiFunction<Higher<C2,ReactiveSeq<T>>,Higher<C2,T>,Higher<C2,ReactiveSeq<T>>> combineToList =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.appendAll(b); return a;}),acc,next);

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
    Function3<Monoid<R>, Function<T, R>, Higher<reactiveSeq, T>, R> foldMapFn = (m, f, l)->Spouts.narrowK(l).map(f).foldLeft(m);
    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }

  private static  <T> ReactiveSeq<T> concat(ReactiveSeq<T> l1, ReactiveSeq<T> l2){
    return Spouts.concat(l1.stream(),l2.stream());
  }
  private static <T> ReactiveSeq<T> of(T value){
    return Spouts.of(value);
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
