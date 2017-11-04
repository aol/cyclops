package cyclops.typeclasses;

import java.util.function.Predicate;
import com.oath.cyclops.hkt.Higher;

public interface Filterable<CRE> {

    public <T> Higher<CRE,T> filter(Predicate<? super T> predicate, Higher<CRE,T> ds);
}
