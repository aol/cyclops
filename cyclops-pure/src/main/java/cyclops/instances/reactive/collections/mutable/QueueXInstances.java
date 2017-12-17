package cyclops.instances.reactive.collections.mutable;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.queue;
import com.oath.cyclops.hkt.Higher;
import cyclops.collections.mutable.QueueX;
import cyclops.companion.CyclopsCollectors;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
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

import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static cyclops.collections.mutable.QueueX.fromIterable;
import static cyclops.collections.mutable.QueueX.narrowK;

/**
 * Companion class for creating Type Class instances for working with Queues
 * @author johnmcclean
 *
 */
@UtilityClass
public class QueueXInstances {
  public static  <T> Kleisli<queue,QueueX<T>,T> kindKleisli(){
    return Kleisli.of(QueueXInstances.monad(), QueueX::widen);
  }

  public static  <T> Cokleisli<queue,T,QueueX<T>> kindCokleisli(){
    return Cokleisli.of(QueueX::narrowK);
  }
  public static <W1,T> Nested<queue,W1,T> nested(QueueX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, QueueXInstances.definitions(),def2);
  }
  public static  <W1,T> Product<queue,W1,T> product(QueueX<T> q,Active<W1,T> active){
    return Product.of(allTypeclasses(q),active);
  }
  public static  <W1,T> Coproduct<W1,queue,T> coproduct(QueueX<T> q,InstanceDefinitions<W1> def2){
    return Coproduct.right(q,def2, QueueXInstances.definitions());
  }
  public static <T> Active<queue,T> allTypeclasses(QueueX<T> q){
    return Active.of(q, QueueXInstances.definitions());
  }
  public static  <W2,R,T> Nested<queue,W2,R> mapM(QueueX<T> q,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(q.map(fn), QueueXInstances.definitions(), defs);
  }
  public static InstanceDefinitions<queue> definitions(){
    return new InstanceDefinitions<queue>() {
      @Override
      public <T, R> Functor<queue> functor() {
        return QueueXInstances.functor();
      }

      @Override
      public <T> Pure<queue> unit() {
        return QueueXInstances.unit();
      }

      @Override
      public <T, R> Applicative<queue> applicative() {
        return QueueXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<queue> monad() {
        return QueueXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<queue>> monadZero() {
        return Option.some(QueueXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<queue>> monadPlus() {
        return Option.some(QueueXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<queue> monadRec() {
        return QueueXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<queue>> monadPlus(MonoidK<queue> m) {
        return Option.some(QueueXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<queue> traverse() {
        return QueueXInstances.traverse();
      }

      @Override
      public <T> Foldable<queue> foldable() {
        return QueueXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<queue>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<queue>> unfoldable() {
        return Option.some(QueueXInstances.unfoldable());
      }
    };
  }
  public static Unfoldable<queue> unfoldable(){
    return new Unfoldable<queue>() {
      @Override
      public <R, T> Higher<queue, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return QueueX.unfold(b,fn);
      }
    };
  }
  /**
   *
   * Transform a queue, mulitplying every element by 2
   *
   * <pre>
   * {@code
   *  QueueX<Integer> queue = Queues.functor().map(i->i*2, QueueX.of(1,2,3));
   *
   *  //[2,4,6]
   *
   *
   * }
   * </pre>
   *
   * An example fluent api working with Queues
   * <pre>
   * {@code
   *   QueueX<Integer> queue = Queues.unit()
  .unit("hello")
  .applyHKT(h->Queues.functor().map((String v) ->v.length(), h))
  .convert(QueueX::narrowK3);
   *
   * }
   * </pre>
   *
   *
   * @return A functor for Queues
   */
  public static <T,R>Functor<queue> functor(){
    BiFunction<QueueX<T>,Function<? super T, ? extends R>,QueueX<R>> map = QueueXInstances::map;
    return General.functor(map);
  }
  /**
   * <pre>
   * {@code
   * QueueX<String> queue = Queues.unit()
  .unit("hello")
  .convert(QueueX::narrowK3);

  //QueueX.of("hello"))
   *
   * }
   * </pre>
   *
   *
   * @return A factory for Queues
   */
  public static <T> Pure<queue> unit(){
    return General.<queue,T>unit(QueueXInstances::of);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.QueueX.widen;
   * import static com.aol.cyclops.util.function.Lambda.l1;
   * import static java.util.QueueX.of;
   *
  Queues.zippingApplicative()
  .ap(widen(asQueue(l1(this::multiplyByTwo))),widen(asQueue(1,2,3)));
   *
   * //[2,4,6]
   * }
   * </pre>
   *
   *
   * Example fluent API
   * <pre>
   * {@code
   * QueueX<Function<Integer,Integer>> queueFn =Queues.unit()
   *                                                  .unit(Lambda.l1((Integer i) ->i*2))
   *                                                  .convert(QueueX::narrowK3);

  QueueX<Integer> queue = Queues.unit()
  .unit("hello")
  .applyHKT(h->Queues.functor().map((String v) ->v.length(), h))
  .applyHKT(h->Queues.zippingApplicative().ap(queueFn, h))
  .convert(QueueX::narrowK3);

  //QueueX.of("hello".length()*2))
   *
   * }
   * </pre>
   *
   *
   * @return A zipper for Queues
   */
  public static <T,R> Applicative<queue> zippingApplicative(){
    BiFunction<QueueX< Function<T, R>>,QueueX<T>,QueueX<R>> ap = QueueXInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }
  /**
   *
   * <pre>
   * {@code
   * import static com.aol.cyclops.hkt.jdk.QueueX.widen;
   * QueueX<Integer> queue  = Queues.monad()
  .flatMap(i->widen(QueueX.range(0,i)), widen(QueueX.of(1,2,3)))
  .convert(QueueX::narrowK3);
   * }
   * </pre>
   *
   * Example fluent API
   * <pre>
   * {@code
   *    QueueX<Integer> queue = Queues.unit()
  .unit("hello")
  .applyHKT(h->Queues.monad().flatMap((String v) ->Queues.unit().unit(v.length()), h))
  .convert(QueueX::narrowK3);

  //QueueX.of("hello".length())
   *
   * }
   * </pre>
   *
   * @return Type class with monad functions for Queues
   */
  public static <T,R> Monad<queue> monad(){

    BiFunction<Higher<queue,T>,Function<? super T, ? extends Higher<queue,R>>,Higher<queue,R>> flatMap = QueueXInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }
  /**
   *
   * <pre>
   * {@code
   *  QueueX<String> queue = Queues.unit()
  .unit("hello")
  .applyHKT(h->Queues.monadZero().filter((String t)->t.startsWith("he"), h))
  .convert(QueueX::narrowK3);

  //QueueX.of("hello"));
   *
   * }
   * </pre>
   *
   *
   * @return A filterable monad (with default value)
   */
  public static <T,R> MonadZero<queue> monadZero(){

    return General.monadZero(monad(), QueueX.empty());
  }

  public static <T> MonadPlus<queue> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.queueXConcat());
  }
  public static <T,R> MonadRec<queue> monadRec(){

    return new MonadRec<queue>(){
      @Override
      public <T, R> Higher<queue, R> tailRec(T initial, Function<? super T, ? extends Higher<queue,? extends Either<T, R>>> fn) {
        return QueueX.tailRec(initial,fn.andThen(QueueX::narrowK));
      }
    };
  }

  public static <T> MonadPlus<queue> monadPlus(MonoidK<queue> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<queue> traverse(){
    BiFunction<Applicative<C2>,QueueX<Higher<C2, T>>,Higher<C2, QueueX<T>>> sequenceFn = (ap,queue) -> {

      Higher<C2,QueueX<T>> identity = ap.unit(QueueX.of());

      BiFunction<Higher<C2,QueueX<T>>,Higher<C2,T>,Higher<C2,QueueX<T>>> combineToQueue =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.add(b); return a;}),acc,next);

      BinaryOperator<Higher<C2,QueueX<T>>> combineQueues = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> { l1.addAll(l2); return l1;}),a,b); ;

      return queue.stream()
        .reduce(identity,
          combineToQueue,
          combineQueues);


    };
    BiFunction<Applicative<C2>,Higher<queue,Higher<C2, T>>,Higher<C2, Higher<queue,T>>> sequenceNarrow  =
      (a,b) -> QueueX.widen2(sequenceFn.apply(a, narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  /**
   *
   * <pre>
   * {@code
   * int sum  = Queues.foldable()
  .foldLeft(0, (a,b)->a+b, QueueX.of(1,2,3,4)));

  //10
   *
   * }
   * </pre>
   *
   *
   * @return Type class for folding / reduction operations
   */
  public static <T> Foldable<queue> foldable(){
    return new Foldable<queue>() {
      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<queue, T> ds) {
        return  fromIterable(narrowK(ds)).foldRight(monoid);
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<queue, T> ds) {
        return  fromIterable(narrowK(ds)).foldLeft(monoid);
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<queue, T> nestedA) {
        return narrowK(nestedA).<R>map(fn).foldLeft(mb);
      }
    };
  }

  private static  <T> QueueX<T> concat(Queue<T> l1, Queue<T> l2){
    return Stream.concat(l1.stream(),l2.stream()).collect(CyclopsCollectors.toQueueX());
  }
  private <T> QueueX<T> of(T value){
    return QueueX.of(value);
  }
  private static <T,R> QueueX<R> ap(QueueX<Function< T, R>> lt,  QueueX<T> queue){
    return lt.zip(queue,(a,b)->a.apply(b));
  }
  private static <T,R> Higher<queue,R> flatMap(Higher<queue,T> lt, Function<? super T, ? extends  Higher<queue,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(QueueX::narrowK));
  }
  private static <T,R> QueueX<R> map(QueueX<T> lt, Function<? super T, ? extends R> fn){
    return lt.map(fn);
  }
}
