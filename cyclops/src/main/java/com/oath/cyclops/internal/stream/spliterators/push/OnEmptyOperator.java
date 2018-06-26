package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class OnEmptyOperator<T> extends BaseOperator<T,T> {


    Supplier<? extends T> value;

    public OnEmptyOperator(Operator<T> source, Supplier<? extends T> value){
        super(source);
        this.value = value;


    }



    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        boolean[] data ={ false};
        return source.subscribe(e->{
                if(!data[0])
                    data[0]=true;
                onNext.accept(e);
            }
            ,onError,()->{
                if(data[0]==false)
                    onNext.accept(value.get());
                onComplete.run();
            });
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        boolean[] data ={ false};
        source.subscribeAll(e->{
                    if(!data[0])
                     data[0]=true;
                    onNext.accept(e);
                }
                ,onError,()->{
                        if(data[0]==false)
                            onNext.accept(value.get());
                        onCompleteDs.run();
                });
    }
}
