package com.aol.cyclops.types.higherkindedtypes.type.constructors;

import java.util.Optional;

import org.derive4j.hkt.Higher;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Simulates Higher Kinded Types for Optional's
 * 
 * OptionalType is a Optional and a Higher Kinded Type (OptionalType.µ,T)
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Optional
 */

public interface OptionalType<T> extends Higher<OptionalType.µ, T> {
        
   
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    public static class µ {
    }
    
    /**
     * Convert a Optional to a simulated HigherKindedType that captures Optional nature
     * and Optional element data type separately. Recover via @see OptionalType#narrow
     * 
     * If the supplied Optional implements OptionalType it is returned already, otherwise it
     * is wrapped into a Optional implementation that does implement OptionalType
     * 
     * @param Optional Optional to widen to a OptionalType
     * @return OptionalType encoding HKT info about Optionals
     */
    public static <T> OptionalType<T> widen(final Optional<T> Optional) {
        
        return new Box<>(Optional);
    }

    /**
     * Convert the HigherKindedType definition for a Optional into
     * 
     * @param Optional Type Constructor to convert back into narrowed type
     * @return Optional from Higher Kinded Type
     */
    public static <T> Optional<T> narrow(final Higher<OptionalType.µ, T> Optional) {
        
         return ((Box<T>)Optional).narrow();
        
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Box<T> implements OptionalType<T> {

       
        private final Optional<T> boxed;

        /**
         * @return wrapped Optional
         */
        public Optional<T> narrow() {
            return boxed;
        }

        
       

    }
}
