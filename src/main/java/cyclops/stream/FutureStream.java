package cyclops.stream;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.*;

import com.aol.cyclops2.internal.react.exceptions.SimpleReactProcessingException;
import com.aol.cyclops2.types.FoldableTraversable;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.futurestream.*;
import com.aol.cyclops2.types.stream.reactive.ReactiveStreamsTerminalFutureOperations;
import cyclops.*;
import cyclops.async.*;
import cyclops.async.Queue;
import cyclops.collections.immutable.PVectorX;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Xor;
import cyclops.control.either.Either;
import cyclops.function.Lambda;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import cyclops.async.Queue.ClosedQueueException;
import cyclops.async.Queue.QueueTimeoutException;
import cyclops.async.QueueFactory;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import cyclops.collections.ListX;
import com.aol.cyclops2.internal.react.FutureStreamImpl;
import com.aol.cyclops2.internal.react.async.future.FastFuture;
import com.aol.cyclops2.internal.react.stream.CloseableIterator;
import com.aol.cyclops2.internal.react.stream.LazyStreamWrapper;
import com.aol.cyclops2.internal.react.stream.traits.future.operators.LazyFutureStreamUtils;
import com.aol.cyclops2.internal.react.stream.traits.future.operators.OperationsOnFuturesImpl;
import com.aol.cyclops2.internal.stream.LazyFutureStreamFutureOpterationsImpl;
import com.aol.cyclops2.react.RetryBuilder;
import com.aol.cyclops2.react.SimpleReactFailedStageException;
import com.aol.cyclops2.react.ThreadPools;
import com.aol.cyclops2.react.async.subscription.Continueable;
import com.aol.cyclops2.react.collectors.lazy.LazyResultConsumer;
import com.aol.cyclops2.react.collectors.lazy.MaxActive;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import cyclops.monads.Witness;
import com.aol.cyclops2.types.stream.HotStream;
import com.aol.cyclops2.types.stream.reactive.FutureStreamSynchronousPublisher;
import cyclops.function.Fn4;
import cyclops.function.Fn3;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;

import lombok.val;

