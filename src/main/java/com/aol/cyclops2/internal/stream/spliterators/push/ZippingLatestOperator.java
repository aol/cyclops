package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.async.adapters.Queue;
import lombok.AllArgsConstructor;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

@AllArgsConstructor
public class ZippingLatestOperator<T1,T2,R> implements Operator<R>{


    Operator<? super T1> left;
    Operator<? super T2> right;
    private final BiFunction<? super T1, ? super T2, ? extends R> fn;


    private final Object UNSET = new Object();

    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {


        StreamSubscription  leftSub[] = {null};
        StreamSubscription  rightSub[] = {null};
        AtomicBoolean leftComplete = new AtomicBoolean(false); //lazyLeft & lazyRight compelte can be merged into singleUnsafe integer
        AtomicBoolean rightComplete = new AtomicBoolean(false);
        AtomicReference<Tuple2<T1,T2>> nextValue = new AtomicReference<>(Tuple.tuple((T1)UNSET,(T2)UNSET));
        AtomicBoolean completing = new AtomicBoolean(false);

        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);
        ManyToOneConcurrentArrayQueue<R> data = new ManyToOneConcurrentArrayQueue<R>(1024);

        List<StreamSubscription> subs = new ArrayList<>(2);
        StreamSubscription sub   = new StreamSubscription(){
            LongConsumer work = n->{
                while(requested.get()>0)
                {
                    if (completed.get() == 2 && data.size() == 0) {

                        onComplete.run();
                        return;
                    }

                    long sent = 0;
                    long reqCycle = requested.get();
                    for (long k = 0; k < reqCycle; k++) {
                        if (!isOpen)
                            return;
                        int toUse = index.incrementAndGet() - 1;
                        if (toUse + 1 >= subs.size()) {
                            index.set(0);

                        }

                        if (subs.get(toUse).isOpen) {
                            subs.get(toUse).request(1l);

                        } else
                            k--;

                        R fromQ = nilsafeOut(data.poll());
                        if (fromQ != null) {

                            onNext.accept(fromQ);
                            requested.decrementAndGet();
                            sent++;
                        }
                        if (completed.get() == subs.size() && data.size() == 0) {

                            onComplete.run();
                            return;
                        }



                    }

                    while (sent < reqCycle && isOpen && !(completed.get() == subs.size() && data.size() == 0)) {

                        R fromQ = nilsafeOut(data.poll());
                        if (fromQ != null) {
                            onNext.accept(fromQ);
                            requested.decrementAndGet();
                            sent++;
                        }

                    }
                    if (completed.get() == subs.size() && data.size() == 0) {

                        onComplete.run();
                        return;

                    }

                }



            };
            @Override
            public void request(long n) {
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }

                this.singleActiveRequest(n,work);

            }

            @Override
            public void cancel() {
                if(leftSub[0]!=null)
                    leftSub[0].cancel();
                if(rightSub[0]!=null)
                    rightSub[0].cancel();
                super.cancel();
            }
        };
        leftSub[0]  = left.subscribe(e->{
            if(!sub.isOpen)
                return;


            try {

                boolean set = false;
                while(!set){
                    Tuple2<T1,T2> local = nextValue.get();
                    Tuple2<T1,T2> updated = local.map1(__->(T1)e);
                    set = nextValue.compareAndSet(local,updated);
                    if(set){
                        if(updated._2()!=UNSET){

                                while(!data.offer((R)nilsafeIn(applyFn(updated)))){

                                }


                        }
                    }
                }


            } catch (Throwable t) {
                onError.accept(t);
            }


        },e->{
            onError.accept(e);
            leftSub[0].request(1l);
        },()->{
            completed.incrementAndGet();


        });
        rightSub[0] = right.subscribe(e->{
            if(!sub.isOpen)
                return;

            try {

                boolean set = false;
                while(!set){
                    Tuple2<T1,T2> local = nextValue.get();
                    Tuple2<T1,T2> updated = local.map2(__->(T2)e);
                    set = nextValue.compareAndSet(local,updated);
                    if(set){
                        if(updated._1()!=UNSET){
                            while(!data.offer((R)nilsafeIn(applyFn(updated)))){

                            }

                        }
                    }
                }



            } catch (Throwable t) {
                onError.accept(t);
            }



        },e->{
            onError.accept(e);
            rightSub[0].request(1l);
        },()->{
            completed.incrementAndGet();


        });

        subs.add(leftSub[0]);
        subs.add(rightSub[0]);


        return sub;
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

    private R applyFn(Tuple2<T1, T2> updated) {
        R res = fn.apply(updated._1(),updated._2());
        return res;
    }

    private void handleComplete(AtomicBoolean completeSent,Runnable onComplete){
        if(completeSent.compareAndSet(false,true)){
            onComplete.run();
        }
    }





    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {


        AtomicBoolean active = new AtomicBoolean(false);
        ManyToOneConcurrentArrayQueue<R> data = new ManyToOneConcurrentArrayQueue<R>(1024);
        AtomicReference<Tuple2<T1,T2>> nextValue = new AtomicReference<>(Tuple.tuple((T1)UNSET,(T2)UNSET));
        AtomicBoolean leftComplete = new AtomicBoolean(false); //lazyLeft & lazyRight compelte can be merged into singleUnsafe integer
        AtomicBoolean rightComplete = new AtomicBoolean(false);
        left.subscribeAll(e->{


            try {

                boolean set = false;
                while(!set){
                    Tuple2<T1,T2> local = nextValue.get();
                    Tuple2<T1,T2> updated = local.map1(__->(T1)e);
                    set = nextValue.compareAndSet(local,updated);
                    if(set){
                        if(updated._2()!=UNSET){
                            if(active.compareAndSet(false,true)){
                                data.drain(onNext::accept);
                                onNext.accept(applyFn(updated));
                                active.set(false);
                            }
                            else{
                                while(!data.offer(applyFn(updated))){

                                }
                            }



                        }
                    }
                }


            } catch (Throwable t) {
                onError.accept(t);
            }


        },e->{
            onError.accept(e);

        },()->{
            while(!active.compareAndSet(false,true)) {

            }
            data.drain(onNext::accept);
            if(rightComplete.get()){
                onCompleteDs.run();
            }
            leftComplete.set(true);
            active.set(false);


        });
        right.subscribeAll(e->{

            try {

                boolean set = false;
                while(!set){
                    Tuple2<T1,T2> local = nextValue.get();
                    Tuple2<T1,T2> updated = local.map2(__->(T2)e);
                    set = nextValue.compareAndSet(local,updated);
                    if(set){
                        if(updated._1()!=UNSET){
                            if(active.compareAndSet(false,true)){
                                data.drain(onNext::accept);
                                onNext.accept(applyFn(updated));
                                active.set(false);
                            }
                            else{
                                while(!data.offer(applyFn(updated))){

                                }
                            }


                        }
                    }
                }


            } catch (Throwable t) {
                onError.accept(t);
            }



        },e->{
            onError.accept(e);

        },()->{

            while(!active.compareAndSet(false,true)) {

            }
            data.drain(onNext::accept);
            if(leftComplete.get()){
                onCompleteDs.run();
            }
            rightComplete.set(true);

            active.set(false);



        });


    }

}
