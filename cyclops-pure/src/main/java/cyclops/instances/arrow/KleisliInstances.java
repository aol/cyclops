package cyclops.instances.arrow;

import com.oath.cyclops.hkt.DataWitness.kleisli;
import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Kleisli;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;

import java.util.function.Function;

import static cyclops.arrow.Kleisli.narrowK;

public interface KleisliInstances {


  public static <W,IN> Functor<Higher<Higher<kleisli,W>,IN>> functor() {
    return new Functor<Higher<Higher<kleisli, W>, IN>>() {
      @Override
      public <T, R> Higher<Higher<Higher<kleisli, W>, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<kleisli, W>, IN>, T> ds) {
        return  narrowK(ds).map(fn);
      }
    };

  }

  public static <W,IN> Pure<Higher<Higher<kleisli,W>,IN>> unit(Monad<W> monad) {
    return new Pure<Higher<Higher<kleisli, W>, IN>>() {
      @Override
      public <T> Higher<Higher<Higher<kleisli, W>, IN>, T> unit(T value) {
        return Kleisli.of(monad,i->monad.unit(value));
      }
    };

  }

  public static <W,IN> Applicative<Higher<Higher<kleisli,W>,IN>> applicative(Monad<W> monad) {

    return new Applicative<Higher<Higher<kleisli, W>, IN>>() {
      @Override
      public <T, R> Higher<Higher<Higher<kleisli, W>, IN>, R> ap(Higher<Higher<Higher<kleisli, W>, IN>, ? extends Function<T, R>> fn, Higher<Higher<Higher<kleisli, W>, IN>, T> apply) {
        Kleisli<W, IN, ? extends Function<T, R>> k = narrowK(fn);
        Kleisli<W, IN, T> ap = narrowK(apply);
        return k.flatMapK(fn2-> ap.map(t -> fn2.apply(t)));
      }

      @Override
      public <T> Higher<Higher<Higher<kleisli, W>, IN>, T> unit(T value) {
        return KleisliInstances.<W,IN>unit(monad).unit(value);
      }

      @Override
      public <T, R> Higher<Higher<Higher<kleisli, W>, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<kleisli, W>, IN>, T> ds) {
        return KleisliInstances.<W,IN>functor().map(fn,ds);
      }
    };

  }





  public static <W,IN> Monad<Higher<Higher<kleisli,W>,IN>>  monad(Monad<W> monad) {

    return new Monad<Higher<Higher<kleisli, W>, IN>>() {
      @Override
      public <T, R> Higher<Higher<Higher<kleisli, W>, IN>, R> flatMap(Function<? super T, ? extends Higher<Higher<Higher<kleisli, W>, IN>, R>> fn, Higher<Higher<Higher<kleisli, W>, IN>, T> ds) {
        return narrowK(ds).flatMapK(fn.andThen(Kleisli::narrowK));
      }

      @Override
      public <T, R> Higher<Higher<Higher<kleisli, W>, IN>, R> ap(Higher<Higher<Higher<kleisli, W>, IN>, ? extends Function<T, R>> fn, Higher<Higher<Higher<kleisli, W>, IN>, T> apply) {
        return KleisliInstances.<W, IN>applicative(monad).ap(fn, apply);
      }

      @Override
      public <T> Higher<Higher<Higher<kleisli, W>, IN>, T> unit(T value) {
        return KleisliInstances.<W,IN>unit(monad).unit(value);
      }

      @Override
      public <T, R> Higher<Higher<Higher<kleisli, W>, IN>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<Higher<kleisli, W>, IN>, T> ds) {
        return KleisliInstances.<W,IN>functor().map(fn, ds);
      }
    };


  }

}
