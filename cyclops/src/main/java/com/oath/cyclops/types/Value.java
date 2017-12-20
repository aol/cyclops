package com.oath.cyclops.types;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import com.oath.cyclops.types.foldable.Visitable;
import cyclops.control.*;
import cyclops.control.LazyEither;
import cyclops.control.Maybe;
import cyclops.function.Function0;
import cyclops.function.Monoid;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.types.reactive.ValueSubscriber;

/**
 * A data type that stores at most 1 Values
 *
 * @author johnmcclean
 *
 * @param <T> Data type of element in this value
 */
@FunctionalInterface
public interface Value<T> extends Visitable<T>, Iterable<T>, Publisher<T> {


    default  Function0<T> asSupplier(T alt){
        return ()-> orElse(alt);
    }
    default boolean isPresent(){
        return visit(p->true,()->false);
    }

     default T orElse(T alt) {
        return visit(p->p,()->alt);
     }

     default T  orElseGet(Supplier<? extends T> s) {
         return visit(p->p,()->s.get());
     }


    default T fold(final Monoid<T> reducer) {
        return orElse(reducer.zero());
    }
    /* An Iterator over the list returned from toList()
         *
         *  (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
    @Override
    default Iterator<T> iterator() {
        boolean[] complete = {false};
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return !complete[0]
                           && visit(p->true,()->false);
            }

            @Override
            public T next() {
                complete[0]=true;
                return visit(p->p,()->null);
            }
        };

    }


    /**
     * @return A factory class generating Values from reactive-streams Subscribers
     */
    default ValueSubscriber<T> newSubscriber() {
        return ValueSubscriber.subscriber();
    }

    /* (non-Javadoc)
     * @see org.reactivestreams.Publisher#forEachAsync(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> sub) {
        sub.onSubscribe(new Subscription() {

            AtomicBoolean running = new AtomicBoolean(
                                                      true);
            AtomicBoolean cancelled = new AtomicBoolean(false);

            @Override
            public void request(final long n) {

                if (n < 1) {
                    sub.onError(new IllegalArgumentException(
                                                             "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                }

                if (!running.compareAndSet(true, false)) {

                    return;
                }
                try {
                    Iterator<T> it = iterator();
                    if(it.hasNext()) {
                        T value = it.next();
                        if (!cancelled.get())
                            sub.onNext(value);
                    }

                } catch (final Throwable t) {
                    sub.onError(t);

                }
                try {
                    sub.onComplete();

                } finally {

                }

            }

            @Override
            public void cancel() {

                cancelled.set(true);

            }

        });

    }




    default <R> R transform(Function<? super Value<? super T>, ? extends R> fn){
        return fn.apply(this);
    }

    default ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(this);
    }




    /**
     * Convert to an Xor where the left value will be used if no right value is present
     *
    * @param secondary Value to use in case no right value is present
    * @return Right Either with same value as this Value, or a Left Either with the provided Value if this Value is zero
    */
    default <ST> Either<ST, T> toEither(final ST secondary) {
        return visit(p-> Either.right(p),()-> Either.left(secondary));

    }
    default LazyEither<Throwable, T> toLazyEither() {
       return LazyEither.fromPublisher(this);
    }

    /**
     * Lazily convert this Value to an Either.right instance
     */
    default <LT> LazyEither<LT,T> toRight(T alt){
        return LazyEither.fromIterable(this,alt);
    }
    /**
     * Lazily convert this Value to an Either.left instance
     */
    default <RT> LazyEither<T,RT> toLeft(T alt){
        return LazyEither.<RT,T>fromIterable(this,alt)
                     .swap();
    }
    /**
     * @param throwable Exception to use if this Value is zero
     * @return Try that has the same value as this Value or the provided Exception
     */
    default <X extends Throwable> Try<T, X> toTry(final X throwable) {
        return Try.fromEither(toTry().asEither().mapLeft(t->throwable));

    }

    /**
     * @return This Value converted to a Try. If this Value is zero the Try will contain a NoSuchElementException
     */
    default Try<T, Throwable> toTry() {
        return Try.fromPublisher(this);
    }

    /**
     * Convert this Value to a Try that will catch the provided exception types on subsequent operations
     *
     * @param classes Exception classes to catch on subsequent operations
     * @return This Value to converted to a Try.
     */
    default <X extends Throwable> Try<T, X> toTry(final Class<X>... classes) {
        return Try.fromPublisher(this,classes);
    }







    default Optional<T> toOptional(){
        return visit(Optional::of,Optional::empty);
    }

    default Maybe<T> toMaybe() {
        return Maybe.fromPublisher(this);
    }


    default Option<T> toOption() {
        return visit(Option::some,Option::none);
    }


    /**
     * Returns the class name and the name of the subclass, if there is any value, the value is showed between square brackets.
     * @return String
     */
    default String mkString() {

        return visit(p->getClass().getSimpleName() + "[" + p + "]",()->getClass().getSimpleName() + "[]");
    }


    /**
     * Write each element within this Folds in turn to the supplied PrintStream
     *
     * @param str PrintStream to tell to
     */
    default void print(final PrintStream str) {
        stream().print(str);
    }

    /**
     * Write each element within this Folds in turn to the supplied PrintWriter
     *
     * @param writer PrintWriter to tell to
     */
    default void print(final PrintWriter writer) {
        stream().print(writer);
    }

    /**
     *  Print each value in this Folds to the console in turn (left-to-right)
     */
    default void printOut() {
        stream().printOut();
    }

    /**
     *  Print each value in this Folds to the error console in turn (left-to-right)
     */
    default void printErr() {
        stream().printErr();
    }
    default <R, A> R collect(final Collector<? super T, A, R> collector) {

        return stream().collect(collector);
    }

    default void forEach(Consumer<? super T> c){
        visit(p->{
            c.accept(p);
            return null;
        },()->null);
    }
    /**
     * Use the value stored in this Value to seed a Stream generated from the provided function
     *
     * @param fn Function to generate a Stream
     * @return Stream generated from a seed value (the Value stored in this Value) and the provided function
     */
    default ReactiveSeq<T> iterate(final UnaryOperator<T> fn,T alt) {
        return asSupplier(alt).iterate(fn);
    }

    /**
     * @return A Stream that repeats the value stored in this Value over and over
     */
    default ReactiveSeq<T> generate(T alt) {
        return asSupplier(alt).generate();
    }
}
