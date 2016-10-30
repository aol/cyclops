package com.aol.cyclops.types.higherkindedtypes.type.constructors;

import java.util.Set;

import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.higherkindedtypes.Higher;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Simulates Higher Kinded Types for Set's
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Set
 */
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public final class SetType<T> implements  Higher<SetType.setx,T>{
    
    /**
     * Convert a Set to a simulated HigherKindedType that captures Set nature
     * and Set element data type separately. Recover via @see SetType#narrow
     * 
     * @param set To convert into a HigherKindedType type constructor
     * @return SetType
     */
    public static <T> SetType<T> widen(Set<T> set){
        return new SetType<>(SetX.fromIterable(set));
    }
    /**
     * Convert the HigherKindedType definition for a Set into
     * @param set Type Constructor to convert back into narrowed type
     * @return SetX from Higher Kinded Type
     */
    public static <T> SetX<T> narrow(Higher<SetType.setx, T> set){
        return ((SetType<T>)set).narrow();
    }
    
    
    private final SetX<T> boxed;
    
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    public static class setx{}
    
    /**
     * @return This back as a SetX
     */
    public SetX<T> narrow(){
        return boxed;
    }
    
}

