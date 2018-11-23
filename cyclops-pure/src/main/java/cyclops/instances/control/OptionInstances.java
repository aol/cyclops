package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.option;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.kinds.OptionalKind;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.Function;

import static cyclops.control.Option.narrowK;


/**
 * Companion class for creating Type Class instances for working with Options
 * @author johnmcclean
 *
 */
@UtilityClass
public class OptionInstances {
  public static <T> Option<T> fromOptionalKind(final OptionalKind<T> opt){
    return fromOptional(OptionalKind.narrow(opt));
  }
  public static  <T> Kleisli<option,Option<T>,T> kindKleisli(){
    return Kleisli.of(OptionInstances.monad(), Option::widen);
  }

  public static <T> Cokleisli<option,T,Option<T>> kindCokleisli(){
    return Cokleisli.of(Option::narrowK);
  }
  public static <W1,T> Nested<option,W1,T> nested(Option<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){

    return Nested.of(nested, OptionInstances.definitions(),def2);
  }
  public static <W1,T> Product<option,W1,T> product(Option<T> m, Active<W1,T> active){
    return Product.of(allTypeclasses(m),active);
  }
  public static <W1,T> Coproduct<W1,option,T> coproduct(Option<T> m, InstanceDefinitions<W1> def2){
    return Coproduct.right(m,def2, OptionInstances.definitions());
  }
  public static <T> Active<option,T> allTypeclasses(Option<T> m){
    return Active.of(m, OptionInstances.definitions());
  }
  public static <W2,R,T> Nested<option,W2,R> mapM(Option<T> m,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(m.map(fn), OptionInstances.definitions(), defs);
  }


  public static <T> Option<T> fromOptional(Higher<DataWitness.optional,T> optional){
    return   Option.fromOptional(OptionalKind.narrowK(optional));

  }

  public static InstanceDefinitions<option> definitions(){
    return new InstanceDefinitions<option>() {
      @Override
      public <T, R> Functor<option> functor() {
        return OptionInstances.functor();
      }

      @Override
      public <T> Pure<option> unit() {
        return OptionInstances.unit();
      }

      @Override
      public <T, R> Applicative<option> applicative() {
        return OptionInstances.applicative();
      }

      @Override
      public <T, R> Monad<option> monad() {
        return OptionInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<option>> monadZero() {
        return Option.some(OptionInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<option>> monadPlus() {
        return Option.some(OptionInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<option> monadRec() {
        return OptionInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<option>> monadPlus(MonoidK<option> m) {
        return Option.some(OptionInstances.monadPlus(m));
      }

      @Override
      public <C2,T> Traverse<option> traverse() {
        return OptionInstances.traverse();
      }


      @Override
      public <T> Foldable<option> foldable() {
        return OptionInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<option>> comonad() {
        return Option.none();
      }

      @Override
      public <T> Option<Unfoldable<option>> unfoldable() {
        return Option.none();
      }
    };
  }


  public static <T,R>Functor<option> functor(){
    BiFunction<Option<T>,Function<? super T, ? extends R>,Option<R>> map = OptionInstances::map;
    return General.functor(map);
  }

  public static <T> Pure<option> unit(){
    return General.<option,T>unit(OptionInstances::of);
  }

  public static <T,R> Applicative<option> applicative(){
    BiFunction<Option< Function<T, R>>,Option<T>,Option<R>> ap = OptionInstances::ap;
    return General.applicative(functor(), unit(), ap);
  }

  public static <T,R> Monad<option> monad(){

    BiFunction<Higher<option,T>,Function<? super T, ? extends Higher<option,R>>,Higher<option,R>> flatMap = OptionInstances::flatMap;
    return General.monad(applicative(), flatMap);
  }
  public static <T,R> MonadRec<option> monadRec(){

    return new MonadRec<option>(){

      @Override
      public <T, R> Higher<option, R> tailRec(T initial, Function<? super T, ? extends Higher<option, ? extends Either<T, R>>> fn) {
        return Option.tailRec(initial,fn.andThen(Option::narrowK));
      }
    };
  }

  public static <T,R> MonadZero<option> monadZero(){

    return General.monadZero(monad(), Option.none());
  }

  public static <T> MonadPlus<option> monadPlus(){

    return General.monadPlus(monadZero(), MonoidKs.firstPresentOption());
  }

  public static <T> MonadPlus<option> monadPlus(MonoidK<option> m){

    return General.monadPlus(monadZero(),m);
  }

  /**
   * @return Type class for traversables with traverse / sequence operations
   */
  public static <C2,T> Traverse<option> traverse(){

    return General.traverseByTraverse(applicative(), OptionInstances::traverseA);
  }


  public static <T,R> Foldable<option> foldable(){
    BiFunction<Monoid<T>,Higher<option,T>,T> foldRightFn =  (m, l)-> narrowK(l).orElse(m.zero());
    BiFunction<Monoid<T>,Higher<option,T>,T> foldLeftFn = (m, l)-> narrowK(l).orElse(m.zero());
    Function3<Monoid<R>, Function<T, R>, Higher<option, T>, R> foldMapFn = (m, f, l)-> narrowK(l).map(f).fold(m);
    return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
  }



  private <T> Option<T> of(T value){
    return Option.of(value);
  }
  private static <T,R> Option<R> ap(Option<Function< T, R>> lt,  Option<T> maybe){
    return lt.zip(maybe, (a,b)->a.apply(b)).toOption();

  }
  private static <T,R> Higher<option,R> flatMap(Higher<option,T> lt, Function<? super T, ? extends  Higher<option,R>> fn){
    return narrowK(lt).flatMap(fn.andThen(Option::narrowK));
  }
  private static <T,R> Option<R> map(Option<T> lt, Function<? super T, ? extends R> fn){
    return lt.map(fn);

  }


  private static <C2,T,R> Higher<C2, Higher<option, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                                              Higher<option, T> ds){

    Option<T> maybe = narrowK(ds);
    Higher<C2, Option<R>> res = maybe.fold(some-> applicative.map(m->Option.of(m), fn.apply(some)),
      ()->applicative.unit(Option.<R>none()));

    return Option.widen2(res);
  }

}
