package com.aol.cyclops.types.higherkindedtypes.type.constructors;

import java.util.List;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.higherkindedtypes.Higher;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;




/**
 * Simulates Higher Kinded Types for List's
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the List
 */
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public final class ListType<T> implements  Higher<ListType.listx,T>{
    
    /**
     * Convert a List to a simulated HigherKindedType that captures List nature
     * and List element data type separately. Recover via @see ListType#narrow
     * 
     * @param list
     * @return
     */
    public static <T> ListType<T> widen(List<T> list){
        return new ListType<>(ListX.fromIterable(list));
    }
    /**
     * Convert the HigherKindedType definition for a List into
     * @param list Type Constructor to convert back into narrowed type
     * @return ListX from Higher Kinded Type
     */
    public static <T> ListX<T> narrow(Higher<ListType.listx, T> list){
        return (ListX<T>)list;
    }
    
    
    private final ListX<T> boxed;
    
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    public static class listx{}
    
    /**
     * @return This back as a ListX
     */
    public ListX<T> narrow(){
        return boxed;
    }
    
}

