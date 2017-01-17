package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class SpliteratorToOperator<T> implements Operator<T> {


    final Spliterator<T> split;
    Runnable run;
    boolean closed= false;
    public SpliteratorToOperator(Spliterator<? super T> split){
         this.split = (Spliterator<T>)split;


    }

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        StreamSubscription sub = new StreamSubscription(){
            @Override
            public void request(long n) {
                if(n<=0)
                    onError.accept(new IllegalArgumentException( "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                super.request(n);
                run.run();
            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };
        run = () -> {
            boolean canAdvance = true;
            while(sub.isActive() && canAdvance) {
                try {

                    canAdvance = split.tryAdvance(onNext);
                    if(canAdvance)
                      sub.requested.decrementAndGet();


                } catch (Throwable t) {
                    onError.accept(t);
                }
            }
            if(!canAdvance || !sub.isOpen) {
                closed = true;
                onComplete.run();
            }
        };
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        run = () -> {
            boolean canAdvance = true;
           while(canAdvance){
                try {
                    canAdvance = split.tryAdvance(onNext);
                } catch (Throwable t) {
                    onError.accept(t);
                }
            }

                closed = true;
                onCompleteDs.run();

        };
        run.run();

    }
}
