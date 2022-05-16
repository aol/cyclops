package com.oath.cyclops.react;

import com.oath.cyclops.types.persistent.PersistentList;

import cyclops.data.Seq;

/**
 * Class that returned to blocking predicates for short circuiting result toX
 *
 * @author johnmcclean
 *
 * @param <T> Result type
 */
public record Status<T> (int completed, int errors, int total, long elapsedNanos, Seq<T> resultsSoFar){

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
