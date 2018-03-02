package cyclops.companion;

import cyclops.function.Semigroup;

import java.util.*;
import java.util.function.BinaryOperator;

public interface BiFunctions {
    /**
     * Concatenate mutable collections
     *
     * To manage javac type inference first assign the semigroup
     * <pre>
     * {@code
     *
     *    BinaryOperator<List<Integer>> list = BiFunctions.collectionConcat();
     *    BinaryOperator<Set<Integer>> set = BiFunctions.collectionConcat();
     *
     *
     *
     * }
     * </pre>
     * @return A Semigroup that can combine any mutable toX type
     */
    static <T, C extends Collection<T>> BinaryOperator<C> mutableCollectionConcat() {
        return (a, b) -> {

            a.addAll(b);
            return a;
        };
    }

    /**  static <T> BinaryOperator<NonEmptyList<T>> nonEmptyList(){
     return (a,b)->b.prependAll(a);
     }**/
    /**
     * @return A combiner for mutable lists
     */
    static <T> BinaryOperator<List<T>> mutableListConcat() {
        return BiFunctions.mutableCollectionConcat();
    }

    /**
     * @return A combiner for mutable sets
     */
    static <T> BinaryOperator<Set<T>> mutableSetConcat() {
        return BiFunctions.mutableCollectionConcat();
    }

    /**
     * @return A combiner for mutable SortedSets
     */
    static <T> BinaryOperator<SortedSet<T>> mutableSortedSetConcat() {
        return BiFunctions.mutableCollectionConcat();
    }

    /**
     * @return A combiner for mutable Queues
     */
    static <T> BinaryOperator<Queue<T>> mutableQueueConcat() {
        return BiFunctions.mutableCollectionConcat();
    }

    /**
     * @return A combiner for mutable Deques
     */
    static <T> BinaryOperator<Deque<T>> mutableDequeConcat() {
        return BiFunctions.mutableCollectionConcat();
    }


    /**
     * This Semigroup will combine JDK Collections.
     *
     * To manage javac type inference first assign the semigroup
     * <pre>
     * {@code
     *
     *    BinaryOperator<List<Integer>> list = BiFunctions.collectionConcat();
     *    BinaryOperator<Set<Integer>> set = BiFunctions.collectionConcat();
     *
     *
     *
     * }
     * </pre>
     * @return A Semigroup that attempts to combine the supplied Collections
     */
    static <T, C extends Collection<T>> BinaryOperator<C> collectionConcat() {
        return (a, b) -> {
            a.addAll(b);
            return a;
        };
    }
}
