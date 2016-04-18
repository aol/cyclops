package com.aol.cyclops.react.collectors.lazy;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.cyclops.internal.react.async.future.FastFuture;
import com.aol.cyclops.internal.react.stream.MissingValue;
import com.aol.cyclops.types.futurestream.BlockingStream;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Perform incremental (optionally parallel) reduction on a stream
 * 
 * @author johnmcclean
 *
 * @param <T> Data type
 */
@Getter
@AllArgsConstructor
public class IncrementalReducer<T> {
	private final LazyResultConsumer<T> consumer;
	private final BlockingStream<T> blocking;
	
	
	public void forEach(Consumer<? super T> c, Function<FastFuture,T> safeJoin){
		
			forEachResults(consumer.getResults(),c, safeJoin);
		
	}
	public void forEachResults( Collection<FastFuture<T>> results,Consumer<? super T> c,
			Function<FastFuture, T> safeJoin) {
		Stream<FastFuture<T>> streamToUse = results.stream();
		streamToUse.map(safeJoin).filter(v -> v != MissingValue.MISSING_VALUE).forEach(c);
		consumer.getResults().clear();
	}
	public  T reduce(Function<FastFuture,T>safeJoin,T identity, BinaryOperator<T> accumulator){
	    return reduceResults(consumer.getResults(),safeJoin, identity, accumulator);
		
	}
	public T reduceResults( Collection<FastFuture<T>> results,Function<FastFuture, T> safeJoin, T identity,
			BinaryOperator<T> accumulator) {
		Stream<FastFuture<T>> streamToUse = results.stream();
		
		 T result = streamToUse.map(safeJoin)
					.filter(v -> v != MissingValue.MISSING_VALUE).reduce(identity, accumulator);
		consumer.getResults().clear();
		return result;
	}
	public  Optional<T> reduce(Function<FastFuture,T>safeJoin, BinaryOperator<T> accumulator){
		
			 return reduceResults(consumer.getResults(),safeJoin, accumulator);
		
	}
	public Optional<T> reduceResults( Collection<FastFuture<T>> results,Function<FastFuture, T> safeJoin,
			BinaryOperator<T> accumulator) {
		Stream<FastFuture<T>> streamToUse = results.stream();
		
		 Optional<T> result = streamToUse.map(safeJoin)
					.filter(v -> v != MissingValue.MISSING_VALUE).reduce( accumulator);
		consumer.getResults().clear();

		return result;
	}
	public <U> U reduce(Function<FastFuture,T>safeJoin,U identity, BiFunction<U,? super T,U> accumulator){
        
        return reduceResults(consumer.getResults(),safeJoin, identity, accumulator);
   
	}
	public <U> U reduce(Function<FastFuture,T>safeJoin,U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner){
		
			 return reduceResults(consumer.getResults(),safeJoin, identity, accumulator,combiner);
		
	}
	public <U> U reduceResults( Collection<FastFuture<T>> results,Function<FastFuture, T> safeJoin, U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner){
		Stream<FastFuture<T>> streamToUse = results.stream();
		
		 U result = streamToUse.map(safeJoin)
					.filter(v -> v != MissingValue.MISSING_VALUE).reduce(identity, accumulator,combiner);
		consumer.getResults().clear();
		return result;
	}
	public <U> U reduceResults( Collection<FastFuture<T>> results,Function<FastFuture, T> safeJoin, U identity, BiFunction<U,? super T,U> accumulator){
        Stream<FastFuture<T>> streamToUse = results.stream();
        
         U result = Seq.seq(streamToUse).map(safeJoin)
                    .filter(v -> v != MissingValue.MISSING_VALUE).foldLeft(identity, accumulator);
        consumer.getResults().clear();
        return result;
    }
}
