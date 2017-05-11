package cyclops.monads.function;

import cyclops.function.Fn4;
import cyclops.function.Fn5;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFn5<W extends WitnessType<W>,T1,T2,T3,T4,T5,R> extends Fn5<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,T3>,AnyM<W,T4>,AnyM<W,T5>,AnyM<W,R>> {

}
