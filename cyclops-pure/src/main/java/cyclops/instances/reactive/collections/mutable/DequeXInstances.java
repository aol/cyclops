package cyclops.instances.reactive.collections.mutable;


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
import cyclops.reactive.collections.mutable.DequeX;
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

import static com.oath.cyclops.data.ReactiveWitness.deque;
import static cyclops.reactive.collections.mutable.DequeX.narrowK;

@UtilityClass
public class DequeXInstances {
  public static  <T> Kleisli<deque,DequeX<T>,T> kindKleisli(){
    return Kleisli.of(DequeXInstances.monad(), DequeX::widen);
  }

  public static  <T> Cokleisli<deque,T,DequeX<T>> kindCokleisli(){
    return Cokleisli.of(DequeX::narrowK);
  }
  public static <W1,T> Nested<deque,W1,T> nested(DequeX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
    return Nested.of(nested, DequeXInstances.definitions(),def2);
  }
  public static  <W1,T> Product<deque,W1,T> product(DequeX<T> d, Active<W1,T> active){
    return Product.of(allTypeclasses(d),active);
  }
  public static  <W1,T> Coproduct<W1,deque,T> coproduct(DequeX<T> d, InstanceDefinitions<W1> def2){
    return Coproduct.right(d,def2, DequeXInstances.definitions());
  }
  public static  <T> Active<deque,T> allTypeclasses(DequeX<T> d){
    return Active.of(d, DequeXInstances.definitions());
  }

  public static  <W2,R,T> Nested<deque,W2,R> mapM(DequeX<T> d,Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
    return Nested.of(d.map(fn), DequeXInstances.definitions(), defs);
  }
  public static InstanceDefinitions<deque> definitions(){
    return new InstanceDefinitions<deque>() {
      @Override
      public <T, R> Functor<deque> functor() {
        return DequeXInstances.functor();
      }

      @Override
      public <T> Pure<deque> unit() {
        return DequeXInstances.unit();
      }

      @Override
      public <T, R> Applicative<deque> applicative() {
        return DequeXInstances.zippingApplicative();
      }

      @Override
      public <T, R> Monad<deque> monad() {
        return DequeXInstances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<deque>> monadZero() {
        return Option.some(DequeXInstances.monadZero());
      }

      @Override
      public <T> Option<MonadPlus<deque>> monadPlus() {
        return Option.some(DequeXInstances.monadPlus());
      }

      @Override
      public <T> MonadRec<deque> monadRec() {
        return DequeXInstances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<deque>> monadPlus(MonoidK<deque> m) {
        return Option.some(DequeXInstances.monadPlus(m));
      }

      @Override
      public <C2, T> Traverse<deque> traverse() {
        return DequeXInstances.traverse();
      }

      @Override
      public <T> Foldable<deque> foldable() {
        return DequeXInstances.foldable();
      }

      @Override
      public <T> Option<Comonad<deque>> comonad() {
        return Maybe.nothing();
      }
      @Override
      public <T> Option<Unfoldable<deque>> unfoldable() {
        return Option.some(DequeXInstances.unfoldable());
      }
    };
  }

    public static Pure<deque> unit() {
      return INSTANCE;
    }

    private final static DequeXTypeClasses INSTANCE = new DequeXTypeClasses();
    @AllArgsConstructor
    @With
    public static class DequeXTypeClasses implements MonadPlus<deque>,
        MonadRec<deque>,
        TraverseByTraverse<deque>,
        Foldable<deque>,
        Unfoldable<deque>{

        private final MonoidK<deque> monoidK;
        public DequeXTypeClasses(){
            monoidK = MonoidKs.dequeXConcat();
        }
        @Override
        public <T> Higher<deque, T> filter(Predicate<? super T> predicate, Higher<deque, T> ds) {
            return narrowK(ds).filter(predicate);
        }

        @Override
        public <T, R> Higher<deque, Tuple2<T, R>> zip(Higher<deque, T> fa, Higher<deque, R> fb) {
            return narrowK(fa).zip(narrowK(fb));
        }

        @Override
        public <T1, T2, R> Higher<deque, R> zip(Higher<deque, T1> fa, Higher<deque, T2> fb, BiFunction<? super T1, ? super T2, ? extends R> f) {
            return narrowK(fa).zip(narrowK(fb),f);
        }

        @Override
        public <T> MonoidK<deque> monoid() {
            return monoidK;
        }

        @Override
        public <T, R> Higher<deque, R> flatMap(Function<? super T, ? extends Higher<deque, R>> fn, Higher<deque, T> ds) {
            return narrowK(ds).concatMap(i->narrowK(fn.apply(i)));
        }

        @Override
        public <T, R> Higher<deque, R> ap(Higher<deque, ? extends Function<T, R>> fn, Higher<deque, T> apply) {
            return narrowK(apply)
                .zip(narrowK(fn),(a,b)->b.apply(a));
        }

        @Override
        public <T> Higher<deque, T> unit(T value) {
            return DequeX.of(value);
        }

        @Override
        public <T, R> Higher<deque, R> map(Function<? super T, ? extends R> fn, Higher<deque, T> ds) {
            return narrowK(ds).map(fn);
        }


        @Override
        public <T, R> Higher<deque, R> tailRec(T initial, Function<? super T, ? extends Higher<deque, ? extends Either<T, R>>> fn) {
            return DequeX.tailRec(initial,i->narrowK(fn.apply(i)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<deque, R>> traverseA(Applicative<C2> ap, Function<? super T, ? extends Higher<C2, R>> fn, Higher<deque, T> ds) {
            DequeX<T> v = narrowK(ds);
            return v.<Higher<C2, Higher<deque,R>>>foldLeft(ap.unit(DequeX.<R>empty()),
                (a, b) -> ap.zip(fn.apply(b), a, (sn, vec) -> narrowK(vec).plus(sn)));


        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<deque, T> ds) {
            DequeX<T> x = narrowK(ds);
            return x.foldLeft(mb.zero(),(a,b)->mb.apply(a,fn.apply(b)));
        }

        @Override
        public <T, R> Higher<deque, Tuple2<T, Long>> zipWithIndex(Higher<deque, T> ds) {
            return narrowK(ds).zipWithIndex();
        }

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<deque, T> ds) {
            return narrowK(ds).foldRight(monoid);
        }


        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<deque, T> ds) {
            return narrowK(ds).foldLeft(monoid);
        }


        @Override
        public <R, T> Higher<deque, R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn) {
            return DequeX.unfold(b,fn);
        }


    }

    public static Unfoldable<deque> unfoldable(){

        return INSTANCE;
    }

    public static MonadPlus<deque> monadPlus(MonoidK<deque> m){

        return INSTANCE.withMonoidK(m);
    }
    public static <T,R> Applicative<deque> zippingApplicative(){
        return INSTANCE;
    }
    public static <T,R>Functor<deque> functor(){
        return INSTANCE;
    }

    public static <T,R> Monad<deque> monad(){
        return INSTANCE;
    }

    public static <T,R> MonadZero<deque> monadZero(){

        return INSTANCE;
    }

    public static <T> MonadPlus<deque> monadPlus(){

        return INSTANCE;
    }
    public static <T,R> MonadRec<deque> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<deque> traverse(){
        return INSTANCE;
    }

    public static <T,R> Foldable<deque> foldable(){
        return INSTANCE;
    }


}
