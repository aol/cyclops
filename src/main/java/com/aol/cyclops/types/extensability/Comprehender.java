package com.aol.cyclops.types.extensability;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.internal.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.types.anyM.WitnessType;

/**
 * Interface for defining how Comprehensions should work for a type
 * Cyclops For Comprehensions will supply either a JDK 8 Predicate or Function
 * for filter / map / flatMap
 * The comprehender should wrap these in a suitable type and make the call to the
 * underlying Monadic Type (T) the Comprehender implementation supports.
 * 
 * E.g. To support mapping for the Functional Java Option type wrap the supplied JDK 8 Function in a Functional Java
 * fj.F type, call the make call to option.map( ) and retun the result.
 * 
 * <pre>{@code
 *  OptionComprehender<Option> {
 *    
 *     public Object map(Option o, Function fn){
 *        return o.map( a-> fn.apply(a));
 *     }
 *     
 * }
 * }</pre>
 * 
 *
 * 
 * @author johnmcclean
 *
 * @param <T> Monadic Type being wrapped
 */
public interface Comprehender<T> {
    
    default Comprehender<T> adapter(){
        return this;
    }

    default int priority() {
        return 5;
    }

    default T unwrap(final Object o) {
        return (T) o;
    }

    /**
     * Wrapper around filter
     * 
     * @param t Monadic type being wrapped
     * @param p JDK Predicate to wrap
     * @return Result of call to <pre>{@code t.filter ( i -> p.test(i)); }</pre>
     */
    default Object filter(final T t, final Predicate p) {
        return this.flatMap(t, d -> p.test(d) ? of(d) : empty());
    }

    /**
     * Wrapper around map
     * 
     * @param t Monadic type being wrapped
     * @param fn JDK Function to wrap
     * @return Result of call to <pre>{@code t.map( i -> fn.apply(i)); }</pre>
     */
    public Object map(T t, Function fn);

    /**
     * A flatMap function that allows flatMapping to a different Monad type
     * will attempt to lift any non-Monadic values returned into a Monadic form
     * 
     * @param t Monad to perform flatMap on
     * @param fn FlatMap function that returns different type
     * @return flatMap applied and return type converted back to host type, non-Monadic return values lifted into a Monadic form
     */
    default Object liftAndFlatMap(final T t, final Function fn) {

        return executeflatMap(t, input -> liftObject(this, fn.apply(input)));

    }

    /**
     * Wrapper around flatMap
     * 
     * @param t Monadic type being wrapped
     * @param fn JDK Function to wrap
     * @return Result of call to <pre>{@code t.flatMap( i -> fn.apply(i)); }</pre>
     */
    default Object executeflatMap(final T t, final Function fn) {
        return flatMap(t, input -> unwrapOtherMonadTypes(this, fn.apply(input)));
    }

    public Object flatMap(T t, Function fn);

    default boolean instanceOfT(final Object apply) {
        return getTargetClass().isAssignableFrom(apply.getClass());
    }

    public T of(Object o);

    public T fromIterator(Iterator o);

    public T empty();

    static Object liftObject(final Comprehender comp, final Object apply) {
        final Object o = new MonadicConverters().convertToMonadicForm(apply);

        return o;

    }

    static <T> T unwrapOtherMonadTypes(final Comprehender<T> comp, final Object apply) {

        if (comp.instanceOfT(apply))
            return (T) apply;

        if (apply instanceof Stream) {
            return comp.of(((Stream) apply).collect(Collectors.toCollection(MaterializedList::new)));
        }

        if (apply instanceof IntStream) {
            return comp.of(((IntStream) apply).boxed()
                                              .collect(Collectors.toCollection(MaterializedList::new)));
        }
        if (apply instanceof DoubleStream) {
            return comp.of(((DoubleStream) apply).boxed()
                                                 .collect(Collectors.toCollection(MaterializedList::new)));
        }
        if (apply instanceof LongStream) {
            return comp.of(((LongStream) apply).boxed()
                                               .collect(Collectors.toCollection(MaterializedList::new)));
        }
        if (apply instanceof CompletableFuture) {
            try {
                return comp.of(((CompletableFuture) apply).join());
            } catch (final Throwable t) {
                return comp.empty();
            }
        }

        return (T) new ComprehenderSelector().selectComprehender(apply,--?)
                                             .resolveForCrossTypeFlatMap(comp, apply);

    }

    /**
     * Answers the question how should this type behave when returned in a flatMap function
     * by another type? For example - Optional uses comp.of(opt.get()) when a value is present
     * and comp.empty() when no value is present.
     * 
     * @param comp
     * @param apply
     * @return
     */
    default Object resolveForCrossTypeFlatMap(final Comprehender comp, final T apply) {
        return comp.of(apply);
    }

    public Class getTargetClass();

}
