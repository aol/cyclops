package com.aol.cyclops.types.experimental.higherkindedtypes;

import java.util.List;

import com.aol.cyclops.data.collections.extensions.standard.ListX;



/**
 * Simulates Higher Kinded Types for List's
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the ListX
 */
public interface ListType<T> extends  Higher2<ListType.listx,T>,List<T>{
    
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    static class listx{}
    
    /**
     * @return This back as a ListX
     */
    default ListX<T> list(){
        return ListX.fromIterable(this);
    }
    
}

