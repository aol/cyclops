package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class IterableFlatMapOperator<T,R> extends BaseOperator<T,R> {


    final Function<? super T, ? extends Iterable<? extends R>> mapper;;

    public IterableFlatMapOperator(Operator<T> source, Function<? super T, ? extends Iterable<? extends R>> mapper){
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
                super.request(n-1);
            }

            @Override
            public void cancel() {
                s[0].cancel();
                super.cancel();
            }
        };
        s[0] = source.subscribe(e-> {
                    try {
                        while(s[0].isActive()) {
                            Spliterator<? extends R> split = mapper.apply(e).spliterator();
                            boolean canAdvance = true;
                            while (s[0].isActive() && canAdvance) {
                                res.requested.decrementAndGet();
                                canAdvance = split.tryAdvance(onNext);
                            }
                        }

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);

        return s[0];
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
