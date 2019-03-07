package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class OnErrorBreakOperator<T> extends BaseOperator<T,T> {



    final Function<? super Throwable,? extends T> recover;

    public OnErrorBreakOperator(Operator<T> source, Function<Throwable,? extends T> recover){
        super(source);

        this.recover = recover;


    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        StreamSubscription[] upstream = {null};
        upstream[0] = source.subscribe(e-> {
                    try {
                        onNext.accept(e);
                    } catch (Throwable t) {

                        try{

                            onNext.accept(recover.apply(t));

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
                        onNext.accept(recover.apply(e));
                    } catch (Throwable t) {
                        onError.accept(t);
                    }
                    upstream[0].cancel();
                    onComplete.run();

                },onComplete);
        return upstream[0];
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        subscribe(onNext,onError,onCompleteDs).request(Long.MAX_VALUE);
    }
}
