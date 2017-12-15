package cyclops.instances.data;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentList;

import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.LazySeq;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function3;
import cyclops.function.Monoid;
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
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static cyclops.data.LazySeq.narrowK;


/**
 * Companion class for creating Type Class instances for working with LazySeq's
 * @author johnmcclean
 *
 */
@UtilityClass
public class LazySeqInstances {

  public static InstanceDefinitions<DataWitness.lazySeq> definitions(){
    return new InstanceDefinitions<DataWitness.lazySeq>() {
      @Override
      public <T, R> Functor<DataWitness.lazySeq> functor() {
        return LazySeqInstances.functor();
      }

      @Override
      public <T> Pure<DataWitness.lazySeq> unit() {
        return LazySeqInstances.unit();
      }

      @Override
      public <T, R> Applicative<DataWitness.lazySeq> applicative() {
        return LazySeqInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<DataWitness.lazySeq> monad() {
        return LazySeqInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<DataWitness.lazySeq>> monadZero() {
        return Option.some(LazySeqInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<DataWitness.lazySeq>> monadPlus() {
        return Option.some(LazySeqInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<DataWitness.lazySeq> monadRec() {
        return LazySeqInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<DataWitness.lazySeq>> monadPlus(MonoidK<DataWitness.lazySeq> m) {
        return Option.some(LazySeqInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<DataWitness.lazySeq> traverse() {
        return LazySeqInstances.traverse();
      }

      @Override
      public <T> Foldable<DataWitness.lazySeq> foldable() {
        return LazySeqInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<DataWitness.lazySeq>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<DataWitness.lazySeq>> unfoldable() {
        return Option.some(LazySeqInstances.unfoldable());
      }
    };
  }
  public static Unfoldable<DataWitness.lazySeq> unfoldable(){
    return new Unfoldable<DataWitness.lazySeq>() {
      @Override
      public <R, T> Higher<DataWitness.lazySeq, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return LazySeq.unfold(b,fn);
      }
    };
  }

  public static <T,R>Functor<DataWitness.lazySeq> functor(){
    BiFunction<LazySeq<T>,Function<? super T, ? extends R>,LazySeq<R>> map = LazySeqInstances::map;
    return General.functor(map);
  }

  public static <T> Pure<DataWitness.lazySeq> unit(){
    return General.<DataWitness.lazySeq,T>unit(LazySeqInstances::of);
  }

  public static <T,R> Applicative<DataWitness.lazySeq> zippingApplicative(){
    BiFunction<LazySeq< Function<T, R>>,LazySeq<T>,LazySeq<R>> ap = LazySeqInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }

  public static <T,R> Monad<DataWitness.lazySeq> monad(){

    BiFunction<Higher<DataWitness.lazySeq,T>,Function<? super T, ? extends Higher<DataWitness.lazySeq,R>>,Higher<DataWitness.lazySeq,R>> flatMap = LazySeqInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }

  public static <T,R> MonadZero<DataWitness.lazySeq> monadZero(){

    return General.monadZero(monad(), LazySeq.empty());
  }

  public static <T> MonadPlus<DataWitness.lazySeq> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.lazySeqConcat());
  }
  public static <T,R> MonadRec<DataWitness.lazySeq> monadRec(){

    return new MonadRec<DataWitness.lazySeq>(){
      @Override
      public <T, R> Higher<DataWitness.lazySeq, R> tailRec(T initial, Function<? super T, ? extends Higher<DataWitness.lazySeq,? extends Either<T, R>>> fn) {
        return LazySeq.tailRec(initial,fn.andThen(LazySeq::narrowK));
      }
    };
  }

  public static MonadPlus<DataWitness.lazySeq> monadPlus(MonoidK<DataWitness.lazySeq> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<DataWitness.lazySeq> traverse(){
    BiFunction<Applicative<C2>,LazySeq<Higher<C2, T>>,Higher<C2, LazySeq<T>>> sequenceFn = (ap, list) -> {

      Higher<C2,LazySeq<T>> identity = ap.unit(LazySeq.empty());

      BiFunction<Higher<C2,LazySeq<T>>,Higher<C2,T>,Higher<C2,LazySeq<T>>> combineToPStack =   (acc, next) -> ap.apBiFn(ap.unit((a, b) ->a.plus(b)),acc,next);

      BinaryOperator<Higher<C2,LazySeq<T>>> combinePStacks = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> l1.plusAll(l2)),a,b); ;


      return list.stream()
        .reverse()
        .reduce(identity,
          combineToPStack,
          combinePStacks);


    };
    BiFunction<Applicative<C2>,Higher<DataWitness.lazySeq,Higher<C2, T>>,Higher<C2, Higher<DataWitness.lazySeq,T>>> sequenceNarrow  =
      (a,b) -> LazySeq.widen2(sequenceFn.apply(a, narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  public static <T,R> Foldable<DataWitness.lazySeq> foldable(){
    BiFunction<Monoid<T>,Higher<DataWitness.lazySeq,T>,T> foldRightFn =  (m, l)-> narrowK(l).foldRight(m);
    BiFunction<Monoid<T>,Higher<DataWitness.lazySeq,T>,T> foldLeftFn = (m, l)-> narrowK(l).reduce(m);
    Function3<Monoid<R>, Function<T, R>, Higher<DataWitness.lazySeq, T>, R> foldMapFn = (m, f, l)->narrowK(l).map(f).foldLeft(m);

    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }

  private static  <T> LazySeq<T> concat(PersistentList<T> l1, PersistentList<T> l2){

    return LazySeq.fromIterable(l1.plusAll(l2));
  }
  private <T> LazySeq<T> of(T value){
    return LazySeq.of(value);
  }
  private static <T,R> LazySeq<R> ap(LazySeq<Function< T, R>> lt, LazySeq<T> list){
    return LazySeq.fromIterable(lt).zip(list,(a, b)->a.apply(b));
  }
  private static <T,R> Higher<DataWitness.lazySeq,R> flatMap(Higher<DataWitness.lazySeq,T> lt, Function<? super T, ? extends  Higher<DataWitness.lazySeq,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(LazySeq::narrowK));
  }
  private static <T,R> LazySeq<R> map(LazySeq<T> lt, Function<? super T, ? extends R> fn){
    return LazySeq.fromIterable(lt).map(fn);
  }
}
