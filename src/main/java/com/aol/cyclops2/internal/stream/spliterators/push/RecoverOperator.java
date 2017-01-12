package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class RecoverOperator<T,R> extends BaseOperator<T,R> {


    final Function<? super T, ? extends R> mapper;
    final Function<? super Throwable,? extends R> recover;

    public RecoverOperator(Operator<T> source, Function<? super T, ? extends R> fn, Function<? super Throwable,? extends R> recover){
        super(source);
        this.mapper = fn;
        this.recover = recover;


    }



    @Override
    public void subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        source.subscribe(e-> {
                    try {
                        onNext.accept(mapper.apply(e));
                    } catch (Throwable t) {
                        try{
                            onNext.accept(recover.apply(t));
                        }catch(Throwable t2) {
                            onError.accept(t2);
                        }
                    }
                }
                ,onError,onCompleteDs);
    }
}
