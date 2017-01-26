package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.async.Queue;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class MergeLatestOperatorAsync<IN> implements Operator<IN> {


    private final Operator<IN>[] operators;


    public MergeLatestOperatorAsync(Operator<IN>[] sources){
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
    AtomicBoolean wip = new AtomicBoolean(false);

    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        ManyToManyConcurrentArrayQueue<IN> data = new ManyToManyConcurrentArrayQueue<IN>(256);
        List<StreamSubscription> subs = new ArrayList<>(operators.length);
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);
        Consumer[] drainQ = {b->{}};
        StreamSubscription sub = new StreamSubscription(){
            {
                Consumer<Boolean> c = this::drainQueue;
                drainQ[0] =c;
            }
            AtomicLong sent = new AtomicLong(0);
            AtomicLong rN = new AtomicLong(0);
            LongConsumer work = n->{
                System.out.println("*****!!!!!!!!!!!!!***************    n is "+ n + " looping " + Math.min(n,subs.size()));
              //  long sent = 0;

                for(long k=0;k<n;k++) {
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
                    System.out.println("From Q "  + fromQ +  " " + data.size());
                    if(fromQ!=null){
                        onNext.accept(fromQ);
                        requested.decrementAndGet();
                        sent.incrementAndGet();
                    }
                    if(completed.get()==subs.size() && data.isEmpty()){
                        onComplete.run();
                        return;
                    }



                }
                System.out.println("Main drainQ");
                drainQueue(true);

            };

            private void drainQueue(boolean wait) {
                wait=true;
                BooleanSupplier b = wait ? ()-> sent.get() < rN.get() && !(completed.get()==subs.size() && data.isEmpty()) : ()->!data.isEmpty();
                System.out.println("Drain Queue " + Thread.currentThread().getId());
                boolean loop = false;
               do{
                    if (wip.compareAndSet(false, true)) {
                        loop= false;
                        try {
                            System.out.println("Active drain queue " + +Thread.currentThread().getId());
                            System.out.println(" Sent " + sent + " demanded " + rN.get() + " data present?" + !data.isEmpty() + " active " + isActive() + " " + b.getAsBoolean());
                            while (isOpen && b.getAsBoolean()) {
                                // System.out.println("Data " + data.size() + " demand " + requested.get());

                                    IN fromQ = nilsafeOut(data.poll());
                                    if (fromQ != null) {
                                        System.out.println("sending " + fromQ);
                                        onNext.accept(fromQ);
                                        requested.decrementAndGet();
                                        sent.incrementAndGet();
                                        System.out.println("Sent! " + data.isEmpty());
                                    }


                                //System.out.println("loop ");
                                //System.out.println("Sent is " + sent);
                            }
                            if (completed.get() == subs.size() && data.isEmpty()) {
                                onComplete.run();

                            }
                            System.out.println("End Drain Q.. sent " + sent.get() + " " + completed.get() + " " + data.isEmpty() + " Thread " + Thread.currentThread().getId());
                        } finally {
                            wip.set(false);
                        }

                    } else {
                        if (wait) {
                            loop = true;
                        }
                    }
                }while(loop);

            }


            @Override
            public void request(long n) {
                rN.accumulateAndGet(n,(a,b)->a+b);
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
                            IN in = (IN)nilsafeIn(e);
                            System.out.println("Queueing! " + in + " on " + current  + "  demand " + sub.requested.get());
                            data.offer(in);


                            System.out.println("On next drainQ ");
                            drainQ[0].accept(false);

                            System.out.println("decrement demand " + sub.requested.get());
                        } catch (Throwable t) {

                            onError.accept(t);
                        }finally{


                        }
                    }
                    ,onError,()->{

                        completed.incrementAndGet();
                        System.out.println("On complete - drainQ");
                        drainQ[0].accept(false);
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
