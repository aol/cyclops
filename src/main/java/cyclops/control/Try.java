
package cyclops.control;

import static cyclops.monads.Witness.*;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops2.types.*;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import cyclops.function.Monoid;
import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.function.Curry;
import cyclops.function.Fn4;
import cyclops.function.Fn3;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * Light weight Try Monad
 * 
 * Fail fast behaviour with more explicit declararions required compared to
 *  with scala.util.Try and javaslang.monad.Try. This is probably closer
 *  to how most Java devs currently handle exceptions.
 * 
 * Goals / features
 * 
 * Support for init block / try block  / finally block
 * Try with resources
 * Try with multiple resources
 * Does not automatically catch exceptions in combinators
 * Can target specific exception types
 * Exception types to be caught can be specified in xxxWithCatch methods
 * Handle exceptions conciously, not coding bugs
 * Fluent step builders
 * Fail fast
 * 
 * Examples :
 * 
 * Create a 'successful' value
 * <pre>
 * {@code 
 *  Try.success("return-value");
 * }
 * </pre>
 * 
 * Create a failure value
 * 
 * <pre>
 * {@code 
 *  Try.failure(new MyException("error details"));
 * }
 * </pre>
 * 
 * Exceute methods that may throw exceptions
 * 
 * Non-void methods
 * <pre>
 * {@code 
 * 
 * Try.withCatch(()-> exceptional2())
                        .map(i->i+" woo!")
                        .onFail(System.out::println)
                        .orElse("default");
                        
 *  //"hello world woo!"
 * 
 *  private String exceptional2() throws RuntimeException{
        return "hello world";
    }
 * }
 * </pre>
 * 
 * Void methods
 * <pre>
 * {@code 
 *   
 *   
 *  //Only catch IOExceptions
 *  
 *  Try.runWithCatch(this::exceptional,IOException.class)
        .onFail(System.err::println);
        
    private void exceptional() throws IOException{
        throw new IOException();
    }
 * 
 * }
 * </pre>
 * 
 * Try with resources
 * <pre>
 * {@code 
 *    
 *   Try.catchExceptions(FileNotFoundException.class,IOException.class)
         .init(()->new BufferedReader(new FileReader("file.txt")))
         .tryWithResources(this::read)
         .map(this::processData)
         .recover(e->"default);
 *  
 * }
 * </pre>
 * 
 * By default Try does not catch exception within it's operators such as map / flatMap, to catch Exceptions in ongoing operations use @see {@link Try#of(Object, Class...)}
 * <pre>
 * {@code 
 *  Try.of(2, RuntimeException.class)
       .map(i->{throw new RuntimeException();});
       
    //Failure[RuntimeException]   
 * 
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Return type (success)
 * @param <X> Base Error type
 */
public interface Try<T, X extends Throwable> extends    To<Try<T,X>>,
                                                        MonadicValue<T> {

    
    
    /**
     * Construct a Try  that contains a single value extracted from the supplied reactive-streams Publisher, will catch any Exceptions
     * of the provided types
     * <pre>
     * {@code 
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);
        
        Try<Integer,Throwable> attempt = Try.fromPublisher(reactiveStream, RuntimeException.class);
        
        //Try[1]
     * 
     * }
     * </pre> 
     * 
     * @param pub Publisher to extract value from
     * @return Try populated with first value from Publisher 
     */
    @SafeVarargs
    public static <T, X extends Throwable> Try<T, X> fromPublisher(final Publisher<T> pub, final Class<X>... classes) {
       
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toTry(classes);
    }

    /**
     * Construct a Try  that contains a single value extracted from the supplied reactive-streams Publisher
     * 
     * <pre>
     * {@code 
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);
        
        Try<Integer,Throwable> attempt = Try.fromPublisher(reactiveStream);
        
        //Try[1]
     * 
     * }
     * </pre> 
     * 
     * @param pub Publisher to extract value from
     * @return Try populated with first value from Publisher
     */
    public static <T> Try<T, Throwable> fromPublisher(final Publisher<T> pub) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toTry();
    }
    /**
     * Construct a Try  that contains a single value extracted from the supplied Iterable
     * 
     * <pre>
     * {@code 
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);
        
        Try<Integer,Throwable> attempt = Try.fromIterable(reactiveStream);
        
        //Try[1]
     * 
     * }
     * </pre> 
     * 
     * @param iterable Iterable to extract value from
     * @return Try populated with first value from Iterable
     */
    public static <T, X extends Throwable> Try<T, X> fromIterable(final Iterable<T> iterable) {
        final Iterator<T> it = iterable.iterator();
        return Try.success(it.hasNext() ? it.next() : null);
    }

    @Override
    default <R> Try<R,X> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (Try<R,X>)MonadicValue.super.zipWith(fn);
    }

    @Override
    default <R> Try<R,X> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (Try<R,X>)MonadicValue.super.zipWithS(fn);
    }

    @Override
    default <R> Try<R,X> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (Try<R,X>)MonadicValue.super.zipWithP(fn);
    }

    @Override
    default <R> Try<R,X> retry(final Function<? super T, ? extends R> fn) {
        return (Try<R,X>)MonadicValue.super.retry(fn);
    }

    @Override
    default <U> Try<Tuple2<T, U>,X> zipP(final Publisher<? extends U> other) {
        return (Try)MonadicValue.super.zipP(other);
    }

    @Override
    default <R> Try<R,X> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (Try<R,X>)MonadicValue.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> Try<Tuple3<T, S, U>,X> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (Try)MonadicValue.super.zip3(second,third);
    }

    @Override
    default <S, U, R> Try<R,X> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (Try<R,X>)MonadicValue.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> Try<Tuple4<T, T2, T3, T4>,X> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (Try)MonadicValue.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> Try<R,X> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Try<R,X>)MonadicValue.super.zip4(second,third,fourth,fn);
    }

    @Override
    default <R> Try<R,X> flatMapS(final Function<? super T, ? extends Stream<? extends R>> mapper) {
        return (Try<R,X>)MonadicValue.super.flatMapS(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Try<R,X> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Try<R,X>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <T2, R1, R2, R3, R> Try<R,X> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (Try<R,X>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Try<R,X> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
      
        return (Try<R,X>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <T2, R1, R2, R> Try<R,X> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Try<R,X>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Try<R,X> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Try<R,X>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> Try<R,X> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Try<R,X>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toTry()
     */
    @Override
    default Try<T, Throwable> toTry() {
        return (Try<T, Throwable>) this;

    }



    
    /**
     * @return The exception returned in the Failure case, Implementations should throw NoSuchElementException if no failure is present
     */
    public X failureGet();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toXor()
     */
    @Override
    public Xor<X, T> toXor();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toIor()
     */
    @Override
    public Ior<X, T> toIor();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> Try<R, X> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                     .apply(this);
    }

    //cojoin
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */
    @Override
    default Try<MonadicValue<T>, X> nest() {
        return this.map(t -> unit(t));
    }

    /**
     * Combine this Try with another using the supplied Monoid as a combiner
     * 
     * <pre>
     * {@code 
     *  
     *  Try<Integer> just = Try.success(10);
     *  Try<Integer> none = Try.failure(new RuntimeException());
     *  
     *  Monoid<Integer> add = Monoid.of(0,Semigroups.intSum);
     *  
     *  
        assertThat(just.combine(add,none),equalTo(Try.success(10)));
        assertThat(none.combine(add,just),equalTo(Try.success(0))); 
        assertThat(none.combine(add,none),equalTo(Try.success(0))); 
        assertThat(just.combine(add,Try.success(10)),equalTo(Try.success(20)));
        Monoid<Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
        assertThat(just.combine(firstNonNull,Try.success(null)),equalTo(just));
        
     * }
     * </pre>
     * 
     * 
     * @param monoid Combiner
     * @param v2 Try to combine with
     * @return Combined Try
     */
    default Try<T, X> combine(final Monoid<T> monoid, final Try<? extends T, X> v2) {
        return unit(this.forEach2( t1 -> v2, (t1, t2) -> monoid
                                                            .apply(t1, t2)).orElseGet(() -> this.orElseGet(() -> monoid.zero())));
    }
    

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Applicative#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Applicative)
     */
    @Override
    default Try<T,X> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (Try<T,X>)MonadicValue.super.zip(combiner, app);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Transformable#cast(java.lang.Class)
     */
    @Override
    default <U> Try<U, X> cast(final Class<? extends U> type) {
        return (Try<U, X>) MonadicValue.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    default <R> Try<R, X> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (Try<R, X>) MonadicValue.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> Maybe<U> ofType(final Class<? extends U> type) {

        return (Maybe<U>) MonadicValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default Maybe<T> filterNot(final Predicate<? super T> fn) {

        return (Maybe<T>) MonadicValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#notNull()
     */
    @Override
    default Maybe<T> notNull() {

        return (Maybe<T>) MonadicValue.super.notNull();
    }

    /**
     * Construct a Failure instance from a throwable (an implementation of Try)
     * <pre>
     * {@code 
     *    Failure<Exception> failure = Try.failure(new RuntimeException());
     * }
     * </pre>
     * 
     * @param error for Failure
     * @return new Failure with error
     */
    public static <T, X extends Throwable> Failure<T, X> failure(final X error) {
        return new Failure<>(
                             error);
    }

    /**
     * Construct a Success instance (an implementation of Try)
     * 
     * <pre>
     * {@code 
     *    Success<Integer> success = Try.success(new RuntimeException());
     * }
     * </pre>
     * 
     * @param value Successful value
     * @return new Success with value
     */

    public static <T, X extends Throwable> Success<T, X> success(final T value) {
        return new Success<>(
                             value, new Class[0]);
    }

    /**
     * @return Convert this Try to an Xor with the error type as the secondary value
     */
    default Xor<X, T> toXorWithError() {
        if (isSuccess())
            return Xor.primary(get());
        else
            return Xor.<X, T> secondary(this.toFailedOptional()
                                            .get());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    default <T> Try<T, X> unit(final T value) {
        return success(value);
    }



    /**
     * Execute one function conditional on Try state (Success / Failure)
     * 
     * <pre>
     * {@code 
     *    
     *     Try<Integer> result = this.execute();
     *     
     *     this.logState(result.visit(t->"value is " +t,e->"error is "+e.getMessage());
     *  
     * }
     * </pre>
     * 
     * @param success Function to execute if this Try is a Success
     * @param failure Funcion to execute if this Try is a Failure
     * @return Result of executed function (one or other depending on case)
     */
    public <R> R visit(Function<? super T, ? extends R> success, Function<? super X, ? extends R> failure);

    /**
     * @return This monad, wrapped as AnyM of Success
     */
    default AnyMValue<tryType,T> anyM(){
        return AnyM.fromTry(this);
    }


    /**
     * @return Successful value or will throw Throwable (X) if Failire
     */
    @Override
    public T get();

    /**
     * Throw exception if Failure, do nothing if success
     */
    public void throwException();

    /**
     * @param value Return value supplied if Failure, otherwise return Success value
     * @return Success value or supplied value
     */
    @Override
    public T orElse(T value);

    /**
     * 
     * @param value from supplied Supplier if Failure otherwise return Success value
     * @return Success value
     */
    @Override
    public T orElseGet(Supplier<? extends T> value);

    /**
     * @param fn Map success value from T to R. Do nothing if Failure (return this)
     * @return New Try with mapped value (Success) or this (Failure)
     */

    @Override
    public <R> Try<R, X> map(Function<? super T, ? extends R> fn);

    /**
     * @param fn FlatMap success value or Do nothing if Failure (return this)
     * @return Try returned from FlatMap fn
     */
    public <R> Try<R, X> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> fn);

    /**
     * @param p Convert a Success to a Failure (with a null value for Exception) if predicate does not hold.
     *          Do nothing to a Failure
     * @return this if Success and Predicate holds, or if Failure. New Failure if Success and Predicate fails
     */
    @Override
    public Maybe<T> filter(Predicate<? super T> p);

    /**
     * @param consumer Accept Exception if present (Failure)
     * @return this
     */
    public Try<T, X> onFail(Consumer<? super X> consumer);

    /**
     * @param t Class type of match Exception against
     * @param consumer Accept Exception if present (Failure) and if class types match
     * @return this
     */
    public Try<T, X> onFail(Class<? extends X> t, Consumer<X> consumer);

    /**
     * @param fn Recovery function - map from a failure to a Success.
     * @return new Try
     */
    public Try<T, X> recover(Function<? super X, ? extends T> fn);

    /**
     * flatMap recovery
     * 
     * @param fn Recovery FlatMap function. Map from a failure to a Success
     * @return Success from recovery function
     */
    public Try<T, X> recoverWith(Function<? super X, ? extends Try<T, X>> fn);

    /**
     * Recover if exception is of specified type
     * @param t Type of exception to match against
     * @param fn Recovery function
     * @return New Success if failure and types match / otherwise this
     */
    public Try<T, X> recoverFor(Class<? extends X> t, Function<? super X, ? extends T> fn);

    /**
     * 
     * FlatMap recovery function if exception is of specified type
     * 
     * @param t Type of exception to match against
     * @param fn Recovery FlatMap function. Map from a failure to a Success
     * @return Success from recovery function or this  and types match or if already Success
     */
    public Try<T, X> recoverWithFor(Class<? extends X> t, Function<? super X, ? extends Success<T, X>> fn);

    

    /**
     * @return Optional present if Success, Optional empty if failure
     */
    @Override
    public Optional<T> toOptional();

    /**
     * @return Stream with value if Sucess, Empty Stream if failure
     */
    @Override
    public ReactiveSeq<T> stream();

    /**
     * @return Optional present if Failure (with Exception), Optional empty if Success
     */
    public Optional<X> toFailedOptional();

    /**
     * @return Stream with error if Failure, Empty Stream if success
     */
    public Stream<X> toFailedStream();

    /**
     * @return true if Success / false if Failure
     */
    public boolean isSuccess();

    /**
     * @return True if Failure / false if Success
     */
    public boolean isFailure();

    /**
     * @param consumer Accept value if Success / not called on Failure
     */
    @Override
    public void forEach(Consumer<? super T> consumer);

    /**
     * @param consumer Accept value if Failure / not called on Failure
     */
    public void forEachFailed(Consumer<? super X> consumer);

    @Override
    default boolean isPresent() {
        return isSuccess();
    }

    /**
     * @param consumer Accept value if Success
     * @return this
     */
    @Override
    default Try<T, X> peek(final Consumer<? super T> consumer) {
        forEach(consumer);
        return this;
    }

    /**
     * @param consumer Accept Exception if Failure
     * @return this
     */
    default Try<T, X> peekFailed(final Consumer<? super X> consumer) {
        forEachFailed(consumer);
        return this;
    }

    @Override
    default Iterator<T> iterator() {

        return MonadicValue.super.iterator();
    }

    /**
     * Return a Try that will catch specified exceptions when map / flatMap called
     * For use with liftM / liftM2 and For Comprehensions (when Try is at the top level)
     * 
     * @param value Initial value
     * @param classes Exceptions to catch during map / flatMap
     * @return Try instance
     */
    @SafeVarargs
    public static <T, X extends Throwable> Try<T, X> of(final T value, final Class<? extends Throwable>... classes) {
        return new Success<>(
                             value, classes);
    }

    /**
     * Try to execute supplied Supplier and will Catch specified Excpetions or java.lang.Exception
     * if none specified.
     * 
     * @param cf CheckedSupplier to attempt to execute
     * @param classes  Exception types to catch (or java.lang.Exception if none specified)
     * @return New Try
     */
    @SafeVarargs
    public static <T, X extends Throwable> Try<T, X> withCatch(final CheckedSupplier<T, X> cf, final Class<? extends X>... classes) {
        Objects.requireNonNull(cf);
        try {
            return Try.success(cf.get());
        } catch (final Throwable t) {
            if (classes.length == 0)
                return Try.failure((X) t);
            val error = Stream.of(classes)
                              .filter(c -> c.isAssignableFrom(t.getClass()))
                              .findFirst();
            if (error.isPresent())
                return Try.failure((X) t);
            else
                throw ExceptionSoftener.throwSoftenedException(t);
        }

    }

    /**
     * Try to execute supplied Runnable and will Catch specified Excpetions or java.lang.Exception
     * if none specified.
     * 
     * @param cf CheckedRunnable to attempt to execute
     * @param classes  Exception types to catch (or java.lang.Exception if none specified)
     * @return New Try
     */
    @SafeVarargs
    public static <X extends Throwable> Try<Void, X> runWithCatch(final CheckedRunnable<X> cf, final Class<? extends X>... classes) {
        Objects.requireNonNull(cf);
        try {
            cf.run();
            return Try.success(null);
        } catch (final Throwable t) {

            if (classes.length == 0)
                return Try.failure((X) t);
            val error = Stream.of(classes)
                              .filter(c -> c.isAssignableFrom(t.getClass()))
                              .findFirst();
            if (error.isPresent())
                return Try.failure((X) t);
            else
                throw ExceptionSoftener.throwSoftenedException(t);
        }

    }

    /**
     * Fluent step builder for Try / Catch / Finally and Try with resources equivalents.
     * Start with Exception types to catch.
     * 
     * @param classes Exception types to catch
     * @return Next step in the fluent Step Builder
     */
    @SafeVarargs
    public static <X extends Throwable> Init<X> catchExceptions(final Class<? extends X>... classes) {
        return new MyInit<X>(
                             (Class[]) classes);
    }

    @AllArgsConstructor
    static class MyInit<X extends Throwable> implements Init<X> {
        private final Class<X>[] classes;

        /* 
         *	@param input
         *	@return
         * @see com.aol.cyclops2.trycatch.Try.Init#init(com.aol.cyclops2.trycatch.Try.CheckedSupplier)
         */
        @Override
        public <V> TryCatch<V, X> init(final CheckedSupplier<V, X> input) {
            return new MyTryCatch(
                                  classes, input);
        }

        @Override
        public Try<Void, X> run(final CheckedRunnable<X> input) {
            return runWithCatch(input, classes);
        }

        @Override
        public <V> Try<V, X> tryThis(final CheckedSupplier<V, X> input) {
            return withCatch(input, classes);
        }

    }

    @AllArgsConstructor
    static class MyTryCatch<V, X extends Throwable> implements TryCatch<V, X> {
        private final Class<X>[] classes;
        private final CheckedSupplier<V, X> inputSupplier;

        @Override
        public <T> AndFinally<T, V, X> tryThis(final CheckedFunction<V, T, X> catchBlock) {
            return new MyFinallyBlock<>(
                                        classes, inputSupplier, catchBlock);
        }

        @Override
        public <T> Try<T, X> tryWithResources(final CheckedFunction<V, T, X> catchBlock) {
            return new MyFinallyBlock<>(
                                        classes, inputSupplier, catchBlock).close();
        }

    }

    @AllArgsConstructor
    public static class MyFinallyBlock<T, V, X extends Throwable> implements AndFinally<T, V, X> {
        private final Class<X>[] classes;
        private final CheckedSupplier<V, X> inputSupplier;
        private final CheckedFunction<V, T, X> catchBlock;

        private void invokeClose(final Object in) {
            if (in instanceof Closeable)
                invokeCloseableClose((Closeable) in);
            else if (in instanceof AutoCloseable)
                invokeAutocloseableClose((AutoCloseable) in);
            else if (in instanceof Iterable)
                invokeClose((Iterable) in);
            else
                _invokeClose(in);
        }

        private void invokeClose(final Iterable in) {
            for (final Object next : in)
                invokeClose(next);

        }

        private void invokeCloseableClose(final Closeable in) {

            Try.runWithCatch(() -> in.close());

        }

        private void invokeAutocloseableClose(final AutoCloseable in) {

            Try.runWithCatch(() -> in.close());

        }

        private void _invokeClose(final Object in) {

            Try.withCatch(() -> in.getClass()
                                  .getMethod("close"))
               .filter(m -> m != null)
               .flatMap(m -> Try.withCatch(() -> m.invoke(in))
                                .filter(o -> o != null));

        }

        @Override
        public Try<T, X> close() {

            return andFinally(in -> {
                invokeClose(in);
            });
        }

        @Override
        public Try<T, X> andFinally(final CheckedConsumer<V, X> finallyBlock) {

            final Try<V, X> input = Try.withCatch(() -> inputSupplier.get(), classes);
            Try<T, X> result = null;
            try {
                result = input.flatMap(in -> withCatch(() -> catchBlock.apply(in), classes));

            } finally {
                final Try finalResult = result.flatMap(i -> Try.runWithCatch(() -> finallyBlock.accept(inputSupplier.get()), classes));
                if (finalResult instanceof Failure)
                    return finalResult;

            }
            return result;
        }

    }

    public static interface Init<X extends Throwable> {
        /**
         * Initialise a try / catch / finally block
         * Define the variables to be used within the block.
         * A Tuple or Iterable can be returned to defined multiple values.
         * Closeables (either individually or within an iterable) will be closed
         * via tryWithResources.
         * 
         * <pre>
         * 
         * Try.catchExceptions(FileNotFoundException.class,IOException.class)
         *		   .init(()-&gt;new BufferedReader(new FileReader(&quot;file.txt&quot;)))
         *		   .tryWithResources(this::read);
         * 
         * </pre>
         * 
         * or
         * 
         * <pre>
         * 
         * Try t2 = Try.catchExceptions(FileNotFoundException.class,IOException.class)
         *		   .init(()-&gt;Tuple.tuple(new BufferedReader(new FileReader(&quot;file.txt&quot;)),new FileReader(&quot;hello&quot;)))
         *		   .tryWithResources(this::read2);
         * 
         * private String read2(Tuple2&lt;BufferedReader,FileReader&gt; res) throws IOException{
         * String line = res.v1.readLine();
         * 
         * </pre>
         * 
         * @param input Supplier that provides input values to be used in the Try / Catch
         * @return
         */
        <V> TryCatch<V, X> init(CheckedSupplier<V, X> input);

        /**
         * Run the supplied CheckedRunnable and trap any Exceptions
         * Return type is Void
         * 
         * @param input CheckedRunnable
         * @return Try that traps any errors (no return type)
         */
        Try<Void, X> run(CheckedRunnable<X> input);

        /**
         * Run the supplied CheckedSupplier and trap the return value or an Exception
         * inside a Try
         * 
         * @param input CheckedSupplier to run
         * @return new Try
         */
        <V> Try<V, X> tryThis(CheckedSupplier<V, X> input);
    }

    public static interface TryCatch<V, X extends Throwable> {

        /**
         * Will execute and run the CheckedFunction supplied and will automatically
         * safely close any Closeables supplied during init (either individually or inside an iterable)
         * 
         * @param catchBlock CheckedFunction to Try
         * @return New Try capturing return data or Exception
         */
        <T> Try<T, X> tryWithResources(CheckedFunction<V, T, X> catchBlock);

        /**
         * Build another stage in try / catch / finally block
         * This defines the CheckedFunction that will be run in the main body of the catch block
         * Next step can define the finally block
         * 
         * @param catchBlock To Try
         * @return Next stage in the fluent step builder (finally block)
         */
        <T> AndFinally<T, V, X> tryThis(CheckedFunction<V, T, X> catchBlock);

    }

    public static interface AndFinally<T, V, X extends Throwable> {

        /**
         * Define the finally block and execute the Try
         * 
         * @param finallyBlock to execute
         * @return New Try capturing return data or Exception
         */
        Try<T, X> andFinally(CheckedConsumer<V, X> finallyBlock);

        /**
         * Create a finally block that auto-closes any Closeables specified during init
         *  including those inside an Iterable
         * 
        * @return New Try capturing return data or Exception
        */
        Try<T, X> close();
    }

    public static interface CheckedFunction<T, R, X extends Throwable> {
        public R apply(T t) throws X;
    }

    public static interface CheckedSupplier<T, X extends Throwable> {
        public T get() throws X;
    }

    public static interface CheckedConsumer<T, X extends Throwable> {
        public void accept(T t) throws X;
    }

    public static interface CheckedRunnable<X extends Throwable> {
        public void run() throws X;
    }
    /* 
     * Flatten a nested Try Structure
     * @return Lowest nested Try
     * @see com.aol.cyclops2.trycatch.Try#flatten()
     */
  
    public static <T,X extends Throwable> Try<T, X> flatten(Try<? extends Try<T,X>,X> nested) {
        return nested.flatMap(Function.identity());
    }

    /**
     * Class that represents a Successful Try
     * 
     * @author johnmcclean
     *
     * @param <T> Success data type
     * @param <X> Error data type
     */
    @RequiredArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Success<T, X extends Throwable> implements Try<T, X> {

        private final T value;
        private final Class<? extends Throwable>[] classes;



        @Override
        public Xor<X, T> toXor() {
            return Xor.primary(value);

        }

        @Override
        public Ior<X, T> toIor() {
            return Ior.primary(value);
        }
        
       
        /* 
         *	@return Current value
         * @see com.aol.cyclops2.trycatch.Try#get()
         */
        @Override
        public T get() {
            return value;
        }

       


        /**
         * @param value Successful value
         * @return new Success with value
         */
        public static <T, X extends Throwable> Success<T, X> of(final T value, final Class<? extends Throwable>[] classes) {
            return new Success<>(
                                 value, classes);
        }

        /* 
         * @param fn Map success value from T to R.
         * @return New Try with mapped value
         * @see com.aol.cyclops2.trycatch.Try#map(java.util.function.Function)
         */
        @Override
        public <R> Try<R, X> map(final Function<? super T, ? extends R> fn) {
            return safeApply(() -> success(fn.apply(get())));
        }

        private <R> R safeApply(final Supplier<? extends R> s) {
            try {
                return s.get();
            } catch (final Throwable t) {
                return (R) Try.failure(orThrow(Stream.of(classes)
                                                     .filter(c -> c.isAssignableFrom(t.getClass()))
                                                     .map(c -> t)
                                                     .findFirst(),
                                               t));

            }
        }

        private Throwable orThrow(final Optional<Throwable> findFirst, final Throwable t) {
            if (findFirst.isPresent())
                return findFirst.get();
            ExceptionSoftener.throwSoftenedException(t);
            return null;
        }

        /* 
         * @param fn FlatMap success value or Do nothing if Failure (return this)
         * @return Try returned from FlatMap fn
         * @see com.aol.cyclops2.trycatch.Try#flatMap(java.util.function.Function)
         */
        @Override
        public <R> Try<R, X> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> fn) {
            return safeApply(() -> fn.apply(get()).toTry( (Class[]) classes ));

        }

        /* 
         * @param p Convert a Success to a Failure (with a null value for Exception) if predicate does not hold.
         *         
         * @return this if  Predicate holds, new Failure if not
         * @see com.aol.cyclops2.trycatch.Try#filter(java.util.function.Predicate)
         */
        @Override
        public Maybe<T> filter(final Predicate<? super T> p) {
            if (p.test(value))
                return Maybe.of(get());
            else
                return Maybe.none();
        }

        /* 
         * Does nothing (no error to recover from)
         * @see com.aol.cyclops2.trycatch.Try#recover(java.util.function.Function)
         */
        @Override
        public Success<T, X> recover(final Function<? super X, ? extends T> fn) {
            return this;
        }

        /* 
         * Does nothing (no error to recover from)
         * @see com.aol.cyclops2.trycatch.Try#recoverWith(java.util.function.Function)
         */
        @Override
        public Try<T, X> recoverWith(final Function<? super X, ? extends Try<T, X>> fn) {
            return this;
        }

        /* 
         * Does nothing (no error to recover from)
         * @see com.aol.cyclops2.trycatch.Try#recoverFor(java.lang.Class, java.util.function.Function)
         */
        @Override
        public Success<T, X> recoverFor(final Class<? extends X> t, final Function<? super X, ? extends T> fn) {
            return this;
        }

        /* 
         * Does nothing (no error to recover from)
         * @see com.aol.cyclops2.trycatch.Try#recoverWithFor(java.lang.Class, java.util.function.Function)
         */
        @Override
        public Success<T, X> recoverWithFor(final Class<? extends X> t, final Function<? super X, ? extends Success<T, X>> fn) {
            return this;
        }

        

        /* 
         *	
         *	@return Returns current value (ignores supplied value)
         * @see com.aol.cyclops2.trycatch.Try#orElse(java.lang.Object)
         */
        @Override
        public T orElse(final T value) {
            return get();
        }

        @Override
        public X failureGet() {
            throw new NoSuchElementException(
                                             "Can't call failureGet() on an instance of Try.Success");
        }

        /* 
         *	@param value (ignored)
         *	@return Returns current value (ignores Supplier)
         * @see com.aol.cyclops2.trycatch.Try#orElseGet(java.util.function.Supplier)
         */
        @Override
        public T orElseGet(final Supplier<? extends T> value) {
            return get();
        }

        /* 
         *	@return Optional of current value
         * @see com.aol.cyclops2.trycatch.Try#toOptional()
         */
        @Override
        public Optional<T> toOptional() {
            return Optional.of(value);
        }

        /* 
         *	@return Stream of current value
         * @see com.aol.cyclops2.trycatch.Try#toStream()
         */
        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.<T> of(value);
        }

        /* 
         *	@return true
         * @see com.aol.cyclops2.trycatch.Try#isSuccess()
         */
        @Override
        public boolean isSuccess() {
            return true;
        }

        /* 
         *	@return false
         * @see com.aol.cyclops2.trycatch.Try#isFailure()
         */
        @Override
        public boolean isFailure() {
            return false;
        }

        /* 
         *	@param consumer to recieve current value
         * @see com.aol.cyclops2.trycatch.Try#foreach(java.util.function.Consumer)
         */
        @Override
        public void forEach(final Consumer<? super T> consumer) {
            consumer.accept(value);

        }

        /* 
         *  does nothing no failure
         * @see com.aol.cyclops2.trycatch.Try#onFail(java.util.function.Consumer)
         */
        @Override
        public Try<T, X> onFail(final Consumer<? super X> consumer) {
            return this;
        }

        /* 
         *  does nothing no failure
         * @see com.aol.cyclops2.trycatch.Try#onFail(java.lang.Class, java.util.function.Consumer)
         */
        @Override
        public Try<T, X> onFail(final Class<? extends X> t, final Consumer<X> consumer) {
            return this;
        }

        /* 
         *  does nothing no failure
         * @see com.aol.cyclops2.trycatch.Try#throwException()
         */
        @Override
        public void throwException() {

        }

        /* 
         *  @return java.util.Optional#empty()
         * @see com.aol.cyclops2.trycatch.Try#toFailedOptional()
         */
        @Override
        public Optional<X> toFailedOptional() {
            return Optional.empty();
        }

        /* 
         *	@return empty Stream
         * @see com.aol.cyclops2.trycatch.Try#toFailedStream()
         */
        @Override
        public Stream<X> toFailedStream() {
            return Stream.of();
        }

        /* 
         *	does nothing - no failure
         * @see com.aol.cyclops2.trycatch.Try#foreachFailed(java.util.function.Consumer)
         */
        @Override
        public void forEachFailed(final Consumer<? super X> consumer) {

        }

        @Override
        public <T2, R> Try<R, X> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
            return app.toTry()
                      .visit(s -> safeApply(() -> success(fn.apply(get(), app.get()))), f -> Try.failure(null));

        }

        /* (non-Javadoc)
         * @see com.aol.cyclops2.trycatch.Try#when(java.util.function.Function, java.util.function.Function)
         */
        @Override
        public <R> R visit(final Function<? super T, ? extends R> success, final Function<? super X, ? extends R> failure) {
            return success.apply(get());
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof Success) {
                final Success s = (Success) o;
                return Objects.equals(this.value, s.value);
            }
            return false;
        }

    }

    /**
     * Class that represents the Failure of a Try
     * 
     * @author johnmcclean
     *
     * @param <T> Value type
     * @param <X> Error type
     */
    @RequiredArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Failure<T, X extends Throwable> implements Try<T, X> {
        @Override
        public String mkString() {
            return "Failure[" + error + "]";
        }

        private final X error;

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public Xor<X, T> toXor() {
            return Xor.secondary(error);

        }

        @Override
        public Ior<X, T> toIor() {
            return Ior.secondary(error);
        }

       

        @Override
        public X failureGet() {
            return error;
        }

       

        /* 
         *	@return throws an Exception
         * @see com.aol.cyclops2.trycatch.Try#get()
         */
        @Override
        public T get() {
            throw ExceptionSoftener.throwSoftenedException(error);

        }

        /* 
         *	@return this
         * @see com.aol.cyclops2.trycatch.Try#map(java.util.function.Function)
         */
        @Override
        public <R> Try<R, X> map(final Function<? super T, ? extends R> fn) {
            return (Failure) this;
        }

        /* 
         *	@return this
         * @see com.aol.cyclops2.trycatch.Try#flatMap(java.util.function.Function)
         */
        @Override
        public <R> Try<R, X> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> fn) {
            return (Try) this;
        }

        /* 
         *	@return Empty optional
         * @see com.aol.cyclops2.trycatch.Try#filter(java.util.function.Predicate)
         */
        @Override
        public Maybe<T> filter(final Predicate<? super T> p) {
            return Maybe.none();
        }

        /* 
         * FlatMap recovery function if exception is of specified type
         * 
         * @param t Type of exception to match against
         * @param fn Recovery FlatMap function. Map from a failure to a Success
         * @return Success from recovery function
         * @see com.aol.cyclops2.trycatch.Try#recoverWithFor(java.lang.Class, java.util.function.Function)
         */
        @Override
        public Try<T, X> recoverWithFor(final Class<? extends X> t, final Function<? super X, ? extends Success<T, X>> fn) {
            if (t.isAssignableFrom(error.getClass()))
                return recoverWith(fn);
            return this;
        }

        /* 
         * Recover if exception is of specified type
         * @param t Type of exception to match against
         * @param fn Recovery function
         * @return New Success
         * @see com.aol.cyclops2.trycatch.Try#recoverFor(java.lang.Class, java.util.function.Function)
         */
        @Override
        public Try<T, X> recoverFor(final Class<? extends X> t, final Function<? super X, ? extends T> fn) {
            if (t.isAssignableFrom(error.getClass()))
                return recover(fn);
            return this;
        }

        /* 
         * @param fn Recovery function - map from a failure to a Success.
         * @return new Success
         * @see com.aol.cyclops2.trycatch.Try#recover(java.util.function.Function)
         */
        @Override
        public Success<T, X> recover(final Function<? super X, ? extends T> fn) {
            return Try.success(fn.apply(error));
        }

        /* 
         * flatMap recovery
         * 
         * @param fn Recovery FlatMap function. Map from a failure to a Success
         * @return Success from recovery function
         * @see com.aol.cyclops2.trycatch.Try#recoverWith(java.util.function.Function)
         */
        @Override
        public Try<T, X> recoverWith(final Function<? super X, ? extends Try<T, X>> fn) {
            return fn.apply(error);
        }

       

        /* 
         *  @param value Return value supplied 
         * @return  supplied value
         * @see com.aol.cyclops2.trycatch.Try#orElse(java.lang.Object)
         */
        @Override
        public T orElse(final T value) {
            return value;
        }

        /* 
         * @param value from supplied Supplier 
         * @return value from supplier
         * @see com.aol.cyclops2.trycatch.Try#orElseGet(java.util.function.Supplier)
         */
        @Override
        public T orElseGet(final Supplier<? extends T> value) {
            return value.get();
        }

        /* 
         *	@return Optional.empty()
         * @see com.aol.cyclops2.trycatch.Try#toOptional()
         */
        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        /* 
         *	@return empty Stream
         * @see com.aol.cyclops2.trycatch.Try#toStream()
         */
        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.<T> empty();
        }

        /* 
         *	@return false
         * @see com.aol.cyclops2.trycatch.Try#isSuccess()
         */
        @Override
        public boolean isSuccess() {
            return false;
        }

        /*  
         *	@return true
         * @see com.aol.cyclops2.trycatch.Try#isFailure()
         */
        @Override
        public boolean isFailure() {
            return true;
        }

        /* 
         *	does nothing
         * @see com.aol.cyclops2.trycatch.Try#foreach(java.util.function.Consumer)
         */
        @Override
        public void forEach(final Consumer<? super T> consumer) {

        }

        /* 
         *	@param consumer is passed error
         *	@return this
         * @see com.aol.cyclops2.trycatch.Try#onFail(java.util.function.Consumer)
         */
        @Override
        public Try<T, X> onFail(final Consumer<? super X> consumer) {
            consumer.accept(error);
            return this;
        }

        /* 
         * @param t Class type of match Exception against
         * @param consumer Accept Exception if present
         * @return this
         * @see com.aol.cyclops2.trycatch.Try#onFail(java.lang.Class, java.util.function.Consumer)
         */
        @Override
        public Try<T, X> onFail(final Class<? extends X> t, final Consumer<X> consumer) {
            if (t.isAssignableFrom(error.getClass()))
                consumer.accept(error);
            return this;
        }

        /* 
         *	
         * @see com.aol.cyclops2.trycatch.Try#throwException()
         */
        @Override
        public void throwException() {
            ExceptionSoftener.throwSoftenedException(error);

        }

        /* 
         * @return Optional containing error
         * @see com.aol.cyclops2.trycatch.Try#toFailedOptional()
         */
        @Override
        public Optional<X> toFailedOptional() {

            return Optional.of(error);
        }

        /* 
         *	@return Stream containing error
         * @see com.aol.cyclops2.trycatch.Try#toFailedStream()
         */
        @Override
        public Stream<X> toFailedStream() {
            return Stream.of(error);
        }

        /* 
         * @param consumer that will accept error
         * @see com.aol.cyclops2.trycatch.Try#foreachFailed(java.util.function.Consumer)
         */
        @Override
        public void forEachFailed(final Consumer<? super X> consumer) {
            consumer.accept(error);

        }

        @Override
        public <T2, R> Try<R, X> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
            return (Try<R, X>) this;

        }

        /* (non-Javadoc)
         * @see com.aol.cyclops2.trycatch.Try#when(java.util.function.Function, java.util.function.Function)
         */
        @Override
        public <R> R visit(final Function<? super T, ? extends R> success, final Function<? super X, ? extends R> failure) {
            return failure.apply(error);
        }

        @Override
        public int hashCode() {
            return Objects.hash(error);
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof Failure) {
                final Failure s = (Failure) o;
                return Objects.equals(this.error, s.error);
            }
            return false;
        }
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> Try<R, X> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (Try<R, X>) MonadicValue.super.combine(app, fn);
    }

    /**
     * Equivalent to ap, but accepts an Iterable and takes the first value
     * only from that iterable.
     * 
     * @param app
     * @param fn
     * @return
     */
    @Override
    default <T2, R> Try<R, X> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Try.fromIterable(app)
                                                                            .visit(i -> Try.success(tuple.v2.apply(i)), () -> Try.failure(null)));
    }

    /**
     * Equivalent to ap, but accepts a Publisher and takes the first value
     * only from that publisher.
     * 
     * @param app
     * @param fn
     * @return
     */
    @Override
    default <T2, R> Try<R, X> zipP( final Publisher<? extends T2> app,final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                            .apply(v))).flatMap(tuple -> Try.fromPublisher(app)
                                                                            .visit(i -> Try.success(tuple.v2.apply(i)), () -> Try.failure(null)));

    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Try<R, X> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (Try<R, X>) MonadicValue.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> Try<Tuple2<T, U>, X> zipS(final Stream<? extends U> other) {

        return (Try) MonadicValue.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    default <U> Try<Tuple2<T, U>, X> zip(final Iterable<? extends U> other) {

        return (Try) MonadicValue.super.zip(other);
    }

}
