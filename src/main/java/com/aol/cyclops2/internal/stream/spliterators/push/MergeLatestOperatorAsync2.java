package com.aol.cyclops2.internal.stream.spliterators.push;


import cyclops.collections.ListX;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.agrona.concurrent.QueuedPipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class MergeLatestOperatorAsync2<IN> implements Operator<IN> {


    private final Operator<IN>[] operators;


    public MergeLatestOperatorAsync2(Operator<IN>[] sources){
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
        ListX<Merger<IN>> mergers = ListX.empty();
        AtomicLong sent = new AtomicLong(0);
        AtomicInteger index = new AtomicInteger(0);
        AtomicBoolean wip = new AtomicBoolean(false);
        LongConsumer demandFinderRef[] ={null};
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n1->{
                System.out.println("*****!!!!!!!!!!!!!***************    n is "+ n1 + " looping " + Math.min(n1,mergers.size()));



                demandFinderRef[0].accept(n1);



                System.out.println("End request.. sent " + sent.get() +  " requested " + requested.get());

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
            mergers.forEach(m -> m.drain());
            if (mergers.allMatch(m -> m.isComplete())) {
                mergers.forEach(m -> m.drain());
                System.out.println("Completing on main completion handler!");
                while(!wip.compareAndSet(false,true)){
                    LockSupport.parkNanos(0l);//wait for drain
                }
                onComplete.run();
                return;
            }
            //mergers.forEach(m->requested.accumulateAndGet(m.returnDemand(),(a,b)->a+b));
            System.out.println("Not completed ? " + mergers.filter(m -> !m.isComplete()).count());
            mergers.filter(m -> !m.isComplete()).forEach(m->System.out.println("Not complete " +System.identityHashCode( m)));
        };
        LongConsumer demandFinder = n-> {
            for (long k = 0; k < Math.min(n, mergers.size()); k++) {
                System.out.println("K is " + k);
                if (!sub.isActive())
                    break;
                int toUse = index.incrementAndGet() - 1;
                if (toUse + 1 >= mergers.size()) {
                    index.set(0);

                }

                if (sub.isActive() && !mergers.get(toUse).isComplete()) {

                    System.out.println("!!!!Booked  Merger " + System.identityHashCode(mergers.get(toUse)) + "  demand " + sub.requested.get());
                    mergers.get(toUse).request(1l);
                    sent.incrementAndGet();

                } else
                    k--;

                if(mergers.allMatch(m -> m.isComplete())) {
                    return;
                }


            }
        };
        demandFinderRef[0]=demandFinder;

        for(int i=0;i<operators.length;i++){
            int current = i;
             mergers.add(new Merger<IN>(wip,queue,operators[current],in->{
                 sub.requested.decrementAndGet();
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
