package cyclops.monads.function;

import cyclops.function.Function0;
import cyclops.function.Function1;
import cyclops.monads.AnyMs;
import cyclops.monads.KleisliM;
import cyclops.monads.WitnessType;
import cyclops.monads.AnyM;
import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;
import cyclops.reactive.collections.mutable.ListX;

import java.util.function.Function;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction1<W extends WitnessType<W>,T1,R> extends KleisliM<W,AnyM<W,T1>,R> {

  static <W1 extends WitnessType<W1>,W2 extends WitnessType<W2>,T,R> Function1<AnyM<W1,T>,AnyM<W2,R>> liftAnyM(Function<? super T, ? extends R> fn,Function<AnyM<W1,T>,AnyM<W2,T>> hktTransform){
    return (T1)-> hktTransform.apply(T1).map(fn);
  }
  static <W extends WitnessType<W>,T,R> AnyMFunction1<W, T,R> liftF(Function<? super T, ? extends R> fn){
    return AnyM.liftF(fn);
  }

  static <W extends WitnessType<W>,T,R> AnyM<W, R> mapF(Function<? super T, ? extends R> fn,AnyM<W, T> functor) {
    return functor.map(fn);
  }
  static <W extends WitnessType<W>,T,R> FutureT<W,R> mapF(Function<? super T, ? extends R> fn, FutureT<W,T> future) {
    return future.map(fn);
  }
  static <W extends WitnessType<W>,T,R> ListT<W,R> mapF(Function<? super T, ? extends R> fn, ListT<W,T> list) {
    return list.map(fn);
  }
  static <W extends WitnessType<W>,T,R> Function1<T, FutureT<W,R>> liftFutureT(Function1<? super T, ? extends R> fn,W witness) {
    Function1<T, R> a = Function1.narrow(fn);
    Function1<T, FutureT<W, R>> x = a.functionOps().liftFuture().andThen(f -> AnyMs.liftM(f, witness));
    return x;
  }
  static <W extends WitnessType<W>,T,R> Function1<T, ListT<W,R>> liftListT(Function1<? super T, ? extends R> fn,W witness) {
      Function1<T,ListX<R>> f = i-> ListX.of(fn.apply(i));
    return f.andThen(l->AnyMs.liftM(l,witness));
  }
}
