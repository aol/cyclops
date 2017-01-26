package com.aol.cyclops2.types;



/**
 * Created by johnmcclean on 26/01/2017.
 */
public interface Completable<T> {

    default boolean isFailed(){
        return false;
    }

    default boolean isDone(){
        return true;
    }
    default boolean complete(T complete){
        return false;
    }
    default  boolean completeExceptionally(Throwable error){
        return false;
    }

}

