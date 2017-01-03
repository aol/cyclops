package cyclops.typeclasses;


import com.aol.cyclops.hkt.Higher;

/**
 * Type class for creating instances of types
 * 
 * @author johnmcclean
 *
 * @param <CRE> The core type of the unit (e.g. the HKT witness type, not the generic type : ListType.µ)
 */
@FunctionalInterface
public interface Pure<CRE> {
    /**
     * Create a new instance of the core type (e.g. a List or CompletableFuture) that is HKT encoded
     * 
     * @param value To populate new instance of
     * @return HKT encoded new instance with supplied value
     */
    public <T> Higher<CRE,T> unit(T value);
}
