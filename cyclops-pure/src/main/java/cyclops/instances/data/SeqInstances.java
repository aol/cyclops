package cyclops.instances.data;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.seq;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.data.Seq;
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

import static cyclops.data.Seq.narrowK;


/**
 * Companion class for creating Type Class instances for working with Seq's
 * @author johnmcclean
 *
 */
@UtilityClass
public class SeqInstances {
  public static  <T> Kleisli<DataWitness.seq,Seq<T>,T> kindKleisli(){
    return Kleisli.of(SeqInstances.monad(), Seq::widen);
  }

  public static  <T> Cokleisli<DataWitness.seq,T,Seq<T>> kindCokleisli(){
    return Cokleisli.of(Seq::narrowK);
  }
  public static <W1,T> Nested<DataWitness.seq,W1,T> nested(Seq<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, SeqInstances.definitions(),def2);
  }
  public static <W1,T> Product<DataWitness.seq,W1,T> product(Seq<T> l, Active<W1,T> active){
    return Product.of(allTypeclasses(l),active);
  }
  public static <W1,T> Coproduct<W1,DataWitness.seq,T> coproduct(Seq<T> l, InstanceDefinitions<W1> def2){
    return Coproduct.right(l,def2, SeqInstances.definitions());
  }
  public static <T> Active<DataWitness.seq,T> allTypeclasses(Seq<T> l){
    return Active.of(l, SeqInstances.definitions());
  }
  public static <W2,R,T> Nested<DataWitness.seq,W2,R> mapM(Seq<T> l, Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(l.map(fn), SeqInstances.definitions(), defs);
  }

  public static InstanceDefinitions<seq> definitions(){
    return new InstanceDefinitions<seq>() {
      @Override
      public <T, R> Functor<seq> functor() {
        return SeqInstances.functor();
      }

      @Override
      public <T> Pure<seq> unit() {
        return SeqInstances.unit();
      }

      @Override
      public <T, R> Applicative<seq> applicative() {
        return SeqInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<seq> monad() {
        return SeqInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<seq>> monadZero() {
        return Option.some(SeqInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<seq>> monadPlus() {
        return Option.some(SeqInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<seq> monadRec() {
        return SeqInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<seq>> monadPlus(MonoidK<seq> m) {
        return Option.some(SeqInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<seq> traverse() {
        return SeqInstances.traverse();
      }

      @Override
      public <T> Foldable<seq> foldable() {
        return SeqInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<seq>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<seq>> unfoldable() {
        return Option.some(SeqInstances.unfoldable());
      }
    };
  }
  public static Unfoldable<seq> unfoldable(){
    return new Unfoldable<seq>() {
      @Override
      public <R, T> Higher<seq, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
        return Seq.unfold(b,fn);
      }
    };
  }

  public static <T,R>Functor<seq> functor(){
    BiFunction<Seq<T>,Function<? super T, ? extends R>,Seq<R>> map = SeqInstances::map;
    return General.functor(map);
  }

  public static <T> Pure<seq> unit(){
    return General.<seq,T>unit(SeqInstances::of);
  }

  public static <T,R> Applicative<seq> zippingApplicative(){
    BiFunction<Seq< Function<T, R>>,Seq<T>,Seq<R>> ap = SeqInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }

  public static <T,R> Monad<seq> monad(){

    BiFunction<Higher<seq,T>,Function<? super T, ? extends Higher<seq,R>>,Higher<seq,R>> flatMap = SeqInstances::flatMap;
    return General.monad(zippingApplicative(), flatMap);
  }

  public static <T,R> MonadZero<seq> monadZero(){

    return General.monadZero(monad(), Seq.empty());
  }

  public static <T> MonadPlus<seq> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.seqConcat());
  }
  public static <T,R> MonadRec<seq> monadRec(){

    return new MonadRec<seq>(){
      @Override
      public <T, R> Higher<seq, R> tailRec(T initial, Function<? super T, ? extends Higher<seq,? extends Either<T, R>>> fn) {
        return Seq.tailRec(initial,fn.andThen(Seq::narrowK));
      }
    };
  }

  public static MonadPlus<seq> monadPlus(MonoidK<seq> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<seq> traverse(){
    BiFunction<Applicative<C2>,Seq<Higher<C2, T>>,Higher<C2, Seq<T>>> sequenceFn = (ap, list) -> {

      Higher<C2,Seq<T>> identity = ap.unit(Seq.empty());

      BiFunction<Higher<C2,Seq<T>>,Higher<C2,T>,Higher<C2,Seq<T>>> combineToPStack =   (acc, next) -> ap.apBiFn(ap.unit((a, b) ->a.plus(b)),acc,next);

      BinaryOperator<Higher<C2,Seq<T>>> combinePStacks = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> l1.plusAll(l2)),a,b); ;


      return list.stream()
        .reverse()
        .reduce(identity,
          combineToPStack,
          combinePStacks);


    };
    BiFunction<Applicative<C2>,Higher<seq,Higher<C2, T>>,Higher<C2, Higher<seq,T>>> sequenceNarrow  =
      (a,b) -> Seq.widen2(sequenceFn.apply(a, narrowK(b)));
    return General.traverse(zippingApplicative(), sequenceNarrow);
  }

  public static <T,R> Foldable<seq> foldable(){
    BiFunction<Monoid<T>,Higher<seq,T>,T> foldRightFn =  (m, l)-> narrowK(l).foldRight(m);
    BiFunction<Monoid<T>,Higher<seq,T>,T> foldLeftFn = (m, l)-> narrowK(l).reduce(m);
    Function3<Monoid<R>, Function<T, R>, Higher<seq, T>, R> foldMapFn = (m, f, l)->narrowK(l).map(f).foldLeft(m);

    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }

  private static  <T> Seq<T> concat(PersistentList<T> l1, PersistentList<T> l2){

    return Seq.fromIterable(l1.plusAll(l2));
  }
  private <T> Seq<T> of(T value){
    return Seq.of(value);
  }
  private static <T,R> Seq<R> ap(Seq<Function< T, R>> lt, Seq<T> list){
    return Seq.fromIterable(lt).zip(list,(a, b)->a.apply(b));
  }
  private static <T,R> Higher<seq,R> flatMap(Higher<seq,T> lt, Function<? super T, ? extends  Higher<seq,R>> fn){
    return narrowK(lt).concatMap(fn.andThen(Seq::narrowK));
  }
  private static <T,R> Seq<R> map(Seq<T> lt, Function<? super T, ? extends R> fn){
    return Seq.fromIterable(lt).map(fn);
  }
}
