package com.oath.cyclops.types;

import java.util.function.Supplier;

/**
 *
 * Data type that represents a wrapper type
 *
 * @author johnmcclean
 *
 */
public interface Unwrappable {
    /**
     * Unwrap a wrapped value
     *
     * @return wrapped value
     */
    default <R> R unwrap() {
        return (R) this;
    }

    default <R> R unwrapIfInstance(Class<?> c,Supplier<? extends R> supplier ){
        R unwrapped = unwrap();
        if(c.isAssignableFrom(unwrapped.getClass())){
            return unwrapped;
        }
        return supplier.get();
    }
    default <R> R unwrapNested(Class<?> c,Supplier<? extends R> supplier ){
        R unwrapped = unwrap();
        while(unwrapped instanceof Unwrappable){
            unwrapped = ((Unwrappable) unwrapped).unwrap();
        }
        if(c.isAssignableFrom(unwrapped.getClass())){
            return unwrapped;
        }
        return supplier.get();
    }
}
