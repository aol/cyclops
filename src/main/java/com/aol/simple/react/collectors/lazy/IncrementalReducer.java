package com.aol.simple.react.collectors.lazy;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Value;

import com.aol.simple.react.stream.MissingValue;
import com.aol.simple.react.stream.lazy.ParallelReductionConfig;
import com.aol.simple.react.stream.traits.BlockingStream;

@Value
public class IncrementalReducer<T> {
	private final LazyResultConsumer<T> consumer;
	@Getter
	private final BlockingStream<T> blocking;
	private final ParallelReductionConfig config;
	
	public void forEach(Consumer<? super T> c, Function<CompletableFuture,T> safeJoin){
		if(consumer.getResults().size()>config.getBatchSize()){
			forEachResults(consumer.getResults(),c, safeJoin);
		}
	}
	public void forEachResults( Collection<CompletableFuture<T>> results,Consumer<? super T> c,
			Function<CompletableFuture, T> safeJoin) {
		Stream<CompletableFuture<T>> stream = consumer.getResults().stream();
		Stream<CompletableFuture<T>> streamToUse = this.config.isParallel() ? stream.parallel() : stream;
		streamToUse.map(safeJoin).filter(v -> v != MissingValue.MISSING_VALUE).forEach(c);
		consumer.getResults().clear();
	}
	public  T reduce(Function<CompletableFuture,T>safeJoin,T identity, BinaryOperator<T> accumulator){
		if(consumer.getResults().size()>config.getBatchSize()){
			 return reduceResults(consumer.getResults(),safeJoin, identity, accumulator);
		}
		
		return identity;
	}
	public T reduceResults( Collection<CompletableFuture<T>> results,Function<CompletableFuture, T> safeJoin, T identity,
			BinaryOperator<T> accumulator) {
		Stream<CompletableFuture<T>> stream = results.stream();
		 Stream<CompletableFuture<T>> streamToUse = this.config.isParallel() ? stream.parallel() : stream;
		 T result = streamToUse.map(safeJoin)
					.filter(v -> v != MissingValue.MISSING_VALUE).reduce(identity, accumulator);
		consumer.getResults().clear();
		return result;
	}
	public  Optional<T> reduce(Function<CompletableFuture,T>safeJoin, BinaryOperator<T> accumulator){
		if(consumer.getResults().size()>config.getBatchSize()){
			 return reduceResults(consumer.getResults(),safeJoin, accumulator);
		}
		
		return Optional.empty();
	}
	public Optional<T> reduceResults( Collection<CompletableFuture<T>> results,Function<CompletableFuture, T> safeJoin,
			BinaryOperator<T> accumulator) {
		Stream<CompletableFuture<T>> stream = results.stream();
		 Stream<CompletableFuture<T>> streamToUse = this.config.isParallel() ? stream.parallel() : stream;
		 Optional<T> result = streamToUse.map(safeJoin)
					.filter(v -> v != MissingValue.MISSING_VALUE).reduce( accumulator);
		consumer.getResults().clear();

		return result;
	}
	public <U> U reduce(Function<CompletableFuture,T>safeJoin,U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner){
		if(consumer.getResults().size()>config.getBatchSize()){
			 return reduceResults(consumer.getResults(),safeJoin, identity, accumulator,combiner);
		}
		return identity;
	}
	public <U> U reduceResults( Collection<CompletableFuture<T>> results,Function<CompletableFuture, T> safeJoin, U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner){
		Stream<CompletableFuture<T>> stream = results.stream();
		 Stream<CompletableFuture<T>> streamToUse = this.config.isParallel() ? stream.parallel() : stream;
		 U result = streamToUse.map(safeJoin)
					.filter(v -> v != MissingValue.MISSING_VALUE).reduce(identity, accumulator,combiner);
		consumer.getResults().clear();
		return result;
	}
}
