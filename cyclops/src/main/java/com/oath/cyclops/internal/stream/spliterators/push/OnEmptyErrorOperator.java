package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class OnEmptyErrorOperator<T, X extends Throwable> extends BaseOperator<T,T> {


    Supplier<? extends X> value;

    public OnEmptyErrorOperator(Operator<T> source, Supplier<? extends X> value){
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
                        onError.accept(value.get());
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
                            onError.accept(value.get());
                        onCompleteDs.run();
                });
    }
}
