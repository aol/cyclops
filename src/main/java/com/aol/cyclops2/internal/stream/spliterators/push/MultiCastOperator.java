package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.collections.ListX;
import org.reactivestreams.Subscription;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class MultiCastOperator<T> extends BaseOperator<T,T> {




    public MultiCastOperator(Operator<T> source,int expect){
        super(source);
        this.expect =expect;


    }

    final int expect;

    ListX<Consumer<? super T>> registeredOnNext = ListX.empty();
    ListX<Consumer<? super Throwable>> registeredOnError= ListX.empty();
    ListX<Runnable> registeredOnComplete= ListX.empty();
    ListX<StreamSubscription> subs = ListX.empty();

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        registeredOnNext.add(onNext);
        registeredOnError.add(onError);
        registeredOnComplete.add(onComplete);
        StreamSubscription result = new StreamSubscription(){

        };
        /**
        (source.subscribe(e -> {
                        for(int i=0;i<subs.size();i++){
                            if(subs.get(i).isActive())
                                registeredOnNext.get(i).accept(e);
                        }


                    }
                    , e->registeredOnError.forEach(t->t.accept(e)), ()->registeredOnComplete.forEach(n->n.run())));
        **/
        return result;

    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        registeredOnNext.add(onNext);
        registeredOnError.add(onError);
        registeredOnComplete.add(onCompleteDs);

            source.subscribeAll(e -> {

                        registeredOnNext.forEach(n -> n.accept(e));

                    }
                    , e -> registeredOnError.forEach(t -> t.accept(e)), () -> registeredOnComplete.forEach(n -> n.run()));

    }
}
