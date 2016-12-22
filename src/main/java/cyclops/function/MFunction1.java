package cyclops.function;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface MFunction1<W extends WitnessType<W>,T1,R> extends F1<AnyM<W,T1>,AnyM<W,R>> {

}
