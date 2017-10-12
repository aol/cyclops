package cyclops.control.anym.function;

import cyclops.function.Function3;
import cyclops.control.anym.AnyM;
import cyclops.control.anym.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction3<W extends WitnessType<W>,T1,T2,T3,R> extends Function3<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,T3>,AnyM<W,R>> {

}
