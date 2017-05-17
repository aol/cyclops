package com.aol.cyclops2.internal.stream;


import com.aol.cyclops2.internal.stream.spliterators.push.*;
import com.aol.cyclops2.types.futurestream.Continuation;
import com.aol.cyclops2.types.stream.reactive.QueueBasedSubscriber;
import com.aol.cyclops2.types.stream.reactive.QueueBasedSubscriber.Counter;
import com.aol.cyclops2.types.stream.reactive.ValueSubscriber;
import cyclops.async.QueueFactories;
import cyclops.async.QueueFactory;
import cyclops.async.Signal;
import cyclops.collections.ListX;
import com.aol.cyclops2.internal.stream.publisher.PublisherIterable;
import com.aol.cyclops2.internal.stream.spliterators.*;
import cyclops.collections.immutable.PVectorX;
import cyclops.companion.*;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.either.Either;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.monads.Witness;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Comparator.comparing;
/*
 * Note on Organization
 * Composite operators (those that work by composing calls to existing operators) should move to ReactiveSeq
 * Shared operators should be defined here
 * Specific operators in the specific base class
 *
 */
public abstract class SpliteratorBasedStream<T> extends BaseExtendedStream<T>{

    final Spliterator<T> stream;
    
    final Optional<ReversableSpliterator> reversible;

    public SpliteratorBasedStream(final Stream<T> stream) {

        this.stream = stream.spliterator();
        this.reversible = Optional.empty();
        

    }
    public SpliteratorBasedStream(final Spliterator<T> stream, final Optional<ReversableSpliterator> rev) {
        this.stream = stream;
        this.reversible = rev;
        

    }
    public SpliteratorBasedStream(final Stream<T> stream, final Optional<ReversableSpliterator> rev) {
        this.stream = stream.spliterator();
        this.reversible = rev;
        

    }
    @Override
    public Iterator<T> iterator(){
        return Spliterators.iterator(copy());

    }
   public  <R> ReactiveSeq<R> coflatMap(Function<? super ReactiveSeq<T>, ? extends R> fn){
        return ReactiveSeq.fromSpliterator(new LazySingleSpliterator<T,ReactiveSeq<T>,R>(createSeq(copy()),fn));

    }

    @Override
    public Either<Throwable,T> findFirstOrError(){
        return Either.fromLazy(Eval.later(()->{
            ValueSubscriber<T> valueSubscriber = ValueSubscriber.subscriber();
            subscribe(valueSubscriber);
            return Either.fromXor(valueSubscriber.toXor());
        }));
    }

    @Override
    public Maybe<T> findOne(){
        return Maybe.fromLazy(Eval.later(()->Maybe.fromOptional(findFirst())));
    }
    
    public  <A,R> ReactiveSeq<R> collectSeq(Collector<? super T,A,R> c){
        Spliterator<T> s = this.spliterator();
        CollectingSinkSpliterator<T,A,R> fs = new CollectingSinkSpliterator<T,A,R>(s.estimateSize(), s.characteristics(), s,c);
        
        return createSeq(new ValueEmittingSpliterator<R>(1, s.characteristics(),createSeq(fs)));


    }


    
    public ReactiveSeq<T> fold(Monoid<T> monoid){
        Spliterator<T> s = this.spliterator();
        FoldingSinkSpliterator<T> fs = new FoldingSinkSpliterator<>(s.estimateSize(), s.characteristics(), s, monoid);
        
        
        return createSeq(new ValueEmittingSpliterator<T>(1, s.characteristics(),createSeq(fs)));
    }

    


    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Pure#unit(java.lang.Object)
     */
    @Override
    public <T> ReactiveSeq<T> unit(final T unit) {
        return ReactiveSeq.of(unit);
    }





   


    public Stream<T> unwrapStream() {

        return StreamSupport.stream(copy(),false);

    }





