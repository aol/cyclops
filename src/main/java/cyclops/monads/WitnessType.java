package cyclops.monads;

import com.aol.cyclops2.types.extensability.FunctionalAdapter;

import java.util.Optional;

public interface WitnessType<W extends WitnessType<W>> {
    
     FunctionalAdapter<W> adapter();
}
