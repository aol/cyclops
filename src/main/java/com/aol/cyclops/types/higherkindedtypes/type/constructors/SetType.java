package com.aol.cyclops.types.higherkindedtypes.type.constructors;

import java.util.Set;

import org.derive4j.hkt.Higher;
import org.derive4j.hkt.__;

import com.aol.cyclops.data.collections.extensions.standard.SetX;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Simulates Higher Kinded Types for Set's
 * 
 * SetType is a Set and a Higher Kinded Type (SetType.µ,T)
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Set
 */

public interface SetType<T> extends Higher<SetType.µ, T>, Set<T> {
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    public static class µ {
    }

    /**
     * Convert a Set to a simulated HigherKindedType that captures Set nature
     * and Set element data type separately. Recover via @see SetType#narrow
     * 
     * If the supplied Set implements SetType it is returned already, otherwise it
     * is wrapped into a Set implementation that does implement SetType
     * 
     * @param Set Set to widen to a SetType
     * @return SetType encoding HKT info about Sets
     */
    public static <T> SetType<T> widen(final Set<T> set) {
        if (set instanceof SetType)
            return (SetType<T>) set;
        return new Box<>(
                         SetX.fromIterable(set));
    }

    /**
     * Convert the HigherKindedType definition for a Set into
     * 
     * @param Set Type Constructor to convert back into narrowed type
     * @return SetX from Higher Kinded Type
     */
    public static <T> SetX<T> narrow(final Higher<SetType.µ, T> set) {
        if (set instanceof Set)
            return SetX.fromIterable((Set) set);
        final Box<T> type = (Box<T>) set;
        return type.narrow();
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Box<T> implements SetType<T> {

        @Delegate
        private final Set<T> boxed;

        /**
         * @return This back as a SetX
         */
        public SetX<T> narrow() {
            return SetX.fromIterable(boxed);
        }

        
       

    }

}
