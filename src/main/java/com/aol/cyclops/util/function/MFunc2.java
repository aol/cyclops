package com.aol.cyclops.util.function;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.WitnessType;

/**
 * Created by johnmcclean on 18/12/2016.
 */
@FunctionalInterface
public interface MFunc2<W extends WitnessType<W>,T1,T2,R> extends F2<AnyM<W,T1>,AnyM<W,T2>,AnyM<W,R>> {

}
