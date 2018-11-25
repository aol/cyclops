package cyclops.instances.data.tuple;

import com.oath.cyclops.hkt.DataWitness.tuple1;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Either;
import cyclops.control.Identity;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple1;
import cyclops.function.Monoid;
import cyclops.arrow.Cokleisli;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.arrow.Kleisli;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.arrow.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;

import java.util.function.Function;

import static cyclops.data.tuple.Tuple1.narrowK;

public class Tuple1Instances {
  public static <T> Identity<T> toIdentity(Tuple1<T> t1){
    return Identity.of(t1._1());
  }
  public static InstanceDefinitions<tuple1> definitions(){
    return new InstanceDefinitions<tuple1>() {
      @Override
      public <T, R> Functor<tuple1> functor() {
        return Tuple1Instances.functor();
      }

      @Override
      public <T> Pure<tuple1> unit() {
        return Tuple1Instances.unit();
      }

      @Override
      public <T, R> Applicative<tuple1> applicative() {
        return Tuple1Instances.applicative();
      }

      @Override
      public <T, R> Monad<tuple1> monad() {
        return Tuple1Instances.monad();
      }

      @Override
      public <T, R> Option<MonadZero<tuple1>> monadZero() {
        return Option.none();
      }

      @Override
      public <T> Option<MonadPlus<tuple1>> monadPlus() {
        return Option.none();
      }

      @Override
      public <T> MonadRec<tuple1> monadRec() {
        return Tuple1Instances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<tuple1>> monadPlus(MonoidK<tuple1> m) {
        return Option.none();
      }

      @Override
      public <C2, T> Traverse<tuple1> traverse() {
        return Tuple1Instances.traverse();
      }

      @Override
      public <T> Foldable<tuple1> foldable() {
        return Tuple1Instances.foldable();
      }

      @Override
      public <T> Option<Comonad<tuple1>> comonad() {
        return Option.some(Tuple1Instances.comonad());
      }

      @Override
      public <T> Option<Unfoldable<tuple1>> unfoldable() {
        return Option.none();
      }
    };
  }
    private final static Tuple1Typeclasses INSTANCE = new Tuple1Typeclasses();

    @AllArgsConstructor
    public static class Tuple1Typeclasses  implements Monad<tuple1>,
                                                        MonadRec<tuple1>,
                                                        TraverseByTraverse<tuple1>,
                                                        Foldable<tuple1>,
                                                        Comonad<tuple1>{

        @Override
        public <T> T foldRight(Monoid<T> monoid, Higher<tuple1, T> ds) {
            return monoid.apply(narrowK(ds)._1(),monoid.zero());
        }



        @Override
        public <T> T foldLeft(Monoid<T> monoid, Higher<tuple1, T> ds) {
            return monoid.apply(monoid.zero(),narrowK(ds)._1());
        }



        @Override
        public <T, R> Higher<tuple1, R> flatMap(Function<? super T, ? extends Higher<tuple1, R>> fn, Higher<tuple1, T> ds) {
            return Tuple1.narrowK(ds).flatMap(t-> Tuple1.narrowK(fn.apply(t)));
        }

        @Override
        public <C2, T, R> Higher<C2, Higher<tuple1, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<tuple1, T> ds) {
            Tuple1<T> tuple1 = Tuple1.narrowK(ds);
            return applicative.map(Tuple1::of, fn.apply(tuple1._1()));
        }

        @Override
        public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
            Tuple1<R>  opt  = Tuple1.narrowK(ds).map(fn);
            return foldLeft(mb,opt);
        }

        @Override
        public <T, R> Higher<tuple1, R> ap(Higher<tuple1, ? extends Function<T, R>> fn, Higher<tuple1, T> apply) {
            return Tuple1.narrowK(apply).zip(Tuple1.narrowK(fn), (a, b)->b.apply(a));
        }

        @Override
        public <T> Higher<tuple1, T> unit(T value) {
            return Tuple1.of(value);
        }

        @Override
        public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
            return Tuple1.narrowK(ds).map(fn);
        }

        @Override
        public <T, R> Higher<tuple1, R> tailRec(T initial, Function<? super T, ? extends Higher<tuple1, ? extends Either<T, R>>> fn) {
            return Tuple1.tailRec(initial,t-> Tuple1.narrowK(fn.apply(t)));
        }

        @Override
        public <T> Higher<tuple1, Higher<tuple1, T>> nest(Higher<tuple1, T> ds) {
            return Tuple1.of(ds);
        }

        @Override
        public <T, R> Higher<tuple1, R> coflatMap(Function<? super Higher<tuple1, T>, R> mapper, Higher<tuple1, T> ds) {
            return Tuple1.of(mapper.apply(ds));
        }

        @Override
        public <T> T extract(Higher<tuple1, T> ds) {
            return Tuple1.narrowK(ds)._1();
        }
    }
    public static <T,R>Functor<tuple1> functor(){
        return INSTANCE;
    }

    public static <T> Pure<tuple1> unit(){
        return INSTANCE;
    }

    public static <T,R> Applicative<tuple1> applicative(){
        return INSTANCE;
    }

    public static <T,R> Monad<tuple1> monad(){
        return INSTANCE;
    }
    public static <T,R> Comonad<tuple1> comonad(){
        return INSTANCE;
    }
    public static <T,R> MonadRec<tuple1> monadRec(){

        return INSTANCE;
    }


    public static <C2,T> Traverse<tuple1> traverse(){
        return INSTANCE;
    }


    public static <T,R> Foldable<tuple1> foldable(){
        return INSTANCE;
    }


  public static  <T> Kleisli<tuple1,Tuple1<T>,T> kindKleisli(){
    return Kleisli.of(Tuple1Instances.monad(), Tuple1Instances::widen);
  }
  public static <T> Higher<tuple1, T> widen(Tuple1<T> narrow) {
    return narrow;
  }
  public static  <T> Cokleisli<tuple1,T,Tuple1<T>> kindCokleisli(){
    return Cokleisli.of(Tuple1::narrowK);
  }
}
