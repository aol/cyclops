package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.io;
import com.oath.cyclops.hkt.Higher;
import cyclops.reactive.IO;

public class LogIO implements LogAlgebra<io> {


    @Override
    public Higher<io, Void> info(String message) {
        return IO.of(null);
    }

    @Override
    public Higher<io, Void> error(String message) {
        return IO.of(null);
    }
}
