package com.aol.cyclops2.internal.stream.spliterators.push;


import cyclops.collections.ListX;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.agrona.concurrent.QueuedPipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class MergeLatestOperatorAsync3<IN> implements Operator<IN> {


    private final Operator<IN>[] operators;


    public MergeLatestOperatorAsync3(Operator<IN>[] sources){
        this.operators=sources;


    }

    private Object nilsafeIn(Object o){
        if(o==null)
            return cyclops.async.Queue.NILL;
        return o;
    }
    private <T> T nilsafeOut(Object o){
        if(cyclops.async.Queue.NILL==o){
            return null;
        }
        return (T)o;
    }

    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        final QueuedPipe<IN> queue = new ManyToManyConcurrentArrayQueue<IN>(1024);
        ListX<Merger2<IN>> mergers = ListX.empty();

        AtomicInteger calls = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicInteger index = new AtomicInteger(0);
        AtomicBoolean wip = new AtomicBoolean(false);
        LongFunction demandFinderRef[] ={null};
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n1->{
                System.out.println("*****!!!!!!!!!!!!!***************    n is "+ n1 + " looping " + Math.min(n1,mergers.size()));





                /**
                  rework to calculate demand so if requests come in when executing we also account for that demand
                **/
                while(isOpen) {
                    long currentRequest = n1;
                    long total = requested.get();

                    long delivered = (long)demandFinderRef[0].apply(n1);

                    if (delivered == currentRequest) {
                        currentRequest = requested.accumulateAndGet(delivered, (a, b) -> a - b);
                        System.out.println("Delivered  " + delivered + " remaining " + currentRequest + " requested " + requested.get());
                        if (currentRequest == 0) {
                            System.out.println("End request..  requested "+  requested.get());
                            return;
                        }
                    }
                }





            };
            @Override
            public void request(long n) {
                System.out.println("Request!! n is "+ n);
                super.singleActiveRequest(n,work);

              //  requested.accumulateAndGet(n,(a,b)->n);
              //  work.accept(n);

            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };
        Runnable completionHandler = ()->{
            calls.incrementAndGet();
            if(complete.get())
                return;
            mergers.forEach(m -> m.drain());
            if (mergers.allMatch(m -> m.isComplete())) {
                sub.cancel();

                mergers.forEach(m -> m.drain());
                System.out.println("Completing on main completion handler!");
                while(!wip.compareAndSet(false,true)){
                    LockSupport.parkNanos(0l);//wait for drain
                }

                System.out.println(" QUEUE SIZE " + queue.size() + " is empty ?" + queue.isEmpty());
                onComplete.run();
                calls.incrementAndGet();
                complete.set(true);
                return;
            }
            //mergers.forEach(m->requested.accumulateAndGet(m.returnDemand(),(a,b)->a+b));
            System.out.println("Not completed ? " + mergers.filter(m -> !m.isComplete()).count());
            mergers.filter(m -> !m.isComplete()).forEach(m->System.out.println("Not complete " +System.identityHashCode( m)));
        };
        LongFunction demandFinder = n-> {
            long sent = 0;
            for (long k = 0; k < Math.min(n, mergers.size()); k++) {
                System.out.println("K is " + k + "  n is " + n + " looping for " + Math.min(n, mergers.size())
                        + " index is " + index + " mergers " + mergers.size()
                        + " open "+  sub.isOpen + " calls " + calls.get());
                if (!sub.isOpen)
                    return sent;
                int toUse = index.incrementAndGet() - 1;
                if (toUse + 1 >= mergers.size()) {
                    index.set(0);

                }

                System.out.println("open ? " + sub.isOpen + "  complete " + mergers.get(toUse).isComplete() + " pred " + (sub.isOpen && !mergers.get(toUse).isComplete()));
                if (sub.isOpen && !mergers.get(toUse).isComplete()) {
                    System.out.println("Sub is open and merger not complete");
                    long activeMergers = mergers.filterNot(m->m.isComplete()).size();
                    if(activeMergers>0){
                        long size = n==Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(1,n/activeMergers);
                        System.out.println("!!!!Booked  Merger " + System.identityHashCode(mergers.get(toUse)) + "  demand " + sub.requested.get() + " " +size);
                        sent++;
                        mergers.get(toUse).request(size);

                    }

                } else {
                    System.out.println("decrementing k");
                    k--;
                }
                mergers.filter(m -> !m.isComplete()).forEach(m->System.out.println("Not complete " +System.identityHashCode( m)));
                if(mergers.allMatch(m -> m.isComplete())) {
                    System.out.println("All complete " +  sub.requested.get() + " sent " + sent + " " + complete.get() + " calls to completion handler " + calls.get());

                    return sent;
                }


            }
            return sent;
        };
        demandFinderRef[0]=demandFinder;

        for(int i=0;i<operators.length;i++){
            int current = i;
             mergers.add(new Merger2<IN>(wip,queue,operators[current],in->{
               //  sub.requested.decrementAndGet();
                 onNext.accept(in);

             },onError,demandFinder,completionHandler));
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
