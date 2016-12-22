package cyclops.function;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface MFunction0<W extends WitnessType<W>,R> extends F0<AnyM<W,R>> {

}
