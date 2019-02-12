package com.oath.cyclops.internal.stream.spliterators.push;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class OnErrorBreakWithPublisherOperator<T> extends BaseOperator<T, Publisher<? extends T>> {



    final Function<Throwable,? extends Publisher<? extends T>> recover;

    public OnErrorBreakWithPublisherOperator(Operator<T> source, Function<Throwable,? extends Publisher<? extends T>> recover){
        super(source);

        this.recover = recover;


    }


    @Override
    public StreamSubscription subscribe(Consumer<? super Publisher<? extends T>> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        StreamSubscription[] upstream = {null};
        upstream[0] = source.subscribe(e-> {
                    try {
                        onNext.accept(Spouts.of(e));
                    } catch (Throwable t) {

                        try{
                            ReactiveSeq<T> rs = Spouts.from(recover.apply(t));
                            onNext.accept(rs.recoverWith(recover));

                        }catch(Throwable t2) {
                            onError.accept(t2);
                        }finally{
                             upstream[0].cancel();
                             onComplete.run();
                        }
                    }
                }
                ,e->{

                    try{
                        ReactiveSeq<T> rs = Spouts.from(recover.apply(e));
                        onNext.accept(rs.recoverWith(recover));

                    } catch (Throwable t) {
                        onError.accept(t);
                    }
                    upstream[0].cancel();
                    onComplete.run();

                },onComplete);
        return upstream[0];
    }

    @Override
    public void subscribeAll(Consumer<? super Publisher<? extends T>> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        subscribe(onNext,onError,onCompleteDs).request(Long.MAX_VALUE);
    }
}
