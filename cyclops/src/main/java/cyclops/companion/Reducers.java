package cyclops.companion;


import com.oath.cyclops.types.persistent.*;
import cyclops.data.*;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.function.Reducer;


import cyclops.reactive.collections.immutable.*;


import lombok.experimental.UtilityClass;

import java.util.Comparator;

/**
 * Class that holds Reducers, Monoids with a type conversion for reducing a dataset to a single value.
 *
 * Primary use case is the reduction of Streams to persistent collections
 *
 * e.g.
 * <pre>
 * {@code
 * PersistentQueueX<Integer> q = Reducers.<Integer>toPersistentQueueX()
                                .mapReduce(Stream.of(1,2,3,4));
 *
 * }
 * </pre>
 *
 * Use with care, as the mapReduce method is not type safe
 *
 * @author johnmcclean
 *
 */
@UtilityClass
public class Reducers {



    private static <T> PersistentQueue<T> queueOf(final T... values) {
        PersistentQueue<T> result = BankersQueue.empty();
        for (final T value : values) {
            result = result.plus(value);
        }
        return result;

    }

    /**
     *
     * <pre>
     * {@code
     * PersistentQueueX<Integer> q = Reducers.toPersistentQueueX()
                                             .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer to PersistentQueueX types
     */
    public static <T> Reducer<PersistentQueueX<T>,T> toPersistentQueueX() {
        return Reducer.fromMonoid(Monoids.persistentQueueXConcat(), a -> PersistentQueueX.singleton(a));
    }

    /**
     * <pre>
     * {@code
     * OrderedSetX<Integer> q = Reducers.toOrderedSetX()
                                        .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer to OrderedSetX
     */
    public static <T> Reducer<OrderedSetX<T>,T> toOrderedSetX() {
        return Reducer.fromMonoid(Monoids.orderedSetXConcat(), a -> OrderedSetX.singleton(Comparators.naturalOrderIdentityComparator(),a));

    }

    /**
     * <pre>
     * {@code
     * PersistentSetX<Integer> q = Reducers.toPersistentSetX()
                                           .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for PersistentSetX
     */
    public static <T> Reducer<PersistentSetX<T>,T> toPersistentSetX() {
        return Reducer.fromMonoid(Monoids.persistentSetXConcat(), a -> PersistentSetX.singleton(a));
    }

    /**
     * <pre>
     * {@code
     * LinkedListX<Integer> q = Reducers.toLinkedListX()
                                        .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for LinkedListX
     */
    public static <T> Reducer<LinkedListX<T>,T> toLinkedListX() {
        return Reducer.fromMonoid(Monoids.linkedListXConcat(), a -> LinkedListX.singleton(a));
    }

    /**
     * <pre>
     * {@code
     * VectorX<Integer> q = Reducers.<Integer>toVectorX()
                                .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for VectorX
     */
    public static <T> Reducer<VectorX<T>,T> toVectorX() {
        return Reducer.fromMonoid(Monoids.vectorXConcat(), a -> VectorX.singleton(a));
    }


    /**
     * <pre>
     * {@code
     * BagX<Integer> q = Reducers.<Integer>toBagX()
                                .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for BagX
     */
    public static <T> Reducer<BagX<T>,T> toBagX() {
        return Reducer.fromMonoid(Monoids.bagXConcat(), a -> BagX.singleton(a));
    }

