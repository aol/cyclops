package cyclops.monads.function;

import cyclops.monads.AnyM;
import cyclops.monads.KleisliM;
import cyclops.monads.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFn1<W extends WitnessType<W>,T1,R> extends KleisliM<W,AnyM<W,T1>,R> {

}
