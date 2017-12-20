package cyclops.reactive;

import com.oath.cyclops.hkt.Higher;

import com.oath.cyclops.internal.stream.spliterators.push.*;
import com.oath.cyclops.types.reactive.BufferOverflowPolicy;
import com.oath.cyclops.types.reactive.PushSubscriber;
import cyclops.control.*;
import com.oath.cyclops.internal.stream.ReactiveStreamX;
import com.oath.cyclops.internal.stream.ReactiveStreamX.Type;
import com.oath.cyclops.internal.stream.spliterators.UnfoldSpliterator;
import com.oath.cyclops.types.reactive.AsyncSubscriber;
import com.oath.cyclops.types.reactive.ReactiveSubscriber;
import cyclops.reactive.collections.mutable.ListX;
import com.oath.cyclops.hkt.DataWitness.reactiveSeq;

import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import cyclops.data.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * reactiveBuffer : is used to denote creational methods for reactiveBuffer-streams that support non-blocking backpressure
 * async : is used to denote creational methods for asynchronous streams that do not support backpressure
 */

public interface Spouts {



    /**
     * Create an Subscriber for Observable style asynchronous push based Streams.
     * Streams generated from AsyncSubscribers are not backpressure aware (in cases
     * where backpressue is not needed they may perform better).
     * For backpressure aware Streams see {@link Spouts#reactiveSubscriber}
     *
     * @param <T> Stream data type
     * @return Async Stream Subscriber
     */
    static <T> AsyncSubscriber<T> asyncSubscriber(){
        return new AsyncSubscriber<T>();
    }

    /**
     * Create a Stream that accepts data via the Subsriber passed into the supplied Consumer.
     * reactiveBuffer-streams susbscription is ignored (i.e. this Stream is backpressure free)
     *
     * <pre>
     *     {@code
     *      ReactiveSeq<Integer> input = Spouts.async(subscriber->{
     *                                                          listener.onEvent(subscriber::onNext);
     *                                                          listener.onError(susbscriber::onError);
     *                                                          closeListener.onEvent(subscriber::onClose);
     *                                                      });
     *      }
     * </pre>
     *
     * @param sub
     * @param <T>
     * @return
     */
    static <T> ReactiveSeq<T> async(Consumer<? super PushSubscriber<T>> sub){
        AsyncSubscriber<T> s = asyncSubscriber();
        return s.registerAndstream(()->{
            while(!s.isInitialized()){
                LockSupport.parkNanos(1l);
            }
            sub.accept(s);
        });
    }

    /**
     * Create a push based Stream with <b>no backpressure</b> fromm the provided Stream.
     * The provided Stream will be executed on the provided executor and pushed to the returned Stream
     *
     * @param seq Stream to execute and push to a new non-backpressure aware Stream
     * @param exec
     * @param <T>
     * @return
     */
    static <T> ReactiveSeq<T> async(Stream<T> seq, Executor exec){
          return async(s->{

            ReactiveSeq.fromStream(seq).foldFuture(exec,t->{

                PushSubscriber<T> local = s;
                t.forEach(local::onNext,local::onError,local::onComplete);
                return null;
            });
        });
    }

