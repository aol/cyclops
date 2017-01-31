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
        ManyToOneConcurrentArrayQueue<IN> data = new ManyToOneConcurrentArrayQueue<IN>(1024 * operators.length);
        List<StreamSubscription> subs = new ArrayList<>(operators.length);
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{
                while(requested.get()>0)
                {
                    if (completed.get() == subs.size() && data.size() == 0) {
                        System.out.println("Completing.. " + data.size());

                        System.out.println("Completing.. " + data.size() );
                        onComplete.run();
                        return;
                    }
                    System.out.println("*****!!!!!!!!!!!!!***************    n is " + n + " looping " + Math.min(n, subs.size()));
                    long sent = 0;
                    long reqCycle = requested.get();
                    for (long k = 0; k < reqCycle; k++) {
                        System.out.println("K is " + k);
                        if (!isOpen)
                            return;
                        int toUse = index.incrementAndGet() - 1;
                        if (toUse + 1 >= subs.size()) {
                            index.set(0);

                        }

                        if (subs.get(toUse).isOpen) {

                            System.out.println("Booked " + subs.get(toUse) + "  demand " + requested.get());
                            subs.get(toUse).request(1l);

                        } else
                            k--;

                        IN fromQ = nilsafeOut(data.poll());
                        if (fromQ != null) {

                            onNext.accept(fromQ);
                            requested.decrementAndGet();
                            sent++;
                        }
                        if (completed.get() == subs.size() && data.size() == 0) {
                            System.out.println("Completing.. " + data.size() +  " sent " + sent);

                            System.out.println("Completing.. " + data.size() +  " sent " + sent);
                            onComplete.run();
                            return;
                        }



                    }

                    while (sent < reqCycle && isOpen && !(completed.get() == subs.size() && data.size() == 0)) {

                        IN fromQ = nilsafeOut(data.poll());
                        if (fromQ != null) {
                            onNext.accept(fromQ);
                            requested.decrementAndGet();
                            sent++;
                        }
                        System.out.println("Sent is " + sent + " data "
                                + data.size() + " isOpen " + isOpen
                                + " completed ? " + (completed.get() == subs.size())
                                + " data " + data.size());
                    }
                    if (completed.get() == subs.size() && data.size() == 0) {
                        System.out.println("Completing.. " + data.size() +  " sent " + sent);
                        onComplete.run();
                        return;

                    }
                    System.out.println("End request.. sent " + sent + "  " + completed.get() + " " + data.size());
                }


            };
            @Override
            public void request(long n) {
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
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

                            while(!data.offer((IN)nilsafeIn(e))){

                            }

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
        ManyToOneConcurrentArrayQueue<IN> data = new ManyToOneConcurrentArrayQueue<IN>(1024 * operators.length);
        AtomicBoolean wip= new AtomicBoolean(false);


        for(int i=0;i<operators.length;i++){
            int current = i;
            operators[current].subscribeAll(e-> {
                        try {
                            if(wip.compareAndSet(false,true)) {
                                onNext.accept(e);
                                System.out.println("Merging! " + e);
                                data.drain(x->onNext.accept(x));
                                wip.set(false);
                            }else{
                                data.offer(e);
                            }

                        } catch (Throwable t) {

                            onError.accept(t);
                        }finally{

                        }
                    }
                    ,onError,()->{

                        if(completed.incrementAndGet()== operators.length){
                            System.out.println("Running on complete");
                            data.drain(x->onNext.accept(x));
                            onCompleteDs.run();

                        }
                        System.out.println("Complete " + completed.get());

                    });
        }


    }
}
