package com.oath.cyclops.react;

import com.oath.cyclops.types.persistent.PersistentList;

import cyclops.collections.immutable.LinkedListX;

import lombok.AllArgsConstructor;

/**
 * Class that returned to blocking predicates for short circuiting result toX
 *
 * @author johnmcclean
 *
 * @param <T> Result type
 */
@AllArgsConstructor
public class Status<T> {

    private final int completed;
    private final int errors;
    private final int total;
    private final long elapsedNanos;
    private final LinkedListX<T> resultsSoFar;

    public final int getAllCompleted() {
        return completed + errors;
    }

    public final long getElapsedMillis() {
        return elapsedNanos / 1000000;
    }

    public int getCompleted() {
        return completed;
    }

    public int getErrors() {
        return errors;
    }

    public int getTotal() {
        return total;
    }

    public long getElapsedNanos() {
        return elapsedNanos;
    }

    public PersistentList<T> getResultsSoFar() {
        return resultsSoFar;
    }
}
