package cyclops.stream;

import com.aol.cyclops2.hkt.Higher;

import com.aol.cyclops2.types.reactive.BufferOverflowPolicy;
import com.aol.cyclops2.types.reactive.PushSubscriber;
import cyclops.control.Xor;
import cyclops.function.Function3;
import cyclops.typeclasses.InstanceDefinitions;
import com.aol.cyclops2.internal.stream.ReactiveStreamX;
import com.aol.cyclops2.internal.stream.ReactiveStreamX.Type;
import com.aol.cyclops2.internal.stream.spliterators.UnfoldSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.push.*;
import com.aol.cyclops2.types.reactive.AsyncSubscriber;
import com.aol.cyclops2.types.reactive.ReactiveSubscriber;
import cyclops.async.Future;
import cyclops.collections.mutable.ListX;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import cyclops.collections.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
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
        return Spouts.of(array).flatMapP(maxConcurrency,i->i);
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
    static <T> ReactiveSeq<T> deferred(final Supplier<? extends Publisher<? extends T>> s){
        return of(s).flatMapP(i->i.get());
    }
    static <T> ReactiveSeq<T> deferredS(final Supplier<? extends Stream<? extends T>> s){
        return of(s).flatMap(i->i.get());
    }
    static <T> ReactiveSeq<T> deferredI(final Supplier<? extends Iterable<? extends T>> s){
        return of(s).flatMapI(i->i.get());
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
    static <U, T> ReactiveSeq<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
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

    static class Instances {
        public static InstanceDefinitions<reactiveSeq> definitions(Executor ex){
            return new InstanceDefinitions<reactiveSeq>() {
                @Override
                public <T, R> Functor<reactiveSeq> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<reactiveSeq> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<reactiveSeq> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<reactiveSeq> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<reactiveSeq>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<reactiveSeq>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<reactiveSeq> monadRec() {
                    return Instances.monadRec(ex);
                }

                @Override
                public <T> Maybe<MonadPlus<reactiveSeq>> monadPlus(Monoid<Higher<reactiveSeq, T>> m) {
                    return Maybe.just(Instances.monadPlus((Monoid)m));
                }

                @Override
                public <C2, T> Traverse<reactiveSeq> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<reactiveSeq> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<reactiveSeq>> comonad() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<Unfoldable<reactiveSeq>> unfoldable() {
                    return Maybe.just(Instances.unfoldable(ex));
                }
            };
        }
        public static Unfoldable<reactiveSeq> unfoldable(Executor ex){
            return new Unfoldable<reactiveSeq>() {
                @Override
                public <R, T> Higher<reactiveSeq, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return Spouts.reactive(Spouts.unfold(b,fn),ex);
                }
            };
        }
        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  ReactiveSeq<Integer> list = Lists.functor().transform(i->i*2, ReactiveSeq.widen(Arrays.asList(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Lists
         * <pre>
         * {@code
         *   ReactiveSeq<Integer> list = ReactiveSeq.Instances.unit()
        .unit("hello")
        .transform(h->Lists.functor().transform((String v) ->v.length(), h))
        .convert(ReactiveSeq::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Lists
         */
        public static <T,R>Functor<reactiveSeq> functor(){
            BiFunction<ReactiveSeq<T>,Function<? super T, ? extends R>,ReactiveSeq<R>> map = Spouts.Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * ReactiveSeq<String> list = Lists.unit()
        .unit("hello")
        .convert(ReactiveSeq::narrowK3);

        //Arrays.asList("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Lists
         */
        public static <T> Pure<reactiveSeq> unit(){
            return General.<reactiveSeq,T>unit(Spouts.Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops2.hkt.jdk.ReactiveSeq.widen;
         * import static com.aol.cyclops2.util.function.Lambda.l1;
         * import static java.util.Arrays.asList;
         *
        Lists.zippingApplicative()
        .ap(widen(asList(l1(this::multiplyByTwo))),widen(asList(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * ReactiveSeq<Function<Integer,Integer>> listFn =Lists.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(ReactiveSeq::narrowK3);

        ReactiveSeq<Integer> list = Lists.unit()
        .unit("hello")
        .transform(h->Lists.functor().transform((String v) ->v.length(), h))
        .transform(h->Lists.zippingApplicative().ap(listFn, h))
        .convert(ReactiveSeq::narrowK3);

        //Arrays.asList("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Lists
         */
        public static <T,R> Applicative<reactiveSeq> zippingApplicative(){
            BiFunction<ReactiveSeq< Function<T, R>>,ReactiveSeq<T>,ReactiveSeq<R>> ap = Spouts.Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops2.hkt.jdk.ReactiveSeq.widen;
         * ReactiveSeq<Integer> list  = Lists.monad()
        .flatMap(i->widen(ReactiveSeq.range(0,i)), widen(Arrays.asList(1,2,3)))
        .convert(ReactiveSeq::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    ReactiveSeq<Integer> list = Lists.unit()
        .unit("hello")
        .transform(h->Lists.monad().flatMap((String v) ->Lists.unit().unit(v.length()), h))
        .convert(ReactiveSeq::narrowK3);

        //Arrays.asList("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Lists
         */
        public static <T,R> Monad<reactiveSeq> monad(){

            BiFunction<Higher<reactiveSeq,T>,Function<? super T, ? extends Higher<reactiveSeq,R>>,Higher<reactiveSeq,R>> flatMap = Spouts.Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  ReactiveSeq<String> list = Lists.unit()
        .unit("hello")
        .transform(h->Lists.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(ReactiveSeq::narrowK3);

        //Arrays.asList("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<reactiveSeq> monadZero(){

            return General.monadZero(monad(), ReactiveSeq.empty());
        }
        /**
         * <pre>
         * {@code
         *  ReactiveSeq<Integer> list = Lists.<Integer>monadPlus()
        .plus(ReactiveSeq.widen(Arrays.asList()), ReactiveSeq.widen(Arrays.asList(10)))
        .convert(ReactiveSeq::narrowK3);
        //Arrays.asList(10))
         *
         * }
         * </pre>
         * @return Type class for combining Lists by concatenation
         */
        public static <T> MonadPlus<reactiveSeq> monadPlus(){
            Monoid<ReactiveSeq<T>> m = Monoid.of(ReactiveSeq.empty(), Spouts.Instances::concat);
            Monoid<Higher<reactiveSeq,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<ReactiveSeq<Integer>> m = Monoid.of(ReactiveSeq.widen(Arrays.asList()), (a,b)->a.isEmpty() ? b : a);
        ReactiveSeq<Integer> list = Lists.<Integer>monadPlus(m)
        .plus(ReactiveSeq.widen(Arrays.asList(5)), ReactiveSeq.widen(Arrays.asList(10)))
        .convert(ReactiveSeq::narrowK3);
        //Arrays.asList(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Lists
         * @return Type class for combining Lists
         */
        public static <T> MonadPlus<reactiveSeq> monadPlus(Monoid<ReactiveSeq<T>> m){
            Monoid<Higher<reactiveSeq,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static <T,R> MonadRec<reactiveSeq> monadRec(Executor ex){

            return new MonadRec<reactiveSeq>(){
                @Override
                public <T, R> Higher<reactiveSeq, R> tailRec(T initial, Function<? super T, ? extends Higher<reactiveSeq,? extends Xor<T, R>>> fn) {
                    return  Spouts.reactive(ReactiveSeq.deferred( ()-> ReactiveSeq.tailRec(initial, fn.andThen(ReactiveSeq::narrowK))),ex);

                }
            };
        }
        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<reactiveSeq> traverse(){
            BiFunction<Applicative<C2>,ReactiveSeq<Higher<C2, T>>,Higher<C2, ReactiveSeq<T>>> sequenceFn = (ap,list) -> {

                Higher<C2,ReactiveSeq<T>> identity = ap.unit(Spouts.empty());

                BiFunction<Higher<C2,ReactiveSeq<T>>,Higher<C2,T>,Higher<C2,ReactiveSeq<T>>> combineToList =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.append(b); return a;}),acc,next);

                BinaryOperator<Higher<C2,ReactiveSeq<T>>> combineLists = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> { l1.appendS(l2); return l1;}),a,b); ;

                return list.stream()
                        .reduce(identity,
                                combineToList,
                                combineLists);


            };
            BiFunction<Applicative<C2>,Higher<reactiveSeq,Higher<C2, T>>,Higher<C2, Higher<reactiveSeq,T>>> sequenceNarrow  =
                    (a,b) -> ReactiveSeq.Instances.widen2(sequenceFn.apply(a, ReactiveSeq.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = Lists.foldable()
        .foldLeft(0, (a,b)->a+b, ReactiveSeq.of(1,2,3,4));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T,R> Foldable<reactiveSeq> foldable(){
            BiFunction<Monoid<T>,Higher<reactiveSeq,T>,T> foldRightFn =  (m,l)-> narrow(l).foldRight(m);
            BiFunction<Monoid<T>,Higher<reactiveSeq,T>,T> foldLeftFn = (m,l)-> narrow(l).reduce(m);
            Function3<Monoid<R>, Function<T, R>, Higher<reactiveSeq, T>, R> foldMapFn = (m, f, l)->narrowK(l).map(f).foldLeft(m);
            return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
        }

        private static  <T> ReactiveSeq<T> concat(ReactiveSeq<T> l1, ReactiveSeq<T> l2){
            return Spouts.concat(l1.stream(),l2.stream());
        }
        private static <T> ReactiveSeq<T> of(T value){
            return Spouts.of(value);
        }
        private static <T,R> ReactiveSeq<R> ap(ReactiveSeq<Function< T, R>> lt,  ReactiveSeq<T> list){
            return lt.zip(list,(a,b)->a.apply(b));
        }
        private static <T,R> Higher<reactiveSeq,R> flatMap( Higher<reactiveSeq,T> lt, Function<? super T, ? extends  Higher<reactiveSeq,R>> fn){
            return ReactiveSeq.narrowK(lt).flatMap(fn.andThen(ReactiveSeq::narrowK));
        }
        private static <T,R> ReactiveSeq<R> map(ReactiveSeq<T> lt, Function<? super T, ? extends R> fn){
            return lt.map(fn);
        }



        /**
         * Widen a ReactiveSeq nest inside another HKT encoded type
         *
         * @param flux HTK encoded type containing  a List to widen
         * @return HKT encoded type with a widened List
         */
        public static <C2, T> Higher<C2, Higher<reactiveSeq, T>> widen2(Higher<C2, ReactiveSeq<T>> flux) {
            // a functor could be used (if C2 is a functor / one exists for C2 type)
            // instead of casting
            // cast seems safer as Higher<reactiveSeq,T> must be a ReactiveSeq
            return (Higher) flux;
        }





        /**
         * Convert the HigherKindedType definition for a List into
         *
         * @param List Type Constructor to convert back into narrowed type
         * @return List from Higher Kinded Type
         */
        public static <T> ReactiveSeq<T> narrow(final Higher<reactiveSeq, T> completableList) {

            return ((ReactiveSeq<T>) completableList);//.narrow();

        }
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
