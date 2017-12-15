package cyclops.instances.reactive.collections.immutable;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.linkedListX;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.collections.immutable.LinkedListX;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
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

import static cyclops.collections.immutable.LinkedListX.narrowK;

/**
 * Companion class for creating Type Class instances for working with reactive LinkedListX's
 * @author johnmcclean
 *
 */
@UtilityClass
public class LinkedListXInstances {

  public static InstanceDefinitions<linkedListX> definitions(){
    return new InstanceDefinitions<linkedListX>() {
      @Override
      public <T, R> Functor<linkedListX> functor() {
        return LinkedListXInstances.functor();
      }

      @Override
      public <T> Pure<linkedListX> unit() {
        return LinkedListXInstances.unit();
      }

      @Override
      public <T, R> Applicative<linkedListX> applicative() {
        return LinkedListXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<linkedListX> monad() {
        return LinkedListXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<linkedListX>> monadZero() {
        return Option.some(LinkedListXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<linkedListX>> monadPlus() {
        return Option.some(LinkedListXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<linkedListX> monadRec() {
        return LinkedListXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<linkedListX>> monadPlus(MonoidK<linkedListX> m) {
        return Option.some(LinkedListXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<linkedListX> traverse() {
        return LinkedListXInstances.traverse();
      }

      @Override
      public <T> Foldable<linkedListX> foldable() {
        return LinkedListXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<linkedListX>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<linkedListX>> unfoldable() {
        return Option.some(LinkedListXInstances.unfoldable());
      }
    };
  }
  public static Unfoldable<linkedListX> unfoldable(){
    return new Unfoldable<linkedListX>() {
      @Override
      public <R, T> Higher<linkedListX, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return LinkedListX.unfold(b,fn);
      }
    };
  }

  public static <T,R>Functor<linkedListX> functor(){
    BiFunction<LinkedListX<T>,Function<? super T, ? extends R>,LinkedListX<R>> map = LinkedListXInstances::map;
    return General.functor(map);
  }

  public static <T> Pure<linkedListX> unit(){
    return General.<linkedListX,T>unit(LinkedListXInstances::of);
  }

  public static <T,R> Applicative<linkedListX> zippingApplicative(){
    BiFunction<LinkedListX< Function<T, R>>,LinkedListX<T>,LinkedListX<R>> ap = LinkedListXInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }

  public static <T,R> Monad<linkedListX> monad(){

    BiFunction<Higher<linkedListX,T>,Function<? super T, ? extends Higher<linkedListX,R>>,Higher<linkedListX,R>> flatMap = LinkedListXInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }

  public static <T,R> MonadZero<linkedListX> monadZero(){

    return General.monadZero(monad(), LinkedListX.empty());
  }

  public static <T> MonadPlus<linkedListX> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.linkedListXConcat());
  }
  public static <T,R> MonadRec<linkedListX> monadRec(){

    return new MonadRec<linkedListX>(){
      @Override
      public <T, R> Higher<linkedListX, R> tailRec(T initial, Function<? super T, ? extends Higher<linkedListX,? extends Either<T, R>>> fn) {
        return LinkedListX.tailRec(initial,fn.andThen(LinkedListX::narrowK));
      }
    };
  }

  public static MonadPlus<linkedListX> monadPlus(MonoidK<linkedListX> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<linkedListX> traverse(){
    BiFunction<Applicative<C2>,LinkedListX<Higher<C2, T>>,Higher<C2, LinkedListX<T>>> sequenceFn = (ap, list) -> {

      Higher<C2,LinkedListX<T>> identity = ap.unit(LinkedListX.empty());

      BiFunction<Higher<C2,LinkedListX<T>>,Higher<C2,T>,Higher<C2,LinkedListX<T>>> combineToPStack =   (acc, next) -> ap.apBiFn(ap.unit((a, b) ->a.plus(b)),acc,next);

      BinaryOperator<Higher<C2,LinkedListX<T>>> combinePStacks = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> l1.plusAll(l2)),a,b); ;


      return list.stream()
        .reverse()
        .reduce(identity,
          combineToPStack,
          combinePStacks);


    };
    BiFunction<Applicative<C2>,Higher<linkedListX,Higher<C2, T>>,Higher<C2, Higher<linkedListX,T>>> sequenceNarrow  =
      (a,b) -> LinkedListX.widen2(sequenceFn.apply(a, narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  public static <T,R> Foldable<linkedListX> foldable(){
    BiFunction<Monoid<T>,Higher<linkedListX,T>,T> foldRightFn =  (m, l)-> narrowK(l).foldRight(m);
    BiFunction<Monoid<T>,Higher<linkedListX,T>,T> foldLeftFn = (m, l)-> narrowK(l).reduce(m);
    Function3<Monoid<R>, Function<T, R>, Higher<linkedListX, T>, R> foldMapFn = (m, f, l)->narrowK(l).map(f).foldLeft(m);

    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }

  private static  <T> LinkedListX<T> concat(PersistentList<T> l1, PersistentList<T> l2){

    return LinkedListX.fromIterable(l1.plusAll(l2));
  }
  private <T> LinkedListX<T> of(T value){
    return LinkedListX.singleton(value);
  }
  private static <T,R> LinkedListX<R> ap(LinkedListX<Function< T, R>> lt, LinkedListX<T> list){
    return LinkedListX.fromIterable(lt).zip(list,(a, b)->a.apply(b));
  }
  private static <T,R> Higher<linkedListX,R> flatMap(Higher<linkedListX,T> lt, Function<? super T, ? extends  Higher<linkedListX,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(LinkedListX::narrowK));
  }
  private static <T,R> LinkedListX<R> map(LinkedListX<T> lt, Function<? super T, ? extends R> fn){
    return LinkedListX.fromIterable(lt).map(fn);
  }
}
