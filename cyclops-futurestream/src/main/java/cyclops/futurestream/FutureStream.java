package cyclops.futurestream;

import com.oath.cyclops.internal.react.async.future.FastFuture;
import com.oath.cyclops.internal.react.exceptions.SimpleReactProcessingException;
import com.oath.cyclops.internal.react.stream.CloseableIterator;
import com.oath.cyclops.internal.react.stream.LazyStreamWrapper;
import com.oath.cyclops.internal.react.stream.traits.future.operators.LazyFutureStreamUtils;
import com.oath.cyclops.internal.react.stream.traits.future.operators.OperationsOnFuturesImpl;
import com.oath.cyclops.internal.stream.FutureOpterationsImpl;
import com.oath.cyclops.react.SimpleReactFailedStageException;
import com.oath.cyclops.react.async.subscription.Continueable;
import com.oath.cyclops.react.collectors.lazy.LazyResultConsumer;
import com.oath.cyclops.types.futurestream.*;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.reactive.FutureStreamSynchronousPublisher;
import com.oath.cyclops.types.reactive.ReactiveStreamsTerminalFutureOperations;
import com.oath.cyclops.types.stream.HotStream;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.control.Future;
import com.oath.cyclops.async.QueueFactories;
import com.oath.cyclops.async.adapters.Adapter;
import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.async.adapters.Queue.ClosedQueueException;
import com.oath.cyclops.async.adapters.Queue.QueueTimeoutException;
import com.oath.cyclops.async.adapters.QueueFactory;
import cyclops.data.Vector;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.control.*;
import cyclops.data.tuple.Tuple;
import cyclops.companion.Streams;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Lambda;
import cyclops.function.Monoid;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import lombok.val;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.*;
import java.util.stream.*;

