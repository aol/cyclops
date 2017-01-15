package com.aol.cyclops2.internal.stream.spliterators.push;

import org.reactivestreams.Subscription;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
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
        StreamSubscription res = new StreamSubscription(){
            @Override
            public void request(long n) {
                s[0].request(1);
                super.request(n);
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
                            boolean canAdvance = true;
                            while (res.isActive() && canAdvance) {
                                res.requested.decrementAndGet();
                                canAdvance = split.tryAdvance(onNext);
                            }


                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);

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
