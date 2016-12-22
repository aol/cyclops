package cyclops.monads;

import com.aol.cyclops.types.extensability.FunctionalAdapter;

public interface WitnessType<W extends WitnessType<W>> {
    
     FunctionalAdapter<W> adapter();
}
