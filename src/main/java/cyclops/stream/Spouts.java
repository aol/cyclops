package cyclops.stream;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.internal.stream.ReactiveStreamX;
import com.aol.cyclops2.internal.stream.spliterators.IteratePredicateSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.UnfoldSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.push.*;
import com.aol.cyclops2.types.stream.reactive.AsyncSubscriber;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import cyclops.Streams;
import cyclops.collections.ListX;
import cyclops.function.Monoid;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 14/01/2017.
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
    static <T> ReactiveSeq<T> async(Consumer<? super Subscriber<T>> sub){
        AsyncSubscriber<T> s = asyncSubscriber();
        sub.accept(s);
        return s.stream();
    }

    /**
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
        return new ReactiveStreamX<>(s).withAsync(ReactiveStreamX.Type.BACKPRESSURE);
    }
    static <T> ReactiveSeq<T> asyncStream(Operator<T> s){
        return new ReactiveStreamX<>(s).withAsync(ReactiveStreamX.Type.NO_BACKPRESSURE);
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
    static <T> ReactiveSeq<T> from(Publisher<T> pub){
        return new ReactiveStreamX<T>(new PublisherToOperator<T>(pub),ReactiveStreamX.Type.BACKPRESSURE);
    }
    static <T> ReactiveSeq<T> merge(Publisher<? extends Publisher<T>> publisher){
        return mergeLatest((Publisher[])Spouts.from(publisher).toArray());
    }
    static <T> ReactiveSeq<T> merge(Publisher<T>... array){
        Operator<T>[] op = new Operator[array.length];
        for(int i=0;i<array.length;i++){
            if(array[i] instanceof ReactiveStreamX){
                ReactiveStreamX<T> stream = (ReactiveStreamX<T>)array[i];
                op[i] = stream.getSource();
            }else{
                op[i] = new PublisherToOperator<T>(array[i]);
            }
        }
        return new ReactiveStreamX<T>(new ArrayMergingOperator<T>(op), ReactiveStreamX.Type.BACKPRESSURE);
    }

    static <T1,T2,R> ReactiveSeq<R> combineLatest(Publisher<? super T1> p1, Publisher<? super T2> p2,BiFunction<? super T1, ? super T2, ? extends R> fn){
        Operator<? super T1> op1 = p1 instanceof  ReactiveStreamX ? ((ReactiveStreamX<T1>)p1).getSource() :  new PublisherToOperator<T1>(p1);
        Operator<? super T2> op2 = p2 instanceof  ReactiveStreamX ? ((ReactiveStreamX<T2>)p2).getSource() :  new PublisherToOperator<T2>(p2);

        return new ReactiveStreamX<R>(new ZippingLatestOperator<T1,T2,R>(op1,op2,fn), ReactiveStreamX.Type.BACKPRESSURE);
    }
    static <T> ReactiveSeq<T> mergeLatest(Publisher<? extends Publisher<T>> publisher){
        return mergeLatest((Publisher[])ReactiveSeq.fromPublisher(publisher).toArray(s->new Publisher[s]));
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
        return new ReactiveStreamX<T>(new MergeLatestOperator<T>(op), ReactiveStreamX.Type.BACKPRESSURE);
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
                    } else if (first.compareAndSet(0, index)) {
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

    public static  <T> ReactiveSeq<T> concat(Stream<? extends T>... streams){
        Operator<T>[] operators = new Operator[streams.length];
        int index = 0;

        for(Stream<T> next : (Stream<T>[])streams){
            if(next instanceof ReactiveStreamX){
                operators[index] = ((ReactiveStreamX)next).getSource();
            }else{
                operators[index] = new SpliteratorToOperator<T>(next.spliterator());
            }
            index++;
        }

        return new ReactiveStreamX<>(new ArrayConcatonatingOperator<T>(operators));
    }

    static class Instances {
        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  ReactiveSeq<Integer> list = Lists.functor().map(i->i*2, ReactiveSeq.widen(Arrays.asList(1,2,3));
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
        .transform(h->Lists.functor().map((String v) ->v.length(), h))
        .convert(ReactiveSeq::narrowK);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Lists
         */
        public static <T,R>Functor<ReactiveSeq.µ> functor(){
            BiFunction<ReactiveSeq<T>,Function<? super T, ? extends R>,ReactiveSeq<R>> map = Spouts.Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * ReactiveSeq<String> list = Lists.unit()
        .unit("hello")
        .convert(ReactiveSeq::narrowK);

        //Arrays.asList("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Lists
         */
        public static <T> Pure<ReactiveSeq.µ> unit(){
            return General.<ReactiveSeq.µ,T>unit(Spouts.Instances::of);
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
         *                                                  .convert(ReactiveSeq::narrowK);

        ReactiveSeq<Integer> list = Lists.unit()
        .unit("hello")
        .transform(h->Lists.functor().map((String v) ->v.length(), h))
        .transform(h->Lists.zippingApplicative().ap(listFn, h))
        .convert(ReactiveSeq::narrowK);

        //Arrays.asList("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Lists
         */
        public static <T,R> Applicative<ReactiveSeq.µ> zippingApplicative(){
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
        .convert(ReactiveSeq::narrowK);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    ReactiveSeq<Integer> list = Lists.unit()
        .unit("hello")
        .transform(h->Lists.monad().flatMap((String v) ->Lists.unit().unit(v.length()), h))
        .convert(ReactiveSeq::narrowK);

        //Arrays.asList("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Lists
         */
        public static <T,R> Monad<ReactiveSeq.µ> monad(){

            BiFunction<Higher<ReactiveSeq.µ,T>,Function<? super T, ? extends Higher<ReactiveSeq.µ,R>>,Higher<ReactiveSeq.µ,R>> flatMap = Spouts.Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  ReactiveSeq<String> list = Lists.unit()
        .unit("hello")
        .transform(h->Lists.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(ReactiveSeq::narrowK);

        //Arrays.asList("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<ReactiveSeq.µ> monadZero(){

            return General.monadZero(monad(), ReactiveSeq.empty());
        }
        /**
         * <pre>
         * {@code
         *  ReactiveSeq<Integer> list = Lists.<Integer>monadPlus()
        .plus(ReactiveSeq.widen(Arrays.asList()), ReactiveSeq.widen(Arrays.asList(10)))
        .convert(ReactiveSeq::narrowK);
        //Arrays.asList(10))
         *
         * }
         * </pre>
         * @return Type class for combining Lists by concatenation
         */
        public static <T> MonadPlus<ReactiveSeq.µ> monadPlus(){
            Monoid<ReactiveSeq<T>> m = Monoid.of(ReactiveSeq.empty(), Spouts.Instances::concat);
            Monoid<Higher<ReactiveSeq.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<ReactiveSeq<Integer>> m = Monoid.of(ReactiveSeq.widen(Arrays.asList()), (a,b)->a.isEmpty() ? b : a);
        ReactiveSeq<Integer> list = Lists.<Integer>monadPlus(m)
        .plus(ReactiveSeq.widen(Arrays.asList(5)), ReactiveSeq.widen(Arrays.asList(10)))
        .convert(ReactiveSeq::narrowK);
        //Arrays.asList(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Lists
         * @return Type class for combining Lists
         */
        public static <T> MonadPlus<ReactiveSeq.µ> monadPlus(Monoid<ReactiveSeq<T>> m){
            Monoid<Higher<ReactiveSeq.µ,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<ReactiveSeq.µ> traverse(){
            BiFunction<Applicative<C2>,ReactiveSeq<Higher<C2, T>>,Higher<C2, ReactiveSeq<T>>> sequenceFn = (ap,list) -> {

                Higher<C2,ReactiveSeq<T>> identity = ap.unit(Spouts.empty());

                BiFunction<Higher<C2,ReactiveSeq<T>>,Higher<C2,T>,Higher<C2,ReactiveSeq<T>>> combineToList =   (acc,next) -> ap.apBiFn(ap.unit((a,b) -> { a.append(b); return a;}),acc,next);

                BinaryOperator<Higher<C2,ReactiveSeq<T>>> combineLists = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> { l1.appendS(l2); return l1;}),a,b); ;

                return list.stream()
                        .reduce(identity,
                                combineToList,
                                combineLists);


            };
            BiFunction<Applicative<C2>,Higher<ReactiveSeq.µ,Higher<C2, T>>,Higher<C2, Higher<ReactiveSeq.µ,T>>> sequenceNarrow  =
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
        public static <T> Foldable<ReactiveSeq.µ> foldable(){
            BiFunction<Monoid<T>,Higher<ReactiveSeq.µ,T>,T> foldRightFn =  (m,l)-> narrow(l).foldRight(m);
            BiFunction<Monoid<T>,Higher<ReactiveSeq.µ,T>,T> foldLeftFn = (m,l)-> narrow(l).reduce(m);
            return General.foldable(foldRightFn, foldLeftFn);
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
        private static <T,R> Higher<ReactiveSeq.µ,R> flatMap( Higher<ReactiveSeq.µ,T> lt, Function<? super T, ? extends  Higher<ReactiveSeq.µ,R>> fn){
            return ReactiveSeq.narrowK(lt).flatMap(fn.andThen(ReactiveSeq::narrowK));
        }
        private static <T,R> ReactiveSeq<R> map(ReactiveSeq<T> lt, Function<? super T, ? extends R> fn){
            return lt.map(fn);
        }



        /**
         * Widen a ReactiveSeq nested inside another HKT encoded type
         *
         * @param flux HTK encoded type containing  a List to widen
         * @return HKT encoded type with a widened List
         */
        public static <C2, T> Higher<C2, Higher<ReactiveSeq.µ, T>> widen2(Higher<C2, ReactiveSeq<T>> flux) {
            // a functor could be used (if C2 is a functor / one exists for C2 type)
            // instead of casting
            // cast seems safer as Higher<ReactiveSeq.µ,T> must be a ReactiveSeq
            return (Higher) flux;
        }





        /**
         * Convert the HigherKindedType definition for a List into
         *
         * @param List Type Constructor to convert back into narrowed type
         * @return List from Higher Kinded Type
         */
        public static <T> ReactiveSeq<T> narrow(final Higher<ReactiveSeq.µ, T> completableList) {

            return ((ReactiveSeq<T>) completableList);//.narrow();

        }
    }
    /**
     * Convert the raw Higher Kinded Type for ReactiveSeq types into the ReactiveSeq type definition class
     *
     * @param future HKT encoded list into a ReactiveSeq
     * @return ReactiveSeq
     */
    public static <T> ReactiveSeq<T> narrowK(final Higher<ReactiveSeq.µ, T> future) {
        return (ReactiveSeq<T>) future;
    }


}
