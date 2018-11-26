package cyclops.typeclasses.taglessfinal;


import com.oath.cyclops.hkt.DataWitness;
import com.oath.cyclops.hkt.DataWitness.identity;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Identity;
import cyclops.reactive.IO;

public class LogID implements LogAlgebra<identity> {


    @Override
    public Higher<identity, Void> info(String message) {
        return Identity.of(null);
    }

    @Override
    public Higher<identity, Void> error(String message) {
        return Identity.of(null);
    }
}
