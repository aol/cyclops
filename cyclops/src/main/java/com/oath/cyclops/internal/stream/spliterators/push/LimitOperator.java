package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LimitOperator<T,R> extends BaseOperator<T,T> {


    long limit;

    public LimitOperator(Operator<T> source,long limit){
        super(source);
        this.limit = limit;



    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        long[] count = {0};
        StreamSubscription sub[] = {null};

        boolean completed[] = {false};
        sub[0] = source.subscribe(e-> {

                    try {

                        if(count[0]++<limit)
                            onNext.accept(e);
                        else{
                            sub[0].cancel();
                            if(!completed[0]) {
                                completed[0]=true;
                                onComplete.run();
                            }
                        }
                    } catch (Throwable t) {
                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    if(!completed[0]) {
                        completed[0]=true;
                        onComplete.run();
                    }
                });
        return sub[0];
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        long[] count = {0};
        StreamSubscription sub[] = {null};

        boolean completed[] = {false};
        source.subscribeAll(e-> {

                    try {

                        if(count[0]++<limit)
                            onNext.accept(e);
                        else{

                            if(!completed[0]) {
                                completed[0]=true;
                                onCompleteDs.run();
                            }
                        }
                    } catch (Throwable t) {
                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    if(!completed[0]) {
                        completed[0]=true;
                        onCompleteDs.run();
                    }
                });
    }
}
