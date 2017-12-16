package cyclops.instances.data;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.vector;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.Vector;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function3;
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

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static cyclops.data.Vector.narrowK;


/**
 * Companion class for creating Type Class instances for working with Vector's
 * @author johnmcclean
 *
 */
@UtilityClass
public class VectorInstances {

  public static  <T> Kleisli<vector,Vector<T>,T> kindKleisli(){
    return Kleisli.of(VectorInstances.monad(), Vector::widen);
  }

  public static  <T> Cokleisli<vector,T,Vector<T>> kindCokleisli(){
    return Cokleisli.of(Vector::narrowK);
  }
  public static <W1,T> Nested<vector,W1,T> nested(Vector<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, VectorInstances.definitions(),def2);
  }
  public static <W1,T> Product<vector,W1,T> product(Vector<T> l, Active<W1,T> active){
    return Product.of(allTypeclasses(l),active);
  }
  public static <W1,T> Coproduct<W1,vector,T> coproduct(Vector<T> l, InstanceDefinitions<W1> def2){
    return Coproduct.right(l,def2, VectorInstances.definitions());
  }
  public static <T> Active<vector,T> allTypeclasses(Vector<T> l){
    return Active.of(l, VectorInstances.definitions());
  }
  public static <W2,R,T> Nested<vector,W2,R> mapM(Vector<T> l, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l.map(fn), VectorInstances.definitions(), defs);
  }

  public static InstanceDefinitions<vector> definitions(){
    return new InstanceDefinitions<vector>() {
      @Override
      public <T, R> Functor<vector> functor() {
        return VectorInstances.functor();
      }

      @Override
      public <T> Pure<vector> unit() {
        return VectorInstances.unit();
      }

      @Override
      public <T, R> Applicative<vector> applicative() {
        return VectorInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<vector> monad() {
        return VectorInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<vector>> monadZero() {
        return Option.some(VectorInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<vector>> monadPlus() {
        return Option.some(VectorInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<vector> monadRec() {
        return VectorInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<vector>> monadPlus(MonoidK<vector> m) {
        return Option.some(VectorInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<vector> traverse() {
        return VectorInstances.traverse();
      }

      @Override
      public <T> Foldable<vector> foldable() {
        return VectorInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<vector>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<vector>> unfoldable() {
        return Option.some(VectorInstances.unfoldable());
      }
    };
  }
  public static Unfoldable<vector> unfoldable(){
    return new Unfoldable<vector>() {
      @Override
      public <R, T> Higher<vector, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return Vector.unfold(b,fn);
      }
    };
  }

  public static <T,R>Functor<vector> functor(){
    BiFunction<Vector<T>,Function<? super T, ? extends R>,Vector<R>> map = VectorInstances::map;
    return General.functor(map);
  }

  public static <T> Pure<vector> unit(){
    return General.<vector,T>unit(VectorInstances::of);
  }

  public static <T,R> Applicative<vector> zippingApplicative(){
    BiFunction<Vector< Function<T, R>>,Vector<T>,Vector<R>> ap = VectorInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }

  public static <T,R> Monad<vector> monad(){

    BiFunction<Higher<vector,T>,Function<? super T, ? extends Higher<vector,R>>,Higher<vector,R>> flatMap = VectorInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }

  public static <T,R> MonadZero<vector> monadZero(){

    return General.monadZero(monad(), Vector.empty());
  }

  public static <T> MonadPlus<vector> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.vectorConcat());
  }
  public static <T,R> MonadRec<vector> monadRec(){

    return new MonadRec<vector>(){
      @Override
      public <T, R> Higher<vector, R> tailRec(T initial, Function<? super T, ? extends Higher<vector,? extends Either<T, R>>> fn) {
        return Vector.tailRec(initial,fn.andThen(Vector::narrowK));
      }
    };
  }

  public static MonadPlus<vector> monadPlus(MonoidK<vector> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / vectoruence operations
   */
  public static <C2,T> Traverse<vector> traverse(){
    BiFunction<Applicative<C2>,Vector<Higher<C2, T>>,Higher<C2, Vector<T>>> vectoruenceFn = (ap, list) -> {

      Higher<C2,Vector<T>> identity = ap.unit(Vector.empty());

      BiFunction<Higher<C2,Vector<T>>,Higher<C2,T>,Higher<C2,Vector<T>>> combineToPStack =   (acc, next) -> ap.apBiFn(ap.unit((a, b) ->a.plus(b)),acc,next);

      BinaryOperator<Higher<C2,Vector<T>>> combinePStacks = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> l1.plusAll(l2)),a,b); ;


      return list.stream()
        .reverse()
        .reduce(identity,
          combineToPStack,
          combinePStacks);


    };
    BiFunction<Applicative<C2>,Higher<vector,Higher<C2, T>>,Higher<C2, Higher<vector,T>>> vectoruenceNarrow  =
      (a,b) -> Vector.widen2(vectoruenceFn.apply(a, narrowK(b)));
    return General.traverse(zippingApplicative(), vectoruenceNarrow);
  }

  public static <T,R> Foldable<vector> foldable(){
    BiFunction<Monoid<T>,Higher<vector,T>,T> foldRightFn =  (m, l)-> narrowK(l).foldRight(m);
    BiFunction<Monoid<T>,Higher<vector,T>,T> foldLeftFn = (m, l)-> narrowK(l).reduce(m);
    Function3<Monoid<R>, Function<T, R>, Higher<vector, T>, R> foldMapFn = (m, f, l)->narrowK(l).map(f).foldLeft(m);

    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }

  private static  <T> Vector<T> concat(PersistentList<T> l1, PersistentList<T> l2){

    return Vector.fromIterable(l1.plusAll(l2));
  }
  private <T> Vector<T> of(T value){
    return Vector.of(value);
  }
  private static <T,R> Vector<R> ap(Vector<Function< T, R>> lt, Vector<T> list){
    return Vector.fromIterable(lt).zip(list,(a, b)->a.apply(b));
  }
  private static <T,R> Higher<vector,R> flatMap(Higher<vector,T> lt, Function<? super T, ? extends  Higher<vector,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(Vector::narrowK));
  }
  private static <T,R> Vector<R> map(Vector<T> lt, Function<? super T, ? extends R> fn){
    return Vector.fromIterable(lt).map(fn);
  }
}
