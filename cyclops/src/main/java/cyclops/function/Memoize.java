package cyclops.function;

import static cyclops.data.tuple.Tuple.tuple;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;

import com.oath.cyclops.util.box.LazyImmutable;
import com.oath.cyclops.util.ExceptionSoftener;

import lombok.val;

public class Memoize {

    /**
     * Convert a Supplier into one that caches it's result
     *
     * @param s Supplier to memoise
     * @return Memoised Supplier
     */
    public static <T> Function0<T> memoizeSupplier(final Supplier<T> s) {
        final Map<Object, T> lazy = new ConcurrentHashMap<>();
        return () -> lazy.computeIfAbsent("k", a -> s.get());
    }

    /**
     * Convert a Supplier into one that caches it's result
     *
     * @param s Supplier to memoise
     * @param cache Cachable to store the results
     * @return Memoised Supplier
     */
    public static <T> Function0<T> memoizeSupplier(final Supplier<T> s, final Cacheable<T> cache) {

        return () -> cache.soften()
                          .computeIfAbsent("k", a -> s.get());
    }
    /**
     * Memoize a Supplier and update the cached values asynchronously using the provided Scheduled Executor Service
     * Does not support null keys
     *
     * @param fn Supplier to Memoize
     * @param ex Scheduled Executor Service
     * @param updateRateInMillis Time in millis between async updates
     * @param <R> Return type of Function
     * @return Memoized asynchronously updating function
     */
    public static <R> Function0<R> memoizeSupplierAsync(final Supplier<R> fn, ScheduledExecutorService ex, long updateRateInMillis){
        return ()-> Memoize.memoizeFunctionAsync(a-> fn.get(),ex,updateRateInMillis)
                           .apply("k");
    }
    /**
     * Memoize a Supplier and update the cached values asynchronously using the provided Scheduled Executor Service
     * Does not support null keys
     *
     * @param fn Supplier to Memoize
     * @param ex Scheduled Executor Service
     * @param cron Expression to determine when async updates occur
     * @param <R> Return type of Function
     * @return Memoized asynchronously updating function
     */
    public static <R> Function0<R> memoizeSupplierAsync(final Supplier<R> fn, ScheduledExecutorService ex, String cron){
        return ()-> Memoize.memoizeFunctionAsync(a-> fn.get(),ex,cron)
                .apply("k");
    }

