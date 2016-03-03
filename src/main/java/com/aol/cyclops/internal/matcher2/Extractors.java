package com.aol.cyclops.internal.matcher2;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import lombok.val;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.data.LazyImmutable;
import com.aol.cyclops.internal.invokedynamic.ReflectionCache;
import com.aol.cyclops.control.FluentFunctions;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.util.ExceptionSoftener;

/**
 * Generic extractors for use s pre and post data extractors.
 * 
 * @author johnmcclean
 *
 */
public class Extractors {
	
	
	
	
	/**
	 * An extractor that caches the extraction result
	 * 
	 * @param extractor to memoise (cache result of)
	 * @return Memoised extractor
	 */
	public static final <T,R > Extractor<T,R> memoised( Extractor<T,R> extractor){
		final LazyImmutable<R> value = new LazyImmutable<>();
		return input -> {
			return value.computeIfAbsent(()->extractor.apply(input));
				
		};
		
	}
	/**
	 * @return Extractor that decomposes Case classes into iterables 
	 */
	public static final <T,R> Extractor<T,R> decompose() {
		return input -> {
			if(input instanceof  Decomposable)
				return (R)((Decomposable)input).unapply();
			else if(input instanceof Iterable)
				return (R)input;
			if(input instanceof Optional){
				return (R)Maybe.fromOptional((Optional)(input));
			}
			return (R)input;
			

		};
	}
	/**
	 * @return Extractor that decomposes Case classes into iterables 
	 */
	public static final <T,R> Extractor<T,R> decomposeCoerced() {
		return input -> {
			if(input instanceof  Decomposable)
				return (R)((Decomposable)input).unapply();
			else if(input instanceof Iterable)
				return (R)input;
			if(input instanceof Optional){
				return (R)Maybe.fromOptional((Optional)(input));
			}
			return (R)ReflectionCache.getUnapplyMethod(input.getClass()).map(FluentFunctions.ofChecked(m->m.invoke(input))).orElse(AsDecomposable.asDecomposable(input).unapply());

		};
	}
	
}
