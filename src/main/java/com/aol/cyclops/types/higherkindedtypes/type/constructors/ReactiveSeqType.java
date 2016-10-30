package com.aol.cyclops.types.higherkindedtypes.type.constructors;




import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.higherkindedtypes.Higher;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;




/**
 * Simulates Higher Kinded Types for ReactiveSeq's
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the ReactiveSeq
 */
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public final class ReactiveSeqType<T> implements  Higher<ReactiveSeqType.reactiveseq,T>{
    
    /**
     * Convert a ReactiveSeq to a simulated HigherKindedType that captures ReactiveSeq nature
     * and ReactiveSeq element data type separately. Recover via @see ReactiveSeqType#narrow
     * 
     * @param ReactiveSeq
     * @return
     */
    public static <T> ReactiveSeqType<T> widen(ReactiveSeq<T> reactiveSeq){
        return new ReactiveSeqType<>(reactiveSeq);
    }
    /**
     * Convert the HigherKindedType definition for a ReactiveSeq into
     * @param ReactiveSeq
     * @return
     */
    public static <T> ReactiveSeq<T> narrow(Higher<ReactiveSeqType.reactiveseq, T> reactiveSeq){
        return ((ReactiveSeqType<T>)reactiveSeq).narrow();
    }
    
    
    private final ReactiveSeq<T> boxed;
    
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    public static class reactiveseq{}
    
    /**
     * @return This back as a ReactiveSeq
     */
    public ReactiveSeq<T> narrow(){
        return boxed;
    }
    
}

