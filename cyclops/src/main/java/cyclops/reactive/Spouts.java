package cyclops.reactive;

import com.oath.cyclops.hkt.Higher;

import com.oath.cyclops.internal.stream.spliterators.push.*;
import com.oath.cyclops.types.reactive.BufferOverflowPolicy;
import com.oath.cyclops.types.reactive.PushSubscriber;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.*;
import com.oath.cyclops.internal.stream.ReactiveStreamX;
import com.oath.cyclops.internal.stream.ReactiveStreamX.Type;
import com.oath.cyclops.internal.stream.spliterators.UnfoldSpliterator;
import com.oath.cyclops.types.reactive.AsyncSubscriber;
import com.oath.cyclops.types.reactive.ReactiveSubscriber;
import cyclops.data.Seq;
import com.oath.cyclops.hkt.DataWitness.reactiveSeq;

import cyclops.function.checked.CheckedSupplier;
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
 * reactive : is used to denote creational methods for reactive-streams that support non-blocking backpressure
 * async : is used to denote creational methods for asynchronous streams that do not support backpressure
 */

public interface Spouts {



    static <T> ReactiveSeq<T> once(CheckedSupplier<T> cs){
        return Spouts.generate(ExceptionSoftener.softenSupplier(cs)).take(1l);
    }

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
     * reactive-streams susbscription is ignored (i.e. this Stream is backpressure free)
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
     *     Subscription sub = Spouts.reactive(10, s -> {
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
        return reactiveStream(new PublisherToOperator<T>(new Publisher<T>() {


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
     *   that implements backpressure internally via the reactive-streams spec.
     *
     *   Subscribers signal demand via their subscription and publishers push data to subscribers
     *   synchronously or asynchronously, never exceeding signalled demand
     *
     * @param <T> Stream data type
     * @return An async Stream Subscriber that supports efficient backpressure via reactive-streams
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
        return new ReactiveStreamX<>(s,Type.BACKPRESSURE);
    }
    static <T> ReactiveSeq<T> asyncStream(Operator<T> s){
        return new ReactiveStreamX<>(s,Type.NO_BACKPRESSURE);
    }
    static <T> ReactiveSeq<T> syncStream(Operator<T> s){
        return new ReactiveStreamX<>(s);
    }

    static <T> ReactiveSeq<T> iterate(final T seed, final UnaryOperator<T> f) {
        return syncStream(new IterateOperator<T>(seed,f));
    }
    static <T> ReactiveSeq<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return syncStream(new IteratePredicateOperator<T>(seed,f,pred));

    }
    public static ReactiveSeq<Integer> range(int start, int end){
        if(start<end)
            return syncStream(new RangeIntOperator(start,end));
        else
            return syncStream(new RangeIntOperator(end,start));
    }
    public static  ReactiveSeq<Long> rangeLong(long start, long end){
        if(start<end)
            return syncStream(new RangeLongOperator(start,end));
        else
            return syncStream(new RangeLongOperator(end,start));
    }
    public static  <T> ReactiveSeq<T> of(T value){
        return syncStream(new SingleValueOperator<T>(value));
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
        return syncStream(new ArrayOfValuesOperator<T>(values));
    }
    public static  <T> ReactiveSeq<T> fromIterable(Iterable<T> iterable){
        if(iterable instanceof ReactiveStreamX)
            return (ReactiveSeq<T>)iterable;
        return syncStream(new IterableSourceOperator<T>(iterable));
    }
    public static  <T> ReactiveSeq<T> fromSpliterator(Spliterator<T> spliterator){
        return syncStream(new SpliteratorToOperator<T>(spliterator));
    }
    /**
     * @see Stream#generate(Supplier)
     */
    static <T> ReactiveSeq<T> generate(final Supplier<T> s) {
        return syncStream(new GenerateOperator<T>(s));

    }
    static <T> ReactiveSeq<T> from(Publisher<? extends T> pub){
        if(pub instanceof ReactiveSeq)
            return (ReactiveSeq<T>)pub;
        return reactiveStream(new PublisherToOperator<T>((Publisher<T>)pub));
    }
    @Deprecated //use mergeLatest
    static <T> ReactiveSeq<T> merge(Publisher<? extends Publisher<T>> publisher){
        return mergeLatest(publisher);
    }



    static <T> ReactiveSeq<T> mergeLatestList(Seq<? extends Publisher<? extends T>> publisher){
        return mergeLatest((Publisher[])publisher.toArray(s->new Publisher[s]));
    }
    static <T> ReactiveSeq<T> mergeLatest(Publisher<? extends Publisher<T>> publisher){
        return Spouts.from(publisher).mergeMap(Integer.MAX_VALUE,i->i);
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
        return reactiveStream(new MergeLatestOperator<T>(op));


    }
    static <T> ReactiveSeq<T> amb(IterableX<? extends Publisher<? extends T>> list){
        return amb(list.toArray(i->new ReactiveSeq[i]));
    }
    static <T> ReactiveSeq<T> amb(Publisher<? extends T>... array){
        return ambWith(array);
    }
    static <T> ReactiveSeq<T> ambWith(Publisher<? extends T>[] array){
       return  reactiveStream(new AmbOperator<T>((Publisher<T>[])array));

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
    static  <T> ReactiveSeq<T> schedule(final Stream<T> stream,final String cron,final ScheduledExecutorService exec) {
        ReactiveSubscriber<T> sub = reactiveSubscriber();
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


        s[0] = ReactiveSeq.fromStream(stream)
                            .takeWhile(e -> isOpen.get())
                            .schedule(cron, exec)
                            .connect()
                            .forEach(0, e -> sub.onNext(e),t->sub.onError(t),()->sub.onComplete());


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

        return reactiveStream(new ArrayConcatonatingOperator<T>(Spouts.from(pubs).seq()
                .map(p->new PublisherToOperator<T>(p))));
    }
    public static  <T> ReactiveSeq<T> lazyConcat(Publisher<Publisher<T>> pubs){

        return reactiveStream(new LazyArrayConcatonatingOperator<T>(Spouts.from(pubs).seq()
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
