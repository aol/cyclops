package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class IterableFlatMapOperator<T,R> extends BaseOperator<T,R> {


    final Function<? super T, ? extends Iterable<? extends R>> mapper;

    public IterableFlatMapOperator(Operator<T> source, Function<? super T, ? extends Iterable<? extends R>> mapper){
        super(source);
        this.mapper = mapper;




    }


    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        StreamSubscription[] s = {null} ;

        AtomicInteger status = new AtomicInteger(0); //1st bit for completing, 2 bit for inner active, 100 for complete

        Runnable[] thunk= {()->{

            s[0].request(1);

        }};


        StreamSubscription res = new StreamSubscription(){
            LongConsumer work = n-> {

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

                        Spliterator<? extends R> split = mapper.apply(e).spliterator();

                        int statusLocal =-1;
                        do {
                            statusLocal = status.get();


                        }while(!status.compareAndSet(statusLocal,statusLocal | (1 << 1))); //set inner active

                        AtomicInteger advancing = new AtomicInteger(0);
                        thunk[0] = () -> {

                            boolean canAdvance = false;

                            if (!advancing.compareAndSet(0, 1)) {

                                return;
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
                                            onComplete.run();
                                            return;
                                        }
                                        break;
                                    }




                                }
                            }finally{
                                advancing.set(0);
                            }
                            if(!canAdvance && res.isActive() && !(status.get()>=100)) {


                                s[0].request(1);
                            }



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