    /**
     * Create a buffering reactive-streams source. Your subscriber can respect or ignore reactive-streams backpressure.
     * This operator will buffer incoming data before sending on when downstream streams are ready.
     * This operator drops values once buffer size has been exceeded
     *
     * E.g. In the example below 2 elements are requested, we ignore this and send 30 elements instead
     * <pre>
     *     {@code
     *     Subscription sub = Spouts.reactiveBuffer(10, s -> {
                                                        s.onSubscribe(new Subscription() {
                                                                @Override
                                                                public void request(long n) {
                                                                    //ignore back pressure
                                                                    //send lots of data downstream regardless
                                                                        Effect e = () -> {
                                                                            s.onNext("hello " + i++);
                                                                        }
                                                                        e.cycle(30).run();

                                                                }

                                                                @Override
                                                                public void cancel() {

                                                                }
                                                                });


                                                }).forEach(2, System.out::println);


            //only 2 elements will be printed out
            //10 will be buffered and potentially 18 dropped
            Thread.sleep(500);
            sub.request(20);
     *
     *
     *     }
     *
     *
     * </pre>
     *
     * @param buffer
     * @param onNext
     * @param <T>
     * @return
     */
    static <T> ReactiveSeq<T> reactiveBuffer(int buffer, Consumer<? super Subscriber<T>> onNext){
        return Spouts.reactiveStream(new BufferingSinkOperator<T>(new ManyToManyConcurrentArrayQueue<T>(buffer), onNext, BufferOverflowPolicy.DROP));
    }
    static <T> ReactiveSeq<T> reactiveBufferBlock(int buffer, Consumer<? super Subscriber<T>> onNext){
        return Spouts.reactiveStream(new BufferingSinkOperator<T>(new ManyToManyConcurrentArrayQueue<T>(buffer), onNext,BufferOverflowPolicy.BLOCK));
    }
    static <T> ReactiveSeq<T> reactiveBuffer(Queue<T> buffer,BufferOverflowPolicy policy, Consumer<? super Subscriber<T>> onNext){
        return Spouts.reactiveStream(new BufferingSinkOperator<T>(buffer, onNext, policy));
    }
    static <T> ReactiveSeq<T> asyncBuffer(int buffer, Consumer<? super PushSubscriber<T>> onNext){
        return Spouts.asyncStream(new BufferingSinkOperator<T>(new ManyToManyConcurrentArrayQueue<T>(buffer),c-> onNext.accept(PushSubscriber.of(c)), BufferOverflowPolicy.DROP));
    }
    static <T> ReactiveSeq<T> asyncBufferBlock(int buffer, Consumer<? super PushSubscriber<T>> onNext){
        return Spouts.asyncStream(new BufferingSinkOperator<T>(new ManyToManyConcurrentArrayQueue<T>(buffer), c-> onNext.accept(PushSubscriber.of(c)), BufferOverflowPolicy.BLOCK));
    }
    static <T> ReactiveSeq<T> asyncBuffer(Queue<T> buffer,BufferOverflowPolicy policy, Consumer<? super PushSubscriber<T>> onNext){
        return Spouts.asyncStream(new BufferingSinkOperator<T>(buffer, c-> onNext.accept(PushSubscriber.of(c)), policy));
    }
    static <T> ReactiveSeq<T> reactive(Stream<T> seq, Executor exec){
        Future<Subscriber<T>> subscriber = Future.future();
        Future<Subscription> sub = Future.future();
        AtomicBoolean complete = new AtomicBoolean();
        AtomicLong requested = new AtomicLong(0);
        ReactiveSeq.fromStream(seq).foldFuture(exec,t->{
            Subscriber<T> local = subscriber.getFuture().join();
            Subscription streamSub = t.forEach(0,local::onNext,local::onError,()->{
                complete.set(true);
                local.onComplete();

            });
            sub.complete(new Subscription() {
                @Override
                public void request(long n) {
                    requested.addAndGet(n);
                }

                @Override
                public void cancel() {
                    streamSub.cancel();
                }
            });
            while(!complete.get()){
                long next = requested.get();
                if(next==0){
                    Thread.yield();
                }else {
                    while (!requested.compareAndSet(next, 0)) {
                        next = requested.get();
                    }
                    streamSub.request(next);
                }
            }

            return null;
        });
        return new ReactiveStreamX<T>(new PublisherToOperator<T>(new Publisher<T>() {


            @Override
            public void subscribe(Subscriber<? super T> s) {

                subscriber.complete((Subscriber<T>)s);
                s.onSubscribe(sub.getFuture().join());


            }
        }));
    }


