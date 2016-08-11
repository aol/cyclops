package com.aol.cyclops.internal.matcher2;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents an empty pattern matching case
 * 
 * @author johnmcclean
 *
 * @param <T> Input type
 * @param <R> Return type
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class EmptyCase<T, R> implements Case<T, R> {

    private final Tuple2<Predicate<? super T>, Function<? super T, ? extends R>> pattern = Tuple.<Predicate<? super T>, Function<? super T, ? extends R>> tuple(t -> false,
                                                                                                                                                                input -> null);
    @Getter
    private final boolean empty = true;

    @Override
    public Optional<R> match(T value) {
        return Optional.empty();
    }

    public Tuple2<Predicate<? super T>, Function<? super T, ? extends R>> get() {
        return pattern;
    }

}
