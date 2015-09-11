package com.aol.simple.react.mixins;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;

import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;

/**
 * Builder of EagerFutureStreams
 * 
 * @author johnmcclean
 *
 */
@AllArgsConstructor
public class OptimizedEagerReact {

	private final EagerReact react;

	private <T> Supplier<T> supplier(T element) {
		return () -> element;
	}

	/**
	 * Generate a FutureStream from the specified elements. Each element will be
	 * wrapped in a Supplier and submitted to a task executor for execution, the
	 * returned Stream will be in synchronous mode, where any subsequent
	 * operations performed on task results will occur on the same thread
	 * without involving a task executor (performance difference between
	 * submitting non-tasks and continuing on calling thread is an order of
	 * magnitude).
	 * 
	 * E.g. given 3 URLs, we can use this method to move each call onto a
	 * separte thread, but work will continue on the same thread once complete
	 * 
	 * <pre>
	 * {@code
	 *  
	 *       microReact.of(ioCallURL1,ioCallURL2,ioCallURL3)  //each URL is wrapped in a task to be recieved on potentially different threads
	 *       		   .map(this::doIO)             //each I/O Call can run on a separate thread, the calling thread always executes
	 *                 .map(this::processResult)     //map occurs on the calling thread
	 *                 .forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * @param elements
	 *            to create EagerFutureStream from
	 * @return EagerFutureStream
	 */
	public <T> EagerFutureStream<T> of(T... elements) {

		return react(Stream.of(elements).map(e -> supplier(e)));
	}

	/**
	 * Generate a FutureStream from the specified Stream. Each element in the
	 * Stream will be wrapped in a Supplier and submitted to a task executor for
	 * execution, the returned Stream will be in synchronous mode, where any
	 * subsequent operations performed on task results will occur on the same
	 * thread without involving a task executor (performance difference between
	 * submitting non-tasks and continuing on calling thread is an order of
	 * magnitude).
	 * 
	 * E.g. given 3 URLs, we can use this method to move each call onto a
	 * separte thread, but work will continue on the same thread once complete
	 * 
	 * <pre>
	 * {@code
	 *  
	 *       microReact.of(Stream.of(ioCallURL1,ioCallURL2,ioCallURL3))  //each URL is wrapped in a task to be recieved on potentially different threads
	 *       		   .map(this::doIO)             //each I/O Call can run on a separate thread, the calling thread always executes
	 *                 .map(this::processResult)     //map occurs on the calling thread
	 *                 .forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * @param stream
	 *            to create EagerFutureStream from
	 * @return EagerFutureStream
	 */
	public <T> EagerFutureStream<T> of(Stream<T> stream) {
		return react(stream.map(e -> supplier(e)));
	}

	/**
	 * Generate a FutureStream from the specified Collection. Each element in
	 * the Collection will be wrapped in a Supplier and submitted to a task
	 * executor for execution, the returned Stream will be in synchronous mode,
	 * where any subsequent operations performed on task results will occur on
	 * the same thread without involving a task executor (performance difference
	 * between submitting non-tasks and continuing on calling thread is an order
	 * of magnitude).
	 * 
	 * E.g. given 3 URLs, we can use this method to move each call onto a
	 * separte thread, but work will continue on the same thread once complete
	 * 
	 * <pre>
	 * {@code
	 *  
	 *       microReact.of(Arrays.asList(ioCallURL1,ioCallURL2,ioCallURL3))  //each URL is wrapped in a task to be recieved on potentially different threads
	 *       		   .map(this::doIO)             //each I/O Call can run on a separate thread, the calling thread always executes
	 *                 .map(this::processResult)     //map occurs on the calling thread
	 *                 .forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * @param collection
	 *            to create EagerFutureStream from
	 * @return EagerFutureStream
	 */
	public <T> EagerFutureStream<T> of(Collection<T> collection) {
		return react(collection.stream().map(e -> supplier(e)));
	}

	/**
	 * Generate a FutureStream from the specified Iterable. Each element in the
	 * Iterable will be wrapped in a Supplier and submitted to a task executor
	 * for execution, the returned Stream will be in synchronous mode, where any
	 * subsequent operations performed on task results will occur on the same
	 * thread without involving a task executor (performance difference between
	 * submitting non-tasks and continuing on calling thread is an order of
	 * magnitude).
	 * 
	 * E.g. given 3 URLs, we can use this method to move each call onto a
	 * separte thread, but work will continue on the same thread once complete
	 * 
	 * <pre>
	 * {@code
	 *  
	 *       microReact.ofIterable(Arrays.asList(ioCallURL1,ioCallURL2,ioCallURL3))  //each URL is wrapped in a task to be recieved on potentially different threads
	 *       		   .map(this::doIO)             //each I/O Call can run on a separate thread, the calling thread always executes
	 *                 .map(this::processResult)     //map occurs on the calling thread
	 *                 .forEach(System.out::println);
	 * }
	 * </pre>
	 *
	 * @param iterable
	 *            to create EagerFutureStream from
	 * @return EagerFutureStream
	 */
	public <T> EagerFutureStream<T> ofIterable(Iterable<T> iterable) {
		return react(StreamSupport.stream(iterable.spliterator(),
				false).map(e -> supplier(e)));
	}

