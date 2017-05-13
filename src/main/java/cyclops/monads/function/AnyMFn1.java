package cyclops.monads.function;

import cyclops.monads.AnyM;
import cyclops.monads.Kleisli;
import cyclops.monads.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFn1<W extends WitnessType<W>,T1,R> extends Kleisli<W,AnyM<W,T1>,R> {

}
