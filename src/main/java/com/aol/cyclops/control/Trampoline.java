package com.aol.cyclops.control;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops.types.Value;

/**
 * simple Trampoline implementation : inspired by excellent TotallyLazy Java 8 impl 
 * and Mario Fusco presentation
 * 
 * @author johnmcclean
 *
 * @param <T> Return type
 */
@FunctionalInterface
public interface Trampoline<T> extends Supplier<T>, Value<T> {

    /**
     * @return next stage in Trampolining
     */
    default Trampoline<T> bounce() {
        return this;
    }

    /**
     * @return The result of Trampoline execution
     */
    default T result() {
        return get();
    }

    @Override
    T get();

    @Override
    default Iterator<T> iterator() {
        return Arrays.asList(result())
                     .iterator();
    }

    /**
     * @return true if complete
     * 
     */
    default boolean complete() {
        return true;
    }

    /**
     * Created a completed Trampoline
     * 
     * @param result Completed result
     * @return Completed Trampoline
     */
    public static <T> Trampoline<T> done(T result) {
        return () -> result;
    }

    /**
     * Create a Trampoline that has more work to do
     * 
     * @param trampoline Next stage in Trampoline
     * @return Trampoline with more work
     */
    public static <T> Trampoline<T> more(Trampoline<Trampoline<T>> trampoline) {
        return new Trampoline<T>() {

            @Override
            public boolean complete() {
                return false;
            }

            @Override
            public Trampoline<T> bounce() {
                return trampoline.result();
            }

            public T get() {
                return trampoline(this);
            }

            T trampoline(Trampoline<T> trampoline) {

                return Stream.iterate(trampoline, Trampoline::bounce)
                             .filter(Trampoline::complete)
                             .findFirst()
                             .get()
                             .result();

            }
        };
    }
}
