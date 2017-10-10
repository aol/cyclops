package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LimitWhileClosedOperator<T,R> extends BaseOperator<T,T> {


    private final Predicate<? super T> predicate;

    public LimitWhileClosedOperator(Operator<T> source, final Predicate<? super T> predicate){
        super(source);
        this.predicate = predicate;



    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        boolean closed[] = {false};
        StreamSubscription sub[] = {null};
        sub[0] = source.subscribe(e-> {
                    try {
                        if(!closed[0])
                         onNext.accept(e);
                        if(closed[0]){
                            sub[0].cancel();
                            onComplete.run();
                        }
                        if(!predicate.test(e)){
                            closed[0]=true;
                        }


                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);
        return sub[0];
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        boolean[] complete = {false};
        boolean closed[] = {false};
        StreamSubscription sub[] = {null};
        source.subscribeAll(e-> {
                    try {
                        if(!closed[0] && !complete[0])
                            onNext.accept(e);
                        if(closed[0] && !complete[0]){

                            onCompleteDs.run();
                            complete[0]=true;
                        }
                        if(!predicate.test(e)){
                            closed[0]=true;
                        }


                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    if(!complete[0]) {
                        complete[0] = true;
                        onCompleteDs.run();
                    }
                });

    }
}
