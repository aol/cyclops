package com.aol.simple.react.threads;

import java.util.concurrent.Executors;

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
	public final static ReactPool<SimpleReact> simpleReact = ReactPool.elasticPool(()->new SimpleReact(Executors.newFixedThreadPool(1)));
	public final static ReactPool<LazyReact> lazyReact = ReactPool.elasticPool(()->new LazyReact(Executors.newFixedThreadPool(1)));
}
