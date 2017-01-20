package com.aol.cyclops2.internal.stream;

import com.aol.cyclops2.internal.stream.spliterators.push.*;
import com.aol.cyclops2.types.Traversable;
import com.aol.cyclops2.types.stream.HotStream;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.Streams;
import cyclops.async.*;
import cyclops.collections.ListX;
import cyclops.collections.immutable.PVectorX;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.aol.cyclops2.internal.comprehensions.comprehenders.StreamAdapter.stream;


@AllArgsConstructor
public class ReactiveStreamX<T> extends BaseExtendedStream<T> {

    @Getter
    final Operator<T> source;
    @Wither
    final Consumer<? super Throwable> defaultErrorHandler;

    @Wither
    final Type async;

    public static enum Type {SYNC, BACKPRESSURE, NO_BACKPRESSURE}

    public ReactiveStreamX(Operator<T> source){
        this.source = source;
        this.defaultErrorHandler = e->{ throw ExceptionSoftener.throwSoftenedException(e);};
        this.async = Type.SYNC;
    }
    @Override
    public ReactiveSeq<T> reverse() {
        return coflatMap(s->ReactiveSeq.reversedListOf(s.toList()))
                .flatMap(i->i);
    }


    @Override
    public ReactiveSeq<T> reduceAll(T identity, BinaryOperator<T> accumulator){
        return createSeq(new ReduceAllOperator<>(source,identity,accumulator));
    }
    @Override
    public <R, A> ReactiveSeq<R> collectAll(Collector<? super T, A, R> collector){
        return createSeq(new CollectAllOperator<T,A,R>(source,collector));
    }
    @Override
    public Iterator<T> iterator() {
        if(async==Type.NO_BACKPRESSURE){

            cyclops.async.Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
                    .build();

            this.source.subscribe(queue::offer,i->queue.close(),queue::close);
            return queue.stream().iterator();

        }
        return new OperatorToIterable<>(source,this.defaultErrorHandler,async==Type.BACKPRESSURE).iterator();
    }

    <X> ReactiveSeq<X> createSeq(Operator<X> stream) {
        return new ReactiveStreamX<X>(stream,defaultErrorHandler,async);
    }


    public  <R> ReactiveSeq<R> coflatMap(Function<? super ReactiveSeq<T>, ? extends R> fn){
        return createSeq(new LazySingleValueOperator<ReactiveSeq<T>,R>(createSeq( source),fn));

    }

    static final Object UNSET = new Object();
    @Override
    public final Optional<T> findFirst() {
        final AtomicReference<T> result = new AtomicReference<T>(null);
        final AtomicBoolean available = new AtomicBoolean(false);
        final AtomicReference<Throwable> error = new AtomicReference<>(null);

            Subscription sub[] = {null};
            //may be quicker to use subscribeAll and throw an Exception with fillInStackTrace overriden
            sub[0] = source.subscribe(e -> {
                    result.set(e);
                    sub[0].cancel();
                    available.set(true);

            },e->{
                error.set(e);
                sub[0].cancel();
                available.set(true);
            },()->available.set(true));
        sub[0].request(1l);
        while(!available.get()){

        }
        if(error.get()!=null)
            throw ExceptionSoftener.throwSoftenedException(error.get());


        return Optional.ofNullable(result.get());
    }



    @Override
    public final ReactiveSeq<PVectorX<T>> sliding(final int windowSize, final int increment) {
        return createSeq(new SlidingOperator<>( source,Function.identity(), windowSize,increment));
    }

