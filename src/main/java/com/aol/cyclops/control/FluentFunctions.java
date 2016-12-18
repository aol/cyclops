package com.aol.cyclops.control;

import com.aol.cyclops.data.MutableInt;
import com.aol.cyclops.internal.invokedynamic.CheckedTriFunction;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.util.ExceptionSoftener;
import com.aol.cyclops.util.function.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.jooq.lambda.fi.util.function.*;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.*;

/**
 * Fluent API for working with java.util.Function types
 * 
 * Supports 
 *      caching (memoization)
 *      aspects (before, after, around)
 *      logging
 *      retry
 *      recovery
 *      Async execution
 *      Reader monad
 *      Partial application
 *      Currying
 *      Pattern Matching
 *   
 * 
 * @author johnmcclean
 *
 */
public class FluentFunctions {

    /**
     * Construct a FluentSupplier from a checked Supplier
     * <pre>
     * {@code
     * FluentFunctions.ofChecked(this::exceptionalFirstTime)
    					.recover(IOException.class, ()->"hello boo!")
    					.println()
    					.get()
     * 
     * }
     * </pre>
     * @param supplier that throws CheckedExcpetion
     * @return FluentSupplier
     */
    public static <R> FluentFunctions.FluentSupplier<R> ofChecked(final CheckedSupplier<R> supplier) {
        return FluentFunctions.of(ExceptionSoftener.softenSupplier(supplier));
    }

    /**
     * Construct a FluentSupplier from a Supplier
     * 
     * <pre>
     * {@code 
     * Cache<Object, Integer> cache = CacheBuilder.newBuilder()
    		       .maximumSize(1000)
    		       .expireAfterWrite(10, TimeUnit.MINUTES)
    		       .build();
    
    	called=0;
    	Supplier<Integer> fn = FluentFunctions.of(this::getOne)
    												  .name("myFunction")
    												  .memoize((key,f)->cache.get(key,()->f.apply(key)));
    	fn.get();
    	fn.get();
    	fn.get();
    	
    	//called == 1
     * 
     * }
     * </pre>
     * @param supplier to make Fluent
     * @return FluentSupplier
     */
    public static <R> FluentFunctions.FluentSupplier<R> of(final Supplier<R> supplier) {
        return new FluentSupplier<>(
                                    supplier);
    }

    /**
     * Construct a FluentFunction from a CheckedFunction
     * 
     * <pre>
     * {@code 
     * FluentFunctions.ofChecked(this::exceptionalFirstTime)
    					.recover(IOException.class, in->in+"boo!")
    					.println()
    					.apply("hello ")
     * 
     * 
     * }
     * </pre>
     * @param fn CheckedFunction
     * @return FluentFunction
     */
    public static <T, R> FluentFunctions.FluentFunction<T, R> ofChecked(final CheckedFunction<T, R> fn) {
        return FluentFunctions.of(ExceptionSoftener.softenFunction(fn));
    }

    /**
     * Construct a FluentFunction from a Function
     * 
     * <pre>
     * {@code 
     * FluentFunctions.of(this::addOne)
    				   .around(advice->advice.proceed(advice.param+1))
    				   .println()
    				   .apply(10)
     * }
     * </pre>
     * @param fn Function
     * @return FluentFunction
     */
    public static <T, R> FluentFunctions.FluentFunction<T, R> of(final Function<T, R> fn) {
        return new FluentFunction<>(
                                    fn);
    }

    /**
     * Construct a FluentBiFunction from a CheckedBiFunction
     * 
     * <pre>
     * {@code 
     * FluentFunctions.ofChecked(this::exceptionalFirstTime)
    				   .println()
    				   .retry(2,500)
    				   .apply("hello","woo!")
     * }
     * </pre>
     * @param fn CheckedBiFunction
     * @return FluentBiFunction
     */
    public static <T1, T2, R> FluentFunctions.FluentBiFunction<T1, T2, R> ofChecked(final CheckedBiFunction<T1, T2, R> fn) {
        return FluentFunctions.of(ExceptionSoftener.softenBiFunction(fn));
    }

    /**
     * Convert a BiFunction to a FluentBiFunction
     * <pre>
     * {@code 
     * CompletableFuture<Integer> cf = FluentFunctions.of(this::add)
    												  .liftAsync(ex)
    												  .apply(1,1)
     * 
     * }
     * </pre>
     * @param fn BiFunction to convert
     * @return FluentBiFuntion
     */
    public static <T1, T2, R> FluentFunctions.FluentBiFunction<T1, T2, R> of(final BiFunction<T1, T2, R> fn) {
        return new FluentBiFunction<>(
                                      fn);
    }

    /**
     * Convert a CheckedTriFunction to a FluentTriFunction
     * <pre>
     * {@code
       FluentFunctions.ofChecked(this::exceptionalFirstTime)
    				   .println()
    				   .retry(2,500)
    				   .apply("hello","woo!","h")
    				   
       } 
       </pre>
     * @param fn CheckedTriFunction to convert
     * @return FluentTriFunction
     */
    public static <T1, T2, T3, R> FluentFunctions.FluentTriFunction<T1, T2, T3, R> ofChecked(final CheckedTriFunction<T1, T2, T3, R> fn) {

        return new FluentTriFunction<>(
                                       softenTriFunction(fn));
    }

    /**
     * Convert a CheckedTriFunction to a FluentTriFunction
     * <pre>
     * {@code
       FluentFunctions.of(this::add)	
    				  .matches(-1,c->c.hasValues(3).then(i->3))
    				  .apply(1,1,1)		   
       }  
       </pre>
     * @param fn TriFunction to convert
     * @return FluentTriFunction
     */
    public static <T1, T2, T3, R> FluentFunctions.FluentTriFunction<T1, T2, T3, R> of(final F3<T1, T2, T3, R> fn) {
        return new FluentTriFunction<>(
                                       fn);
    }

    /**
     * Convert a statement (e.g. a method or Consumer with no return value) to an Expression. The input is returned as output
     * <pre>
     * {@code 
     * FluentFunctions.expression(System.out::println)
    				   .apply("hello");
    	
    	//hello
     * }
     * </pre>
     * @param action Consumer
     * @return FluentFunction
     */
    public static <T> FluentFunctions.FluentFunction<T, T> expression(final Consumer<? super T> action) {
        return FluentFunctions.of(t -> {
            action.accept(t);
            return t;
        });
    }

    /**
     * Convert a checked statement (e.g. a method or Consumer with no return value that throws a Checked Exception) to a 
     * fluent expression (FluentFunction).  The input is returned as output
     * <pre>
     * {@code 
     * public void print(String input) throws IOException{
    	  System.out.println(input);
        }
        FluentFunctions.checkedExpression(this::print)
    			   .apply("hello")
     * }
     * </pre>
     * @param action
     * @return FluentFunction
     */
    public static <T> FluentFunctions.FluentFunction<T, T> checkedExpression(final CheckedConsumer<T> action) {
        final Consumer<T> toUse = ExceptionSoftener.softenConsumer(action);
        return FluentFunctions.of(t -> {
            toUse.accept(t);
            return t;
        });
    }