	/**
	 * Generate a FutureStream from the specified Suppliers. Each Supplier and
	 * submitted to a task executor for execution, the returned Stream will be
	 * in synchronous mode, where any subsequent operations performed on task
	 * results will occur on the same thread without involving a task executor
	 * (performance difference between submitting non-tasks and continuing on
	 * calling thread is an order of magnitude).
	 * 
	 * <pre>
	 * {@code
	 *  
	 *       microReact.react(()->ioCallURL1,()->ioCallURL2,()->ioCallURL3)  //each URL Supplier is an Async task to be recieved on potentially different threads
	 *       		   .map(this::doIO)             //each I/O Call can run on a separate thread, the calling thread always executes
	 *                 .map(this::processResult)     //map occurs on the calling thread
	 *                 .forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param suppliers
	 *            to create EagerFutureStream from
	 * @return EagerFutureStream
	 */
	public <T> EagerFutureStream<T> react(Supplier<T>... suppliers) {
		return react.react(suppliers).sync();
	}

	/**
	 * Generate a FutureStream from the specified Stream of Suppliers. Each
	 * Supplier and submitted to a task executor for execution, the returned
	 * Stream will be in synchronous mode, where any subsequent operations
	 * performed on task results will occur on the same thread without involving
	 * a task executor (performance difference between submitting non-tasks and
	 * continuing on calling thread is an order of magnitude).
	 * 
	 * <pre>
	 * {@code
	 *  
	 *       microReact.react(Stream.of(()->ioCallURL1,()->ioCallURL2,()->ioCallURL3))  //each URL Supplier is an Async task to be recieved on potentially different threads
	 *       		   .map(this::doIO)             //each I/O Call can run on a separate thread, the calling thread always executes
	 *                 .map(this::processResult)     //map occurs on the calling thread
	 *                 .forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * @param suppliers
	 *            to create EagerFutureStream from
	 * @return EagerFutureStream
	 */
	public <T> EagerFutureStream<T> react(Stream<Supplier<T>> suppliers) {
		return react.react(suppliers).sync();
	}

	/**
	 * Generate a FutureStream from the specified Collection of Suppliers. Each
	 * Supplier and submitted to a task executor for execution, the returned
	 * Stream will be in synchronous mode, where any subsequent operations
	 * performed on task results will occur on the same thread without involving
	 * a task executor (performance difference between submitting non-tasks and
	 * continuing on calling thread is an order of magnitude).
	 * 
	 * <pre>
	 * {@code
	 *  
	 *       microReact.react(Arrays.asList(()->ioCallURL1,()->ioCallURL2,()->ioCallURL3))  //each URL Supplier is an Async task to be recieved on potentially different threads
	 *       		   .map(this::doIO)             //each I/O Call can run on a separate thread, the calling thread always executes
	 *                 .map(this::processResult)     //map occurs on the calling thread
	 *                 .forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param suppliers
	 *            to create EagerFutureStream from
	 * @return EagerFutureStream
	 */
	public <T> EagerFutureStream<T> react(Collection<Supplier<T>> suppliers) {
		return react.react(suppliers).sync();
	}

	/**
	 * Generate a FutureStream from the specified Iterable of Suppliers. Each
	 * Supplier and submitted to a task executor for execution, the returned
	 * Stream will be in synchronous mode, where any subsequent operations
	 * performed on task results will occur on the same thread without involving
	 * a task executor (performance difference between submitting non-tasks and
	 * continuing on calling thread is an order of magnitude).
	 * 
	 * <pre>
	 * {@code
	 *  
	 *       microReact.reactIterable(Arrays.asList(()->ioCallURL1,()->ioCallURL2,()->ioCallURL3))  //each URL Supplier is an Async task to be recieved on potentially different threads
	 *       		   .map(this::doIO)             //each I/O Call can run on a separate thread, the calling thread always executes
	 *                 .map(this::processResult)     //map occurs on the calling thread
	 *                 .forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param suppliers
	 *            to create EagerFutureStream from
	 * @return EagerFutureStream
	 */
	public <T> EagerFutureStream<T> reactIterable(Iterable<Supplier<T>> suppliers) {
		return react.reactIterable(suppliers).sync();
	}
}