    @Override
    public ReactiveSeq<ListX<T>> grouped(final int groupSize) {
        return createSeq(new GroupingOperator<T,List<T>,ListX<T>>( source,()->new ArrayList(groupSize), c->ListX.fromIterable(c),groupSize));

    }
    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyWhile(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return createSeq(new GroupedStatefullyOperator<>( source,()->ListX.of(),Function.identity(), predicate));
    }
    @Override
    public <C extends Collection<T>,R> ReactiveSeq<R> groupedStatefullyWhile(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                                             Function<? super C, ? extends R> finalizer) {
        return this.<R>createSeq(new GroupedStatefullyOperator<>( source,factory,finalizer, predicate));
    }
    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return createSeq(new GroupedStatefullyOperator<>( source,()->ListX.of(),Function.identity(), predicate.negate()));
    }
    @Override
    public <C extends Collection<T>,R> ReactiveSeq<R> groupedStatefullyUntil(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                                             Function<? super C, ? extends R> finalizer) {
        return this.<R>createSeq(new GroupedStatefullyOperator<>( source,factory,finalizer, predicate.negate()));
    }

    @Override
    public final ReactiveSeq<T> distinct() {

        Supplier<Predicate<? super T>> predicate = ()->{
            Set<T> values = new HashSet<>();
            return in-> values.add(in);
        };
        return this.filterLazyPredicate(predicate);
    }

    @Override
    public final ReactiveSeq<T> scanLeft(final Monoid<T> monoid) {
        return scanLeft(monoid.zero(),monoid);

    }

    @Override
    public final <U> ReactiveSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        Supplier<Function<? super T, ? extends Object>> scanLeft = () -> {
             Object[] current = {seed};
             return in-> current[0]=function.apply((U)current[0],in);
        };

        return createSeq(new ArrayConcatonatingOperator<>(new SingleValueOperator<U>(seed),
                        extract((ReactiveSeq<U>)mapLazyFn(scanLeft))));


    }

    private <U> Operator<U> extract(ReactiveSeq<U> seq){
        return ((ReactiveStreamX<U>)seq).source;
    }


    @Override
    public final ReactiveSeq<T> skipWhile(final Predicate<? super T> p) {
        return createSeq(new SkipWhileOperator<>( source,p));
    }


    @Override
    public final ReactiveSeq<T> limitWhile(final Predicate<? super T> p) {
        return createSeq(new LimitWhileOperator<>( source, p));
    }

    @Override
    public final ReactiveSeq<T> limitUntil(final Predicate<? super T> p) {
        return limitWhile(p.negate());
    }



    @Override
    public ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new SkipWhileOperator<>( source,predicate) );
    }

    @Override
    public ReactiveSeq<T> limitWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new LimitWhileClosedOperator<>( source,predicate));
    }

    @Override
    public String format() {
        return null;
    }


    @Override
    public final <R> ReactiveSeq<R> map(final Function<? super T, ? extends R> fn) {


        return createSeq(new MapOperator<T,R>(this. source,fn));
    }


    @Override
    public final <R> ReactiveSeq<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> fn) {

        return createSeq(new FlatMapOperator<>( source,fn));

    }

    @Override
    public final <R> ReactiveSeq<R> flatMapAnyM(final Function<? super T, AnyM<Witness.stream,? extends R>> fn) {
        return createSeq(Streams.flatMapAnyM(this, fn));
    }

    @Override
    public final <R> ReactiveSeq<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> fn) {

        return createSeq(new IterableFlatMapOperator<>( source,fn));

    }
    @Override
    public final <R> ReactiveSeq<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> fn) {

        return createSeq(new PublisherFlatMapOperator<>( source,fn));
    }


    @Override
    public final ReactiveSeq<T> filter(final Predicate<? super T> fn) {
        return createSeq(new FilterOperator<T>( source,fn));

    }

    @Override
    public Spliterator<T> spliterator() {
        return unwrapStream().spliterator();
    }

    @Override
    public Stream<T> unwrapStream() {
        if(async==Type.NO_BACKPRESSURE){
           // System.out.println("Setting up queue..");
            cyclops.async.Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
                    .build();

            this.source.subscribe(queue::offer,i->queue.close(),queue::close);
            return queue.stream();

        }
        //return StreamSupport.stream(new OperatorToIterable<>(source,this.defaultErrorHandler,async==Type.BACKPRESSURE).spliterator(),false);
        return StreamSupport.stream(new OperatorToIterable<>(source,this.defaultErrorHandler,true).spliterator(),false);
    }

    @Override
    protected <R> ReactiveSeq<R> createSeq(Stream<R> rStream) {
        if(stream instanceof ReactiveSeq)
            return (ReactiveSeq)rStream;
        return new ReactiveStreamX<>(new SpliteratorToOperator<>(rStream.spliterator()));
    }

    @Override
    public <R> ReactiveSeq<R> mapLazyFn(Supplier<Function<? super T, ? extends R>> fn) {
        return createSeq(new LazyMapOperator<>(source,fn));
    }

    public final ReactiveSeq<T> filterLazyPredicate(final Supplier<Predicate<? super T>> fn) {
        return createSeq(new LazyFilterOperator<T>( source,fn));

    }



    @Override
    public void forEach(final Consumer<? super T> action) {
        if(async==Type.BACKPRESSURE){
            this.source.subscribe(action, this.defaultErrorHandler, () -> {
            }).request(Long.MAX_VALUE);
        }else {
            this.source.subscribeAll(action, this.defaultErrorHandler, () -> {
            });
        }

    }
    @Override
    public long count() {

        long[] result = {0};
        forEach(t -> result[0]++);
        return result[0];


    }

    @Override //TODO can be replaced by a dedicated Spliterator that keeps an index
    public  ReactiveSeq<T> insertAt(final int pos, final T... values) {
        Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> s = this.splitAt(pos);
        return ReactiveSeq.concat(s.v1,ReactiveSeq.of(values),s.v2);

    }

    @Override //TODO can be replaced by a dedicated Spliterator that keeps an index
    public ReactiveSeq<T> deleteBetween(final int start, final int end) {
        Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> s = duplicate();
        return ReactiveSeq.concat(s.v1.limit(start),s.v2.skip(end));
    }

    @Override //TODO can be replaced by a dedicated Spliterator that keeps an index
    public ReactiveSeq<T> insertAtS(final int pos, final Stream<T> stream) {
        Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> s = this.splitAt(pos);
        return ReactiveSeq.concat(s.v1,ReactiveSeq.fromStream(
                stream),s.v2);

    }

    @Override
    public <U> Traversable<U> unitIterator(Iterator<U> it) {
        Iterable<U> iterable = ()->it;
        return createSeq(new SpliteratorToOperator<U>(iterable.spliterator()));
    }

    @Override
    public void subscribe(final Subscriber<? super T> sub) {
        sub.onSubscribe(source.subscribe(sub::onNext,sub::onError,sub::onComplete));
    }



    @Override
    public ReactiveSeq<T> onEmpty(final T value) {
        return createSeq(new OnEmptyOperator<T>(source,()->value));

    }
    @Override
    public ReactiveSeq<T> onEmptySwitch(final Supplier<? extends Stream<T>> switchTo) {
        final Object value = new Object();
        ReactiveSeq res = createSeq(onEmptyGet((Supplier) () ->value).flatMap(s -> {
            if (s==value)
                return (Stream) switchTo.get();
            return Stream.of(s);
        }));
        return res;
    }

    @Override
    public ReactiveSeq<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return createSeq(new OnEmptyOperator<T>(source,supplier));
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return createSeq(new OnEmptyOperator<T>(source,()->{throw ExceptionSoftener.throwSoftenedException(supplier.get());}));
    }
    @Override
    public ReactiveSeq<T> appendS(final Stream<? extends T> other) {
        return Spouts.concat( this,(Stream<T>)other);
    }
    public ReactiveSeq<T> append(final Iterable<? extends T> other) {
        return Spouts.concat( this,(Stream<T>)ReactiveSeq.fromIterable(other));
    }

    //TODO use spliterators and createSeq
    @Override
    public ReactiveSeq<T> append(final T other) {
        return Spouts.concat( this,Spouts.of(other));
    }

    @Override
    public ReactiveSeq<T> append(final T... other) {
        return ReactiveSeq.concat( this,Spouts.of(other));
    }
    @Override
    public ReactiveSeq<T> prependS(final Stream<? extends T> other) {
        return ReactiveSeq.concat((Stream<T>)(other), this);
    }
    public ReactiveSeq<T> prepend(final Iterable<? extends T> other) {
        return ReactiveSeq.concat((Stream<T>)(other),this);
    }

    @Override
    public ReactiveSeq<T> prepend(final T other) {
        return Spouts.concat(Spouts.of(other), this);
    }

    @Override
    public ReactiveSeq<T> prepend(final T... other) {
        return Spouts.concat(Spouts.of(other), this);
    }


    @Override
    public <U> ReactiveSeq<T> distinct(final Function<? super T, ? extends U> keyExtractor) {
        Supplier<Predicate<? super T>> predicate = ()->{
            Set<U> values = new HashSet<>();
            return in-> values.add(keyExtractor.apply(in));
        };
        return this.filterLazyPredicate(predicate);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedBySizeAndTime(final int size, final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeAndSizeOperator<>(this. source,()->ListX.fromIterable(new ArrayList<>(size)),
                        Function.identity(),time,t,size)
               );

    }

    @Override
    public ReactiveSeq<ListX<T>> groupedByTime(final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeOperator<>( source,
                ()->ListX.fromIterable(new ArrayList<>(100)),
                Function.identity(),time, t));
    }
    @Override
    public ReactiveSeq<T> skip(final long time, final TimeUnit unit) {
        return createSeq(new SkipWhileTimeOperator<>( source, time, unit));
    }

    @Override
    public ReactiveSeq<T> limit(final long time, final TimeUnit unit) {
        return createSeq(new LimitWhileTimeOperator<>( source,time,unit));

    }

    @Override
    public ReactiveSeq<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return createSeq(new GroupedWhileOperator<>( source,()->ListX.of(),Function.identity(), predicate));


    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return createSeq(new GroupedWhileOperator<>( source,factory,Function.identity(), predicate));
    }


    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
                                                                                 final Supplier<C> factory) {
        return createSeq(new GroupedByTimeAndSizeOperator(this. source,factory,
                        Function.identity(),time,unit,size));

    }

    @Override
    public <C extends Collection<? super T>,R> ReactiveSeq<R> groupedBySizeAndTime(final int size, final long time,
                                                                                   final TimeUnit unit,
                                                                                   final Supplier<C> factory,
                                                                                   Function<? super C, ? extends R> finalizer
    ) {
        return createSeq(new GroupedByTimeAndSizeOperator(this. source,factory,
                        finalizer,time,unit,size)
               );

    }
    @Override
    public <C extends Collection<? super T>,R> ReactiveSeq<R> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return createSeq(new GroupedByTimeOperator(this. source,factory,
                        finalizer,time,unit)
               );

    }
    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return createSeq(new GroupedByTimeOperator(this. source,factory,
                        Function.identity(),time,unit)
               );

    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> grouped(final int size, final Supplier<C> factory) {
        return createSeq(new GroupingOperator<>( source,factory, Function.identity(),size));

    }

    @Override
    public ReactiveSeq<T> skipLast(final int num) {
        if(num==1)
            return createSeq(new SkipLastOneOperator<>( source));
        return createSeq(new SkipLastOperator<>( source, num));
    }

    @Override
    public ReactiveSeq<T> limitLast(final int num) {
        if(num==1)
            return createSeq(new LimitLastOneOperator<>( source));
        return createSeq(new LimitLastOperator<>( source, num));
    }

    @Override
    public ReactiveSeq<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return createSeq(new RecoverOperator<>( source,fn));
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(final Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {

        Function<? super Throwable, ? extends EX> accept =  e->{
            if (exceptionClass.isAssignableFrom(e.getClass())){
                return (EX)e;
            }
            throw ExceptionSoftener.throwSoftenedException(e);
        };
        return createSeq(new RecoverOperator<>( source,fn.compose(accept)));
    }






    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer) {
        StreamSubscription sub = source.subscribe(consumer, this.defaultErrorHandler, ()->{});
        sub.request(numberOfElements);
        return sub;
    }

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError) {

        StreamSubscription sub = source.subscribe(consumer, consumerError, ()->{});
        sub.request(numberOfElements);
        return sub;
    }

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        StreamSubscription sub = source.subscribe(consumer, consumerError, onComplete);
        sub.request(numberOfElements);
        return sub;
    }

    @Override
    public <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        if(async==Type.BACKPRESSURE){
            this.source.subscribe(consumerElement, consumerError, () -> {
            }).request(Long.MAX_VALUE);
        }else {
            source.subscribeAll(consumerElement, consumerError, () -> {
            });
        }


    }

    @Override
    public <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
                                              final Runnable onComplete) {
        if(async==Type.BACKPRESSURE){
            this.source.subscribe(consumerElement, consumerError, onComplete).request(Long.MAX_VALUE);
        }else {
            source.subscribeAll(consumerElement, consumerError, onComplete);
        }


    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Pure#unit(java.lang.Object)
     */
    @Override
    public <T> ReactiveSeq<T> unit(final T unit) {
        return ReactiveSeq.of(unit);
    }

    @Override
    public <U, R> ReactiveSeq<R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        Operator<U> right;
        if(other instanceof ReactiveStreamX){
            right = ((ReactiveStreamX<U>)other).source;
        }else{
            right = new SpliteratorToOperator<U>(((Stream<U>)other).spliterator());
        }
        return createSeq(new ZippingOperator<>(source,right,zipper));
    }

    @Override
    public HotStream<T> schedule(final String cron, final ScheduledExecutorService ex) {
        return Streams.schedule(this, cron, ex);

    }

    @Override
    public HotStream<T> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
        return Streams.scheduleFixedDelay(this, delay, ex);
    }

    @Override
    public HotStream<T> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        return Streams.scheduleFixedRate(this, rate, ex);

    }
    

    @Override
    public ReactiveSeq<T> limit(long num){
        return createSeq(new LimitOperator<>(source,num));
    }
    @Override
    public ReactiveSeq<T> skip(long num){
        return createSeq(new SkipOperator<>(source,num));
    }
    @Override
    public ReactiveSeq<T> cycle() {
        return grouped(Integer.MAX_VALUE,()->new ArrayList<>(100_000))
                    .map(ListX::fromIterable)
                          .flatMapI(s -> s.cycle(Long.MAX_VALUE));

    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        if(async==Type.NO_BACKPRESSURE){

            cyclops.async.Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
                    .build();
            Topic<T> topic = new Topic<>(queue);
            this.source.subscribe(queue::offer,i->queue.close(),queue::close);
            return Tuple.tuple(topic.stream(),topic.stream());

        }
        Iterable<T> sourceIt = new OperatorToIterable<T,T>(source,this.defaultErrorHandler,async==Type.BACKPRESSURE);
        Tuple2<Iterable<T>, Iterable<T>> copy = Streams.toBufferingDuplicator(sourceIt);
        Tuple2<Operator<T>, Operator<T>> operators = copy.map((a,b)->
            Tuple.tuple(new SpliteratorToOperator<T>(a.spliterator()),new SpliteratorToOperator<T>(b.spliterator()))
        );
        return operators.map((a,b)->Tuple.tuple(createSeq(a),createSeq(b)));

    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {
        if(async==Type.NO_BACKPRESSURE){

            cyclops.async.Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
                    .build();
            Topic<T> topic = new Topic<>(queue);
            this.source.subscribe(queue::offer,i->queue.close(),queue::close);
            return Tuple.tuple(topic.stream(),topic.stream(),topic.stream());

        }
        Iterable<T> sourceIt = new OperatorToIterable<T,T>(source,this.defaultErrorHandler,async==Type.BACKPRESSURE);
        ListX<SpliteratorToOperator<T>> copy = Streams.toBufferingCopier(sourceIt, 3)
                                         .map(it->new SpliteratorToOperator<>(it.spliterator()));

        return Tuple.tuple(createSeq(copy.get(0)),
                createSeq(copy.get(1)),
                createSeq(copy.get(2)));

    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        if(async==Type.NO_BACKPRESSURE){

            cyclops.async.Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
                    .build();
            Topic<T> topic = new Topic<>(queue);
            this.source.subscribe(queue::offer,i->queue.close(),queue::close);
            return Tuple.tuple(topic.stream(),topic.stream(),topic.stream(),topic.stream());

        }
        Iterable<T> sourceIt = new OperatorToIterable<T,T>(source,this.defaultErrorHandler,async==Type.BACKPRESSURE);
        ListX<SpliteratorToOperator<T>> copy = Streams.toBufferingCopier(sourceIt, 4)
                .map(it->new SpliteratorToOperator<>(it.spliterator()));

        return Tuple.tuple(createSeq(copy.get(0)),
                            createSeq(copy.get(1)),
                            createSeq(copy.get(2)),
                            createSeq(copy.get(3)));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Tuple2<Optional<T>, ReactiveSeq<T>> splitAtHead() {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = splitAt(1);
        return new Tuple2(
                Tuple2.v1.toOptional()
                        .flatMap(l -> l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty()),
                Tuple2.v2);
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(final int where) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return new Tuple2(
                Tuple2.v1.limit(where), Tuple2.v2.skip(where));


    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(final Predicate<T> splitter) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return new Tuple2(
                Tuple2.v1.limitWhile(splitter), Tuple2.v2.skipWhile(splitter));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partition(final Predicate<? super T> splitter) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return new Tuple2(
                Tuple2.v1.filter(splitter), Tuple2.v2.filter(splitter.negate()));

    }

    @Override
    public <U> ReactiveSeq<Tuple2<T, U>> zipS(Stream<? extends U> other) {
        Operator<U> right;
        if(other instanceof ReactiveStreamX){
            right = ((ReactiveStreamX<U>)other).source;
        }else{
            right = new SpliteratorToOperator<U>(((Stream<U>)other).spliterator());
        }
        return createSeq(new ZippingOperator<>(source,right,Tuple::tuple));

    }

    @Override
    public <S, U> ReactiveSeq<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return zip(second,Tuple::tuple).zip(third,(a,b)->Tuple.tuple(a.v1,a.v2,b));
    }

    @Override
    public <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return zip(second,Tuple::tuple).zip(third,(a,b)->Tuple.tuple(a.v1,a.v2,b))
                .zip(fourth,(a,b)->(Tuple4<T,T2,T3,T4>)Tuple.tuple(a.v1,a.v2,a.v3,b));
    }

    @Override
    public ReactiveSeq<T> cycle(long times) {
        return grouped(Integer.MAX_VALUE,()->new ArrayList<>(100_000))
                .map(ListX::fromIterable)
                .flatMapI(s->s.cycle(times));

    }

    
    

    
}
