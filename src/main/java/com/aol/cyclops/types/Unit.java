package com.aol.cyclops.types;

/** 
 * A Data type that supports instantiation of instances of the same type 
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) stored inside this Pure instance
 */
@FunctionalInterface
public interface Unit<T> {

    public <T> Unit<T> unit(T unit);

}