    /**
     * Convert a Callable into one that caches it's result
     *
     * @param s Callable to memoise
     * @param cache Cachable to store the results
     * @return Memoised Callable
     */
    public static <T> Callable<T> memoizeCallable(final Callable<T> s, final Cacheable<T> cache) {

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
    public static <T> Callable<T> memoizeCallable(final Callable<T> s) {
        final Map<Object, T> lazy = new ConcurrentHashMap<>();
        return () -> lazy.computeIfAbsent("k", a -> {

            return ExceptionSoftener.softenCallable(s)
                                    .get();

        });
    }

    public static Runnable memoizeRunnable(final Runnable r) {
        final AtomicReference<Boolean> isSet = new AtomicReference<>(
                                                                     false);
        final Object lock = new Object();
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
    public static <T, R> Function1<T, R> memoizeFunction(final Function<T, R> fn) {
        final Map<T, R> lazy = new ConcurrentHashMap<>();
        LazyImmutable<R> nullR = LazyImmutable.def();
        return t -> t==null? nullR.computeIfAbsent(()->fn.apply(null)) : lazy.computeIfAbsent(t, fn);
    }

    /**
     * Memoize a function and update the cached values asynchronously using the provided Scheduled Executor Service
     * Does not support null keys
     *
     * @param fn Function to Memoize
     * @param ex Scheduled Executor Service
     * @param updateRateInMillis Time in millis between async updates
     * @param <T> Input Type of Function
     * @param <R> Return type of Function
     * @return Memoized asynchronously updating function
     */
    public static <T, R> Function1<T, R> memoizeFunctionAsync(final Function<T, R> fn, ScheduledExecutorService ex, long updateRateInMillis){
        final Map<T, R> lazy = new ConcurrentHashMap<>();

        ReactiveSeq.generate(()->{

            lazy.forEach((k,v)->{

                lazy.put(k,fn.apply(k));
            });
            return null;
        }).scheduleFixedRate(updateRateInMillis,ex);

        return t -> lazy.computeIfAbsent(t, fn);
    }
    /**
     * Memoize this function and update cached values on a schedule
     * Does not support null keys
     *
     * @param fn  Function to Memoize
     * @param ex Scheduled Executor Service
     * @param cron Cron expression for updating cached values asynchonrously
     * @param <T> Input Type of Function
     * @param <R> Return type of Function
     * @return Memoized asynchronously updating function
     */
    public static <T, R> Function1<T, R> memoizeFunctionAsync(final Function<T, R> fn, ScheduledExecutorService ex, String cron) {
        final Map<T, R> lazy = new ConcurrentHashMap<>();

        ReactiveSeq.generate(()->{

            lazy.forEach((k,v)->{

                lazy.put(k,fn.apply(k));
            });
            return null;
        }).schedule(cron,ex);

        return t -> lazy.computeIfAbsent(t, fn);
    }


    /**
     * Convert a Function into one that caches it's result
     *
     * @param fn Function to memoise
     * @param cache Cachable to store the results
     * @return Memoised Function
     */
    public static <T, R> Function1<T, R> memoizeFunction(final Function<T, R> fn, final Cacheable<R> cache) {
        LazyImmutable<R> nullR = LazyImmutable.def();
        return t -> t==null? nullR.computeIfAbsent(()->fn.apply(null)) : (R)cache.soften()
                         .computeIfAbsent(t, (Function) fn);
    }

    /**
     * Memoize a function and update the cached values asynchronously using the provided Scheduled Executor Service
     * Does not support null keys
     *
     * @param fn Function to Memoize
     * @param ex Scheduled Executor Service
     * @param updateRateInMillis Time in millis between async updates
     * @return Memoized asynchronously updating function
     */
    public static <T1, T2, R> Function2<T1, T2, R> memoizeBiFunctionAsync(final BiFunction<T1, T2, R> fn, ScheduledExecutorService ex, long updateRateInMillis) {
        val memoise2 = memoizeFunctionAsync((final Tuple2<T1, T2> pair) -> fn.apply(pair._1(), pair._2()),ex,updateRateInMillis);
        return (t1, t2) -> memoise2.apply(tuple(t1, t2));
    }
    /**
     * Memoize this function and update cached values on a schedule
     * Does not support null keys
     *
     * @param fn  Function to Memoize
     * @param ex Scheduled Executor Service
     * @param cron Cron expression for updating cached values asynchonrously
     * @return Memoized asynchronously updating function
     */
    public static <T1, T2, R> Function2<T1, T2, R> memoizeBiFunctionAsync(final BiFunction<T1, T2, R> fn, ScheduledExecutorService ex, String cron) {
        val memoise2 = memoizeFunctionAsync((final Tuple2<T1, T2> pair) -> fn.apply(pair._1(), pair._2()),ex,cron);
        return (t1, t2) -> memoise2.apply(tuple(t1, t2));
    }

    /**
     * Convert a BiFunction into one that caches it's result
     *
     * @param fn BiFunction to memoise
     * @return Memoised BiFunction
     */
    public static <T1, T2, R> Function2<T1, T2, R> memoizeBiFunction(final BiFunction<T1, T2, R> fn) {
        val memoise2 = memoizeFunction((final Tuple2<T1, T2> pair) -> fn.apply(pair._1(), pair._2()));
        return (t1, t2) -> memoise2.apply(tuple(t1, t2));
    }

    /**
     * Convert a BiFunction into one that caches it's result
     *
     * @param fn BiFunction to memoise
     * @param cache Cachable to store the results
     * @return Memoised BiFunction
     */
    public static <T1, T2, R> Function2<T1, T2, R> memoizeBiFunction(final BiFunction<T1, T2, R> fn, final Cacheable<R> cache) {
        val memoise2 = memoizeFunction((final Tuple2<T1, T2> pair) -> fn.apply(pair._1(), pair._2()), cache);
        return (t1, t2) -> memoise2.apply(tuple(t1, t2));
    }

    /**
     * Convert a TriFunction into one that caches it's result
     *
     * @param fn TriFunction to memoise
     * @return Memoised TriFunction
     */
    public static <T1, T2, T3, R> Function3<T1, T2, T3, R> memoizeTriFunction(final Function3<T1, T2, T3, R> fn) {
        val memoise2 = memoizeFunction((final Tuple3<T1, T2, T3> triple) -> fn.apply(triple._1(), triple._2(), triple._3()));
        return (t1, t2, t3) -> memoise2.apply(tuple(t1, t2, t3));
    }

    /**
     * Memoize this function and update cached values on a schedule
     * Does not support null keys
     *
     * @param fn  Function to Memoize
     * @param ex Scheduled Executor Service
     * @param cron Cron expression for updating cached values asynchonrously
     * @return Memoized asynchronously updating function
     */
    public static <T1, T2, T3, R> Function3<T1, T2, T3, R> memoizeTriFunctionAsync(final Function3<T1, T2, T3, R> fn, ScheduledExecutorService ex, String cron) {
        val memoise2 = memoizeFunctionAsync((final Tuple3<T1, T2, T3> triple) -> fn.apply(triple._1(), triple._2(), triple._3()),ex,cron);
        return (t1, t2, t3) -> memoise2.apply(tuple(t1, t2, t3));
    }
    /**
     * Convert a TriFunction into one that caches it's result
     *
     * @param fn TriFunction to memoise
     * @param cache Cachable to store the results
     * @return Memoised TriFunction
     */
    public static <T1, T2, T3, R> Function3<T1, T2, T3, R> memoizeTriFunction(final Function3<T1, T2, T3, R> fn, final Cacheable<R> cache) {
        val memoise2 = memoizeFunction((final Tuple3<T1, T2, T3> triple) -> fn.apply(triple._1(), triple._2(), triple._3()), cache);
        return (t1, t2, t3) -> memoise2.apply(tuple(t1, t2, t3));
    }
    /**
     * Memoize a function and update the cached values asynchronously using the provided Scheduled Executor Service
     * Does not support null keys
     *
     * @param fn Function to Memoize
     * @param ex Scheduled Executor Service
     * @param updateRateInMillis Time in millis between async updates
     * @return Memoized asynchronously updating function
     */
    public static <T1, T2, T3, R> Function3<T1, T2, T3, R> memoizeTriFunctionAsync(final Function3<T1, T2, T3, R> fn, ScheduledExecutorService ex, long updateRateInMillis) {
        val memoise2 = memoizeFunctionAsync((final Tuple3<T1, T2, T3> triple) -> fn.apply(triple._1(), triple._2(), triple._3()),ex,updateRateInMillis);
        return (t1, t2, t3) -> memoise2.apply(tuple(t1, t2, t3));
    }
    /**
     * Convert a QuadFunction into one that caches it's result
     *
     * @param fn QuadFunction to memoise
     * @return Memoised TriFunction
     */
    public static <T1, T2, T3, T4, R> Function4<T1, T2, T3, T4, R> memoizeQuadFunction(final Function4<T1, T2, T3, T4, R> fn) {
        val memoise2 = memoizeFunction((final Tuple4<T1, T2, T3, T4> quad) -> fn.apply(quad._1(), quad._2(), quad._3(), quad._4()));
        return (t1, t2, t3, t4) -> memoise2.apply(tuple(t1, t2, t3, t4));
    }
    /**
     * Memoize this function and update cached values on a schedule
     * Does not support null keys
     *
     * @param fn  Function to Memoize
     * @param ex Scheduled Executor Service
     * @param cron Cron expression for updating cached values asynchonrously
     * @return Memoized asynchronously updating function
     */
    public static <T1, T2, T3, T4, R> Function4<T1, T2, T3, T4, R> memoizeQuadFunctionAsync(final Function4<T1, T2, T3, T4, R> fn, ScheduledExecutorService ex, String cron) {
        val memoise2 = memoizeFunctionAsync((final Tuple4<T1, T2, T3, T4> quad) -> fn.apply(quad._1(), quad._2(), quad._3(), quad._4()),ex,cron);
        return (t1, t2, t3, t4) -> memoise2.apply(tuple(t1, t2, t3, t4));
    }


    /**
     * Convert a QuadFunction into one that caches it's result
     *
     * @param fn QuadFunction to memoise
     * @param cache Cachable to store the results
     * @return Memoised TriFunction
     */
    public static <T1, T2, T3, T4, R> Function4<T1, T2, T3, T4, R> memoizeQuadFunction(final Function4<T1, T2, T3, T4, R> fn,
                                                                                       final Cacheable<R> cache) {
        val memoise2 = memoizeFunction((final Tuple4<T1, T2, T3, T4> quad) -> fn.apply(quad._1(), quad._2(), quad._3(), quad._4()), cache);
        return (t1, t2, t3, t4) -> memoise2.apply(tuple(t1, t2, t3, t4));
    }
    /**
     * Memoize a function and update the cached values asynchronously using the provided Scheduled Executor Service
     * Does not support null keys
     *
     * @param fn Function to Memoize
     * @param ex Scheduled Executor Service
     * @param updateRateInMillis Time in millis between async updates
     * @return Memoized asynchronously updating function
     */
    public static <T1, T2, T3, T4, R> Function4<T1, T2, T3, T4, R> memoizeQuadFunctionAsync(final Function4<T1, T2, T3, T4, R> fn, ScheduledExecutorService ex, long updateRateInMillis) {
        val memoise2 = memoizeFunctionAsync((final Tuple4<T1, T2, T3, T4> quad) -> fn.apply(quad._1(), quad._2(), quad._3(), quad._4()),ex,updateRateInMillis);
        return (t1, t2, t3, t4) -> memoise2.apply(tuple(t1, t2, t3, t4));
    }

    /**
     * Convert a Predicate into one that caches it's result
     *
     * @param p Predicate to memoise
     * @return Memoised Predicate
     */
    public static <T> Predicate<T> memoizePredicate(final Predicate<T> p) {
        final Function<T, Boolean> memoised = memoizeFunction((Function<T, Boolean>) t -> p.test(t));
        LazyImmutable<Boolean> nullR = LazyImmutable.def();
        return (t) ->  t==null? nullR.computeIfAbsent(()->p.test(null)) : memoised.apply(t);
    }

    /**
     * Convert a Predicate into one that caches it's result
     *
     * @param p Predicate to memoise
     * @param cache Cachable to store the results
     * @return Memoised Predicate
     */
    public static <T> Predicate<T> memoizePredicate(final Predicate<T> p, final Cacheable<Boolean> cache) {
        final Function<T, Boolean> memoised = memoizeFunction((Function<T, Boolean>) t -> p.test(t), cache);
        LazyImmutable<Boolean> nullR = LazyImmutable.def();
        return (t) -> t==null? nullR.computeIfAbsent(()->p.test(null)) : memoised.apply(t);
    }

}
