package cyclops.instances.free;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.free;
import com.oath.cyclops.hkt.Higher;
import cyclops.free.Free;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;

import java.util.function.Function;

public class FreeInstances {

  public static <F> Pure<Higher<free, F>> pure(Pure<F> pure, Functor<F> functor) {
    return new Pure<Higher<free, F>>() {
      @Override
      public <T> Higher<Higher<free, F>, T> unit(T value) {
        return Free.liftF(pure.unit(value),functor);
      }
    };
  }
  public static <F> Functor<Higher<free, F>> functor() {
    return new Functor<Higher<free, F>>() {
      @Override
      public <T, R> Higher<Higher<free, F>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<free, F>, T> ds) {
        return Free.narrowK(ds).map(fn);
      }
    };
  }
  public static <F> Applicative<Higher<free, F>> applicative(Pure<F> pure, Functor<F> functor) {
    return new Applicative<Higher<free, F>>() {

      @Override
      public <T, R> Higher<Higher<free, F>, R> ap(Higher<Higher<free, F>, ? extends Function<T, R>> fn, Higher<Higher<free, F>, T> apply) {
        Free<F, ? extends Function<T, R>> f = Free.narrowK(fn);
        Free<F, T> a = Free.narrowK(apply);
        return f.flatMap(x->a.map(t->x.apply(t)));
      }

      @Override
      public <T, R> Higher<Higher<free, F>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<free, F>, T> ds) {
        return Free.narrowK(ds).map(fn);
      }

      @Override
      public <T> Higher<Higher<free, F>, T> unit(T value) {
        return Free.liftF(pure.unit(value),functor);
      }
    };
  }
  public static <F> Monad<Higher<free, F>> monad(Pure<F> pure, Functor<F> functor) {
    return new Monad<Higher<free, F>>() {
      @Override
      public <T, R> Higher<Higher<free, F>, R> flatMap(Function<? super T, ? extends Higher<Higher<free, F>, R>> fn, Higher<Higher<free, F>, T> ds) {
        return Free.narrowK(ds).flatMap(f->Free.narrowK(fn.apply(f)));
      }

      @Override
      public <T, R> Higher<Higher<free, F>, R> ap(Higher<Higher<free, F>, ? extends Function<T, R>> fn, Higher<Higher<free, F>, T> apply) {
        return FreeInstances.<F>applicative(pure,functor).ap(fn,apply);
      }

      @Override
      public <T> Higher<Higher<free, F>, T> unit(T value) {
        return FreeInstances.<F>pure(pure,functor).unit(value);
      }

      @Override
      public <T, R> Higher<Higher<free, F>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<free, F>, T> ds) {
        return FreeInstances.<F>functor().map(fn,ds);
      }
    };
  }
}
