package com.aol.cyclops.types.anyM;

import com.aol.cyclops.types.extensability.Comprehender;

public interface WitnessType {
    
    <W extends WitnessType> Comprehender<W> adapter();
}
