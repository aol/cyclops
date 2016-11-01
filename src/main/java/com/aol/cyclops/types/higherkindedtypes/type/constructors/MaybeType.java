package com.aol.cyclops.types.higherkindedtypes.type.constructors;

import java.util.Objects;

import org.derive4j.hkt.Higher;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Maybe.Just;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Simulates Higher Kinded Types for Maybe's
 * 
 * MaybeType is a Maybe and a Higher Kinded Type (MaybeType.µ,T)
 * 
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Maybe
 */

public interface MaybeType<T> extends Higher<MaybeType.µ, T>, Maybe<T> {
    /**
     * Witness type
     * 
     * @author johnmcclean
     *
     */
    public static class µ {
    }

    /**
     * Construct an Maybe which contains the provided (non-null) value.
     * Alias for @see {@link Maybe#of(Object)}
     * 
     * <pre>
     * {@code 
     * 
     *    Maybe<Integer> some = Maybe.just(10);
     *    some.map(i->i*2);
     * }
     * </pre>
     * 
     * @param value Value to wrap inside a Maybe
     * @return Maybe containing the supplied value
     */
    static <T> MaybeType<T> just(final T value) {
        return of(value);
    }

    /**
     * Construct an Maybe which contains the provided (non-null) value
     * Equivalent to @see {@link Maybe#just(Object)}
     * <pre>
     * {@code 
     * 
     *    Maybe<Integer> some = Maybe.of(10);
     *    some.map(i->i*2);
     * }
     * </pre>
     * 
     * @param value Value to wrap inside a Maybe
     * @return Maybe containing the supplied value
     */
    static <T> MaybeType<T> of(final T value) {
       return widen(Maybe.of(value));
    }

    /**
     * <pre>
     * {@code 
     *    Maybe<Integer> maybe  = Maybe.ofNullable(null);
     *    //None
     *     
     *    Maybe<Integer> maybe = Maybe.ofNullable(10);
     *    //Maybe[10], Some[10]
     * 
     * }
     * </pre>
     * 
     * 
     * @param value
     * @return
     */
    static <T> MaybeType<T> ofNullable(final T value) {

        return widen(Maybe.ofNullable(value));
    }
    /**
     * Convert a Maybe to a simulated HigherKindedType that captures Maybe nature
     * and Maybe element data type separately. Recover via @see MaybeType#narrow
     * 
     * If the supplied Maybe implements MaybeType it is returned already, otherwise it
     * is wrapped into a Maybe implementation that does implement MaybeType
     * 
     * @param Maybe Maybe to widen to a MaybeType
     * @return MaybeType encoding HKT info about Maybes
     */
    public static <T> MaybeType<T> widen(final Maybe<T> maybe) {
        if (maybe instanceof MaybeType)
            return (MaybeType<T>) maybe;
        return new Box<>(
                         maybe);
    }

    /**
     * Convert the HigherKindedType definition for a Maybe into
     * 
     * @param Maybe Type Constructor to convert back into narrowed type
     * @return MaybeX from Higher Kinded Type
     */
    public static <T> Maybe<T> narrow(final Higher<MaybeType.µ, T> maybe) {
        if (maybe instanceof Maybe)
            return (Maybe) maybe;
        final Box<T> type = (Box<T>) maybe;
        return type.narrow();
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Box<T> implements MaybeType<T> {

        @Delegate
        private final Maybe<T> boxed;

        /**
         * @return This back as a MaybeX
         */
        public MaybeX<T> narrow() {
            return MaybeX.fromIterable(boxed);
        }

        
       

    }

}
