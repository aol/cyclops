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
import cyclops.data.tuple.Tuple2;
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
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.monad.MonadPlus;
import cyclops.typeclasses.monad.MonadRec;
import cyclops.typeclasses.monad.MonadZero;
import cyclops.typeclasses.monad.Traverse;
import cyclops.typeclasses.monad.TraverseByTraverse;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.experimental.Wither;

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
  private final OptionTypeclasses INSTANCE = new OptionTypeclasses();

  @AllArgsConstructor
  @Wither
  public static class OptionTypeclasses  implements MonadPlus<option>,
                                                    MonadRec<option>,
                                                    TraverseByTraverse<option>,
                                                    Foldable<option>,
                                                    Unfoldable<option>{

      private final MonoidK<option> monoidK;
      public OptionTypeclasses(){
          monoidK= MonoidKs.firstPresentOption();
      }

      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<option, T> ds) {
          return narrowK(ds).fold(monoid);
      }



      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<option, T> ds) {
          return narrowK(ds).fold(monoid);
      }

      @Override
      public <R, T> Higher<option, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
          return fn.apply(b).map(t->t._1());
      }

      @Override
      public <T> MonoidK<option> monoid() {
          return monoidK;
      }

      @Override
      public <T, R> Higher<option, R> flatMap(Function<? super T, ? extends Higher<option, R>> fn, Higher<option, T> ds) {
          return narrowK(ds).flatMap(t->narrowK(fn.apply(t)));
      }

      @Override
      public <C2, T, R> Higher<C2, Higher<option, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<option, T> ds) {
          Option<T> maybe = narrowK(ds);
          return maybe.fold(some-> applicative.map(m->Option.of(m), fn.apply(some)),
                                 ()->applicative.unit(Option.<R>none()));
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<option, T> ds) {
          Option<R>  opt  = narrowK(ds).map(fn);
          return opt.fold(mb);
      }

      @Override
      public <T, R> Higher<option, R> ap(Higher<option, ? extends Function<T, R>> fn, Higher<option, T> apply) {
          return narrowK(apply).zip(narrowK(fn), (a,b)->b.apply(a));
      }

      @Override
      public <T> Higher<option, T> unit(T value) {
          return Option.some(value);
      }

      @Override
      public <T, R> Higher<option, R> map(Function<? super T, ? extends R> fn, Higher<option, T> ds) {
          return narrowK(ds).map(fn);
      }

      @Override
      public <T, R> Higher<option, R> tailRec(T initial, Function<? super T, ? extends Higher<option, ? extends Either<T, R>>> fn) {
          return Option.tailRec(initial,t->narrowK(fn.apply(t)));
      }
  }
  public static <T,R>Functor<option> functor(){
    return INSTANCE;
  }

  public static <T> Pure<option> unit(){
      return INSTANCE;
  }

  public static <T,R> Applicative<option> applicative(){
      return INSTANCE;
  }

  public static <T,R> Monad<option> monad(){
      return INSTANCE;
  }
  public static <T,R> MonadRec<option> monadRec(){

      return INSTANCE;
  }

  public static <T,R> MonadZero<option> monadZero(){

      return INSTANCE;
  }

  public static <T> MonadPlus<option> monadPlus(){
      return INSTANCE;
  }

  public static <T> MonadPlus<option> monadPlus(MonoidK<option> m){

      return INSTANCE.withMonoidK(m);
  }


  public static <C2,T> Traverse<option> traverse(){
      return INSTANCE;
  }


  public static <T,R> Foldable<option> foldable(){
      return INSTANCE;
  }





}
