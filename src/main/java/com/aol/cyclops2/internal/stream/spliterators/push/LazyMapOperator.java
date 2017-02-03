package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LazyMapOperator<T,R> extends BaseOperator<T,R> {


    final Supplier<Function<? super T, ? extends R>> mapperSupplier;

    public LazyMapOperator(Operator<T> source, Supplier<Function<? super T, ? extends R>> mapperSupplier){
        super(source);
        this.mapperSupplier = mapperSupplier;

    }




    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        Function<? super T, ? extends R> mapper = mapperSupplier.get();
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
        Function<? super T, ? extends R> mapper = mapperSupplier.get();
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
