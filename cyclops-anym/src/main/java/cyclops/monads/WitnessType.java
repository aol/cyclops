package cyclops.monads;

import com.oath.anym.extensability.FunctionalAdapter;

public interface WitnessType<W extends WitnessType<W>> {

     FunctionalAdapter<W> adapter();
}
