package com.oath.cyclops.internal.stream.spliterators.push;

import cyclops.data.Seq;
import lombok.AllArgsConstructor;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Queue;
import java.util.concurrent.atomic.*;
import java.util.function.Consumer;
import java.util.function.Function;


public class ConcurrentFlatMapper<T, R> {

    volatile Seq<ActiveSubscriber> activeList = Seq.empty();
    static final AtomicReferenceFieldUpdater<ConcurrentFlatMapper, Seq> queueUpdater =
            AtomicReferenceFieldUpdater.newUpdater(ConcurrentFlatMapper.class, Seq.class, "activeList");

    final Consumer<? super R> onNext;
    final Consumer<? super Throwable> onError;
    final Runnable onComplete;


    final Function<? super T, ? extends Publisher<? extends R>> mapper;

    final StreamSubscription sub;
    final int maxConcurrency;
    private volatile boolean running = true;


    final AtomicLong requested = new AtomicLong(0);
    final AtomicInteger wip = new AtomicInteger(0);


    int subscriberIndex;
    boolean processAll = false;


    public ConcurrentFlatMapper(StreamSubscription s, Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete,
                       Function<? super T, ? extends Publisher<? extends R>> mapper,
                       int maxConcurrency) {
        this.sub = s;
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
        this.mapper = mapper;
        this.maxConcurrency = maxConcurrency;


    }

    public void request(long n) {

        if(!sub.isOpen)
            return;
        if (processAll)
            return;

        if(n==Long.MAX_VALUE){
            processAll =true;
            requested.set(Long.MAX_VALUE);
        }
        requested.accumulateAndGet(n,(a,b)->{
            long sum = a+b;
            return sum <0L ? Long.MAX_VALUE : sum;
        });

        handleMainPublisher();
    }


    public void onNext(T t) {
        if (!running)
            return;
        try {
            Publisher<? extends R> next = mapper.apply(t);
            ActiveSubscriber inner = new ActiveSubscriber();
            queueUpdater.getAndUpdate(this, q -> q.plus(inner));
            next.subscribe(inner);
        }catch(Throwable e){
            onError.accept(e);
        }

    }

    private boolean remove(ActiveSubscriber toRemove) {
        queueUpdater.getAndUpdate(this, q -> q.removeValue(toRemove));
        return true;
    }


    public void onError(Throwable t) {
        if (!running)
            return;
        onError.accept(t);
        handleMainPublisher();
    }


    public void onComplete() {
        if (!running)
            return;

        running = false;
        handleMainPublisher();

    }

    void handleMainPublisher() {
        if (wip.getAndIncrement() != 0) {
            return;
        }
        populateFromQueuesAndCleanup();
    }

    int incrementActiveIndex(int index, Seq<ActiveSubscriber> active){
        return index > active.size() ? 0 : subscriberIndex;
    }

    @AllArgsConstructor
    class SubscriberRequests {

        boolean completed;
        long pendingRequests;
        long requestedLocal;
        long missing;
        boolean rerun;
        ActiveSubscriber nextActive;

        boolean populateRequestsFromQueue(){
            while (pendingRequests != requestedLocal) {
                completed = nextActive.done;

                R raw = nextActive.queue.poll();

                if (complete(false)) {
                    return false;
                }
                if(raw == null) {
                    if (completed)
                        removeAndReturn();

                    return true;
                }

                onNext.accept(com.oath.cyclops.async.adapters.Queue.nillSafe(raw));

                pendingRequests++;
            }
            return true;
        }

        void setNextActive(ActiveSubscriber nextActive){
            this.nextActive = nextActive;
            completed = nextActive.done;
        }
        void handleComplete(){
            if (pendingRequests == requestedLocal) {
                completed = nextActive.done;
                cleanup();
            }
        }

        void cleanup() {
            if (completed && nextActive.queue.isEmpty()) {
                removeAndReturn();
            }
        }

        private void removeAndReturn() {
            remove(nextActive);
            rerun = true;
            missing++;
        }

        void processPendingRequests(){
            if (pendingRequests != 0L) {
                if (!nextActive.done) {
                    nextActive.sub.get().request(pendingRequests);
                }
                if (requestedLocal != Long.MAX_VALUE) {
                    requestedLocal =  requested.addAndGet(-pendingRequests);
                }
                pendingRequests = 0L;
            }
        }

