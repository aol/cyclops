package com.aol.cyclops2.types.futurestream;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import cyclops.async.LazyReact;
import cyclops.companion.Streams;
import cyclops.async.adapters.QueueFactory;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.internal.react.async.future.FastFuture;
import com.aol.cyclops2.internal.react.async.future.PipelineBuilder;
import com.aol.cyclops2.internal.react.exceptions.FilteredExecutionPathException;
import com.aol.cyclops2.internal.react.stream.LazyStreamWrapper;
import com.aol.cyclops2.react.SimpleReactFailedStageException;
import com.aol.cyclops2.react.async.subscription.Continueable;


public interface LazySimpleReactStream<U> extends BlockingStream<U>, ConfigurableStream<U, FastFuture<U>>, ToQueue<U>, BaseSimpleReactStream<U> {

    @Override
    LazyReact getSimpleReact();

    @Override
    LazySimpleReactStream<U> withTaskExecutor(Executor e);


    @Override
    LazySimpleReactStream<U> withQueueFactory(QueueFactory<U> queue);

    @Override
    LazySimpleReactStream<U> withErrorHandler(Optional<Consumer<Throwable>> errorHandler);

    @Override
    LazySimpleReactStream<U> withSubscription(Continueable sub);

    @Override
    LazySimpleReactStream<U> withAsync(boolean b);

    @Override
    Continueable getSubscription();

    <R> LazySimpleReactStream<R> withLastActive(LazyStreamWrapper<R> streamWrapper);

    @Override
    abstract LazyStreamWrapper<U> getLastActive();

    /* 
     * React toNested new events with the supplied function on the supplied Executor
     * 
     *	@param fn Apply toNested incoming events
     *	@param service Service toNested execute function on
     *	@return next stage in the Stream
     */
    @Override
    default <R> LazySimpleReactStream<R> then(final Function<? super U, ? extends R> fn, final Executor service) {

        return this.withLastActive(getLastActive().operation((ft) -> ft.thenApplyAsync(LazySimpleReactStream.<U, R> handleExceptions(fn), service)));
    }

