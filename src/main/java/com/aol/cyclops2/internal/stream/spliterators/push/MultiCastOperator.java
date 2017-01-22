package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.collections.ListX;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class MultiCastOperator<T> extends BaseOperator<T,T> {




    public MultiCastOperator(Operator<T> source){
        super(source);


    }

    ListX<Consumer<? super T>> registeredOnNext = ListX.empty();
    ListX<Consumer<? super Throwable>> registeredOnError= ListX.empty();
    ListX<Runnable> registeredOnComplete= ListX.empty();
    boolean registered = false;
    StreamSubscription result;

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        registeredOnNext.add(onNext);
        registeredOnError.add(onError);
        registeredOnComplete.add(onComplete);
        if(!registered) {
            registered = true;
            result = source.subscribe(e -> {

                        registeredOnNext.forEach(n->n.accept(e));

                    }
                    , e->registeredOnError.forEach(t->t.accept(e)), ()->registeredOnComplete.forEach(n->n.run()));
        }
        return result;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        source.subscribeAll(e -> {

                    registeredOnNext.forEach(n->n.accept(e));

                }
                , e->registeredOnError.forEach(t->t.accept(e)), ()->registeredOnComplete.forEach(n->n.run()));
    }
}
