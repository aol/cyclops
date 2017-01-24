package com.aol.cyclops2.internal.stream.spliterators.push;

import org.reactivestreams.Subscription;

import java.util.Spliterator;
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
        Runnable[] thunk= {()->{
            if(completeRecieved[0]){
                onComplete.run();
            }else{
                s[0].request(1);
            }
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

                        thunk[0] = () -> {

                                boolean canAdvance = false;
                                while (res.isActive()) {
                                    try {
                                        canAdvance = split.tryAdvance(onNext);
                                    }catch(Throwable t){
                                        onError.accept(t);
                                    }
                                    if(canAdvance)
                                        res.requested.decrementAndGet();
                                    else {
                                        if(completeRecieved[0])
                                            onComplete.run();
                                        break;
                                    }


                                }
                            if(!canAdvance && res.isActive())
                                s[0].request(1);


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
                    completeRecieved[0]=true;
                    thunk[0].run();

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
