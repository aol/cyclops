
package cyclops.control;

import static cyclops.monads.Witness.*;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.Value;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.recoverable.Recoverable;
import cyclops.async.Future;
import cyclops.control.lazy.Either;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import lombok.*;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import cyclops.function.Monoid;
import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.reactive.ValueSubscriber;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.function.Curry;
import cyclops.function.Fn4;
import cyclops.function.Fn3;

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
 * Support eager, lazy and reactive execution modes
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
                        .orElse("public");

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
         .recover(e->"public);
 *
 * }
 * </pre>
 *
 * By public Try does not catch exception within it's operators such as map / flatMap, to catch Exceptions in ongoing operations use @see {@link Try#of(Object, Class...)}
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
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class Try<T, X extends Throwable> implements  To<Try<T,X>>,
                                                      Recoverable<X,T>,
                                                      MonadicValue<T>{


    final Xor<X,T> xor;

    public Xor<X,T> asXor(){
        return xor;
    }

    public Try<T,X> recover(Supplier<? extends T> s){
        return recover(t->s.get());
    }
    /**
     * Construct a Try  that contains a singleUnsafe value extracted from the supplied reactive-streams Publisher, will catch any Exceptions
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
     * @return Try populated with takeOne value from Publisher
     */
    @SafeVarargs
    public static <T, X extends Throwable> Try<T, X> fromPublisher(final Publisher<T> pub, final Class<X>... classes) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toTry(classes);
    }

    /**
     * Construct a Try  that contains a singleUnsafe value extracted from the supplied reactive-streams Publisher
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
     * @return Try populated with takeOne value from Publisher
     */
    public static <T> Try<T, Throwable> fromPublisher(final Publisher<T> pub) {
        return new Try<>(Either.fromPublisher(pub));
    }


    /**
     * Construct a Try  that contains a singleUnsafe value extracted from the supplied Iterable
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
     * @return Try populated with takeOne value from Iterable
     */
    public static <T, X extends Throwable> Try<T, X> fromIterable(final Iterable<T> iterable) {
        return new Try<>(Either.fromIterable(iterable));
    }

    @Override
    public <R> Try<R,X> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (Try<R,X>)MonadicValue.super.zipWith(fn);
    }

    @Override
    public <R> Try<R,X> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (Try<R,X>)MonadicValue.super.zipWithS(fn);
    }

    @Override
    public <R> Try<R,X> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (Try<R,X>)MonadicValue.super.zipWithP(fn);
    }

    @Override
    public <R> Try<R,X> retry(final Function<? super T, ? extends R> fn) {
        return (Try<R,X>)MonadicValue.super.retry(fn);
    }

    @Override
    public <U> Try<Tuple2<T, U>,X> zipP(final Publisher<? extends U> other) {
        return (Try)MonadicValue.super.zipP(other);
    }

    @Override
    public <R> Try<R,X> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (Try<R,X>)MonadicValue.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    public <S, U> Try<Tuple3<T, S, U>,X> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (Try)MonadicValue.super.zip3(second,third);
    }

    @Override
    public <S, U, R> Try<R,X> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (Try<R,X>)MonadicValue.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4> Try<Tuple4<T, T2, T3, T4>,X> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (Try)MonadicValue.super.zip4(second,third,fourth);
    }

    @Override
    public <T2, T3, T4, R> Try<R,X> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Try<R,X>)MonadicValue.super.zip4(second,third,fourth,fn);
    }

    @Override
    public <R> Try<R,X> flatMapS(final Function<? super T, ? extends Stream<? extends R>> mapper) {
        return (Try<R,X>)MonadicValue.super.flatMapS(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> Try<R,X> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Try<R,X>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> Try<R,X> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
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
    public <T2, R1, R2, R> Try<R,X> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Try<R,X>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> Try<R,X> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Try<R,X>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> Try<R,X> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Try<R,X>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> Try<R,X> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Try<R,X>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toTry()
     */
    @Override
    public Try<T, Throwable> toTry() {
        return (Try<T, Throwable>) this;

    }




    /**
     * @return The exception returned in the Failure case, Implementations should throw NoSuchElementException if no failure is present
     */
    public X failureGet(){
        return xor.secondaryGet();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toXor()
     */
    @Override
    public Xor<X, T> toXor(){
        return xor;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#toIor()
     */
    @Override
    public Ior<X, T> toIor(){
        return xor.toIor();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    public <R> Try<R, X> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                     .apply(this);
    }

    //cojoin
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */
    @Override
    public Try<MonadicValue<T>, X> nest() {
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
    public Try<T, X> combine(final Monoid<T> monoid, final Try<? extends T, X> v2) {
        return unit(this.forEach2( t1 -> v2, (t1, t2) -> monoid
                                                            .apply(t1, t2)).orElseGet(() -> this.orElseGet(() -> monoid.zero())));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Applicative#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Applicative)
     */
    @Override
    public Try<T,X> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (Try<T,X>)MonadicValue.super.zip(combiner, app);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.Transformable#cast(java.lang.Class)
     */
    @Override
    public <U> Try<U, X> cast(final Class<? extends U> type) {
        return (Try<U, X>) MonadicValue.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    public <R> Try<R, X> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (Try<R, X>) MonadicValue.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#ofType(java.lang.Class)
     */
    @Override
    public <U> Maybe<U> ofType(final Class<? extends U> type) {

        return (Maybe<U>) MonadicValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    public Maybe<T> filterNot(final Predicate<? super T> fn) {

        return (Maybe<T>) MonadicValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#notNull()
     */
    @Override
    public Maybe<T> notNull() {

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
    public static <T, X extends Throwable> Try<T, X> failure(final X error) {
        return new Try<>(Xor.secondary(
                             error));
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

    public static <T, X extends Throwable> Try<T, X> success(final T value) {
        return new Try<>(Xor.primary(
                             value));
    }

    /**
     * @return Convert this Try to an Xor with the error type as the secondary value
     */
    public Xor<X, T> toXorWithError() {
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
    public <T> Try<T, X> unit(final T value) {
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
    public <R> R visit(Function<? super T, ? extends R> success, Function<? super X, ? extends R> failure){
        return xor.visit(failure,success);
    }

    /**
     * @return This monad, wrapped as AnyM of Success
     */
    public AnyMValue<tryType,T> anyM(){
        return AnyM.fromTry(this);
    }


    /**
     * @return Successful value or will throw Throwable (X) if Failire
     */
    @Override
    public T get(){
        return xor.get();
    }

    /**
     * Throw exception if Failure, do nothing if success
     */
    public void throwException(){
        if(xor.isSecondary())
            throw ExceptionSoftener.throwSoftenedException(xor.secondaryGet());
    }

    /**
     * @param value Return value supplied if Failure, otherwise return Success value
     * @return Success value or supplied value
     */
    @Override
    public T orElse(T value){
        return xor.orElse(value);
    }

    /**
     *
     * @param value from supplied Supplier if Failure otherwise return Success value
     * @return Success value
     */
    @Override
    public T orElseGet(Supplier<? extends T> value){
        return xor.orElseGet(value);
    }

    /**
     * @param fn Map success value from T to R. Do nothing if Failure (return this)
     * @return New Try with mapped value (Success) or this (Failure)
     */

    @Override
    public <R> Try<R, X> map(Function<? super T, ? extends R> fn){
        return new Try<>(xor).map(fn);
    }

    /**
     * @param fn FlatMap success value or Do nothing if Failure (return this)
     * @return Try returned from FlatMap fn
     */
    public <R> Try<R, X> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> fn){
        return new Try<>(xor.flatMap(fn));
    }

    /**
     * @param p Convert a Success to a Failure (with a null value for Exception) if predicate does not hold.
     *          Do nothing to a Failure
     * @return this if Success and Predicate holds, or if Failure. New Failure if Success and Predicate fails
     */
    @Override
    public Maybe<T> filter(Predicate<? super T> p){
        return xor.filter(p).toMaybe();
    }

    /**
     * @param consumer Accept Exception if present (Failure)
     * @return this
     */
    public Try<T, X> onFail(Consumer<? super X> consumer){
        return new Try<>(xor.secondaryPeek(consumer));
    }

    /**
     * @param t Class type of match Exception against
     * @param consumer Accept Exception if present (Failure) and if class types match
     * @return this
     */
    public Try<T, X> onFail(Class<? extends X> t, Consumer<X> consumer){
        return new Try<>(xor.secondaryPeek(error->{
            if (t.isAssignableFrom(error.getClass()))
                 consumer.accept(error);
        }));
    }

    /**
     * @param fn Recovery function - map from a failure to a Success.
     * @return new Try
     */
    public Try<T, X> recover(Function<? super X, ? extends T> fn){
        return new Try<>(xor.secondaryToPrimayMap(fn));
    }

    /**
     * flatMap recovery
     *
     * @param fn Recovery FlatMap function. Map from a failure to a Success
     * @return Success from recovery function
     */
    public Try<T, X> recoverWith(Function<? super X, ? extends Try<T, X>> fn){
        return new Try<>(xor.secondaryToPrimayFlatMap(fn.andThen(t->t.xor)));
    }

    /**
     * Recover if exception is of specified type
     * @param t Type of exception to match against
     * @param fn Recovery function
     * @return New Success if failure and types match / otherwise this
     */
    public Try<T, X> recoverFor(Class<? extends X> t, Function<? super X, ? extends T> fn){
        return new Try<T,X>(xor.secondaryToPrimayFlatMap(x->{
            if (t.isAssignableFrom(x.getClass()))
                 return Xor.primary(fn.apply(x));
            return xor;
        }));
    }





    /**
     * @return Optional present if Success, Optional empty if failure
     */
    @Override
    public Optional<T> toOptional(){
        return xor.toOptional();
    }

    /**
     * @return Stream with value if Sucess, Empty Stream if failure
     */
    @Override
    public ReactiveSeq<T> stream(){
        return xor.toStream();
    }

    /**
     * @return Optional present if Failure (with Exception), Optional empty if Success
     */
    public Optional<X> toFailedOptional(){
        return xor.swap().toOptional();
    }

    /**
     * @return Stream with error if Failure, Empty Stream if success
     */
    public Stream<X> toFailedStream(){
        return xor.swap().toStream();
    }

    /**
     * @return true if Success / false if Failure
     */
    public boolean isSuccess(){
        return xor.isPrimary();
    }

    /**
     * @return True if Failure / false if Success
     */
    public boolean isFailure(){
        return !xor.isPrimary();
    }

    /**
     * @param consumer Accept value if Success / not called on Failure
     */
    @Override
    public void forEach(Consumer<? super T> consumer){
        xor.forEach(consumer);
    }

    /**
     * @param consumer Accept value if Failure / not called on Failure
     */
    public void forEachFailed(Consumer<? super X> consumer){
        xor.swap().forEach(consumer);
    }

    @Override
    public boolean isPresent() {
        return isSuccess();
    }

    /**
     * @param consumer Accept value if Success
     * @return this
     */
    @Override
    public Try<T, X> peek(final Consumer<? super T> consumer) {
        forEach(consumer);
        return this;
    }

    /**
     * @param consumer Accept Exception if Failure
     * @return this
     */
    public Try<T, X> peekFailed(final Consumer<? super X> consumer) {
        forEachFailed(consumer);
        return this;
    }

    @Override
    public Iterator<T> iterator() {

        return MonadicValue.super.iterator();
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
                if (finalResult.isFailure())
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
         * Closeables (lazy individually or within an iterable) will be closed
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
         * safely close any Closeables supplied during init (lazy individually or inside an iterable)
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



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> Try<R, X> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (Try<R, X>) MonadicValue.super.combine(app, fn);
    }

    /**
     * Equivalent to ap, but accepts an Iterable and takes the takeOne value
     * only from that iterable.
     *
     * @param app
     * @param fn
     * @return
     */
    @Override
    public <T2, R> Try<R, X> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return new Try<R, X>(xor.zip(app, fn));
    }

    /**
     * Equivalent to ap, but accepts a Publisher and takes the takeOne value
     * only from that publisher.
     * 
     * @param app
     * @param fn
     * @return
     */
    @Override
    public <T2, R> Try<R, X> zipP( final Publisher<? extends T2> app,final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new Try<R, X>(xor.zipP(app, fn));
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream, java.util.function.BiFunction)
     */
    @Override
    public <U, R> Try<R, X> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (Try<R, X>) MonadicValue.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.reactiveStream.Stream)
     */
    @Override
    public <U> Try<Tuple2<T, U>, X> zipS(final Stream<? extends U> other) {

        return (Try) MonadicValue.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    public <U> Try<Tuple2<T, U>, X> zip(final Iterable<? extends U> other) {

        return (Try) MonadicValue.super.zip(other);
    }

}
