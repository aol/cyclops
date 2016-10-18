package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Try.Success;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.extensability.ValueComprehender;

/**
 * Comprehender for performant for-comprehensions for Try 
 * Behaviour in cross-type flatMap is to create an empty instance for Failures, but always pass Successes on
 * 
 * @author johnmcclean
 *
 */
public class TryComprehender implements ValueComprehender<Try> {

    /* 
     * @see com.aol.cyclops.lambda.api.Comprehender#filter(java.lang.Object, java.util.function.Predicate)
     */
    @Override
    public Object filter(final Try t, final Predicate p) {
        return t.filter(p);
    }

    /* 
     * @see com.aol.cyclops.lambda.api.Comprehender#map(java.lang.Object, java.util.function.Function)
     */
    @Override
    public Object map(final Try t, final Function fn) {
        return t.map(fn);
    }

    /* 
     * @see com.aol.cyclops.lambda.api.Comprehender#flatMap(java.lang.Object, java.util.function.Function)
     */
    @Override
    public Try flatMap(final Try t, final Function fn) {
        return t.flatMap(fn);
    }

    /* 
     * @see com.aol.cyclops.lambda.api.Comprehender#instanceOfT(java.lang.Object)
     */
    @Override
    public boolean instanceOfT(final Object apply) {
        if (apply instanceof Try)
            return true;

        return false;
    }

    /* 
     * @see com.aol.cyclops.lambda.api.Comprehender#of(java.lang.Object)
     */
    @Override
    public Try of(final Object o) {
        if (o instanceof Throwable)
            return Try.failure((Throwable) o);
        return Try.success(o);
    }

    /* 
     * @see com.aol.cyclops.lambda.api.Comprehender#of()
     */
    @Override
    public Try empty() {
        return Try.failure(null);
    }

    @Override
    public Class getTargetClass() {

        return Try.class;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object resolveForCrossTypeFlatMap(final Comprehender comp, final Try apply) {
        return apply instanceof Success ? comp.of(apply.get()) : comp.empty();
    }

}
