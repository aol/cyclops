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

    private void subscribe(int index, boolean[] completed,Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs){

        operators.get(index).subscribeAll(e-> {
                    try {
                        onNext.accept(e);
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                        if(index<operators.size()-1)
                          subscribe(index+1,completed,onNext,onError,onCompleteDs);
                        if(!completed[0])
                            onCompleteDs.run();
                });
    }

    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        QueueX<StreamSubscription> subs = QueueX.empty();
        int index[] = {0};
        boolean[] finished = {false};
        AtomicReference<StreamSubscription> active = new AtomicReference<>(null);

        StreamSubscription sub = new StreamSubscription() {

            @Override
            public void request(long n) {
                System.out.println("Requesting " + n);
                if (n <= 0)
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                System.out.println("R 1" +  active.get());
                active.get().request(1);
                super.request(n);






            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };



        subscribeNext(finished,sub,active,index,onNext,onError,onComplete);



        return sub;
    }

    public void subscribeNext(boolean[] finished, StreamSubscription sub,AtomicReference<StreamSubscription> active,int[] index,Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete){
        if(index[0]==operators.size()){
            if(!finished[0]) {
                finished[0] = true;
                onComplete.run();
            }
        }
        active.set(operators.get(index[0]).subscribe(e-> {
                    try {
                        System.out.println("on next " +e);
                        onNext.accept(e);
                        sub.requested.decrementAndGet();
                        if(sub.isActive()){

                            active.get().request(1l);
                        }
                    } catch (Throwable t) {
                        onError.accept(t);
                    }
                }
                ,t->{
                    t.printStackTrace();
                    onError.accept(t);
                    active.get().request(1l);

                },()->{

                    if(index[0]==operators.size()){
                        if(!finished[0]) {
                            finished[0] = true;
                            onComplete.run();
                        }
                    }
                    index[0]++;
                    subscribeNext(finished,sub,active,index,onNext,onError,onComplete);
                    System.out.println("demenad " + sub.requested.get() + " " + active.get());
                    if(sub.requested.get()>0)
                        active.get().request(1l);


                }));
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