    @Override
    public final <S> ReactiveSeq<Tuple2<T, S>> zipS(final Stream<? extends S> second) {
        return createSeq( new ZippingSpliterator<>(get(),second.spliterator(),(a, b) -> new Tuple2<>(
                                                        a, b)));
    }
    @Override
    public final <U, R> ReactiveSeq<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper){
        return createSeq( new ZippingSpliterator<>(get(),other.spliterator(),zipper));
    }
    @Override
    public final <S, U,R> ReactiveSeq<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third,
                                                          final Fn3<? super T, ? super S, ? super U,? extends R> fn3) {
        return createSeq( new Zipping3Spliterator<>(get(),second.spliterator(),third.spliterator(),fn3));
    }

    @Override
    public <S, U> ReactiveSeq<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return createSeq( new Zipping3Spliterator<>(get(),second.spliterator(),third.spliterator(),(a, b, c)->Tuple.tuple(a,b,c)));
    }


    @Override
    public <T2, T3, T4, R> ReactiveSeq<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return createSeq( new Zipping4Spliterator<>(get(),second.spliterator(),third.spliterator(),fourth.spliterator(),fn));

    }

    @Override
    public final <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {
        return zip4(second,third,fourth,Tuple::tuple);

    }




    @Override
    public final ReactiveSeq<PVectorX<T>> sliding(final int windowSize, final int increment) {
        return createSeq(new SlidingSpliterator<>(get(),Function.identity(), windowSize,increment), reversible);
    }

    @Override
    public ReactiveSeq<ListX<T>> grouped(final int groupSize) {
        return createSeq(new GroupingSpliterator<T,List<T>,ListX<T>>(get(),()->new ArrayList(groupSize), c->ListX.fromIterable(c),groupSize), this.reversible);

    }
    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyWhile(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return createSeq(new GroupedStatefullySpliterator<>(get(),()->ListX.of(),Function.identity(), predicate), this.reversible);
    }
    @Override
    public <C extends Collection<T>,R> ReactiveSeq<R> groupedStatefullyWhile(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                                             Function<? super C, ? extends R> finalizer) {
        return this.<R>createSeq(new GroupedStatefullySpliterator<T,C,R>(get(),factory,finalizer, predicate), this.reversible);
    }
    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return createSeq(new GroupedStatefullySpliterator<>(get(),()->ListX.of(),Function.identity(), predicate.negate()), this.reversible);
    }
    @Override
    public <C extends Collection<T>,R> ReactiveSeq<R> groupedStatefullyUntil(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                        Function<? super C, ? extends R> finalizer) {
        return this.<R>createSeq(new GroupedStatefullySpliterator<T,C,R>(get(),factory,finalizer, predicate.negate()), this.reversible);
    }

    @Override
    public final ReactiveSeq<T> distinct() {
        return createSeq(new DistinctSpliterator<T,T>(get()), reversible);
    }


    @Override
    public final <U> ReactiveSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return createSeq(new ConcatonatingSpliterator<>(new SingleSpliterator<U>(seed),
                new ScanLeftSpliterator<T,U>(get(),
                        seed,function)),reversible);


    }






    @Override
    public ReactiveSeq<T> skip(final long num) {
       /** TODO future optimization so position of skip doesn't matter
        if(reversible.isPresent()){
            ReversableSpliterator rev = reversible.get();
            if(rev instanceof Indexable){
                Indexable<T> indexable = (Indexable)rev;
                Optional<ReversableSpliterator> newRev = Optional.of((ReversableSpliterator) (indexable).skip(num));
                return createSeq(get(),newRev);
            }
        }**/
        if(this.stream instanceof Indexable){
            Indexable<T> indexable = (Indexable)stream;
            return createSeq(indexable.skip(num),reversible);
        }
        return createSeq(new SkipSpliterator<>(get(),num), reversible);
    }

    @Override
    public final ReactiveSeq<T> skipWhile(final Predicate<? super T> p) {
        return createSeq(new SkipWhileSpliterator<T>(get(),p), reversible);
    }

    @Override
    public final ReactiveSeq<T> skipUntil(final Predicate<? super T> p) {
        return skipWhile(p.negate());
    }

    @Override
    public ReactiveSeq<T> limit(final long num) {


       if(this.stream instanceof Indexable){
           Indexable<T> indexable = (Indexable)stream;
           Spliterator<T> limit = indexable.take(num);
         //  Optional<ReversableSpliterator> newRev = Optional.of((ReversableSpliterator) (indexable).take(num));
           return createSeq(limit,Optional.empty());
       }
        return createSeq(new LimitSpliterator<T>(get(),num), reversible);
    }

    @Override
    public final ReactiveSeq<T> limitWhile(final Predicate<? super T> p) {
        return createSeq(new LimitWhileSpliterator<T>(get(), p), reversible);
    }

    @Override
    public final ReactiveSeq<T> limitUntil(final Predicate<? super T> p) {
        return limitWhile(p.negate());
    }







    @Override
    public ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new SkipWhileSpliterator<T>(get(),predicate),reversible );
    }

    @Override
    public ReactiveSeq<T> limitWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new LimitWhileClosedSpliterator<T>(get(),predicate),reversible);
    }






    @Override
    public final Optional<T> findFirst() {
        Object[] result = {null};

        copy().tryAdvance(e->{
               result[0]=e;
           });


        return Optional.ofNullable((T)result[0]);
    }




    @Override
    public final Optional<T> reduce(final BinaryOperator<T> accumulator) {
        Object[] result = {null};
        stream.forEachRemaining(e->{
            if(result[0]==null)
                result[0]=e;
            else{
                result[0] = accumulator.apply((T)result[0],e);
            }
        });
       return result[0]==null? Optional.empty() : Optional.of((T)result[0]);
    }

    @Override
    public final T reduce(final T identity, final BinaryOperator<T> accumulator) {
       Object[] result = {identity};
       copy().forEachRemaining(e->{
           result[0] = accumulator.apply((T)result[0],e);
       });
        return (T)result[0];
    }


    @Override
    public final <R> ReactiveSeq<R> map(final Function<? super T, ? extends R> fn) {

        if(this.stream instanceof ComposableFunction){
            ComposableFunction f = (ComposableFunction)stream;
            return createSeq(f.compose(fn),reversible);
        }
        return createSeq(new MappingSpliterator<T,R>(this.get(),fn), reversible);
    }

    @Override
    public final ReactiveSeq<T> peek(final Consumer<? super T> c) {
        return map(i->{c.accept(i); return i;});
    }

    @Override
    public final <R> ReactiveSeq<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> fn) {
        if(this.stream instanceof FunctionSpliterator){
            FunctionSpliterator f = (FunctionSpliterator)stream;
            return createSeq(StreamFlatMappingSpliterator.compose(f,fn),reversible);
        }
        return createSeq(new StreamFlatMappingSpliterator<>(get(),fn), Optional.empty());

    }

    @Override
    public final <R> ReactiveSeq<R> flatMapAnyM(final Function<? super T, AnyM<Witness.stream,? extends R>> fn) {
        return createSeq(Streams.flatMapAnyM(this, fn), reversible);
    }

    @Override
    public final <R> ReactiveSeq<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> fn) {
        if(this.stream instanceof FunctionSpliterator){
            FunctionSpliterator f = (FunctionSpliterator)stream;
            return createSeq(IterableFlatMappingSpliterator.compose(f,fn),reversible);
        }

        return createSeq(new IterableFlatMappingSpliterator<>(get(),fn), Optional.empty());

    }

    /**
     * A potentially asynchronous flatMap operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously, users can use the overloaded @see {@link IterableFunctor#flatMapPublisher(Function, int, QueueFactory)}
     * method to subscribeAll asynchronously also. A default limit of 10k active publishers is enforced, along with a default limit of 5k queued values before
     * backpressure is applied.
     *
     * @param mapper
     * @return
     */
    public <R> ReactiveSeq<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return flatMapP(256,mapper);
    }

    /**
     * A potentially asynchronous flatMap operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously, users can use the overloaded @see {@link IterableFunctor#flatMapPublisher(Function, int, QueueFactory)}
     * method to subscribeAll asynchronously also. Active publishers are limited by the maxConcurrency parameter, along with a default limit of 5k queued values before
     * backpressure is applied.
     *
     * @param mapper
     * @return
     */
    public <R> ReactiveSeq<R> flatMapP(final int maxConcurrency,final Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return flatMapP(maxConcurrency, QueueFactories.boundedNonBlockingQueue(maxConcurrency*4),mapper);
    }

    /**
     * A potentially asynchronous flatMap operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously.
     * Active publishers are limited by the maxConcurrency parameter. The QueueFactory parameter can be used to control the maximum queued elements @see {@link QueueFactories}
     *
     *
     */
    public <R> ReactiveSeq<R> flatMapP(final int maxConcurrency,
                                       final QueueFactory<R> factory,final Function<? super T, ? extends Publisher<? extends R>> mapper) {


        final Counter c = new Counter();
        final QueueBasedSubscriber<R> init = QueueBasedSubscriber.subscriber(factory, c, maxConcurrency);

        final ReactiveSeq<T> stream = stream();
        final Supplier<Continuation> sp = () -> {

            stream.map(mapper)
                    .forEach(p -> {
                        c.active.incrementAndGet();
                        p.subscribe(QueueBasedSubscriber.subscriber(init.getQueue(), c, maxConcurrency));

                    } , i -> {
                    } , () -> {
                        init.close();
                    });

            return Continuation.empty();
        };
        final Continuation continuation = new Continuation(
                sp);
        init.addContinuation(continuation);
        return ReactiveSeq.fromStream(init.jdkStream());
    }



    abstract <X> ReactiveSeq<X> createSeq(Stream<X> stream,Optional<ReversableSpliterator> reversible);
    abstract <X> ReactiveSeq<X> createSeq(Spliterator<X> stream,Optional<ReversableSpliterator> reversible);
     <X> ReactiveSeq<X> createSeq(Spliterator<X> stream){

         return createSeq(stream, Optional.empty());

     }
    protected <X> ReactiveSeq<X> createSeq(Stream<X> stream){

        return createSeq(stream, Optional.empty());

    }
    @Override
    public final ReactiveSeq<T> filter(final Predicate<? super T> fn) {
        return createSeq(new FilteringSpliterator<T>(get(),fn).compose(), reversible);

    }

    public final ReactiveSeq<T> filterLazyPredicate(final Supplier<Predicate<? super T>> fn) {
        return createSeq(new LazyFilteringSpliterator<T>(get(),fn), reversible);

    }



    @Override
    public void forEach(final Consumer<? super T> action) {
        this.copy().forEachRemaining(action);

    }

   
    
    @Override
    public Spliterator<T> spliterator() {

        return copy();
    }


    @Override
    public long count() {

        long[] result = {0};
        stream.forEachRemaining(t -> result[0]++);
        return result[0];


    }











    public ReactiveSeq<T> changes(){

            cyclops.async.Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
                    .build();


            Spliterator<T> copy = copy();

            Continuation[] contRef ={null};
            Signal<T> signal = new Signal<T>(null, queue);
            AtomicBoolean wip = new AtomicBoolean(false);
            Continuation cont = new Continuation(()->{

                if(wip.compareAndSet(false,true)) {
                    if(!copy.tryAdvance(signal::set)){
                        signal.close();
                        return Continuation.empty();
                    }
                    wip.set(false);
                }
                return contRef[0];
            });

            contRef[0]= cont;

            queue.addContinuation(cont);

            return signal.getDiscrete().stream();


    }


    @Override
    public void subscribe(final Subscriber<? super T> sub) {
       new PublisherIterable<>(this).subscribe(sub);
    }



    @Override
    public ReactiveSeq<T> onEmpty(final T value) {
        return createSeq(new OnEmptySpliterator<>(stream,value));

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
        return createSeq(new OnEmptyGetSpliterator<>(stream,supplier));
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return createSeq(new OnEmptyThrowSpliterator<>(stream,supplier));
    }

    private Spliterator<? extends T> avoidCopy(Stream<? extends T> stream ){
        if(stream instanceof StreamX){
            return ((StreamX)stream).get();
        }
        return stream.spliterator();
    }
    private Spliterator<? extends T> avoidCopy(Iterable<? extends T> stream ){
        if(stream instanceof StreamX){
            return ((StreamX)stream).get();
        }
        return stream.spliterator();
    }
    @Override
    public ReactiveSeq<T> appendS(final Stream<? extends T> other) {
        return ReactiveSeq.concat(get(),avoidCopy(other));
    }
    public ReactiveSeq<T> append(final Iterable<? extends T> other) {
        return ReactiveSeq.concat(get(),avoidCopy(other));
    }

    //TODO use spliterators and createSeq
    @Override
    public ReactiveSeq<T> append(final T other) {
        return ReactiveSeq.concat(get(),new SingleSpliterator<T>(other));
    }

    @Override
    public ReactiveSeq<T> append(final T... other) {
        return ReactiveSeq.concat(get(),Stream.of(other).spliterator());
    }
    @Override
    public ReactiveSeq<T> prependS(final Stream<? extends T> other) {
        return ReactiveSeq.concat(avoidCopy(other),get());
    }
    public ReactiveSeq<T> prepend(final Iterable<? extends T> other) {
        return ReactiveSeq.concat(avoidCopy(other),get());
    }

    @Override
    public ReactiveSeq<T> prepend(final T other) {
        return ReactiveSeq.concat(new SingleSpliterator<T>(other),get());
    }

    @Override
    public ReactiveSeq<T> prepend(final T... other) {
        return ReactiveSeq.concat(Stream.of(other).spliterator(),get());
    }


    @Override
    public <U> ReactiveSeq<T> distinct(final Function<? super T, ? extends U> keyExtractor) {
        return createSeq(new DistinctKeySpliterator<>(keyExtractor,stream),reversible);
    }






    @Override
    public ReactiveSeq<ListX<T>> groupedBySizeAndTime(final int size, final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeAndSizeSpliterator(this.get(),()->ListX.fromIterable(new ArrayList<>(size)),
                        Function.identity(),size,time,t),
                reversible);

    }
    public  <R> ReactiveSeq<R> mapLazyFn(Supplier<Function<? super T, ? extends R>> fn){
        //not composable to the 'left' (as statefulness is lost)
        return createSeq(new LazyMappingSpliterator<T,R>(this.get(),fn), reversible);

    }
    @Override
    public ReactiveSeq<ListX<T>> groupedByTime(final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeSpliterator<>(get(),
                ()->ListX.fromIterable(new ArrayList<>(100)),
                Function.identity(),time, t), reversible);
    }


    @Override
    public ReactiveSeq<T> skip(final long time, final TimeUnit unit) {
        return createSeq(new SkipWhileTimeSpliterator<T>(get(), time, unit), this.reversible);
    }

    @Override
    public ReactiveSeq<T> limit(final long time, final TimeUnit unit) {
        return createSeq(new LimitWhileTimeSpliterator<T>(get(),time,unit),reversible);

    }

   
    @Override
    public ReactiveSeq<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return groupedWhile(predicate.negate());

    }

    @Override
    public ReactiveSeq<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return createSeq(new GroupedWhileSpliterator<>(get(),()->ListX.of(),Function.identity(), predicate), this.reversible);


    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return createSeq(new GroupedWhileSpliterator<>(get(),factory,Function.identity(), predicate), this.reversible);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
      return groupedWhile(predicate.negate(),factory);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
            final Supplier<C> factory) {
        return createSeq(new GroupedByTimeAndSizeSpliterator(this.get(),factory,
                        Function.identity(),size,time,unit),
                reversible);

    }

    @Override
    public <C extends Collection<? super T>,R> ReactiveSeq<R> groupedBySizeAndTime(final int size, final long time,
                                                                                 final TimeUnit unit,
                                                                                 final Supplier<C> factory,
                                                                                 Function<? super C, ? extends R> finalizer
    ) {
        return createSeq(new GroupedByTimeAndSizeSpliterator(this.get(),factory,
                        finalizer,size,time,unit),
                reversible);

    }
    @Override
    public <C extends Collection<? super T>,R> ReactiveSeq<R> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return createSeq(new GroupedByTimeSpliterator(this.get(),factory,
                        finalizer,time,unit),
                reversible);

    }
    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return createSeq(new GroupedByTimeSpliterator(this.get(),factory,
                        Function.identity(),time,unit),
                reversible);

    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> grouped(final int size, final Supplier<C> factory) {
        return createSeq(new GroupingSpliterator<>(get(),factory, Function.identity(),size), this.reversible);

    }

    @Override
    public ReactiveSeq<T> skipLast(final int num) {
        return createSeq(SkipLastSpliterator.skipLast(get(), num), this.reversible);
    }

    @Override
    public ReactiveSeq<T> limitLast(final int num) {
        return createSeq(LimitLastSpliterator.limitLast(get(), num), this.reversible);
    }

    @Override
    public ReactiveSeq<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return createSeq(new RecoverSpliterator<T,Throwable>(get(),fn,Throwable.class), this.reversible);
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(final Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return createSeq(new RecoverSpliterator<T,EX>(get(),fn,exceptionClass), this.reversible);
    }
    

  
 
    

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer) {
        return Streams.forEach(this, numberOfElements, consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError) {

        return Streams.forEach(this, numberOfElements, consumer, consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError, final Runnable onComplete) {

        return Streams.forEach(this, numberOfElements, consumer, consumerError, onComplete);
    }

    @Override
    public <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {


        new ForEachWithError<T>(this.copy(),consumerError).forEachRemaining(consumerElement);


    }

    @Override
    public <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
                                              final Runnable onComplete) {


        new ForEachWithError<T>(this.copy(),consumerError,onComplete).forEachRemaining(consumerElement);
    }



    @Override
    public String format() {
        return Seq.seq(this.copy())
                  .format();
    }

    @Override
    public Collectable<T> collectable() {
        return Seq.seq(copy());
    }

    @Override
    public <T> ReactiveSeq<T> unitIterator(final Iterator<T> it) {
        return ReactiveSeq.fromIterator(it);
    }


    Spliterator<T> get() {

        return stream;
    }
    Spliterator<T> copy() {

        return CopyableSpliterator.copy(stream);
    }


}
