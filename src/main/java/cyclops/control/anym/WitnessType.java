package cyclops.control.anym;

import com.aol.cyclops2.types.extensability.FunctionalAdapter;

public interface WitnessType<W extends WitnessType<W>> {
    
     FunctionalAdapter<W> adapter();
}
