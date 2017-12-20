package cyclops.instances.free;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.Higher;
import cyclops.free.Free;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;

import java.util.function.Function;

public class FreeInstances {
  public static <F> Applicative<Higher<DataWitness.free, F>> applicative(cyclops.typeclasses.Pure<F> pure, Functor<F> functor) {
    return new Applicative<Higher<DataWitness.free, F>>() {

      @Override
      public <T, R> Higher<Higher<DataWitness.free, F>, R> ap(Higher<Higher<DataWitness.free, F>, ? extends Function<T, R>> fn, Higher<Higher<DataWitness.free, F>, T> apply) {
        Free<F, ? extends Function<T, R>> f = Free.narrowK(fn);
        Free<F, T> a = Free.narrowK(apply);
        return f.flatMap(x->a.map(t->x.apply(t)));
      }

      @Override
      public <T, R> Higher<Higher<DataWitness.free, F>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<DataWitness.free, F>, T> ds) {
        return Free.narrowK(ds).map(fn);
      }

      @Override
      public <T> Higher<Higher<DataWitness.free, F>, T> unit(T value) {
        return Free.liftF(pure.unit(value),functor);
      }
    };


  }
}
