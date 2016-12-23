package com.aol.cyclops.hkt;

import java.util.function.Function;

public interface Convert<T extends Convert<?>> {

    /**
     * Fluent interface for converting this type to another
     *
     * @param reduce Funtion to convert this type
     * @return Converted type
     */
    default <R> R convert(Function<? super T,? extends R> reduce){
        return reduce.apply((T)this);
    }
}