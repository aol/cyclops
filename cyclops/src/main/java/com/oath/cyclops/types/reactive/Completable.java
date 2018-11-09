package com.oath.cyclops.types.reactive;


public interface Completable<T> {

    boolean isFailed();

    boolean isDone();
    boolean complete(T complete);
    boolean completeExceptionally(Throwable error);


}

