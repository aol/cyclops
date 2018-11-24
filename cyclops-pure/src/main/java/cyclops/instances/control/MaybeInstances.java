package cyclops.instances.control;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.option;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function3;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.kinds.OptionalKind;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.Function;

import static cyclops.control.Maybe.narrowK;

/**
 * Companion class for creating Type Class instances for working with Maybes
 * @author johnmcclean
 *
 */
@UtilityClass
public class MaybeInstances {
  public static <T> Maybe<T> fromOptionalKind(final OptionalKind<T> opt){
    return fromOptional(OptionalKind.narrow(opt));
  }
  public static  <T> Kleisli<option,Maybe<T>,T> kindKleisli(){
    return Kleisli.of(MaybeInstances.monad(), Maybe::widen);
  }

  public static <T> Cokleisli<option,T,Maybe<T>> kindCokleisli(){
    return Cokleisli.of(Maybe::narrowK);
  }
  public static <W1,T> Nested<option,W1,T> nested(Maybe<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){

    return Nested.of(nested, MaybeInstances.definitions(),def2);
  }
  public static <W1,T> Product<option,W1,T> product(Maybe<T> m, Active<W1,T> active){
    return Product.of(allTypeclasses(m),active);
  }
  public static <W1,T> Coproduct<W1,option,T> coproduct(Maybe<T> m, InstanceDefinitions<W1> def2){
    return Coproduct.right(m,def2, MaybeInstances.definitions());
  }
  public static <T> Active<option,T> allTypeclasses(Maybe<T> m){
    return Active.of(m, MaybeInstances.definitions());
  }
  public static <W2,R,T> Nested<option,W2,R> mapM(Maybe<T> m,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(m.map(fn), MaybeInstances.definitions(), defs);
  }


  /**
   * Construct an equivalent Maybe from the Supplied Optional
   * <pre>
   * {@code
   *   MaybeType<Integer> some = MaybeType.fromOptional(Optional.of(10));
   *   //Maybe[10], Some[10]
   *
   *   MaybeType<Integer> none = MaybeType.fromOptional(Optional.zero());
   *   //Maybe.zero, None[]
   * }
   * </pre>
   *
   * @param optional Optional to construct Maybe from
   * @return Maybe created from Optional
   */
  public static <T> Maybe<T> fromOptional(Higher<DataWitness.optional,T> optional){
    return   Maybe.fromOptional(OptionalKind.narrowK(optional));

  }

  public static InstanceDefinitions<option> definitions(){
    return new InstanceDefinitions<option>() {
      @Override
      public <T, R> Functor<option> functor() {
        return MaybeInstances.functor();
      }

      @Override
      public <T> Pure<option> unit() {
        return MaybeInstances.unit();
      }

      @Override
      public <T, R> Applicative<option> applicative() {
        return MaybeInstances.applicative();
      }

      @Override
      public <T, R> Monad<option> monad() {
        return MaybeInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<option>> monadZero() {
        return Option.some(MaybeInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<option>> monadPlus() {
        return Option.some(MaybeInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<option> monadRec() {
        return MaybeInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<option>> monadPlus(MonoidK<option> m) {
        return Option.some(MaybeInstances.monadPlus(m));
      }

      @Override
      public <C2,T> Traverse<option> traverse() {
        return MaybeInstances.traverse();
      }


      @Override
      public <T> Foldable<option> foldable() {
        return MaybeInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<option>> comonad() {
        return Maybe.nothing();
      }

      @Override
      public <T> Option<Unfoldable<option>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }

    private final OptionInstances.OptionTypeclasses INSTANCE = new OptionInstances.OptionTypeclasses();

    @AllArgsConstructor
    public static class OptionTypeclasses  implements MonadPlus<option>,
                                                        MonadRec<option>,
                                                        TraverseByTraverse<option>,
                                                        Foldable<option>,
                                                        Unfoldable<option>{

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<option, T> ds) {
            return Maybe.narrowK(ds).fold(monoid);
        }



        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<option, T> ds) {
            return Maybe.narrowK(ds).fold(monoid);
        }

        @Override
        public <R, T> Higher<option, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return Maybe.fromPublisher(fn.apply(b).map(t->t._1()));
        }

        @Override
        public <T> MonoidK<option> monoid() {
            return MonoidKs.firstPresentMaybe();
        }

        @Override
        public <T, R> Higher<option, R> flatMap(Function<? super T, ? extends Higher<option, R>> fn, Higher<option, T> ds) {
            return Maybe.narrowK(ds).flatMap(t-> Option.narrowK(fn.apply(t)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<option, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<option, T> ds) {
            Maybe<T> maybe = Maybe.narrowK(ds);
            return maybe.fold(some-> applicative.map(m->Option.of(m), fn.apply(some)),
                ()->applicative.unit(Option.<R>none()));
        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<option, T> ds) {
            Maybe<R>  opt  = Maybe.narrowK(ds).map(fn);
            return opt.fold(mb);
        }

        @Override
        public <T, R> Higher<option, R> ap(Higher<option, ? extends Function<T, R>> fn, Higher<option, T> apply) {
            return Maybe.narrowK(apply).zip(Option.narrowK(fn), (a, b)->b.apply(a));
        }

        @Override
        public <T> Higher<option, T> unit(T value) {
            return Maybe.just(value);
        }

        @Override
        public <T, R> Higher<option, R> map(Function<? super T, ? extends R> fn, Higher<option, T> ds) {
            return Maybe.narrowK(ds).map(fn);
        }

        @Override
        public <T, R> Higher<option, R> tailRec(T initial, Function<? super T, ? extends Higher<option, ? extends Either<T, R>>> fn) {
            return Maybe.tailRec(initial,t-> Maybe.narrowK(fn.apply(t)));
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

        return INSTANCE;
    }


    public static <C2,T> Traverse<option> traverse(){
        return INSTANCE;
    }


    public static <T,R> Foldable<option> foldable(){
        return INSTANCE;
    }




}
