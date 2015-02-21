package com.aol.simple.react.stream.eager;

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
public class EagerReact extends BaseSimpleReact{
	@Getter
	private final ExecutorService executor;
	@Getter
	private final RetryExecutor retrier;
	@Getter
	private final boolean eager = true;
	
	public EagerReact(){
		this( ThreadPools.getStandard());
		
	}
	public EagerReact(ExecutorService executor) {
		this.executor = executor;
		this.retrier = null;
		
	}

	@Override
	public <U> EagerFutureStream<U> construct(Stream s,
			ExecutorService executor, RetryExecutor retrier, boolean eager) {
		return (EagerFutureStream) new EagerFutureStreamImpl<U>( s,executor, retrier);
	}

	@Override
	public <U> EagerFutureStream<U> fromStream(
			Stream<CompletableFuture<U>> stream) {
	
		return (EagerFutureStream)super.fromStream(stream);
	}

	@Override
	public <U> EagerFutureStream<U> fromStreamWithoutFutures(Stream<U> stream) {
		
		return (EagerFutureStream)super.fromStreamWithoutFutures(stream);
	}

	@Override
	public <U> EagerFutureStream<U> of(U... array) {
		
		return (EagerFutureStream)super.of(array);
	}

	@Override
	public <U> EagerFutureStream<U> react(List<Supplier<U>> actions) {
		
		return (EagerFutureStream)super.react(actions);
	}

	@Override
	public <U> EagerFutureStream<U> react(Iterator<U> iterator, int maxTimes) {
		
		return (EagerFutureStream)super.react(iterator, maxTimes);
	}

	@Override
	public <R> EagerFutureStream<R> reactToCollection(Collection<R> collection) {
		
		return (EagerFutureStream)super.reactToCollection(collection);
	}

	@Override
	public <U> EagerFutureStream<U> react(Supplier<U> s, Generator t) {
		
		return (EagerFutureStream)super.react(s, t);
	}

	@Override
	public <U> EagerFutureStream<U> iterateInfinitely(U seed, UnaryOperator<U> f) {
		
		return (EagerFutureStream)super.iterateInfinitely(seed, f);
	}

	@Override
	public <U> EagerFutureStream<U> react(Function<U, U> f, ReactIterator<U> t) {
		
		return (EagerFutureStream)super.react(f, t);
	}

}
