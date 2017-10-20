package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;

/**
 * Created by wyang14 on 17/07/2017.
 */
public class CompleteOperator<T> extends BaseOperator<T, T> {


    final Runnable complete;

    public CompleteOperator(Operator<T> source, Runnable complete) {
        super(source);
        this.complete = complete;
    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        return source.subscribe(onNext, onError, () -> {
            complete.run();
            onComplete.run();
        });
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        source.subscribeAll(onNext, onError, () -> {
            complete.run();
            onCompleteDs.run();
        });
    }

    /*public static void main(String[] args) {
        Integer value = ReactiveSeq.of(1, 2, 3, 4).map(i -> i + 2).complete(() -> {
            System.out.println("Done.");
        }).firstValue();
        System.out.println(value);
    }*/
}
