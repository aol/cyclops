package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class SkipWhileClosedOperator<T,R> extends BaseOperator<T,T> {


    final Predicate<? super T> predicate;

    public SkipWhileClosedOperator(Operator<T> source, final Predicate<? super T> predicate){
        super(source);
        this.predicate = predicate;



    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        boolean[] skipping = {true};
        boolean[] resetSkipping = {false};
        boolean[] first = {true};
        StreamSubscription sub[] = {null};
        StreamSubscription res = new StreamSubscription(){
            @Override
            public void request(long n) {
                super.request(n);
                sub[0].request(n);
            }

            @Override
            public void cancel() {
                sub[0].cancel();
                super.cancel();
            }
        };
        sub[0] = source.subscribe(e-> {
               try {

                        if(skipping[0]){
                            if(!predicate.test(e)){
                                skipping[0] = false;

                            }
                            sub[0].request(1l);
                        }
                        else {

                            onNext.accept(e);
                        }

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);
        return res;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        boolean[] skipping = {true};
        boolean[] first = {true};
        source.subscribeAll(e->{
            try {

                if (skipping[0]) {
                    if (!predicate.test(e)) {
                        skipping[0] = false;

                    }

                } else {
                    onNext.accept(e);
                }
            }catch(Throwable t){
                onError.accept(t);
            }
        },onError,onCompleteDs);

    }
}
