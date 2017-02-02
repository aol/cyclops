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

        AtomicInteger status = new AtomicInteger(0); //1st bit for completing, 2 bit for inner active, 100 for complete

        BooleanSupplier[] thunk= {()->{

            s[0].request(1);
            return true;

        }};


        StreamSubscription res = new StreamSubscription(){
            LongConsumer work = n-> {

                thunk[0].getAsBoolean();

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

                        Spliterator<? extends R> split = mapper.apply(e).spliterator();

                        int statusLocal =-1;
                        do {
                            statusLocal = status.get();


                        }while(!status.compareAndSet(statusLocal,statusLocal | (1 << 1))); //set inner active

                        AtomicInteger advancing = new AtomicInteger(0);
                        thunk[0] = () -> {
                            while (res.isActive()) { //outer loop to capture missed demand
                                boolean canAdvance = false;

                                if (!advancing.compareAndSet(0, 1)) {

                                    return false;
                                }
                                try {
                                    while (res.isActive()) {
                                        try {
                                            canAdvance = split.tryAdvance(onNext);

                                        } catch (Throwable t) {
                                            onError.accept(t);
                                        }

                                        if (canAdvance) {
                                            res.requested.decrementAndGet();

                                        } else {

                                            int thunkStatusLocal = -1;
                                            do {
                                                thunkStatusLocal = status.get();


                                            }
                                            while (!status.compareAndSet(thunkStatusLocal, thunkStatusLocal & ~(1 << 1))); //unset inner active

                                            if (status.compareAndSet(1, 100)) {
                                                System.out.println("Completing in thunk!  demand " + res.requested.get() + " thread " + Thread.currentThread().getId());
                                                onComplete.run();
                                                return true;
                                            }
                                            break;
                                        }


                                    }
                                } finally {
                                    advancing.set(0);
                                }
                                if (!canAdvance && res.isActive() && !(status.get() >= 100)) {
                                    s[0].request(1);
                                    return true;
                                }else if(!canAdvance){
                                    return true;
                                }
                            }
                            return true;

                         };
                        thunk[0].getAsBoolean();



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
                    int statusLocal = -1;
                   do {
                        statusLocal = status.get();


                   }while(!status.compareAndSet(statusLocal,statusLocal | (1 << 0)));

                   if(status.compareAndSet(1,100)){
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
