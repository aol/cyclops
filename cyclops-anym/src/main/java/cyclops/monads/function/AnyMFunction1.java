package cyclops.monads.function;

import cyclops.monads.KleisliM;
import cyclops.monads.WitnessType;
import cyclops.monads.AnyM;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction1<W extends WitnessType<W>,T1,R> extends KleisliM<W,AnyM<W,T1>,R> {

}
