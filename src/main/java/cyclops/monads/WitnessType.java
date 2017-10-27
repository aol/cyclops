package cyclops.monads;

import com.oath.cyclops.types.extensability.FunctionalAdapter;

public interface WitnessType<W extends WitnessType<W>> {

     FunctionalAdapter<W> adapter();
}
