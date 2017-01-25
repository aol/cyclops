package com.aol.cyclops2.internal.stream.spliterators.push;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Spliterator;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class PublisherToOperator<T> implements Operator<T> {


    final Publisher<T> split;

    boolean closed= false;

    public PublisherToOperator(Publisher<? super T> split){
         this.split = (Publisher<T>)split;


    }

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        Subscription[] sArray = {null};
        StreamSubscription sub = new StreamSubscription(){

            @Override
            public void request(long n) {
                super.request(n);
                if(sArray[0]!=null)
                    sArray[0].request(n);

            }

            @Override
            public void cancel() {
                super.cancel();
                closed = true;
                if(sArray[0]!=null)
                    sArray[0].cancel();
            }
        };

            split.subscribe(new Subscriber<T>() {
                @Override
                public void onSubscribe(Subscription s) {
                    sArray[0] = s;
                    if (sub.isActive()) {
                        s.request(1l);

                    }else if(!sub.isOpen){
                        s.cancel();
                    }

                }

                @Override
                public void onNext(T t) {
                    System.out.println("PublisherOp " + t);
                   onNext.accept(t);
                   sub.requested.decrementAndGet();

                }

                @Override
                public void onError(Throwable t) {
                    onError.accept(t);
                }

                @Override
                public void onComplete() {
                    System.out.println("Publisher On compelte!");
                    onComplete.run();
                    closed = true;
                }
            });


        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

            split.subscribe(new Subscriber<T>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(Long.MAX_VALUE);

                }

                @Override
                public void onNext(T t) {

                        onNext.accept(t);


                }

                @Override
                public void onError(Throwable t) {
                    onError.accept(t);
                }

                @Override
                public void onComplete() {
                    onCompleteDs.run();
                    closed = true;
                }
            });




    }
}
