package com.aol.cyclops.types.experimental.higherkindedtypes;

import org.jooq.lambda.Seq;

import com.aol.cyclops.control.ReactiveSeq;



public interface ReactiveSeqType<T> extends  Higher2<ReactiveSeqType.reactiveSeq,T>,Seq<T>{
    
    static class reactiveSeq{}
    
    default ReactiveSeq<T> reactiveSeq(){
        return ReactiveSeq.fromIterable(this);
    }
}

