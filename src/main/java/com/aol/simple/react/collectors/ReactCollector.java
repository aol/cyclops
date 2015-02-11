package com.aol.simple.react.collectors;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collector;

import com.aol.simple.react.Stage;
import com.aol.simple.react.StageWithResults;
import com.aol.simple.react.Status;
import com.aol.simple.react.exceptions.ThrowsSoftened;
import com.aol.simple.react.extractors.Extractor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReactCollector<U> {

	private final Stage<U> builder;
	
	/**
	 * React and <b>block</b>
	 * 
	 * <code>
	 	List&lt;String&gt; strings = SimpleReact.&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
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
	 * @return Results of currently active stage aggregated in a List
	 * throws InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "hiding", "unchecked" })
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public  StageWithResults<List<U>,U> block() {
		return (StageWithResults<List<U>,U>)this.packageResults(builder.block());
	}
	
	/**
	 * @param collector to perform aggregation / reduction operation on the results (e.g. to Collect into a List or String)
	 * @return Results of currently active stage in aggregated in form determined by collector
	 * throws InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes"})
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public <X> StageWithResults<X,U> block(final Collector collector) {
		return (StageWithResults<X,U>)this.packageResults(builder.block(collector));
	}
	/**
	 * Block until first result recieved
	 * 
	 * @return  first result.
	 * throws InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes"})
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public StageWithResults<U,U> first() {
		return (StageWithResults<U,U>)packageResults(builder.first());
	}
	/**
	 * Block until all results recieved.
	 * 
	 * @return  last result
	 * throws InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes"})
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public StageWithResults<U,U> last() {
		return (StageWithResults<U,U>)packageResults(builder.last());
	}
	
	/**
	 * Block until tasks complete and return a value determined by the extractor supplied.
	 * 
	 * @param extractor used to determine which value should be returned, recieves current collected input and extracts a return value
	 * @return Value determined by the supplied extractor
	 * throws InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes"})
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public <X> StageWithResults<X,U> blockAndExtract(final Extractor extractor) {
		return (StageWithResults<X,U>)this.packageResults(builder.blockAndExtract(extractor));
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
	@SuppressWarnings({ "hiding", "unchecked","rawtypes"})
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public <X> StageWithResults<X,U> blockAndExtract(final Extractor extractor,final Predicate<Status> breakout) {
		return (StageWithResults<X,U>)this.packageResults(builder.blockAndExtract(extractor,breakout));
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
	  	List&lt;String&gt; strings = SimpleReact.&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
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
	@SuppressWarnings({ "hiding", "rawtypes", "unchecked" })
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public  StageWithResults<List<U>,U> block(final Predicate<Status> breakout) {
		return (StageWithResults<List<U>,U>)packageResults(builder.block(breakout) );
	}
	
	
	/**
	 * @param collector to perform aggregation / reduction operation on the results (e.g. to Collect into a List or String)
	 * @param breakout  Predicate that determines whether the block should be
	 *            continued or removed
	 * @return Completed results of currently active stage at full completion
	 *         point or when breakout triggered (which ever comes first), in aggregated in form determined by collector
	 *         
	 * throws InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes" })
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public <X> StageWithResults<X,U> block(final Collector collector,final Predicate<Status> breakout) {
		return (StageWithResults<X,U>)this.packageResults( builder.block(collector,breakout));
	}
	private <Y> StageWithResults<Y,U> packageResults(Y results){
		return new StageWithResults(builder,results);
	}
	
}
