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
        return Maybe.nothing();
      }

      @Override
      public <T> Option<MonadPlus<tuple1>> monadPlus() {
        return Maybe.nothing();
      }

      @Override
      public <T> MonadRec<tuple1> monadRec() {
        return Tuple1Instances.monadRec();
      }

      @Override
      public <T> Option<MonadPlus<tuple1>> monadPlus(MonoidK<tuple1> m) {
        return Maybe.nothing();
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
        return Maybe.just(Tuple1Instances.comonad());
      }

      @Override
      public <T> Option<Unfoldable<tuple1>> unfoldable() {
        return Maybe.nothing();
      }
    };
  }

  public static Functor<tuple1> functor(){
    return new Functor<tuple1>(){
      @Override
      public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
        return narrowK(ds).map(fn);
      }
    };
  }

  public static Pure<tuple1> unit(){
    return new Pure<tuple1>(){
      @Override
      public <T> Higher<tuple1, T> unit(T value) {
        return Tuple1.of(value);
      }
    };
  }
  public static Applicative<tuple1> applicative(){
    return new Applicative<tuple1>(){


      @Override
      public <T, R> Higher<tuple1, R> ap(Higher<tuple1, ? extends Function<T, R>> fn, Higher<tuple1, T> apply) {
        Tuple1<? extends Function<T, R>> f = narrowK(fn);
        Tuple1<T> ap = narrowK(apply);
        return f.flatMap(x -> ap.map(x));
      }

      @Override
      public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
        return functor().map(fn,ds);
      }

      @Override
      public <T> Higher<tuple1, T> unit(T value) {
        return Tuple1Instances.unit().unit(value);
      }
    };
  }
  public static Monad<tuple1> monad(){
    return new Monad<tuple1>(){


      @Override
      public <T, R> Higher<tuple1, R> ap(Higher<tuple1, ? extends Function<T, R>> fn, Higher<tuple1, T> apply) {
        return applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
        return functor().map(fn,ds);
      }

      @Override
      public <T> Higher<tuple1, T> unit(T value) {
        return Tuple1Instances.unit().unit(value);
      }

      @Override
      public <T, R> Higher<tuple1, R> flatMap(Function<? super T, ? extends Higher<tuple1, R>> fn, Higher<tuple1, T> ds) {
        return narrowK(ds).flatMap(fn.andThen(i-> narrowK(i)));
      }
    };
  }
  public static MonadRec<tuple1> monadRec() {

    return new MonadRec<tuple1>(){
      @Override
      public <T, R> Higher<tuple1, R> tailRec(T initial, Function<? super T, ? extends Higher<tuple1, ? extends Either<T, R>>> fn) {
        return Tuple1.tailRec(initial,fn.andThen(Tuple1::narrowK));
      }

    };


  }
  public static Traverse<tuple1> traverse(){
    return new Traverse<tuple1>(){

      @Override
      public <T, R> Higher<tuple1, R> ap(Higher<tuple1, ? extends Function<T, R>> fn, Higher<tuple1, T> apply) {
        return applicative().ap(fn,apply);
      }

      @Override
      public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
        return functor().map(fn,ds);
      }

      @Override
      public <T> Higher<tuple1, T> unit(T value) {
        return Tuple1Instances.unit().unit(value);
      }

      @Override
      public <C2, T, R> Higher<C2, Higher<tuple1, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<tuple1, T> ds) {
        Tuple1<T> id = narrowK(ds);
        Function<R, Tuple1<R>> rightFn = r -> Tuple1.of(r);
        return applicative.map(rightFn, fn.apply(id._1()));
      }

      @Override
      public <C2, T> Higher<C2, Higher<tuple1, T>> sequenceA(Applicative<C2> applicative, Higher<tuple1, Higher<C2, T>> ds) {
        return traverseA(applicative,Function.identity(),ds);
      }
    };
  }
  public static Foldable<tuple1> foldable(){
    return new Foldable<tuple1>(){


      @Override
      public <T> T foldRight(Monoid<T> monoid, Higher<tuple1, T> ds) {
        return monoid.apply(narrowK(ds)._1(),monoid.zero());
      }

      @Override
      public <T> T foldLeft(Monoid<T> monoid, Higher<tuple1, T> ds) {
        return monoid.apply(monoid.zero(), narrowK(ds)._1());
      }

      @Override
      public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<tuple1, T> nestedA) {
        return foldLeft(mb, narrowK(nestedA).<R>map(fn));
      }
    };
  }
  public static Comonad<tuple1> comonad(){
    return new ComonadByPure<tuple1>(){


      @Override
      public <T> T extract(Higher<tuple1, T> ds) {
        return narrowK(ds)._1();
      }

      @Override
      public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
        return functor().map(fn,ds);
      }

      @Override
      public <T> Higher<tuple1, T> unit(T value) {
        return Tuple1Instances.unit().unit(value);
      }
    };
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
