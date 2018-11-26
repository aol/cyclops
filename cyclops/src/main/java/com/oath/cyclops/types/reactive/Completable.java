package com.oath.cyclops.types.reactive;


import org.reactivestreams.Publisher;

public interface Completable<T>{

    boolean isFailed();

    boolean isDone();
    boolean complete(T complete);
    boolean completeExceptionally(Throwable error);


}

