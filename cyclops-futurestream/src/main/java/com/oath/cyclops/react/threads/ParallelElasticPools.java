package com.oath.cyclops.react.threads;

import java.util.concurrent.ForkJoinPool;

import com.oath.cyclops.async.LazyReact;
import com.oath.cyclops.async.SimpleReact;

/**
 * A ReactPool of each type for parallel Streams
 * Thread pool will be sized to number of processors
 *
 * @author johnmcclean
 *
 */
public class ParallelElasticPools {
    public final static ReactPool<SimpleReact> simpleReact = ReactPool.elasticPool(() -> new SimpleReact(
                                                                                                         new ForkJoinPool(
                                                                                                                          Runtime.getRuntime()
                                                                                                                                 .availableProcessors())));
    public final static ReactPool<LazyReact> lazyReact = ReactPool.elasticPool(() -> new LazyReact(
                                                                                                   new ForkJoinPool(
                                                                                                                    Runtime.getRuntime()
                                                                                                                           .availableProcessors())));
}
