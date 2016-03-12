package com.aol.cyclops.types.stream.reactive;

import java.util.concurrent.Executor;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.async.QueueFactories;
import com.aol.cyclops.data.async.QueueFactory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class FlatMapConfig<T>{
    public final QueueFactory<T> factory;
    public final Maybe<Executor> ex;
    
    public static <T> FlatMapConfig<T> unbounded(Executor ex){
        return new FlatMapConfig<T>(QueueFactories.unboundedNonBlockingQueue(),Maybe.just(ex));
    }
    public static <T> FlatMapConfig<T> unbounded(){
        return new FlatMapConfig<T>(QueueFactories.unboundedNonBlockingQueue(),Maybe.none());
    }
    public static <T> FlatMapConfig<T> maxConcurrent(int maxConcurrent,Executor ex){
        return new FlatMapConfig<T>(QueueFactories.<T>boundedQueue(maxConcurrent),Maybe.just(ex));
    }
    public static <T> FlatMapConfig<T> maxConcurrent(int maxConcurrent){
        return new FlatMapConfig<T>(QueueFactories.<T>boundedQueue(maxConcurrent),Maybe.none());
    }
    public static <T> FlatMapConfig<T> defaultConfig(){
        return new FlatMapConfig<T>(QueueFactories.<T>boundedQueue(5_000),Maybe.none());
    }
}