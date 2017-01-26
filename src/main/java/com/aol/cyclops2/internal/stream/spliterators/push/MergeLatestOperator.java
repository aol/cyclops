package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.async.Queue;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class MergeLatestOperator<IN> implements Operator<IN> {


    private final Operator<IN>[] operators;


    public MergeLatestOperator(Operator<IN>[] sources){
        this.operators=sources;


    }

    private Object nilsafeIn(Object o){
        if(o==null)
            return Queue.NILL;
        return o;
    }
    private <T> T nilsafeOut(Object o){
        if(Queue.NILL==o){
            return null;
        }
        return (T)o;
    }

    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        ManyToOneConcurrentArrayQueue<IN> data = new ManyToOneConcurrentArrayQueue<IN>(256);
        List<StreamSubscription> subs = new ArrayList<>(operators.length);
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{
                System.out.println("*****!!!!!!!!!!!!!***************    n is "+ n + " looping " + Math.min(n,subs.size()));
                long sent = 0;
                for(long k=0;k<requested.get();k++) {
                    System.out.println("K is " +k);
                    if(!isActive())
                        break;
                    int toUse = index.incrementAndGet() - 1;
                    if (toUse+1 >= subs.size()) {
                        index.set(0);

                    }

                    if(isActive() && subs.get(toUse).isOpen) {

                        System.out.println("Booked " + subs.get(toUse) + "  demand " + requested.get());
                        subs.get(toUse).request(1l);

                    }
                    else
                        k--;

                    IN fromQ = nilsafeOut(data.poll());
                    if(fromQ!=null){
                        onNext.accept(fromQ);
                        requested.decrementAndGet();
                        sent++;
                    }
                    if(completed.get()==subs.size() && data.size()==0){
                        onComplete.run();
                        return;
                    }



                }
                while(isActive() && !(completed.get()==subs.size() && data.size()==0)){
                    IN fromQ = nilsafeOut(data.poll());
                    if(fromQ!=null){
                        onNext.accept(fromQ);
                        requested.decrementAndGet();
                        sent++;
                    }
                    System.out.println("Sent is " + sent + " data " + data.size());
                }
                if(completed.get()==subs.size()&& data.size()==0){
                    onComplete.run();

                }
                System.out.println("End request.. sent " + sent + "  " + completed.get() + " " + data.size());

            };
            @Override
            public void request(long n) {
                System.out.println("Request!! n is "+ n);
                super.singleActiveRequest(n,work);

            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };

        for(int i=0;i<operators.length;i++){
            int current = i;
            subs.add(operators[current].subscribe(e-> {
                        try {

                            data.offer((IN)nilsafeIn(e));

                            System.out.println("Queueing! " + e + " on " + current  + "  demand " + sub.requested.get());



                            System.out.println("decrement demand " + sub.requested.get());
                        } catch (Throwable t) {

                            onError.accept(t);
                        }finally{


                        }
                    }
                    ,onError,()->{

                        completed.incrementAndGet();
                        System.out.println("Completed so far " + completed.get());

                    }));

        }


        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        List<StreamSubscription> subs = new ArrayList<>(operators.length);
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);




        for(int i=0;i<operators.length;i++){
            int current = i;
            operators[current].subscribeAll(e-> {
                        try {
                            onNext.accept(e);
                            System.out.println("Merging! " + e);

                        } catch (Throwable t) {

                            onError.accept(t);
                        }finally{

                        }
                    }
                    ,onError,()->{

                        if(completed.incrementAndGet()== operators.length){
                            System.out.println("Running on complete");
                            onCompleteDs.run();

                        }
                        System.out.println("Complete " + completed.get());

                    });
        }


    }
}
