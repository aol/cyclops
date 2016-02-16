package com.aol.cyclops.internal.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;


import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.reactive.ReactiveTask;

public class ReactiveSeqFutureOpterationsImpl<T> extends BaseFutureOperationsImpl<T>{
	
	public ReactiveSeqFutureOpterationsImpl(Executor exec, ReactiveSeq<T> stream) {
		super(exec, stream);
		
	}
	@Override
	public <X extends Throwable> ReactiveTask forEachX(long numberOfElements, Consumer<? super T> consumer) {
		return new ReactiveTask( getExec(),FutureStreamUtils.forEachX(getStream(),numberOfElements, consumer)
								.map2(r->CompletableFuture.runAsync(r,getExec())));
	}
	@Override
	public <X extends Throwable> ReactiveTask forEachXWithError(long numberOfElements, Consumer<? super T> consumer,
			Consumer<? super Throwable> consumerError) {
		return new ReactiveTask(getExec(),FutureStreamUtils.forEachXWithError(getStream(),numberOfElements, consumer, consumerError)
								.map2(r->CompletableFuture.runAsync(r,getExec())));

		
	}
	@Override
	public <X extends Throwable> ReactiveTask forEachXEvents(long numberOfElements, Consumer<? super T> consumer,
			Consumer<? super Throwable> consumerError, Runnable onComplete) {
		return new ReactiveTask(getExec(),FutureStreamUtils.forEachXEvents(getStream(),numberOfElements, consumer, consumerError, onComplete)
								.map2(r->CompletableFuture.runAsync(r,getExec())));
		
	}
	@Override
	public <X extends Throwable> ReactiveTask forEachWithError(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError) {
		
		return new ReactiveTask( getExec(),FutureStreamUtils.forEachWithError(getStream(),consumerElement, consumerError)
							.map2(r->CompletableFuture.runAsync(r,getExec())));
	}
	@Override
	public <X extends Throwable> ReactiveTask forEachEvent(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError,
			Runnable onComplete) {
		return new ReactiveTask(getExec(),FutureStreamUtils.forEachEvent(getStream(),consumerElement, consumerError,onComplete)
									.map2(r->CompletableFuture.runAsync(r,getExec())));
	}
}
