package cyclops.monads.function;

import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction4<W extends WitnessType<W>,T1,T2,T3,T4,R> extends Function4<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,T3>,AnyM<W,T4>,AnyM<W,R>> {

}
