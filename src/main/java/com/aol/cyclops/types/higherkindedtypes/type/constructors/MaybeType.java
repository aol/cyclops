package com.aol.cyclops.types.higherkindedtypes.type.constructors;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.types.higherkindedtypes.Higher;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;




/**
 * Simulates Higher Kinded Types for Maybe's
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the List
 */
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public final class MaybeType<T> implements  Higher<MaybeType.maybe,T>{
    
    /**
     * Convert a Maybe to a simulated HigherKindedType that captures Maybe nature
     * and Maybe element data type separately. Recover via @see MaybeType#narrow
     * 
     * @param maybe Maybe to widen
     * @return MaybeType 
     */
    public static <T> MaybeType<T> widen(Maybe<T> maybe){
        return new MaybeType<>(maybe);
    }
    /**
     * Convert the HigherKindedType definition for a Maybe into a Maybe
     * @param maybeType Type Constructor to convert
     * @return Maybe
     */
    public static <T> Maybe<T> narrow(Higher<MaybeType.maybe, T> maybeType){
        return ((MaybeType<T>)maybeType).narrow();
    }
    
    
    private final Maybe<T> boxed;
    
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    public static class maybe{}
    
    /**
     * @return This back as a Maybe
     */
    public Maybe<T> narrow(){
        return boxed;
    }
    
}

