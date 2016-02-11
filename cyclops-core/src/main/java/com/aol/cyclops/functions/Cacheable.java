package com.aol.cyclops.functions;

import java.util.function.Function;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;

/**
 * Interface that represents a pluggable cache
 * 
 * @author johnmcclean
 * 
 * E.g. plugging in a Guava cache
 * <pre>
 * {@code 
 * Cache<Object, String> cache = CacheBuilder.newBuilder()
       .maximumSize(1000)
       .expireAfterWrite(10, TimeUnit.MINUTES)
       .build();

 * BiFunction<Integer,Integer,Integer> s = Memoize.memoizeBiFunction( (a,b)->a + ++called,
 * 													(key,fn)-> cache.get(key,()->fn.apply(key)); 
 * }
 * </pre>
 *
 * @param <OUT>
 */
public interface Cacheable<OUT> {

	default SoftenedCacheable<OUT> soften(){
		return (key,fn) -> {
			try{
				return computeIfAbsent(key,fn);
			}catch(Throwable t){
				throw ExceptionSoftener.throwSoftenedException(t);
			}
		};
	}
	/**
	 * Implementation should call the underlying cache
	 * @param key To lookup cached value
	 * @param fn Function to compute value, if it is not in the cache
	 * @return Cached (or computed) result
	 */
	public OUT computeIfAbsent(Object key, Function<Object,OUT> fn) throws Throwable;
}
