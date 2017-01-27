package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.types.mixins.Printable;
import cyclops.async.Queue;
import lombok.AllArgsConstructor;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class PublisherFlatMapOperatorAsyncMulti<T,R> extends BaseOperator<T,R> implements Printable {


    int maxCapacity=256;
    final Function<? super T, ? extends Publisher<? extends R>> mapper;;

    public PublisherFlatMapOperatorAsyncMulti(Operator<T> source, Function<? super T, ? extends Publisher<? extends R>> mapper){
        super(source);
        this.mapper = mapper;




    }




    AtomicInteger innerRequests = new AtomicInteger(0);

    @AllArgsConstructor
    static class InnerPublisher<R>{
        volatile long nextRequest= 0;
        volatile long recieved =0;
        volatile long requested =0;

        final AtomicReference<Subscription> activeSub;
        final AtomicInteger status;// = new AtomicInteger(0); //1st bit for completing, 2 bit for inner active, 100 for complete

        final AtomicBoolean init;
        final AtomicBoolean activeRequest;
        final StreamSubscription res;
        final Consumer<? super R> onNext;
        final Consumer<? super Throwable> onError;
        final Runnable onComplete;
        final AtomicInteger parentRequests = new AtomicInteger(0);
        final Subscription[] parent;

        public void onNext(R el){


                System.out.println("!!!!!!!!!Pushing " + el + "  demand " + res.requested.get()  + " status " + status.get() + " thread " + Thread.currentThread().getId() + " demand "  + res.requested.get());

                onNext.accept(el);
                recieved++;
                //res.requested.decrementAndGet();
                System.out.println("******************Setting active to false ON "+ activeRequest.get()+ " T " + Thread.currentThread().getId() + " demand "  + res.requested.get());
                activeRequest.set(false);
                System.out.println("Reset demand " + el + "  demand " + res.requested.get()  + " status " + status.get() + " thread " + Thread.currentThread().getId() + " demand "  + res.requested.get());


                System.out.println("Set active request to false "+  activeRequest.get() + " attempting demand ");
                singleActiveInnerRequest(activeSub, activeRequest, res);


        }
        public void onComplete(){

                activeRequest.set(true);

                System.out.println("Inner complete   thread " + Thread.currentThread().getId() + " demand "  + res.requested.get() + " active " + activeRequest.get());
                activeSub.set(null);
                System.out.println("Active sub is  " + activeSub.get());




                int thunkStatusLocal = -1;
                do {
                    thunkStatusLocal = status.get();
                    System.out.println("Setting status INNER " + (thunkStatusLocal & ~(1 << 1)));

                }
                while (!status.compareAndSet(thunkStatusLocal, thunkStatusLocal & ~(1 << 1))); //unset inner active
                if (status.compareAndSet(1, 100)) { //inner active and complete
                    onComplete.run();
                    return;
                }
                System.out.println("Inner demand to parent " + parentRequests.incrementAndGet() + " status " + status.get());

                parent[0].request(1l);
                //always request more from the parent until outer complete
                System.out.println("****************Setting active to false IC "+ activeRequest.get()+ " T " + Thread.currentThread().getId() + " demand "  + res.requested.get());
                System.out.println("Awaiting next subscription " + activeSub.get()+ " T " + Thread.currentThread().getId() + " demand "  + res.requested.get());
                if(activeSub.get()==null){ //check for new subscription or completeness
                    if (status.compareAndSet(1, 100)) { //inner active and complete
                        System.out.println("Complete while awaiting next sub"+ " T " + Thread.currentThread().getId() + " demand "  + res.requested.get());
                        onComplete.run();
                        return;
                    }
                }
                nextRequest = requested-recieved;
                System.out.println("Got next subscription " + activeSub.get()+ " T " + Thread.currentThread().getId() + " demand "  + res.requested.get());
                activeRequest.set(false);
                System.out.println("Checking demand in Inner on complete! " +  activeRequest.get() + " " + res.requested.get());
                singleActiveInnerRequest(activeSub, activeRequest, res);

                //  after.run();


        }
        private void singleActiveInnerRequest(AtomicReference<Subscription> activeSub, AtomicBoolean activeRequest,  StreamSubscription res) {
            System.out.println("Request " + activeRequest.get() + " " + res.requested.get());
            Subscription a = activeSub.get();
            if(res.isOpen && activeRequest.compareAndSet(false,true)) {

                System.out.println("Signalling demand! " + activeRequest.get() + " demand " + res.requested.get() + " Thread "
                        + Thread.currentThread().getId()
                        + " ************************* " + System.identityHashCode(a));
                if(a!=null) { //track inner requests and deliveries to increase this from 1
                    long requestsLocal = res.requested.get();

                    if(requestsLocal==Long.MAX_VALUE){
                        a.request(Long.MAX_VALUE);
                    }else {
                        res.requested.accumulateAndGet(requestsLocal, (a1, b1) -> a1 - b1);
                        requested += requestsLocal;
                        //int requestsLocal  = Math.max(res.requested.get(),2500)
                        //res.decrement(requests);
                        //switch to isActive
                        //requests += requestsLocal
                        //recieved ++ on every record
                        //in oncomplete pass requests - recieved to next sub - set var that is used until 0
                        a.request(requestsLocal+nextRequest);
                        nextRequest=0;
                    }
                }
                else{
                    System.out.println("Active Sub is null - falling back");
                    activeRequest.set(false);
                }
            }else{

                System.out.println("Failed to signal demand " + activeRequest.get() + " active " + res.isActive());
            }
        }
    }

    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        AtomicReference<Subscription> activeSub = new AtomicReference<>(null);
        StreamSubscription[] s = {null} ;

        final AtomicInteger status = new AtomicInteger(0); //1st bit for completing, 2 bit for inner active, 100 for complete

        final AtomicBoolean init = new AtomicBoolean(false);
        final AtomicBoolean activeRequest = new AtomicBoolean(false);
        final InnerPublisher innerPublisherRef[]={null};
        StreamSubscription res = new StreamSubscription(){
            LongConsumer work = n-> {

                System.out.println("New demand! Requesting on thread " + Thread.currentThread().getId() + " demand "  + this.requested.get());

                System.out.println("Active test = " + ( (status.get() & (1L << 1))==0) +  " status " + status.get());
                if (!init.get()) {
                    init.set(true);
                    s[0].request(1);
                }else{
                    System.out.println("Signalling to active sub " + activeSub.get() + " " + status.get());
                    while(activeSub.get()==null){
                        if(status.get()>=100 || requested.get()==0){
                            return;
                        }
                        LockSupport.parkNanos(1l);
                    }
                    if(status.get()>=100 || requested.get()==0){
                        return;
                    }
                    long reqs = requested.get();
                    innerPublisherRef[0].singleActiveInnerRequest(activeSub,activeRequest,this);
                    //  System.out.println("Outer request to inner " + innerRequests.incrementAndGet() + " status " + status.get() + " demand " + requested.get());
                     // activeSub.get().request(1);
                }
            };
            @Override
            public void request(long n) {
                if(n<=0)
                    onError.accept(new IllegalArgumentException( "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));

                this.singleActiveRequest(n,work);

            }

            @Override
            public void cancel() {
                s[0].cancel();
                super.cancel();
            }
        };

        final InnerPublisher innerPublisher = new InnerPublisher(0,0,0,activeSub,status,init,activeRequest,res,onNext,onError,onComplete,s);
        innerPublisherRef[0] = innerPublisher;
        s[0] = source.subscribe(e-> {
                    try {

                        Publisher<? extends R> split = mapper.apply(e);
                        System.out.println("Registering next publisher for " +e  + " thread " + Thread.currentThread().getId() + " demand "  + res.requested.get());
                        PublisherToOperator<R> op = new PublisherToOperator<>((Publisher<R>)split);
                        Subscription sLocal = op.subscribe(innerPublisher::onNext,onError,innerPublisher::onComplete);


                        int statusLocal =-1;
                        do {
                            statusLocal = status.get();

                            System.out.println("Status local  is "  + statusLocal);
                            System.out.println("Setting status active to " + (statusLocal | (1 << 1)));

                        }while(!status.compareAndSet(statusLocal,statusLocal | (1 << 1))); //set inner active



                        System.out.println("On register demand to inner " + innerRequests.incrementAndGet() + " status " + status.get());
                        System.out.println("Switching sub! " + activeRequest.get());

                        activeSub.set(sLocal);//set after active


                        System.out.println("Checking demand in main onnext " + activeRequest.get() + " demand is " + res.requested.get());
                        innerPublisher.singleActiveInnerRequest(activeSub, activeRequest, res);
                        System.out.println("Demand signalled on thread " + Thread.currentThread().getId() + " demand "  + res.requested.get());

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,t->{
                    onError.accept(t);
                    res.requested.decrementAndGet();
                    if(res.isOpen){
                        s[0].request(1);
                    }
                },()->{
                    System.out.println("OUTER COMPLETE!!");
                    int statusLocal = -1;
                    do {
                        statusLocal = status.get();
                        System.out.println("Setting status OUTER " + (statusLocal | (1 << 0)));

                    }while(!status.compareAndSet(statusLocal,statusLocal | (1 << 0)));

                    if(status.compareAndSet(1,100)){
                        System.out.println("Outer complete!");
                        onComplete.run();
                    }

                });

        return res;
    }



    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Deque<Publisher<? extends R>> queued = new ConcurrentLinkedDeque<>();
        ManyToOneConcurrentArrayQueue<R> data = new ManyToOneConcurrentArrayQueue<R>(256);
        ManyToOneConcurrentArrayQueue<Throwable> errors = new ManyToOneConcurrentArrayQueue<>(256);
        AtomicReference<PVector<Subscription>> activeSubs = new AtomicReference<>(TreePVector.empty());
        AtomicInteger active = new AtomicInteger(0);
        source.subscribeAll(e-> {
                    try {

                        Publisher<? extends R> next = mapper.apply(e);
                        queued.add(next);
                        StreamSubscription sub = new StreamSubscription();
                        sub.request(maxCapacity);
                        drainAndLaunch(active,sub,onNext, queued, data, errors);

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{

                    while(active.get()>0 || data.size()>0 || queued.size()>0 || errors.size()>0){
                        //drain, create demand, launch queued publishers
                        Object next = data.poll();
                        if(next!=null)
                            onNext.accept(print(nilsafe(next)));

                        if(errors.size()>0){
                                onError.accept(errors.poll());
                        }
                    }
                    System.out.println("Completing!! " + queued.size() + "  " + active.get());
                    onCompleteDs.run();
                });
    }

    private <T> T nilsafe(Object o){
        if(Queue.NILL==o){
            return null;
        }
        return (T)o;
    }

    private void drainAndLaunch(AtomicInteger active, StreamSubscription maxCapacity,Consumer<? super R> onNext, Deque<Publisher<? extends R>> queued, ManyToOneConcurrentArrayQueue<R> data, ManyToOneConcurrentArrayQueue<Throwable> errors) {

        while(queued.size()>0 && active.get()<10) {
            subscribe(active,queued, data, errors);
            Object next = data.poll();
            if(next!=null)
                onNext.accept(print(nilsafe(next)));


        }



    }

    private void subscribe(AtomicInteger active,Deque<Publisher<? extends R>> queued,
                           ManyToOneConcurrentArrayQueue<R> data,
                           ManyToOneConcurrentArrayQueue<Throwable> errors){


        Publisher<? extends R> next = queued.poll();
        if(next==null)
            return;
        next.subscribe(new Subscriber<R>() {
            AtomicReference<Subscription> sub;

            private Object nilsafe(Object o){
                if(o==null)
                    return Queue.NILL;
                return o;
            }

            @Override
            public void onSubscribe(Subscription s) {

                this.sub=new AtomicReference<>(s);
                active.incrementAndGet();
                s.request(Long.MAX_VALUE);

            }

            @Override
            public void onNext(R r) {
                //Optimization check if this is the
                //main thread and if so just call onNext
                data.offer((R)nilsafe(r));
               // sub.get().request(1l);

            }

            @Override
            public void onError(Throwable t) {
                //Optimization check if this is the
                //main thread and if so just call onError
                errors.offer(t);
             //   sub.get().request(1l);
            }

            @Override
            public void onComplete() {


                if(queued.size()>0){
                    subscribe(active,queued,data,errors);
                }
                active.decrementAndGet();
            }
        });
    }

}