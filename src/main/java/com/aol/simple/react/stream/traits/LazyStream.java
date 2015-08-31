package com.aol.simple.react.stream.traits;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.collectors.lazy.EmptyCollector;
import com.aol.simple.react.collectors.lazy.IncrementalReducer;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.MissingValue;
import com.aol.simple.react.stream.Runner;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.lazy.ParallelReductionConfig;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.threads.SequentialElasticPools;

public interface LazyStream<U> extends BlockingStream<U>{
	
	LazyStreamWrapper<U> getLastActive();
	Supplier<LazyResultConsumer<U>> getLazyCollector();
	
	
	Optional<Consumer<Throwable>> getErrorHandler();
	 ParallelReductionConfig getParallelReduction();
	 MaxActive getMaxActive();
	 
	/**
	 * Trigger a lazy stream as a task on the provided Executor
	 * 
	 * @param e
	 *            Executor service to trigger lazy stream on (Stream
	 *            CompletableFutures will use Executor associated with
	 *            this Stage may not be the same one).
	 * 
	 * 
	 */
	default void runOn(Executor e) {
		SimpleReact reactor  = SequentialElasticPools.simpleReact.nextReactor();
		reactor.react(() -> run(new NonCollector())).peek(n-> SequentialElasticPools.simpleReact.populate(reactor));

	}

	default void runThread(Runnable r) {
		Function<FastFuture,U> safeJoin = (FastFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		new Thread(() -> new Runner(r).run(getLastActive(),new EmptyCollector(getMaxActive(),safeJoin))).start();

	}
	
	default Continuation runContinuation(Runnable r) {
		Function<FastFuture,U> safeJoin = (FastFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		return new Runner(r).runContinuations(getLastActive(),new EmptyCollector(getMaxActive(),safeJoin));

	}
	/**
	 * Trigger a lazy stream
	 */
	default void runOnCurrent() {
		
		
		run(new NonCollector());

	}
	
	/**
	 * Trigger a lazy stream
	 */
	default void run() {
		//FIXME this should use an elastic pool of executors try {  } finally { }
		runOn(ThreadPools.getLazyExecutor());

	}

	/**
	 * Trigger a lazy stream and return the results in the Collection created by
	 * the collector
	 * 
	 * @param collector
	 *            Supplier that creates a collection to store results in
	 * @return Collection of results
	 */
	default <A,R> R run(Collector<U,A,R> collector) {
/**		if(getLastActive().isSequential()){ 
			//if single threaded we can simply push from each Future into the collection to be returned
			if(collector.supplier().get()==null){
				forEach(r->{});
				return null;
			}
			A col = collector.supplier().get();
			forEach(r->collector.accumulator().accept(col,r));
			return collector.finisher().apply(col);
		
		}**/
		Optional<LazyResultConsumer<U>> batcher = collector.supplier().get() != null ? Optional
				.of(getLazyCollector().get().withResults( new ArrayList<>())) : Optional.empty();

		try {
		
			this.getLastActive().injectFutures().forEach(n -> {
				
				batcher.ifPresent(c -> c.accept(n));
			
				
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		if (collector.supplier().get() == null)
			return null;
		
		return (R)batcher.get().getAllResults().stream()
									.map(cf -> BlockingStreamHelper.getSafe(cf,getErrorHandler()))
									.filter(v -> v != MissingValue.MISSING_VALUE)
									.collect((Collector)collector);
		

	}
	
	
	
	default void forEach(Consumer<? super U> c){

		
	
		Function<FastFuture,U> safeJoin = (FastFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		IncrementalReducer<U> collector = new IncrementalReducer(this.getLazyCollector().get().withResults(new ArrayList<>()), this,
				getParallelReduction());
		try {
			this.getLastActive().operation(f-> f.peek(c)).injectFutures().forEach( next -> {
				
			 collector.getConsumer().accept(next);
				
		
				
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		collector.getConsumer().block();
	
		
		

	}
	
	default Optional<U> reduce(BinaryOperator<U> accumulator){
/**		if(getLastActive().isSequential()){ 
			Object[] result = {null};
			forEach(r-> {
				if(result[0]==null)
					result[0]=r;
				else{
					result[0]=accumulator.apply((U)result[0], r);
				}
			});
			return (Optional)Optional.ofNullable(result[0]);
		
		}**/
		Function<FastFuture,U> safeJoin = (FastFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		IncrementalReducer<U> collector = new IncrementalReducer(this.getLazyCollector().get().withResults(new ArrayList<>()), this,
			getParallelReduction());
		Optional[] result =  {Optional.empty()};
		try {
			this.getLastActive().injectFutures().forEach(next -> {

				
				collector.getConsumer().accept(next);
			
				
				
				if(!result[0].isPresent())
					result[0] = collector.reduce(safeJoin,accumulator);
				else
					result[0] = result[0].map(v ->collector.reduce(safeJoin,(U)v,accumulator));	
				
			});
		} catch (SimpleReactProcessingException e) {
			
		}
	
		 if(result[0].isPresent())
					return result[0].map(v-> collector.reduceResults(collector.getConsumer().getAllResults(), safeJoin,(U)v, accumulator));
			
			return		collector.reduceResults(collector.getConsumer().getAllResults(), safeJoin, accumulator);
			
	}
	default U reduce(U identity, BinaryOperator<U> accumulator){
		
		Function<FastFuture,U> safeJoin = (FastFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		IncrementalReducer<U> collector = new IncrementalReducer(this.getLazyCollector().get().withResults(new ArrayList<>()), this,
			getParallelReduction());
		Object[] result =  {identity};
		try {
			this.getLastActive().injectFutures().forEach(next -> {

				
				collector.getConsumer().accept(next);
			
				result[0] = collector.reduce(safeJoin,(U)result[0],accumulator);	
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		return collector.reduceResults(collector.getConsumer().getAllResults(), safeJoin,(U)result[0], accumulator);
	}
	
	default<T> T reduce(T identity, BiFunction<T,? super U,T> accumulator, BinaryOperator<T> combiner){
		Function<FastFuture,U> safeJoin = (FastFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		IncrementalReducer<U> collector = new IncrementalReducer(this.getLazyCollector().get().withResults(new ArrayList<>()), this,
			getParallelReduction());
		Object[] result =  {identity};
		try {
			this.getLastActive().injectFutures().forEach(next -> {

				
				collector.getConsumer().accept(next);
				result[0] = collector.reduce(safeJoin,(T)result[0],accumulator,combiner);	
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		return collector.reduceResults(collector.getConsumer().getAllResults(), safeJoin,(T)result[0], accumulator,combiner);
	}
	
	default <R> R collect(Supplier<R> supplier, BiConsumer<R,? super U> accumulator, BiConsumer<R,R> combiner){
		LazyResultConsumer<U> batcher =  getLazyCollector().get().withResults( new ArrayList<>());

		try {
			this.getLastActive().injectFutures().forEach(n -> {

				batcher.accept(n);
			
				
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		
		
		return (R)batcher.getAllResults().stream()
									.map(cf ->  BlockingStreamHelper.getSafe(cf,getErrorHandler()))
									.filter(v -> v != MissingValue.MISSING_VALUE)
									.collect((Supplier)supplier,(BiConsumer)accumulator,(BiConsumer)combiner);
		
	}
	
}