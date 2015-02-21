package com.aol.simple.react.stream.lazy;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.generators.Generator;
import com.aol.simple.react.generators.ReactIterator;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.ThreadPools;
import com.nurkiewicz.asyncretry.RetryExecutor;

@Builder
@Wither
@AllArgsConstructor
public class LazyReact extends BaseSimpleReact {

	@Getter
	private final ExecutorService executor;
	@Getter
	private final RetryExecutor retrier;
	@Getter
	private final boolean eager = false;

	public LazyReact(){
		this( ThreadPools.getStandard());
		
	}
	public LazyReact(ExecutorService executor) {
		this.executor = executor;
		this.retrier = null;
		
	}
	
	@Override
	public <U> LazyFutureStream<U> construct(Stream s,
			ExecutorService executor, RetryExecutor retrier, boolean eager) {
		
		return (LazyFutureStream) new LazyFutureStreamImpl<U>( s,executor, retrier);

	}

	@Override
	public <U> LazyFutureStream<U> fromStream(
			Stream<CompletableFuture<U>> stream) {
	
		return (LazyFutureStream)super.fromStream(stream);
	}

	@Override
	public <U> LazyFutureStream<U> fromStreamWithoutFutures(Stream<U> stream) {
		
		return (LazyFutureStream)super.fromStreamWithoutFutures(stream);
	}

	@Override
	public <U> LazyFutureStream<U> of(U... array) {
		
		return (LazyFutureStream)super.of(array);
	}

	@Override
	public <U> LazyFutureStream<U> react(List<Supplier<U>> actions) {
		
		return (LazyFutureStream)super.react(actions);
	}

	@Override
	public <U> LazyFutureStream<U> react(Iterator<U> iterator, int maxTimes) {
		
		return (LazyFutureStream)super.react(iterator, maxTimes);
	}

	@Override
	public <R> LazyFutureStream<R> reactToCollection(Collection<R> collection) {
		
		return (LazyFutureStream)super.reactToCollection(collection);
	}

	@Override
	public <U> LazyFutureStream<U> react(Supplier<U> s, Generator t) {
		
		return (LazyFutureStream)super.react(s, t);
	}

	@Override
	public <U> LazyFutureStream<U> reactInfinitely(Supplier<U> s) {
		
		return (LazyFutureStream)super.reactInfinitely(s);
	}

	@Override
	public <U> LazyFutureStream<U> iterateInfinitely(U seed, UnaryOperator<U> f) {
		
		return (LazyFutureStream)super.iterateInfinitely(seed, f);
	}

	@Override
	public <U> LazyFutureStream<U> react(Function<U, U> f, ReactIterator<U> t) {
		
		return (LazyFutureStream)super.react(f, t);
	}

	@Override
	protected <U> LazyFutureStream<U> reactI(Supplier<U>... actions) {
		
		return (LazyFutureStream)super.reactI(actions);
	}

}
