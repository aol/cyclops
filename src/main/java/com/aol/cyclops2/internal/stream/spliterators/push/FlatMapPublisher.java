package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Function;

import org.reactivestreams.Publisher;


public class FlatMapPublisher<T, R> extends BaseOperator<T, R> {

    final Function<? super T, ? extends Publisher<? extends R>> mapper;
    final int maxConcurrency;


    public FlatMapPublisher(Operator<? extends T> source,
                            Function<? super T, ? extends Publisher<? extends R>> mapper,
                            int maxConcurrency) {
        super((Operator<T>) source);
        this.mapper = mapper;
        this.maxConcurrency = maxConcurrency;


    }

    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        StreamSubscription sub[] = new StreamSubscription[1];
        ConcurrentFlatMapper<T, R>[] ref = new ConcurrentFlatMapper[1];
        StreamSubscription res = new StreamSubscription() {

            @Override
            public void request(long n) {

                if (n <= 0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                ref[0].request(n);

            }

            @Override
            public void cancel() {
                sub[0].cancel();
                super.cancel();

            }
        };
       sub[0] = source.subscribe(n ->
                ref[0].onNext(n), e -> ref[0].onError(e), () -> ref[0].onComplete());
        ref[0] = new ConcurrentFlatMapper<T, R>(sub[0], onNext, onError, onComplete,
                mapper,
                maxConcurrency);



        sub[0].request(maxConcurrency);
        return res;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
            subscribe(onNext,onError,onComplete).request(Long.MAX_VALUE);
    }

}