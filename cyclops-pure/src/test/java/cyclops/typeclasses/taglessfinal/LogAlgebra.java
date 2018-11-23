package cyclops.typeclasses.taglessfinal;

import com.oath.cyclops.hkt.Higher;
import cyclops.typeclasses.taglessfinal.Cases.Account;

public interface LogAlgebra<W> {

    Higher<W,Void> info(String message);
    Higher<W, Void> error(String message);

}