        void sendMissingRequests() {

            if (missing != 0L && running && sub.isOpen) {

                sub.request(missing);
            }
        }


        int recalcConcurrentRequests(int missed) {

            if(!rerun){
                return wip.addAndGet(-missed);
            }
            return missed;
        }
        boolean complete(boolean empty) {

            if (!sub.isOpen)
                return true;


            if(completed && empty) {
                onComplete.run();
                return true;
            }

            return false;
        }
    }

    void populateFromQueuesAndCleanup() {
        int incomingRequests = 1;

        do {

            Seq<ActiveSubscriber> localActiveSubs = activeList;
            SubscriberRequests state = new SubscriberRequests(!running,0l,requested.get(),0L,false,null);

            if (state.complete(activeList.isEmpty()))
                return;

            if (activeRequestsAndSubscriptions(state)) {
                if (processRequests(localActiveSubs, state))
                    return;
            }

            if (noActiveRequestsAndSubscriptions(state)) {
                localActiveSubs = activeList;
                if (cleanupSubsAndReqs(localActiveSubs, state))
                    return;
            }
            state.sendMissingRequests();
            incomingRequests =state.recalcConcurrentRequests(incomingRequests);

        } while (incomingRequests != 0);
    }

    private boolean noActiveRequestsAndSubscriptions(SubscriberRequests state) {
        return state.requestedLocal == 0L && !activeList.isEmpty();
    }

    private boolean activeRequestsAndSubscriptions(SubscriberRequests state) {
        return state.requestedLocal != 0L && !activeList.isEmpty();
    }

    private boolean cleanupSubsAndReqs(Seq<ActiveSubscriber> localActiveSubs, SubscriberRequests state) {
        ActiveSubscriber active =null;
        for (int i = 0; i < localActiveSubs.size() && (active=localActiveSubs.getOrElse(i,null)).queue.isEmpty() && sub.isOpen; i++) {
            if (!sub.isOpen) {
                return true;
            }
            state.setNextActive(active);
            state.cleanup();
        }
        return false;
    }

    private boolean processRequests(Seq<ActiveSubscriber> localActiveSubs, SubscriberRequests state) {
        int activeIndex = incrementActiveIndex(subscriberIndex,activeList);

        for (int i = 0; i < localActiveSubs.size() && state.requestedLocal !=0L && sub.isOpen; i++) {
            state.setNextActive( localActiveSubs.getOrElse(activeIndex,null));
            if(!state.populateRequestsFromQueue())
                return true;
            state.handleComplete();
            state.processPendingRequests();
            activeIndex = incrementActiveIndex(activeIndex+1,localActiveSubs);

        }

        subscriberIndex = activeIndex;
        return false;
    }

    final class ActiveSubscriber implements Subscriber<R> {


        final AtomicReference<Subscription> sub = new AtomicReference();
        final Queue<R> queue = new OneToOneConcurrentArrayQueue<>(1024);
        private volatile boolean done;


        @Override
        public void onSubscribe(Subscription s) {
            if (this.sub.compareAndSet(null, s)) {
                s.request(1);
            }
        }

        @Override
        public void onNext(R t) {

            if (wip.compareAndSet(0, 1)) {
                long localRequested = requested.get();


                if (localRequested != 0L) {

                    onNext.accept(t);
                    if (localRequested != Long.MAX_VALUE) {
                        requested.decrementAndGet();
                    }
                    sub.get().request(1);
                } else {
                    queue.offer(com.oath.cyclops.async.adapters.Queue.nullSafe(t)); //queue full! handle somehow
                }
                if (wip.decrementAndGet() == 0) {
                    return;
                }
                populateFromQueuesAndCleanup();
            } else {
                queue.offer(com.oath.cyclops.async.adapters.Queue.nullSafe(t)); //queue full! handle somehow
                handleMainPublisher();
            }
        }

        @Override
        public void onError(Throwable t) {
            if(done)
                return;
            onError.accept(t);
            handleMainPublisher();
        }

        @Override
        public void onComplete() {
            done = true;
            if (wip.getAndIncrement() != 0) {
                return;
            }
            populateFromQueuesAndCleanup();

        }
    }
}


