package com.oath.cyclops.react.threads;

import java.util.concurrent.Executors;

import cyclops.async.LazyReact;
import cyclops.async.SimpleReact;

/**
 *
 * A ReactPool of each type for sequential Streams
 *
 * @author johnmcclean
 *
 */
public class SequentialElasticPools {
    public final static ReactPool<SimpleReact> simpleReact = ReactPool.elasticPool(() -> new SimpleReact(
                                                                                                         Executors.newFixedThreadPool(1)));
    public final static ReactPool<LazyReact> lazyReact = ReactPool.elasticPool(() -> new LazyReact(
                                                                                                   Executors.newFixedThreadPool(1)));
}
