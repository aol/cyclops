package cyclops.control.anym.function;

import cyclops.control.anym.AnyM;
import cyclops.control.anym.KleisliM;
import cyclops.control.anym.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface AnyMFunction1<W extends WitnessType<W>,T1,R> extends KleisliM<W,AnyM<W,T1>,R> {

}
