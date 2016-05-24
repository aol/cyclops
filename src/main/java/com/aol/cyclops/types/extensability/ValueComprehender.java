package com.aol.cyclops.types.extensability;

import java.util.Iterator;

public interface ValueComprehender<T> extends Comprehender<T> {
    default T fromIterator(Iterator it){
        if(!it.hasNext())
            return empty();
        return of(it.next());
    }
}
