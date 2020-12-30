package cyclops.instances.reactive.collections.immutable;


import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Cokleisli;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.arrow.MonoidKs;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.hkt.Active;
import cyclops.hkt.Coproduct;
import cyclops.hkt.Nested;
import cyclops.hkt.Product;
import cyclops.reactive.collections.immutable.VectorX;
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
import lombok.With;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.oath.cyclops.data.ReactiveWitness.vectorX;
import static cyclops.reactive.collections.immutable.VectorX.narrowK;

/**
 * Companion class for creating Type Class instances for working with PVectors
 * @author johnmcclean
 *
 */
@UtilityClass
public class VectorXInstances {

  public static  <T> Kleisli<vectorX,VectorX<T>,T> kindKleisli(){
    return Kleisli.of(VectorXInstances.monad(), VectorX::widen);
  }

  public static  <T> Cokleisli<vectorX,T,VectorX<T>> kindCokleisli(){
    return Cokleisli.of(VectorX::narrowK);
  }
  public static <W1,T> Nested<vectorX,W1,T> nested(VectorX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, VectorXInstances.definitions(),def2);
  }
  public static <W1,T> Product<vectorX,W1,T> product(VectorX<T> vec, Active<W1,T> active){
    return Product.of(allTypeclasses(vec),active);
  }
  public static <W1,T> Coproduct<W1,vectorX,T> coproduct(VectorX<T> vec, InstanceDefinitions<W1> def2){
    return Coproduct.right(vec,def2, VectorXInstances.definitions());
  }
  public static <T> Active<vectorX,T> allTypeclasses(VectorX<T> vec){
    return Active.of(vec, VectorXInstances.definitions());
  }
  public static <W2,R,T> Nested<vectorX,W2,R> mapM(VectorX<T> vec,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(vec.map(fn), VectorXInstances.definitions(), defs);
  }
  public static InstanceDefinitions<vectorX> definitions(){
    return new InstanceDefinitions<vectorX>() {
      @Override
      public <T, R> Functor<vectorX> functor() {
        return VectorXInstances.functor();
      }

      @Override
      public <T> Pure<vectorX> unit() {
        return VectorXInstances.unit();
      }

      @Override
      public <T, R> Applicative<vectorX> applicative() {
        return VectorXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<vectorX> monad() {
        return VectorXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<vectorX>> monadZero() {
        return Option.some(VectorXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<vectorX>> monadPlus() {
        return Option.some(VectorXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<vectorX> monadRec() {
        return VectorXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<vectorX>> monadPlus(MonoidK<vectorX> m) {
        return Option.some(VectorXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<vectorX> traverse() {
        return VectorXInstances.traverse();
      }

      @Override
      public <T> Foldable<vectorX> foldable() {
        return VectorXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<vectorX>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<vectorX>> unfoldable() {
        return Option.some(VectorXInstances.unfoldable());
      }
    };
  }

    public static Pure<vectorX> unit() {
      return INSTANCE;
    }

    private final static VectorXTypeClasses INSTANCE = new VectorXTypeClasses();
    @AllArgsConstructor
    @With
    public static class VectorXTypeClasses implements MonadPlus<vectorX>,
        MonadRec<vectorX>,
        TraverseByTraverse<vectorX>,
        Foldable<vectorX>,
        Unfoldable<vectorX>{

        private final MonoidK<vectorX> monoidK;
        public VectorXTypeClasses(){
            monoidK = MonoidKs.vectorXConcat();
        }
        @Override
        public <T> Higher<vectorX, T> filter(Predicate<? super T> predicate, Higher<vectorX, T> ds) {
            return narrowK(ds).filter(predicate);
        }

        @Override
        public <T, R> Higher<vectorX, Tuple2<T, R>> zip(Higher<vectorX, T> fa, Higher<vectorX, R> fb) {
            return narrowK(fa).zip(narrowK(fb));
        }

        @Override
        public <T1, T2, R> Higher<vectorX, R> zip(Higher<vectorX, T1> fa, Higher<vectorX, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
            return narrowK(fa).zip(narrowK(fb),f);
        }

        @Override
        public <T> MonoidK<vectorX> monoid() {
            return monoidK;
        }

        @Override
        public <T, R> Higher<vectorX, R> flatMap(Function<? super T, ? extends Higher<vectorX, R>> fn, Higher<vectorX, T> ds) {
            return narrowK(ds).concatMap(i->narrowK(fn.apply(i)));
        }

        @Override
        public <T, R> Higher<vectorX, R> ap(Higher<vectorX, ? extends Function<T, R>> fn, Higher<vectorX, T> apply) {
            return narrowK(apply)
                .zip(narrowK(fn),(a,b)->b.apply(a));
        }

        @Override
        public <T> Higher<vectorX, T> unit(T value) {
            return VectorX.of(value);
        }

        @Override
        public <T, R> Higher<vectorX, R> map(Function<? super T, ? extends R> fn, Higher<vectorX, T> ds) {
            return narrowK(ds).map(fn);
        }


        @Override
        public <T, R> Higher<vectorX, R> tailRec(T initial, Function<? super T, ? extends Higher<vectorX, ? extends Either<T, R>>> fn) {
            return VectorX.tailRec(initial,i->narrowK(fn.apply(i)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<vectorX, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<vectorX, T> ds) {
            VectorX<T> v = narrowK(ds);
            return v.<Higher<C2, Higher<vectorX,R>>>foldLeft(ap.unit(VectorX.<R>empty()),
                (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> narrowK(vec).plus(sn)));


        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<vectorX, T> ds) {
            VectorX<T> x = narrowK(ds);
            return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));
        }

        @Override
        public <T, R> Higher<vectorX, Tuple2<T, Long>> zipWithIndex(Higher<vectorX, T> ds) {
            return narrowK(ds).zipWithIndex();
        }

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<vectorX, T> ds) {
            return narrowK(ds).foldRight(monoid);
        }


        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<vectorX, T> ds) {
            return narrowK(ds).foldLeft(monoid);
        }


        @Override
        public <R, T> Higher<vectorX, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return VectorX.unfold(b,fn);
        }


    }

    public static Unfoldable<vectorX> unfoldable(){

        return INSTANCE;
    }

    public static MonadPlus<vectorX> monadPlus(MonoidK<vectorX> m){

        return INSTANCE.withMonoidK(m);
    }
    public static <T,R> Applicative<vectorX> zippingApplicative(){
        return INSTANCE;
    }
    public static <T,R>Functor<vectorX> functor(){
        return INSTANCE;
    }

    public static <T,R> Monad<vectorX> monad(){
        return INSTANCE;
    }

    public static <T,R> MonadZero<vectorX> monadZero(){

        return INSTANCE;
    }

    public static <T> MonadPlus<vectorX> monadPlus(){

        return INSTANCE;
    }
    public static <T,R> MonadRec<vectorX> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<vectorX> traverse(){
        return INSTANCE;
    }

    public static <T,R> Foldable<vectorX> foldable(){
        return INSTANCE;
    }


}
