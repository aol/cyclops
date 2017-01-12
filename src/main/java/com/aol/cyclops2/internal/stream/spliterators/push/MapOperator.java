package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.types.mixins.AsMappable;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class MapOperator<T,R> extends BaseOperator<T,R> {


    Function<? super T, ? extends R> mapper;

    public MapOperator(Operator<T> source, Function<? super T, ? extends R> mapper){
        super(source);
        this.mapper = mapper;
        this.mapper = mapper;



    }


    public <R1> MapOperator<T,R1> compose(Function<? super R, ? extends R1> fn){
        return new MapOperator<T, R1>(source,mapper.andThen(fn));

    }

    @Override
    public void subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        source.subscribe(e-> {
                    try {
                        onNext.accept(mapper.apply(e));
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onCompleteDs);
    }
}
