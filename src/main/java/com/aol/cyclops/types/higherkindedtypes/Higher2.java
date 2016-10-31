package com.aol.cyclops.types.higherkindedtypes;

/**
 * 
 * Higher Kinded Type - a core type (e.g. a Xor) and the data types which it may store / manipulate (e.g. String and Exception).
 * A less strict alternative to org.derive4j.hkt.__2 (see https://github.com/aol/cyclops-react/issues/347).
 * Higher does not have an Annotation Processor to enforce the presence /correctness of the Witness type on the core type
 * T
 * 
 * @author johnmcclean
 *
 * @param <T1> Core type
 * @param <T2> First data type of the Core Type
 * @param <T3> Second data type of the Core type
 */
public interface Higher2<T1,T2,T3> extends Convert<Higher2<T1,T2,T3>> {
    
   
}
