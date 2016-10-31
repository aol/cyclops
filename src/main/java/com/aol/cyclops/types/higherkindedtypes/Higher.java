package com.aol.cyclops.types.higherkindedtypes;

/**
 * Higher Kinded Type - a core type (e.g. a List) and a data type of the elements within the List (e.g. Integers).
 * A less strict alternative to org.derive4j.hkt.__ (see https://github.com/aol/cyclops-react/issues/347).
 * Higher does not have an Annotation Processor to enforce the presence /correctness of the Witness type on the core type
 * This means it can be used to retro fit support for Higher Kinded Types onto 3rd party libraries that don't have them
 * 
 * @author johnmcclean
 *
 * @param <T1> Core type
 * @param <T2> Data type of elements in Core Type
 */
public interface Higher<T1,T2> extends Convert<Higher<T1,T2>>{

    
}
