package cyclops.monads.function;

import cyclops.function.Fn7;
import cyclops.function.Fn8;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFn8<W extends WitnessType<W>,T1,T2,T3,T4,T5,T6,T7,T8,R> extends Fn8<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,T3>,AnyM<W,T4>,AnyM<W,T5>,AnyM<W,T6>,AnyM<W,T7>,AnyM<W,T8>,AnyM<W,R>> {

}
