package com.oath.cyclops.internal.stream.spliterators.push;

import cyclops.data.Seq;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LazyArrayConcatonatingOperator<IN> implements Operator<IN> {


    private final Seq<Operator<IN>> operators;


    public LazyArrayConcatonatingOperator(Operator<IN>... sources){
        Seq<Operator<IN>> ops  = Seq.empty();
        for(Operator<IN> next : sources){
            ops = ops.append(next);
        }
        this.operators = ops;

    }
    public LazyArrayConcatonatingOperator(Seq<Operator<IN>> sources){
        this.operators = sources;


    }



    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        LazyConcat[] ref = {null};
        StreamSubscription sub = new StreamSubscription() {

            @Override
            public void request(long n) {

                if (n <= 0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }


                super.request(n);

                ref[0].request(n);





            }

            @Override
            public void cancel() {
                ref[0].cancel();
                super.cancel();

            }
        };

        LazyConcat c = new LazyConcat(sub,operators,onNext,onError,onComplete);
        ref[0]=c;






        return sub;
    }





    @Override
    public void subscribeAll(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        subscribe(onNext,onError,onCompleteDs).request(Long.MAX_VALUE);



    }

}
