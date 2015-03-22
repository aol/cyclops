package com.aol.simple.react.threads;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;

/**
 * 
 * A ReactPool of each type for sequential Streams
 *
 * @author johnmcclean
 *
 */
public class SequentialElasticPools {
	public final ReactPool<SimpleReact> simpleReact = ReactPool.elasticPool(()->new SimpleReact(Executors.newFixedThreadPool(1)));
	public final ReactPool<EagerReact> eagerReact = ReactPool.elasticPool(()->new EagerReact(Executors.newFixedThreadPool(1)));
	public final ReactPool<LazyReact> lazyReact = ReactPool.elasticPool(()->new LazyReact(Executors.newFixedThreadPool(1)));
}
