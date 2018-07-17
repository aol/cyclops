package cyclops.companion;


import com.oath.cyclops.types.persistent.*;
import cyclops.data.*;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.function.Reducer;


import lombok.experimental.UtilityClass;

import java.util.Comparator;


@UtilityClass
public class Reducers {

    public static <T> Reducer<BankersQueue<T>,T> toBankersQueue() {
        return Reducer.fromMonoid(Monoids.<T>bankersQueueConcat(),a->BankersQueue.of(a));
    }
    public static <T> Reducer<Seq<T>,T> toSeq() {
        return Reducer.fromMonoid(Monoids.<T>seqConcat(),a->Seq.of(a));
    }
    public static <T> Reducer<LazySeq<T>,T> toLazySeq() {
        return Reducer.fromMonoid(Monoids.<T>lazySeqConcat(),a->LazySeq.of(a));
    }
    public static <T> Reducer<IntMap<T>,T> toIntMap() {
        return Reducer.fromMonoid(Monoids.<T>intMapConcat(),a->IntMap.of(a));
    }
    public static <T> Reducer<Vector<T>,T> toVector() {
        return Reducer.fromMonoid(Monoids.<T>vectorConcat(),a->Vector.of(a));
    }
    public static <T> Reducer<TreeSet<T>,T> toTreeSet(Comparator<T> c) {
        return Reducer.fromMonoid(Monoids.<T>treeSetConcat(c),a->TreeSet.of(c,a));
    }
    public static <T> Reducer<Bag<T>,T> toBag() {
        return Reducer.fromMonoid(Monoids.<T>bagConcat(),a->Bag.of(a));
    }
    public static <T extends Comparable<? super T>> Reducer<TreeSet<T>,T> toTreeSet() {
        return Reducer.fromMonoid(Monoids.<T>treeSetConcat(Comparator.naturalOrder()),a->TreeSet.of(a));
    }
    public static <T> Reducer<HashSet<T>,T> toHashSet() {
        return Reducer.fromMonoid(Monoids.<T>hashSetConcat(),a->HashSet.of(a));
    }
    public static <T> Reducer<TrieSet<T>,T> toTrieSet() {
        return Reducer.fromMonoid(Monoids.<T>trieSetConcat(),a->TrieSet.of(a));
    }

    /**
     *
     * <pre>
     * {@code
     * PersistentQueue<Integer> q = Reducers.toPersistentQueue()
                                            .foldMap(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer to PQueue types
     */
    public static <T> Reducer<PersistentQueue<T>,T> toPersistentQueue() {
        return Reducer.fromMonoid(Monoids.<T,PersistentQueue<T>>concatPersistentCollection(BankersQueue.empty()), a -> BankersQueue.of(a));
    }
    /**
     * <pre>
     * {@code
     * PersistentSortedSet<Integer> q = Reducers.toPersistentSortedSet()
                                                .foldMap(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer to POrderedSet
     */
    public static <T> Reducer<PersistentSortedSet<T>,T> toPersistentSortedSet() {
        return Reducer.fromMonoid(Monoids.concatPersistentCollection(TreeSet.empty(Comparators.naturalOrderIdentityComparator())),
                a -> TreeSet.of(Comparators.naturalOrderIdentityComparator(),a));

    }
    public static <T> Reducer<PersistentSortedSet<T>,T> toPersistentSortedSet(Comparator<T> comp) {
        return Reducer.fromMonoid(Monoids.concatPersistentCollection(TreeSet.empty(comp)),
                a -> TreeSet.of(comp,a));

    }
    /**
     * <pre>
     * {@code
     * PersistentBag<Integer> q = Reducers.toPersistentBag()
                                 .foldMap(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for PBag
     */
    public static <T> Reducer<PersistentBag<T>,T> toPersistentBag() {
        return Reducer.fromMonoid(Monoids.concatPersistentCollection(Bag.empty()), a -> Bag.of(a));
    }
    /**
     * <pre>
     * {@code
     * PersistentSet<Integer> q = Reducers.toPSet()
                                          .foldMap(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for PSet
     */
    public static <T> Reducer<PersistentSet<T>,T> toPersistentSet() {
        return Reducer.fromMonoid(Monoids.concatPersistentCollection(HashSet.empty()), a -> HashSet.of(a));
    }
    /**
     * <pre>
     * {@code
     * PersistentList<Integer> q = Reducers.toPersistentVector()
                                           .foldMap(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for Vector
     */
    public static <T> Reducer<PersistentList<T>,T> toPersistentVector() {
        return Reducer.fromMonoid(Monoids.concatPersistentCollection(Vector.empty()), a -> Vector.of(a));
    }
    /**
     * <pre>
     * {@code
     * PersistentList<Integer> q = Reducers.toPersistentList()
                                           .foldMap(Stream.of(1,2,3,4));
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
                                   .foldMap(Stream.of(1,2,3,4));
     *
     * }
     * </pre>
     * @return Reducer for PersistentList in reveresed order
     */
    public static <T> Reducer<PersistentList<T>,T> toPersistentListReversed() {
        return Reducer.fromMonoid(Monoids.concatPersistentCollection(Seq.empty()), a -> Seq.of(a));
    }
    /**
     * <pre>
     * {@code
     * PersistentMap<Integer,String> q = Reducers.toPersistentMap()
                                                 .foldMap(Stream.of(Arrays.asList("hello",1),Arrays.asList("world",2)));
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
