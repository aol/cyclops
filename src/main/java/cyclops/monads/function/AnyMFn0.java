package cyclops.monads.function;

import cyclops.function.Fn0;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFn0<W extends WitnessType<W>,R> extends Fn0<AnyM<W,R>> {

}
