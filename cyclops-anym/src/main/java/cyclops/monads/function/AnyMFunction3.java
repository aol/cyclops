package cyclops.monads.function;

import cyclops.function.Function3;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;

import java.util.function.BiFunction;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction3<W extends WitnessType<W>,T1,T2,T3,R> extends Function3<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,T3>,AnyM<W,R>> {
  static <W extends WitnessType<W>,T1,T2,T3,R> AnyMFunction3<W,T1,T2,T3,R> liftF(Function3<? super T1, ? super T2, ? super T3, ? extends R> fn){
    return (u1, u2, u3) -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3))));
  }

}
