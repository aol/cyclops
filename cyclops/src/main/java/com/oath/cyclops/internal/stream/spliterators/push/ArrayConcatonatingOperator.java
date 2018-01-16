package com.oath.cyclops.internal.stream.spliterators.push;

import cyclops.data.BankersQueue;
import cyclops.data.Seq;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class ArrayConcatonatingOperator<IN> implements Operator<IN> {


    private final Seq<Operator<IN>> operators;


    public ArrayConcatonatingOperator(Operator<IN>... sources){
        Seq<Operator<IN>> ops  = Seq.empty();
        for(Operator<IN> next : sources){
            ops = ops.append(next);
        }
        this.operators = ops;


    }

    public ArrayConcatonatingOperator(Seq<Operator<IN>> sources){
        this.operators = sources;
    }




    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        BankersQueue<StreamSubscription> subs = BankersQueue.empty();
        int index[] = {0};
        boolean[] finished = {false};


        Concat[] ref = {null};
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

        Concat c = new Concat(sub,operators,onNext,onError,onComplete);
        ref[0]=c;






        return sub;
    }





    @Override
    public void subscribeAll(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        subscribeAll(0,onNext,onError,onCompleteDs);



    }
    public void subscribeAll(int index,Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        if(index>=operators.size()) {
            onCompleteDs.run();
            return;
        }


        Operator<IN> next = operators.getOrElse(index,null);
        next.subscribeAll(onNext,onError,()->subscribeAll(index+1,onNext,onError,onCompleteDs));



    }
}
