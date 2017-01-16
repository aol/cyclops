package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

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

        Runnable[] thunk= {()->s[0].request(1)};
        boolean[] completeRecieved = {false};
        StreamSubscription res = new StreamSubscription(){
            @Override
            public void request(long n) {
                super.request(n);
                thunk[0].run();
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
                                canAdvance = split.tryAdvance(onNext);
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
                ,onError,()->{
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
