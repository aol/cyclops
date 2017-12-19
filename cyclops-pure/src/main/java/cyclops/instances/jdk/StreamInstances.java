package cyclops.instances.jdk;

import com.oath.cyclops.hkt.DataWitness.stream;
import com.oath.cyclops.hkt.Higher;
import cyclops.companion.Streams;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.kinds.StreamKind;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;

import java.util.function.*;
import java.util.stream.Stream;
/**
 * Companion class for creating Type Class instances for working with Streams
 * @author johnmcclean
 *
 */
@UtilityClass
public  class StreamInstances {

  public static InstanceDefinitions<stream> definitions(){
    return new InstanceDefinitions<stream>() {
      @Override
      public <T, R> Functor<stream> functor() {
        return StreamInstances.functor();
      }

      @Override
      public <T> Pure<stream> unit() {
        return StreamInstances.unit();
      }

      @Override
      public <T, R> Applicative<stream> applicative() {
        return StreamInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<stream> monad() {
        return StreamInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<stream>> monadZero() {
        return Option.some(StreamInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<stream>> monadPlus() {
        return Option.some(StreamInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<stream> monadRec() {
        return StreamInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<stream>> monadPlus(MonoidK<stream> m) {
        return Option.some(StreamInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<stream> traverse() {
        return StreamInstances.traverse();
      }

      @Override
      public <T> Foldable<stream> foldable() {
        return StreamInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<stream>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<stream>> unfoldable() {
        return Option.some(StreamInstances.unfoldable());
      }
    };
  }
  public static Unfoldable<stream> unfoldable(){
    return new Unfoldable<stream>() {
      @Override
      public <R, T> Higher<stream, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return StreamKind.widen(ReactiveSeq.unfold(b,fn));
      }
    };
  }
  /*
   *
   * Transform a list, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  StreamKind<Integer> list = Streams.functor().map(i->i*2, StreamKind.widen(Stream.of(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with Streams
   * <pre>
   * {@code
   *   StreamKind<Integer> list = Streams.unit()
  .unit("hello")
  .applyHKT(h->Streams.functor().map((String v) ->v.length(), h))
  .convert(StreamKind::narrow);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Streams
   */
  public static <T,R>Functor<stream> functor(){
    BiFunction<StreamKind<T>,Function<? super T, ? extends R>,StreamKind<R>> map = StreamInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * StreamKind<String> list = Streams.unit()
  .unit("hello")
  .convert(StreamKind::narrow);

  //Stream.of("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Streams
   */
  public static <T> Pure<stream> unit(){
    return General.<stream,T>unit(StreamInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.StreamKind.widen;
   * import static com.aol.cyclops.util.function.Lambda.l1;
   *
  Streams.zippingApplicative()
  .ap(widen(Stream.of(l1(this::multiplyByTwo))),widen(Stream.of(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * StreamKind<Function<Integer,Integer>> listFn =Streams.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(StreamKind::narrow);

  StreamKind<Integer> list = Streams.unit()
  .unit("hello")
  .applyHKT(h->Streams.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Streams.zippingApplicative().ap(listFn, h))
  .convert(StreamKind::narrow);

  //Stream.of("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Streams
   */
  public static <T,R> Applicative<stream> zippingApplicative(){
    BiFunction<StreamKind< Function<T, R>>,StreamKind<T>,StreamKind<R>> ap = StreamInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.StreamKind.widen;
   * StreamKind<Integer> list  = Streams.monad()
  .flatMap(i->widen(StreamX.range(0,i)), widen(Stream.of(1,2,3)))
  .convert(StreamKind::narrow);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    StreamKind<Integer> list = Streams.unit()
  .unit("hello")
  .applyHKT(h->Streams.monad().flatMap((String v) ->Streams.unit().unit(v.length()), h))
  .convert(StreamKind::narrow);

  //Stream.of("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad arrow for Streams
   */
  public static <T,R> Monad<stream> monad(){

    BiFunction<Higher<stream,T>,Function<? super T, ? extends Higher<stream,R>>,Higher<stream,R>> flatMap = StreamInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  StreamKind<String> list = Streams.unit()
  .unit("hello")
  .applyHKT(h->Streams.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(StreamKind::narrow);

  //Stream.of("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<stream> monadZero(){
    BiFunction<Higher<stream,T>,Predicate<? super T>,Higher<stream,T>> filter = StreamInstances::filter;
    Supplier<Higher<stream, T>> zero = ()->StreamKind.widen(Stream.of());
    return General.<stream,T,R>monadZero(monad(), zero,filter);
  }
  public static <T,R> MonadRec<stream> monadRec() {

    return new MonadRec<stream>() {
      @Override
      public <T, R> Higher<stream, R> tailRec(T initial, Function<? super T, ? extends Higher<stream, ? extends Either<T, R>>> fn) {
        return StreamKind.widen(ReactiveSeq.tailRec(initial, fn.andThen(s -> ReactiveSeq.fromStream(StreamKind.narrowK(s)))));
      }
    };
  }
  public static <T> MonadPlus<stream> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.combineStream());
  }

  public static <T> MonadPlus<stream> monadPlus(MonoidK<stream> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<stream> traverse(){
    BiFunction<Applicative<C2>,StreamKind<Higher<C2, T>>,Higher<C2, StreamKind<T>>> sequenceFn = (ap,list) -> {

      Higher<C2,StreamKind<T>> identity = ap.unit(StreamKind.widen(Stream.of()));

      BiFunction<Higher<C2,StreamKind<T>>,Higher<C2,T>,Higher<C2,StreamKind<T>>> combineToStream =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> StreamKind.widen(Stream.concat(a,Stream.of(b)))),acc,next);

      BinaryOperator<Higher<C2,StreamKind<T>>> combineStreams = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> { return StreamKind.widen(Stream.concat(l1,l2));}),a,b); ;

      return list.reduce(identity,
        combineToStream,
        combineStreams);


    };
    BiFunction<Applicative<C2>,Higher<stream,Higher<C2, T>>,Higher<C2, Higher<stream,T>>> sequenceNarrow  =
      (a,b) -> StreamKind.widen2(sequenceFn.apply(a, StreamKind.narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = Streams.foldable()
  .foldLeft(0, (a,b)->a+b, StreamKind.widen(Stream.of(1,2,3,4)));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T,R> Foldable<stream> foldable(){
    BiFunction<Monoid<T>,Higher<stream,T>,T> foldRightFn =  (m, l)-> ReactiveSeq.fromStream(StreamKind.narrowK(l)).foldRight(m);
    BiFunction<Monoid<T>,Higher<stream,T>,T> foldLeftFn = (m, l)-> ReactiveSeq.fromStream(StreamKind.narrowK(l)).reduce(m);
    Function3<Monoid<R>, Function<T, R>, Higher<stream, T>, R> foldMapFn = (m, f, l)->StreamKind.narrowK(l).map(f).reduce(m.zero(),m);
    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }

  private static  <T> StreamKind<T> concat(Stream<T> l1, Stream<T> l2){
    return StreamKind.widen(Stream.concat(l1,l2));
  }
  private <T> StreamKind<T> of(T value){
    return StreamKind.widen(Stream.of(value));
  }
  private static <T,R> StreamKind<R> ap(StreamKind<Function< T, R>> lt, StreamKind<T> list){
    return StreamKind.widen(Streams.zipStream(lt,list,(a, b)->a.apply(b)));
  }
  private static <T,R> Higher<stream,R> flatMap(Higher<stream,T> lt, Function<? super T, ? extends  Higher<stream,R>> fn){
    return StreamKind.widen(StreamKind.narrow(lt).flatMap(fn.andThen(StreamKind::narrow)));
  }
  private static <T,R> StreamKind<R> map(StreamKind<T> lt, Function<? super T, ? extends R> fn){
    return StreamKind.widen(lt.map(fn));
  }
  private static <T> StreamKind<T> filter(Higher<stream,T> lt, Predicate<? super T> fn){
    return StreamKind.widen(StreamKind.narrowK(lt).filter(fn));
  }
}
