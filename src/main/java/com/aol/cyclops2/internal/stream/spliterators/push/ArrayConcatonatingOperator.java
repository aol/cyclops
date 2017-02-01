package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.collections.DequeX;
import cyclops.collections.ListX;
import cyclops.collections.QueueX;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class ArrayConcatonatingOperator<IN> implements Operator<IN> {


    private final ListX<Operator<IN>> operators;


    public ArrayConcatonatingOperator(Operator<IN>... sources){
        this.operators = ListX.empty();
        for(Operator<IN> next : sources){
            operators.add(next);
        }


    }



    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        QueueX<StreamSubscription> subs = QueueX.empty();
        int index[] = {0};
        boolean[] finished = {false};
        AtomicReference<StreamSubscription> active = new AtomicReference<>(null);

        Concat[] ref = {null};
        StreamSubscription sub = new StreamSubscription() {

            @Override
            public void request(long n) {
                System.out.println("Requesting " + n);
                if (n <= 0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }

                System.out.println("R 1" +  active.get());

                ref[0].request(n);





            }

            @Override
            public void cancel() {
                ref[0].cancel();
                super.cancel();

            }
        };

        Concat c = new Concat(sub,operators,onNext,onError,onComplete);
        ref[0]=c;






        return sub;
    }





    @Override
    public void subscribeAll(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        subscribeAll(0,onNext,onError,onCompleteDs);



    }
    public void subscribeAll(int index,Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        if(index>=operators.size()) {
            onCompleteDs.run();
            return;
        }


        Operator<IN> next = operators.get(index);
        next.subscribeAll(onNext,onError,()->subscribeAll(index+1,onNext,onError,onCompleteDs));



    }
}
