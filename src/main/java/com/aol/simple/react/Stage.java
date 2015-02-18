package com.aol.simple.react;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import org.jooq.lambda.Seq;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.blockers.Blocker;
import com.aol.simple.react.collectors.ReactCollector;
import com.aol.simple.react.collectors.lazy.LazyCollector;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.exceptions.ThrowsSoftened;
import com.aol.simple.react.extractors.Extractor;
import com.aol.simple.react.extractors.Extractors;
import com.aol.simple.react.waiter.DoNothingWaiter;
import com.nurkiewicz.asyncretry.RetryExecutor;


/**
 * 
 * An Immutable Builder Class that represents a stage in a SimpleReact Dataflow.
 * Chain stages together to build complext reactive dataflows.
 * 
 * Access the underlying CompletableFutures via the 'with' method. Return to
 * using JDK Collections and (parrellel) Streams via 'allOf' or 'block'
 * 
 * 
 * 
 * @author johnmcclean
 *
 * @param <U>
 *            Return parameter for this stage
 */

//lombok annotations to aid Immutability (Wither and AllArgsConstructor)
@Wither(value=AccessLevel.PACKAGE)
@AllArgsConstructor
@Slf4j
public class Stage<U> implements Seq<U>{

	private final ExceptionSoftener exceptionSoftener = ExceptionSoftener.singleton.factory.getInstance();
	@Getter(AccessLevel.PACKAGE)
	@Wither(value=AccessLevel.PUBLIC)
	private final ExecutorService taskExecutor;
	@Wither(value=AccessLevel.PUBLIC)
	private final RetryExecutor retrier;
	
	private final Optional<Consumer<Throwable>> errorHandler;

	private final StreamWrapper lastActive;
	private final boolean eager;
	@Wither(value=AccessLevel.PUBLIC)
	private final Consumer<CompletableFuture> waitStrategy;
	private final LazyResultConsumer<U> lazyCollector;
	
	
	
	

	/**
	 * 
	 * Construct a SimpleReact stage - this acts as a fluent SimpleReact builder
	 * 
	 * @param stream
	 *            Stream that will generate the events that will be reacted to.
	 * @param executor
	 *            The next stage's tasks will be submitted to this executor
	 */
	Stage(final Stream<CompletableFuture<U>> stream,
			final ExecutorService executor,final RetryExecutor retrier, final boolean eager) {

		this.taskExecutor = executor;
		Stream s = stream;
		this.lastActive = new StreamWrapper(s,Optional.ofNullable(eager).orElse(true));
		this.errorHandler = Optional.of( (e)-> log.error(e.getMessage(),e));
		this.eager = eager;
		this.retrier=retrier;
		this.waitStrategy = new DoNothingWaiter();
		this.lazyCollector = new LazyCollector<>();
	}
	
