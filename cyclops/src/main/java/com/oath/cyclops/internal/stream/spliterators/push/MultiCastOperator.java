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

        registeredOnNext.add(onNext);
        registeredOnError.add(onError);
        registeredOnComplete.add(onComplete);
        StreamSubscription result = new StreamSubscription(){

        };
        /**
        (source.forEachAsync(pendingRequests -> {
                        for(int i=0;i<subs.size();i++){
                            if(subs.getValue(i).isActive())
                                registeredOnNext.getValue(i).accept(pendingRequests);
                        }


                    }
                    , pendingRequests->registeredOnError.forEach(t->t.accept(pendingRequests)), ()->registeredOnComplete.forEach(n->n.run())));
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
