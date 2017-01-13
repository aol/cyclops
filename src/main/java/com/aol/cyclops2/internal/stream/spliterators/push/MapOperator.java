package com.aol.cyclops2.internal.stream.spliterators.push;

import org.reactivestreams.Subscription;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class MapOperator<T,R> extends BaseOperator<T,R> {


    final Function<? super T, ? extends R> mapper;

    public MapOperator(Operator<T> source, Function<? super T, ? extends R> mapper){
        super(source);
        this.mapper = mapper;

    }


    public <R1> MapOperator<T,R1> compose(Function<? super R, ? extends R1> fn){
        return new MapOperator<T, R1>(source,mapper.andThen(fn));

    }

    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        return source.subscribe(e-> {
                    try {
                        onNext.accept(mapper.apply(e));
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        source.subscribeAll(e-> {
                    try {
                        onNext.accept(mapper.apply(e));
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onCompleteDs);
    }
}