    /* 
     * React toNested new events with the supplied function on the supplied Executor
     * 
     *	@param fn Apply toNested incoming events
     *	@param service Service toNested execute function on
     *	@return next stage in the Stream
     */
    @Override
    @SuppressWarnings("unchecked")
    default <R> LazySimpleReactStream<R> thenSync(final Function<? super U, ? extends R> fn) {

        return this.withLastActive(getLastActive().operation((ft) -> ft.thenApply(LazySimpleReactStream.<U, R> handleExceptions(fn))));
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
     * returns a collection of Future references, React transform is a fluent
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
    default <R> LazySimpleReactStream<R> then(final Function<? super U, ? extends R> fn) {
        if (!isAsync())
            return thenSync(fn);
        final Function<PipelineBuilder, PipelineBuilder> streamMapper = ft -> ft.thenApplyAsync(LazySimpleReactStream.<U, R> handleExceptions(fn),
                                                                                                getTaskExecutor());
        return (LazySimpleReactStream<R>) this.withLastActive(getLastActive().operation(streamMapper));
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
    default LazySimpleReactStream<U> peek(final Consumer<? super U> consumer) {
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
    default LazySimpleReactStream<U> peekSync(final Consumer<? super U> consumer) {
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
    default <R> LazySimpleReactStream<R> flatMapToCompletableFuture(final Function<? super U, CompletableFuture<? extends R>> flatFn) {
        if (!isAsync())
            return flatMapToCompletableFutureSync(flatFn);
        final Function<PipelineBuilder, PipelineBuilder> streamMapper = ft -> ft.thenComposeAsync(LazySimpleReactStream.handleExceptions(flatFn),
                                                                                                  getTaskExecutor());
        return this.withLastActive(getLastActive().operation(streamMapper));
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
    default <R> LazySimpleReactStream<R> flatMapToCompletableFutureSync(final Function<? super U, CompletableFuture<? extends R>> flatFn) {

        final Function<PipelineBuilder, PipelineBuilder> streamMapper = ft -> ft.thenCompose(LazySimpleReactStream.handleExceptions(flatFn));
        return this.withLastActive(getLastActive().operation(streamMapper));
    }

    /**
     * Allows aggregate values in a Stream toNested be flatten into a single Stream.
     * flatMap function turn each aggregate value into it's own Stream, and SimpleReact aggregates those Streams
     * into a single flattened reactiveStream
     * 
     * @param flatFn Function that coverts a value (e.g. a Collection) into a Stream
     * @return SimpleReactStream
     */
    @Override
    default <R> LazySimpleReactStream<R> flatMap(final Function<? super U, ? extends Stream<? extends R>> flatFn) {

        //need toNested pass in a builder in the constructor and build using it
        return (LazySimpleReactStream) getSimpleReact().construct(Stream.of())
                                                       .withSubscription(getSubscription())
                                                       .withQueueFactory((QueueFactory<Object>) getQueueFactory())
                                                       .fromStream(toQueue().stream(getSubscription())
                                                                            .flatMap(flatFn));
    }

    default ListX<BaseSimpleReactStream<U>> copySimpleReactStream(final int times) {

        return Streams.toBufferingCopier(iterator(), times)
                          .stream()
                          .map(it -> StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator) it, Spliterator.ORDERED), false))
                          .<BaseSimpleReactStream<U>> map(fs -> (BaseSimpleReactStream) this.getSimpleReact()
                                                                                            .construct((Stream) fs))
                          .toListX();
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
    default LazySimpleReactStream<U> filter(final Predicate<? super U> p) {

        if (!isAsync())
            return filterSync(p);
        final Function<PipelineBuilder, PipelineBuilder> fn = ft -> ft.thenApplyAsync((in) -> {
            if (!p.test((U) in)) {
                throw new FilteredExecutionPathException();
            }
            return in;
        } , getTaskExecutor());
        return this.withLastActive(getLastActive().operation(fn));

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
    default LazySimpleReactStream<U> filterSync(final Predicate<? super U> p) {
        final Function<PipelineBuilder, PipelineBuilder> fn = ft -> ft.thenApply((in) -> {
            if (!p.test((U) in)) {
                throw new FilteredExecutionPathException();
            }
            return in;
        });
        return this.withLastActive(getLastActive().operation(fn));

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
     * is a suitable mechanism toNested set the default value. Asynchronously apply
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
     * input toNested the first 'transform' stage is 100.
     * 
     * @param fn
     *            Recovery function, the exception is input, and the recovery
     *            value is emitted
     * @return A new builder object that can be used toNested define the next stage in
     *         the dataflow
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default LazySimpleReactStream<U> onFail(final Function<? super SimpleReactFailedStageException, ? extends U> fn) {
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
    default LazySimpleReactStream<U> onFail(final Class<? extends Throwable> exceptionClass,
            final Function<? super SimpleReactFailedStageException, ? extends U> fn) {

        final Function<PipelineBuilder, PipelineBuilder> mapper = (ft) -> ft.exceptionally((t) -> {
            if (t instanceof FilteredExecutionPathException)
                throw (FilteredExecutionPathException) t;
            Throwable throwable = t;
            if (t instanceof CompletionException)
                throwable = ((Exception) t).getCause();

            final SimpleReactFailedStageException simpleReactException = assureSimpleReactException(throwable);//exceptions from initial supplier won't be wrapper in SimpleReactFailedStageException
            if (exceptionClass.isAssignableFrom(simpleReactException.getCause()
                                                                    .getClass()))
                return fn.apply(simpleReactException);
            throw simpleReactException;

        });
        return this.withLastActive(getLastActive().operation(mapper));
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
    default LazySimpleReactStream<U> capture(final Consumer<Throwable> errorHandler) {
        return this.withErrorHandler(Optional.of(errorHandler));
    }

}
