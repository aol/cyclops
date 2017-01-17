package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class IterableSourceOperator<T> implements Operator<T> {


    final Iterable<T> values;


    public IterableSourceOperator(Iterable<T> values){
        this.values = values;
    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        final Iterator<T> it = values.iterator();

        boolean complete[] ={false};
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{
                if (n == Long.MAX_VALUE) {
                    pushAll();

                    return;
                }

                while (isActive() && it.hasNext()) {

                    ((Consumer) onNext).accept(it.next());
                    requested.decrementAndGet();
                }

                if (!it.hasNext() && !complete[0]) {
                    onComplete.run();
                    complete[0]=true;

                }

            };
            @Override
            public void request(long n) {
                if(n<=0)
                    onError.accept(new IllegalArgumentException( "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                singleActiveRequest(n,work);

            }

            private void pushAll() {
                while(it.hasNext()){
                    if(!isOpen)
                        break;
                   ((Consumer) onNext).accept(it.next());
                }
                requested.set(0);
                onComplete.run();
            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        final Iterator<T> it = values.iterator();
        while(it.hasNext())
            ((Consumer)onNext).accept(it.next());
        onCompleteDs.run();
    }
}
