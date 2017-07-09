package com.aol.cyclops2.types.futurestream;

import cyclops.async.LazyReact;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import cyclops.async.SimpleReact;
import cyclops.companion.Streams;
import cyclops.async.adapters.Queue;
import cyclops.async.adapters.QueueFactory;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.internal.react.exceptions.FilteredExecutionPathException;
import com.aol.cyclops2.internal.react.stream.EagerStreamWrapper;
import com.aol.cyclops2.react.SimpleReactFailedStageException;
import com.aol.cyclops2.react.StageWithResults;
import com.aol.cyclops2.react.Status;
import com.aol.cyclops2.react.async.subscription.Continueable;
import com.aol.cyclops2.react.collectors.lazy.Blocker;
import com.aol.cyclops2.util.ThrowsSoftened;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface SimpleReactStream<U> extends BaseSimpleReactStream<U>, BlockingStream<U>, ConfigurableStream<U, CompletableFuture<U>>, ToQueue<U> {

    @Override
    SimpleReact getSimpleReact();

    SimpleReactStream<U> withLastActive(EagerStreamWrapper streamWrapper);

    @Override
    abstract EagerStreamWrapper getLastActive();

    @Override
    SimpleReactStream<U> withTaskExecutor(Executor e);



    @Override
    SimpleReactStream<U> withQueueFactory(QueueFactory<U> queue);

    @Override
    SimpleReactStream<U> withErrorHandler(Optional<Consumer<Throwable>> errorHandler);

    @Override
    SimpleReactStream<U> withSubscription(Continueable sub);

    @Override
    SimpleReactStream<U> withAsync(boolean b);

    @Override
    Continueable getSubscription();

    default <T2> Seq<Tuple2<U, T2>> combineLatest(final SimpleReactStream<T2> right) {
        return EagerFutureStreamFunctions.combineLatest(this, right);
    }

    default <T2> Seq<Tuple2<U, T2>> withLatest(final SimpleReactStream<T2> right) {
        return EagerFutureStreamFunctions.withLatest(this, right);
    }

    /**
     * Give a function access toNested the current stage of a SimpleReact Stream
     *
     * @param consumer
     *            Consumer that will recieve current stage
     * @return Self (current stage)
     */
    default SimpleReactStream<U> self(final Consumer<SimpleReactStream<U>> consumer) {
        return peek(n -> consumer.accept(this));
    }

    default void run() {
        getLastActive().collect();
    }

    /**
     * Split a reactiveStream at a given position. (Operates on futures)
     * <pre>
     * {@code
     * // tuple((1, 2, 3), (4, 5, 6))
     *  EagerFutureStream.of(1, 2, 3, 4, 5,6).splitAt(3)
     *
     *  }</pre>
     * @return
     *
     *
     */
    default Tuple2<SimpleReactStream<U>, SimpleReactStream<U>> splitAt(final long position) {
        final Stream stream = getLastActive().stream();
        final Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>> split = Seq.seq((Stream<CompletableFuture<U>>) stream)
                                                                                      .splitAt(position);

        return new Tuple2(
                          fromListCompletableFuture(split.v1.collect(Collectors.toList())),
                          fromListCompletableFuture(split.v2.collect(Collectors.toList())));
    }

    /**
     * Duplicate a Stream into two equivalent LazyFutureStreams, underlying
     * Stream of Futures is duplicated
     * <pre>
     * {@code
     * EagerFutureStream.of(1, 2, 3).duplicate()
     *
     * results in
     *
     * tuple((1,2,3),(1,2,3))
     * }</pre>
     * Care should be taken not toNested use this method with infinite streams!
     *
     * @return Two equivalent Streams
     *
     * @see #duplicate()
     */

    default Tuple2<SimpleReactStream<U>, SimpleReactStream<U>> duplicate() {
        // unblocking impl
        final Stream stream = getLastActive().stream();
        final Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>> duplicated = Seq.seq((Stream<CompletableFuture<U>>) stream)
                                                                                           .duplicate();
        final Tuple2 dup = new Tuple2(
                                      fromStreamOfFutures(duplicated.v1), fromStreamOfFutures(duplicated.v2));

        return dup;
    }

    /**
     * Zip two Streams, zipping against the underlying futures of this reactiveStream
     *
     * @param other
     * @return
     */
    default <R> SimpleReactStream<Tuple2<U, R>> zip(final Stream<R> other) {

        final Seq seq = Seq.seq(getLastActive().stream())
                           .zip(Seq.seq(other));
        final Seq<Tuple2<CompletableFuture<U>, R>> withType = seq;
        final SimpleReactStream futureStream = fromStreamOfFutures((Stream) withType.map(t -> t.v1.thenApply(v -> Tuple.tuple(t.v1.join(), t.v2))));

        return futureStream;

    }

    /**
     * Zip two Streams, zipping against the underlying futures of both Streams
     * Placeholders (Futures) will be populated immediately in the new zipped Stream and results
     * will be populated asyncrhonously
     *
     * @param other  Another FutureStream toNested zip Futures with
     * @return New Sequence of CompletableFutures
     */
    default <R> SimpleReactStream<Tuple2<U, R>> zip(final SimpleReactStream<R> other) {
        final Seq seq = Seq.seq(getLastActive().stream())
                           .zip(Seq.seq(other.getLastActive()
                                             .stream()));
        final Seq<Tuple2<CompletableFuture<U>, CompletableFuture<R>>> withType = seq;
        final SimpleReactStream futureStream = fromStreamOfFutures((Stream) withType.map(t -> CompletableFuture.allOf(t.v1, t.v2)
                                                                                                               .thenApply(v -> Tuple.tuple(t.v1.join(),
                                                                                                                                           t.v2.join()))));

        return futureStream;

    }

    /**
     * Zip this Stream with an index, but Zip based on the underlying tasks, not completed results.
     *
     * e.g.
     * two function that return method name, but take varying lengths of time.
     * <pre>
     * <code>
     * EagerFutureStream.react(()-&gt;takesALotOfTime(),()-&gt;veryQuick()).zipWithIndex();
     *
     *  [["takesALotOfTime",0],["veryQuick",1]]
     *
     *  Where as with standard zipWithIndex you would get a new Stream ordered by completion
     *
     *  [["veryQuick",0],["takesALotOfTime",1]]
     * </code>
     * </pre>
     */
    default SimpleReactStream<Tuple2<U, Long>> zipWithIndex() {

        final Seq seq = Seq.seq(getLastActive().stream()
                                               .iterator())
                           .zipWithIndex();
        final Seq<Tuple2<CompletableFuture<U>, Long>> withType = seq;
        final SimpleReactStream futureStream = fromStreamOfFutures((Stream) withType.map(t -> t.v1.thenApply(v -> Tuple.tuple(t.v1.join(), t.v2))));
        return futureStream;

    }

    /**
     * Return takeOne Stream out of provided Streams that starts emitted results
     *
     * @param futureStreams Streams toNested race
     * @return First Stream toNested skip emitting values
     */
    @SafeVarargs
    public static <U> SimpleReactStream<U> firstOf(final SimpleReactStream<U>... futureStreams) {
        return EagerFutureStreamFunctions.firstOf(futureStreams);
    }

    /**
     * Reversed, operating on the underlying futures.
     * <pre>
     * {@code
     *
     * // (3, 2, 1) EagerFutureStream.of(1, 2, 3).reverse()
     * }</pre>
     *
     * @return
     */
    default SimpleReactStream<U> reverse() {
        final EagerStreamWrapper lastActive = getLastActive();
        final ListIterator<CompletableFuture> it = lastActive.list()
                                                             .listIterator();
        final List<CompletableFuture> result = new ArrayList<>();
        while (it.hasPrevious())
            result.add(it.previous());

        final EagerStreamWrapper limited = lastActive.withList(result);
        return this.withLastActive(limited);

    }

    /**
     * Returns a limited interval from a given Stream.
     *
     * <pre>
     * {@code
     * // (4, 5) EagerFutureStream.of(1, 2, 3, 4, 5, 6).sliceFutures(3, 5)
     * }
     *</pre>
     * @see #slice(long, long)
     */

    default SimpleReactStream<U> slice(final long from, final long to) {
        final List noType = Seq.seq(getLastActive().stream())
                               .slice(from, to)
                               .collect(Collectors.toList());
        return fromListCompletableFuture(noType);
    }

    /**
     * Perform a limit operation on the underlying Stream of Futures
     * In contrast toNested EagerFutureStream#limit this removes entries basaed on their
     * skip position
     *
     * <pre>
     * {@code
     * EagerFutureStream.of(()>loadSlow(),()>loadMedium(),()>loadFast())
     * 				.limitFutures(2)
     * }
     *
     * //[loadSlow, loadMedium]
     * </pre>
     *
     *
     *
     * @param maxSize The size of the subsequent Stream
     * @return limited Stream
     */
    default SimpleReactStream<U> limit(final long maxSize) {

        final EagerStreamWrapper lastActive = getLastActive();
        final EagerStreamWrapper limited = lastActive.withList(lastActive.stream()
                                                                         .limit(maxSize)
                                                                         .collect(Collectors.toList()));
        return this.withLastActive(limited);

    }

    /**
     * In contast toNested EagerFutureStream#skip skipFutures will skip the takeOne n entries
     * of the underlying Stream of Futures.
     * <pre>
     * {@code
     * EagerFutureStream.of(()>loadSlow(),()>loadMedium(),()>loadFast())
     * 				.skip(2)
     * }
     *
     * //[loadFast]
     * </pre>
     *
     * @param n
     * @return
     */
    @Override
    default SimpleReactStream<U> skip(final long n) {
        final EagerStreamWrapper lastActive = getLastActive();
        final EagerStreamWrapper limited = lastActive.withList(lastActive.stream()
                                                                         .skip(n)
                                                                         .collect(Collectors.toList()));
        return this.withLastActive(limited);
    }

    /**
     * Return a Stream with the same values as this Stream, but with all values omitted until the provided reactiveStream starts emitting values.
     * Provided Stream ends the reactiveStream of values from this reactiveStream.
     *
     * @param s Stream that will skip the emission of values from this reactiveStream
     * @return Next stage in the Stream but with all values skipped until the provided Stream starts emitting
     */
    default <T> ReactiveSeq<U> skipUntil(final SimpleReactStream<T> s) {
        return EagerFutureStreamFunctions.skipUntil(this, s);
    }

    /**
     * Return a Stream with the same values, but will stop emitting values once the provided Stream starts toNested emit values.
     * e.g. if the provided Stream is asynchronously refreshing state from some remote store, this reactiveStream can proceed until
     * the provided Stream succeeds in retrieving data.
     *
     * @param s Stream that will stop the emission of values from this reactiveStream
     * @return Next stage in the Stream but will only emit values until provided Stream starts emitting values
     */
    default <T> ReactiveSeq<U> takeUntil(final SimpleReactStream<T> s) {
        return EagerFutureStreamFunctions.takeUntil(this, s);
    }

    /**
     * Cancel the CompletableFutures in this stage of the reactiveStream
     */
    default void cancel() {
        this.streamCompletableFutures()
            .forEach(next -> next.cancel(true));
    }

    default ListX<SimpleReactStream<U>> copySimpleReactStream(final int times) {

        return (ListX) Streams.toBufferingCopier(getLastActive().stream()
                                                                    .iterator(),
                                                     times)
                                  .stream()
                                  .map(it -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false))
                                  .<BaseSimpleReactStream<U>> map(fs -> this.getSimpleReact()
                                                                            .construct(fs))
                                  .toListX();
    }

    /*
     * React toNested new events with the supplied function on the supplied Executor
     *
     *	@param fn Apply toNested incoming events
     *	@param service Service toNested execute function on
     *	@return next stage in the Stream
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    default <R> SimpleReactStream<R> then(final Function<? super U, ? extends R> fn, final Executor service) {

        return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(s -> s.<CompletableFuture> map((
                ft) -> ft.thenApplyAsync(SimpleReactStream.<U, R> handleExceptions(fn), getTaskExecutor()))));
    }

    /*
     * React toNested new events with the supplied function on the supplied Executor
     *
     *	@param fn Apply toNested incoming events
     *	@param service Service toNested execute function on
     *	@return next stage in the Stream
     */
    @Override
    default <R> SimpleReactStream<R> thenSync(final Function<? super U, ? extends R> fn) {

        return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(s -> s.<CompletableFuture> map((
                ft) -> ft.thenApply(SimpleReactStream.<U, R> handleExceptions(fn)))));
    }

    /**
     * @param collector
     *            toNested perform aggregation / reduction operation on the results
     *            from active stage (e.g. toNested Collect into a List or String)
     * @param fn
     *            Function that receives the results of all currently active
     *            tasks as input
     * @return A new builder object that can be used toNested define the next stage in
     *         the dataflow
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default <R1, R2> SimpleReactStream<R2> allOf(final Collector<? super U, ?, R1> collector, final Function<? super R1, ? extends R2> fn) {

        final CompletableFuture[] array = lastActiveArray(getLastActive());
        final CompletableFuture cf = CompletableFuture.allOf(array);
        final Function<Exception, R2> f = (final Exception e) -> {
            BlockingStreamHelper.capture(e, getErrorHandler());
            return BlockingStreamHelper.block(this, Collectors.toList(), new EagerStreamWrapper(
                                                                                                Stream.of(array), getErrorHandler()));
        };
        final CompletableFuture onFail = cf.exceptionally(f);
        final CompletableFuture onSuccess = onFail.thenApplyAsync((result) -> {
            return new StageWithResults(
                                        getTaskExecutor(), null,
                                        result).submit(() -> fn.apply(BlockingStreamHelper.aggregateResultsCompletable(collector, Stream.of(array)
                                                                                                                                        .collect(Collectors.toList()),
                                                                                                                       getErrorHandler())));
        } , getTaskExecutor());
        return (SimpleReactStream<R2>) withLastActive(new EagerStreamWrapper(
                                                                             onSuccess, getErrorHandler()));

    }

    /**
     * React toNested the completion of any of the events in the previous stage. Will not work reliably with Streams
     * where filter has been applied in earlier stages. (As Filter completes the Stream for events that are filtered out, they
     * potentially shortcircuit the completion of the stage).
     *
     * @param fn Function toNested applyHKT when any of the previous events complete
     * @return Next stage in the reactiveStream
     */
    default <R> SimpleReactStream<R> anyOf(final Function<? super U, ? extends R> fn) {
        final CompletableFuture[] array = lastActiveArray(getLastActive());
        final CompletableFuture cf = CompletableFuture.anyOf(array);
        final CompletableFuture onSuccess = cf.thenApplyAsync(fn, getTaskExecutor());

        return (SimpleReactStream<R>) withLastActive(new EagerStreamWrapper(
                                                                            onSuccess, getErrorHandler()));

    }

    @SuppressWarnings("rawtypes")
    static CompletableFuture[] lastActiveArray(final EagerStreamWrapper lastActive) {
        return lastActive.list()
                         .toArray(new CompletableFuture[0]);
    }



    @Override
    default <R> SimpleReactStream<R> fromStream(final Stream<R> stream) {

        return (SimpleReactStream<R>) this.withLastActive(getLastActive().withNewStream(stream.map(CompletableFuture::completedFuture),
                                                                                        this.getSimpleReact()));
    }

    /**
     * Construct a SimpleReactStream from provided Stream of CompletableFutures
     *
     * @param stream JDK Stream toNested construct new SimpleReactStream from
     * @return SimpleReactStream
     */
    default <R> SimpleReactStream<R> fromStreamOfFutures(final Stream<CompletableFuture<R>> stream) {
        final Stream noType = stream;
        return (SimpleReactStream<R>) this.withLastActive(getLastActive().withNewStream(noType, this.getSimpleReact()));
    }

    default <R> SimpleReactStream<R> fromStreamCompletableFutureReplace(final Stream<CompletableFuture<R>> stream) {
        final Stream noType = stream;
        return (SimpleReactStream<R>) this.withLastActive(getLastActive().withStream(noType));
    }

    default <R> SimpleReactStream<R> fromListCompletableFuture(final List<CompletableFuture<R>> list) {
        final List noType = list;
        return (SimpleReactStream<R>) this.withLastActive(getLastActive().withList(noType));
    }

    /**
     * React <b>transform</b>
     *
     *
     *
     * Unlike 'with' this method is fluent, and returns another Stage Builder
     * that can represent the next stage in the dataflow.
     *
     * <pre>
     * {@code
        new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
                .transform((it) -> it * 100)
                .transform((it) -> "*" + it)
    
                }
    </pre>
     *
     * React transform allows event reactors toNested be chained. Unlike React with, which
     * returns a toX of Future references, React transform is a fluent
     * interface that returns the React builder - allowing further reactors toNested
     * be added toNested the chain.
     *
     * React transform does not block.
     *
     * React with can be called after React transform which gives access toNested the full
     * CompleteableFuture api. CompleteableFutures can be passed back into
     * SimpleReact via SimpleReact.react(streamOfCompleteableFutures);
     *
     * See this blog post for examples of what can be achieved via
     * CompleteableFuture :- <a href=
     * 'http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.html'>http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.htm
     * l </a>
     *
     * @param fn
     *            Function toNested be applied toNested the results of the currently active
     *            event tasks
     * @return A new builder object that can be used toNested define the next stage in
     *         the dataflow
     */
    @Override
    @SuppressWarnings("unchecked")
    default <R> SimpleReactStream<R> then(final Function<? super U, ? extends R> fn) {
        if (!isAsync())
            return thenSync(fn);
        final Function<Stream<CompletableFuture>, Stream<CompletableFuture>> streamMapper = s -> s.<CompletableFuture> map(ft -> ft.thenApplyAsync(SimpleReactStream.<U, R> handleExceptions(fn),
                                                                                                                                                   getTaskExecutor()));
        return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
    }

    /**
     * Peek asynchronously at the results in the current stage. Current results
     * are passed through toNested the next stage.
     *
     * @param consumer
     *            That will recieve current results
     * @return A new builder object that can be used toNested define the next stage in
     *         the dataflow
     */
    @Override
    default SimpleReactStream<U> peek(final Consumer<? super U> consumer) {
        if (!isAsync())
            return peekSync(consumer);
        return then((t) -> {
            consumer.accept(t);
            return t;
        });
    }

    /**
     * Synchronous peek operator
     *
     * @param consumer Peek consumer
     * @return Next stage
     */
    @Override
    default SimpleReactStream<U> peekSync(final Consumer<? super U> consumer) {
        return thenSync((t) -> {
            consumer.accept(t);
            return t;
        });
    }

    static <U, R> Function<U, R> handleExceptions(final Function<? super U, ? extends R> fn) {
        return (input) -> {
            try {
                return fn.apply(input);
            } catch (final Throwable t) {

                throw new SimpleReactFailedStageException(
                                                          input, t);

            }
        };
    }

    /**
     * Perform a flatMap operation where the CompletableFuture type returned is flattened from the resulting Stream
     * If in async mode this operation is performed asyncrhonously
     * If in sync mode this operation is performed synchronously
     *
     * <pre>
     * {@code
     * assertThat( new SimpleReact()
                                        .of(1,2,3)
                                        .flatMapCompletableFuture(i->CompletableFuture.completedFuture(i))
                                        .block(),equalTo(Arrays.asList(1,2,3)));
     * }
     * </pre>
     *
     * In this example the result of the flatMapCompletableFuture is 'flattened' toNested the raw integer values
     *
     *
     * @param flatFn flatMap function
     * @return Flatten Stream with flatFn applied
     */
    @Override
    default <R> SimpleReactStream<R> flatMapToCompletableFuture(final Function<? super U, CompletableFuture<? extends R>> flatFn) {
        if (!isAsync())
            return flatMapToCompletableFutureSync(flatFn);
        final Function<Stream<CompletableFuture>, Stream<CompletableFuture>> streamMapper = s -> (Stream) s.<CompletableFuture> map(ft -> ft.thenComposeAsync(SimpleReactStream.handleExceptions(flatFn),
                                                                                                                                                              getTaskExecutor()));
        return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
    }

    /**
     * Perform a flatMap operation where the CompletableFuture type returned is flattened from the resulting Stream
     * This operation is performed synchronously
     *
     * <pre>
     * {@code
     * assertThat( new SimpleReact()
                                        .of(1,2,3)
                                        .flatMapCompletableFutureSync(i->CompletableFuture.completedFuture(i))
                                        .block(),equalTo(Arrays.asList(1,2,3)));
     * }
     *</pre>
     * In this example the result of the flatMapCompletableFuture is 'flattened' toNested the raw integer values
     *
     *
     * @param flatFn flatMap function
     * @return Flatten Stream with flatFn applied
     */
    @Override
    default <R> SimpleReactStream<R> flatMapToCompletableFutureSync(final Function<? super U, CompletableFuture<? extends R>> flatFn) {

        final Function<Stream<CompletableFuture>, Stream<CompletableFuture>> streamMapper = s -> (Stream) s.<CompletableFuture> map(ft -> ft.thenCompose(SimpleReactStream.handleExceptions(flatFn)));
        return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
    }

    /**
     * Allows aggregate values in a Stream toNested be flatten into a singleUnsafe Stream.
     * flatMap function turn each aggregate value into it's own Stream, and SimpleReact aggregates those Streams
     * into a singleUnsafe flattened reactiveStream
     *
     * @param flatFn Function that coverts a value (e.g. a Collection) into a Stream
     * @return SimpleReactStream
     */
    @Override
    default <R> SimpleReactStream<R> flatMap(final Function<? super U, ? extends Stream<? extends R>> flatFn) {

        //need toNested pass in a builder in the constructor and build using it
        return (SimpleReactStream) getSimpleReact().construct(Stream.of())
                                                   .withSubscription(getSubscription())
                                                   .withQueueFactory((QueueFactory<Object>) getQueueFactory())
                                                   .fromStream(toQueue().stream(getSubscription())
                                                                        .flatMap(flatFn));
    }

    /**
     *
     * React <b>with</b>
     *
     * Asynchronously applyHKT the function supplied toNested the currently active event
     * tasks in the dataflow.
     *
     * While most methods in this class are fluent, and return a reference toNested a
     * SimpleReact Stage builder, this method can be used this method toNested access
     * the underlying CompletableFutures.
     *
     * <pre>
        {@code
        List<CompletableFuture<Integer>> futures = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
                                    .with((it) -> it * 100);
        }
        </pre>
     *
     * In this instance, 3 suppliers generate 3 numbers. These may be executed
     * in parallel, when they complete each number will be multiplied by 100 -
     * as a separate parrellel task (handled by a ForkJoinPool or configurable
     * task executor). A List of Future objects will be returned immediately
     * from Simple React and the tasks will be executed asynchronously.
     *
     * React with does not block.
     *
     * @param fn
     *            Function toNested be applied toNested the results of the currently active
     *            event tasks
     * @return A list of CompletableFutures which will contain the result of the
     *         application of the supplied function
     */
    @SuppressWarnings("unchecked")
    default <R> List<CompletableFuture<R>> with(final Function<? super U, ? extends R> fn) {

        return getLastActive().stream()
                              .map(future -> (CompletableFuture<R>) future.thenApplyAsync(fn, getTaskExecutor()))
                              .collect(Collectors.toList());
    }

    /**
     * Removes elements that do not match the supplied predicate from the
     * dataflow
     *
     * @param p
     *            Predicate that will be used toNested filter elements from the
     *            dataflow
     * @return A new builder object that can be used toNested define the next stage in
     *         the dataflow
     */
    @Override
    @SuppressWarnings("unchecked")
    default SimpleReactStream<U> filter(final Predicate<? super U> p) {
        if (!isAsync())
            return filterSync(p);
        final Function<Stream<CompletableFuture>, Stream<CompletableFuture>> fn = s -> s.map(ft -> ft.thenApplyAsync((in) -> {
            if (!p.test((U) in)) {
                throw new FilteredExecutionPathException();
            }
            return in;
        }));
        return this.withLastActive(getLastActive().stream(fn));

    }

    /**
     * Synchronous filtering operation
     *
     * Removes elements that do not match the supplied predicate from the
     * dataflow
     *
     * @param p
     *            Predicate that will be used toNested filter elements from the
     *            dataflow
     * @return A new builder object that can be used toNested define the next stage in
     *         the dataflow
     */
    @Override
    default SimpleReactStream<U> filterSync(final Predicate<? super U> p) {
        final Function<Stream<CompletableFuture>, Stream<CompletableFuture>> fn = s -> s.map(ft -> ft.thenApply((in) -> {
            if (!p.test((U) in)) {
                throw new FilteredExecutionPathException();
            }
            return in;
        }));
        return this.withLastActive(getLastActive().stream(fn));

    }

    /**
     * @return A Stream of CompletableFutures that represent this stage in the
     *         dataflow
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    default <T> Stream<CompletableFuture<T>> streamCompletableFutures() {
        final Stream s = this.getLastActive()
                             .stream();
        return s;

    }

    /*
      * Merge two simple-react Streams, by merging the Stream of underlying
     * futures - not suitable for merging infinite Streams - use
     * see FutureStream#switchOnNext for infinite Streams
     *
     * <pre>
     * {@code
     * List<String> result = 	SimpleReactStream.of(1,2,3)
     * 											 .zip(FutureStream.of(100,200,300))
                                                  .map(it ->it+"!!")
                                                  .toList();
        assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
     *
     * }
     * </pre>
     *
     * @param s Stream toNested zip
     *
     * @return Next stage in reactiveStream
     *
     * @see
     * com.aol.simple.react.reactiveStream.traits.FutureStream#zip(com.aol.simple.
     * react.reactiveStream.traits.SimpleReactStream)
     */

    @SuppressWarnings({ "unchecked", "rawtypes" })
    default SimpleReactStream<U> merge(final SimpleReactStream<U>... s) {

        final List merged = Stream.concat(Stream.of(this), Stream.of(s))
                                  .map(stream -> ((SimpleReactStream) stream).getLastActive()
                                                                             .list())
                                  .flatMap(Collection::stream)
                                  .collect(Collectors.toList());
        return this.withLastActive(new EagerStreamWrapper(
                                                          merged, getErrorHandler()));
    }

    /**
     * React and <b>block</b> with <b>breakout</b>
     *
     * Sometimes you may not need toNested block until all the work is complete, one
     * result or a subset may be enough. To faciliate this, block can accept a
     * Predicate functional interface that will allow SimpleReact toNested stop
     * blocking the current thread when the Predicate has been fulfilled. E.g.
     *
     * <pre>
     * {@code 
        List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
                                                .transform(it -> it * 100)
                                                .transform(it -> "*" + it)
                                                .block(status -> status.getCompleted()->1);
                
         }       
      </pre>
     *
     * In this example the current thread will unblock once more than one result
     * has been returned.
     *
     * @param breakout
     *            Predicate that determines whether the block should be
     *            continued or removed
     * @return List of Completed results of currently active stage at full
     *         completion point or when breakout triggered (which ever comes
     *         takeOne). throws InterruptedException,ExecutionException
     */
    @ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
    default ListX<U> block(final Predicate<Status<U>> breakout) {
        return new Blocker<U>(
                              (List)getLastActive().list(), getErrorHandler()).block(breakout);
    }

    /**
     * @param collector
     *            toNested perform aggregation / reduction operation on the results
     *            (e.g. toNested Collect into a List or String)
     * @param breakout
     *            Predicate that determines whether the block should be
     *            continued or removed
     * @return Completed results of currently active stage at full completion
     *         point or when breakout triggered (which ever comes takeOne), in
     *         aggregated in form determined by collector throws
     *         InterruptedException,ExecutionException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
    default <A, R> R block(final Collector<? super U, A, R> collector, final Predicate<Status<U>> breakout) {

        return block(breakout).stream()
                              .collect(collector);
    }

    /**
     * Merge this reactive dataflow with another - recommended for merging
     * different types. To merge flows of the same type the instance method
     * merge is more appropriate.
     *
     * @param s1
     *            Reactive stage builder toNested merge
     * @param s2
     *            Reactive stage builder toNested merge
     * @return Merged dataflow
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <R> SimpleReactStream<R> merge(final SimpleReactStream s1, final SimpleReactStream s2) {
        final List merged = Stream.of(s1.getLastActive()
                                        .list(),
                                      s2.getLastActive()
                                        .list())
                                  .flatMap(Collection::stream)
                                  .collect(Collectors.toList());
        return s1.withLastActive(new EagerStreamWrapper(
                                                        merged, s1.getErrorHandler()));
    }

    /**
     * React <b>onFail</b>
     *
     *
     * Define a function that can be used toNested recover from exceptions during the
     * preceeding stage of the dataflow. e.g.
     *
     *
     *
     * onFail allows disaster recovery for each task (a separate onFail should
     * be configured for each react phase that can fail). E.g. if reading data
     * from an external service fails, but default value is acceptable - onFail
     * is a suitable mechanism toNested set the default value. Asynchronously applyHKT
     * the function supplied toNested the currently active event tasks in the
     * dataflow.
     *
     * <pre>
      {@code
    List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 100, () -> 2, () -> 3)
                    .transform(it -> {
                        if (it == 100)
                            throw new RuntimeException("boo!");
    
                        return it;
                    })
                    .onFail(e -> 1)
                    .transform(it -> "*" + it)
                    .block();
    
    
    
      }
    
          </pre>
     *
     *
     * In this example onFail recovers from the RuntimeException thrown when the
     * input toNested the takeOne 'transform' stage is 100.
     *
     * @param fn
     *            Recovery function, the exception is input, and the recovery
     *            value is emitted
     * @return A new builder object that can be used toNested define the next stage in
     *         the dataflow
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default SimpleReactStream<U> onFail(final Function<? super SimpleReactFailedStageException, ? extends U> fn) {
        return onFail(Throwable.class, fn);
    }

    /**
     * Recover for a particular class of exceptions only. Chain onFail methods from specific Exception classes
     * toNested general, as Exceptions will be caught and handled in order.
     * e.g.
     * <pre>
     * {@code
                onFail(IOException.class, recoveryFunction1)
                .onFail(Throwable.class,recovertyFunction2)
     *  }
     * </pre>
     * For an IOException recoveryFunction1 will be executed
     *
     * but with the definitions reveresed
     * <pre>
      {@code
        onFail(Throwable.class,recovertyFunction2)
            .onFail(IOException.class, recoveryFunction1)
        }
        </pre>
    
     * recoveryFunction1 will not be called
     *
     *
     * @param exceptionClass Class of exceptions toNested recover from
     * @param fn Recovery function
     * @return recovery value
     */
    @Override
    default SimpleReactStream<U> onFail(final Class<? extends Throwable> exceptionClass,
            final Function<? super SimpleReactFailedStageException, ? extends U> fn) {

        final Function<Stream<CompletableFuture>, Stream<CompletableFuture>> mapper = s -> s.map((ft) -> ft.exceptionally((t) -> {
            if (t instanceof FilteredExecutionPathException)
                throw (FilteredExecutionPathException) t;
            Throwable throwable = (Throwable) t;
            if (t instanceof CompletionException)
                throwable = ((Exception) t).getCause();

            final SimpleReactFailedStageException simpleReactException = assureSimpleReactException(throwable);//exceptions from initial supplier won't be wrapper in SimpleReactFailedStageException
            if (exceptionClass.isAssignableFrom(simpleReactException.getCause()
                                                                    .getClass()))
                return ((Function) fn).apply(simpleReactException);
            throw simpleReactException;

        }));
        return this.withLastActive(getLastActive().stream(mapper));
    }

    static SimpleReactFailedStageException assureSimpleReactException(final Throwable throwable) {
        if (throwable instanceof SimpleReactFailedStageException)
            return (SimpleReactFailedStageException) throwable;
        return new SimpleReactFailedStageException(
                                                   null, throwable);
    }

    /**
     * React <b>capture</b>
     *
     * While onFail is used for disaster recovery (when it is possible toNested
     * recover) - capture is used toNested capture those occasions where the full
     * pipeline has failed and is unrecoverable.
     *
     * <pre>
        {@code
        List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
            .transform(it -> it * 100)
            .transform(it -> {
                if (it == 100)
                    throw new RuntimeException("boo!");
    
                return it;
            })
            .onFail(e -> 1)
            .transform(it -> "*" + it)
            .transform(it -> {
    
                if ("*200".equals(it))
                    throw new RuntimeException("boo!");
    
                return it;
            })
            .capture(e -> logger.error(e.getMessage(),e))
            .block();
            }
        </pre>
       
     *
     * In this case, strings will only contain the two successful results (for
     * ()-&gt;1 and ()-&gt;3), an exception for the chain starting from Supplier
     * ()-&gt;2 will be logged by capture. Capture will not capture the
     * exception thrown when an Integer value of 100 is found, but will catch
     * the exception when the String value "*200" is passed along the chain.
     *
     * @param errorHandler
     *            A consumer that recieves and deals with an unrecoverable error
     *            in the dataflow
     * @return A new builder object that can be used toNested define the next stage in
     *         the dataflow
     */
    @Override
    @SuppressWarnings("unchecked")
    default SimpleReactStream<U> capture(final Consumer<Throwable> errorHandler) {
        return this.withLastActive(this.getLastActive()
                                       .withErrorHandler(Optional.of(errorHandler)))
                   .withErrorHandler(Optional.of(errorHandler));
    }

    /**
     * React and <b>allOf</b>
     *
     * allOf is a non-blocking equivalent of block. The current thread is not
     * impacted by the calculations, but the reactive chain does not continue
     * until all currently alloted tasks complete. The allOf task is transform
     * provided with a list of the results from the previous tasks in the chain.
     *
     * <pre>
      {@code
      boolean blocked[] = {false};
        new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
    
                .transform(it -> {
                    try {
                        Thread.sleep(50000);
                    } catch (Exception e) {
    
                    }
                    blocked[0] =true;
                    return 10;
                })
                .allOf( it -> it.size());
    
        assertThat(blocked[0],is(false));
    
      }
        </pre>
     *
     * In this example, the current thread will continue and assert that it is
     * not blocked, allOf could continue and be executed in a separate thread.
     *
     * @param fn
     *            Function that recieves the results of all currently active
     *            tasks as input
     * @return A new builder object that can be used toNested define the next stage in
     *         the dataflow
     */
    @SuppressWarnings("unchecked")
    default <R> SimpleReactStream<R> allOf(final Function<? super List<U>, ? extends R> fn) {

        return (SimpleReactStream<R>) allOf(Collectors.<U> toList(), fn);

    }

    /**
     * Convert between an Lazy and Eager future reactiveStream,
     * can be used toNested take advantages of each approach during a singleUnsafe Stream
     *
     * @return An EagerFutureStream from this LazyFutureStream, will use the same executors
     */
    default FutureStream<U> convertToLazyStream() {
        return new LazyReact(
                             getTaskExecutor())
                                               .fromStreamFutures((Stream) getLastActive().stream());
    }

    /*
     * Execute subsequent stages on the completing thread (until async called)
     * 10X faster than async execution.
     * Use async for blocking IO or distributing work across threads or cores.
     * Switch toNested sync for non-blocking tasks when desired thread utlisation reached
     *
     *	@return Version of FutureStream that will use sync CompletableFuture methods
     *
     */
    @Override
    default SimpleReactStream<U> sync() {
        return this.withAsync(false);
    }

    /*
     * Execute subsequent stages by submission toNested an Executor for async execution
     * 10X slower than sync execution.
     * Use async for blocking IO or distributing work across threads or cores.
     * Switch toNested sync for non-blocking tasks when desired thread utlisation reached
     *
     *
     *	@return Version of FutureStream that will use async CompletableFuture methods
     *
     */
    @Override
    default SimpleReactStream<U> async() {
        return this.withAsync(true);
    }

    /**
     *
     * flatMap / bind implementation that returns the correct type (SimpleReactStream)
     *
     * @param stream Stream toNested flatMap
     * @param flatFn flatMap function
     * @return
     */
    static <U, R> SimpleReactStream<R> bind(final SimpleReactStream<U> stream, final Function<U, BaseSimpleReactStream<R>> flatFn) {

        return join(stream.then(flatFn));

    }

    /**
     * flatten nested SimpleReactStreams
     *
     * @param stream Stream toNested flatten
     * @return flattened Stream
     */
    static <U, R> SimpleReactStream<R> join(final SimpleReactStream<BaseSimpleReactStream<U>> stream) {
        final Queue queue = stream.getQueueFactory()
                                  .build();
        stream.then(it -> it.sync()
                            .then(in->queue.offer(in)))
              .allOf(it -> queue.close());

        return stream.fromStream(queue.stream(stream.getSubscription()));

    }

}