package com.aol.cyclops.util.function;

import static org.jooq.lambda.tuple.Tuple.tuple;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.util.ExceptionSoftener;

import lombok.val;

public class Memoize {

    /**
     * Convert a Supplier into one that caches it's result
     * 
     * @param s Supplier to memoise
     * @return Memoised Supplier
     */
    public static <T> Supplier<T> memoizeSupplier(Supplier<T> s) {
        Map<Object, T> lazy = new ConcurrentHashMap<>();
        return () -> lazy.computeIfAbsent("k", a -> s.get());
    }

    /**
     * Convert a Supplier into one that caches it's result
     * 
     * @param s Supplier to memoise
     * @param cache Cachable to store the results
     * @return Memoised Supplier
     */
    public static <T> Supplier<T> memoizeSupplier(Supplier<T> s, Cacheable<T> cache) {

        return () -> cache.soften()
                          .computeIfAbsent("k", a -> s.get());
    }

    /**
     * Convert a Callable into one that caches it's result
     * 
     * @param s Callable to memoise
     * @param cache Cachable to store the results
     * @return Memoised Callable
     */
    public static <T> Callable<T> memoizeCallable(Callable<T> s, Cacheable<T> cache) {

        return () -> cache.soften()
                          .computeIfAbsent("k", a -> {

                              return ExceptionSoftener.softenCallable(s)
                                                      .get();

                          });
    }

    /**
     * Convert a Callable into one that caches it's result
     * 
     * @param s Callable to memoise
     * @return Memoised Callable
     */
    public static <T> Callable<T> memoizeCallable(Callable<T> s) {
        Map<Object, T> lazy = new ConcurrentHashMap<>();
        return () -> lazy.computeIfAbsent("k", a -> {

            return ExceptionSoftener.softenCallable(s)
                                    .get();

        });
    }

    public static Runnable memoizeRunnable(Runnable r) {
        AtomicReference<Boolean> isSet = new AtomicReference<>(
                                                               false);
        Object lock = new Object();
        return () -> {
            if (isSet.get())
                return;
            synchronized (lock) {
                if (isSet.get())
                    return;
                isSet.compareAndSet(false, true);
                r.run();
            }
        };
    }

    /**
     * Convert a Function into one that caches it's result
     * 
     * @param fn Function to memoise
     * @return Memoised Function
     */
    public static <T, R> Function<T, R> memoizeFunction(Function<T, R> fn) {
        Map<T, R> lazy = new ConcurrentHashMap<>();
        return t -> lazy.computeIfAbsent(t, fn);
    }

    /**
     * Convert a Function into one that caches it's result
     * 
     * @param fn Function to memoise
     * @param cache Cachable to store the results
     * @return Memoised Function
     */
    public static <T, R> Function<T, R> memoizeFunction(Function<T, R> fn, Cacheable<R> cache) {
        return t -> (R) cache.soften()
                             .computeIfAbsent(t, (Function) fn);
    }

    /**
     * Convert a BiFunction into one that caches it's result
     * 
     * @param fn BiFunction to memoise
     * @return Memoised BiFunction
     */
    public static <T1, T2, R> BiFunction<T1, T2, R> memoizeBiFunction(BiFunction<T1, T2, R> fn) {
        val memoise2 = memoizeFunction((Tuple2<T1, T2> pair) -> fn.apply(pair.v1, pair.v2));
        return (t1, t2) -> memoise2.apply(tuple(t1, t2));
    }

    /**
     * Convert a BiFunction into one that caches it's result
     * 
     * @param fn BiFunction to memoise
     * @param cache Cachable to store the results
     * @return Memoised BiFunction
     */
    public static <T1, T2, R> BiFunction<T1, T2, R> memoizeBiFunction(BiFunction<T1, T2, R> fn, Cacheable<R> cache) {
        val memoise2 = memoizeFunction((Tuple2<T1, T2> pair) -> fn.apply(pair.v1, pair.v2), cache);
        return (t1, t2) -> memoise2.apply(tuple(t1, t2));
    }

    /**
     * Convert a TriFunction into one that caches it's result
     * 
     * @param fn TriFunction to memoise
     * @return Memoised TriFunction
     */
    public static <T1, T2, T3, R> TriFunction<T1, T2, T3, R> memoizeTriFunction(TriFunction<T1, T2, T3, R> fn) {
        val memoise2 = memoizeFunction((Tuple3<T1, T2, T3> triple) -> fn.apply(triple.v1, triple.v2, triple.v3));
        return (t1, t2, t3) -> memoise2.apply(tuple(t1, t2, t3));
    }

    /**
     * Convert a TriFunction into one that caches it's result
     * 
     * @param fn TriFunction to memoise
     * @param cache Cachable to store the results
     * @return Memoised TriFunction
     */
    public static <T1, T2, T3, R> TriFunction<T1, T2, T3, R> memoizeTriFunction(TriFunction<T1, T2, T3, R> fn, Cacheable<R> cache) {
        val memoise2 = memoizeFunction((Tuple3<T1, T2, T3> triple) -> fn.apply(triple.v1, triple.v2, triple.v3), cache);
        return (t1, t2, t3) -> memoise2.apply(tuple(t1, t2, t3));
    }

    /**
     * Convert a QuadFunction into one that caches it's result
     * 
     * @param fn QuadFunction to memoise
     * @return Memoised TriFunction
     */
    public static <T1, T2, T3, T4, R> QuadFunction<T1, T2, T3, T4, R> memoizeQuadFunction(QuadFunction<T1, T2, T3, T4, R> fn) {
        val memoise2 = memoizeFunction((Tuple4<T1, T2, T3, T4> quad) -> fn.apply(quad.v1, quad.v2, quad.v3, quad.v4));
        return (t1, t2, t3, t4) -> memoise2.apply(tuple(t1, t2, t3, t4));
    }

    /**
     * Convert a QuadFunction into one that caches it's result
     * 
     * @param fn QuadFunction to memoise
     * @param cache Cachable to store the results
     * @return Memoised TriFunction
     */
    public static <T1, T2, T3, T4, R> QuadFunction<T1, T2, T3, T4, R> memoizeQuadFunction(QuadFunction<T1, T2, T3, T4, R> fn, Cacheable<R> cache) {
        val memoise2 = memoizeFunction((Tuple4<T1, T2, T3, T4> quad) -> fn.apply(quad.v1, quad.v2, quad.v3, quad.v4), cache);
        return (t1, t2, t3, t4) -> memoise2.apply(tuple(t1, t2, t3, t4));
    }

    /**
     * Convert a Predicate into one that caches it's result
     * 
     * @param p Predicate to memoise
     * @return Memoised Predicate
     */
    public static <T> Predicate<T> memoizePredicate(Predicate<T> p) {
        Function<T, Boolean> memoised = memoizeFunction((Function<T, Boolean>) t -> p.test(t));
        return (t) -> memoised.apply(t);
    }

    /**
     * Convert a Predicate into one that caches it's result
     * 
     * @param p Predicate to memoise
     * @param cache Cachable to store the results
     * @return Memoised Predicate
     */
    public static <T> Predicate<T> memoizePredicate(Predicate<T> p, Cacheable<Boolean> cache) {
        Function<T, Boolean> memoised = memoizeFunction((Function<T, Boolean>) t -> p.test(t), cache);
        return (t) -> memoised.apply(t);
    }

}
