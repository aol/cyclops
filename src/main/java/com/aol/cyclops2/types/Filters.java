package com.aol.cyclops2.types;

import java.util.function.Predicate;

/**
 * Trait that represents any class with a single argument Filter method
 * Will coerce that method into accepting JDK 8 java.util.function.Predicates
 * 
 * @author johnmcclean
 *
 * @param <T> Type of this Filters
 */
public interface Filters<T> {

    /**
     * Keep only elements for which the supplied predicates hold
     * 
     * e.g.
     * <pre>
     * {@code 
     *   
     *     of(1,2,3).filter(i->i>2);
     *     
     *     //[3]
     *   
     * }
     * </pre>
     * 
     * 
     * @param predicate toNested filter elements by, retaining matches
     * @return Filters with elements removed where the predicate does not hold
     */
    Filters<T> filter(Predicate<? super T> predicate);

    /**
     * Keep only those elements in a reactiveStream that are of a given type.
     * 
     * <pre>
     * {@code 
     * 
     * // (1, 2, 3) ReactiveSeq.of(1, "a", 2, "b",3).ofType(Integer.class)
     * 
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    default <U> Filters<U> ofType(final Class<? extends U> type) {
        return (Filters<U>) filter(type::isInstance);
    }

    /**
     * Remove any elements for which the predicate holds (inverse operation toNested filter)
     * 
     * e.g.
     * <pre>
     * {@code 
     *   
     *     of(1,2,3).filter(i->i>2);
     *     
     *     //[1,2]
     *   
     * }
     * </pre>
     * 
     * 
     * @param predicate toNested filter elements by, retaining matches
     * @return Filters with elements removed where the predicate does not hold
     */
    default Filters<T> filterNot(final Predicate<? super T> predicate) {
        return filter(predicate.negate());
    }

    /**
     * Filter elements retaining only values which are not null
     * 
     * <pre>
     * {@code 
     * 
     *   of(1,2,null,4).nonNull();
     * 
     *   //[1,2,4]
     *   
     * }
     * </pre>
     * 
     * @return Filters with nulls removed
     */
    default Filters<T> notNull() {
        return filter(t -> t != null);
    }

}