    /**
     * Convert a BiConsumer into a FluentBiFunction that returns it's input in a Tuple
     * <pre>
     * {@code 
     * public void withTwo(Integer a,Integer b){
    	 System.out.println(a+b);
    	}
    	FluentFunctions.expression(this::withTwo)
    				   .apply(1,2)
    				   
    	//returns Tuple2[1,2]
     * 
     * }
     * </pre>
     * @param action BiConsumer
     * @return FluentBiFunction
     */
    public static <T1, T2> FluentFunctions.FluentBiFunction<T1, T2, Tuple2<T1, T2>> expression(final BiConsumer<? super T1, ? super T2> action) {
        return FluentFunctions.of((t1, t2) -> {
            action.accept(t1, t2);
            return Tuple.tuple(t1, t2);
        });
    }

    /**
     * Convert a CheckedBiConsumer into a FluentBiConsumer that returns it's input in a tuple
     * 
     * <pre>
     * {@code 
     * public void printTwo(String input1,String input2) throws IOException{
    	System.out.println(input1);
    	System.out.println(input2);
       }
     * 
     * FluentFunctions.checkedExpression(this::printTwo)
    				   .apply("hello","world");
    				   
    	//returns Tuple2["hello","world"]		   
     * }
     * </pre>
     * @param action
     * @return
     */
    public static <T1, T2> FluentFunctions.FluentBiFunction<T1, T2, Tuple2<T1, T2>> checkedExpression(final CheckedBiConsumer<T1, T2> action) {
        final BiConsumer<T1, T2> toUse = ExceptionSoftener.softenBiConsumer(action);
        return FluentFunctions.of((t1, t2) -> {
            toUse.accept(t1, t2);
            return Tuple.tuple(t1, t2);
        });
    }

    private static <T1, T2, T3, R> F3<T1, T2, T3, R> softenTriFunction(final CheckedTriFunction<T1, T2, T3, R> fn) {
        return (t1, t2, t3) -> {
            try {
                return fn.apply(t1, t2, t3);
            } catch (final Throwable e) {
                throw ExceptionSoftener.throwSoftenedException(e);
            }
        };
    }