    /**
     *   The recommended way to connect a Spout to a Publisher is via Spouts#from
     *   Create an Subscriber for Observable style asynchronous push based Streams,
     *   that implements backpressure internally via the reactiveBuffer-streams spec.
     *
     *   Subscribers signal demand via their subscription and publishers push data to subscribers
     *   synchronously or asynchronously, never exceeding signalled demand
     *
     * @param <T> Stream data type
     * @return An async Stream Subscriber that supports efficient backpressure via reactiveBuffer-streams
     */
    static <T> ReactiveSubscriber<T> reactiveSubscriber(){
        return new ReactiveSubscriber<T>();
    }

    static <T> ReactiveSeq<T> reactive(Consumer<? super Subscriber<T>> sub){
        ReactiveSubscriber<T> reactive = new ReactiveSubscriber<T>();
        sub.accept(reactive);
        return reactive.reactiveStream();
    }
    static <T> ReactiveSeq<T> reactiveStream(Operator<T> s){
        return new ReactiveStreamX<>(s).withAsync(Type.BACKPRESSURE);
    }
    static <T> ReactiveSeq<T> asyncStream(Operator<T> s){
        return new ReactiveStreamX<>(s).withAsync(Type.NO_BACKPRESSURE);
    }
    static <T> ReactiveSeq<T> syncStream(Operator<T> s){
        return new ReactiveStreamX<>(s);
    }

