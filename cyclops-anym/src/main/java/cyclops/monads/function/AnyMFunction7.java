package cyclops.monads.function;

import cyclops.function.Function7;
import cyclops.monads.WitnessType;
import cyclops.monads.AnyM;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction7<W extends WitnessType<W>,T1,T2,T3,T4,T5,T6,T7,R> extends Function7<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,T3>,AnyM<W,T4>,AnyM<W,T5>,AnyM<W,T6>,AnyM<W,T7>,AnyM<W,R>> {

}
