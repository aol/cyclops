package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public interface Operator<T> {


    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete);
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete);

}
