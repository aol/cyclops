package com.aol.cyclops.javaslang.reactivestreams;

import java.util.function.Consumer;
import java.util.function.Function;

import javaslang.collection.Stream;

import org.reactivestreams.Subscription;

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.javaslang.streams.StreamUtils;


public class ReactiveStreamImpl<T> implements ReactiveStream<T> {

	private final Stream<? extends T> stream;
	
	ReactiveStreamImpl(Stream<? extends T> stream){
		this.stream = stream;
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#isParallel()
	 */
	@Override
	public boolean isParallel() {
		return false;
	}


	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#sequential()
	 */
	@Override
	public ReactiveStream<T> sequential() {
		return this;
	}


	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#parallel()
	 */
	@Override
	public ReactiveStream<T> parallel() {
		return this;
	}


	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#unordered()
	 */
	@Override
	public ReactiveStream<T> unordered() {
		return this;
	}


	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#onClose(java.lang.Runnable)
	 */
	@Override
	public ReactiveStream<T> onClose(Runnable closeHandler) {
		return this;
	}


	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#close()
	 */
	@Override
	public void close() {
		
		
	}


	@Override
	public Stream<T> toStream() {
	       return (Stream)stream;
	 }
	
	public <X extends Throwable> Subscription forEachX(long numberOfElements,Consumer<? super T> consumer){
		return StreamUtils.forEachX(this, numberOfElements, consumer);
	}
	public <X extends Throwable> Subscription forEachXWithError(long numberOfElements,Consumer<? super T> consumer,Consumer<? super Throwable> consumerError){
		return StreamUtils.forEachXWithError(this,numberOfElements,consumer,consumerError);
	}
	public <X extends Throwable> Subscription forEachXEvents(long numberOfElements,Consumer<? super T> consumer,Consumer<? super Throwable> consumerError, Runnable onComplete){
		return StreamUtils.forEachXEvents(this, numberOfElements, consumer, consumerError, onComplete);
	}
	
	public <X extends Throwable> void forEachWithError(Consumer<? super T> consumerElement,
			Consumer<? super Throwable> consumerError){
			StreamUtils.forEachWithError(this, consumerElement, consumerError);
	}
	public <X extends Throwable> void forEachEvent(Consumer<? super T> consumerElement,
			Consumer<? super Throwable> consumerError,
			Runnable onComplete){
		StreamUtils.forEachEvent(this, consumerElement, consumerError, onComplete);
	}

	@Override
	public <R1, R2, R> ReactiveStream<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
			Function<? super T, Function<? super R1, ? extends Iterable<R2>>> stream2,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {
		
		return ReactiveStream.fromStream(Do.add(this)
				  .withIterable(u->stream1.apply(u))
				  .withIterable(u->r1->stream2.apply(u).apply(r1))
				  	.yield(yieldingFunction).unwrap());
	}

	@Override
	public <R1, R2, R> ReactiveStream<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
			Function<? super T, Function<? super R1, ? extends Iterable<R2>>> stream2,
			Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {
		 return ReactiveStream.fromStream(Do.add(this)
				  .withIterable(u->stream1.apply(u))
				  .withIterable(u->r1->stream2.apply(u).apply(r1))
				  .filter(filterFunction)
				  .yield(yieldingFunction).unwrap());
	}

	@Override
	public <R1, R> ReactiveStream<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
		return ReactiveStream.fromStream(Do.add(this)
				.withIterable(u->stream1.apply(u))
				.yield(yieldingFunction).unwrap());
	}

	@Override
	public <R1, R> ReactiveStream<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
			Function<? super T, Function<? super R1, Boolean>> filterFunction, Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
		return ReactiveStream.fromStream(Do.add(this)
							.withIterable(u->stream1.apply(u))
							.filter(filterFunction)
							.yield(yieldingFunction).unwrap());
	}



	/* (non-Javadoc)
	 * @see javaslang.collection.Traversable#head()
	 */
	@Override
	public T head() {
		return stream.head();
	}



	/* (non-Javadoc)
	 * @see javaslang.collection.Traversable#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return stream.isEmpty();
	}

}
