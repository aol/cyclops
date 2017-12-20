package cyclops.instances.free;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.Higher;
import cyclops.free.FreeAp;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

@UtilityClass
public class FreeApInstances {
  public static <F> Applicative<Higher<DataWitness.freeAp, F>> applicative(cyclops.typeclasses.Pure<F> pure, Functor<F> functor) {
    return new Applicative<Higher<DataWitness.freeAp, F>>() {

      @Override
      public <T, R> Higher<Higher<DataWitness.freeAp, F>, R> ap(Higher<Higher<DataWitness.freeAp, F>, ? extends Function<T, R>> fn, Higher<Higher<DataWitness.freeAp, F>, T> apply) {
        FreeAp<F, ? extends Function<T, R>> f = FreeAp.narrowK(fn);
        FreeAp<F, T> a = FreeAp.narrowK(apply);
        return a.ap(f);

      }

      @Override
      public <T, R> Higher<Higher<DataWitness.freeAp, F>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<DataWitness.freeAp, F>, T> ds) {
        return FreeAp.narrowK(ds).map(fn);
      }

      @Override
      public <T> Higher<Higher<DataWitness.freeAp, F>, T> unit(T value) {
        return FreeAp.pure(value);
      }
    };


  }
}
