package cyclops.monads.function;

import cyclops.function.Function0;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction0<W extends WitnessType<W>,R> extends Function0<AnyM<W,R>> {

}