	/**
	 * This method allows the SimpleReact ExecutorService to be reused by JDK parallel streams.
	 * This offers less control over blocking than raw submit, with the parameterless block() method called.
	 * 
	 * @param fn Function that contains parallelStream code to be executed by the SimpleReact ForkJoinPool (if configured)
	 */
	public <R> R submitAndBlock(Function <List<U>,R> fn){
		return collectResults().block().submit (r -> fn.apply(r));
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
	 * <code>
	 	List&lt;CompletableFuture&lt;Integer&gt;&gt; futures = new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				.with((it) -&gt; it * 100);
			</code>
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
	public <R> List<CompletableFuture<R>> with(
			final Function<U,R> fn) {

		return lastActive
				.stream()
				.map(future -> (CompletableFuture<R>) future.thenApplyAsync(fn,
						taskExecutor)).collect(Collectors.toList());
	}

	/**
	 * React <b>then</b>
	 * 
	 * 
	 * 
	 * Unlike 'with' this method is fluent, and returns another Stage Builder
	 * that can represent the next stage in the dataflow.
	 * 
	 * <code>
	  new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				.then((it) -&gt; it * 100)
				.then((it) -&gt; "*" + it)
	</code>
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
	 * CompleteableFuture :- <a href='http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.html'>http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.html </a>
	 * 
	 * @param fn
	 *            Function to be applied to the results of the currently active
	 *            event tasks
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings("unchecked")
	public <R> Stage<R> then(final Function<U, R> fn) {
		return (Stage<R>) this.withLastActive( lastActive.permutate(lastActive.stream()
				.map((ft) -> ft.thenApplyAsync(fn, taskExecutor))
				,Collectors.toList()));
	}
	
	public <R> Stage<R> flatMap(Function<? super U,? extends Stream<? extends R>> flatFn) {
		Queue q = new Queue();
		Stage flattened = SimpleReact.builder().eager(eager).executor(taskExecutor).retrier(retrier).build().fromStream(q.stream());
		if(!eager){
			allOf(it -> q.fromStream(flatFn.apply((U)it.get(0)))).run(new ForkJoinPool(1));
		}else{
			allOf(it -> q.fromStream(flatFn.apply((U)it.get(0))));
		}
						
		
		return flattened;
		
	}

	
	
	@SuppressWarnings("unchecked")
	public <R> Stage<R> retry(final Function<U, R> fn) {

		return (Stage<R>) this.withLastActive( lastActive.permutate(lastActive.stream()
				.map((ft) ->  ft.thenApplyAsync((res)->getSafe(retrier.getWithRetry(()->fn.apply((U) res))), taskExecutor))
				,Collectors.toList()));
	}
	/**
	 * Peek asynchronously at the results in the current stage. Current results are passed through to the next stage.
	 * 
	 * @param consumer That will recieve current results
	 * @return   A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	public Stage<U> peek(final Consumer<? super U> consumer) {
		return (Stage<U>)then( (t) -> {  consumer.accept(t); return (U)t;});
	}
	
	/**
	 * Removes elements that do not match the supplied predicate from the dataflow
	 * 
	 * @param p Predicate that will be used to filter elements from the dataflow
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings("unchecked")
	public  Stage<U> filter(final Predicate<? super U> p) {
		
		return (Stage<U>) this.withLastActive(lastActive.permutate(lastActive.stream().map( ft ->
			ft.thenApplyAsync( (in) -> {
				if(!p.test((U)in)) { 
					throw new FilteredExecutionPathException(); 
				}
				return in;
		})),Collectors.toList()));
				
	}
	
	
	
	/**
	 * @return A Stream of CompletableFutures that represent this stage in the dataflow
	 */
	@SuppressWarnings({ "unchecked"})
	public <T> Stream<CompletableFuture<T>> streamCompletableFutures(){
		Stream s  = this.lastActive.stream();
		return s;
		
	}

	
	/**
	 * Merge this reactive dataflow with another of the same type.
	 * To merge flows of different types use the static method merge and merge to a common ancestor.
	 * 
	 * @param s Reactive stage builder to merge with
	 * @return Merged dataflow
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public Stage<U> merge(Stage<U> s){
		List merged = Stream.of(this.lastActive.list(),s.lastActive.list())
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		return (Stage<U>)this.withLastActive(new StreamWrapper(merged));
	}
	
	/**
	 * Merge this reactive dataflow with another - recommended for merging different types.
	 *  To merge flows of the same type the instance method merge is more appropriate.
	 * 
	 * @param s1 Reactive stage builder to merge 
	 * @param s2 Reactive stage builder to merge 
	 * @return Merged dataflow
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public static <R> Stage<R> merge(Stage s1, Stage s2){
		List merged = Stream.of(s1.lastActive.list(),s2.lastActive.list())
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		return (Stage<R>)s1.withLastActive(new StreamWrapper(merged));
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
	 * <code>
	  	List&lt;String&gt; strings = new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 100, () -&gt; 2, () -&gt; 3)
					.then(it -&gt; {
						if (it == 100)
							throw new RuntimeException("boo!");
			
						return it;
					})
					.onFail(e -&gt; 1)
					.then(it -&gt; "*" + it)
					.block();
		  </code>
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
	public Stage<U> onFail(final Function<? extends Throwable, U> fn) {
		return (Stage<U>) this
				.withLastActive(lastActive.permutate(lastActive.stream()
						.map((ft) -> ft.exceptionally( (t) -> { 
							if(t instanceof FilteredExecutionPathException)
								throw (FilteredExecutionPathException)t;
							
							return	((Function)fn).apply(t); 
						}))
						,Collectors.toList()));
	}

	/**
	 * React <b>capture</b>
	 * 
	 * While onFail is used for disaster recovery (when it is possible to
	 * recover) - capture is used to capture those occasions where the full
	 * pipeline has failed and is unrecoverable.
	 * 
	 * <code>
	 * List&lt;String&gt; strings = new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
			.then(it -&gt; it * 100)
			.then(it -&gt; {
				if (it == 100)
					throw new RuntimeException("boo!");
	
				return it;
			})
			.onFail(e -&gt; 1)
			.then(it -&gt; "*" + it)
			.then(it -&gt; {
				
				if ("*200".equals(it))
					throw new RuntimeException("boo!");
	
				return it;
			})
			.capture(e -&gt; logger.error(e.getMessage(),e))
			.block();
		</code>
	 * 
	 * In this case, strings will only contain the two successful results (for
	 * ()-&gt;1 and ()-&gt;3), an exception for the chain starting from Supplier ()-&gt;2
	 * will be logged by capture. Capture will not capture the exception thrown
	 * when an Integer value of 100 is found, but will catch the exception when
	 * the String value "*200" is passed along the chain.
	 * 
	 * @param errorHandler
	 *            A consumer that recieves and deals with an unrecoverable error
	 *            in the dataflow
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings("unchecked")
	public Stage <U> capture(final Consumer<? extends Throwable> errorHandler) {
		return this.withErrorHandler(Optional
				.of((Consumer<Throwable>) errorHandler));
	}
	
	/**
	 * This provides a mechanism to collect all of the results of active tasks inside a dataflow stage.
	 * This can then be used to provide those results to a function. Inside that function client code can leverage
	 * JDK 8 parallel Streams that will be executed within the SimpleReact ExecutorService if that service is an instance
	 * of ForkJoinPool (the default setting). 
	 * 
	 * Example :
	 * <code>
	 * Integer result = new SimpleReact()
				.&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				.then((it) -&gt; { it * 200)
				.collectResults()
				.&lt;List&lt;Integer&gt;&gt;block()
				.submit( 
						it -&gt; it.orElse(new ArrayList())
								.parallelStream()
								.filter(f -&gt; f &gt; 300)
								.map(m -&gt; m - 5)
								.reduce(0, (acc, next) -&gt; acc + next));
								
	 * </code>
	 * 
	 * @return A builder that allows the blocking mechanism for results collection to be set
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ReactCollector<U> collectResults(){
		return new ReactCollector(this);
	}

	/**
	 * React and <b>block</b>
	 * 
	 * <code>
	 	List&lt;String&gt; strings = new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				.then((it) -&gt; it * 100)
				.then((it) -&gt; "*" + it)
				.block();
	  </code>
	 * 
	 * In this example, once the current thread of execution meets the React
	 * block method, it will block until all tasks have been completed. The
	 * result will be returned as a List. The Reactive tasks triggered by the
	 * Suppliers are non-blocking, and are not impacted by the block method
	 * until they are complete. Block, only blocks the current thread.
	 * 
	 * 
	 * @return Results of currently active stage aggregated in a List
	 * throws InterruptedException,ExecutionException
	 */
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public List<U> block() {
		return block(Collectors.toList(),lastActive);
	}
	
	/**
	 * @param collector to perform aggregation / reduction operation on the results (e.g. to Collect into a List or String)
	 * @return Results of currently active stage in aggregated in form determined by collector
	 * throws InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "unchecked","rawtypes"})
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public <R> R block(final Collector collector) {
		return (R)block(collector,lastActive);
	}
	
	/**
	 * Block until first result received
	 * 
	 * @return  first result.
	 * throws InterruptedException,ExecutionException
	 */
	
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public U first() {
		return blockAndExtract(Extractors.first(),status -> status.getCompleted() > 0);
	}
	
	/**
	 * Block until all results received.
	 * 
	 * @return  last result
	 * throws InterruptedException,ExecutionException
	 */
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public U last() {
		return blockAndExtract(Extractors.last());
	}
	
	/**
	 * Block until tasks complete and return a value determined by the extractor supplied.
	 * 
	 * @param extractor used to determine which value should be returned, recieves current collected input and extracts a return value
	 * @return Value determined by the supplied extractor
	 * throws InterruptedException,ExecutionException
	 */
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public <R> R blockAndExtract(@SuppressWarnings("rawtypes") final Extractor extractor) {
		return blockAndExtract(extractor, status -> false);
	}
	
	/**
	 *  Block until tasks complete, or breakout conditions met and return a value determined by the extractor supplied.
	 * 
	 * @param extractor used to determine which value should be returned, recieves current collected input and extracts a return value 
	 * @param breakout Predicate that determines whether the block should be
	 *            continued or removed
	 * @return Value determined by the supplied extractor
	 * throws InterruptedException,ExecutionException
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public <R> R blockAndExtract(final Extractor extractor,final Predicate<Status> breakout) {
		return (R)extractor.extract(block());
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
	 * @return List of Completed results of currently active stage at full completion
	 *         point or when breakout triggered (which ever comes first).
	 * throws InterruptedException,ExecutionException
	 */
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public  List<U> block(final Predicate<Status> breakout) {
		return new Blocker<U>(lastActive.list(), errorHandler).block(breakout);
	}
	
	/**
	 * @param collector to perform aggregation / reduction operation on the results (e.g. to Collect into a List or String)
	 * @param breakout  Predicate that determines whether the block should be
	 *            continued or removed
	 * @return Completed results of currently active stage at full completion
	 *         point or when breakout triggered (which ever comes first), in aggregated in form determined by collector
	 * throws InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "unchecked","rawtypes" })
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public <R> R block(final Collector collector,final Predicate<Status> breakout) {
		return (R)block(breakout).stream().collect(collector);
	}
	

	/**
	 * React and <b>allOf</b>
	 * 
	 * allOf is a non-blocking equivalent of block. The current thread is not
	 * impacted by the calculations, but the reactive chain does not continue
	 * until all currently alloted tasks complete. The allOf task is then
	 * provided with a list of the results from the previous tasks in the chain.
	 * 
	 * <code>
	  boolean blocked[] = {false};
		new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				
				.then(it -&gt; {
					try {
						Thread.sleep(50000);
					} catch (Exception e) {
						
					}
					blocked[0] =true;
					return 10;
				})
				.allOf( it -&gt; it.size());

		
		assertThat(blocked[0],is(false));
		</code>
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
	public <T,R> Stage<R> allOf(final Function<List<T>, R> fn) {

		
		
		return (Stage<R>)allOf(Collectors.toList(), (Function<R, U>)fn);

	}
	/**
	 * @param collector to perform aggregation / reduction operation on the results from active stage (e.g. to Collect into a List or String)
	 * @param fn  Function that receives the results of all currently active
	 *            tasks as input
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public <T,R> Stage<R> allOf(final Collector collector,final Function<T,R> fn) {
		CompletableFuture cf = CompletableFuture.allOf(
				lastActiveArray());  
		Function<Exception,T> f = (Exception e) -> {  capture(e); return block(Collectors.toList(),lastActive);};
		CompletableFuture onFail = cf.exceptionally(f);
		CompletableFuture onSuccess = onFail.thenApplyAsync((result) -> {
			return new StageWithResults(this,result).submit( () -> fn.apply(aggregateResults(collector, lastActive.stream().collect(Collectors.toList()))));
		}, taskExecutor);
		return (Stage<R>) withLastActive(new StreamWrapper(onSuccess ,eager));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <R> R block(final Collector collector,final StreamWrapper lastActive) {
		return (R) lastActive.stream().map((future) -> {
			return (U) getSafe(future);
		}).filter(v -> v != MISSING_VALUE)
		.collect(collector);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private<R> R aggregateResults(final  Collector collector,
			final List<CompletableFuture> completedFutures) {
		return (R) completedFutures.stream().map(next -> getSafe(next)).filter(v -> v != MISSING_VALUE)
				.collect(collector);
	}

	@SuppressWarnings("rawtypes")
	private CompletableFuture[] lastActiveArray() {
		return lastActive.list().toArray(new CompletableFuture[0]);
	}

	private void capture(final Exception e) {
		errorHandler.ifPresent((handler) -> { 
		if(!(e.getCause() instanceof FilteredExecutionPathException)){
			handler.accept(e.getCause());
		}});
	}

	@SuppressWarnings("rawtypes")
	private Object getSafe(final CompletableFuture next) {
		try {
			return next.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			capture(e);
			exceptionSoftener.throwSoftenedException(e);
		}catch (RuntimeException e) {
			capture(e);
		}
		catch (Exception e) {
			capture(e);
		}
		
		return MISSING_VALUE;
	}
	
	
	

	private final static MissingValue MISSING_VALUE =new MissingValue();
	private static class MissingValue {
		
	}
	private static class FilteredExecutionPathException extends SimpleReactProcessingException{

		private static final long serialVersionUID = 1L;
		
	}



	/**
	 * Trigger a lazy stream as a task on the provided ExecutorService
	 * 
	 * @param e Executor service to trigger lazy stream on (Stream CompletableFutures will use ExecutorService associated with this Stage
	 * may not be the same one).
	 * 
	 * 
	 */
	public void run(ExecutorService e) {
		new SimpleReact(e).react(()->run(()->null));
		
	}

	/**
	 * Trigger a lazy stream
	 */
	public void run() {
		run(()->null);
		
	}
	/**
	 * Trigger a lazy stream and return the results in the Collection created by the collector
	 * 
	 * @param collector Supplier that creates a collection to store results in
	 * @return Collection of results
	 */
	public <C extends Collection<U>>  C run(Supplier<C> collector) {
	
		C result = (C)collector.get();
		
		Optional<LazyResultConsumer<U>> batcher = result!=null ? 
					Optional.of(lazyCollector.withResults(result)) : Optional.empty();
		
		try{
		  this.lastActive.stream().forEach(n-> {
			
			  		  	
			  batcher.ifPresent(c -> c.accept(n));
			  /**
				if(result!=null){
					try {
						result.add((U)n.join()); 
					} catch (Exception e) {
						capture(e);
					}
				}**/
				this.waitStrategy.accept(n);
			});
		}catch(SimpleReactProcessingException e){
			
		}
		if(result==null)
			return null;
		return  (C)batcher.get().getResults();
		
	}

	
	@Override
	public Iterator<U> iterator() {
		
		
		return buildQueue().stream().iterator();
	}

	@Override
	public Spliterator<U> spliterator() {
		return buildQueue().stream().spliterator();
	}

	@Override
	public boolean isParallel() {
		return true;
	}

	@Override
	public Seq<U> sequential() {
		Queue q = new Queue();
		q.fromStream(lastActive.stream().map(it -> it.join()));
		q.close();
		return Seq.seq(q.stream());
	}

	@Override
	public Stage<U> parallel() {
		return this;
	}

	@Override
	public Stage<U> unordered() {
		return this;
	}

	@Override
	public Seq<U> onClose(Runnable closeHandler) {
		
		return Seq.seq(lastActive.stream().onClose(closeHandler).map(it -> (U)it.join()));
	}

	@Override
	public void close() {
		lastActive.stream().close();
		
	}

	
	@Override
	public <R> Stage<R> map(Function<? super U, ? extends R> mapper) {
		return then((Function)mapper);
	}

	
	@Override
	public IntStream mapToInt(ToIntFunction<? super U> mapper) {
		return buildQueue().stream().mapToInt(mapper);
	}

	
	@Override
	public LongStream mapToLong(ToLongFunction<? super U> mapper) {
		return buildQueue().stream().mapToLong(mapper);
	}

	
	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super U> mapper) {
		return buildQueue().stream().mapToDouble(mapper);
	}

	
	@Override
	public IntStream flatMapToInt(
			Function<? super U, ? extends IntStream> mapper) {
		return buildQueue().stream().flatMapToInt(mapper);
	}

	
	@Override
	public LongStream flatMapToLong(
			Function<? super U, ? extends LongStream> mapper) {
		return buildQueue().stream().flatMapToLong(mapper);
	}

	
	@Override
	public DoubleStream flatMapToDouble(
			Function<? super U, ? extends DoubleStream> mapper) {
		return buildQueue().stream().flatMapToDouble(mapper);
	}

	
	@Override
	public Seq<U> distinct() {
		return Seq.seq(buildQueue().stream().distinct());
		
	}

