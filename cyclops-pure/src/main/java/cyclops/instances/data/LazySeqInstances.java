package cyclops.instances.data;

import com.oath.cyclops.hkt.DataWitness.lazySeq;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.LazySeq;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function3;
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

import static cyclops.data.LazySeq.narrowK;


/**
 * Companion class for creating Type Class instances for working with LazySeq's
 * @author johnmcclean
 *
 */
@UtilityClass
public class LazySeqInstances {
  public static  <T> Kleisli<lazySeq,LazySeq<T>,T> kindKleisli(){
    return Kleisli.of(LazySeqInstances.monad(), LazySeq::widen);
  }

  public static  <T> Cokleisli<lazySeq,T,LazySeq<T>> kindCokleisli(){
    return Cokleisli.of(LazySeq::narrowK);
  }
  public static <W1,T> Nested<lazySeq,W1,T> nested(LazySeq<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, LazySeqInstances.definitions(),def2);
  }
  public static <W1,T> Product<lazySeq,W1,T> product(LazySeq<T> l, Active<W1,T> active){
    return Product.of(allTypeclasses(l),active);
  }
  public static <W1,T> Coproduct<W1,lazySeq,T> coproduct(LazySeq<T> l, InstanceDefinitions<W1> def2){
    return Coproduct.right(l,def2, LazySeqInstances.definitions());
  }
  public static <T> Active<lazySeq,T> allTypeclasses(LazySeq<T> l){
    return Active.of(l, LazySeqInstances.definitions());
  }
  public static <W2,R,T> Nested<lazySeq,W2,R> mapM(LazySeq<T> l, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l.map(fn), LazySeqInstances.definitions(), defs);
  }

  public static InstanceDefinitions<lazySeq> definitions(){
    return new InstanceDefinitions<lazySeq>() {
      @Override
      public <T, R> Functor<lazySeq> functor() {
        return LazySeqInstances.functor();
      }

      @Override
      public <T> Pure<lazySeq> unit() {
        return LazySeqInstances.unit();
      }

      @Override
      public <T, R> Applicative<lazySeq> applicative() {
        return LazySeqInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<lazySeq> monad() {
        return LazySeqInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<lazySeq>> monadZero() {
        return Option.some(LazySeqInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<lazySeq>> monadPlus() {
        return Option.some(LazySeqInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<lazySeq> monadRec() {
        return LazySeqInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<lazySeq>> monadPlus(MonoidK<lazySeq> m) {
        return Option.some(LazySeqInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<lazySeq> traverse() {
        return LazySeqInstances.traverse();
      }

      @Override
      public <T> Foldable<lazySeq> foldable() {
        return LazySeqInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<lazySeq>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<lazySeq>> unfoldable() {
        return Option.some(LazySeqInstances.unfoldable());
      }
    };
  }
  public static Unfoldable<lazySeq> unfoldable(){
    return new Unfoldable<lazySeq>() {
      @Override
      public <R, T> Higher<lazySeq, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return LazySeq.unfold(b,fn);
      }
    };
  }

  public static <T,R>Functor<lazySeq> functor(){
    BiFunction<LazySeq<T>,Function<? super T, ? extends R>,LazySeq<R>> map = LazySeqInstances::map;
    return General.functor(map);
  }

  public static <T> Pure<lazySeq> unit(){
    return General.<lazySeq,T>unit(LazySeqInstances::of);
  }

  public static <T,R> Applicative<lazySeq> zippingApplicative(){
    BiFunction<LazySeq< Function<T, R>>,LazySeq<T>,LazySeq<R>> ap = LazySeqInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }

  public static <T,R> Monad<lazySeq> monad(){

    BiFunction<Higher<lazySeq,T>,Function<? super T, ? extends Higher<lazySeq,R>>,Higher<lazySeq,R>> flatMap = LazySeqInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }

  public static <T,R> MonadZero<lazySeq> monadZero(){

    return General.monadZero(monad(), LazySeq.empty());
  }

  public static <T> MonadPlus<lazySeq> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.lazySeqConcat());
  }
  public static <T,R> MonadRec<lazySeq> monadRec(){

    return new MonadRec<lazySeq>(){
      @Override
      public <T, R> Higher<lazySeq, R> tailRec(T initial, Function<? super T, ? extends Higher<lazySeq,? extends Either<T, R>>> fn) {
        return LazySeq.tailRec(initial,fn.andThen(LazySeq::narrowK));
      }
    };
  }

  public static MonadPlus<lazySeq> monadPlus(MonoidK<lazySeq> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<lazySeq> traverse(){
    BiFunction<Applicative<C2>,LazySeq<Higher<C2, T>>,Higher<C2, LazySeq<T>>> sequenceFn = (ap, list) -> {

      Higher<C2,LazySeq<T>> identity = ap.unit(LazySeq.empty());
     BiFunction<Higher<C2,T>,Higher<C2,LazySeq<T>>,Higher<C2,LazySeq<T>>> combineToPStack =   (acc, next) -> ap.apBiFn(ap.unit((a, b) ->a.plus(b)),next,acc);

      return list.foldRight(identity,combineToPStack);



    };
    BiFunction<Applicative<C2>,Higher<lazySeq,Higher<C2, T>>,Higher<C2, Higher<lazySeq,T>>> sequenceNarrow  =
      (a,b) -> LazySeq.widen2(sequenceFn.apply(a, narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  public static <T,R> Foldable<lazySeq> foldable(){
    BiFunction<Monoid<T>,Higher<lazySeq,T>,T> foldRightFn =  (m, l)-> narrowK(l).foldRight(m);
    BiFunction<Monoid<T>,Higher<lazySeq,T>,T> foldLeftFn = (m, l)-> narrowK(l).foldLeft(m);
    Function3<Monoid<R>, Function<T, R>, Higher<lazySeq, T>, R> foldMapFn = (m, f, l)->narrowK(l).map(f).foldLeft(m);

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
  private static <T,R> Higher<lazySeq,R> flatMap(Higher<lazySeq,T> lt, Function<? super T, ? extends  Higher<lazySeq,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(LazySeq::narrowK));
  }
  private static <T,R> LazySeq<R> map(LazySeq<T> lt, Function<? super T, ? extends R> fn){
    return LazySeq.fromIterable(lt).map(fn);
  }
}
