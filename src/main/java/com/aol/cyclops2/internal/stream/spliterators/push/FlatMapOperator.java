package com.aol.cyclops2.internal.stream.spliterators.push;

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
    public void subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        source.subscribe(e-> {
                    try {
                        mapper.apply(e).forEach(onNext);

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onCompleteDs);
    }
}
