package cyclops.control.anym.function;

import cyclops.function.Function2;
import cyclops.control.anym.AnyM;
import cyclops.control.anym.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction2<W extends WitnessType<W>,T1,T2,R> extends Function2<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,R>> {

}
