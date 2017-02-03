package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class RecoverOperator<T> extends BaseOperator<T,T> {



    final Function<? super Throwable,? extends T> recover;

    public RecoverOperator(Operator<T> source, Function<? super Throwable,? extends T> recover){
        super(source);

        this.recover = recover;


    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        return source.subscribe(e-> {
                    try {
                        onNext.accept(e);
                    } catch (Throwable t) {
                        try{
                            onNext.accept(recover.apply(t));
                        }catch(Throwable t2) {
                            onError.accept(t2);
                        }
                    }
                }
                ,e->{
                    try{
                        onNext.accept(recover.apply(e));
                    } catch (Throwable t) {
                        onError.accept(t);
                    }

                },onComplete);
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        source.subscribeAll(e-> {
                    try {
                        onNext.accept(e);
                    } catch (Throwable t) {
                        try{
                            onNext.accept(recover.apply(t));
                        }catch(Throwable t2) {
                            onError.accept(t2);
                        }
                    }
                }
                ,e->{
                    try{
                        onNext.accept(recover.apply(e));
                    } catch (Throwable t) {
                        onError.accept(t);
                    }

                },onCompleteDs);
    }
}
