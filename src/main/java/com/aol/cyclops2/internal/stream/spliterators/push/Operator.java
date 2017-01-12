package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public interface Operator<T> {


    public void cancel();
    public void subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete);

}