    static <T> ReactiveSeq<T> iterate(final T seed, final UnaryOperator<T> f) {
        return new ReactiveStreamX(new IterateOperator<T>(seed,f));
    }
    static <T> ReactiveSeq<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return new ReactiveStreamX(new IteratePredicateOperator<T>(seed,f,pred));

    }
    public static ReactiveSeq<Integer> range(int start, int end){
        if(start<end)
            return new ReactiveStreamX<Integer>(new RangeIntOperator(start,end));
        else
            return new ReactiveStreamX<Integer>(new RangeIntOperator(end,start));
    }
    public static  ReactiveSeq<Long> rangeLong(long start, long end){
        if(start<end)
            return new ReactiveStreamX<>(new RangeLongOperator(start,end));
        else
            return new ReactiveStreamX<>(new RangeLongOperator(end,start));
    }
    public static  <T> ReactiveSeq<T> of(T value){
        return new ReactiveStreamX<>(new SingleValueOperator<T>(value));
    }

    public static <T> ReactiveSeq<T> ofNullable(T nullable){
        if(nullable==null){
            return empty();
        }
        return of(nullable);
    }
    public static <T> ReactiveSeq<T> empty(){
        return of();
    }

    public static  <T> ReactiveSeq<T> of(T... values){
        return new ReactiveStreamX<>(new ArrayOfValuesOperator<T>(values));
    }
    public static  <T> ReactiveSeq<T> fromIterable(Iterable<T> iterable){
        if(iterable instanceof ReactiveStreamX)
            return (ReactiveSeq<T>)iterable;
        return new ReactiveStreamX<>(new IterableSourceOperator<T>(iterable));
    }
    public static  <T> ReactiveSeq<T> fromSpliterator(Spliterator<T> spliterator){
        return new ReactiveStreamX<>(new SpliteratorToOperator<T>(spliterator));
    }
    /**
     * @see Stream#generate(Supplier)
     */
    static <T> ReactiveSeq<T> generate(final Supplier<T> s) {
        return new ReactiveStreamX<>(new GenerateOperator<T>(s));

    }
    static <T> ReactiveSeq<T> from(Publisher<? extends T> pub){
        if(pub instanceof ReactiveSeq)
            return (ReactiveSeq<T>)pub;
        return new ReactiveStreamX<T>(new PublisherToOperator<T>((Publisher<T>)pub), Type.BACKPRESSURE);
    }
    static <T> ReactiveSeq<T> merge(Publisher<? extends Publisher<T>> publisher){
        return mergeLatest((Publisher[])Spouts.from(publisher).toArray());
    }



    static <T> ReactiveSeq<T> mergeLatestList(ListX<? extends Publisher<? extends T>> publisher){
        return mergeLatest((Publisher[])ReactiveSeq.fromPublisher(publisher).toArray(s->new Publisher[s]));
    }
    static <T> ReactiveSeq<T> mergeLatest(Publisher<? extends Publisher<T>> publisher){
        return mergeLatest((Publisher[])ReactiveSeq.fromPublisher(publisher).toArray(s->new Publisher[s]));
    }
    static <T> ReactiveSeq<T> mergeLatest(int maxConcurrency,Publisher<T>... array){
        return Spouts.of(array).mergeMap(maxConcurrency, i->i);
    }
    static <T> ReactiveSeq<T> mergeLatest(Publisher<T>... array){


        Operator<T>[] op = new Operator[array.length];
        for(int i=0;i<array.length;i++){
            if(array[i] instanceof ReactiveStreamX){
                ReactiveStreamX<T> stream = (ReactiveStreamX<T>)array[i];
                op[i] = stream.getSource();
            }else{
                op[i] = new PublisherToOperator<T>(array[i]);
            }
        }
        return new ReactiveStreamX<T>(new MergeLatestOperator<T>(op), Type.BACKPRESSURE);


    }
    static <T> ReactiveSeq<T> amb(ListX<? extends Publisher<? extends T>> list){
        return amb(list.toArray(new ReactiveSeq[0]));
    }
    static <T> ReactiveSeq<T> amb(Publisher<? extends T>... array){
        return ambWith(array);
    }
    static <T> ReactiveSeq<T> ambWith(Publisher<? extends T>[] array){
        ReactiveSubscriber<T> res = Spouts.reactiveSubscriber();

        AtomicInteger first = new AtomicInteger(0);
        AtomicBoolean[] complete = new AtomicBoolean[array.length];
        Subscription[] subs = new Subscription[array.length];
        for(int i=0;i<array.length;i++) {
            complete[i] = new AtomicBoolean(false);
        }
        Subscription winner[] ={null};

        ReactiveSubscriber<T> sub = Spouts.reactiveSubscriber();


        for(int i=0;i<array.length;i++){
            Publisher<T> next = (Publisher<T>)array[i];
            final int index= i;
            next.subscribe(new Subscriber<T>() {
                boolean won = false;

                @Override
                public void onSubscribe(Subscription s) {
                    subs[index] = s;

                }

                @Override
                public void onNext(T t) {

                    if (won) {
                        sub.onNext(t);
                    } else if (first.compareAndSet(0, index+1)) {
                        winner[0] = subs[index];
                        sub.onNext(t);
                        won = true;
                    }

                }

                @Override
                public void onError(Throwable t) {
                    complete[index].set(true);
                    if (won || othersComplete(index))
                        sub.onError(t);
                }

                @Override
                public void onComplete() {

                    complete[index].set(true);
                    if (won || othersComplete(index)) {
                        sub.onComplete();
                    }
                }

                boolean othersComplete(int avoid){
                    boolean allComplete = true;
                    for(int i=0;i<array.length;i++) {
                        if(i!=avoid) {
                            allComplete = allComplete && complete[i].get();
                            if(!allComplete)
                                return false;
                        }
                    }
                    return allComplete;
                }
            });
        }


        sub.onSubscribe(new StreamSubscription() {
            int count = 0;

            @Override
            public void request(long n) {
                if(count==0) {
                    for(int i=0;i<array.length;i++) {
                        subs[i].request(1l);
                    }


                    if(n-1>0)
                        super.request(n-1);
                    if(first.get()!=0){
                        count=2;
                    }else
                        count=1;
                }else if(count<2){
                    if(first.get()!=0){
                        count=2;
                    }
                    super.request(n);
                }
                else if(count==2){
                    if(requested.get()>0)
                        winner[0].request(requested.get());
                    winner[0].request(n);
                    count=2;
                }
                else{
                    winner[0].request(n);
                }
            }

            @Override
            public void cancel() {
                winner[0].cancel();
            }
        });
        return sub.reactiveStream();

    }
    static  ReactiveSeq<Integer> interval(String cron,ScheduledExecutorService exec) {
        ReactiveSubscriber<Integer> sub = reactiveSubscriber();
        AtomicBoolean isOpen = new AtomicBoolean(true);
        Subscription[] s= {null};
        sub.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                s[0].request(n);
            }

            @Override
            public void cancel() {
                isOpen.set(false);
            }
        });

        s[0] = ReactiveSeq.iterate(1, a -> a + 1)
                          .takeWhile(e -> isOpen.get())
                          .schedule(cron, exec)
                          .connect()
                          .forEach(1, e -> sub.onNext(e));

        return sub.reactiveStream();

    }
    static  ReactiveSeq<Integer> interval(final  long millis,ScheduledExecutorService exec) {
        ReactiveSubscriber<Integer> sub = reactiveSubscriber();
        AtomicBoolean isOpen = new AtomicBoolean(true);
        Subscription[] s= {null};
        sub.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                s[0].request(n);
            }

            @Override
            public void cancel() {
                isOpen.set(false);
            }
        });

        s[0] = ReactiveSeq.iterate(1, a -> a + 1)
                                    .takeWhile(e -> isOpen.get())
                                    .scheduleFixedDelay(millis, exec)
                                    .connect()
                                    .forEach(1, e -> sub.onNext(e));

        return sub.reactiveStream();

    }
    static <T> ReactiveSeq<T> defer(final Supplier<? extends Publisher<? extends T>> s){
        return of(s).mergeMap(i->i.get());
    }
    static <T> ReactiveSeq<T> deferFromStream(final Supplier<? extends Stream<? extends T>> s){
        return of(s).flatMap(i->i.get());
    }
    static <T> ReactiveSeq<T> deferFromIterable(final Supplier<? extends Iterable<? extends T>> s){
        return of(s).concatMap(i->i.get());
    }
    /**
     * Unfold a function into a ReactiveSeq
     *
     * <pre>
     * {@code
     *  ReactiveSeq.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     *
     * //(1,2,3,4,5)
     *
     * }</code>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return ReactiveSeq generated by unfolder function
     */
    static <U, T> ReactiveSeq<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return reactiveStream(new SpliteratorToOperator<T>(new UnfoldSpliterator<>(seed, unfolder)));
    }
    public static  <T> ReactiveSeq<T> concat(Publisher<Publisher<T>> pubs){

        return new ReactiveStreamX<>(new ArrayConcatonatingOperator<T>(ListX.fromPublisher(pubs)
                .map(p->new PublisherToOperator<T>(p))));
    }
    public static  <T> ReactiveSeq<T> lazyConcat(Publisher<Publisher<T>> pubs){

        return new ReactiveStreamX<>(new LazyArrayConcatonatingOperator<T>(ListX.fromPublisher(pubs)
                .map(p->new PublisherToOperator<T>(p))));
    }
    public static  <T> ReactiveSeq<T> concat(Stream<? extends T>... streams){
        Operator<T>[] operators = new Operator[streams.length];
        int index = 0;

        Type async = Type.SYNC;
        for(Stream<T> next : (Stream<T>[])streams){
            if(next instanceof ReactiveStreamX){
                ReactiveStreamX rsx = ((ReactiveStreamX) next);
                operators[index] = rsx.getSource();
                if(rsx.getType()== Type.BACKPRESSURE){
                    async = Type.BACKPRESSURE;
                }
                if(async== Type.SYNC && rsx.getType()== Type.NO_BACKPRESSURE){
                    async = Type.NO_BACKPRESSURE;
                }
            }else{
                operators[index] = new SpliteratorToOperator<T>(next.spliterator());
            }
            index++;
        }

        return new ReactiveStreamX<>(new ArrayConcatonatingOperator<T>(operators)).withAsync(async);
    }


    /**
     * Convert the raw Higher Kinded Type for ReactiveSeq types into the ReactiveSeq type definition class
     *
     * @param future HKT encoded list into a ReactiveSeq
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> narrowK(final Higher<reactiveSeq, T> future) {
        return (ReactiveSeq<T>) future;
    }


}