	@Override
	public Seq<U> sorted() {
		return Seq.seq(buildQueue().stream().sorted());
	}

	@Override
	public Seq<U> sorted(Comparator<? super U> comparator) {
		return Seq.seq(buildQueue().stream().sorted(comparator));
	}

	private Queue<U> buildQueue(){
		Queue<U> queue = new Queue<>();
		if(eager)
			then(it -> queue.offer(it)).allOf(it -> queue.close());
		else
			then(it -> queue.offer(it)).allOf(it -> queue.close()).run(new ForkJoinPool(1));
		return queue;
	}
	
	@Override
	public Seq<U> limit(long maxSize) {
		return Seq.seq(buildQueue().stream().limit(maxSize));
	}

	@Override
	public  Seq<U> skip(long n) {
		return Seq.seq(buildQueue().stream().skip(n));
	}

	@Override
	public void forEach(Consumer<? super U> action) {
		buildQueue().stream().forEach((Consumer)action);
		
	}

	@Override
	public void forEachOrdered(Consumer<? super U> action) {
		buildQueue().stream().forEachOrdered((Consumer)action);
		
	}

	@Override
	public Object[] toArray() {
		return buildQueue().stream().toArray();
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		return buildQueue().stream().toArray(generator);
	}

	@Override
	public U reduce(U identity, BinaryOperator<U> accumulator) {
		
	return (U)buildQueue().stream().reduce(identity, accumulator);
	}

