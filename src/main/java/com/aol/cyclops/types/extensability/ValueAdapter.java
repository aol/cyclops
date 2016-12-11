package com.aol.cyclops.types.extensability;

import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.WitnessType;

public interface ValueAdapter<W extends WitnessType<W>> extends FunctionalAdapter<W> {

    <T> T get(AnyMValue<W,T> t);
}