public interface FutureStream<U> extends LazySimpleReactStream<U>,
                                          LazyStream<U>,
                                            ReactiveSeq<U>,
                                            LazyToQueue<U>,
                                          ConfigurableStream<U, FastFuture<U>>,
                                          FutureStreamSynchronousPublisher<U> {

    @Override
    default ReactiveSeq<U> changes(){
        return fromStream(stream().changes());
    }

    @Override
    default Maybe<U> findOne(){
        return stream().findOne();
    }

    @Override
    default Either<Throwable, U> findFirstOrError(){
        return stream().findFirstOrError();
    }

    @Override
    default <R> FutureStream<R> parallel(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<R>> fn) {
        return fromStream(stream().parallel(fj,fn));
    }

    @Override
    default <U1, R> FutureStream<R> zipLatest(final Publisher<? extends U1> other, final BiFunction<? super U, ? super U1, ? extends R> zipper) {
        return fromStream(stream().zipLatest(other,zipper));
    }

    @Override
    default FutureStream<U> skipUntilClosed(final Predicate<? super U> p) {
        return fromStream(stream().skipUntilClosed(p));
    }

    @Override
    default ReactiveSeq<U> limitUntilClosed(final Predicate<? super U> p) {
        return fromStream(stream().limitUntilClosed(p));
    }

    @Override
    default FutureStream<U> reduceAll(U identity, BinaryOperator<U> accumulator) {
        return fromStream(stream().reduceAll(identity,accumulator));
    }

    @Override
    default <R, A> FutureStream<R> collectAll(Collector<? super U, A, R> collector) {
        return fromStream(stream().collectAll(collector));
    }

    @Override
    default FutureStream<U> append(U value){
        return fromStream(stream().append(value));
    }

    @Override
    default FutureStream<Tuple2<U, Long>> timestamp() {
        return fromStream(stream().timestamp());
    }

    @Override
    default <R> FutureStream<R> retry(final Function<? super U, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return fromStream(stream().retry(fn,retries,delay,timeUnit));
    }


    @Override
    default FutureStream<ReactiveSeq<U>> combinations(final int size) {
        return fromStream(stream().combinations(size));
    }


    @Override
    default FutureStream<U> removeAllS(final Stream<? extends U> stream) {
        return fromStream(stream().removeAllS(stream));
    }

    @Override
    default FutureStream<U> removeAllS(final Iterable<? extends U> it) {
        return fromStream(stream().removeAllS(it));
    }

    @Override
    default FutureStream<U> removeAllS(final U... values) {
        return fromStream(stream().removeAllS(values));
    }

    @Override
    default FutureStream<U> retainAllS(final Iterable<? extends U> it) {
        return fromStream(stream().removeAllS(it));
    }

    @Override
    default FutureStream<U> retainAllS(final Stream<? extends U> stream) {
        return fromStream(stream().retainAllS(stream));
    }

    @Override
    default FutureStream<U> retainAllS(final U... values) {
        return fromStream(stream().retainAllS(values));
    }

    @Override
    default FutureStream<U> zip(BinaryOperator<Zippable<U>> combiner, final Zippable<U> app) {
        return fromStream(stream().zip(combiner,app));
    }

    @Override
    default <R> FutureStream<R> zipWith(Iterable<Function<? super U, ? extends R>> fn) {
        return fromStream(stream().zipWith(fn));
    }

    @Override
    default <R> FutureStream<R> zipWithS(Stream<Function<? super U, ? extends R>> fn) {
        return fromStream(stream().zipWithS(fn));
    }

    @Override
    default <R> FutureStream<R> zipWithP(Publisher<Function<? super U, ? extends R>> fn) {
        return fromStream(stream().zipWithP(fn));
    }

    @Override
    default <U1> FutureStream<Tuple2<U, U1>> zipP(final Publisher<? extends U1> other) {
        return fromStream(stream().zipP(other));
    }

    @Override
    default <S, U1, R> FutureStream<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U1> third, final Fn3<? super U, ? super S, ? super U1, ? extends R> fn3) {
        return fromStream(stream().zip3(second,third,fn3));
    }

    @Override
    default <T2, T3, T4, R> FutureStream<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super U, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return fromStream(stream().zip4(second,third,fourth,fn));
    }

    @Override
    default FutureStream<U> mergeP(final Publisher<U>... publishers) {
        return fromStream(stream().mergeP(publishers));
    }

    @Override
    default FutureStream<U> mergeP(final QueueFactory<U> factory, final Publisher<U>... publishers) {
        return fromStream(stream().mergeP(factory,publishers));
    }

    @Override
    default FutureStream<U> merge(Adapter<U>... adapters) {
        return fromStream(stream().merge(adapters));
    }

    @Override
    default <R1, R2, R3> FutureStream<R3> fanOutZipIn(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R1>> path1, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R2>> path2, BiFunction<? super R1, ? super R2, ? extends R3> zipFn) {
        return fromStream(stream().fanOutZipIn(path1,path2,zipFn));
    }

    @Override
    default <R1, R2, R3> FutureStream<R3> parallelFanOutZipIn(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<R1>> path1, Function<? super Stream<U>, ? extends Stream<R2>> path2, BiFunction<? super R1, ? super R2, ? extends R3> zipFn) {
        return fromStream(stream().parallelFanOutZipIn(fj,path1,path2,zipFn));
    }

    @Override
    default <R> FutureStream<R> fanOut(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path1, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path2) {
        return fromStream(stream().fanOut(path1,path2));
    }

    @Override
    default <R> FutureStream<R> parallelFanOut(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<R>> path1, Function<? super Stream<U>, ? extends Stream<R>> path2) {
        return fromStream(stream().parallelFanOut(fj,path1,path2));
    }

    @Override
    default <R> FutureStream<R> fanOut(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R>> path1, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R>> path2, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R>> path3) {
        return fromStream(stream().fanOut(path1,path2,path3));
    }

    @Override
    default <R> FutureStream<R> parallelFanOut(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<R>> path1, Function<? super Stream<U>, ? extends Stream<R>> path2, Function<? super Stream<U>, ? extends Stream<R>> path3) {
        return fromStream(stream().parallelFanOut(fj,path1, path2, path3));
    }

    @Override
    default <R1, R2, R3, R4> FutureStream<R4> parallelFanOutZipIn(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<R1>> path1, Function<? super Stream<U>, ? extends Stream<R2>> path2, Function<? super Stream<U>, ? extends Stream<R3>> path3, Fn3<? super R1, ? super R2, ? super R3, ? extends R4> zipFn) {
        return fromStream(stream().parallelFanOutZipIn(fj,path1, path2, path3,zipFn));
    }

    @Override
    default <R1, R2, R3, R4> FutureStream<R4> fanOutZipIn(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R1>> path1, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R2>> path2, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R3>> path3, Fn3<? super R1, ? super R2, ? super R3, ? extends R4> zipFn) {
        return fromStream(stream().fanOutZipIn(path1, path2, path3,zipFn));
    }

    @Override
    default <R> FutureStream<R> fanOut(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R>> path1, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R>> path2, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R>> path3, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R>> path4) {
        return fromStream(stream().fanOut(path1, path2, path3,path4));
    }

    @Override
    default <R> FutureStream<R> parallelFanOut(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<R>> path1, Function<? super Stream<U>, ? extends Stream<R>> path2, Function<? super Stream<U>, ? extends Stream<R>> path3, Function<? super Stream<U>, ? extends Stream<R>> path4) {
        return fromStream(stream().parallelFanOut(fj,path1, path2, path3,path4));
    }

    @Override
    default <R1, R2, R3, R4, R5> FutureStream<R5> fanOutZipIn(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R1>> path1, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R2>> path2, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R3>> path3, Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<R4>> path4, Fn4<? super R1, ? super R2, ? super R3, ? super R4, ? extends R5> zipFn) {
        return fromStream(stream().fanOutZipIn(path1, path2, path3,path4,zipFn));
    }

    @Override
    default <R1, R2, R3, R4, R5> FutureStream<R5> parallelFanOutZipIn(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<R1>> path1, Function<? super Stream<U>, ? extends Stream<R2>> path2, Function<? super Stream<U>, ? extends Stream<R3>> path3, Function<? super Stream<U>, ? extends Stream<R4>> path4, Fn4<? super R1, ? super R2, ? super R3, ? super R4, ? extends R5> zipFn) {
        return fromStream(stream().parallelFanOutZipIn(fj,path1, path2, path3,path4,zipFn));
    }




    default LazyReact builder(int maxActiveTasks, Executor exec){
        return new LazyReact(maxActiveTasks,exec);
    }
    default LazyReact builder(){
        return new LazyReact();
    }

    default <R> Future<R> foldFuture(Function<? super FoldableTraversable<U>,? extends R> fn){
        return Future.ofSupplier(()->fn.apply(this),getSimpleReact().getExecutor());
    }
    @Override
    default ReactiveStreamsTerminalFutureOperations<U> futureOperations(Executor ex){
        return new LazyFutureStreamFutureOpterationsImpl<U>(ex,this);
    }
    default ReactiveStreamsTerminalFutureOperations<U> futureOperations(){
        return new LazyFutureStreamFutureOpterationsImpl<U>(getSimpleReact().getExecutor(),this);
    }
    default <A,R> FutureStream<R> collectSeq(Collector<? super U,A,R> c){
        return this.getSimpleReact().fromStream(Stream.of(Lambda.λ(()->this.collect(c))).map(Supplier::get));
    }
    default FutureStream<U> fold(Monoid<U> monoid){
        return this.getSimpleReact().fromStream(Stream.of(Lambda.λ(()->this.reduce(monoid))).map(Supplier::get));
    }
    
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#crossApply(java.util.function.Function)
     */
    default <U1> FutureStream<Tuple2<U, U1>> crossApply(Function<? super U, ? extends Iterable<? extends U1>> function) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).seq().crossApply(function));
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#outerApply(java.util.function.Function)
     */
    default <U1> FutureStream<Tuple2<U, U1>> outerApply(Function<? super U, ? extends Iterable<? extends U1>> function) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).seq().outerApply(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#append(java.util.reactiveStream.Stream)
     */
    @Override
    default FutureStream<U> appendS(Stream<? extends U> other) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).appendS(other));
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#append(java.lang.Iterable)
     */
    @Override
    default FutureStream<U> append(Iterable<? extends U> other) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).append(other));
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#prepend(java.util.reactiveStream.Stream)
     */
    @Override
    default FutureStream<U> prependS(Stream<? extends U> other) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).prependS(other));
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#prepend(java.lang.Iterable)
     */
    @Override
    default FutureStream<U> prepend(Iterable<? extends U> other) {
        
        return fromStream(ReactiveSeq.oneShotStream(stream()).prepend(other));
    }



    /**
     * Create a Stream that finitely cycles this Stream, provided number of times
     *
     * <pre>
     * {@code
     * assertThat(FutureStream.of(1,2,2).cycle(3)
    .collect(Collectors.toList()),
    equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
     * }
     * </pre>
     * @return New cycling reactiveStream
     */
    @Override
    default FutureStream<U> cycle(long times) {
        return fromStream(stream().cycle(times));
    }
  
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#skipWhileClosed(java.util.function.Predicate)
     */
    @Override
    default FutureStream<U> skipWhileClosed(Predicate<? super U> predicate) {
        return fromStream(stream()).skipWhileClosed(predicate);
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#limitWhileClosed(java.util.function.Predicate)
     */
    @Override
    default FutureStream<U> limitWhileClosed(Predicate<? super U> predicate) {
        return fromStream(stream()).limitWhileClosed(predicate);
        
    }

 
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#sorted(java.util.function.Function, java.util.Comparator)
     */
    @Override
    default <U1> FutureStream<U> sorted(Function<? super U, ? extends U1> function, Comparator<? super U1> comparator) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).sorted(function,comparator));
        
    }

    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     * 
     * <pre>
     * {@code 
     *   
     *      FutureStream.of(1,2,3)
     *                      .map(i->i*2)
     *                      .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *      
     *      //FutureStream[12]
     * }
     * </pre>
     * 
     * 
     * @param fn
     * @return
     */
    default <R> FutureStream<R> coflatMap(Function<? super ReactiveSeq<U>, ? extends R> fn){
        
        return this.getSimpleReact().<R>generate(()->fn.apply(this))
                                    .limit(1);
    }

    @Override
    default FutureStream<U> filterNot(final Predicate<? super U> fn) {

        return (FutureStream<U>) ReactiveSeq.super.filterNot(fn);
    }
    @Override
    default FutureStream<U> notNull() {

        return (FutureStream<U>) ReactiveSeq.super.notNull();
    }

    @Override
    default <R> FutureStream<R> trampoline(final Function<? super U, ? extends Trampoline<? extends R>> mapper) {

        return (FutureStream<R>) ReactiveSeq.super.trampoline(mapper);
    }

    @Override
    default <R> R foldRight(final R identity, final BiFunction<? super U, ? super R, ? extends R> accumulator) {
        return stream().foldRight(identity, accumulator);

    }




    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> FutureStream<R> zipP(Publisher<? extends T2> publisher, BiFunction<? super U, ? super T2, ? extends R> fn) {
      
        return (FutureStream<R>)ReactiveSeq.super.zipP(publisher,fn);
    }

    /**
     * <pre>
     * {@code
     *  FutureStream.of(1,2,3,4,5)
     * 				 		.elapsed()
     * 				 	.forEach(System.out::println);
     * }
     * </pre>
     *
     * @return FutureStream that adds the time between elements in millis to
     *         each element
     */
    @Override
    default FutureStream<Tuple2<U, Long>> elapsed() {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .elapsed());
    }

    /* (non-Javadoc)
     * @see cyclops2.reactiveStream.ReactiveSeq#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default FutureStream<U> combine(final BiPredicate<? super U, ? super U> predicate, final BinaryOperator<U> op) {
        return fromStream(Streams.combine(this, predicate, op));
    }

    /**
     * If this SequenceM is empty replace it with a another Stream
     *
     * <pre>
     * {@code
     * assertThat(FutureStream.of(4,5,6)
     * 							.onEmptySwitch(()->ReactiveSeq.of(1,2,3))
     * 							.toList(),
     * 							equalTo(Arrays.asList(4,5,6)));
     * }
     * </pre>
     *
     * @param switchTo
     *            Supplier that will generate the alternative Stream
     * @return SequenceM that will switch to an alternative Stream if empty
     */
    @Override
    default FutureStream<U> onEmptySwitch(final Supplier<? extends Stream<U>> switchTo) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .onEmptySwitch(switchTo));
    }


    /* (non-Javadoc)
     * @see cyclops2.reactiveStream.ReactiveSeq#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> FutureStream<R> forEach4(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                                     BiFunction<? super U, ? super R1, ? extends BaseStream<R2, ?>> stream2,
                                                     Fn3<? super U, ? super R1, ? super R2, ? extends BaseStream<R3, ?>> stream3,
                                                     Fn4<? super U, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (FutureStream<R>)ReactiveSeq.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.reactiveStream.ReactiveSeq#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> FutureStream<R> forEach4(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                                     BiFunction<? super U, ? super R1, ? extends BaseStream<R2, ?>> stream2,
                                                     Fn3<? super U, ? super R1, ? super R2, ? extends BaseStream<R3, ?>> stream3,
                                                     Fn4<? super U, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                     Fn4<? super U, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (FutureStream<R>)ReactiveSeq.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.reactiveStream.ReactiveSeq#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> FutureStream<R> forEach3(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                                 BiFunction<? super U, ? super R1, ? extends BaseStream<R2, ?>> stream2,
                                                 Fn3<? super U, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (FutureStream<R>)ReactiveSeq.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.reactiveStream.ReactiveSeq#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> FutureStream<R> forEach3(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                                 BiFunction<? super U, ? super R1, ? extends BaseStream<R2, ?>> stream2,
                                                 Fn3<? super U, ? super R1, ? super R2, Boolean> filterFunction,
                                                 Fn3<? super U, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (FutureStream<R>)ReactiveSeq.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.reactiveStream.ReactiveSeq#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> FutureStream<R> forEach2(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                             BiFunction<? super U, ? super R1, ? extends R> yieldingFunction) {
        
        return (FutureStream<R>)ReactiveSeq.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.reactiveStream.ReactiveSeq#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> FutureStream<R> forEach2(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                             BiFunction<? super U, ? super R1, Boolean> filterFunction,
                                             BiFunction<? super U, ? super R1, ? extends R> yieldingFunction) {
        
        return (FutureStream<R>)ReactiveSeq.super.forEach2(stream1, filterFunction, yieldingFunction);
    }


    /**
     * Remove all occurances of the specified element from the SequenceM
     * <pre>
     * {@code
     * 	FutureStream.of(1,2,3,4,5,1,2,3).remove(1)
     *
     *  //FutureStream[2,3,4,5,2,3]
     * }
     * </pre>
     *
     * @param t element to remove
     * @return Filtered Stream
     */
    @Override
    default FutureStream<U> remove(final U t) {

        return (FutureStream<U>) ReactiveSeq.super.remove(t);
    }

    /**
     * Return a Stream with elements before the provided skip index removed, and elements after the provided
     * take index removed
     *
     * <pre>
     * {@code
     *   FutureStream.of(1,2,3,4,5,6).subStream(1,3);
     *
     *
     *   //FutureStream[2,3]
     * }
     * </pre>
     *
     * @param start index inclusive
     * @param end index exclusive
     * @return LqzyFutureStream between supplied indexes of original Sequence
     */
    @Override
    default FutureStream<U> subStream(final int start, final int end) {
        return this.fromStream(ReactiveSeq.oneShotStream(stream())
                                          .subStream(start, end));
    }

    /**
     * Generate the permutations based on values in the FutureStream
     * Makes use of Streamable to store intermediate stages in a collection
     *
     *
     * @return Permutations from this FutureStream
     */
    @Override
    default FutureStream<ReactiveSeq<U>> permutations() {
        return this.fromStream(ReactiveSeq.oneShotStream(stream())
                                          .permutations());

    }

    /**
     * <pre>
     * {@code
     *   FutureStream.of(1,2,3).combinations()
     *
     *   //FutureStream[SequenceM[],SequenceM[1],SequenceM[2],SequenceM[3].SequenceM[1,2],SequenceM[1,3],SequenceM[2,3]
     *   			,SequenceM[1,2,3]]
     * }
     * </pre>
     *
     *
     * @return All combinations of the elements in this reactiveStream
     */
    @Override
    default FutureStream<ReactiveSeq<U>> combinations() {
        return this.fromStream(ReactiveSeq.oneShotStream(stream())
                                          .combinations());

    }

    /**
     * FutureStream operators act on the results of the previous stage by default. That means limiting,
     * skipping, zipping all occur once results being to reactiveStream in from active Future tasks. This
     * operator allows access to a set of operators that behave differently. Limiting, skipping and zipping all occur on
     * the underlying Stream of Futures.
     *
     * Operating on results
     * <pre>
     * {@code
     *     new LazyReact().react(()->slow(),()->fast())
     *                    .zipWithIndex();
     *
     *     //[fast,0l],[slow,1l]
     * }</pre>
     * The first result will be fast and will have index 0 (the result index)
     *
     * Operating on futures
     * <pre>
     * {@code
     *     new LazyReact().react(()->slow(),()->fast())
     *                    .actOnFutures()
     *                    .zipWithIndex();
     *
     *     //[fast,1l],[slow,0l]
     * }</pre>
     * The first result will still be fast, but the index will now be the skip index 1
     *
     * @return Access a set of operators that act on the underlying futures in this
     * Stream.
     *
     */
    default OperationsOnFutures<U> actOnFutures() {
        return new OperationsOnFuturesImpl<>(
                                             this);
    }

    /*
     * @return an iterator over this Stream
     * @see com.aol.cyclops2.react.reactiveStream.traits.SimpleReactStream#iterator()
     */
    @Override
    default CloseableIterator<U> iterator() {
        return (CloseableIterator) LazySimpleReactStream.super.iterator();
    }

    /*
     * @return Queue membership subscription for this Stream
     * @see com.aol.cyclops2.react.reactiveStream.traits.LazySimpleReactStream#getSubscription()
     */
    @Override
    Continueable getSubscription();

    /*
     * @see com.aol.cyclops2.react.reactiveStream.traits.LazySimpleReactStream#withLastActive(com.aol.simple.react.reactiveStream.LazyStreamWrapper)
     */
    @Override
    <R> FutureStream<R> withLastActive(LazyStreamWrapper<R> streamWrapper);

    /*
     *	@return Stream Builder for this Stream
     * @see com.aol.cyclops2.react.reactiveStream.traits.LazySimpleReactStream#getSimpleReact()
     */
    @Override
    LazyReact getSimpleReact();

    /*
     * Subscribe to this Stream
     * If this Stream is executing in async mode it will operate as an Async Publisher, otherwise it will operate as a Synchronous publisher.
     * async() or sync() can be used just prior to subscribeAll.
     *
     * <pre>
     * {@code
     *  FutureStreamSubscriber<Integer> sub = new FutureStreamSubscriber();
        FutureStream.of(1,2,3).subscribeAll(sub);
        sub.getStream().forEach(System.out::println);
     * }
     * </pre>
     *	@param s Subscriber
     * @see org.reactivestreams.Publisher#subscribeAll(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super U> s) {
        
        FutureStreamSynchronousPublisher.super.subscribe(s);
    }

    /**
     * @return an Iterator that chunks all completed elements from this reactiveStream since last it.next() call into a collection
     */
    default Iterator<Collection<U>> chunkLastReadIterator() {

        final Queue.QueueReader reader = new Queue.QueueReader(
                                                               this.withQueueFactory(QueueFactories.unboundedQueue())
                                                                   .toQueue(q -> q.withTimeout(100)
                                                                                  .withTimeUnit(TimeUnit.MICROSECONDS)),
                                                               null);
        class Chunker implements Iterator<Collection<U>> {
            volatile boolean open = true;

            @Override
            public boolean hasNext() {

                return open == true && reader.isOpen();
            }

            @Override
            public Collection<U> next() {

                while (hasNext()) {
                    try {
                        return reader.drainToOrBlock();
                    } catch (final ClosedQueueException e) {
                        open = false;
                        return new ArrayList<>();
                    } catch (final QueueTimeoutException e) {
                        LockSupport.parkNanos(0l);
                    }
                }
                return new ArrayList<>();

            }
        }
        return new Chunker();
    }

    /**
     * @return a Stream that batches all completed elements from this reactiveStream since last read attempt into a collection
     */
    default FutureStream<Collection<U>> chunkSinceLastRead() {
        final Queue queue = this.withQueueFactory(QueueFactories.unboundedQueue())
                                .toQueue();
        final Queue.QueueReader reader = new Queue.QueueReader(
                                                               queue, null);
        class Chunker implements Iterator<Collection<U>> {

            @Override
            public boolean hasNext() {

                return reader.isOpen();
            }

            @Override
            public Collection<U> next() {
                return reader.drainToOrBlock();

            }
        }
        final Chunker chunker = new Chunker();
        final Function<Supplier<U>, Supplier<Collection<U>>> fn = s -> {
            return () -> {

                try {
                    return chunker.next();
                } catch (final ClosedQueueException e) {

                    throw new ClosedQueueException();
                }

            };
        };
        return fromStream(queue.streamBatchNoTimeout(getSubscription(), fn));

    }

    //terminal operations
    /*
     * (non-Javadoc)
     *
     * @see java.util.reactiveStream.Stream#count()
     */
    @Override
    default long count() {
        //performance optimisation count the underlying futures
        return getLastActive().stream()
                              .count();
    }

    /*
     * Change task executor for the next stage of the Stream
     *
     * <pre>
     * {@code
     *  FutureStream.of(1,2,3,4)
     *  					.map(this::loadFromDb)
     *  					.withTaskExecutor(parallelBuilder().getExecutor())
     *  					.map(this::processOnDifferentExecutor)
     *  					.toList();
     * }
     * </pre>
     *
     *	@param e New executor to use
     *	@return Stream ready for next stage definition
     * @see com.aol.cyclops2.react.reactiveStream.traits.ConfigurableStream#withTaskExecutor(java.util.concurrent.Executor)
     */
    @Override
    FutureStream<U> withTaskExecutor(Executor e);

    /*
     * Change the Retry Executor used in this reactiveStream for subsequent stages
     * <pre>
     * {@code
     * List<String> result = new LazyReact().react(() -> 1)
                .withRetrier(executor)
                .(e -> error = e)
                .retry(serviceMock).block();
     *
     * }
     * </pre>
     *
     *
     *	@param retry Retry executor to use
     *	@return Stream
     * @see com.aol.cyclops2.react.reactiveStream.traits.ConfigurableStream#withRetrier(com.nurkiewicz.asyncretry.RetryExecutor)
     */
    @Override
    FutureStream<U> withRetrier(RetryExecutor retry);

    FutureStream<U> withLazyCollector(Supplier<LazyResultConsumer<U>> lazy);

    /*
     * Change the QueueFactory type for the next phase of the Stream.
     * Default for EagerFutureStream is an unbounded blocking queue, but other types
     * will work fine for a subset of the tasks (e.g. an unbonunded non-blocking queue).
     *
     * <pre>
     * {@code
     * List<Collection<String>> collected = FutureStream
                .react(data)
                .withQueueFactory(QueueFactories.boundedQueue(1))
                .onePer(1, TimeUnit.SECONDS)
                .batchByTime(10, TimeUnit.SECONDS)
                .limit(15)
                .toList();
     * }
     * </pre>
     *	@param queue Queue factory to use for subsequent stages
     *	@return Stream
     * @see com.aol.cyclops2.react.reactiveStream.traits.ConfigurableStream#withQueueFactory(com.aol.simple.react.async.QueueFactory)
     * @see com.aol.cyclops2.react.reactiveStream.traits.FutureStream#unboundedWaitFree()
     * @see com.aol.cyclops2.react.reactiveStream.traits.FutureStream#boundedWaitFree(int size)
     */
    @Override
    FutureStream<U> withQueueFactory(QueueFactory<U> queue);

    /*
     *	@param sub Queue Subscription to use for this Stream
     * @see com.aol.cyclops2.react.reactiveStream.traits.LazySimpleReactStream#withSubscription(com.aol.simple.react.async.subscription.Continueable)
     */
    @Override
    FutureStream<U> withSubscription(Continueable sub);

    /*
     * Convert this reactiveStream into an async / sync reactiveStream
     *
     *	@param async true if aysnc reactiveStream
     *	@return
     * @see com.aol.cyclops2.react.reactiveStream.traits.ConfigurableStream#withAsync(boolean)
     */
    @Override
    FutureStream<U> withAsync(boolean async);

    /*
     * @see com.aol.cyclops2.react.reactiveStream.traits.LazyStream#forEach(java.util.function.Consumer)
     */
    @Override
    default void forEach(final Consumer<? super U> c) {
        LazyStream.super.forEach(c);

    }

    /*  Transfer data in this Stream asyncrhonously to a Queue
     * <pre>
     * {@code
     *  Queue<String> q = new LazyReact().reactInfinitely(() -> "100")
                       .limit(100)
                        .withQueueFactory(QueueFactories.boundedQueue(10))
                        .toQueue();
         q.reactiveStream().collect(Collectors.toList())
                   .size()
    
        //100
     * }</pre>
     *	@return Queue
     * @see com.aol.cyclops2.react.reactiveStream.traits.ToQueue#toQueue()
     */
    @Override
    default Queue<U> toQueue() {

        return LazyToQueue.super.toQueue();
    }
    @Override
    default <R> FutureStream<R> parallel(Function<? super Stream<U>,? extends Stream<R>> fn){
        return fromStream(ReactiveSeq.super.parallel(fn));

    }
    @Override
    default <R> FutureStream<R> jooλ(Function<? super Seq<U>, ? extends Seq<R>> mapper){
        return fromStream(ReactiveSeq.super.jooλ(mapper));
    }

    @Override
    default <T> T reduce(final T identity, final BiFunction<T, ? super U, T> accumulator) {
        return LazyStream.super.reduce(identity, accumulator, (a, b) -> a);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.reactiveStream.Stream#reduce(java.lang.Object,
     * java.util.function.BinaryOperator)
     */
    @Override
    default U reduce(final U identity, final BinaryOperator<U> accumulator) {
        return LazyStream.super.reduce(identity, accumulator);
    }

    /*
     * @see com.aol.cyclops2.react.reactiveStream.traits.LazyStream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
     */
    @Override
    default <T> T reduce(final T identity, final BiFunction<T, ? super U, T> accumulator, final BinaryOperator<T> combiner) {
        return LazyStream.super.reduce(identity, accumulator, combiner);
    }

    /*
     * @see com.aol.cyclops2.react.reactiveStream.traits.LazyStream#reduce(java.util.function.BinaryOperator)
     */
    @Override
    default Optional<U> reduce(final BinaryOperator<U> accumulator) {
        return LazyStream.super.reduce(accumulator);
    }

    /*
     * @see com.aol.cyclops2.react.reactiveStream.traits.LazyStream#collect(java.util.function.Supplier, java.util.function.BiConsumer, java.util.function.BiConsumer)
     */
    @Override
    default <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super U> accumulator, final BiConsumer<R, R> combiner) {
        return LazyStream.super.collect(supplier, accumulator, combiner);
    }

    /*
     * Execute subsequent stages on the completing thread (until async called)
     * 10X faster than async execution.
     * Use async for blocking IO or distributing work across threads or cores.
     * Switch to sync for non-blocking tasks when desired thread utlisation reached
     * <pre>
     * {@code
     *      new LazyReact().of(1,2,3)
                            .sync() //synchronous mode
                            .flatMapToCompletableFuture(i->CompletableFuture.completedFuture(i))
                            .block()
     *
     * }
     * </pre>
     *	@return Version of FutureStream that will use sync CompletableFuture methods
     * @see com.aol.cyclops2.react.reactiveStream.traits.SimpleReactStream#sync()
     */
    @Override
    default FutureStream<U> sync() {
        return this.withAsync(false);
    }

    /*
     * Execute subsequent stages by submission to an Executor for async execution
     * 10X slower than sync execution.
     * Use async for blocking IO or distributing work across threads or cores.
     * Switch to sync for non-blocking tasks when desired thread utlisation reached
     * <pre>
     * {@code
     * FutureStream reactiveStream = of(1,2,3,4).async()
     *                                      .map(this::doWorkOnSeparateThreads)
     *                                      .map(this::resubmitTaskForDistribution)
     *                                      .forEach(System.out::println);
     *
     * }</pre>
     *
     *	@return Version of FutureStream that will use async CompletableFuture methods
     * @see com.aol.cyclops2.react.reactiveStream.traits.SimpleReactStream#async()
     */
    @Override
    default FutureStream<U> async() {
        return this.withAsync(true);
    }

    /**
     * This is the default setting, internal queues are backed by a ConcurrentLinkedQueue
     * This operator will return the next stage to using this Queue type if it has been changed
     *
     * @return FutureStream backed by a ConcurrentLinkedQueue
     */
    default FutureStream<U> unboundedWaitFree() {
        return this.withQueueFactory(QueueFactories.unboundedNonBlockingQueue());
    }

    /**
     * Use an Agrona ManyToOneConcurrentArrayQueue for the next operations (wait-free, mechanical sympathy).
     * Note Queued data will be somewhat limited by configured concurrency level, but that flatMap operations
     * can increase the amount of data to be buffered significantly.
     *
     * <pre>
     * {@code
     *     FutureStream.of(col)
     *                     .boundedWaitFree(110)
     *                     .flatMap(Collection::reactiveStream)
     *                     .toList();
     * }
     * </pre>
     *
     * @param size Buffer size
     * @return FutureStream backed by an Agrona ManyToOneConcurrentArrayQueue
     */
    default FutureStream<U> boundedWaitFree(final int size) {
        return this.withQueueFactory(QueueFactories.boundedNonBlockingQueue(size));
    }

    /**
     * Configure the max active concurrent tasks. The last set value wins, this can't be set per stage.
     *
     * <pre>
     *    {@code
     *    	List<String> data = new LazyReact().react(urlFile)
     *    										.maxActive(100)
     *    										.flatMap(this::loadUrls)
     *    										.map(this::callUrls)
     *    										.block();
     *    }
     * </pre>
     *
     * @param concurrentTasks Maximum number of active task chains
     * @return FutureStream with new limits set
     */
    public FutureStream<U> maxActive(int concurrentTasks);

    /*
     * Equivalent functionally to map / transform but always applied on the completing thread (from the previous stage)
     *
     * When autoOptimize functionality is enabled, thenSync is the default behaviour for transform / map operations
     *
     * <pre>
     * {@code
     *  new LazyReact().withAutoOptimize(false)
     *                 .react(()->1,()->2,()->3)
     *                  .thenSync(it->it+100) //add 100
                        .toList();
     * }
     * //results in [100,200,300]
     * </pre>
     *
     * @see com.aol.cyclops2.react.reactiveStream.traits.LazySimpleReactStream#thenSync(java.util.function.Function)
     */
    @Override
    default <R> FutureStream<R> thenSync(final Function<? super U, ? extends R> fn) {
        return (FutureStream<R>) LazySimpleReactStream.super.thenSync(fn);
    }

    /*
     *  Equivalent functionally to peek but always applied on the completing thread (from the previous stage)
     *  When autoOptimize functionality is enabled, peekSync is the default behaviour for peek operations
     * <pre>
     * {@code
     * new LazyReact().withAutoOptimize(false)
     *                .of(1,2,3,4)
                      .map(this::performIO)
                      .peekSync(this::cpuBoundTaskNoResult)
                      .run();
     *
     * }</pre>
     * @see com.aol.cyclops2.react.reactiveStream.traits.LazySimpleReactStream#peekSync(java.util.function.Consumer)
     */
    @Override
    default FutureStream<U> peekSync(final Consumer<? super U> consumer) {
        return (FutureStream<U>) LazySimpleReactStream.super.peekSync(consumer);
    }

    /**
     * closes all open queues.
     */
    default void closeAll() {
        getSubscription().closeAll();
    }

    /**
     * Turns this FutureStream into a HotStream, a connectable Stream, being executed on a thread on the
     * in it's current task executor, that is producing data
     * <pre>
     * {@code
     *  HotStream<Integer> ints = new LazyReact().range(0,Integer.MAX_VALUE)
                                                .hotStream()
        ints.connect().forEach(System.out::println);
     *  //print out all the ints
     *  //multiple consumers are possible, so other Streams can connect on different Threads
     *
     * }
     * </pre>
     * @return a Connectable HotStream
     */
    default HotStream<U> hotStream() {
        return Streams.hotStream(this, getTaskExecutor());

    }

    /* 
     * @see cyclops2.reactiveStream.ReactiveSeq#findFirst()
     */
    @Override
    default Optional<U> findFirst() {
        final List<U> results = this.run(Collectors.toList());
        if (results.size() == 0)
            return Optional.empty();
        return Optional.of(results.get(0));
    }

    /**
     * Convert between an Lazy and Eager SimpleReact Stream, can be used to take
     * advantages of each approach during a single Stream
     *
     * Allows callers to take advantage of functionality only available in
     * SimpleReactStreams such as allOf
     *
     * <pre>
     * {@code
     * LazyReact.parallelCommonBuilder()
     * 						.react(()->slow(),()->1,()->2)
     * 						.peek(System.out::println)
     * 						.convertToSimpleReact()
     * 						.allOf(list->list)
     * 						.block()
     * }
     * </pre>
     *
     * @return An SimpleReactStream from this FutureStream, will use the
     *         same executors
     */
    default SimpleReactStream<U> convertToSimpleReact() {
        return new SimpleReact(
                               getTaskExecutor()).withRetrier(getRetrier())
                                                 .fromStream((Stream) getLastActive().injectFutures()
                                                                                     .map(f -> {
                                                                                         try {
                                                                                             return CompletableFuture.completedFuture(f.join());
                                                                                         } catch (final Throwable t) {
                                                                                             return new CompletableFuture().completeExceptionally(t);
                                                                                         }
                                                                                     }));

    }

    /*
     * Apply a function to all items in the reactiveStream.
     * <pre>
     * {@code
     *  LazyReact.sequentialBuilder().react(()->1,()->2,()->3)
                                             .map(it->it+100) //add 100
                                             .toList();
     * }
     * //results in [100,200,300]
     * </pre>
     *	@param mapper Function to be applied to all items in the Stream
     *	@return
     * @see com.aol.cyclops2.react.reactiveStream.traits.FutureStream#map(java.util.function.Function)
     */
    @Override
    default <R> FutureStream<R> map(final Function<? super U, ? extends R> mapper) {

        return (FutureStream<R>) LazySimpleReactStream.super.then((Function) mapper);
    }

    /**
     * Break a reactiveStream into multiple Streams based of some characteristic of the
     * elements of the Stream
     *
     * e.g.
     *
     * <pre>
     * <code>
     *
     * FutureStream.of(10,20,25,30,41,43).shard(ImmutableMap.of("even",new
     * 															Queue(),"odd",new Queue(),element-&gt; element%2==0? "even" : "odd");
     *
     * </code>
     * </pre>
     *
     * results in 2 Streams "even": 10,20,30 "odd" : 25,41,43
     *
     * @param shards
     *            Map of Queue's keyed by shard identifier
     * @param sharder
     *            Function to split split incoming elements into shards
     * @return Map of new sharded Streams
     */
    default <K> Map<K, FutureStream<U>> shard(final Map<K, Queue<U>> shards, final Function<? super U, ? extends K> sharder) {

        toQueue(shards, sharder);
        final Map res = shards.entrySet()
                              .stream()
                              .collect(Collectors.toMap(e -> e.getKey(), e -> fromStream(e.getValue()
                                                                                          .stream(getSubscription()))));
        return res;
    }

    /**
     * Can be used to debounce (accept a single data point from a unit of time)
     * data. This drops data. For a method that slows emissions and keeps data
     * #see#onePer
     *
     * <pre>
     * {@code
     * FutureStream.of(1,2,3,4,5,6)
     *          .debounce(1000,TimeUnit.SECONDS).toList();
     *
     * // [1]
     * }
     * </pre>
     *
     * @param time
     *            Time from which to accept only one element
     * @param unit
     *            Time unit for specified time
     * @return Next stage of reactiveStream, with only 1 element per specified time
     *         windows
     */
    @Override
    default FutureStream<U> debounce(final long time, final TimeUnit unit) {
        return fromStream(Streams.debounce(stream(),time, unit));

    }

    /**
     * Allows clients to control the emission of data for the next phase of the
     * Stream. The user specified function can delay, drop, or change elements
     *
     * @param fn
     *            Function takes a supplier, which can be used repeatedly to get
     *            the next value from the Stream. If there are no more values, a
     *            ClosedQueueException will be thrown. This function should
     *            return a Supplier which returns the desired result for the
     *            next element (or just the next element).
     * @return Next stage in Stream
     */
    default FutureStream<U> control(final Function<Supplier<U>, Supplier<U>> fn) {
        final Queue queue = toQueue();
        return fromStream(queue.streamControl(getSubscription(), fn));
    }

    /**
     * Batch elements into a Stream of collections with user defined function
     *
     * @param fn
     *            Function takes a supplier, which can be used repeatedly to get
     *            the next value from the Stream. If there are no more values, a
     *            ClosedQueueException will be thrown. This function should
     *            return a Supplier which creates a collection of the batched
     *            values
     * @return Stream of batched values
     */
    default <C extends Collection<U>> FutureStream<C> group(final Function<Supplier<U>, Supplier<C>> fn) {
        final Queue queue = toQueue();
        return fromStream(queue.streamBatchNoTimeout(getSubscription(), fn));
    }

    /*
     * Batch the elements in the Stream by a combination of Size and Time
     * If batch exceeds max size it will be split
     * If batch exceeds max time it will be split
     * Excludes Null values (neccessary for timeout handling)
     *
     * <pre>
     * {@code
     * assertThat(react(()->1,()->2,()->3,()->4,()->5,()->{sleep(100);return 6;})
                        .batchBySizeAndTime(30,60,TimeUnit.MILLISECONDS)
                        .toList()
                        .get(0)
                        ,not(hasItem(6)));
        }
     * </pre>
     *
     * <pre>
     * {@code
     *
        assertThat(of(1,2,3,4,5,6).batchBySizeAndTime(3,10,TimeUnit.SECONDS).toList().get(0).size(),is(3));
    
     * }</pre>
     *
     *	@param size Max batch size
     *	@param time Max time length
     *	@param unit time unit
     *	@return batched reactiveStream
     * @see com.aol.cyclops2.react.reactiveStream.traits.FutureStream#batchBySizeAndTime(int, long, java.util.concurrent.TimeUnit)
     */
    @Override
    default FutureStream<ListX<U>> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedBySizeAndTime(size, time, unit));
     }
    @Override
    default <C extends Collection<? super U>,R> FutureStream<R> groupedBySizeAndTime(final int size, final long time,
                                                                            final TimeUnit unit,
                                                                            final Supplier<C> factory,
                                                                            Function<? super C, ? extends R> finalizer
    ){
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .groupedBySizeAndTime(size, time, unit,factory,finalizer));
    }


    /**
     * Batch the elements in this reactiveStream into Collections of specified size The
     * type of Collection is determined by the specified supplier
     *
     * <pre>
     * {@code
     * 		FutureStream.of(1,1,1,1,1,1)
     * 						.batchBySize(3,()->new TreeSet<>())
     * 						.toList()
     *
     *   //[[1],[1]]
     * }
     *
     * </pre>
     *
     * @param size
     *            Size of batch
     * @param supplier
     *            Create the batch holding collection
     * @return Stream of Collections
     */
    @Override
    default <C extends Collection<? super U>> FutureStream<C> grouped(final int size, final Supplier<C> supplier) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .grouped(size, supplier));

    }

    /**
     * Introduce a random delay between events in a reactiveStream Can be used to
     * prevent behaviour synchronizing within a system
     *
     * <pre>
     * {@code
     *
     * FutureStream.parallelCommonBuilder()
     * 						.of(IntStream.range(0, 100))
     * 						.map(it -> it*100)
     * 						.jitter(10l)
     * 						.peek(System.out::println)
     * 						.block();
     *
     * }
     *
     * </pre>
     *
     * @param jitterInNanos
     *            Max number of nanos for jitter (random number less than this
     *            will be selected)/
     * @return Next stage in Stream with jitter applied
     */
    @Override
    default FutureStream<U> jitter(final long jitterInNanos) {
        return fromStream(Streams.jitter(stream(),jitterInNanos));
    }

    /**
     * Apply a fixed delay before emitting elements to the next phase of the
     * Stream. Note this doesn't neccessarily imply a fixed delay between
     * element creation (although it may do). e.g.
     *
     * <pre>
     * {@code
     *    FutureStream.of(1,2,3,4)
     *                    .fixedDelay(1,TimeUnit.hours);
     * }
     * </pre>
     *
     * Will emit 1 on skip, transform 2 after an hour, 3 after 2 hours and so on.
     *
     * However all 4 numbers will be populated in the Stream immediately.
     *
     * <pre>
     * {@code
     * FutureStream.of(1,2,3,4)
     *                 .withQueueFactories(QueueFactories.boundedQueue(1))
     *                 .fixedDelay(1,TimeUnit.hours);
     *
     * }
     * </pre>
     *
     * Will populate each number in the Stream an hour apart.
     *
     * @param time
     *            amount of time between emissions
     * @param unit
     *            TimeUnit for emissions
     * @return Next Stage of the Stream
     */
    @Override
    default FutureStream<U> fixedDelay(final long time, final TimeUnit unit) {
        return fromStream(Streams.fixedDelay(stream(),time, unit));
    }

    /**
     * Slow emissions down, emiting one element per specified time period
     *
     * <pre>
     * {@code
     * 		FutureStream.of(1,2,3,4,5,6)
     * 						 .onePer(1000,TimeUnit.NANOSECONDS)
     * 						 .toList();
     *
     * }
     *
     * </pre>
     *
     * @param time
     *            Frequency period of element emission
     * @param unit
     *            Time unit for frequency period
     * @return Stream with emissions slowed down by specified emission frequency
     */
    @Override
    default FutureStream<U> onePer(final long time, final TimeUnit unit) {
        return fromStream(Streams.onePer(stream(),time, unit));

    }

    /**
     * Allows x (specified number of) emissions with a time period before
     * stopping emmissions until specified time has elapsed since last emission
     *
     * <pre>
     * {@code
     *    FutureStream.of(1,2,3,4,5,6)
     *    				   .xPer(6,100000000,TimeUnit.NANOSECONDS)
     *    				   .toList();
     *
     * }
     *
     * </pre>
     *
     * @param x
     *            Number of allowable emissions per time period
     * @param time
     *            Frequency time period
     * @param unit
     *            Frequency time unit
     * @return Stream with emissions slowed down by specified emission frequency
     */
    @Override
    default FutureStream<U> xPer(final int x, final long time, final TimeUnit unit) {
        return fromStream(Streams.xPer(stream(),x, time, unit));
    }

    /**
     * Organise elements in a Stream into a Collections based on the time period
     * they pass through this stage
     *
     * <pre>
     * {@code
     * 	FutureStream.react(()->load1(),()->load2(),()->load3(),()->load4(),()->load5(),()->load6())
     * 					.batchByTime(15000,TimeUnit.MICROSECONDS);
     *
     * }
     *
     * </pre>
     *
     * @param time
     *            Time period during which all elements should be collected
     * @param unit
     *            Time unit during which all elements should be collected
     * @return Stream of Lists
     */
    @Override
    default FutureStream<ListX<U>> groupedByTime(final long time, final TimeUnit unit) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedByTime(time, unit));

    }

    /**
     * Organise elements in a Stream into a Collections based on the time period
     * they pass through this stage
     *
     * <pre>
     * {@code
     * List <TreeSet<Integer>> set = FutureStream.ofThread(1,1,1,1,1,1)
     *                                               .batchByTime(1500,TimeUnit.MICROSECONDS,()-> new TreeSet<>())
     *                                               .block();
    
            assertThat(set.get(0).size(),is(1));
     *
     * }
     * </pre>
     *
     *
     * @param time
     *            Time period during which all elements should be collected
     * @param unit
     *            Time unit during which all elements should be collected
     * @param factory
     *            Instantiates the collections used in the batching
     * @return Stream of collections
     */
    @Override
    default <C extends Collection<? super U>> FutureStream<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedByTime(time, unit, factory));

    }
    @Override
    default <C extends Collection<? super U>,R> FutureStream<R> groupedByTime(final long time, final TimeUnit unit,
                                                                            final Supplier<C> factory,
                                                                            Function<? super C, ? extends R> finalizer) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .groupedByTime(time, unit, factory,finalizer));

    }
    /*
     *
     * React to new events with the supplied function on the supplied
     * Executor
     *
     * @param fn Apply to incoming events
     *
     * @param service Service to execute function on
     *
     * @return next stage in the Stream
     */
    @Override
    default <R> FutureStream<R> then(final Function<? super U, ? extends R> fn, final Executor service) {
        return (FutureStream<R>) LazySimpleReactStream.super.then(fn, service);
    }

    /*
     * Non-blocking asyncrhonous application of the supplied function.
     * Equivalent to map from Streams / Seq apis.
     *
     * @param fn Function to be applied asynchronously
     *
     * @return Next stage in reactiveStream
     *
     * @see
     * com.aol.simple.react.reactiveStream.traits.FutureStream#transform(java.util.function
     * .Function)
     */
    @Override
    default <R> FutureStream<R> then(final Function<? super U, ? extends R> fn) {
        return (FutureStream) LazySimpleReactStream.super.then(fn);
    }

    /**
     * Copy this Stream the specified number of times
     *
     * <pre>
     * {@code
     * FutureStream.of(1,2,3,4,5,6)
                .map(i->i+2)
                .copy(5)
                .forEach(s -> System.out.println(s.toList()));
     *
     * }</pre>
     *
     * @param times to copy this Stream
     * @return List with specified number of copies
     */
    default List<FutureStream<U>> copy(final int times) {
        return (List) LazySimpleReactStream.super.copySimpleReactStream(times);

    }

    /**
     * Merges this reactiveStream and the supplied Streams into a single Stream where the next value
     * is the next returned across any of the involved Streams. Suitable for merging infinite streams
     *
     * <pre>
     * {@code
     * 	FutureStream<Integer> fast =  ... //  [1,2,3,4,5,6,7..]
     * 	FutureStream<Integer> slow =  ... //  [100,200,300,400,500,600..]
     *
     *  FutureStream<Integer> merged = fast.switchOnNextValue(Stream.of(slow));  //[1,2,3,4,5,6,7,8,100,9,10,11,12,13,14,15,16,200..]
     * }
     * </pre>
     *
     * @param streams
     * @return
     */
    default <R> FutureStream<R> switchOnNextValue(final Stream<FutureStream> streams) {
        final Queue queue = Queue.createMergeQueue();
        addToQueue(queue);
        streams.forEach(s -> s.addToQueue(queue));

        return fromStream(queue.stream(this.getSubscription()));
    }

    /**
     * Merges this reactiveStream and the supplied Streams into a single Stream where the next value
     * is the next returned across any of the involved Streams. Suitable for merging infinite streams
     *
     * <pre>
     * {@code
     * 	FutureStream<Integer> fast =  ... //  [1,2,3,4,5,6,7..]
     * 	FutureStream<Integer> slow =  ... //  [100,200,300,400,500,600..]
     *
     *  FutureStream<Integer> merged = fast.mergeLatest(slow);  //[1,2,3,4,5,6,7,8,100,9,10,11,12,13,14,15,16,200..]
     * }
     * </pre>
     *
     * @param streams
     * @return
     */
    default <R> FutureStream<R> mergeLatest(final FutureStream<?>... streams) {
        final Queue queue = Queue.createMergeQueue();
        addToQueue(queue);
        Seq.of(streams)
           .forEach(s -> s.addToQueue(queue));

        return fromStream(queue.stream(this.getSubscription()));
    }

    /*
     * Define failure handling for this stage in a reactiveStream. Recovery function
     * will be called after an exception Will be passed a
     * SimpleReactFailedStageException which contains both the cause, and the
     * input value.
     *
     * <pre>
     * {@code
     * List<String> results = LazyReact.sequentialCommonBuilder()
                                        .withRetrier(retrier)
                                        .react(() -> "new event1", () -> "new event2")
                                        .retry(this::unreliable)
                                        .onFail(e -> "default")
                                        .peek(System.out::println)
                                        .capture(Throwable::printStackTrace)
                                        .block();
     *
     * }
     * </pre>
     *
     * @param fn Recovery function
     *
     * @return Next stage in reactiveStream
     *
     * @see
     * com.aol.simple.react.reactiveStream.traits.FutureStream#onFail(java.util.function
     * .Function)
     */
    @Override
    default FutureStream<U> onFail(final Function<? super SimpleReactFailedStageException, ? extends U> fn) {
        return (FutureStream) LazySimpleReactStream.super.onFail(fn);
    }

    /*
     * Handle failure for a particular class of exceptions only
     *
     * @param exceptionClass Class of exceptions to handle
     *
     * @param fn recovery function
     *
     * @return recovered value
     *
     * @see
     * com.aol.simple.react.reactiveStream.traits.FutureStream#onFail(java.lang.Class,
     * java.util.function.Function)
     */
    @Override
    default FutureStream<U> onFail(final Class<? extends Throwable> exceptionClass,
                                   final Function<? super SimpleReactFailedStageException, ? extends U> fn) {
        return (FutureStream) LazySimpleReactStream.super.onFail(exceptionClass, fn);
    }

    /*
     * Capture non-recoverable exception
     *
     * <pre>
     * {@code
     *  FutureStream.of(1, "a", 2, "b", 3, null)
     *                  .capture(e-> e.printStackTrace())
                        .peek(it ->System.out.println(it))
                        .cast(Integer.class)
                        .peek(it ->System.out.println(it))
                        .toList();
    
     *  //prints the ClasCastException for failed Casts
     * }
     * </pre>
     * @param errorHandler Consumer that captures the exception
     *
     * @return Next stage in reactiveStream
     *
     * @see
     * com.aol.simple.react.reactiveStream.traits.FutureStream#capture(java.util.function
     * .Consumer)
     */
    @Override
    default FutureStream<U> capture(final Consumer<Throwable> errorHandler) {
        return (FutureStream) LazySimpleReactStream.super.capture(errorHandler);
    }

    /*
     * @see
     * com.aol.simple.react.reactiveStream.traits.FutureStream#peek(java.util.function
     * .Consumer)
     */
    @Override
    default FutureStream<U> peek(final Consumer<? super U> consumer) {
        return (FutureStream) LazySimpleReactStream.super.peek(consumer);
    }

    /*
     * @see
     * com.aol.simple.react.reactiveStream.traits.FutureStream#filter(java.util.function
     * .Predicate)
     */
    @Override
    default FutureStream<U> filter(final Predicate<? super U> p) {
        return (FutureStream) LazySimpleReactStream.super.filter(p);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.simple.react.reactiveStream.FutureStreamImpl#flatMap(java.util.function
     * .Function)
     */
    @Override
    default <R> FutureStream<R> flatMap(final Function<? super U, ? extends Stream<? extends R>> flatFn) {
        return  (FutureStream<R>)LazySimpleReactStream.super.flatMap(flatFn);
    }

    @Override
    default <R> FutureStream<R> flatMapAnyM(final Function<? super U, AnyM<Witness.stream,? extends R>> flatFn) {

        return (FutureStream<R>) LazySimpleReactStream.super.flatMap(flatFn.andThen(anyM -> Witness.stream(anyM)));
    }

    /**
     * Perform a flatMap operation where the CompletableFuture type returned is flattened from the resulting Stream
     * If in async mode this operation is performed asyncrhonously
     * If in sync mode this operation is performed synchronously
     *
     * <pre>
     * {@code
     * assertThat( new LazyReact()
                                        .of(1,2,3)
                                        .flatMapCompletableFuture(i->CompletableFuture.completedFuture(i))
                                        .block(),equalTo(Arrays.asList(1,2,3)));
     * }
     * </pre>
     * In this example the result of the flatMapCompletableFuture is 'flattened' to the raw integer values
     *
     *
     * @param flatFn flatMap function
     * @return Flatten Stream with flatFn applied
     */
    default <R> FutureStream<R> flatMapCompletableFuture(final Function<? super U, CompletableFuture<? extends R>> flatFn) {
        return fromStream(Streams.flatMapCompletableFuture(stream(), flatFn));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.simple.react.reactiveStream.FutureStreamImpl#retry(java.util.function
     * .Function)
     */
    @Override
    default <R> FutureStream<R> retry(final Function<? super U, ? extends R> fn) {

        return (FutureStream) LazySimpleReactStream.super.retry(fn);
    }

    /*
     * Convert the specified Stream to a FutureStream, using the configuration
     * of this FutureStream (task executors, current config settings)
     *
     * @see com.aol.cyclops2.react.reactiveStream.traits.SimpleReactStream#fromStream(java.util.reactiveStream.Stream)
     */
    @Override
    default <R> FutureStream<R> fromStream(final Stream<R> stream) {

        return this.withLastActive(getLastActive().withNewStream(stream, this.getSimpleReact()));
    }

    /*
     * Convert the specified Stream to a FutureStream, using the configuration
     * of this FutureStream (task executors, current config settings)
     *
     * (non-Javadoc)
     *
     * @see
     * com.aol.simple.react.reactiveStream.FutureStreamImpl#fromStreamCompletableFuture
     * (java.util.reactiveStream.Stream)
     */

    default <R> FutureStream<R> fromStreamOfFutures(final Stream<FastFuture<R>> stream) {

        return (FutureStream) this.withLastActive(getLastActive().withNewStreamFutures(stream.map(f -> f.toCompletableFuture())));
    }

    /**
     * Concatenate two streams operating on the underlying futures rather than results.
     *
     *
     * // (1, 2, 3, 4, 5, 6) FutureStream.of(1, 2,
     * 3).concat(FutureStream.of(4, 5, 6))
     *
     *
     * @see #concat(Stream[])
     */
    @SuppressWarnings({ "unchecked" })
    default FutureStream<U> concat(final Stream<? extends U> other) {
        return fromStream(Stream.concat(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false),
                                        StreamSupport.stream(other.spliterator(), false)));

    }

    /**
    * Concatenate two streams operating on the underlying futures rather than results.

    * <pre>
    * {@code
    * // (1, 2, 3, 4)
    * FutureStream.of(1, 2, 3).concat(4)
    * }
    * </pre>
    *
    * @see #concat(Stream[])
    */
    default FutureStream<U> concat(final U other) {
        return concat(Stream.of(other));
    }

    /**
     * Concatenate two streams. operating on the underlying futures rather than results.
     *
     * <pre>
     * {@code
     * // (1, 2, 3, 4, 5, 6)
     * FutureStream.of(1, 2, 3).concat(4, 5, 6)
     * }
     * </pre>
     *
     * @see #concat(Stream[])
     */
    @SuppressWarnings({ "unchecked" })
    default FutureStream<U> concat(final U... other) {
        return concat(Stream.of(other));
    }

    /*
     * Cast all elements in this reactiveStream to specified type. May throw {@link
     * ClassCastException}.
     *
     * FutureStream.of(1, "a", 2, "b", 3).cast(Integer.class)
     *
     * will throw a ClassCastException
     *
     * @param type Type to cast to
     *
     * @return FutureStream
     *
     * @see
     * com.aol.simple.react.reactiveStream.traits.FutureStream#cast(java.lang.Class)
     */
    @Override
    default <U> FutureStream<U> cast(final Class<? extends U> type) {
        return (FutureStream) LazySimpleReactStream.super.cast(type);

    }

    /**
     * Keep only those elements in a reactiveStream that are of a given type.
     *
     *
     *
     * FutureStream.of(1, "a", 2, "b", 3).ofType(Integer.class)
     *
     * gives a Stream of (1,2,3)
     *
     * FutureStream.of(1, "a", 2, "b", 3).ofType(String.class)
     *
     * gives a Stream of ("a","b")
     *
     */
    @Override
    default <U> FutureStream<U> ofType(final Class<? extends U> type) {
        return (FutureStream) LazySimpleReactStream.super.ofType(type);
    }

    /**
     * Returns a reactiveStream with a given value interspersed between any two values
     * of this reactiveStream.
     *
     * <code>
     *
     * // (1, 0, 2, 0, 3, 0, 4)
     *
     * FutureStream.of(1, 2, 3, 4).intersperse(0)
     *
     * </code>
     *
     * @see #intersperse(Stream, Object)
     */
    @Override
    default FutureStream<U> intersperse(final U value) {
        return (FutureStream) LazySimpleReactStream.super.intersperse(value);
    }

    /*
     *
     * FutureStream.of(1,2,3,4).limit(2)
     *
     * Will result in a Stream of (1,2). Only the first two elements are used.
     *
     * @param maxSize number of elements to take
     *
     * @return Limited FutureStream
     *
     * @see org.jooq.lambda.Seq#limit(long)
     */
    @Override
    default FutureStream<U> limit(final long maxSize) {
        final Continueable sub = this.getSubscription();
        sub.registerLimit(maxSize);
        return fromStream(ReactiveSeq.oneShotStream(toQueue().stream(sub))
                                     .limit(maxSize));

    }

    /* (non-Javadoc)
     * @see cyclops2.reactiveStream.ReactiveSeq#take(long)
     */
    @Override
    default FutureStream<U> drop(final long drop) {
        return skip(drop);
    }

    /* (non-Javadoc)
     * @see cyclops2.reactiveStream.ReactiveSeq#take(long)
     */
    @Override
    default FutureStream<U> take(final long take) {
        return limit(take);
    }

    @Override
    default FutureStream<U> takeWhile(final Predicate<? super U> p) {

        return this.limitWhile(p);
    }

    @Override
    default FutureStream<U> dropWhile(final Predicate<? super U> p) {

        return this.skipWhile(p);
    }

    @Override
    default FutureStream<U> takeUntil(final Predicate<? super U> p) {

        return this.limitUntil(p);
    }

    @Override
    default FutureStream<U> dropUntil(final Predicate<? super U> p) {

        return this.skipUntil(p);
    }

    @Override
    default FutureStream<U> dropRight(final int num) {

        return this.skipLast(num);
    }

    @Override
    default FutureStream<U> takeRight(final int num) {

        return this.limitLast(num);
    }

    /*
     * FutureStream.of(1,2,3,4).skip(2)
     *
     * Will result in a reactiveStream of (3,4). The first two elements are skipped.
     *
     * @param n Number of elements to skip
     *
     * @return FutureStream missing skipped elements
     *
     * @see org.jooq.lambda.Seq#skip(long)
     */
    @Override
    default FutureStream<U> skip(final long n) {
        final Continueable sub = this.getSubscription();
        sub.registerSkip(n);
        return fromStream(ReactiveSeq.oneShotStream(toQueue().stream(sub))
                                     .skip(n));

    }

    /*
     * @return distinct elements in this Stream (must be a finite reactiveStream!)
     *
     * @see org.jooq.lambda.Seq#distinct()
     */
    @Override
    default FutureStream<U> distinct() {

        return fromStream(stream()
                                   .distinct());
    }

    /**
     * Create a sliding view over this Stream
     *
     * <pre>
     * {@code
     * //futureStream of [1,2,3,4,5,6]
     *
     * List<List<Integer>> list = futureStream.sliding(2)
                                    .collect(Collectors.toList());
    
    
        assertThat(list.get(0),hasItems(1,2));
        assertThat(list.get(1),hasItems(2,3));
     * }
     * </pre>
     * @param size
     *            Size of sliding window
     * @return Stream with sliding view over data in this reactiveStream
     */
    @Override
    default FutureStream<PVectorX<U>> sliding(final int size) {
        //    return this.fromStream(SlidingWindow.sliding(this,size, 1));
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .sliding(size));
    }

    /**
     * Create a sliding view over this Stream
     *
     * <pre>
     * {@code
     * //futureStream of [1,2,3,4,5,6,7,8]
     *
     * List<List<Integer>> list = futureStream.sliding(3,2)
                                    .collect(Collectors.toList());
    
    
        assertThat(list.get(0),hasItems(1,2,3));
        assertThat(list.get(1),hasItems(3,4,5));
     * }
     * </pre>
     * @param size
     *            Size of sliding window
     * @return Stream with sliding view over data in this reactiveStream
     */
    @Override
    default FutureStream<PVectorX<U>> sliding(final int size, final int increment) {

        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .sliding(size, increment));

    }



    /*
     * <pre>
     * {@code
     *
     * // tuple((1, 2, 3), (1, 2, 3))
     *
     * FutureStream.of(1, 2, 3).duplicate()
     * }
     * </pre>
     *
     * @see FutureStream#copy(int)
     *
     * @see #duplicate(Stream)
     */
    default Tuple2<FutureStream<U>, FutureStream<U>> duplicateFutureStream() {
        final Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> duplicated = this.duplicate();
        return new Tuple2(
                          fromStream(duplicated.v1), fromStream(duplicated.v2));
    }


    /**
     * Partition a reactiveStream in two given a predicate. Two LazyFutureStreams are
     * returned but Seq interface specifies return type is Seq. See partitionFutureStream to
     * see an alternative which returns FutureStream
     *
     * <code>
     *
     * // tuple((1, 3, 5), (2, 4, 6))
     *
     * FutureStream.of(1, 2, 3, 4, 5,6).partition(i -&gt; i % 2 != 0)
     *
     * </code>
     *
     * @see #partitionFutureStream(Predicate)
     * @see #partition(Stream, Predicate)
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> partition(final Predicate<? super U> predicate) {
        return ReactiveSeq.oneShotStream(stream())
                .partition(predicate);
    }

    /**
     * Partition an FutureStream into two LazyFutureStreams given a
     * predicate.
     * <pre>
     * {@code
     * FutureStream.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)
     *
     * results in
     *
     * tuple((1, 3, 5), (2, 4, 6))
     * }</pre>
     * @param predicate
     *            Predicate to split Stream
     * @return FutureStream
     * @see #partition(Predicate)
     */
    default Tuple2<FutureStream<U>, FutureStream<U>> partitionFutureStream(final Predicate<? super U> predicate) {
        final Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> partition = partition(predicate);
        return new Tuple2(
                          fromStream(partition.v1), fromStream(partition.v2));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#slice(long, long)
     */
    @Override
    default FutureStream<U> slice(final long from, final long to) {

        return fromStream(ReactiveSeq.oneShotStream(stream()
                                                          .slice(from, to)));
    }

    /**
     * Zip a Stream with a corresponding Stream of indexes.
     *
     * <code>
     *
     * // (tuple("a", 0), tuple("b", 1), tuple("c", 2))
     *
     * FutureStream.of("a","b", "c").zipWithIndex()
     *
     *</code>
     *
     * @see #zipWithIndex(Stream)
     *
     *
     */
    @Override
    default FutureStream<Tuple2<U, Long>> zipWithIndex() {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .zipWithIndex());
    }



    @Override
    default <T> FutureStream<Tuple2<U, T>> zipS(final Stream<? extends T> other) {
        return fromStream(LazyFutureStreamFunctions.zip(this, other));
    }

    @Override
    default <T> FutureStream<Tuple2<U, T>> zip(final Iterable<? extends T> other) {
        return fromStream(LazyFutureStreamFunctions.zip(this, ReactiveSeq.fromIterable(other)));
    }



    @Override
    default <T, R> FutureStream<R> zipS(final Stream<? extends T> other, final BiFunction<? super U, ? super T, ? extends R> zipper) {
        return fromStream(LazyFutureStreamFunctions.zip(this, ReactiveSeq.oneShotStream(other), zipper));
    }

    @Override
    default <T, R> FutureStream<R> zip(final Iterable<? extends T> other, final BiFunction<? super U, ? super T, ? extends R> zipper) {
        return fromStream(LazyFutureStreamFunctions.zip(this, ReactiveSeq.fromIterable(other), zipper));
    }

    /**
     * Scan a reactiveStream to the left.
     *
     *
     * // ("", "a", "ab", "abc") FutureStream.of("a", "b", "c").scanLeft("",
     * (u, t) &gt; u + t)
     *
     */
    @Override
    default <T> FutureStream<T> scanLeft(final T seed, final BiFunction<? super T, ? super U, ? extends T> function) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .scanLeft(seed, function));

    }

    /**
     * Scan a reactiveStream to the right. - careful with infinite streams!
     *
     *
     * // ("", "c", "cb", "cba") FutureStream.of("a", "b",
     * "c").scanRight("", (t, u) &gt; u + t)
     *
     */
    @Override
    default <R> FutureStream<R> scanRight(final R seed, final BiFunction<? super U, ? super R, ? extends R> function) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .scanRight(seed, function));

    }

    @Override
    default FutureStream<U> scanRight(final Monoid<U> monoid) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .scanRight(monoid));

    }

    /**
     * Reverse a reactiveStream. - eager operation that materializes the Stream into a list - careful with infinite streams!
     *
     *
     * // (3, 2, 1) FutureStream.of(1, 2, 3).reverse()
     *
     */
    @Override
    default FutureStream<U> reverse() {
        //reverse using FutureStream semantics to ensure concurrency / parallelism
        return fromStream(fromStream(stream()).block()
                                                                         .reverse()
                                                                         .stream());

    }

    /**
     * Shuffle a reactiveStream
     *
     *
     * // e.g. (2, 3, 1) FutureStream.of(1, 2, 3).shuffle()
     *
     */
    @Override
    default FutureStream<U> shuffle() {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .shuffle());
    }

    /**
     * Shuffle a reactiveStream using specified source of randomness
     *
     *
     * // e.g. (2, 3, 1) FutureStream.of(1, 2, 3).shuffle(new Random())
     *
     */
    @Override
    default FutureStream<U> shuffle(final Random random) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .shuffle(random));
    }

    /**
     * Returns a reactiveStream with all elements skipped for which a predicate
     * evaluates to true.
     *
     *
     * // (3, 4, 5) FutureStream.of(1, 2, 3, 4, 5).skipWhile(i &gt; i &lt;
     * 3)
     *
     *
     * @see #skipWhile(Stream, Predicate)
     */
    @Override
    default FutureStream<U> skipWhile(final Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .skipWhile(predicate));
    }

    /**
     * Returns a reactiveStream with all elements skipped for which a predicate
     * evaluates to false.
     *
     *
     * // (3, 4, 5) FutureStream.of(1, 2, 3, 4, 5).skipUntil(i &gt; i == 3)
     *
     *
     * @see #skipUntil(Stream, Predicate)
     */
    @Override
    default FutureStream<U> skipUntil(final Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .skipUntil(predicate));
    }

    /**
     * Returns a reactiveStream limited to all elements for which a predicate evaluates
     * to true.
     *
     *
     * // (1, 2) FutureStream.of(1, 2, 3, 4, 5).limitWhile(i -&gt; i &lt; 3)
     *
     *
     * @see #limitWhile(Stream, Predicate)
     */
    @Override
    default FutureStream<U> limitWhile(final Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .limitWhile(predicate));
    }

    /**
     * Returns a reactiveStream limited to all elements for which a predicate evaluates
     * to false.
     *
     *
     * // (1, 2) FutureStream.of(1, 2, 3, 4, 5).limitUntil(i &gt; i == 3)
     *
     *
     * @see #limitUntil(Stream, Predicate)
     */
    @Override
    default FutureStream<U> limitUntil(final Predicate<? super U> predicate) {
        return fromStream(LazyFutureStreamFunctions.limitUntil(this, predicate));
    }

    /**
     * Cross join 2 streams into one.
     * <p>
     * <pre>{@code
     * // (tuple(1, "a"), tuple(1, "b"), tuple(2, "a"), tuple(2, "b"))
     * FutureStream.of(1, 2).crossJoin(FutureStream.of("a", "b"))
     * }</pre>
     */
    default <T> FutureStream<Tuple2<U, T>> crossJoin(final Stream<? extends T> other) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).seq()
                                     .crossJoin(other));
    }

    /**
     * Produce this reactiveStream, or an alternative reactiveStream from the
     * {@code value}, in case this reactiveStream is empty.
     * <pre>
     * {@code
     *    FutureStream.of().onEmpty(1)
     *
     *     //1
     * }</pre>
     *
     *
     */
    @Override
    default FutureStream<U> onEmpty(final U value) {

        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .onEmpty(value));
    }

    /**
     * Produce this reactiveStream, or an alternative reactiveStream from the
     * {@code supplier}, in case this reactiveStream is empty.
     *
     * <pre>
     * {@code
     *    FutureStream.of().onEmptyGet(() -> 1)
     *
     *
     *  //1
     * }
     * </pre>
     *
     */
    @Override
    default FutureStream<U> onEmptyGet(final Supplier<? extends U> supplier) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .onEmptyGet(supplier));
    }

    /**
     * Produce this reactiveStream, or an alternative reactiveStream from the
     * {@code supplier}, in case this reactiveStream is empty.
     *
     * <pre>
     * {@code
     *   FutureStream.of().capture(e -> ex = e).onEmptyThrow(() -> new RuntimeException()).toList();
     *
     *   //throws RuntimeException
     * }
     * </pre>
     *
     */
    @Override
    default <X extends Throwable> FutureStream<U> onEmptyThrow(final Supplier<? extends X> supplier) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).onEmptyThrow(supplier));
    }

    /**
     * Inner join 2 streams into one.
     *
     * <pre>
     * {@code
     * // (tuple(1, 1), tuple(2, 2))
     * FutureStream.of(1, 2, 3).innerJoin(Seq.of(1, 2), t -> Objects.equals(t.v1, t.v2))
     * }</pre>
     */
    default <T> FutureStream<Tuple2<U, T>> innerJoin(final Stream<? extends T> other, final BiPredicate<? super U, ? super T> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).seq()
                                     .innerJoin(other, predicate));

    }

    /**
     * Left outer join 2 streams into one.
     * <p>
     * <pre>
     * {@code
     * // (tuple(1, 1), tuple(2, 2), tuple(3, null))
     * FutureStream.of(1, 2, 3).leftOuterJoin(Seq.of(1, 2), t -> Objects.equals(t.v1, t.v2))
     * }</pre>
     */
    default <T> FutureStream<Tuple2<U, T>> leftOuterJoin(final Stream<? extends T> other, final BiPredicate<? super U, ? super T> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).seq()
                                     .leftOuterJoin(other, predicate));
    }

    /**
     * Right outer join 2 streams into one.
     * <p>
     * <pre>
     * {@code
     * // (tuple(1, 1), tuple(2, 2), tuple(null, 3))
     * FutureStream.of(1, 2).rightOuterJoin(Seq.of(1, 2, 3), t -> Objects.equals(t.v1, t.v2))
     * }</pre>
     */
    default <T> FutureStream<Tuple2<U, T>> rightOuterJoin(final Stream<? extends T> other, final BiPredicate<? super U, ? super T> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).seq()
                                     .rightOuterJoin(other, predicate));
    }

    /**
     * Create a Stream that infinitely cycles this Stream
     *
     * <pre>
     * {@code
     * assertThat(FutureStream.of(1,2,2).cycle().limit(6)
                                .collect(Collectors.toList()),
                                    equalTo(Arrays.asList(1,2,2,1,2,2));
     * }
     * </pre>
     * @return New cycling reactiveStream
     */
    @Override
    default FutureStream<U> cycle() {
        return fromStream(Streams.cycle(this));

    }




    /**
     * Repeat in a Stream while specified predicate holds
     * <pre>
     * {@code
     *  int count =0;
     *
        assertThat(FutureStream.of(1,2,2).cycleWhile(next -> count++<6 )
                                            .collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
     * }
     * </pre>
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    @Override
    default FutureStream<U> cycleWhile(final Predicate<? super U> predicate) {
        return cycle().limitWhile(predicate);
    }

    /**
     * Repeat in a Stream until specified predicate holds
     *
     * <pre>
     * {@code
     * 	count =0;
        assertThat(FutureStream.of(1,2,2,3).cycleUntil(next -> count++>10 )
                                            .collect(Collectors.toList()),equalTo(Arrays.asList(1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2)));
    
     * }
     * </pre>
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    @Override
    default FutureStream<U> cycleUntil(final Predicate<? super U> predicate) {

        return cycle().limitUntil(predicate);
    }


    /*
     *	@return Convert to standard JDK 8 Stream
     * @see com.aol.cyclops2.react.reactiveStream.traits.FutureStream#reactiveStream()
     */
    @Override
    default ReactiveSeq<U> stream() {
        return Streams.oneShotStream(toQueue().jdkStream(getSubscription()));

    }

    /*
     *	@return New version of this reactiveStream converted to execute asynchronously and in parallel
     * @see com.aol.cyclops2.react.reactiveStream.traits.FutureStream#parallel()
     */
    @Override
    default FutureStream<U> parallel() {
        return this.withAsync(true)
                   .withTaskExecutor(LazyReact.parallelBuilder()
                                              .getExecutor());
    }

    /*
     *	@return New version of this reactiveStream  converted to execute synchronously and sequentially
     * @see com.aol.cyclops2.react.reactiveStream.traits.FutureStream#sequential()
     */
    @Override
    default FutureStream<U> sequential() {
        return this.withAsync(false)
                   .withTaskExecutor(LazyReact.sequentialBuilder()
                                              .getExecutor());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jooq.lambda.Seq#unordered()
     */
    @Override
    default FutureStream<U> unordered() {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jooq.lambda.Seq#onClose(java.lang.Runnable)
     */
    @Override
    default FutureStream<U> onClose(final Runnable closeHandler) {
        getLastActive().stream()
                       .onClose(closeHandler);
        return this;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jooq.lambda.Seq#sorted()
     */
    @Override
    default FutureStream<U> sorted() {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .sorted());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jooq.lambda.Seq#sorted(java.util.Comparator)
     */
    @Override
    default FutureStream<U> sorted(final Comparator<? super U> comparator) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .sorted(comparator));
    }

    /**
     * Give a consumer access to this Stream
     *
     * @param consumer
     *            Consumer that will recieve current stage
     * @return Self (current stage)
     */
    default FutureStream<U> self(final Consumer<FutureStream<U>> consumer) {
        return then((t) -> {
            consumer.accept(this);
            return t;
        });

    }



    /*
     * Flatten this reactiveStream one level
     * * <pre>
     * {@code
     *  FutureStream.of(Arrays.asList(1,2))
     *                  .flatten();
     *
     *    // reactiveStream of 1,2
     *  }
     *  </pre>
     *
     * @see cyclops2.reactiveStream.ReactiveSeq#flatten()
     */
    public static <T1> FutureStream<T1> flatten(ReactiveSeq<? extends Stream<? extends T1>> nested){
        return FutureStream.lazyFutureStream(nested).flatMap(Function.identity());

    }
    public static <T1> FutureStream<T1> narrow(FutureStream<? extends T1> broad){
        return (FutureStream<T1>)broad;

    }

    /* Optional empty, if empty Stream. Otherwise collects to a List
     *	@return this Stream as an Optional
     * @see cyclops2.reactiveStream.ReactiveSeq#toOptional()
     */
    @Override
    default Optional<ListX<U>> toOptional() {
        return Optional.of(block())
                       .flatMap(list -> list.size() == 0 ? Optional.<ListX<U>> empty() : Optional.of(list));
    }

    /*
     * <pre>
     * {@code
     * FutureStream.of(1,2,3,4)
                        .toCompletableFuture()
    
        //Future of	[1,2,3,4]
     *
     * }
     * </pre>
     * Future is populated asynchronously using current Streams task executor
     * @return This Stream as a CompletableFuture
     * @see cyclops2.reactiveStream.ReactiveSeq#toCompletableFuture()
     */
    default CompletableFuture<ListX<U>> toCompletableFuture() {
        return CompletableFuture.completedFuture(this)
                                .thenApplyAsync(s -> s.block(), getTaskExecutor());
    }

    /*
     * @see java.util.reactiveStream.BaseStream#spliterator()
     */
    @Override
    default Spliterator<U> spliterator() {
        return stream().spliterator();
    }

    /*
     * @see java.util.reactiveStream.BaseStream#isParallel()
     */
    @Override
    default boolean isParallel() {
        return false;
    }

    /*
     * @see java.util.reactiveStream.Stream#mapToInt(java.util.function.ToIntFunction)
     */
    @Override
    default IntStream mapToInt(final ToIntFunction<? super U> mapper) {
        return stream().mapToInt(mapper);
    }

    /*
     * @see java.util.reactiveStream.Stream#mapToLong(java.util.function.ToLongFunction)
     */
    @Override
    default LongStream mapToLong(final ToLongFunction<? super U> mapper) {
        return stream().mapToLong(mapper);
    }

    /*
     * @see java.util.reactiveStream.Stream#mapToDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    default DoubleStream mapToDouble(final ToDoubleFunction<? super U> mapper) {
        return stream().mapToDouble(mapper);
    }

    /*
     * @see java.util.reactiveStream.Stream#flatMapToInt(java.util.function.Function)
     */
    @Override
    default IntStream flatMapToInt(final Function<? super U, ? extends IntStream> mapper) {
        return stream().flatMapToInt(mapper);
    }

    /*
     * @see java.util.reactiveStream.Stream#flatMapToLong(java.util.function.Function)
     */
    @Override
    default LongStream flatMapToLong(final Function<? super U, ? extends LongStream> mapper) {
        return stream().flatMapToLong(mapper);
    }

    /*
     * @see java.util.reactiveStream.Stream#flatMapToDouble(java.util.function.Function)
     */
    @Override
    default DoubleStream flatMapToDouble(final Function<? super U, ? extends DoubleStream> mapper) {
        return stream().flatMapToDouble(mapper);
    }

    /*
     * @see java.util.reactiveStream.Stream#forEachOrdered(java.util.function.Consumer)
     */
    @Override
    default void forEachOrdered(final Consumer<? super U> action) {
        stream().forEachOrdered(action);

    }

    /*
     * @see java.util.reactiveStream.Stream#toArray()
     */
    @Override
    default Object[] toArray() {
        return stream().toArray();
    }

    /*
     * @see java.util.reactiveStream.Stream#toArray(java.util.function.IntFunction)
     */
    @Override
    default <A> A[] toArray(final IntFunction<A[]> generator) {
        return stream().toArray(generator);
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#findAny()
     */
    @Override
    default Optional<U> findAny() {
       Object[] result = {null};
            //use forEachRemaining as it is the fast path for many operators
             forEach(e -> {
                result[0]=e;
                throw new SimpleReactProcessingException();
            });

        return  Optional.ofNullable((U)result[0]);
    }


    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#toSet()
     */
    @Override
    default Set<U> toSet() {
        return collect(Collectors.toSet());
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#toList()
     */
    @Override
    default List<U> toList() {
        return collect(Collectors.toList());
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#toCollection(java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<U>> C toCollection(final Supplier<C> collectionFactory) {
        return collect(Collectors.toCollection(collectionFactory));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#distinct(java.util.function.Function)
     */
    @Override
    default <R> FutureStream<U> distinct(final Function<? super U, ? extends R> keyExtractor) {
        return fromStream(stream()
                          .distinct());
    }

    /*
     * Duplicate a FutureStream into two equivalent Streams.
     * Two LazyFutureStreams are
     * returned but Seq interface specifies return type is Seq. See duplicateFutureStream to
     * see an alternative which returns FutureStream
     *
     * <pre>
     * {@code
     *
     * // tuple((1, 2, 3), (1, 2, 3))
     *
     * FutureStream.of(1, 2, 3).duplicate()
     * }
     * </pre>
     *
     * @see FutureStream#copy(int)
     *
     * @see #duplicate(Stream)
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> duplicate() {
        return Streams.duplicate(stream())
                .map1(ReactiveSeq::oneShotStream).map2(ReactiveSeq::oneShotStream);
    }
    /*
     * Duplicate a FutureStream into two equivalent Streams.
     * Two LazyFutureStreams are
     * returned but Seq interface specifies return type is Seq. See duplicateFutureStream to
     * see an alternative which returns FutureStream
     *
     * <pre>
     * {@code
     *
     * // tuple((1, 2, 3), (1, 2, 3))
     *
     * FutureStream.of(1, 2, 3).duplicate()
     * }
     * </pre>
     *
     * @see FutureStream#copy(int)
     *
     * @see #duplicate(Stream)
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> duplicate(Supplier<Deque<U>> bufferFactory) {
        return Streams.duplicate(stream(),bufferFactory)
                .map1(ReactiveSeq::oneShotStream).map2(ReactiveSeq::oneShotStream);
    }

    /*
     * Triplicate the data in this Stream. To triplicate into 3 LazyFutureStreams use actOnFutures#triplicate
     *
     * @see cyclops2.reactiveStream.ReactiveSeq#triplicate()
     */
    @Override
    default Tuple3<ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>> triplicate() {
        return Streams.triplicate(stream())
                .map1(ReactiveSeq::oneShotStream).map2(ReactiveSeq::oneShotStream)
                .map3(ReactiveSeq::oneShotStream);

    }

    /*
     * Triplicate the data in this Stream. To triplicate into 3 LazyFutureStreams use actOnFutures#triplicate
     *
     * @see cyclops2.reactiveStream.ReactiveSeq#triplicate()
     */
    @Override
    default Tuple3<ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>> triplicate(Supplier<Deque<U>> bufferFactory) {
        return Streams.triplicate(stream(),bufferFactory)
                .map1(ReactiveSeq::oneShotStream).map2(ReactiveSeq::oneShotStream)
                .map3(ReactiveSeq::oneShotStream);

    }

    /*
     * Quadruplicate the data in this Stream. To quadruplicate into 3 LazyFutureStreams use actOnFutures#quadruplicate
     * @see cyclops2.reactiveStream.ReactiveSeq#quadruplicate()
     */
    @Override
    default Tuple4<ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>> quadruplicate() {
        return Streams.quadruplicate(stream())
                .map1(ReactiveSeq::oneShotStream).map2(ReactiveSeq::oneShotStream)
                .map3(ReactiveSeq::oneShotStream).map4(ReactiveSeq::oneShotStream);

    }
    /*
     * Quadruplicate the data in this Stream. To quadruplicate into 3 LazyFutureStreams use actOnFutures#quadruplicate
     * @see cyclops2.reactiveStream.ReactiveSeq#quadruplicate()
     */
    @Override
    default Tuple4<ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>> quadruplicate(Supplier<Deque<U>> bufferFactory) {
        return Streams.quadruplicate(stream(),bufferFactory)
                .map1(ReactiveSeq::oneShotStream).map2(ReactiveSeq::oneShotStream)
                .map3(ReactiveSeq::oneShotStream).map4(ReactiveSeq::oneShotStream);

    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#splitSequenceAtHead()
     */
    @Override
    default Tuple2<Optional<U>, ReactiveSeq<U>> splitAtHead() {
        return stream()
                          .splitAtHead();

    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#splitAt(int)
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> splitAt(final int where) {
        return ReactiveSeq.oneShotStream(stream())
                          .splitAt(where);

    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#splitBy(java.util.function.Predicate)
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> splitBy(final Predicate<U> splitter) {
        return ReactiveSeq.oneShotStream(stream())
                          .splitBy(splitter);

    }



    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default FutureStream<U> cycle(final Monoid<U> m, final long times) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .cycle(m, times));

    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#zip3(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <S, R> FutureStream<Tuple3<U, S, R>> zip3(final Iterable<? extends S> second, final Iterable<? extends R> third) {
        return (FutureStream) fromStream(ReactiveSeq.oneShotStream(stream())
                                                        .zip3(second, third));

    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#zip4(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <T2, T3, T4> FutureStream<Tuple4<U, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                  final Iterable<? extends T4> fourth) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .zip4(second, third, fourth));

    }



    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#grouped(int)
     */
    @Override
    default FutureStream<ListX<U>> grouped(final int groupSize) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .grouped(groupSize));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#scanLeft(com.aol.cyclops2.sequence.Monoid)
     */
    @Override
    default FutureStream<U> scanLeft(final Monoid<U> monoid) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .scanLeft(monoid));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#toStreamable()
     */
    @Override
    default Streamable<U> toStreamable() {
        return ReactiveSeq.oneShotStream(stream())
                          .toStreamable();
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#toStream()
     */
    @Override
    default <U> Stream<U> toStream() {
        return (Stream<U>) stream();
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#startsWith(java.lang.Iterable)
     */
    @Override
    default boolean startsWithIterable(final Iterable<U> iterable) {
        return ReactiveSeq.oneShotStream(stream())
                          .startsWithIterable(iterable);
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#startsWith(java.util.Iterator)
     */
    @Override
    default boolean startsWith(final Stream<U> iterator) {
        return ReactiveSeq.oneShotStream(stream())
                          .startsWith(iterator);
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#endsWith(java.lang.Iterable)
     */
    @Override
    default boolean endsWithIterable(final Iterable<U> iterable) {
        return ReactiveSeq.oneShotStream(stream())
                          .endsWithIterable(iterable);
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#endsWith(java.util.reactiveStream.Stream)
     */
    @Override
    default boolean endsWith(final Stream<U> stream) {
        return ReactiveSeq.oneShotStream(stream())
                          .endsWith(stream);
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#anyM()
     */
    @Override
    default AnyMSeq<Witness.reactiveSeq,U> anyM() {
        return AnyM.fromStream(this);
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#flatMapI(java.util.function.Function)
     */
    @Override
    default <R> FutureStream<R> flatMapI(final Function<? super U, ? extends Iterable<? extends R>> fn) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .flatMapI(fn));
    }
    /**
     * A potentially non-blocking analog of {@link ReactiveSeq#forEach}.
     * For push based reactive Stream types (created via Spouts of FutureStream}
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *               non-interfering</a> action to perform on the elements
     */
    @Override
    default void subscribeAll(final Consumer<? super U> action){
            peek(action).run();
    }
    @Override
    default <R> FutureStream<R> flatMapP(final Function<? super U, ? extends Publisher<? extends R>> fn) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .flatMapP(fn));
    }

    @Override
    default <R> FutureStream<R> flatMapP(final int maxConcurrency,final Function<? super U, ? extends Publisher<? extends R>> fn) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .flatMapP(maxConcurrency,fn));
    }
    @Override
    default <R> ReactiveSeq<R> flatMapP(final int maxConcurrency, final QueueFactory<R> factory,Function<? super U, ? extends Publisher<? extends R>> mapper){
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .flatMapP(maxConcurrency,factory,mapper));
    }


    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#flatMapStream(java.util.function.Function)
     */
    @Override
    default <R> FutureStream<R> flatMapStream(final Function<? super U, BaseStream<? extends R, ?>> fn) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .flatMapStream(fn));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#toLazyCollection()
     */
    @Override
    default CollectionX<U> toLazyCollection() {
        return ReactiveSeq.oneShotStream(stream())
                          .toLazyCollection();
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#toConcurrentLazyCollection()
     */
    @Override
    default CollectionX<U> toConcurrentLazyCollection() {
        return ReactiveSeq.oneShotStream(stream())
                          .toConcurrentLazyCollection();
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#toConcurrentLazyStreamable()
     */
    @Override
    default Streamable<U> toConcurrentLazyStreamable() {
        return ReactiveSeq.oneShotStream(stream())
                          .toConcurrentLazyStreamable();
    }


    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#append(java.lang.Object[])
     */
    @Override
    default FutureStream<U> append(final U... values) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .append(values));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#prepend(java.lang.Object[])
     */
    @Override
    default FutureStream<U> prepend(final U... values) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .prepend(values));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#insertAt(int, java.lang.Object[])
     */
    @Override
    default FutureStream<U> insertAt(final int pos, final U... values) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .insertAt(pos, values));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#deleteBetween(int, int)
     */
    @Override
    default FutureStream<U> deleteBetween(final int start, final int end) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .deleteBetween(start, end));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#insertAtS(int, java.util.reactiveStream.Stream)
     */
    @Override
    default FutureStream<U> insertAtS(final int pos, final Stream<U> stream) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .insertAtS(pos, stream));
    }



    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#skip(long, java.util.concurrent.TimeUnit)
     */
    @Override
    default FutureStream<U> skip(final long time, final TimeUnit unit) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .skip(time, unit));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#limit(long, java.util.concurrent.TimeUnit)
     */
    @Override
    default FutureStream<U> limit(final long time, final TimeUnit unit) {
        getSubscription().registerTimeLimit(unit.toNanos(time));
        return fromStream(stream()
                                   .limit(time, unit));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#skipLast(int)
     */
    @Override
    default FutureStream<U> skipLast(final int num) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .skipLast(num));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#limitLast(int)
     */
    @Override
    default FutureStream<U> limitLast(final int num) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .limitLast(num));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#firstValue()
     */
    @Override
    default U firstValue() {

        return ReactiveSeq.oneShotStream(stream())
                          .firstValue();
    }

    /*
     * Batch the elements in the Stream by a combination of Size and Time
     * If batch exceeds max size it will be split
     * If batch exceeds max time it will be split
     * Excludes Null values (neccessary for timeout handling)
     *
     * @see cyclops2.reactiveStream.ReactiveSeq#batchBySizeAndTime(int, long, java.util.concurrent.TimeUnit, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super U>> FutureStream<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
                                                                                   final Supplier<C> factory) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedBySizeAndTime(size, time, unit, factory));
        /**         Queue<U> queue = toQueue();
            Function<BiFunction<Long,TimeUnit,U>, Supplier<Collection<U>>> fn = new BatchByTimeAndSize(size,time,unit,factory);
            return (FutureStream)fromStream(queue.streamBatch(getSubscription(), (Function)fn));**/
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default FutureStream<ListX<U>> groupedStatefullyUntil(final BiPredicate<ListX<? super U>, ? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedStatefullyUntil(predicate));
    }
    @Override
    default <C extends Collection<U>,R> FutureStream<R> groupedStatefullyUntil(final BiPredicate<C, ? super U> predicate, final Supplier<C> factory,
                                                                      Function<? super C, ? extends R> finalizer){
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .groupedStatefullyUntil(predicate,factory,finalizer));

    }
    @Override
    default FutureStream<ListX<U>> groupedStatefullyWhile(final BiPredicate<ListX<? super U>, ? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .groupedStatefullyUntil(predicate));
    }
    @Override
    default <C extends Collection<U>,R> FutureStream<R> groupedStatefullyWhile(final BiPredicate<C, ? super U> predicate, final Supplier<C> factory,
                                                                               Function<? super C, ? extends R> finalizer){
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .groupedStatefullyUntil(predicate,factory,finalizer));

    }
    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#batchUntil(java.util.function.Predicate)
     */
    @Override
    default FutureStream<ListX<U>> groupedUntil(final Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedUntil(predicate));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#batchWhile(java.util.function.Predicate)
     */
    @Override
    default FutureStream<ListX<U>> groupedWhile(final Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedWhile(predicate));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#batchWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super U>> FutureStream<C> groupedWhile(final Predicate<? super U> predicate, final Supplier<C> factory) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedWhile(predicate, factory));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
     */
    @Override
    default <R extends Comparable<? super R>> FutureStream<U> sorted(final Function<? super U, ? extends R> function) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .sorted(function));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#batchUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super U>> FutureStream<C> groupedUntil(final Predicate<? super U> predicate, final Supplier<C> factory) {

        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedUntil(predicate, factory));
    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#recover(java.util.function.Function)
     */
    @Override
    default FutureStream<U> recover(final Function<? super Throwable, ? extends U> fn) {
        return this.onFail(e -> fn.apply(e.getCause()));

    }

    /*
     * @see cyclops2.reactiveStream.ReactiveSeq#recover(java.lang.Class, java.util.function.Function)
     */
    @Override
    default <EX extends Throwable> FutureStream<U> recover(final Class<EX> exceptionClass, final Function<? super EX, ? extends U> fn) {
        return this.onFail(exceptionClass, e -> fn.apply((EX) e.getCause()));
    }

    /**
     * Perform a forEach operation over the Stream, without closing it, consuming only the specified number of elements from
     * the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     * 
     * e.g.
     * <pre>
     * {@code
     *     Subscription next = FutureStream.of(1,2,3,4)
     *          					    .forEach(2,System.out::println);
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     3
     *     4 
     *     Second batch processed!
     * }
     * </pre>
     * 
     * 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming events from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    @Override
    default <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super U> consumer) {
        val t2 = LazyFutureStreamUtils.forEachX(this, numberOfElements, consumer);
        t2.v2.run();
        return t2.v1.join();
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription 
     * <pre>
     * {@code
     *     Subscription next = FutureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get)
     *          					    .forEach(2,System.out::println, e->e.printStackTrace());
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     
     *     RuntimeException Stack Trace on System.err
     *     
     *     4 
     *     Second batch processed!
     * }
     * </pre>	 
     * 
     * 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    @Override
    default <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super U> consumer,
                                                       final Consumer<? super Throwable> consumerError) {
        val t2 = LazyFutureStreamUtils.forEachXWithError(this, numberOfElements, consumer, consumerError);
        t2.v2.run();
        return t2.v1.join();
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription,
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * {@code
     *     Subscription next = LazyFurtureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get)
     *          					    .forEach(2,System.out::println, e->e.printStackTrace(),()->System.out.println("the take!"));
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     
     *     RuntimeException Stack Trace on System.err
     *     
     *     4 
     *     Second batch processed!
     *     The take!
     * }
     * </pre>	 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    @Override
    default <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super U> consumer,
                                                       final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        val t2 = LazyFutureStreamUtils.forEachXEvents(this, numberOfElements, consumer, consumerError, onComplete);
        t2.v2.run();
        return t2.v1.join();
    }

    /**
     *  Perform a forEach operation over the Stream    capturing any elements and errors in the supplied consumers,  
     * <pre>
     * {@code
     *     Subscription next = FutureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get)
     *          					    .forEach(System.out::println, e->e.printStackTrace());
     *          
     *     System.out.println("processed!");
     *     
     *    
     *      
     *     //prints
     *     1
     *     2
     *     RuntimeException Stack Trace on System.err
     *     4
     *     processed!
     *     
     * }
     * </pre>	 
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     */
    @Override
    default <X extends Throwable> void forEach(final Consumer<? super U> consumerElement, final Consumer<? super Throwable> consumerError) {
        val t2 = LazyFutureStreamUtils.forEachWithError(this, consumerElement, consumerError);
        t2.v2.run();
    }

    /**
     * Perform a forEach operation over the Stream  capturing any elements and errors in the supplied consumers
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * {@code
     *     Subscription next = FutureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get)
     *          					    .forEachEvents(System.out::println, e->e.printStackTrace(),()->System.out.println("the take!"));
     *          
     *     System.out.println("processed!");
     *     
     *      
     *     //prints
     *     1
     *     2
     *     RuntimeException Stack Trace on System.err
     *      4 
     *     processed!
     *     
     *     
     * }
     * </pre>	
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     */
    @Override
    default <X extends Throwable> void forEach(final Consumer<? super U> consumerElement, final Consumer<? super Throwable> consumerError,
                                               final Runnable onComplete) {
        val t2 = LazyFutureStreamUtils.forEachEvent(this, consumerElement, consumerError, onComplete);
        t2.v2.run();
    }

    /** END REACTIVESEQ **/

    /**
     * Construct an parallel FutureStream from specified array, using the configured
     * standard parallel thread pool. By default this is the Common ForkJoinPool.
     * To use a different thread pool, the recommended approach is to construct your own LazyReact builder
     *
     * @see ThreadPools#getStandard()
     * @see ThreadPools#setUseCommon(boolean)
     *
     * @param array
     *            Values to react to
     * @return Next SimpleReact stage
     */
    public static <U> FutureStream<U> parallel(final U... array) {
        return LazyReact.parallelCommonBuilder()
                        .of(array);
    }

    /**
     *  Create a 'free threaded' asynchronous reactiveStream that runs on the supplied CompletableFutures executor service (unless async operator invoked
     *  , in which it will switch to the common 'free' thread executor)
     *  Subsequent tasks will be executed synchronously unless the async() operator is invoked.
     *
     *
     */
    static <T> FutureStream<T> lazyFutureStreamFrom(final Stream<CompletableFuture<T>> stream) {
        return new LazyReact(
                             ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(
                                                                                             ThreadPools.getSequentialRetry()))
                                                         .withAsync(false)
                                                         .fromStreamFutures(stream);
    }

    /**
     *  Create a 'free threaded' asynchronous reactiveStream that runs on the supplied CompletableFutures executor service (unless async operator invoked
     *  , in which it will switch to the common 'free' thread executor)
     *  Subsequent tasks will be executed synchronously unless the async() operator is invoked.
     *
     *
     */
    static <T> FutureStream<T> lazyFutureStream(final CompletableFuture<T> value) {
        return new LazyReact(
                             ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(
                                                                                             ThreadPools.getSequentialRetry()))
                                                         .withAsync(false)
                                                         .fromStreamFutures(Stream.of(value));
    }

    /**
     *  Create a 'free threaded' asynchronous reactiveStream that runs on a single thread (not current)
     *  The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator
     *  is invoked.
     *
     *
     */
    static <T> FutureStream<T> lazyFutureStream(final CompletableFuture<T>... values) {
        return new LazyReact(
                             ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(
                                                                                             ThreadPools.getSequentialRetry()))
                                                         .withAsync(false)
                                                         .fromStreamFutures(Stream.of(values));
    }

    /**
     *  Create a 'free threaded' asynchronous reactiveStream that runs on a single thread (not current)
     *  The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator
     *  is invoked.
     *
     * @see Stream#of(Object)
     */
    static <T> FutureStream<T> react(final Supplier<T> value) {
        return new LazyReact(
                             ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(
                                                                                             ThreadPools.getSequentialRetry()))
                                                         .withAsync(false)
                                                         .ofAsync(value);
    }

    /**
     Create a 'free threaded' asynchronous reactiveStream that runs on a single thread (not current)
     * The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator is invoked.
    
     */
    @SafeVarargs
    static <T> FutureStream<T> react(final Supplier<T>... values) {
        return new LazyReact(
                             ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(
                                                                                             ThreadPools.getSequentialRetry()))
                                                         .withAsync(false)
                                                         .ofAsync(values);
    }

    /**
     * Create a sequential synchronous reactiveStream that runs on the current thread
     *
     */
    static <T> FutureStream<T> of(final T value) {
        return lazyFutureStream((Stream) ReactiveSeq.of(value));
    }

    /**
     * Create a sequential synchronous reactiveStream that runs on the current thread
     *
     */
    @SafeVarargs
    static <T> FutureStream<T> of(final T... values) {
        return lazyFutureStream((Stream) ReactiveSeq.of(values));
    }

    /**
     * Create a sequential synchronous reactiveStream that runs on a free thread (commonFreeThread executor by default, shared
     * across any instances created in this manner.
     * @see com.aol.cyclops2.react.ThreadPools#setUseCommon(boolean)
     *
     */
    static <T> FutureStream<T> freeThread(final T value) {
        return (FutureStream) freeThread(new Object[] { value });
    }

    /**
     * Create a sequential synchronous reactiveStream that runs on a free thread (commonFreeThread executor by default, shared
     * across any instances created in this manner.
     * @see com.aol.cyclops2.react.ThreadPools#setUseCommon(boolean)
     *
     */
    @SafeVarargs
    static <T> FutureStream<T> freeThread(final T... values) {
        final LazyReact react = new LazyReact(
                                              ThreadPools.getSequential(), RetryBuilder.getDefaultInstance()
                                                                                       .withScheduler(ThreadPools.getSequentialRetry()),
                                              false, new MaxActive(
                                                                   1, 1));
        return new FutureStreamImpl<T>(
                                           react, Stream.of(values));

    }

    /**
     * Create a sequential synchronous reactiveStream that runs on the current thread
     */
    static <T> FutureStream<T> empty() {
        return lazyFutureStream((Stream) Seq.empty());
    }

    /**
     * @see Stream#iterate(Object, UnaryOperator)
     */
    static <T> FutureStream<T> iterate(final T seed, final UnaryOperator<T> f) {
        return lazyFutureStream((Stream) Seq.iterate(seed, f));
    }

    /**
     * Generate an infinite Stream of null values that runs on the current thread
     * @see Stream#generate(Supplier)
     */
    static FutureStream<Void> generate() {
        return generate(() -> null);
    }

    /**
     * Generate an infinite Stream of given value that runs on the current thread
     * @see Stream#generate(Supplier)
     */
    static <T> FutureStream<T> generate(final T value) {
        return generate(() -> value);
    }

    /**
     * Generate an infinite Stream of value returned from Supplier that runs on the current thread
     * @see Stream#generate(Supplier)
     */
    static <T> FutureStream<T> generate(final Supplier<T> s) {
        return lazyFutureStream(Stream.generate(s));
    }

    /**
     * Wrap a Stream into a FutureStream that runs on the current thread
     */
    static <T> FutureStream<T> lazyFutureStream(final Stream<T> stream) {
        if (stream instanceof FutureStream)
            return (FutureStream<T>) stream;
        final LazyReact react = new LazyReact(
                                              ThreadPools.getCurrentThreadExecutor(), RetryBuilder.getDefaultInstance()
                                                                                                  .withScheduler(ThreadPools.getSequentialRetry()),
                                              false, new MaxActive(
                                                                   1, 1));
        return new FutureStreamImpl<T>(
                                           react, stream);

    }

    /**
     * Wrap an Iterable into a FutureStream that runs on the current thread
     */
    static <T> FutureStream<T> lazyFutureStreamFromIterable(final Iterable<T> iterable) {
        return lazyFutureStream(iterable.iterator());
    }

    /**
     * Wrap an Iterator into a FutureStream that runs on the current thread
     */
    static <T> FutureStream<T> lazyFutureStream(final Iterator<T> iterator) {
        return lazyFutureStream(StreamSupport.stream(spliteratorUnknownSize(iterator, ORDERED), false));
    }

}
