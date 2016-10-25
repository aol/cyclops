package com.aol.cyclops.types.mixins;

/**
 * Mixin a print method with a return value (can be used as a method reference to
 * methods that accept Functions).
 * 
 * @author johnmcclean
 *
 */
public interface Printable {

    default <T> T print(final T object) {
        System.out.println(object);
        return object;
    }

}