public interface FutureStream<U> extends LazySimpleReactStream<U>,
                                         LazyStream<U>,
                                          ReactiveSeq<U>,
                                          LazyToQueue<U>,
                                          ConfigurableStream<U, FastFuture<U>>,
                                          FutureStreamSynchronousPublisher<U> {

    default  <R> R toType(Function<? super FutureStream<U>,? extends R> fn){
    return fn.apply(this);
  }
    @Override
    default ListX<ReactiveSeq<U>> multicast(int num) {
        return stream().multicast(num);
    }

    @Override
    default ReactiveSeq<U> changes(){
        return fromStream(stream().changes());
    }

    @Override
    default Maybe<U> takeOne(){
        return stream().takeOne();
    }

    @Override
    default LazyEither<Throwable, U> findFirstOrError(){
        return stream().findFirstOrError();
    }

    @Override
    default <R> FutureStream<R> parallel(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<? extends R>> fn) {
        return fromStream(stream().parallel(fj,fn));
    }
   @Override
   default <U1, R> FutureStream<R> zipLatest(final Publisher<? extends U1> other, final BiFunction<? super U, ? super U1, ? extends R> zipper) {
       return fromStream(stream().zipLatest(other, zipper));
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
    default FutureStream<U> appendAll(U value){
        return fromStream(stream().appendAll(value));
    }

    @Override
    default FutureStream<Tuple2<U, Long>> timestamp() {
        return fromStream(stream().timestamp());
    }

    @Override
    default <R> FutureStream<R> retry(final Function<? super U, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (FutureStream)ReactiveSeq.super.retry(fn,retries,delay,timeUnit);
    }


    @Override
    default FutureStream<ReactiveSeq<U>> combinations(final int size) {
        return fromStream(stream().combinations(size));
    }


    @Override
    default FutureStream<U> removeStream(final Stream<? extends U> stream) {
        return fromStream(stream().removeStream(stream));
    }



    @Override
    default FutureStream<U> removeAll(final U... values) {
        return fromStream(stream().removeAll(values));
    }

    @Override
    default FutureStream<U> retainAll(final Iterable<? extends U> it) {
        return fromStream(stream().retainAll(it));
    }

    @Override
    default FutureStream<U> retainStream(final Stream<? extends U> stream) {
        return fromStream(stream().retainStream(stream));
    }

    @Override
    default FutureStream<U> retainAll(final U... values) {
        return fromStream(stream().retainAll(values));
    }

  @Override
    default <U1> FutureStream<Tuple2<U, U1>> zipWithPublisher(final Publisher<? extends U1> other) {
        return fromStream(stream().zipWithPublisher(other));
    }

    @Override
    default <S, U1, R> FutureStream<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U1> third, final Function3<? super U, ? super S, ? super U1, ? extends R> fn3) {
        return fromStream(stream().zip3(second,third,fn3));
    }

    @Override
    default <T2, T3, T4, R> FutureStream<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super U, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
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
    default <R1, R2, R3> FutureStream<R3> fanOutZipIn(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R1>> path1,
                                                      Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R2>> path2,
                                                      BiFunction<? super R1, ? super R2, ? extends R3> zipFn) {
        return fromStream(stream().fanOutZipIn(path1,path2,zipFn));
    }

    @Override
    default <R1, R2, R3> FutureStream<R3> parallelFanOutZipIn(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<? extends R1>> path1,
                                                              Function<? super Stream<U>, ? extends Stream<? extends R2>> path2,
                                                              BiFunction<? super R1, ? super R2, ? extends R3> zipFn) {
        return fromStream(stream().parallelFanOutZipIn(fj,path1,path2,zipFn));
    }

    @Override
    default <R> FutureStream<R> fanOut(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path1,
                                                Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path2) {
        return fromStream(stream().fanOut(path1,path2));
    }

    @Override
    default <R> FutureStream<R> parallelFanOut(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<? extends R>> path1,
                                               Function<? super Stream<U>, ? extends Stream<? extends R>> path2) {
        return fromStream(stream().parallelFanOut(fj,path1,path2));
    }

    @Override
    default <R> FutureStream<R> fanOut(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path1,
                                       Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path2,
                                       Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path3) {
        return fromStream(stream().fanOut(path1,path2,path3));
    }

    @Override
    default <R> FutureStream<R> parallelFanOut(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<? extends R>> path1,
                                               Function<? super Stream<U>, ? extends Stream<? extends R>> path2,
                                               Function<? super Stream<U>, ? extends Stream<? extends R>> path3) {
        return fromStream(stream().parallelFanOut(fj,path1, path2, path3));
    }

    @Override
    default <R1, R2, R3, R4> FutureStream<R4> parallelFanOutZipIn(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<? extends R1>> path1,
                                                                  Function<? super Stream<U>, ? extends Stream<? extends R2>> path2,
                                                                  Function<? super Stream<U>, ? extends Stream<? extends R3>> path3,
                                                                  Function3<? super R1, ? super R2, ? super R3, ? extends R4> zipFn) {
        return fromStream(stream().parallelFanOutZipIn(fj,path1, path2, path3,zipFn));
    }

    @Override
    default <R1, R2, R3, R4> FutureStream<R4> fanOutZipIn(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R1>> path1,
                                                          Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R2>> path2,
                                                          Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R3>> path3,
                                                          Function3<? super R1, ? super R2, ? super R3, ? extends R4> zipFn) {
        return fromStream(stream().fanOutZipIn(path1, path2, path3,zipFn));
    }

    @Override
    default <R> ReactiveSeq<R> fanOut(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path1,
                                       Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path2,
                                       Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path3,
                                       Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R>> path4) {
        return fromStream(stream().fanOut(path1, path2, path3,path4));
    }

    @Override
    default <R> FutureStream<R> parallelFanOut(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<? extends R>> path1,
                                               Function<? super Stream<U>, ? extends Stream<? extends R>> path2,
                                               Function<? super Stream<U>, ? extends Stream<? extends R>> path3,
                                               Function<? super Stream<U>, ? extends Stream<? extends R>> path4) {
        return fromStream(stream().parallelFanOut(fj,path1, path2, path3,path4));
    }

    @Override
    default <R1, R2, R3, R4, R5> FutureStream<R5> fanOutZipIn(Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R1>> path1,
                                                              Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R2>> path2,
                                                              Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R3>> path3,
                                                              Function<? super ReactiveSeq<U>, ? extends ReactiveSeq<? extends R4>> path4,
                                                              Function4<? super R1, ? super R2, ? super R3, ? super R4, ? extends R5> zipFn) {
        return fromStream(stream().fanOutZipIn(path1, path2, path3,path4,zipFn));
    }

    @Override
    default <R1, R2, R3, R4, R5> FutureStream<R5> parallelFanOutZipIn(ForkJoinPool fj, Function<? super Stream<U>, ? extends Stream<? extends R1>> path1,
                                                                      Function<? super Stream<U>, ? extends Stream<? extends R2>> path2,
                                                                      Function<? super Stream<U>, ? extends Stream<? extends R3>> path3,
                                                                      Function<? super Stream<U>, ? extends Stream<? extends R4>> path4,
                                                                      Function4<? super R1, ? super R2, ? super R3, ? super R4, ? extends R5> zipFn) {
        return fromStream(stream().parallelFanOutZipIn(fj,path1, path2, path3,path4,zipFn));
    }




    public static LazyReact builder(int maxActiveTasks, Executor exec){
        return new LazyReact(maxActiveTasks,exec);
    }
    public static LazyReact builder(){
        return new LazyReact();
    }

    default <R> Future<R> foldFuture(Function<? super IterableX<U>,? extends R> fn){
        return Future.of(()->fn.apply(this),getSimpleReact().getExecutor());
    }

    default ReactiveStreamsTerminalFutureOperations<U> futureOperations(){
        return new FutureOpterationsImpl<U>(getSimpleReact().getExecutor(),this);
    }
    default <A,R> FutureStream<R> collectSeq(Collector<? super U,A,R> c){
        return this.getSimpleReact().fromStream(Stream.of(Lambda.λ(()->this.collect(c))).map(Supplier::get));
    }
    default FutureStream<U> fold(Monoid<U> monoid){
        return this.getSimpleReact().fromStream(Stream.of(Lambda.λ(()->this.reduce(monoid))).map(Supplier::get));
    }



    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#append(java.util.stream.Stream)
     */
    @Override
    default FutureStream<U> appendStream(Stream<? extends U> other) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).appendStream(other));
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#append(java.lang.Iterable)
     */
    @Override
    default FutureStream<U> append(Iterable<? extends U> other) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).append(other));
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#prependAll(java.util.stream.Stream)
     */
    @Override
    default FutureStream<U> prependStream(Stream<? extends U> other) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).prependStream(other));
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#prependAll(java.lang.Iterable)
     */
    @Override
    default FutureStream<U> prependAll(Iterable<? extends U> other) {

        return fromStream(this.prependAll(other));
    }



    /**
     * Create a Stream that finitely cycles this Stream, provided number of times
     *
     * <pre>
     * {@code
     * assertThat(FutureStream.of(1,2,2).cycle(3)
    .collect(CyclopsCollectors.toList()),
    equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
     * }
     * </pre>
     * @return New cycling stream
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

    @Override
    default <U1> FutureStream<Tuple2<U, U1>> crossJoin(ReactiveSeq<? extends U1> other) {
        Streamable<? extends U1> s = Streamable.fromStream(other);
        return fromStream(stream().forEach2(a->ReactiveSeq.fromIterable(s), Tuple::tuple));
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
     * @see com.oath.cyclops.types.Zippable#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> FutureStream<R> zip(BiFunction<? super U, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {

        return (FutureStream<R>)ReactiveSeq.super.zip(fn, publisher);
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
     * @see cyclops2.stream.ReactiveSeq#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default FutureStream<U> combine(final BiPredicate<? super U, ? super U> predicate, final BinaryOperator<U> op) {
        return fromStream(Streams.combine(this, predicate, op));
    }
    @Override
    default FutureStream<U> combine(final Monoid<U> op, final BiPredicate<? super U, ? super U> predicate) {
        return this.fromStream(ReactiveSeq.oneShotStream(stream())
                .combine(op,predicate));
    }
    /**
     * If this Stream is empty one it with a another Stream
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
     * @return Stream that will switch to an alternative Stream if empty
     */
    @Override
    default FutureStream<U> onEmptySwitch(final Supplier<? extends Stream<U>> switchTo) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .onEmptySwitch(switchTo));
    }


    /* (non-Javadoc)
     * @see cyclops2.stream.ReactiveSeq#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> FutureStream<R> forEach4(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                                     BiFunction<? super U, ? super R1, ? extends BaseStream<R2, ?>> stream2,
                                                     Function3<? super U, ? super R1, ? super R2, ? extends BaseStream<R3, ?>> stream3,
                                                     Function4<? super U, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (FutureStream<R>)ReactiveSeq.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.stream.ReactiveSeq#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> FutureStream<R> forEach4(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                                     BiFunction<? super U, ? super R1, ? extends BaseStream<R2, ?>> stream2,
                                                     Function3<? super U, ? super R1, ? super R2, ? extends BaseStream<R3, ?>> stream3,
                                                     Function4<? super U, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                     Function4<? super U, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (FutureStream<R>)ReactiveSeq.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.stream.ReactiveSeq#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> FutureStream<R> forEach3(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                                 BiFunction<? super U, ? super R1, ? extends BaseStream<R2, ?>> stream2,
                                                 Function3<? super U, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (FutureStream<R>)ReactiveSeq.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.stream.ReactiveSeq#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> FutureStream<R> forEach3(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                                 BiFunction<? super U, ? super R1, ? extends BaseStream<R2, ?>> stream2,
                                                 Function3<? super U, ? super R1, ? super R2, Boolean> filterFunction,
                                                 Function3<? super U, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (FutureStream<R>)ReactiveSeq.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.stream.ReactiveSeq#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> FutureStream<R> forEach2(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                             BiFunction<? super U, ? super R1, ? extends R> yieldingFunction) {

        return (FutureStream<R>)ReactiveSeq.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see cyclops2.stream.ReactiveSeq#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> FutureStream<R> forEach2(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
                                             BiFunction<? super U, ? super R1, Boolean> filterFunction,
                                             BiFunction<? super U, ? super R1, ? extends R> yieldingFunction) {

        return (FutureStream<R>)ReactiveSeq.super.forEach2(stream1, filterFunction, yieldingFunction);
    }


    /**
     * Remove all occurances of the specified element from the Stream
     * <pre>
     * {@code
     * 	FutureStream.of(1,2,3,4,5,1,2,3).removeValue(1)
     *
     *  //FutureStream[2,3,4,5,2,3]
     * }
     * </pre>
     *
     * @param t element to removeValue
     * @return Filtered Stream
     */
    @Override
    default FutureStream<U> removeValue(final U t) {

        return (FutureStream<U>) ReactiveSeq.super.removeValue(t);
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
     * Makes use of Streamable to store intermediate stages in a toX
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
     *   //FutureStream[Stream[],Stream[1],Stream[2],Stream[3].Stream[1,2],Stream[1,3],Stream[2,3]
     *   			,Stream[1,2,3]]
     * }
     * </pre>
     *
     *
     * @return All combinations of the elements in this stream
     */
    @Override
    default FutureStream<ReactiveSeq<U>> combinations() {
        return this.fromStream(ReactiveSeq.oneShotStream(stream())
                                          .combinations());

    }

    /**
     * FutureStream operators act on the results of the previous stage by default. That means limiting,
     * skipping, zipping all occur once results being to stream in from active Future tasks. This
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
     * @see com.oath.cyclops.react.stream.traits.SimpleReactStream#iterator()
     */
    @Override
    default CloseableIterator<U> iterator() {
        return (CloseableIterator) LazySimpleReactStream.super.iterator();
    }

    /*
     * @return Queue membership subscription for this Stream
     * @see com.oath.cyclops.react.stream.traits.LazySimpleReactStream#getSubscription()
     */
    @Override
    Continueable getSubscription();

    /*
     * @see com.oath.cyclops.react.stream.traits.LazySimpleReactStream#withLastActive(com.aol.simple.react.stream.LazyStreamWrapper)
     */
    @Override
    <R> FutureStream<R> withLastActive(LazyStreamWrapper<R> streamWrapper);

    /*
     *	@return Stream Builder for this Stream
     * @see com.oath.cyclops.react.stream.traits.LazySimpleReactStream#getSimpleReact()
     */
    @Override
    LazyReact getSimpleReact();

    /*
     * Subscribe to this Stream
     * If this Stream is executing in async mode it will operate as an Async Publisher, otherwise it will operate as a Synchronous publisher.
     * async() or sync() can be used just prior to forEachAsync.
     *
     * <pre>
     * {@code
     *  FutureStreamSubscriber<Integer> sub = new FutureStreamSubscriber();
        FutureStream.of(1,2,3).forEachAsync(sub);
        sub.getStream().forEach(System.out::println);
     * }
     * </pre>
     *	@param s Subscriber
     * @see org.reactivestreams.Publisher#forEachAsync(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super U> s) {

        FutureStreamSynchronousPublisher.super.subscribe(s);
    }

    /**
     * @return an Iterator that chunks all completed elements from this stream since last it.next() call into a toX
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
     * @return a Stream that batches all completed elements from this stream since last read recover into a toX
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
     * @see java.util.stream.Stream#count()
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
     * @see com.oath.cyclops.react.stream.traits.ConfigurableStream#withTaskExecutor(java.util.concurrent.Executor)
     */
    @Override
    FutureStream<U> withTaskExecutor(Executor e);



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
     * @see com.oath.cyclops.react.stream.traits.ConfigurableStream#withQueueFactory(com.aol.simple.react.async.QueueFactory)
     * @see com.oath.cyclops.react.stream.traits.FutureStream#unboundedWaitFree()
     * @see com.oath.cyclops.react.stream.traits.FutureStream#boundedWaitFree(int size)
     */
    @Override
    FutureStream<U> withQueueFactory(QueueFactory<U> queue);

    /*
     *	@param sub Queue Subscription to use for this Stream
     * @see com.oath.cyclops.react.stream.traits.LazySimpleReactStream#withSubscription(com.aol.simple.react.async.subscription.Continueable)
     */
    @Override
    FutureStream<U> withSubscription(Continueable sub);

    /*
     * Convert this stream into an async / sync stream
     *
     *	@param async true if aysnc stream
     *	@return
     * @see com.oath.cyclops.react.stream.traits.ConfigurableStream#withAsync(boolean)
     */
    @Override
    FutureStream<U> withAsync(boolean async);

    /*
     * @see com.oath.cyclops.react.stream.traits.LazyStream#forEach(java.util.function.Consumer)
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
         q.stream().collect(CyclopsCollectors.toList())
                   .size()

        //100
     * }</pre>
     *	@return Queue
     * @see com.oath.cyclops.react.stream.traits.ToQueue#toQueue()
     */
    @Override
    default Queue<U> toQueue() {

        return LazyToQueue.super.toQueue();
    }
    @Override
    default <R> FutureStream<R> parallel(Function<? super Stream<U>,? extends Stream<? extends R>> fn){
        return fromStream(ReactiveSeq.super.parallel(fn));

    }


    @Override
    default <T> T reduce(final T identity, final BiFunction<T, ? super U, T> accumulator) {
        return LazyStream.super.reduce(identity, accumulator, (a, b) -> a);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.stream.Stream#reduce(java.lang.Object,
     * java.util.function.BinaryOperator)
     */
    @Override
    default U reduce(final U identity, final BinaryOperator<U> accumulator) {
        return LazyStream.super.reduce(identity, accumulator);
    }

    /*
     * @see com.oath.cyclops.react.stream.traits.LazyStream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
     */
    @Override
    default <T> T reduce(final T identity, final BiFunction<T, ? super U, T> accumulator, final BinaryOperator<T> combiner) {
        return LazyStream.super.reduce(identity, accumulator, combiner);
    }

    /*
     * @see com.oath.cyclops.react.stream.traits.LazyStream#reduce(java.util.function.BinaryOperator)
     */
    @Override
    default Optional<U> reduce(final BinaryOperator<U> accumulator) {
        return LazyStream.super.reduce(accumulator);
    }

    /*
     * @see com.oath.cyclops.react.stream.traits.LazyStream#collect(java.util.function.Supplier, java.util.function.BiConsumer, java.util.function.BiConsumer)
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
         * @see com.oath.cyclops.react.stream.traits.SimpleReactStream#sync()
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
     * FutureStream stream = of(1,2,3,4).async()
     *                                      .map(this::doWorkOnSeparateThreads)
     *                                      .map(this::resubmitTaskForDistribution)
     *                                      .forEach(System.out::println);
     *
     * }</pre>
     *
     *	@return Version of FutureStream that will use async CompletableFuture methods
     * @see com.oath.cyclops.react.stream.traits.SimpleReactStream#async()
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
     *                     .flatMap(Collection::stream)
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
     * Equivalent functionally to transform / applyHKT but always applied on the completing thread (from the previous stage)
     *
     * When autoOptimize functionality is enabled, thenSync is the default behaviour for applyHKT / transform operations
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
     * @see com.oath.cyclops.react.stream.traits.LazySimpleReactStream#thenSync(java.util.function.Function)
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
     * @see com.oath.cyclops.react.stream.traits.LazySimpleReactStream#peekSync(java.util.function.Consumer)
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
     * @see cyclops2.stream.ReactiveSeq#findFirst()
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
                               getTaskExecutor())
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
     * Apply a function to all items in the stream.
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
     * @see com.oath.cyclops.react.stream.traits.FutureStream#transform(java.util.function.Function)
     */
    @Override
    default <R> FutureStream<R> map(final Function<? super U, ? extends R> mapper) {

        return (FutureStream<R>) LazySimpleReactStream.super.then((Function) mapper);
    }

    /**
     * Break a stream into multiple Streams based of some characteristic of the
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
     * @return Next stage of stream, with only 1 element per specified time
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
     *            Function takes a supplier, which can be used repeatedly to getValue
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
     *            Function takes a supplier, which can be used repeatedly to getValue
     *            the next value from the Stream. If there are no more values, a
     *            ClosedQueueException will be thrown. This function should
     *            return a Supplier which creates a toX of the batched
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
                        .getValue(0)
                        ,not(hasItem(6)));
        }
     * </pre>
     *
     * <pre>
     * {@code
     *
        assertThat(of(1,2,3,4,5,6).batchBySizeAndTime(3,10,TimeUnit.SECONDS).toList().getValue(0).size(),is(3));

     * }</pre>
     *
     *	@param size Max batch size
     *	@param time Max time length
     *	@param unit time unit
     *	@return batched stream
     * @see com.oath.cyclops.react.stream.traits.FutureStream#batchBySizeAndTime(int, long, java.util.concurrent.TimeUnit)
     */
    @Override
    default ReactiveSeq<Vector<U>> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit) {
        return fromStream(stream().groupedBySizeAndTime(size, time, unit));
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
     * Batch the elements in this stream into Collections of specified size The
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
     *            Create the batch holding toX
     * @return Stream of Collections
     */
    @Override
    default <C extends Collection<? super U>> FutureStream<C> grouped(final int size, final Supplier<C> supplier) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .grouped(size, supplier));

    }

    /**
     * Introduce a random delay between events in a stream Can be used to
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
     * Will emit 1 on skip, applyHKT 2 after an hour, 3 after 2 hours and so on.
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
    default ReactiveSeq<Vector<U>> groupedByTime(final long time, final TimeUnit unit) {
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

            assertThat(set.getValue(0).size(),is(1));
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
     * Equivalent to transform from Streams / Seq apis.
     *
     * @param fn Function to be applied asynchronously
     *
     * @return Next stage in stream
     *
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#applyHKT(java.util.function
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
     * Merges this stream and the supplied Streams into a single Stream where the next value
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
     * Merges this stream and the supplied Streams into a single Stream where the next value
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
        ReactiveSeq.of(streams)
           .forEach(s -> s.addToQueue(queue));

        return fromStream(queue.stream(this.getSubscription()));
    }

    /*
     * Define failure handling for this stage in a stream. Recovery function
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
     * @return Next stage in stream
     *
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#onFail(java.util.function
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
     * com.aol.simple.react.stream.traits.FutureStream#onFail(java.lang.Class,
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
     * @return Next stage in stream
     *
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#capture(java.util.function
     * .Consumer)
     */
    @Override
    default FutureStream<U> capture(final Consumer<Throwable> errorHandler) {
        return (FutureStream) LazySimpleReactStream.super.capture(errorHandler);
    }

    /*
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#peek(java.util.function
     * .Consumer)
     */
    @Override
    default FutureStream<U> peek(final Consumer<? super U> consumer) {
        return (FutureStream) LazySimpleReactStream.super.peek(consumer);
    }

    /*
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#filter(java.util.function
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
     * com.aol.simple.react.stream.FutureStreamImpl#flatMap(java.util.function
     * .Function)
     */
    @Override
    default <R> FutureStream<R> flatMap(final Function<? super U, ? extends Stream<? extends R>> flatFn) {
        return  (FutureStream<R>)LazySimpleReactStream.super.flatMap(flatFn);
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
     * com.aol.simple.react.stream.FutureStreamImpl#retry(java.util.function
     * .Function)
     */
    @Override
    default <R> FutureStream<R> retry(final Function<? super U, ? extends R> fn) {

        return (FutureStream) ReactiveSeq.super.retry(fn);
    }



    /*
         * Convert the specified Stream to a FutureStream, using the configuration
         * of this FutureStream (task executors, current config settings)
         *
         * @see com.oath.cyclops.react.stream.traits.SimpleReactStream#fromStream(java.util.stream.Stream)
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
     * com.aol.simple.react.stream.FutureStreamImpl#fromStreamCompletableFuture
     * (java.util.stream.Stream)
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
     * Cast all elements in this stream to specified type. May throw {@link
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
     * com.aol.simple.react.stream.traits.FutureStream#cast(java.lang.Class)
     */
    @Override
    default <U> FutureStream<U> cast(final Class<? extends U> type) {
        return (FutureStream) LazySimpleReactStream.super.cast(type);

    }

    /**
     * Keep only those elements in a stream that are of a given type.
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
     * Returns a stream with a given value interspersed between any two values
     * of this stream.
     *
     * <code>
     *
     * // (1, 0, 2, 0, 3, 0, 4)
     *
     * FutureStream.of(1, 2, 3, 4).intersperse(0)
     *
     * </code>
     *
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
     * @see cyclops2.stream.ReactiveSeq#take(long)
     */
    @Override
    default FutureStream<U> drop(final long drop) {
        return skip(drop);
    }

    /* (non-Javadoc)
     * @see cyclops2.stream.ReactiveSeq#take(long)
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
     * Will result in a stream of (3,4). The first two elements are skipped.
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
     * @return distinct elements in this Stream (must be a finite stream!)
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
                                    .collect(CyclopsCollectors.toList());


        assertThat(list.getValue(0),hasItems(1,2));
        assertThat(list.getValue(1),hasItems(2,3));
     * }
     * </pre>
     * @param size
     *            Size of sliding window
     * @return Stream with sliding view over data in this stream
     */
    @Override
    default FutureStream<VectorX<U>> sliding(final int size) {
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
                                    .collect(CyclopsCollectors.toList());


        assertThat(list.getValue(0),hasItems(1,2,3));
        assertThat(list.getValue(1),hasItems(3,4,5));
     * }
     * </pre>
     * @param size
     *            Size of sliding window
     * @return Stream with sliding view over data in this stream
     */
    @Override
    default FutureStream<VectorX<U>> sliding(final int size, final int increment) {

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
                          fromStream(duplicated._1()), fromStream(duplicated._2()));
    }


    /**
     * Partition a stream in two given a predicate. Two LazyFutureStreams are
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
                          fromStream(partition._1()), fromStream(partition._2()));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#slice(long, long)
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
     *
     *
     */
    @Override
    default FutureStream<Tuple2<U, Long>> zipWithIndex() {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .zipWithIndex());
    }



    @Override
    default <T> FutureStream<Tuple2<U, T>> zipWithStream(final Stream<? extends T> other) {
        return fromStream(LazyFutureStreamFunctions.zip(this, other));
    }

    @Override
    default <T> FutureStream<Tuple2<U, T>> zip(final Iterable<? extends T> other) {
        return fromStream(LazyFutureStreamFunctions.zip(this, ReactiveSeq.fromIterable(other)));
    }



    @Override
    default <T, R> FutureStream<R> zipWithStream(final Stream<? extends T> other, final BiFunction<? super U, ? super T, ? extends R> zipper) {
        return fromStream(LazyFutureStreamFunctions.zip(this, ReactiveSeq.oneShotStream(other), zipper));
    }

    @Override
    default <T, R> FutureStream<R> zip(final Iterable<? extends T> other, final BiFunction<? super U, ? super T, ? extends R> zipper) {
        return fromStream(LazyFutureStreamFunctions.zip(this, ReactiveSeq.fromIterable(other), zipper));
    }

    /**
     * Scan a stream to the left.
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
     * Scan a stream to the right. - careful with infinite streams!
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
     * Reverse a stream. - eager operation that materializes the Stream into a list - careful with infinite streams!
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
     * Shuffle a stream
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
     * Shuffle a stream using specified source of randomness
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
     * Returns a stream with all elements skipped for which a predicate
     * evaluates to true.
     *
     *
     * // (3, 4, 5) FutureStream.of(1, 2, 3, 4, 5).skipWhile(i &gt; i &lt;
     * 3)
     *
     */
    @Override
    default FutureStream<U> skipWhile(final Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .skipWhile(predicate));
    }

    /**
     * Returns a stream with all elements skipped for which a predicate
     * evaluates to false.
     *
     *
     * // (3, 4, 5) FutureStream.of(1, 2, 3, 4, 5).skipUntil(i &gt; i == 3)
     *
     *
     */
    @Override
    default FutureStream<U> skipUntil(final Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .skipUntil(predicate));
    }

    /**
     * Returns a stream limited to all elements for which a predicate evaluates
     * to true.
     *
     *
     * // (1, 2) FutureStream.of(1, 2, 3, 4, 5).limitWhile(i -&gt; i &lt; 3)
     *
     *
     */
    @Override
    default FutureStream<U> limitWhile(final Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .limitWhile(predicate));
    }

    /**
     * Returns a stream limited to all elements for which a predicate evaluates
     * to false.
     *
     *
     * // (1, 2) FutureStream.of(1, 2, 3, 4, 5).limitUntil(i &gt; i == 3)
     *
     */
    @Override
    default FutureStream<U> limitUntil(final Predicate<? super U> predicate) {
        return fromStream(LazyFutureStreamFunctions.limitUntil(this, predicate));
    }



    /**
     * Produce this stream, or an alternative stream from the
     * {@code value}, in case this stream is empty.
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
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
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
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     *
     * <pre>
     * {@code
     *   FutureStream.of().capture(e -> ex = e).onEmptyError(() -> new RuntimeException()).toList();
     *
     *   //throws RuntimeException
     * }
     * </pre>
     *
     */
    @Override
    default <X extends Throwable> FutureStream<U> onEmptyError(final Supplier<? extends X> supplier) {
        return fromStream(ReactiveSeq.oneShotStream(stream()).onEmptyError(supplier));
    }




    /**
     * Create a Stream that infinitely cycles this Stream
     *
     * <pre>
     * {@code
     * assertThat(FutureStream.of(1,2,2).cycle().limit(6)
                                .collect(CyclopsCollectors.toList()),
                                    equalTo(Arrays.asList(1,2,2,1,2,2));
     * }
     * </pre>
     * @return New cycling stream
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
                                            .collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
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
                                            .collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2)));

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
     * @see com.oath.cyclops.react.stream.traits.FutureStream#stream()
     */
    @Override
    default ReactiveSeq<U> stream() {
          return Streams.oneShotStream(toQueue().jdkStream(getSubscription()));

    }

    /*
     *	@return New version of this stream converted to execute asynchronously and in parallel
     * @see com.oath.cyclops.react.stream.traits.FutureStream#parallel()
     */
    @Override
    default FutureStream<U> parallel() {
        return this.withAsync(true)
                   .withTaskExecutor(LazyReact.parallelBuilder()
                                              .getExecutor());
    }

    /*
     *	@return New version of this stream  converted to execute synchronously and sequentially
     * @see com.oath.cyclops.react.stream.traits.FutureStream#sequential()
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
     * Flatten this stream one level
     * * <pre>
     * {@code
     *  FutureStream.of(Arrays.asList(1,2))
     *                  .flatten();
     *
     *    // stream of 1,2
     *  }
     *  </pre>
     *
     * @see cyclops2.stream.ReactiveSeq#flatten()
     */
    public static <T1> FutureStream<T1> flatten(ReactiveSeq<? extends Stream<? extends T1>> nested){

        ReactiveSeq<? extends T1> res = nested.flatMap(Function.identity());
        if(res instanceof FutureStream){
            return (FutureStream<T1>)res;
        }
        FutureStream<? extends T1> ret = new LazyReact().fromStream(res);
        return narrow(ret);

    }
    public static <T1> FutureStream<T1> narrow(FutureStream<? extends T1> broad){
        return (FutureStream<T1>)broad;

    }

    /* Optional empty, if empty Stream. Otherwise collects to a List
     *	@return this Stream as an Optional
     * @see cyclops2.stream.ReactiveSeq#optional()

    @Override
    default Optional<ListX<U>> optional() {
        return Optional.of(block())
                       .flatMap(list -> list.size() == 0 ? Optional.<ListX<U>> empty() : Optional.of(list));
    }
*/
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
     * @see cyclops2.stream.ReactiveSeq#toCompletableFuture()
     */
    default CompletableFuture<ListX<U>> toCompletableFuture() {
        return CompletableFuture.completedFuture(this)
                                .thenApplyAsync(s -> s.block(), getTaskExecutor());
    }

    /*
     * @see java.util.stream.BaseStream#spliterator()
     */
    @Override
    default Spliterator<U> spliterator() {
        return stream().spliterator();
    }

    /*
     * @see java.util.stream.BaseStream#isParallel()
     */
    @Override
    default boolean isParallel() {
        return false;
    }

    /*
     * @see java.util.stream.Stream#mapToInt(java.util.function.ToIntFunction)
     */
    @Override
    default IntStream mapToInt(final ToIntFunction<? super U> mapper) {
        return stream().mapToInt(mapper);
    }

    /*
     * @see java.util.stream.Stream#mapToLong(java.util.function.ToLongFunction)
     */
    @Override
    default LongStream mapToLong(final ToLongFunction<? super U> mapper) {
        return stream().mapToLong(mapper);
    }

    /*
     * @see java.util.stream.Stream#mapToDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    default DoubleStream mapToDouble(final ToDoubleFunction<? super U> mapper) {
        return stream().mapToDouble(mapper);
    }

    /*
     * @see java.util.stream.Stream#flatMapToInt(java.util.function.Function)
     */
    @Override
    default IntStream flatMapToInt(final Function<? super U, ? extends IntStream> mapper) {
        return stream().flatMapToInt(mapper);
    }

    /*
     * @see java.util.stream.Stream#flatMapToLong(java.util.function.Function)
     */
    @Override
    default LongStream flatMapToLong(final Function<? super U, ? extends LongStream> mapper) {
        return stream().flatMapToLong(mapper);
    }

    /*
     * @see java.util.stream.Stream#flatMapToDouble(java.util.function.Function)
     */
    @Override
    default DoubleStream flatMapToDouble(final Function<? super U, ? extends DoubleStream> mapper) {
        return stream().flatMapToDouble(mapper);
    }

    /*
     * @see java.util.stream.Stream#forEachOrdered(java.util.function.Consumer)
     */
    @Override
    default void forEachOrdered(final Consumer<? super U> action) {
        stream().forEachOrdered(action);

    }

    /*
     * @see java.util.stream.Stream#toArray()
     */
    @Override
    default Object[] toArray() {
        return stream().toArray();
    }

    /*
     * @see java.util.stream.Stream#toArray(java.util.function.IntFunction)
     */
    @Override
    default <A> A[] toArray(final IntFunction<A[]> generator) {
        return stream().toArray(generator);
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#findAny()
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
     * @see cyclops2.stream.ReactiveSeq#toSet()
     */
    @Override
    default Set<U> toSet() {
        return collect(Collectors.toSet());
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#toList()
     */
    @Override
    default List<U> toList() {
        return collect(Collectors.toList());
    }



    /*
     * @see cyclops2.stream.ReactiveSeq#distinct(java.util.function.Function)
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
     * @see cyclops2.stream.ReactiveSeq#triplicate()
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
     * @see cyclops2.stream.ReactiveSeq#triplicate()
     */
    @Override
    default Tuple3<ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>> triplicate(Supplier<Deque<U>> bufferFactory) {
        return Streams.triplicate(stream(),bufferFactory)
                .map1(ReactiveSeq::oneShotStream).map2(ReactiveSeq::oneShotStream)
                .map3(ReactiveSeq::oneShotStream);

    }

    /*
     * Quadruplicate the data in this Stream. To quadruplicate into 3 LazyFutureStreams use actOnFutures#quadruplicate
     * @see cyclops2.stream.ReactiveSeq#quadruplicate()
     */
    @Override
    default Tuple4<ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>> quadruplicate() {
        return Streams.quadruplicate(stream())
                .map1(ReactiveSeq::oneShotStream).map2(ReactiveSeq::oneShotStream)
                .map3(ReactiveSeq::oneShotStream).map4(ReactiveSeq::oneShotStream);

    }
    /*
     * Quadruplicate the data in this Stream. To quadruplicate into 3 LazyFutureStreams use actOnFutures#quadruplicate
     * @see cyclops2.stream.ReactiveSeq#quadruplicate()
     */
    @Override
    default Tuple4<ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>> quadruplicate(Supplier<Deque<U>> bufferFactory) {
        return Streams.quadruplicate(stream(),bufferFactory)
                .map1(ReactiveSeq::oneShotStream).map2(ReactiveSeq::oneShotStream)
                .map3(ReactiveSeq::oneShotStream).map4(ReactiveSeq::oneShotStream);

    }

    /*
     * @see cyclops2.stream.ReactiveSeq#splitSequenceAtHead()
     */
    @Override
    default Tuple2<Option<U>, ReactiveSeq<U>> splitAtHead() {
        return stream()
                          .splitAtHead();

    }

    /*
     * @see cyclops2.stream.ReactiveSeq#splitAt(int)
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> splitAt(final int where) {
        return ReactiveSeq.oneShotStream(stream())
                          .splitAt(where);

    }

    /*
     * @see cyclops2.stream.ReactiveSeq#splitBy(java.util.function.Predicate)
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> splitBy(final Predicate<U> splitter) {
        return ReactiveSeq.oneShotStream(stream())
                          .splitBy(splitter);

    }



    /*
     * @see cyclops2.stream.ReactiveSeq#cycle(com.oath.cyclops.sequence.Monoid, int)
     */
    @Override
    default FutureStream<U> cycle(final Monoid<U> m, final long times) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .cycle(m, times));

    }

    /*
     * @see cyclops2.stream.ReactiveSeq#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, R> FutureStream<Tuple3<U, S, R>> zip3(final Iterable<? extends S> second, final Iterable<? extends R> third) {
        return (FutureStream) fromStream(ReactiveSeq.oneShotStream(stream())
                                                        .zip3(second, third));

    }

    /*
     * @see cyclops2.stream.ReactiveSeq#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> FutureStream<Tuple4<U, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                  final Iterable<? extends T4> fourth) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .zip4(second, third, fourth));

    }



    /*
     * @see cyclops2.stream.ReactiveSeq#grouped(int)
     */
    @Override
    default FutureStream<Vector<U>> grouped(final int groupSize) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .grouped(groupSize));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#scanLeft(com.oath.cyclops.sequence.Monoid)
     */
    @Override
    default FutureStream<U> scanLeft(final Monoid<U> monoid) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .scanLeft(monoid));
    }




    /*
     * @see cyclops2.stream.ReactiveSeq#startsWith(java.lang.Iterable)
     */
    @Override
    default boolean startsWithIterable(final Iterable<U> iterable) {
        return ReactiveSeq.oneShotStream(stream())
                          .startsWithIterable(iterable);
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#startsWith(java.util.Iterator)
     */
    @Override
    default boolean startsWith(final Stream<U> iterator) {
        return ReactiveSeq.oneShotStream(stream())
                          .startsWith(iterator);
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#endsWith(java.lang.Iterable)
     */
    @Override
    default boolean endsWithIterable(final Iterable<U> iterable) {
        return ReactiveSeq.oneShotStream(stream())
                          .endsWithIterable(iterable);
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#endsWith(java.util.stream.Stream)
     */
    @Override
    default boolean endsWith(final Stream<U> stream) {
        return ReactiveSeq.oneShotStream(stream())
                          .endsWith(stream);
    }


    /*
     * @see cyclops2.stream.ReactiveSeq#concatMap(java.util.function.Function)
     */
    @Override
    default <R> FutureStream<R> concatMap(final Function<? super U, ? extends Iterable<? extends R>> fn) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .concatMap(fn));
    }
    /**
     * A potentially non-blocking analog of {@link ReactiveSeq#forEach}.
     * For push based reactive Stream types (created via Spouts of FutureStream}
     *
     * @param action a <a href="package-summary.html#NonInterference">
     *               non-interfering</a> action to perform on the elements
     */
    @Override
    default void forEachAsync(final Consumer<? super U> action){
            peek(action).run();
    }
    @Override
    default <R> FutureStream<R> mergeMap(final Function<? super U, ? extends Publisher<? extends R>> fn) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .mergeMap(fn));
    }
    @Override
    default <R> FutureStream<R> mergeMap(int maxConcurrency, final Function<? super U, ? extends Publisher<? extends R>> fn) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .mergeMap(maxConcurrency,fn));
    }


    /*
     * @see cyclops2.stream.ReactiveSeq#flatMapStream(java.util.function.Function)
     */
    @Override
    default <R> FutureStream<R> flatMapStream(final Function<? super U, BaseStream<? extends R, ?>> fn) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .flatMapStream(fn));
    }



    /*
     * @see cyclops2.stream.ReactiveSeq#append(java.lang.Object[])
     */
    @Override
    default FutureStream<U> appendAll(final U... values) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .appendAll(values));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#prependAll(java.lang.Object[])
     */
    @Override
    default FutureStream<U> prependAll(final U... values) {
        return fromStream(stream().prependAll(values));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#insertAt(int, java.lang.Object[])
     */
    @Override
    default FutureStream<U> insertAt(final int pos, final U... values) {
        return fromStream(stream().insertAt(pos, values));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#deleteBetween(int, int)
     */
    @Override
    default FutureStream<U> deleteBetween(final int start, final int end) {
        return fromStream(stream().deleteBetween(start, end));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#insertAtS(int, java.util.stream.Stream)
     */
    @Override
    default FutureStream<U> insertStreamAt(final int pos, final Stream<U> stream) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .insertStreamAt(pos, stream));
    }



    /*
     * @see cyclops2.stream.ReactiveSeq#skip(long, java.util.concurrent.TimeUnit)
     */
    @Override
    default FutureStream<U> skip(final long time, final TimeUnit unit) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .skip(time, unit));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#limit(long, java.util.concurrent.TimeUnit)
     */
    @Override
    default FutureStream<U> limit(final long time, final TimeUnit unit) {
        getSubscription().registerTimeLimit(unit.toNanos(time));
        return fromStream(stream()
                                   .limit(time, unit));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#skipLast(int)
     */
    @Override
    default FutureStream<U> skipLast(final int num) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .skipLast(num));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#limitLast(int)
     */
    @Override
    default FutureStream<U> limitLast(final int num) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .limitLast(num));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#firstValue()
     */
    @Override
    default U firstValue(U alt) {

        return ReactiveSeq.oneShotStream(stream())
                          .firstValue(null);
    }

    /*
     * Batch the elements in the Stream by a combination of Size and Time
     * If batch exceeds max size it will be split
     * If batch exceeds max time it will be split
     * Excludes Null values (neccessary for timeout handling)
     *
     * @see cyclops2.stream.ReactiveSeq#batchBySizeAndTime(int, long, java.util.concurrent.TimeUnit, java.util.function.Supplier)
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
     * @see cyclops2.stream.ReactiveSeq#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default FutureStream<ListX<U>> groupedStatefullyUntil(final BiPredicate<ListX<? super U>, ? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedUntil(predicate));
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
                .groupedUntil(predicate));
    }
    @Override
    default <C extends Collection<U>,R> FutureStream<R> groupedStatefullyWhile(final BiPredicate<C, ? super U> predicate, final Supplier<C> factory,
                                                                               Function<? super C, ? extends R> finalizer){
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .groupedStatefullyUntil(predicate,factory,finalizer));

    }
    /*
     * @see cyclops2.stream.ReactiveSeq#batchUntil(java.util.function.Predicate)
     */
    @Override
    default FutureStream<Vector<U>> groupedUntil(final Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedUntil(predicate));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#batchWhile(java.util.function.Predicate)
     */
    @Override
    default FutureStream<Vector<U>> groupedWhile(final Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedWhile(predicate));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#batchWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends PersistentCollection<? super U>> FutureStream<C> groupedWhile(final Predicate<? super U> predicate, final Supplier<C> factory) {
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
     * @see cyclops2.stream.ReactiveSeq#batchUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends PersistentCollection<? super U>> FutureStream<C> groupedUntil(final Predicate<? super U> predicate, final Supplier<C> factory) {

        return fromStream(ReactiveSeq.oneShotStream(stream())
                                     .groupedUntil(predicate, factory));
    }

    /*
     * @see cyclops2.stream.ReactiveSeq#recover(java.util.function.Function)
     */
    @Override
    default FutureStream<U> recover(final Function<? super Throwable, ? extends U> fn) {
        return this.onFail(e -> fn.apply(e.getCause()));

    }

    /*
     * @see cyclops2.stream.ReactiveSeq#recover(java.lang.Class, java.util.function.Function)
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
        Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> t2 = LazyFutureStreamUtils.forEachX(this, numberOfElements, consumer);
        t2._2().run();
        return t2._1().join();
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     * <pre>
     * {@code
     *     Subscription next = FutureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::getValue)
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
        Tuple3<CompletableFuture<Subscription>, Runnable, CompletableFuture<Boolean>> t2 = LazyFutureStreamUtils.forEachXWithError(this, numberOfElements, consumer, consumerError);
        t2._2().run();
        return t2._1().join();
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription,
     * when the entire Stream has been processed an onComplete event will be recieved.
     *
     * <pre>
     * {@code
     *     Subscription next = LazyFurtureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::getValue)
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
        t2._2().run();
        return t2._1().join();
    }

    /**
     *  Perform a forEach operation over the Stream    capturing any elements and errors in the supplied consumers,
     * <pre>
     * {@code
     *     Subscription next = FutureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::getValue)
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
        t2._2().run();
    }

    /**
     * Perform a forEach operation over the Stream  capturing any elements and errors in the supplied consumers
     * when the entire Stream has been processed an onComplete event will be recieved.
     *
     * <pre>
     * {@code
     *     Subscription next = FutureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::getValue)
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
        t2._2().run();
    }

    @Override
    default FutureStream<U> onComplete(final Runnable fn) {
        return fromStream(stream().onComplete(fn));
    }

    @Override
    default FutureStream<U> removeFirst(Predicate<? super U> pred) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .removeFirst(pred));
    }

    @Override
    default FutureStream<U> updateAt(int i, U e) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .updateAt(i,e));
    }

    @Override
    default FutureStream<U> removeAt(int pos) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .removeAt(pos));
    }

    @Override
    default FutureStream<U> insertAt(int pos, U value) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .insertAt(pos,value));
    }

    @Override
    default FutureStream<U> insertAt(int pos, Iterable<? extends U> values) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .insertAt(pos,values));
    }

    @Override
    default FutureStream<U> insertAt(int pos, ReactiveSeq<? extends U> values) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .insertAt(pos,values));
    }

    @Override
    default FutureStream<U> removeAt(long index) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .removeAt(index));
    }

    @Override
    default FutureStream<U> plusAll(Iterable<? extends U> list) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .plusAll(list));
    }

    @Override
    default FutureStream<U> plus(U value) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .plus(value));
    }

    @Override
    default FutureStream<U> removeAll(Iterable<? extends U> value) {
        return fromStream(ReactiveSeq.oneShotStream(stream())
                .removeAll(value));
    }


}
