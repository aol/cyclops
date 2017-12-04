package cyclops.monads.function;

import cyclops.function.Function2;
import cyclops.monads.WitnessType;
import cyclops.monads.AnyM;
import cyclops.monads.transformers.FutureT;
import cyclops.monads.transformers.ListT;

import java.util.function.BiFunction;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction2<W extends WitnessType<W>,T1,T2,R> extends Function2<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,R>> {
  static <W extends WitnessType<W>,T1,T2,R> AnyMFunction2<W,T1,T2,R> liftF(BiFunction<? super T1, ? super T2, ? extends R> fn){
    return AnyM.liftF2(fn);
  }
  static <W extends WitnessType<W>,T1,T2,R> AnyMFunction2<W,T1,T2,R> anyMZip(BiFunction<? super T1, ? super T2, ? extends R> fn) {
    return (a,b) -> a.zip(b,fn);
  }
  static <W extends WitnessType<W>,T1,T2,R> Function2<FutureT<W,T1>,FutureT<W,T2>, FutureT<W,R>> futureTM(BiFunction<? super T1, ? super T2, ? extends R> fn,W witness) {
    return (a,b) -> a.forEach2M(x->b,fn);
  }
  static <W extends WitnessType<W>,T1,T2,R> Function2<FutureT<W,T1>,FutureT<W,T2>, FutureT<W,R>> futureTZip(BiFunction<? super T1, ? super T2, ? extends R> fn,W witness) {
    return (a,b) -> a.zip(b,fn);
  }

  static <W extends WitnessType<W>,T1,T2,R> Function2<ListT<W,T1>,ListT<W,T2>, ListT<W,R>> listTM(BiFunction<? super T1, ? super T2, ? extends R> fn,W witness) {
    return (a,b) -> a.forEach2M(x->b,fn);
  }
  static <W extends WitnessType<W>,T1,T2,R> Function2<ListT<W,T1>,ListT<W,T2>, ListT<W,R>> listTZip(BiFunction<? super T1, ? super T2, ? extends R> fn,W witness) {
    return (a,b) -> a.zip(b,fn);
  }
}
