package com.aol.cyclops.types.anyM;

import com.aol.cyclops.types.extensability.FunctionalAdapter;

public interface WitnessType<W extends WitnessType<?>> {
    
     FunctionalAdapter<W> adapter();
}
