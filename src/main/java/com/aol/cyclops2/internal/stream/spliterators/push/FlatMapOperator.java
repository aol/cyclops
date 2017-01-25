package com.aol.cyclops2.internal.stream.spliterators.push;

import org.reactivestreams.Subscription;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class FlatMapOperator<T,R> extends BaseOperator<T,R> {


    final Function<? super T, ? extends Stream<? extends R>> mapper;;

    public FlatMapOperator(Operator<T> source, Function<? super T, ? extends Stream<? extends R>> mapper){
        super(source);
        this.mapper = mapper;

    }

    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        StreamSubscription[] s = {null} ;
        boolean[] completeRecieved = {false};
        boolean[] innerComplete = {false};
        AtomicInteger status = new AtomicInteger(0);
        //  0/1           2/3           1
        // 0/active
        // 1/outer complete
        //              0/inner ready
        //              1/inner actvive

        Runnable[] thunk= {()->{
            if(completeRecieved[0]){
                onComplete.run();
            }else{
                s[0].request(1);
            }
        }};


        StreamSubscription res = new StreamSubscription(){
            LongConsumer work = n-> {
                System.out.println("New demand! Requesting on thread " + Thread.currentThread().getId() + " demand "  + this.requested.get());
                thunk[0].run();
            };
            @Override
            public void request(long n) {
                if(n<=0)
                    onError.accept(new IllegalArgumentException( "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));

                this.singleActiveRequest(n,work);

            }

            @Override
            public void cancel() {
                s[0].cancel();
                super.cancel();
            }
        };
        s[0] = source.subscribe(e-> {
                    try {
                        System.out.println("On next " + e);
                        Spliterator<? extends R> split = mapper.apply(e).spliterator();
                        System.out.println("Status is " + status.get());
                        int statusLocal =-1;
                        do {
                            statusLocal = status.get();
                            System.out.println("Active starting  Setting to " + (statusLocal | (1 << 1)));

                        }while(!status.compareAndSet(statusLocal,statusLocal | (1 << 1))); //set inner active
                        System.out.println("Status is " + status.get());
                        AtomicInteger advancing = new AtomicInteger(0);
                        thunk[0] = () -> {

                                boolean canAdvance = false;
                            System.out.println("Attempting to progress on " + Thread.currentThread().getId());
                            if (!advancing.compareAndSet(0, (int)Thread.currentThread().getId())) {
                                System.out.println("Failed to become active - returning " + Thread.currentThread().getId());
                                return;
                            }
                            try {
                                while (res.isActive()) {
                                    try {

                                        System.out.println("Inner advancing on thread " + Thread.currentThread().getId() + " advancing " + advancing.get() + " demand " + res.requested.get());
                                        canAdvance = split.tryAdvance(onNext);

                                    } catch (Throwable t) {
                                        onError.accept(t);
                                    }
                                    System.out.println("Sent value? " + canAdvance + " on thread " + Thread.currentThread().getId() + " demand " + res.requested.get());
                                    if (canAdvance) {
                                        res.requested.decrementAndGet();
                                        System.out.println("Decremented demand " + res.requested.get());
                                    } else {
                                        System.out.println("Completing inner..");
                                        int thunkStatusLocal = -1;
                                        do {
                                            thunkStatusLocal = status.get();
                                            System.out.println("Inner complete Setting to " + (thunkStatusLocal & ~(1 << 1)));

                                        }
                                        while (!status.compareAndSet(thunkStatusLocal, thunkStatusLocal & ~(1 << 1))); //unset inner active
                                        System.out.println("Status is " + status.get());
                                        if (status.compareAndSet(1, 100)) {
                                            onComplete.run();
                                            return;
                                        }
                                        break;
                                    }

                                    System.out.println("Looping " + res.isActive());


                                }
                            }finally{
                                advancing.set(0);
                            }
                            if(!canAdvance && res.isActive() && !(status.get()>=100)) {

                                    System.out.println("Outer request on "  + Thread.currentThread().getId());
                                s[0].request(1);
                            }
                            System.out.println("End thunk! " + status.get() +  " thread " +  Thread.currentThread().getId() + " " + advancing.get());


                         };
                        thunk[0].run();



                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,t->{
                onError.accept(t);
                res.requested.decrementAndGet();
                 if(res.isActive()){
                     s[0].request(1);
                 }
                },()->{


            System.out.println("On complete...");
                    int statusLocal = -1;
                   do {
                        statusLocal = status.get();
                       System.out.println("On complete Setting to " + (statusLocal | (1 << 0)));

                   }while(!status.compareAndSet(statusLocal,statusLocal | (1 << 0)));
                    System.out.println("Status is " + status.get());
                   if(status.compareAndSet(1,100)){
                       System.out.println("Running oncomplete!");
                       onComplete.run();
                   }

                });

        return res;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        source.subscribeAll(e-> {
                    try {
                        mapper.apply(e).forEach(onNext);

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onCompleteDs);
    }
}
