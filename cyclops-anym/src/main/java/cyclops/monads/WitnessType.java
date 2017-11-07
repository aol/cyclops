package cyclops.monads;

import com.oath.anym.extensability.MonadAdapter;

public interface WitnessType<W extends WitnessType<W>> {

     MonadAdapter<W> adapter();
}
