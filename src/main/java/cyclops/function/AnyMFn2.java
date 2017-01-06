package cyclops.function;

import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFn2<W extends WitnessType<W>,T1,T2,R> extends Fn2<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,R>> {

}