    @Wither(AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class FluentSupplier<R> implements Supplier<R> {
        private final Supplier<R> fn;
        private final String name;

        public FluentSupplier(final Supplier<R> fn) {
            this.name = null;
            this.fn = fn;
        }

        /* (non-Javadoc)
         * @see java.util.function.Supplier#get()
         */
        @Override
        public R get() {
            return fn.get();
        }

        /**
         * Apply before advice to this Supplier
         * 
         * @param r Runnable that represents before advice (AOP)
         * @return Returns a new Supplier with before advice attached
         */
        public FluentSupplier<R> before(final Runnable r) {
            return withFn(() -> {
                r.run();
                return fn.get();
            });
        }

        /**
         * Apply after advice to this Supplier
         * 
         * @param action Runnable that represents after advice (AOP)
         * @return Returns a new Supplier with after advice attached
         */
        public FluentSupplier<R> after(final Consumer<R> action) {
            return withFn(() -> {
                final R result = fn.get();
                action.accept(result);
                return result;
            });
        }

        /**
         * Apply around advice to this Supplier
         * 
         * @param around Function that captures input to this Supplier and can optionally pass on the call
         * @return Supplier with around advice attached
         */
        public FluentSupplier<R> around(final Function<Advice0<R>, R> around) {
            return withFn(() -> around.apply(new Advice0<R>(
                                                            fn)));
        }

        /**
         * @return A caching version of this Supplier
         */
        public FluentSupplier<R> memoize() {
            return withFn(Memoize.memoizeSupplier(fn));
        }

        /**
         * @param cache A cache implementation wrapper
         * @return A caching version using the attached cache implemenation wrapper
         */
        public FluentSupplier<R> memoize(final Cacheable<R> cache) {
            return withFn(Memoize.memoizeSupplier(fn, cache));
        }

        /**
         * @param name To give this supplier
         * @return A supplier with a name (useful for logging purposes)
         */
        public FluentSupplier<R> name(final String name) {
            return this.withName(name);
        }

        private String handleNameStart() {
            return name == null ? "(fluent-supplier-" : "(" + name + "-";

        }

        private String handleNameEnd() {
            return ")";

        }

        /**
         * @return A supplier that prints out it's result or error on failure
         */
        public FluentSupplier<R> println() {
            return log(s -> System.out.println(s), t -> t.printStackTrace());

        }

        /**
         * A supplier that logs it's success or error states to the provided Consumers
         * 
         * @param logger Success logger
         * @param error Failure logger
         * @return Supplier that logs it's state
         */
        public FluentSupplier<R> log(final Consumer<String> logger, final Consumer<Throwable> error) {
            return FluentFunctions.of(() -> {
                try {
                    final R result = fn.get();
                    logger.accept(handleNameStart() + "Result[" + result + "]" + handleNameEnd());
                    return result;
                } catch (final Throwable t) {
                    error.accept(t);
                    throw ExceptionSoftener.throwSoftenedException(t);
                }
            });
        }

        /**
         * A supplier that can recover from the specified exception types, using the provided Supplier
         * 
         * @param type Recoverable exception types
         * @param onError Supplier to use on error
         * @return Supplier capable of error recovery
         */
        public <X extends Throwable> FluentSupplier<R> recover(final Class<X> type, final Supplier<R> onError) {
            return FluentFunctions.of(() -> {
                try {
                    return fn.get();
                } catch (final Throwable t) {
                    if (type.isAssignableFrom(t.getClass())) {
                        return onError.get();
                    }
                    throw ExceptionSoftener.throwSoftenedException(t);

                }
            });

        }

        /**
         * A supplier capable of retrying on failure using an exponential backoff strategy
         * 
         * @param times Number of times to retry 
         * @param backoffStartTime Wait time before first retry
         * @return Supplier with a retry strategy
         */
        public FluentSupplier<R> retry(final int times, final int backoffStartTime) {
            return FluentFunctions.of(() -> {
                int count = times;
                final MutableInt sleep = MutableInt.of(backoffStartTime);
                Throwable exception = null;
                while (count-- > 0) {
                    try {
                        return fn.get();
                    } catch (final Throwable e) {
                        exception = e;
                    }
                    ExceptionSoftener.softenRunnable(() -> Thread.sleep(sleep.get()));

                    sleep.mutate(s -> s * 2);
                }
                throw ExceptionSoftener.throwSoftenedException(exception);

            });

        }



        /**
         * @return A stream generated from this suppliers Value
         */
        public ReactiveSeq<R> generate() {
            return ReactiveSeq.generate(fn);
        }

        /**
         * @return A Supplier that returns it's value wrapped in an Optional
         */
        public FluentSupplier<Optional<R>> lift() {
            return new FluentSupplier<>(
                                        () -> Optional.ofNullable(fn.get()));
        }

        /**
         * @param classes To catch exceptions for
         * @return A Supplier that returns it's value wrapped in a Try
         */
        public <X extends Throwable> FluentSupplier<Try<R, X>> liftTry(final Class<X>... classes) {
            return FluentFunctions.of(() -> Try.withCatch(() -> fn.get(), classes));
        }

        /**
         * @return A Supplier that returns it's value wrapped in an Optional inside an AnyM
         */
        public   FluentSupplier<AnyM<Witness.maybe,R>> liftF() {
            return new FluentSupplier<>(
                                        () -> AnyM.fromMaybe(Maybe.ofNullable(get())));
        }

        /**
         * @param ex Executor to execute this Supplier on
         * @return A Supplier that returns it's value wrapped in a CompletableFuture, populated asyncrhonously
         */
        public FluentSupplier<CompletableFuture<R>> liftAsync(final Executor ex) {
            return FluentFunctions.of(() -> CompletableFuture.supplyAsync(fn, ex));
        }

        /**
         * @param ex Executor to execute this Supplier on
         * @return The value generated by this Supplier in a CompletableFuture executed Asynchronously
         */
        public CompletableFuture<FluentSupplier<R>> async(final Executor ex) {
            return CompletableFuture.supplyAsync(() -> FluentFunctions.of(fn), ex);
        }

    }

    @Wither(AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class FluentFunction<T, R> implements Function<T, R>, Reader<T, R> {
        private final Function<T, R> fn;
        private final String name;

        public FluentFunction(final Function<T, R> fn) {
            this.name = null;
            this.fn = fn;
        }

        /* (non-Javadoc)
         * @see java.util.function.Function#apply(java.lang.Object)
         */
        @Override
        public R apply(final T t) {
            return fn.apply(t);
        }

        /* (non-Javadoc)
         * @see com.aol.cyclops.control.Reader#map(java.util.function.Function)
         */
        @Override
        public <R1> FluentFunction<T, R1> map(final Function<? super R, ? extends R1> f2) {
            return FluentFunctions.of(fn.andThen(f2));
        }

        /* (non-Javadoc)
         * @see com.aol.cyclops.control.Reader#flatMap(java.util.function.Function)
         */
        @Override
        public <R1> FluentFunction<T, R1> flatMap(final Function<? super R, ? extends Reader<T, R1>> f) {
            return FluentFunctions.of(a -> f.apply(fn.apply(a))
                                            .apply(a));
        }

        /**
         * Apply before advice to this function, capture the input with the provided Consumer
         * 
         * @param action Before advice
         * @return Function with Before advice attached
         */
        public FluentFunction<T, R> before(final Consumer<T> action) {
            return withFn(t -> {
                action.accept(t);
                return fn.apply(t);
            });
        }

        /**
         * Apply After advice to this function capturing both the input and the output with the provided BiConsumer
         * 
         * @param action After advice
         * @return  Function with After advice attached
         */
        public FluentFunction<T, R> after(final BiConsumer<T, R> action) {
            return withFn(t -> {

                final R result = fn.apply(t);
                action.accept(t, result);
                return result;
            });
        }

        /**
         * Apply around advice to this function, captures input allows output to be controlled by the advice
         * 
         * @param around Around advice
         * @return Function with Around advice attached
         */
        public FluentFunction<T, R> around(final Function<Advice1<T, R>, R> around) {
            return withFn(t -> around.apply(new Advice1<T, R>(
                                                              t, fn)));
        }

        /**
         * Partially apply the provided value to this function, to turn it into a Supplier
         * 
         * @param param Input param to apply to this function
         * @return Supplier generated by partially applying the input param to this function
         */
        public FluentSupplier<R> partiallyApply(final T param) {
            return new FluentSupplier<>(
                                        PartialApplicator.partial(param, fn));
        }

        /**
         * @return A caching (memoizing) version of this Function
         */
        public FluentFunction<T, R> memoize() {
            return withFn(Memoize.memoizeFunction(fn));
        }

        /**
         * @param cache A wrapper around an external cache implementation
         * @return A caching (memoizing) version of this Function
         */
        public FluentFunction<T, R> memoize(final Cacheable<R> cache) {
            return withFn(Memoize.memoizeFunction(fn, cache));
        }

        /**
         * @param name To give this Function
         * @return A Function with a name (useful for logging purposes)
         */
        public FluentFunction<T, R> name(final String name) {
            return this.withName(name);
        }

        private String handleNameStart() {
            return name == null ? "(fluent-function-" : "(" + name + "-";

        }

        private String handleNameEnd() {
            return ")";

        }

        /**
         *  A Function that logs it's success or error states to the provided Consumers
         * 
         * @param logger Success logger
         * @param error Failure logger
         * @return Function that logs it's state
         */
        public FluentFunction<T, R> log(final Consumer<String> logger, final Consumer<Throwable> error) {
            return FluentFunctions.of(t1 -> {

                try {
                    logger.accept(handleNameStart() + "Parameter[" + t1 + "]" + handleNameEnd());
                    final R result = fn.apply(t1);
                    logger.accept(handleNameStart() + "Result[" + result + "]" + handleNameEnd());
                    return result;
                } catch (final Throwable t) {
                    error.accept(t);
                    throw ExceptionSoftener.throwSoftenedException(t);
                }

            });
        }

        /**
         * Visit the result of this Function once it has been executed, if the Function executes successfully the
         * result will be passes to the eventConsumer, if there is an error it will be passed to the errorConsumer
         * 
         * @param eventConsumer Consumer to recieve result on successful execution
         * @param errorConsumer Consumer to recieve error on failure
         * @return Function with event vistor attached.
         */
        public FluentFunction<T, R> visitEvent(final Consumer<R> eventConsumer, final Consumer<Throwable> errorConsumer) {

            return FluentFunctions.of(t1 -> {

                try {
                    final R result = fn.apply(t1);
                    eventConsumer.accept(result);
                    return result;
                } catch (final Throwable t) {
                    errorConsumer.accept(t);
                    throw ExceptionSoftener.throwSoftenedException(t);
                }

            });
        }

        /**
         * @return Function that logs it's result or error to the console
         */
        public FluentFunction<T, R> println() {
            return log(s -> System.out.println(s), t -> t.printStackTrace());
        }

        /**
         * A Function that can recover from the specified exception types, using the provided recovery Function
         * 
         * @param type Recoverable exception types
         * @param onError Recovery function
         * @return Function capable of error recovery
         */
        public <X extends Throwable> FluentFunction<T, R> recover(final Class<X> type, final Function<T, R> onError) {
            return FluentFunctions.of(t1 -> {
                try {
                    return fn.apply(t1);
                } catch (final Throwable t) {
                    if (type.isAssignableFrom(t.getClass())) {
                        return onError.apply(t1);

                    }
                    throw ExceptionSoftener.throwSoftenedException(t);

                }
            });
        }

        /**
         *  A Function capable of retrying on failure using an exponential backoff strategy
         * 
         * @param times Number of times to retry 
         * @param backoffStartTime Wait time before first retry
         * @return Function with a retry strategy
         */
        public FluentFunction<T, R> retry(final int times, final int backoffStartTime) {
            return FluentFunctions.of(t -> {
                int count = times;
                final MutableInt sleep = MutableInt.of(backoffStartTime);
                Throwable exception = null;
                while (count-- > 0) {
                    try {
                        return fn.apply(t);
                    } catch (final Throwable e) {
                        exception = e;
                    }
                    ExceptionSoftener.softenRunnable(() -> Thread.sleep(sleep.get()));

                    sleep.mutate(s -> s * 2);
                }
                throw ExceptionSoftener.throwSoftenedException(exception);

            });

        }


        /**
         * Generate an infinite Stream that iterates from the specified seed using the currently wrapped function
         * 
         * e.g.
         * 
         * <pre>
         * {@code 
         * FluentFunctions.of(this::addOne)	
        				.iterate(95281,i->i)
        				.forEach(System.out::println);
         * 
         * 
         * //95282
           //95283
           //95284
           //95285
           //95286
         * //etc
         * }
         * </pre>
         * 
         * 
         * @param seed initial value
         * @param mapToType Convert from supplied functions return type to Stream input type
         * @return Infinite Stream
         */
        public ReactiveSeq<R> iterate(final T seed, final Function<R, T> mapToType) {
            return ReactiveSeq.iterate(fn.apply(seed), t -> fn.compose(mapToType)
                                                              .apply(t));
        }

        /**
         * @param input Input value, this function will applied to this value to generate the value that will be infinitely repeated in this Stream
         * @return An infinitely generating Stream {@link com.aol.cyclops.control.ReactiveSeq} of values determined by the application
         *        of this function to the input value
         */
        public ReactiveSeq<R> generate(final T input) {
            return ReactiveSeq.generate(() -> fn.apply(input));
        }

        /**
         * @return A Function that accepts and returns an Optional
         */
        public FluentFunction<Optional<T>, Optional<R>> lift() {
            return new FluentFunction<>(
                                        opt -> opt.map(t -> fn.apply(t)));
        }

        /**
         * @param classes To catch exceptions for
         * @return A function that executes and returns a Try as the result typ
         */
        public <X extends Throwable> FluentFunction<T, Try<R, X>> liftTry(final Class<X>... classes) {
            return FluentFunctions.of((t1) -> Try.withCatch(() -> fn.apply(t1), classes));
        }

        /**
         * @return A function that accepts and reurns an AnyM type
         */
        public  <W extends WitnessType<W>> FluentFunction<AnyM<W,T>, AnyM<W,R>> liftF() {
            return FluentFunctions.of(AnyM.liftF(fn));
        }

        /**
         * @param ex Executor to execute this function on
         * @return A Function that returns an asynchonously executing CompletableFuture
         */
        public FluentFunction<T, CompletableFuture<R>> liftAsync(final Executor ex) {
            return FluentFunctions.of(t -> CompletableFuture.supplyAsync(() -> fn.apply(t), ex));
        }

        /**
         * Move this function into an asynchronous context
         * 
         * @param ex Executor to manage operations on this function on
         * @return A CompletableFuture that contains this function
         */
        public CompletableFuture<FluentFunction<T, R>> async(final Executor ex) {
            return CompletableFuture.supplyAsync(() -> FluentFunctions.of(fn), ex);
        }

        /* (non-Javadoc)
         * @see java.util.function.Function#compose(java.util.function.Function)
         */
        @Override
        public <V> Function<V, R> compose(final Function<? super V, ? extends T> before) {

            return FluentFunctions.of(Reader.super.compose(before));
        }

        /* (non-Javadoc)
         * @see java.util.function.Function#andThen(java.util.function.Function)
         */
        @Override
        public <V> Function<T, V> andThen(final Function<? super R, ? extends V> after) {

            return FluentFunctions.of(Reader.super.andThen(after));
        }

    }

    @Wither(AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class FluentBiFunction<T1, T2, R> implements BiFunction<T1, T2, R> {
        BiFunction<T1, T2, R> fn;
        private final String name;

        public FluentBiFunction(final BiFunction<T1, T2, R> fn) {
            this.name = null;
            this.fn = fn;
        }

        /* (non-Javadoc)
         * @see java.util.function.BiFunction#apply(java.lang.Object, java.lang.Object)
         */
        @Override
        public R apply(final T1 t1, final T2 t2) {
            return fn.apply(t1, t2);
        }

        /**
         * Apply before advice to this BiFunction
         * 
         * @param action BiConsumer to recieve input parameters to BiFunction
         * @return BiFunction with before advice attached
         */
        public FluentBiFunction<T1, T2, R> before(final BiConsumer<T1, T2> action) {
            return withFn((t1, t2) -> {
                action.accept(t1, t2);
                return fn.apply(t1, t2);
            });
        }

        /**
         * Apply after advice to this BiFunction
         * 
         * @param action TriConsumer to recieve input parameters and return value from BiFunction after it has executed
         * @return BiFunction with after advice attached
         */
        public FluentBiFunction<T1, T2, R> after(final TriConsumer<T1, T2, R> action) {
            return withFn((t1, t2) -> {

                final R result = fn.apply(t1, t2);
                action.accept(t1, t2, result);
                return result;
            });
        }

        /**
         * Apply around advice to this BiFunction
         * 
         * <pre>
         * {@code 
         * public int add(Integer a,Integer b ){
               return a+b;
           }
         *    FluentFunctions.of(this::add)
                       .around(advice->advice.proceed1(advice.param1+1))
                       .println()
                       .apply(10,1)
         *    
         *   //12   (input = 10+1 with advice + 1 = 12)
         * }</pre>
         * 
         * 
         * @param around Function that allows the execution of this BiFunction to be contolled via it's input parameter
         * @return BiFunction with around advice attached
         */
        public FluentBiFunction<T1, T2, R> around(final Function<Advice2<T1, T2, R>, R> around) {
            return withFn((t1, t2) -> around.apply(new Advice2<>(
                                                                 t1, t2, fn)));
        }

        /**
         * Partially apply the provided parameter as the first parameter to this BiFunction to generate a Function (single input value)
         * 
         * @param param Input parameter to Partially Applied
         * @return A Function generated from the BiFunction with the first parameter already applied
         */
        public FluentFunction<T2, R> partiallyApply(final T1 param) {
            return new FluentFunction<>(
                                        PartialApplicator.partial2(param, fn));
        }

        /**
         * Partially apply the provided parameters to this BiFunction to generate a Supplier (that takes no inputs)
         * 
         * @param param1 First Input parameter
         * @param param2 Second Input parameter
         * @return Supplier generated from the partial application of the provided input parameters to this BiFunction
         */
        public FluentSupplier<R> partiallyApply(final T1 param1, final T2 param2) {
            return new FluentSupplier<>(
                                        PartialApplicator.partial2(param1, param2, fn));
        }

        /**
         * Curry this BiFunction, that is convert it from a BiFunction that accepts two input parameters to a 'chain'
         * of two Functions that accept a single parameter
         * 
         * <pre>
         * {@code 
         * public int add(Integer a,Integer b ){
               return a+b;
           }
         * 
         *      FluentFunctions.of(this::add)
                               .curry()
                               .apply(1)
                               .apply(2);
                               
                //3               
         *    
         * }
         * </pre>
         * 
         * @return Curried function 
         */
        public FluentFunction<T1, Function<T2, R>> curry() {
            return new FluentFunction(
                                      CurryVariance.curry2(fn));
        }

        /**
         * @return A caching (memoizing) version of this BiFunction, outputs for all inputs will be cached
         */
        public FluentBiFunction<T1, T2, R> memoize() {
            return withFn(Memoize.memoizeBiFunction(fn));
        }

        /**
         * This methods creates a caching version of this BiFunction, caching is implemented via the Cacheable wrapper,
         * that can be used to wrap any concrete cache implementation
         * 
         * E.g. to use a Guava cache for memoization
         * 
         * <pre>
         * {@code 
         * 
         * Cache<Object, Integer> cache = CacheBuilder.newBuilder()
                   .maximumSize(1000)
                   .expireAfterWrite(10, TimeUnit.MINUTES)
                   .build();
        
                   called=0;
            BiFunction<Integer,Integer,Integer> fn = FluentFunctions.of(this::add)
                                                                    .name("myFunction")
                                                                     .memoize((key,f)->cache.get(key,()->f.apply(key)));
        
            fn.apply(10,1);
            fn.apply(10,1);
            fn.apply(10,1);
        
            assertThat(called,equalTo(1));
         * 
         * 
         * 
         * }</pre>
         * 
         * 
         * @param cache Cache implementation wrapper
         * 
         * @return A caching (memoizing) version of this BiFunction, outputs for all inputs will be cached (unless ejected from the cache)
         */
        public FluentBiFunction<T1, T2, R> memoize(final Cacheable<R> cache) {
            return withFn(Memoize.memoizeBiFunction(fn));
        }

        /**
         * @param name To give this BiFunction
         * @return A BiFunction with a name (useful for logging purposes)
         */
        public FluentBiFunction<T1, T2, R> name(final String name) {
            return this.withName(name);
        }

        private String handleNameStart() {
            return name == null ? "(fluent-function-" : "(" + name + "-";

        }

        private String handleNameEnd() {
            return ")";

        }

        /**
         *  A BiFunction that logs it's success or error states to the provided Consumers
         * 
         * @param logger Success logger
         * @param error Failure logger
         * @return BiFunction that logs it's state
         */
        public FluentBiFunction<T1, T2, R> log(final Consumer<String> logger, final Consumer<Throwable> error) {
            return FluentFunctions.of((t1, t2) -> {
                try {
                    logger.accept(handleNameStart() + "Parameters[" + t1 + "," + t2 + "]" + handleNameEnd());
                    final R result = fn.apply(t1, t2);
                    logger.accept(handleNameStart() + "Result[" + result + "]" + handleNameEnd());
                    return result;
                } catch (final Throwable t) {
                    error.accept(t);
                    throw ExceptionSoftener.throwSoftenedException(t);
                }
            });
        }

        /**
         * Visit the result of this BiFunction once it has been executed, if the Function executes successfully the
         * result will be passes to the eventConsumer, if there is an error it will be passed to the errorConsumer
         * 
         * @param eventConsumer Consumer to recieve result on successful execution
         * @param errorConsumer Consumer to recieve error on failure
         * @return BiFunction with event vistor attached.
         */
        public FluentBiFunction<T1, T2, R> visitEvent(final Consumer<R> eventConsumer, final Consumer<Throwable> errorConsumer) {

            return FluentFunctions.of((t1, t2) -> {

                try {
                    final R result = fn.apply(t1, t2);
                    eventConsumer.accept(result);
                    return result;
                } catch (final Throwable t) {
                    errorConsumer.accept(t);
                    throw ExceptionSoftener.throwSoftenedException(t);
                }

            });
        }

        /**
         * @return Function that logs it's result or error to the console
         */
        public FluentBiFunction<T1, T2, R> println() {
            return log(s -> System.out.println(s), t -> t.printStackTrace());
        }

        /**
         * A BiFunction that can recover from the specified exception types, using the provided recovery Function
         * 
         * @param type Recoverable exception types
         * @param onError Recovery BiFunction
         * @return BiFunction capable of error recovery
         */
        public <X extends Throwable> FluentBiFunction<T1, T2, R> recover(final Class<X> type, final BiFunction<T1, T2, R> onError) {
            return FluentFunctions.of((t1, t2) -> {
                try {
                    return fn.apply(t1, t2);
                } catch (final Throwable t) {
                    if (type.isAssignableFrom(t.getClass())) {
                        return onError.apply(t1, t2);

                    }
                    throw ExceptionSoftener.throwSoftenedException(t);

                }
            });

        }

        /**
         *  A BiFunction capable of retrying on failure using an exponential backoff strategy
         * 
         * @param times Number of times to retry 
         * @param backoffStartTime Wait time before first retry
         * @return BiFunction with a retry strategy
         */
        public FluentBiFunction<T1, T2, R> retry(final int times, final int backoffStartTime) {
            return FluentFunctions.of((t1, t2) -> {
                int count = times;
                final MutableInt sleep = MutableInt.of(backoffStartTime);
                Throwable exception = null;
                while (count-- > 0) {
                    try {
                        return fn.apply(t1, t2);
                    } catch (final Throwable e) {
                        exception = e;
                    }
                    ExceptionSoftener.softenRunnable(() -> Thread.sleep(sleep.get()));

                    sleep.mutate(s -> s * 2);
                }
                throw ExceptionSoftener.throwSoftenedException(exception);

            });

        }



        /**
         * 
         * Generate an infinite Stream from the provided seed values and mapping function.
         * The supplied mapping function is inverted taking an input of type R and returning two outputs T1, T2 (in a Tuple)
         * 
         * <pre>
         * {@code 
         *   FluentFunctions.of(this::add) 
                            .iterate(1,2,(i)->Tuple.tuple(i,i))
                            .limit(10)
                            .printOut();
         * 
         *   //3
               6
               12
               24
               48
               96
               192
               384
               768
               1536
         * }</pre>
         * 
         * 
         * @param seed1 Initial input parameter 1
         * @param seed2 Initial input parameter 2
         * @param mapToTypeAndSplit Reversed mapping function
         * @return Infinite Stream
         */
        public ReactiveSeq<R> iterate(final T1 seed1, final T2 seed2, final Function<R, Tuple2<T1, T2>> mapToTypeAndSplit) {
            return ReactiveSeq.iterate(fn.apply(seed1, seed2), t -> {
                final Tuple2<T1, T2> tuple = mapToTypeAndSplit.apply(t);
                return fn.apply(tuple.v1, tuple.v2);
            });
        }

        /**
         * Generate an infinite Stream by applying the input parameters to this function
         * repeatedly
         * 
         * @param input1 First input parameter
         * @param input2 Second input parameter
         * @return Infinite Stream
         */
        public ReactiveSeq<R> generate(final T1 input1, final T2 input2) {
            return ReactiveSeq.generate(() -> fn.apply(input1, input2));
        }

        /**
         * @return A BiFunction that accepts and returns Optionals
         */
        public FluentBiFunction<Optional<T1>, Optional<T2>, Optional<R>> lift() {
            return new FluentBiFunction<>(
                                          (opt1, opt2) -> opt1.flatMap(t1 -> opt2.map(t2 -> fn.apply(t1, t2))));
        }

        /**
         * @param classes Classes to catch exceptions for
         * @return BiFunction that returns it's result in a Try
         */
        public <X extends Throwable> FluentBiFunction<T1, T2, Try<R, X>> liftTry(final Class<X>... classes) {
            return FluentFunctions.of((t1, t2) -> Try.withCatch(() -> fn.apply(t1, t2), classes));
        }

        /**
         * @return A BiFunction that accepts and returns a generic Monad instance
         */
        public <W extends WitnessType<W>> FluentBiFunction<AnyM<W,T1>, AnyM<W,T2>, AnyM<W,R>> liftF() {
            return FluentFunctions.of(AnyM.liftF2(fn));
        }

        /**
         * @param ex Executor to execute this function on
         * @return A BiFunction that executes asynchronously on the provided Executor returning a CompletableFuture as it's result
         */
        public FluentBiFunction<T1, T2, CompletableFuture<R>> liftAsync(final Executor ex) {
            return FluentFunctions.of((t1, t2) -> CompletableFuture.supplyAsync(() -> fn.apply(t1, t2), ex));
        }

        /**
         * Wrap this BiFunction in a CompletableFuture for asyncrhonous execution
         * 
         * <pre>
         * {@code 
         *   FluentFunctions.of(this::add)
                            .async(ex)
                            .thenApplyAsync(f->f.apply(4,1))
                            .join()
         * }</pre>
         * 
         * 
         * @param ex Executor to execute this BiFunction on
         * @return CompletableFuture containing BiFunction for asyncrhonous execution
         */
        public CompletableFuture<FluentBiFunction<T1, T2, R>> async(final Executor ex) {
            return CompletableFuture.supplyAsync(() -> FluentFunctions.of(fn), ex);
        }

        /* (non-Javadoc)
         * @see java.util.function.BiFunction#andThen(java.util.function.Function)
         */
        @Override
        public <V> FluentBiFunction<T1, T2, V> andThen(final Function<? super R, ? extends V> after) {
            return FluentFunctions.of(BiFunction.super.andThen(after));
        }

    }

    @Wither(AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static class FluentTriFunction<T1, T2, T3, R> implements F3<T1, T2, T3, R> {
        private final F3<T1, T2, T3, R> fn;
        private final String name;

        public FluentTriFunction(final F3<T1, T2, T3, R> fn) {
            this.name = null;
            this.fn = fn;
        }

        /* (non-Javadoc)
         * @see com.aol.cyclops.util.function.TriFunction#apply(java.lang.Object, java.lang.Object, java.lang.Object)
         */
        @Override
        public R apply(final T1 t1, final T2 t2, final T3 t3) {
            return fn.apply(t1, t2, t3);
        }
   
        /**
         * Apply before advice to this TriFunction
         * 
         * @param action TriConsumer to recieve the input parameters to TriFunction
         * @return TriFunction with before advice attached
         */
        public FluentTriFunction<T1, T2, T3, R> before(final TriConsumer<T1, T2, T3> action) {
            return withFn((t1, t2, t3) -> {
                action.accept(t1, t2, t3);
                return fn.apply(t1, t2, t3);
            });
        }

        /**
         * Apply after advice to this TriFunction
         * 
         * @param action QuadConsumer to recieve  the input parameters and result from this TriFunction
         * @return TriFunction with after advice attached
         */
        public FluentTriFunction<T1, T2, T3, R> after(final QuadConsumer<T1, T2, T3, R> action) {
            return withFn((t1, t2, t3) -> {

                final R result = fn.apply(t1, t2, t3);
                action.accept(t1, t2, t3, result);
                return result;
            });
        }

        /**
         * Apply around advic to this TriFunction
         * 
         * <pre>
         * {@code 
         * 
         * FluentFunctions.of((a,b,c)->a+b+c)
                          .around(advice->advice.proceed1(advice.param1+1))
                          .println()
                          .apply(10,1,0)
                          
              //12            
         * }
         * </pre>
         * 
         * 
         * @param around Function that gives controlling access to this Function via the Advice inout parameter
         * @return TriFunction with around advice attached
         */
        public FluentTriFunction<T1, T2, T3, R> around(final Function<Advice3<T1, T2, T3, R>, R> around) {
            return withFn((t1, t2, t3) -> around.apply(new Advice3<>(
                                                                     t1, t2, t3, fn)));
        }
        /**
         * Partially apply the provided parameter as the first parameter to this TriFunction to generate a Function (single input value)
         * 
         * @param param Input parameter to Partially Applied
         * @return A BiFunction generated from the BiFunction with the first parameter already applied
         */
        public FluentBiFunction<T2, T3, R> partiallyApply(final T1 param) {
            return new FluentBiFunction<>(
                                          PartialApplicator.partial3(param, fn));
        }
        /**
         * Partially apply the provided parameters to this BiFunction to generate a Function (single input)
         * 
         * @param param1 First Input parameter
         * @param param2 Second Input parameter
         * @return Function generated from the partial application of the provided input parameters to this TriFunction
         */
        public FluentFunction<T3, R> partiallyApply(final T1 param1, final T2 param2) {
            return new FluentFunction<>(
                                        PartialApplicator.partial3(param1, param2, fn));
        }

        /**
         * Partially apply the provided parameters to this TriFunction to generate a Supplier (that takes no inputs)
         * 
         * @param param1 First Input parameter
         * @param param2 Second Input parameter
         * @param param3 Third Input parameter
         * @return Supplier generated from the partial application of the provided input parameters to this TriFunction
         */
        public FluentSupplier<R> partiallyApply(final T1 param1, final T2 param2, final T3 param3) {
            return new FluentSupplier<>(
                                        PartialApplicator.partial3(param1, param2, param3, fn));
        }
        /**
         * Curry this BiFunction, that is convert it from a TriFunction that accepts thre input parameters to a 'chain'
         * of three Functions that accept a single parameter
         * 
         * <pre>
         * {@code 
         * public int add(Integer a,Integer b, Integer c ){
               return a+b;
           }
         * 
         *      FluentFunctions.of(this::add)
                               .curry()
                               .apply(1)
                               .apply(2)
                               .apply(3);
                               
                //6               
         *    
         * }
         * </pre>
         * 
         * @return Curried function 
         */
        public FluentFunction<? super T1, Function<? super T2, Function<? super T3,? extends R>>> curry() {
            return new FluentFunction(
                                        Curry.curry3(fn));
        }
        /**
         * @return Function that logs it's result or error to the console
         */
        public FluentTriFunction<T1, T2, T3, R> memoize() {
            return withFn(Memoize.memoizeTriFunction(fn));
        }

        /**
         * This methods creates a caching version of this BiFunction, caching is implemented via the Cacheable wrapper,
         * that can be used to wrap any concrete cache implementation
         * 
         * E.g. to use a Guava cache for memoization
         * 
         * <pre>
         * {@code 
         * 
         * Cache<Object, Integer> cache = CacheBuilder.newBuilder()
                   .maximumSize(1000)
                   .expireAfterWrite(10, TimeUnit.MINUTES)
                   .build();
        
                   called=0;
            TriFunction<Integer,Integer,Integer> fn = FluentFunctions.of(this::add)
                                                                     .name("myFunction")
                                                                     .memoize((key,f)->cache.get(key,()->f.apply(key)));
        
            fn.apply(10,1,4);
            fn.apply(10,1,4);
            fn.apply(10,1,4);
        
            assertThat(called,equalTo(1));
         * 
         * 
         * 
         * }</pre>
         * 
         * 
         * @param cache Cache implementation wrapper
         * 
         * @return A caching (memoizing) version of this BiFunction, outputs for all inputs will be cached (unless ejected from the cache)
         */
        public FluentTriFunction<T1, T2, T3, R> memoize(final Cacheable<R> cache) {
            return withFn(Memoize.memoizeTriFunction(fn));
        }
        /**
         * @param name To give this TriFunction
         * @return A TriFunction with a name (useful for logging purposes)
         */
        public FluentTriFunction<T1, T2, T3, R> name(final String name) {
            return this.withName(name);
        }

        private String handleNameStart() {
            return name == null ? "(fluent-function-" : "(" + name + "-";

        }

        private String handleNameEnd() {
            return ")";

        }
        /**
         *  A TriFunction that logs it's success or error states to the provided Consumers
         * 
         * @param logger Success logger
         * @param error Failure logger
         * @return TriFunction that logs it's state
         */
        public FluentTriFunction<T1, T2, T3, R> log(final Consumer<String> logger, final Consumer<Throwable> error) {
            return FluentFunctions.of((t1, t2, t3) -> {
                try {
                    logger.accept(handleNameStart() + "Parameters[" + t1 + "," + t2 + "," + t3 + "]" + handleNameEnd());
                    final R result = fn.apply(t1, t2, t3);
                    logger.accept(handleNameStart() + "Result[" + result + "]" + handleNameEnd());
                    return result;
                } catch (final Throwable t) {
                    error.accept(t);
                    throw ExceptionSoftener.throwSoftenedException(t);
                }
            });
        }
        /**
         * Visit the result of this TriFunction once it has been executed, if the Function executes successfully the
         * result will be passes to the eventConsumer, if there is an error it will be passed to the errorConsumer
         * 
         * @param eventConsumer Consumer to recieve result on successful execution
         * @param errorConsumer Consumer to recieve error on failure
         * @return TriFunction with event vistor attached.
         */
        public FluentTriFunction<T1, T2, T3, R> visitEvent(final Consumer<R> eventConsumer, final Consumer<Throwable> errorConsumer) {

            return FluentFunctions.of((t1, t2, t3) -> {

                try {
                    final R result = fn.apply(t1, t2, t3);
                    eventConsumer.accept(result);
                    return result;
                } catch (final Throwable t) {
                    errorConsumer.accept(t);
                    throw ExceptionSoftener.throwSoftenedException(t);
                }

            });
        }
        /**
         * @return TriFunction that logs it's result or error to the console
         */
        public FluentTriFunction<T1, T2, T3, R> println() {
            return log(s -> System.out.println(s), t -> t.printStackTrace());
        }
        
        /**
         * A TriFunction that can recover from the specified exception types, using the provided recovery Function
         * 
         * @param type Recoverable exception types
         * @param onError Recovery BiFunction
         * @return TriFunction capable of error recovery
         */
        public <X extends Throwable> FluentTriFunction<T1, T2, T3, R> recover(final Class<X> type, final F3<T1, T2, T3, R> onError) {
            return FluentFunctions.of((t1, t2, t3) -> {
                try {
                    return fn.apply(t1, t2, t3);
                } catch (final Throwable t) {
                    if (type.isAssignableFrom(t.getClass())) {
                        return onError.apply(t1, t2, t3);

                    }
                    throw ExceptionSoftener.throwSoftenedException(t);

                }
            });

        }
        /**
         *  A TriFunction capable of retrying on failure using an exponential backoff strategy
         * 
         * @param times Number of times to retry 
         * @param backoffStartTime Wait time before first retry
         * @return TriFunction with a retry strategy
         */
        public FluentTriFunction<T1, T2, T3, R> retry(final int times, final int backoffStartTime) {
            return FluentFunctions.of((t1, t2, t3) -> {
                int count = times;
                final MutableInt sleep = MutableInt.of(backoffStartTime);
                Throwable exception = null;
                while (count-- > 0) {
                    try {
                        return fn.apply(t1, t2, t3);
                    } catch (final Throwable e) {
                        exception = e;
                    }
                    ExceptionSoftener.softenRunnable(() -> Thread.sleep(sleep.get()));

                    sleep.mutate(s -> s * 2);
                }
                throw ExceptionSoftener.throwSoftenedException(exception);

            });

        }

        /**
         * Generate an infinite Stream from the provided seed values and mapping function.
         * The supplied mapping function is inverted taking an input of type R and returning three outputs T1, T2 (in a Tuple)
         * 
         * <pre>
         * {@code 
         *   FluentFunctions.of(this::add)  
                           .iterate(1,2,3,(i)->Tuple.tuple(i,i,i))
                           .limit(2) 
                           .printOut();
                           
                           
              //6
                18             
         * }
         * </pre>
         * 
         * 
         * @param seed1 Initial input parameter 1
         * @param seed2 Initial input parameter 2
         * @param seed3 Initial input parameter 3
         * @param mapToType Reversed mapping function
         * @return Infinite Stream
         */
        public ReactiveSeq<R> iterate(final T1 seed1, final T2 seed2, final T3 seed3, final Function<R, Tuple3<T1, T2, T3>> mapToType) {
            return ReactiveSeq.iterate(fn.apply(seed1, seed2, seed3), t -> {
                final Tuple3<T1, T2, T3> tuple = mapToType.apply(t);
                return fn.apply(tuple.v1, tuple.v2, tuple.v3);
            });
        }
        /**
         * Generate an infinite Stream by applying the input parameters to this function
         * repeatedly
         * 
         * @param input1 First input parameter
         * @param input2 Second input parameter
         * @param input3 Third input parameter
         * @return Infinite Stream
         */
        public ReactiveSeq<R> generate(final T1 input1, final T2 input2, final T3 input3) {
            return ReactiveSeq.generate(() -> fn.apply(input1, input2, input3));
        }

        /**
         * @return A TriFunction that accepts and returns Optionals
         */
        public FluentTriFunction<Optional<T1>, Optional<T2>, Optional<T3>, Optional<R>> liftOptional() {
            return new FluentTriFunction<>(
                                           (opt1, opt2, opt3) -> opt1.flatMap(t1 -> opt2.flatMap(t2 -> opt3.map(t3 -> fn.apply(t1, t2, t3)))));
        }

        /**
         * @param classes Classes to catch exceptions for
         * @return TriFunction that returns it's result in a Try
         */
        public <X extends Throwable> FluentTriFunction<T1, T2, T3, Try<R, X>> liftTry(final Class<X>... classes) {
            return FluentFunctions.of((t1, t2, t3) -> Try.withCatch(() -> fn.apply(t1, t2, t3), classes));
        }

        /**
         * @return Lift this TriFunction into one that accepts and returns generic monad types (AnyM)
         */
        public  <W extends WitnessType<W>> FluentTriFunction<AnyM<W,T1>, AnyM<W,T2>, AnyM<W,T3>, AnyM<W,R>> liftF() {
            return FluentFunctions.of(AnyM.liftF3(fn));
        }

        /**
         * Convert this TriFunction into one that executes asynchronously and returns a CompleteableFuture with the result
         * 
         * @param ex Executor to execute this TriFunction on
         * @return TriFunction that executes Asynchronous 
         */
        public FluentTriFunction<T1, T2, T3, CompletableFuture<R>> liftAsync(final Executor ex) {
            return FluentFunctions.of((t1, t2, t3) -> CompletableFuture.supplyAsync(() -> fn.apply(t1, t2, t3), ex));
        }
      
        /**
         * Move this function into an asynchronous context
         * 
         * @param ex Executor to manage operations on this function on
         * @return A CompletableFuture that contains this function
         */
        public CompletableFuture<FluentTriFunction<T1, T2, T3, R>> async(final Executor ex) {
            return CompletableFuture.supplyAsync(() -> FluentFunctions.of(fn), ex);
        }

        /**
         * Compose this TriFunction with the provided function into a single TriFunction. 
         * This TriFunction would be executed first and the result passed to the provided Function and applied there.
         * 
         * @param after Function to execute after this one in a chain
         * @return TriFunction that executes this TriFunction and the provided Function in a chain
         */
        public <R2> FluentTriFunction<T1, T2, T3, R2> andThen(final Function<? super R, ? extends R2> after) {
            Objects.requireNonNull(after);
            return FluentFunctions.of((final T1 t1, final T2 t2, final T3 t3) -> after.apply(apply(t1, t2, t3)));
        }
    }

    @AllArgsConstructor
    public static class Advice0<R> {

        private final Supplier<R> fn;

        /**
         * Proceed and execute wrapped Supplier
         * 
         * @return Result of executing wrapped Supplier
         */
        public R proceed() {
            return fn.get();
        }

    }

    @AllArgsConstructor
    public static class Advice1<T, R> {
        public final T param;
        private final Function<T, R> fn;

        /**
         * Proceed and execute wrapped Function with it's input param as captured
         * 
         * @return  Result of executing wrapped Function
         */
        public R proceed() {
            return fn.apply(param);
        }

        /**
         * Proceed and execute wrapped Function replacing it's input param
         * 
         * @param param Replacement parameter
         * @return Result of executing wrapped Function
         */
        public R proceed(final T param) {
            return fn.apply(param);
        }
    }

    @AllArgsConstructor
    public static class Advice2<T1, T2, R> {
        public final T1 param1;
        public final T2 param2;
        private final BiFunction<T1, T2, R> fn;

        /**
         * Proceed and execute wrapped BiFunction with it's input params as captured
         * 
         * @return Result of executing wrapped BiFunction
         */
        public R proceed() {
            return fn.apply(param1, param2);
        }

        /**
         * Proceed and execute wrapped BiFunction with it's input params as captured
         * 
         * @param param1 First replacement parameter
         * @param param2 Second replacement parameter
         * @return Result of executing wrapped BiFunction 
         */
        public R proceed(final T1 param1, final T2 param2) {
            return fn.apply(param1, param2);
        }

        /**
         * Proceed and execute wrapped BiFunction with it's second input parameter as a captured and the replacement parameter as provided
         * 
         * @param param First replacement parameter
         * @return Result of executing wrapped BiFunction
         */
        public R proceed1(final T1 param) {
            return fn.apply(param, param2);
        }

        /**
         * Proceed and execute wrapped BiFunction with it's first input parameter as a captured and the replacement parameter as provided
         * 
         * @param param Second replacement parameter
         * @return Result of executing wrapped BiFunction
         */
        public R proceed2(final T2 param) {
            return fn.apply(param1, param);
        }
    }

    @AllArgsConstructor
    public static class Advice3<T1, T2, T3, R> {
        public final T1 param1;
        public final T2 param2;
        public final T3 param3;
        private final F3<T1, T2, T3, R> fn;

        /**
         * Proceed and execute wrapped TriFunction with it's input params as captured
         * 
         * @return Result of executing wrapped TriFunction
         */
        public R proceed() {
            return fn.apply(param1, param2, param3);
        }

        /**
         * Proceed and execute wrapped TriFunction with it's input params as captured
         * 
         * @param param1 First replacement parameter
         * @param param2 Second replacement parameter
         * @param param3 Third replacement parameter
         * @return Result of executing wrapped TriFunction
         */
        public R proceed(final T1 param1, final T2 param2, final T3 param3) {
            return fn.apply(param1, param2, param3);
        }

        /**
         * Proceed and execute wrapped TriFunction with it's second and third input parameters as a captured and the replacement parameter as provided
         * 
         * @param param First replacement parameter
         * @return Result of executing wrapped TriFunction
         */
        public R proceed1(final T1 param) {
            return fn.apply(param, param2, param3);
        }

        /**
         * Proceed and execute wrapped TriFunction with it's first and third input parameters as a captured and the replacement parameter as provided
         * 
         * @param param Second replacement parameter
         * @return Result of executing wrapped TriFunction
         */
        public R proceed2(final T2 param) {
            return fn.apply(param1, param, param3);
        }

        /**
         * Proceed and execute wrapped TriFunction with it's first and second input parameters as a captured and the replacement parameter as provided
         * 
         * @param param Third replacement parameter
         * @return Result of executing wrapped TriFunction
         */
        public R proceed3(final T3 param) {
            return fn.apply(param1, param2, param);
        }
    }
}
