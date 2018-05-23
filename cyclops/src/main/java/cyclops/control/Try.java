
package cyclops.control;


import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import com.oath.cyclops.matching.Sealed2;
import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.OrElseValue;
import com.oath.cyclops.types.Value;

import com.oath.cyclops.types.factory.Unit;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.Transformable;
import com.oath.cyclops.types.recoverable.RecoverableFrom;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.function.*;

import com.oath.cyclops.hkt.DataWitness.tryType;
import cyclops.reactive.ReactiveSeq;

import lombok.*;
import lombok.experimental.Wither;
import org.reactivestreams.Publisher;

import com.oath.cyclops.util.ExceptionSoftener;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Success biased Try monad.
 *
 * Key features
 *
 * 1. Lazy / Eager / Reactive operational modes
 * 2. Illegal states are unrepresentable
 * 3. Exception types are explicitly declared
 * 4. Exceptions during execution are not caught be default
 * 5. To handle exceptions during operations provide the Exception classes explicitly on initiatialization
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
 *   Try.withResources(()->new BufferedReader(new FileReader("file.txt")),
                        this::read,
                        FileNotFoundException.class,IOException.class)
                        .map(this::processData)
                        .recover(e->"public);
 *
 * }
 * </pre>
 *
 * By public Try does not catch exception within it's operators such as transform / flatMap, to catch Exceptions in ongoing operations use @see {@link Try#success(Object, Class...)}
 * <pre>
 * {@code
 *  Try.success(2, RuntimeException.class)
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
                                                     RecoverableFrom<X,T>,Value<T>,
                                                     Unit<T>, Transformable<T>, Filters<T>,
                                                     Sealed2<T,X>,
                                                     OrElseValue<T,Try<T,X>>,
                                                     Higher2<tryType,X,T> {


    public final int arity(){
        return 2;
    }
    final Either<X,T> xor;
    @Wither(AccessLevel.PRIVATE)
    private final Class<? extends Throwable>[] classes;

    public Either<X,T> asEither(){
        return xor;
    }

    @SafeVarargs
    public final Try<T,X> withExceptions(Class<? extends X>... toCatch){
        return withClasses(toCatch);
    }

    public static  <X extends Throwable,T,R> Try<R,X> tailRec(T initial, Function<? super T, ? extends Try<? extends Either<T, R>,X>> fn){
        Try<? extends Either<T, R>,X> next[] = new Try[1];
        next[0] = Try.success(Either.left(initial));
        boolean cont = true;
        do {
            cont = next[0].visit(p -> p.visit(s -> {
                next[0] = fn.apply(s);
                return true;
            }, pr -> false), () -> false);
        } while (cont);
        return next[0].map(x->x.orElse(null));
    }
    public static <X extends Throwable,T> Higher<Higher<tryType,X>, T> widen(Try<T,X> narrow) {
        return narrow;
    }

    public Trampoline<Either<X,T>> toTrampoline() {
        return xor.toTrampoline();
    }


    /**
     * Retry a transformation if it fails. Default settings are to retry up to 7
     * times, with an doubling backoff period starting @ 2 seconds delay before
     * retry.
     *
     *
     * @param fn
     *            Function to retry if fails
     *
     */
    public <R> Try<R,Throwable> retry(final Function<? super T, ? extends R> fn) {
        return retry(fn, 7, 2, TimeUnit.SECONDS);
    }

    /**
     * Retry a transformation if it fails. Retries up to <b>retries</b>
     * times, with an doubling backoff period starting @ <b>delay</b> TimeUnits delay before
     * retry.
     *
     *
     * @param fn
     *            Function to retry if fails
     * @param retries
     *            Number of retries
     * @param delay
     *            Delay in TimeUnits
     * @param timeUnit
     *            TimeUnit to use for delay
     */
    public <R> Try<R,Throwable> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        Try<T,Throwable> narrow = this.mapFailure(x->x);
        final Function<T, Try<R,Throwable>> retry = t -> {
            final long[] sleep = { timeUnit.toMillis(delay) };
            Throwable exception = null;
            for (int count = retries; count >=0; count--) {
                try {
                    return Try.success(fn.apply(t));
                } catch (final Throwable e) {
                    exception = e;
                    ExceptionSoftener.softenRunnable(() -> Thread.sleep(sleep[0]))
                        .run();
                    sleep[0] = sleep[0] * 2;
                }
            }
            return Try.failure(exception);


        };
        return narrow.flatMap(retry);
    }
    @Override
    public void subscribe(Subscriber<? super T> sub) {

        xor.nestedEval().subscribe(new Subscriber<Either<X, T>>() {
            boolean onCompleteSent = false;
            @Override
            public void onSubscribe(Subscription s) {
                sub.onSubscribe(s);
            }

            @Override
            public void onNext(Either<X, T> pts) {
                if(pts.isRight()){
                    T v = pts.orElse(null);
                    if(v!=null)
                        sub.onNext(v);
                }
                if(pts.isLeft()){
                    X v = pts.swap().orElse(null);
                    if(v!=null)
                        sub.onError(v);
                }
                else if(!onCompleteSent){
                    sub.onComplete();

                }
            }

            @Override
            public void onError(Throwable t) {
                sub.onError(t);
            }

            @Override
            public void onComplete() {
                if(!onCompleteSent){
                    sub.onComplete();
                    onCompleteSent =true;
                }
            }
        });
    }



    public Try<T,X> recover(Supplier<? extends T> s){
        return recover(t->s.get());
    }


    public static <T, X extends Throwable> Try<T, X> fromEither(final Either<X,T> pub) {
        return new Try<>(pub,new Class[0]);
    }
    /**
     * Construct a Try  that contains a single value extracted from the supplied reactive-streams Publisher, will catch any Exceptions
     * of the provided types
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> stream =  Spouts.of(1,2,3);

        Try<Integer,Throwable> recover = Try.fromPublisher(stream, RuntimeException.class);

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
        return new Try<T,X>(LazyEither.fromPublisher(pub).<X>mapLeft(t->{
            if (classes.length == 0)
                return (X) t;
            val error = Stream.of(classes)
                .filter(c -> c.isAssignableFrom(t.getClass()))
                .findFirst();
            if (error.isPresent())
                return (X) t;
            else
                throw ExceptionSoftener.throwSoftenedException(t);
        }),classes);
    }

    /**
     * Construct a Try  that contains a single value extracted from the supplied reactive-streams Publisher
     *
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> stream =  Spouts.of(1,2,3);

         Try<Integer,Throwable> recover = Try.fromPublisher(stream);

        //Try[1]
     *
     * }
     * </pre>
     *
     * @param pub Publisher to extract value from
     * @return Try populated with first value from Publisher
     */
    public static <T> Try<T, Throwable> fromPublisher(final Publisher<T> pub) {
        return new Try<>(LazyEither.fromPublisher(pub),new Class[0]);
    }


    /**
     * Construct a Try  that contains a single value extracted from the supplied Iterable
     *
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Try<Integer,Throwable> recover = Try.fromIterable(stream);

         //Try[1]
     *
     * }
     * </pre>
     *
     * @param iterable Iterable to extract value from
     * @return Try populated with first value from Iterable
     */
    public static <T, X extends Throwable> Try<T, X> fromIterable(final Iterable<T> iterable, T alt) {
        if(iterable instanceof Try){
            return (Try)iterable;
        }
        return new Try<>(LazyEither.fromIterable(iterable,alt), new Class[0]);
    }






    public <T2, R1, R2, R3, R> Try<R,X> forEach4(Function<? super T, ? extends Try<R1,X>> value1,
                                                 BiFunction<? super T, ? super R1, ? extends Try<R2,X>> value2,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends Try<R3,X>> value3,
                                                 Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMap(in-> {

            Try<R1,X> a = value1.apply(in);
            return a.flatMap(ina-> {
                Try<R2,X> b = value2.apply(in,ina);
                return b.flatMap(inb-> {
                    Try<R3,X> c= value3.apply(in,ina,inb);
                    return c.map(in2->yieldingFunction.apply(in,ina,inb,in2));
                });

            });

        });
    }




    public <T2, R1, R2, R> Try<R,X> forEach3(Function<? super T, ? extends Try<R1,X>> value1,
                                             BiFunction<? super T, ? super R1, ? extends Try<R2,X>> value2,
                                             Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {

            Try<R1,X> a = value1.apply(in);
            return a.flatMap(ina-> {
                Try<R2,X> b = value2.apply(in,ina);
                return b.map(in2->yieldingFunction.apply(in,ina, in2));
            });

        });
    }



    public <R1, R> Try<R,X> forEach2(Function<? super T, ? extends Try<R1,X>> value1,
                                     BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMap(in-> {
            Try<R1,X> b = value1.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
    }




    @Override
    public Try<T, Throwable> toTry() {
        return (Try<T, Throwable>) this;

    }





    public Option<X> failureGet(){
        return xor.getLeft();
    }


    public Either<X, T> toEither(){
        return xor;
    }


    public Ior<X, T> toIor(){
        return xor.toIor();
    }


    public <R> Try<R, X> coflatMap(final Function<? super Try<T,X>, R> mapper) {
        return mapper.andThen(r -> unit(r))
            .apply(this);
    }



    @Override
    public <U> Option<U> ofType(final Class<? extends U> type) {

        return (Option<U>) Filters.super.ofType(type);
    }


    @Override
    public Option<T> filterNot(final Predicate<? super T> fn) {

        return (Option<T>) Filters.super.filterNot(fn);
    }


    @Override
    public Option<T> notNull() {

        return (Option<T>) Filters.super.notNull();
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
        return new Try<>(Either.left(
            error),new Class[0]);
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
        return new Try<>(Either.right(
            value),new Class[0]);
    }
    @SafeVarargs
    public static <T, X extends Throwable> Try<T, X> success(final T value,final Class<? extends Throwable>... classes) {
        return new Try<>(Either.right(
            value),classes);
    }


    public Either<X, T> toEitherWithError() {
        return xor;
    }


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





    public Option<T> get(){
        return xor.get();
    }


    @Override
    public T orElse(T value){
        return xor.orElse(value);
    }


    @Override
    public T orElseGet(Supplier<? extends T> value){
        return xor.orElseGet(value);
    }


    @Override
    public <R> Try<R, X> map(Function<? super T, ? extends R> fn){
        return new Try<>(xor.flatMap(i->safeApply(i, fn,classes)),classes);
    }


    /**
     * Perform a mapping operation that may catch the supplied Exception types
     * The supplied Exception types are only applied during this map operation
     *
     * @param fn mapping function
     * @param classes exception types to catch
     * @param <R> return type of mapping function
     * @return Try with result or caught exception
     */
    @SafeVarargs
    public final <R> Try<R,X> mapOrCatch(CheckedFunction<? super T, ? extends R,X> fn, Class<? extends X>... classes){
        return new Try<R,X>(xor.flatMap(i ->safeApply(i, fn.asFunction(),classes)),this.classes);
    }


    public <XR extends Throwable> Try<T, XR> mapFailure(Function<? super X, ? extends XR> fn){
        return new Try<>(xor.mapLeft(i->fn.apply(i)),new Class[0]);
    }

    /**
     * @param fn FlatMap success value or do nothing if Failure (return this)
     * @return Try returned from FlatMap fn
     */
    public <R> Try<R, X> flatMap(Function<? super T, ? extends Try<? extends R,X>> fn){
        return new Try<>(xor.flatMap(i->safeApplyM(i, fn,classes).toEither()),classes);
    }

    /**
     * Perform a flatMapping operation that may catch the supplied Exception types
     * The supplied Exception types are only applied during this map operation
     *
     * @param fn flatMapping function
     * @param classes exception types to catch
     * @param <R> return type of mapping function
     * @return Try with result or caught exception
     */
    @SafeVarargs
    public final <R> Try<R, X> flatMapOrCatch(CheckedFunction<? super T, ? extends Try<? extends R,X>,X> fn,Class<? extends X>... classes){
        return new Try<>(xor.flatMap(i->safeApplyM(i, fn.asFunction(),classes).toEither()),classes);
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
    public Try<T, X> filter(Predicate<? super T> test, Function<? super T, ? extends X> errorGenerator){
        return flatMap(e->test.test(e) ? Try.success(e) : Try.failure(errorGenerator.apply(e)));
    }
    /**
     * @param consumer Accept Exception if present (Failure)
     * @return this
     */
    public Try<T, X> onFail(Consumer<? super X> consumer){
        return new Try<>(xor.peekLeft(consumer),classes);
    }

    /**
     * @param t Class type of Exception to handle
     * @param consumer Accept Exception if present (Failure) and if class types fold
     * @return this
     */
    public Try<T, X> onFail(Class<? extends X> t, Consumer<X> consumer){
        return new Try<>(xor.peekLeft(error->{
            if (t.isAssignableFrom(error.getClass()))
                consumer.accept(error);
        }),classes);
    }

    /**
     * @param fn Recovery function - transform from a failure to a Success.
     * @return new Try
     */
    public Try<T, X> recover(Function<? super X, ? extends T> fn){
        return new Try<>(xor.mapLeftToRight(fn),classes);
    }

    /**
     * flatMap recovery
     *
     * @param fn Recovery FlatMap function. Map from a failure to a Success
     * @return Success from recovery function
     */
    public Try<T, X> recoverFlatMap(Function<? super X, ? extends Try<T, X>> fn){
        return new Try<>(xor.flatMapLeftToRight(fn.andThen(t->t.xor)),classes);
    }
    public Try<T, X> recoverFlatMapFor(Class<? extends X> t,Function<? super X, ? extends Try<T, X>> fn){
        return new Try<T,X>(xor.flatMapLeftToRight(x->{
            if (t.isAssignableFrom(x.getClass()))
                return fn.apply(x).xor;
            return xor;
        }),classes);
    }

    @SafeVarargs
    public static <T extends AutoCloseable,R,X extends Throwable> Try<R, X> withResources(CheckedSupplier<T,X> rs,
                                                                                          CheckedFunction<? super T,? extends R,X> fn,Class<? extends X>... classes)
    {
        try{
            T in = ExceptionSoftener.softenSupplier(() -> rs.get()).get();
            try {
                return Try.success(fn.apply(in));
            } catch (Throwable t) {
                return handleError(t, classes);
            } finally {
                ExceptionSoftener.softenRunnable(() -> in.close()).run();

            }
        } catch (Throwable e) {
            return handleError(e, classes);
        }
    }

    private static <R, X extends Throwable> Try<R, X> handleError(Throwable t, Class<? extends X>[] classes) {
        Either<Throwable, ? extends R> x = Either.left(orThrow(Stream.of(classes)
                .filter(c -> c.isAssignableFrom(t.getClass()))
                .map(c -> t)
                .findFirst(),
            t));
        return (Try<R, X>) Try.fromEither(x);
    }

    @SafeVarargs
    public static <T1 extends AutoCloseable,T2 extends AutoCloseable,R,X extends Throwable> Try<R, X> withResources(CheckedSupplier<T1,X> rs1,
                                                                                                                    CheckedSupplier<T2,X> rs2,
                                                                                                                    CheckedBiFunction<? super T1,? super T2,? extends R,X> fn,
                                                                                                                    Class<? extends X>... classes){
        T1 t1 = ExceptionSoftener.softenSupplier(()->rs1.get()).get();
        T2 t2 = ExceptionSoftener.softenSupplier(()->rs2.get()).get();
        try {
            try {
                return Try.success(fn.apply(t1, t2));
            } catch (Throwable t) {
                return handleError(t, (Class<? extends X>[]) classes);
            } finally {
                ExceptionSoftener.softenRunnable(() -> t1.close()).run();
                ExceptionSoftener.softenRunnable(() -> t2.close()).run();
            }
        }catch(Throwable e){
            return handleError(e, (Class<? extends X>[]) classes);
        }
    }
    public static <T extends AutoCloseable,R> Try<R, Throwable> withResources(cyclops.function.checked.CheckedSupplier<T> rs,
                                                                              cyclops.function.checked.CheckedFunction<? super T,? extends R> fn){
        try {
            T in = ExceptionSoftener.softenSupplier(rs).get();
            try {
                return Try.success(fn.apply(in));
            } catch (Throwable t) {
                return Try.failure(t);
            } finally {
                ExceptionSoftener.softenRunnable(() -> in.close()).run();

            }
        }catch(Throwable e){
            return Try.failure(e);
        }
    }
    public static <T1 extends AutoCloseable,T2 extends AutoCloseable,R> Try<R, Throwable> withResources(cyclops.function.checked.CheckedSupplier<T1> rs1,
                                                                                                        cyclops.function.checked.CheckedSupplier<T2> rs2,
                                                                                                        cyclops.function.checked.CheckedBiFunction<? super T1,? super T2,? extends R> fn){
        try {


            T1 t1 = ExceptionSoftener.softenSupplier(rs1).get();
            T2 t2 = ExceptionSoftener.softenSupplier(rs2).get();
            try {
                return Try.success(fn.apply(t1, t2));
            } catch (Throwable t) {
                return Try.failure(t);
            } finally {
                ExceptionSoftener.softenRunnable(() -> t1.close()).run();
                ExceptionSoftener.softenRunnable(() -> t2.close()).run();
            }
        }catch(Throwable e){
            return Try.failure(e);
        }
    }
    /**
     * Recover if exception is of specified type
     * @param t Type of exception to fold against
     * @param fn Recovery function
     * @return New Success if failure and types fold / otherwise this
     */
    public Try<T, X> recoverFor(Class<? extends X> t, Function<? super X, ? extends T> fn){
        return new Try<T,X>(xor.flatMapLeftToRight(x->{
            if (t.isAssignableFrom(x.getClass()))
                return Either.right(fn.apply(x));
            return xor;
        }),classes);
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
        return xor.stream();
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
        return xor.swap().stream();
    }

    /**
     * @return true if Success / false if Failure
     */
    public boolean isSuccess(){
        return xor.isRight();
    }

    /**
     * @return True if Failure / false if Success
     */
    public boolean isFailure(){
        return !xor.isRight();
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

    public <T2, R> Try<R,X> zip(final Try<T2,X> app, final BiFunction<? super T, ? super T2, ? extends R> fn){
        return flatMap(t->app.map(t2->fn.apply(t,t2)));
    }
    public <T2, R> Try<R,X> zip(final Either<X,T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn){
        return Try.fromEither(xor.zip(app,fn));
    }
    public <T2, R> Try<R,X> zip(final Ior<X,T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn){
        return Try.fromEither(xor.zip(app,fn));
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

        return stream().iterator();
    }



    /**
     * Try to execute supplied Supplier and will Catch specified Excpetions or java.lang.Exception
     * if none specified.
     *
     * @param cf CheckedSupplier to recover to execute
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
     * @param cf CheckedRunnable to recover to execute
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


    @Override
    public <R> R fold(Function<? super T, ? extends R> fn1, Function<? super X, ? extends R> fn2) {
        return xor.fold(fn2,fn1);
    }

    @Override
    public <R> R visit(Function<? super T, ? extends R> present, Supplier<? extends R> absent) {
        return xor.visit(present,absent);
    }




    public static interface CheckedFunction<T, R, X extends Throwable> {
        public R apply(T t) throws X;
        default Function<T,R> asFunction(){
            return i->{
                try{
                    return apply(i);
                }catch(Throwable t){
                    throw ExceptionSoftener.throwSoftenedException(t);
                }
            };
        }
    }
    public static interface CheckedBiFunction<T1, T2, R, X extends Throwable> {
        public R apply(T1 t,T2 t2) throws X;
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
     * Flatten a nest Try Structure
     * @return Lowest nest Try
     * @see com.oath.cyclops.trycatch.Try#flatten()
     */

    public static <T,X extends Throwable> Try<T, X> flatten(Try<? extends Try<T,X>,X> nested) {
        return nested.flatMap(Function.identity());
    }




    private <R> Try<? extends R,X> safeApplyM(T in,final Function<? super T,? extends Try<? extends R,X>> s,Class<? extends Throwable>[] classes) {
        try {
            return s.apply(in);
        } catch (final Throwable t) {
            return handleError(t, (Class<? extends X>[]) classes);

        }
    }
    private <R> Either<X,R> safeApply(T in, final Function<? super T,? extends R> s,Class<? extends Throwable>[] classes) {
        try {
            return Either.right(s.apply(in));
        } catch (final Throwable t) {
            return (Either) Either.left(orThrow(Stream.of(classes)
                    .filter(c -> c.isAssignableFrom(t.getClass()))
                    .map(c -> t)
                    .findFirst(),
                t));

        }
    }


    private static Throwable orThrow(final Optional<Throwable> findFirst, final Throwable t) {
        if (findFirst.isPresent())
            return findFirst.get();
        throw ExceptionSoftener.throwSoftenedException(t);

    }

    @Override
    public String mkString(){
        return toString();
    }
    @Override
    public String toString() {
        return xor.visit(s->"Failure["+s.toString()+"]",p->"Success["+p.toString()+"]");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Try<?, ?> aTry = (Try<?, ?>) o;

        if (!xor.equals(aTry.xor)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return xor.hashCode();
    }


    public static <T,X extends Throwable> Try<T,X> narrowK2(final Higher2<tryType, X,T> t) {
        return (Try<T,X>)t;
    }
    public static <T,X extends Throwable> Try<T,X> narrowK(final Higher<Higher<tryType, X>,T> t) {
        return (Try)t;
    }



}