    private static <T> PersistentQueue<T> queueSingleton(final T value) {
        PersistentQueue<T> result = BankersQueue.empty();
        result = result.plus(value);
        return result;
    }
    /**
     *
     * <pre>
     * {@code
     * PersistentQueue<Integer> q = Reducers.toPersistentQueue()
                                            .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer to PQueue types
     */
    public static <T> Reducer<PersistentQueue<T>,T> toPersistentQueue() {
        return Reducer.fromMonoid(Monoids.<T,PersistentQueue<T>>pcollectionConcat(BankersQueue.empty()), a -> BankersQueue.of(a));
    }
    /**
     * <pre>
     * {@code
     * PersistentSortedSet<Integer> q = Reducers.toPersistentSortedSet()
                                                .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer to POrderedSet
     */
    public static <T> Reducer<PersistentSortedSet<T>,T> toPersistentSortedSet() {
        return Reducer.fromMonoid(Monoids.pcollectionConcat(TreeSet.empty(Comparators.naturalOrderIdentityComparator())),
                a -> TreeSet.of(Comparators.naturalOrderIdentityComparator(),a));

    }
    public static <T> Reducer<PersistentSortedSet<T>,T> toPersistentSortedSet(Comparator<T> comp) {
        return Reducer.fromMonoid(Monoids.pcollectionConcat(TreeSet.empty(comp)),
                a -> TreeSet.of(comp,a));

    }
    /**
     * <pre>
     * {@code
     * PersistentBag<Integer> q = Reducers.toPersistentBag()
                                 .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for PBag
     */
    public static <T> Reducer<PersistentBag<T>,T> toPersistentBag() {
        return Reducer.fromMonoid(Monoids.pcollectionConcat(Bag.empty()), a -> Bag.of(a));
    }
    /**
     * <pre>
     * {@code
     * PersistentSet<Integer> q = Reducers.toPSet()
                                          .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for PSet
     */
    public static <T> Reducer<PersistentSet<T>,T> toPersistentSet() {
        return Reducer.fromMonoid(Monoids.pcollectionConcat(HashSet.empty()), a -> HashSet.of(a));
    }
    /**
     * <pre>
     * {@code
     * PersistentList<Integer> q = Reducers.toPersistentVector()
                                           .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for Vector
     */
    public static <T> Reducer<PersistentList<T>,T> toPersistentVector() {
        return Reducer.fromMonoid(Monoids.pcollectionConcat(Vector.empty()), a -> Vector.of(a));
    }
    /**
     * <pre>
     * {@code
     * PersistentList<Integer> q = Reducers.toPersistentList()
                                           .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for PersistentList
     */
    public static <T> Reducer<PersistentList<T>,T> toPersistentList() {
        return Reducer.<PersistentList<T>,T> of(Seq.empty(), (final PersistentList<T> a) -> b -> b.plusAll(a), (final T x) -> Seq.of(x));

    }
    /**
     * <pre>
     * {@code
     * PersistentList<Integer> q = Reducers.toPersistentListReversed()
                                   .mapReduce(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for PersistentList in reveresed order
     */
    public static <T> Reducer<PersistentList<T>,T> toPersistentListReversed() {
        return Reducer.fromMonoid(Monoids.pcollectionConcat(Seq.empty()), a -> Seq.of(a));
    }
    /**
     * <pre>
     * {@code
     * PersistentMap<Integer,String> q = Reducers.toPersistentMap()
                                                 .mapReduce(Stream.of(Arrays.asList("hello",1),Arrays.asList("world",2)));
     *
     * }
     * </pre>
     * @return Reducer for PersistentMap
     */
    public static <K, V> Reducer<PersistentMap<K, V>,Tuple2<K,V>> toPersistentMap() {
        return Reducer.of(HashMap.empty(), (final PersistentMap<K, V> a) -> b -> a.putAll(b), (in) -> {
            Tuple2<K, V> w = in;
            return HashMap.of((K) w._1(), (V) w._2());

        });

    }
    /**
     * <pre>
     * {@code
     * PersistentMapX<Integer,String> q = Reducers.toPMapX()
                                        .mapReduce(Stream.of(Arrays.asList("hello",1),Arrays.asList("world",2)));
     *
     * }
     * </pre>
     * @return Reducer for PersistentMapX
     */
    public static <K, V> Reducer<PersistentMapX<K, V>,Tuple2<K,V>> toPMapX() {
        return Reducer.of(PersistentMapX.empty(), (final PersistentMapX<K, V> a) -> b -> a.putAll(b), (in) -> {
            Tuple2<K, V> w = in;
            return PersistentMapX.singleton((K) w._1(), (V) w._2());
        });


    }

    /**
     * Monoid for String concatonation
     *
     * @param joiner String combiner
     * @return
     */
    public static Monoid<String> toString(final String joiner) {
        return Monoid.of("", (a, b) -> a + joiner + b);
    }

    /**
     *
     * @return Monoid for summing Ints
     */
    public static Monoid<Integer> toTotalInt() {
        return Monoid.of(0, a -> b -> a + b);
    }

    /**
     * @return Reducer for counting number of values
     */
    public static <T> Reducer<Integer,T> toCountInt() {

        return Reducer.of(0, a -> b -> a + 1, (x) -> 1);
    }

    /**
     * @return Monoid for summing doubles
     */
    public static Monoid<Double> toTotalDouble() {
        return Monoid.of(0.0, (a, b) -> a + b);
    }

    /**
     * @return Reducer for counting doubles by converting toString to Double.valueOf( )
     */
    public static <T> Reducer<Double,T> toCountDouble() {
        return Reducer.of(0.0, a -> b -> a + 1, (x) -> Double.valueOf("" + x));
    }

}
