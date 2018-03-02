package com.oath.cyclops.internal.stream.spliterators.push;

import cyclops.data.Seq;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class MultiCastOperator<T> extends BaseOperator<T,T> {




    public MultiCastOperator(Operator<T> source,int expect){
        super(source);
        this.expect =expect;


    }

    final int expect;

    Seq<Consumer<? super T>> registeredOnNext = Seq.empty();
    Seq<Consumer<? super Throwable>> registeredOnError= Seq.empty();
    Seq<Runnable> registeredOnComplete= Seq.empty();
    Seq<StreamSubscription> subs = Seq.empty();

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        registeredOnNext= registeredOnNext.plus(onNext);
        registeredOnError=registeredOnError.plus(onError);
        registeredOnComplete =registeredOnComplete.plus(onComplete);
        StreamSubscription result = new StreamSubscription(){

        };

        return result;

    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        registeredOnNext= registeredOnNext.plus(onNext);
        registeredOnError = registeredOnError.plus(onError);
        registeredOnComplete=registeredOnComplete.plus(onCompleteDs);

            source.subscribeAll(e -> {

                        registeredOnNext.forEach(n -> n.accept(e));

                    }
                    , e -> registeredOnError.forEach(t -> t.accept(e)), () -> registeredOnComplete.forEach(n -> n.run()));

    }
}
