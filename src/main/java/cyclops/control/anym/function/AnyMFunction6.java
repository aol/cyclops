package cyclops.control.anym.function;

import cyclops.function.Function6;
import cyclops.control.anym.AnyM;
import cyclops.control.anym.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction6<W extends WitnessType<W>,T1,T2,T3,T4,T5,T6,R> extends Function6<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,T3>,AnyM<W,T4>,AnyM<W,T5>,AnyM<W,T6>,AnyM<W,R>> {

}
