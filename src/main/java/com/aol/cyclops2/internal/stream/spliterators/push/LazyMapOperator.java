package com.aol.cyclops2.internal.stream.spliterators.push;

import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LazyMapOperator<T,R> implements Operator<R> {

    final Operator<T> source;
    Supplier<Tuple2<Function<? super T, ? extends R>, Consumer<Consumer<? super R >>>> events;

    public LazyMapOperator(Operator<T> source, Supplier<Tuple2<Function<? super T, ? extends R>, Consumer<Consumer<? super R >>>> events){
        this.source = source;
        this.events = events;


    }


    @Override
    public void subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Tuple2<Function<? super T, ? extends R>, Consumer<Consumer<? super R >>> local = events.get();
        Function<? super T, ? extends R> mapper = local.v1;
        Consumer<Consumer<? super R >> onComplete = local.v2;
        source.subscribe(e-> {
                    try {
                        onNext.accept(mapper.apply(e));
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    onComplete.accept(onNext);
                    onCompleteDs.run();
                });
    }
}