	@Override
	public Optional<U> reduce(BinaryOperator<U> accumulator) {
		return buildQueue().stream().reduce(accumulator);
	}

	

	@Override
	public <R> R collect(Supplier<R> supplier,
			BiConsumer<R, ? super U> accumulator, BiConsumer<R, R> combiner) {
		
		return (R)buildQueue().stream().collect(supplier,accumulator,combiner);
	}

	@Override
	public <R, A> R collect(Collector<? super U, A, R> collector) {
	
		return block(collector);
	}

	@Override
	public Optional<U> min(Comparator<? super U> comparator) {
		
		return buildQueue().stream().min(comparator);
	}

	@Override
	public Optional<U> max(Comparator<? super U> comparator) {
		return buildQueue().stream().max(comparator);
	}

	@Override
	public long count() {
		
		return lastActive.stream().count();
	}

	@Override
	public boolean anyMatch(Predicate<? super U> predicate) {
		return buildQueue().stream().anyMatch(predicate);
	}

	@Override
	public boolean allMatch(Predicate<? super U> predicate) {
		return buildQueue().stream().allMatch(predicate);
	}

	@Override
	public boolean noneMatch(Predicate<? super U> predicate) {
		return buildQueue().stream().noneMatch(predicate);
	}

	@Override
	public Optional<U> findFirst() {
		return buildQueue().stream().findFirst();
	}

	@Override
	public Optional<U> findAny() {
		return buildQueue().stream().findAny();
	}

	@Override
	public <R> R reduce(R identity, BiFunction<R, ? super U, R> accumulator,
			BinaryOperator<R> combiner) {
		
		return buildQueue().stream().reduce(identity, accumulator, combiner);
	}

	@Override
	public Stream<U> stream() {
		return buildQueue().stream();
	}

	
	

}
