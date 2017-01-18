package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.collections.DequeX;
import cyclops.collections.QueueX;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class ArrayConcatonatingOperator<IN> implements Operator<IN> {


    private final DequeX<Operator<IN>> operators;


    public ArrayConcatonatingOperator(Operator<IN>... sources){
        this.operators = DequeX.empty();
        for(Operator<IN> next : sources){
            operators.add(next);
        }


    }

    private void subscribe(int index, boolean[] completed,Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs){

        operators.get(index).get().subscribeAll(e-> {
                    try {
                        onNext.accept(e);
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                        if(index<operators.size()-1)
                          subscribe(index+1,completed,onNext,onError,onCompleteDs);
                        if(!completed[0])
                            onCompleteDs.run();
                });
    }

    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        QueueX<StreamSubscription> subs = QueueX.empty();
        int index[] = {0};
        boolean[] finished = {false};
        StreamSubscription[] active = {null};
        BooleanSupplier[] ensureOpenRef = {null};
        StreamSubscription sub = new StreamSubscription() {
            {
                ensureOpenRef[0] =this::ensureOpen;
            }
            boolean ensureOpen(){
                if(!isOpen)
                    return false;
                while(!active[0].isOpen ){
                    if(subs.size()==0)
                        return false;
                    active[0] = subs.poll();

                }
                return active[0].isOpen;
            }
            @Override
            public void request(long n) {

                if (ensureOpen()) {
                    if(n<=0)
                        onError.accept(new IllegalArgumentException( "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    super.request(n);
                    active[0].request(1);
                }



            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };

        for(Operator<IN> next : operators){
            subs.add(next.subscribe(e-> {

                        try {
                            onNext.accept(e);
                        } catch (Throwable t) {

                            onError.accept(t);
                        }finally{

                            handleSubCompletion(ensureOpenRef[0],sub.requested.decrementAndGet(),onComplete, subs, finished, active, sub);
                        }
                    }
                    ,t->{
                        onError.accept(t);
                        handleSubCompletion(ensureOpenRef[0],sub.requested.decrementAndGet(),onComplete, subs, finished, active, sub);
                    },()->{

                        active[0].cancel();
                        handleSubCompletion(ensureOpenRef[0],sub.requested.get(),onComplete, subs, finished, active, sub);
                    }));
        }
        if(subs.size()>0)
            active[0]= subs.poll();

        return sub;
    }

    private void handleSubCompletion(BooleanSupplier ensureOpen,long remaining, Runnable onComplete, QueueX<StreamSubscription> subs, boolean[] finished, StreamSubscription[] active, StreamSubscription sub) {
        if(sub.isOpen) {
            System.out.println("Remaining is " + remaining);
            if(remaining>0 && ensureOpen.getAsBoolean()){

                active[0].request(1l);
            }

        }
        if(subs.size()==0 && !ensureOpen.getAsBoolean()){
            if(!finished[0]) {
                finished[0] = true;
                onComplete.run();
            }
        }
    }

    @Override
    public void subscribeAll(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

       subscribe(0,new boolean[]{false},onNext,onError,onCompleteDs);
    }
}
