package com.aol.simple.react.threads;

import java.util.concurrent.ForkJoinPool;

import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;

/**
 * A ReactPool of each type for parallel Streams
 * Thread pool will be sized to number of processors
 * 
 * @author johnmcclean
 *
 */
public class ParallelElasticPools {
	public final static ReactPool<SimpleReact> simpleReact = ReactPool.elasticPool(()->new SimpleReact(new ForkJoinPool(Runtime.getRuntime().availableProcessors())));
	public final static ReactPool<EagerReact> eagerReact = ReactPool.elasticPool(()->new EagerReact(new ForkJoinPool(Runtime.getRuntime().availableProcessors())));
	public final static ReactPool<LazyReact> lazyReact = ReactPool.elasticPool(()->new LazyReact(new ForkJoinPool(Runtime.getRuntime().availableProcessors())));
}
