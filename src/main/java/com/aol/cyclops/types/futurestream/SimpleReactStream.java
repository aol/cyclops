package com.aol.cyclops.types.futurestream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
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

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.QueueFactory;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.react.exceptions.FilteredExecutionPathException;
import com.aol.cyclops.internal.react.stream.EagerStreamWrapper;
import com.aol.cyclops.react.SimpleReactFailedStageException;
import com.aol.cyclops.react.StageWithResults;
import com.aol.cyclops.react.Status;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.react.collectors.lazy.Blocker;
import com.aol.cyclops.util.ThrowsSoftened;
import com.aol.cyclops.util.stream.StreamUtils;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.nurkiewicz.asyncretry.policy.AbortRetryException;


public interface SimpleReactStream<U> extends BaseSimpleReactStream<U>,  
                BlockingStream<U>,
                ConfigurableStream<U,CompletableFuture<U>>,
                ToQueue<U>{

    SimpleReact getSimpleReact();
    SimpleReactStream<U> withLastActive(EagerStreamWrapper streamWrapper);
    abstract EagerStreamWrapper getLastActive();
    SimpleReactStream<U> withTaskExecutor(Executor e);
    SimpleReactStream<U> withRetrier(RetryExecutor retry);


    SimpleReactStream<U> withQueueFactory(QueueFactory<U> queue);

    SimpleReactStream<U>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler);
    SimpleReactStream<U> withSubscription(Continueable sub);
    SimpleReactStream<U> withAsync(boolean b);

    Continueable getSubscription();

    default  <T2> Seq<Tuple2<U, T2>> combineLatest(
            SimpleReactStream<T2> right) {
        return EagerFutureStreamFunctions.combineLatest(this,right);
    }
    default  <T2> Seq<Tuple2<U, T2>> withLatest(
            SimpleReactStream<T2> right) {
        return EagerFutureStreamFunctions.withLatest(this,right);
    }
    /**
     * Give a function access to the current stage of a SimpleReact Stream
     *
     * @param consumer
     *            Consumer that will recieve current stage
     * @return Self (current stage)
     */
    default SimpleReactStream<U> self(Consumer<SimpleReactStream<U>> consumer) {
        return peek(n->consumer.accept(this));
    }
    /**
     * Split a stream at a given position. (Operates on futures)
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
    default Tuple2<SimpleReactStream<U>, SimpleReactStream<U>> splitAt(
            long position) {
        Stream stream = getLastActive().stream();
        Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>> split = Seq
                .seq((Stream<CompletableFuture<U>>) stream).splitAt(position);

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
     * Care should be taken not to use this method with infinite streams!
     *
     * @return Two equivalent Streams
     *
     * @see #duplicate()
     */

    default Tuple2<SimpleReactStream<U>, SimpleReactStream<U>> duplicate() {
        // unblocking impl
        Stream stream = getLastActive().stream();
        Tuple2<Seq<CompletableFuture<U>>, Seq<CompletableFuture<U>>> duplicated = Seq
                .seq((Stream<CompletableFuture<U>>) stream).duplicate();
        Tuple2 dup =new Tuple2(fromStreamOfFutures(duplicated.v1),
                fromStreamOfFutures(duplicated.v2));

        return (Tuple2<SimpleReactStream<U>,SimpleReactStream<U>>) dup;
    }
    /**
     * Zip two Streams, zipping against the underlying futures of this stream
     *
     * @param other
     * @return
     */
    default <R> SimpleReactStream<Tuple2<U,R>> zip(Stream<R> other) {

        Seq seq = Seq.seq(getLastActive().stream()).zip(Seq.seq(other));
        Seq<Tuple2<CompletableFuture<U>,R>> withType = (Seq<Tuple2<CompletableFuture<U>,R>>)seq;
        SimpleReactStream futureStream = fromStreamOfFutures((Stream)withType.map(t ->t.v1.thenApply(v -> Tuple.tuple(t.v1.join(),t.v2)))
                );


        return (SimpleReactStream<Tuple2<U,R>>)futureStream;


    }
    /**
     * Zip two Streams, zipping against the underlying futures of both Streams
     * Placeholders (Futures) will be populated immediately in the new zipped Stream and results
     * will be populated asyncrhonously
     *
     * @param other  Another FutureStream to zip Futures with
     * @return New Sequence of CompletableFutures
     */
    default <R> SimpleReactStream<Tuple2<U,R>> zip(SimpleReactStream<R> other) {
        Seq seq = Seq.seq(getLastActive().stream()).zip(Seq.seq(other.getLastActive().stream()));
        Seq<Tuple2<CompletableFuture<U>,CompletableFuture<R>>> withType = (Seq<Tuple2<CompletableFuture<U>,CompletableFuture<R>>>)seq;
        SimpleReactStream futureStream =  fromStreamOfFutures((Stream)withType.map(t ->CompletableFuture.allOf(t.v1,t.v2).thenApply(v -> Tuple.tuple(t.v1.join(),t.v2.join())))
                );

        return (SimpleReactStream<Tuple2<U,R>>)futureStream;


    }

    /**
     * Zip this Stream with an index, but Zip based on the underlying tasks, not completed results.
     *
     * e.g.
     * two functions that return method name, but take varying lengths of time.
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
    default SimpleReactStream<Tuple2<U,Long>> zipWithIndex() {

        Seq seq = Seq.seq(getLastActive().stream().iterator()).zipWithIndex();
        Seq<Tuple2<CompletableFuture<U>,Long>> withType = (Seq<Tuple2<CompletableFuture<U>,Long>>)seq;
        SimpleReactStream futureStream =  fromStreamOfFutures((Stream)withType.map(t -> t.v1.thenApply(v ->
                            Tuple.tuple(t.v1.join(),t.v2))));
        return (SimpleReactStream<Tuple2<U,Long>>)futureStream;

    }
    /**
     * Return first Stream out of provided Streams that starts emitted results
     *
     * @param futureStreams Streams to race
     * @return First Stream to start emitting values
     */
    @SafeVarargs
    public static <U> SimpleReactStream<U> firstOf(SimpleReactStream<U>... futureStreams){
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
        EagerStreamWrapper lastActive = getLastActive();
        ListIterator<CompletableFuture> it = lastActive.list().listIterator();
        List<CompletableFuture> result = new ArrayList<>();
        while(it.hasPrevious())
            result.add(it.previous());

        EagerStreamWrapper limited = lastActive.withList( result);
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

    default SimpleReactStream<U> slice(long from, long to) {
        List noType = Seq.seq(getLastActive().stream()).slice(from, to)
                .collect(Collectors.toList());
        return fromListCompletableFuture(noType);
    }
    /**
     * Perform a limit operation on the underlying Stream of Futures
     * In contrast to EagerFutureStream#limit this removes entries basaed on their
     * start position
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
    default SimpleReactStream<U> limit(long maxSize) {

        EagerStreamWrapper lastActive = getLastActive();
        EagerStreamWrapper limited = lastActive.withList(lastActive.stream()
                .limit(maxSize).collect(Collectors.toList()));
        return this.withLastActive(limited);

    }
    /**
     * In contast to EagerFutureStream#skip skipFutures will skip the first n entries
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
    default SimpleReactStream<U> skip(long n) {
        EagerStreamWrapper lastActive = getLastActive();
        EagerStreamWrapper limited = lastActive.withList(lastActive.stream().skip(n)
                .collect(Collectors.toList()));
        return this.withLastActive(limited);
    }
    /**
     * Return a Stream with the same values as this Stream, but with all values omitted until the provided stream starts emitting values.
     * Provided Stream ends the stream of values from this stream.
     *
     * @param s Stream that will start the emission of values from this stream
     * @return Next stage in the Stream but with all values skipped until the provided Stream starts emitting
     */
    default<T>  Seq<U> skipUntil(SimpleReactStream<T> s) {
        return EagerFutureStreamFunctions.skipUntil(this, s);
    }
    /**
     * Return a Stream with the same values, but will stop emitting values once the provided Stream starts to emit values.
     * e.g. if the provided Stream is asynchronously refreshing state from some remote store, this stream can proceed until
     * the provided Stream succeeds in retrieving data.
     *
     * @param s Stream that will stop the emission of values from this stream
     * @return Next stage in the Stream but will only emit values until provided Stream starts emitting values
     */
    default<T>  Seq<U> takeUntil(SimpleReactStream<T> s) {
        return EagerFutureStreamFunctions.takeUntil(this, s);
    }

    /**
     * Cancel the CompletableFutures in this stage of the stream
     */
    default void cancel(){
        this.streamCompletableFutures().forEach(next-> next.cancel(true));
    }

    default ListX<SimpleReactStream<U>> copySimpleReactStream(final int times){

        return (ListX)StreamUtils.toBufferingCopier(getLastActive().stream().iterator(), times)
                .stream()
                .map(it->StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false))
                .<BaseSimpleReactStream<U>>map(fs-> this.getSimpleReact().construct(fs))
                .toListX();
    }
    /*
     * React to new events with the supplied function on the supplied Executor
     *
     *	@param fn Apply to incoming events
     *	@param service Service to execute function on
     *	@return next stage in the Stream
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	default <R> SimpleReactStream<R> then(final Function<? super U,? extends R> fn, Executor service) {



        return (SimpleReactStream<R>) this.withLastActive(
                getLastActive().stream(s -> s.<CompletableFuture>map(
                        (ft) -> ft.thenApplyAsync(SimpleReactStream.<U,R>handleExceptions(fn),getTaskExecutor()))));
    }
    /*
     * React to new events with the supplied function on the supplied Executor
     *
     *	@param fn Apply to incoming events
     *	@param service Service to execute function on
     *	@return next stage in the Stream
     */
    default <R> SimpleReactStream<R> thenSync(final Function<? super U,? extends R> fn) {


        return (SimpleReactStream<R>) this.withLastActive(
                getLastActive().stream(s -> s.<CompletableFuture>map(
                        (ft) -> ft.thenApply(SimpleReactStream.<U,R>handleExceptions(fn)))));
    }


    /**
     * @param collector
     *            to perform aggregation / reduction operation on the results
     *            from active stage (e.g. to Collect into a List or String)
     * @param fn
     *            Function that receives the results of all currently active
     *            tasks as input
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default <R1,R2> SimpleReactStream<R2> allOf(final Collector<? super U, ?, R1> collector,
            final Function<? super R1,? extends R2> fn) {
       
        CompletableFuture[] array = lastActiveArray(getLastActive());
        CompletableFuture cf = CompletableFuture.allOf(array);
        Function<Exception, R2> f = (Exception e) -> {
            BlockingStreamHelper.capture(e,getErrorHandler());
            return BlockingStreamHelper.block(this,Collectors.toList(),
                    new EagerStreamWrapper(Stream.of(array)));
        };
        CompletableFuture onFail = cf.exceptionally(f);
        CompletableFuture onSuccess = onFail.thenApplyAsync((result) -> {
            return new StageWithResults(this.getTaskExecutor(),null, result).submit(() -> (R2)fn
                    .apply(BlockingStreamHelper.aggregateResultsCompletable(collector, Stream.of(array)
                            .collect(Collectors.toList()),getErrorHandler())));
        }, getTaskExecutor());
        return (SimpleReactStream<R2>) withLastActive(new EagerStreamWrapper(onSuccess));

    }
    /**
     * React to the completion of any of the events in the previous stage. Will not work reliably with Streams
     * where filter has been applied in earlier stages. (As Filter completes the Stream for events that are filtered out, they
     * potentially shortcircuit the completion of the stage).
     *
     * @param fn Function to apply when any of the previous events complete
     * @return Next stage in the stream
     */
    default <R> SimpleReactStream<R> anyOf(
            final Function<? super U, ? extends R> fn) {
        CompletableFuture[] array = lastActiveArray(getLastActive());
        CompletableFuture cf = CompletableFuture.anyOf(array);
        CompletableFuture onSuccess = cf.thenApplyAsync(fn,getTaskExecutor());

        return (SimpleReactStream<R>) withLastActive(new EagerStreamWrapper(onSuccess));

    }




    @SuppressWarnings("rawtypes")
    static CompletableFuture[] lastActiveArray(EagerStreamWrapper lastActive) {
        return lastActive.list().toArray(new CompletableFuture[0]);
    }

    /**
     * Will execute this phase on the RetryExecutor (default or user supplied).
     * The RetryExecutor can be changed via withRetrier.
     *
     * This stage will be retried according to the configured rules. See
     * https://github.com/nurkiewicz/async-retry for detailed advice on how to
     * conifugre
     *
     *
     * @param fn
     *            Function that will be executed and retried on failure
     * @return Next Stage in the Strea,
     */
    @SuppressWarnings("unchecked")
    default <R> SimpleReactStream<R> retry(final Function<? super U,? extends R> fn) {
        Function<Stream<CompletableFuture>,Stream<CompletableFuture>>  mapper = stream -> stream.map(
                (ft) -> ft.thenApplyAsync(res ->
                getRetrier().getWithRetry( ()->SimpleReactStream.<U,R>handleExceptions(fn).apply((U)res)).join(),getTaskExecutor() ));

        return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(mapper));
    }



    default <R> SimpleReactStream<R> fromStream(Stream<R> stream) {


        return (SimpleReactStream<R>) this.withLastActive(getLastActive()
                .withNewStream(stream.map(CompletableFuture::completedFuture),this.getSimpleReact()));
    }


    /**
     * Construct a SimpleReactStream from provided Stream of CompletableFutures
     *
     * @param stream JDK Stream to construct new SimpleReactStream from
     * @return SimpleReactStream
     */
    default <R> SimpleReactStream<R> fromStreamOfFutures(
            Stream<CompletableFuture<R>> stream) {
        Stream noType = stream;
        return (SimpleReactStream<R>) this.withLastActive(getLastActive()
                .withNewStream(noType,this.getSimpleReact()));
    }
    default <R> SimpleReactStream<R> fromStreamCompletableFutureReplace(
            Stream<CompletableFuture<R>> stream) {
        Stream noType = stream;
        return (SimpleReactStream<R>) this.withLastActive(getLastActive()
                .withStream(noType));
    }

    default <R> SimpleReactStream<R> fromListCompletableFuture(
            List<CompletableFuture<R>> list) {
        List noType = list;
        return (SimpleReactStream<R>) this.withLastActive(getLastActive()
                .withList(noType));
    }

    /**
     * React <b>then</b>
     *
     *
     *
     * Unlike 'with' this method is fluent, and returns another Stage Builder
     * that can represent the next stage in the dataflow.
     *
     * <pre>
     * {@code
        new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
                .then((it) -> it * 100)
                .then((it) -> "*" + it)

                }
    </pre>
     *
     * React then allows event reactors to be chained. Unlike React with, which
     * returns a collection of Future references, React then is a fluent
     * interface that returns the React builder - allowing further reactors to
     * be added to the chain.
     *
     * React then does not block.
     *
     * React with can be called after React then which gives access to the full
     * CompleteableFuture api. CompleteableFutures can be passed back into
     * SimpleReact via SimpleReact.react(streamOfCompleteableFutures);
     *
     * See this blog post for examples of what can be achieved via
     * CompleteableFuture :- <a href=
     * 'http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.html'>http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.htm
     * l </a>
     *
     * @param fn
     *            Function to be applied to the results of the currently active
     *            event tasks
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    @SuppressWarnings("unchecked")
    default  <R> SimpleReactStream<R> then(final Function<? super U,? extends R> fn) {
        if(!this.isAsync())
            return thenSync(fn);
        Function<Stream<CompletableFuture>,Stream<CompletableFuture>> streamMapper = s ->s.<CompletableFuture>map(ft ->
        	ft.thenApplyAsync(SimpleReactStream.<U,R>handleExceptions(fn),getTaskExecutor()));
        return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
    }



    
    /**
     * Peek asynchronously at the results in the current stage. Current results
     * are passed through to the next stage.
     *
     * @param consumer
     *            That will recieve current results
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    default SimpleReactStream<U> peek(final Consumer<? super U> consumer) {
        if(!isAsync())
            return peekSync(consumer);
        return (SimpleReactStream<U>) then((t) -> {
            consumer.accept(t);
            return (U) t;
        });
    }



    /**
     * Synchronous peek operator
     *
     * @param consumer Peek consumer
     * @return Next stage
     */
    default SimpleReactStream<U> peekSync(final Consumer<? super U> consumer) {
        return (SimpleReactStream<U>) thenSync((t) -> {
            consumer.accept(t);
            return (U) t;
        });
    }

    static <U,R> Function<U, R> handleExceptions(Function<? super U,? extends R> fn) {
        return (input) -> {
            try {
                return fn.apply(input);
            } catch (Throwable t) {
                if(t instanceof AbortRetryException)//special case for retry
                    throw t;
                throw new SimpleReactFailedStageException(input, t);

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
     * In this example the result of the flatMapCompletableFuture is 'flattened' to the raw integer values
     *
     *
     * @param flatFn flatMap function
     * @return Flatten Stream with flatFn applied
     */
    @Override
    default <R> SimpleReactStream<R> flatMapToCompletableFuture(
            Function<? super U,  CompletableFuture<? extends R>> flatFn) {
        if(!this.isAsync())
            return flatMapToCompletableFutureSync(flatFn);
        Function<Stream<CompletableFuture>,Stream<CompletableFuture>> streamMapper = s ->(Stream)s.<CompletableFuture>map(ft ->
            ft.thenComposeAsync(SimpleReactStream.handleExceptions(flatFn),getTaskExecutor()));
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
     * In this example the result of the flatMapCompletableFuture is 'flattened' to the raw integer values
     *
     *
     * @param flatFn flatMap function
     * @return Flatten Stream with flatFn applied
     */
    @Override
    default <R> SimpleReactStream<R> flatMapToCompletableFutureSync(
            Function<? super U, CompletableFuture<? extends R>> flatFn) {

        Function<Stream<CompletableFuture>,Stream<CompletableFuture>> streamMapper = s ->(Stream)s.<CompletableFuture>map(ft -> ft.thenCompose(SimpleReactStream.handleExceptions(flatFn)));
        return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
    }

    /**
     * Allows aggregate values in a Stream to be flatten into a single Stream.
     * flatMap functions turn each aggregate value into it's own Stream, and SimpleReact aggregates those Streams
     * into a single flattened stream
     *
     * @param flatFn Function that coverts a value (e.g. a Collection) into a Stream
     * @return SimpleReactStream
     */
    default <R> SimpleReactStream<R> flatMap(
            Function<? super U, ? extends Stream<? extends R>> flatFn) {

        //need to pass in a builder in the constructor and build using it
        return (SimpleReactStream)getSimpleReact().construct( Stream.of()).withSubscription(getSubscription())
                            .withQueueFactory((QueueFactory<Object>) getQueueFactory())
                .fromStream(
                        toQueue()
                                .stream(getSubscription())
                                .flatMap(flatFn));
    }






    /**
     *
     * React <b>with</b>
     *
     * Asynchronously apply the function supplied to the currently active event
     * tasks in the dataflow.
     *
     * While most methods in this class are fluent, and return a reference to a
     * SimpleReact Stage builder, this method can be used this method to access
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
     *            Function to be applied to the results of the currently active
     *            event tasks
     * @return A list of CompletableFutures which will contain the result of the
     *         application of the supplied function
     */
    @SuppressWarnings("unchecked")
    default <R> List<CompletableFuture<R>> with(final Function<? super U,? extends R> fn) {

        return getLastActive()
                .stream()
                .map(future -> (CompletableFuture<R>) future.thenApplyAsync(fn,
                        getTaskExecutor())).collect(Collectors.toList());
    }



    /**
     * Removes elements that do not match the supplied predicate from the
     * dataflow
     *
     * @param p
     *            Predicate that will be used to filter elements from the
     *            dataflow
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    @SuppressWarnings("unchecked")
    default SimpleReactStream<U> filter(final Predicate<? super U> p) {
        if(!isAsync())
            return filterSync(p);
    Function<Stream<CompletableFuture>,Stream<CompletableFuture>> fn = s -> s.map(ft -> ft.thenApplyAsync((in) -> {
            if (!p.test((U) in)) {
                throw new FilteredExecutionPathException();
            }
            return in;
        }));
        return (SimpleReactStream<U>) this.withLastActive(getLastActive()
                .stream(fn));

    }
    /**
     * Synchronous filtering operation
     *
     * Removes elements that do not match the supplied predicate from the
     * dataflow
     *
     * @param p
     *            Predicate that will be used to filter elements from the
     *            dataflow
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    default SimpleReactStream<U> filterSync(final Predicate<? super U> p) {
        Function<Stream<CompletableFuture>,Stream<CompletableFuture>> fn = s -> s.map(ft -> ft.thenApply((in) -> {
                if (!p.test((U) in)) {
                    throw new FilteredExecutionPathException();
                }
                return in;
            }));
            return (SimpleReactStream<U>) this.withLastActive(getLastActive()
                    .stream(fn));

        }
    /**
     * @return A Stream of CompletableFutures that represent this stage in the
     *         dataflow
     */
    @SuppressWarnings({ "unchecked" })
    default <T> Stream<CompletableFuture<T>> streamCompletableFutures() {
        Stream s = this.getLastActive().stream();
        return s;

    }

    /*
      * Merge two simple-react Streams, by merging the Stream of underlying
     * futures - not suitable for merging infinite Streams - use
     * see LazyFutureStream#switchOnNext for infinite Streams
     *
     * <pre>
     * {@code
     * List<String> result = 	SimpleReactStream.of(1,2,3)
     * 											 .merge(LazyFutureStream.of(100,200,300))
                                                  .map(it ->it+"!!")
                                                  .toList();
        assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
     *
     * }
     * </pre>
     *
     * @param s Stream to merge
     *
     * @return Next stage in stream
     *
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#merge(com.aol.simple.
     * react.stream.traits.SimpleReactStream)
     */

    @SuppressWarnings({ "unchecked", "rawtypes" })
    default SimpleReactStream<U> merge(SimpleReactStream<U>... s) {

        List merged = Stream.concat(Stream.of(this),Stream.of(s))
                .map(stream -> ((SimpleReactStream)stream).getLastActive().list())
                .flatMap(Collection::stream).collect(Collectors.toList());
        return (SimpleReactStream<U>) this.withLastActive(new EagerStreamWrapper(merged));
    }
    /**
     * React and <b>block</b> with <b>breakout</b>
     *
     * Sometimes you may not need to block until all the work is complete, one
     * result or a subset may be enough. To faciliate this, block can accept a
     * Predicate functional interface that will allow SimpleReact to stop
     * blocking the current thread when the Predicate has been fulfilled. E.g.
     *
     * <code>
        List&lt;String&gt; strings = new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
                .then(it -&gt; it * 100)
                .then(it -&gt; "*" + it)
                .block(status -&gt; status.getCompleted()&gt;1);
      </code>
     *
     * In this example the current thread will unblock once more than one result
     * has been returned.
     *
     * @param breakout
     *            Predicate that determines whether the block should be
     *            continued or removed
     * @return List of Completed results of currently active stage at full
     *         completion point or when breakout triggered (which ever comes
     *         first). throws InterruptedException,ExecutionException
     */
    @ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
    default ListX<U> block(final Predicate<Status> breakout) {
        return new Blocker<U>(getLastActive().list(), getErrorHandler()).block(breakout);
    }
    /**
     * @param collector
     *            to perform aggregation / reduction operation on the results
     *            (e.g. to Collect into a List or String)
     * @param breakout
     *            Predicate that determines whether the block should be
     *            continued or removed
     * @return Completed results of currently active stage at full completion
     *         point or when breakout triggered (which ever comes first), in
     *         aggregated in form determined by collector throws
     *         InterruptedException,ExecutionException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
    default  <R> R block(final Collector collector,
            final Predicate<Status> breakout) {
        return (R) block(breakout).stream().collect(collector);
    }
    /**
     * Merge this reactive dataflow with another - recommended for merging
     * different types. To merge flows of the same type the instance method
     * merge is more appropriate.
     *
     * @param s1
     *            Reactive stage builder to merge
     * @param s2
     *            Reactive stage builder to merge
     * @return Merged dataflow
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <R> SimpleReactStream<R> merge(SimpleReactStream s1, SimpleReactStream s2) {
        List merged = Stream.of(s1.getLastActive().list(), s2.getLastActive().list())
                .flatMap(Collection::stream).collect(Collectors.toList());
        return (SimpleReactStream<R>) s1.withLastActive(new EagerStreamWrapper(merged));
    }

    /**
     * React <b>onFail</b>
     *
     *
     * Define a function that can be used to recover from exceptions during the
     * preceeding stage of the dataflow. e.g.
     *
     *
     *
     * onFail allows disaster recovery for each task (a separate onFail should
     * be configured for each react phase that can fail). E.g. if reading data
     * from an external service fails, but default value is acceptable - onFail
     * is a suitable mechanism to set the default value. Asynchronously apply
     * the function supplied to the currently active event tasks in the
     * dataflow.
     *
     * <pre>
      {@code
    List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 100, () -> 2, () -> 3)
                    .then(it -> {
                        if (it == 100)
                            throw new RuntimeException("boo!");

                        return it;
                    })
                    .onFail(e -> 1)
                    .then(it -> "*" + it)
                    .block();



      }

          </pre>
     *
     *
     * In this example onFail recovers from the RuntimeException thrown when the
     * input to the first 'then' stage is 100.
     *
     * @param fn
     *            Recovery function, the exception is input, and the recovery
     *            value is output
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default SimpleReactStream<U> onFail(final Function<? super SimpleReactFailedStageException,? extends U> fn) {
        return onFail(Throwable.class,fn);
    }

    /**
     * Recover for a particular class of exceptions only. Chain onFail methods from specific Exception classes
     * to general, as Exceptions will be caught and handled in order.
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
     * @param exceptionClass Class of exceptions to recover from
     * @param fn Recovery function
     * @return recovery value
     */
    default SimpleReactStream<U> onFail(Class<? extends Throwable> exceptionClass, 
                        final Function<? super SimpleReactFailedStageException, ? extends U> fn){

        Function<Stream<CompletableFuture>,Stream<CompletableFuture>> mapper = s -> s.map((ft) -> ft.exceptionally((t) -> {
            if (t instanceof FilteredExecutionPathException)
                throw (FilteredExecutionPathException) t;
            Throwable throwable =(Throwable)t;
            if(t instanceof CompletionException)
                throwable = ((Exception)t).getCause();

            SimpleReactFailedStageException simpleReactException = assureSimpleReactException( throwable);//exceptions from initial supplier won't be wrapper in SimpleReactFailedStageException
            if(exceptionClass.isAssignableFrom(simpleReactException.getCause().getClass()))
                return ((Function) fn).apply(simpleReactException);
            throw simpleReactException;

        }));
        return (SimpleReactStream<U>) this.withLastActive(getLastActive()
                .stream(mapper));
    }


    static SimpleReactFailedStageException assureSimpleReactException(
            Throwable throwable){
        if(throwable instanceof SimpleReactFailedStageException)
            return (SimpleReactFailedStageException)throwable;
        return new SimpleReactFailedStageException(null,(throwable));
    }
    /**
     * React <b>capture</b>
     *
     * While onFail is used for disaster recovery (when it is possible to
     * recover) - capture is used to capture those occasions where the full
     * pipeline has failed and is unrecoverable.
     *
     * <pre>
        {@code
        List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
            .then(it -> it * 100)
            .then(it -> {
                if (it == 100)
                    throw new RuntimeException("boo!");

                return it;
            })
            .onFail(e -> 1)
            .then(it -> "*" + it)
            .then(it -> {

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
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    @SuppressWarnings("unchecked")
    default SimpleReactStream<U> capture(final Consumer<? extends Throwable> errorHandler) {
        return (SimpleReactStream)this.withErrorHandler(Optional
                .of((Consumer<Throwable>) errorHandler));
    }



    /**
     * React and <b>allOf</b>
     *
     * allOf is a non-blocking equivalent of block. The current thread is not
     * impacted by the calculations, but the reactive chain does not continue
     * until all currently alloted tasks complete. The allOf task is then
     * provided with a list of the results from the previous tasks in the chain.
     *
     * <pre>
      {@code
      boolean blocked[] = {false};
        new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)

                .then(it -> {
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
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    @SuppressWarnings("unchecked")
    default <R> SimpleReactStream<R> allOf(final Function<? super List<U>,? extends R> fn) {

        return (SimpleReactStream<R>) allOf(Collectors.<U>toList(),  fn);

    }

    /**
     * Convert between an Lazy and Eager future stream,
     * can be used to take advantages of each approach during a single Stream
     *
     * @return An EagerFutureStream from this LazyFutureStream, will use the same executors
     */
    default LazyFutureStream<U> convertToLazyStream(){
        return new LazyReact(getTaskExecutor()).withRetrier(getRetrier())
                .fromStreamFutures((Stream)getLastActive().stream());
    }








    /*
     * Execute subsequent stages on the completing thread (until async called)
     * 10X faster than async execution.
     * Use async for blocking IO or distributing work across threads or cores.
     * Switch to sync for non-blocking tasks when desired thread utlisation reached
     *
     *	@return Version of FutureStream that will use sync CompletableFuture methods
     *
     */
    default SimpleReactStream<U> sync(){
        return this.withAsync(false);
    }

    /*
     * Execute subsequent stages by submission to an Executor for async execution
     * 10X slower than sync execution.
     * Use async for blocking IO or distributing work across threads or cores.
     * Switch to sync for non-blocking tasks when desired thread utlisation reached
     *
     *
     *	@return Version of FutureStream that will use async CompletableFuture methods
     *
     */
    default SimpleReactStream<U> async(){
        return this.withAsync(true);
    }

    /**
     *
     * flatMap / bind implementation that returns the correct type (SimpleReactStream)
     *
     * @param stream Stream to flatMap
     * @param flatFn flatMap function
     * @return
     */
    static <U,R> SimpleReactStream<R> bind(SimpleReactStream<U> stream,
            Function< U, BaseSimpleReactStream<R>> flatFn) {

        return join(stream.then(flatFn));

    }

    /**
     * flatten nested SimpleReactStreams
     *
     * @param stream Stream to flatten
     * @return flattened Stream
     */
    static <U,R> SimpleReactStream<R> join(SimpleReactStream<BaseSimpleReactStream<U>> stream){
        Queue queue =  stream.getQueueFactory().build();
        stream.then(it -> it.sync().then(queue::offer)).allOf(it ->queue.close());
         return stream.fromStream(queue.stream(stream.getSubscription()));

    }




}