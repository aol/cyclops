package cyclops;


import com.oath.cyclops.types.persistent.*;
import cyclops.companion.Comparators;
import cyclops.data.*;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Reducer;
import cyclops.reactive.collections.immutable.*;
import lombok.experimental.UtilityClass;

/**
 * Class that holds Reducers, Monoids with a type conversion for reducing a dataset to a single value.
 *
 * Primary use case is the reduction of Streams to persistent collections
 *
 * e.g.
 * <pre>
 * {@code
 * PersistentQueueX<Integer> q = Reducers.<Integer>toPersistentQueueX()
                                .foldMap(Stream.of(1,2,3,4));
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
public class ReactiveReducers {




    public static <T> Reducer<PersistentQueueX<T>,T> toPersistentQueueX() {
        return Reducer.fromMonoid(ReactiveMonoids.persistentQueueXConcat(), a -> PersistentQueueX.singleton(a));
    }

    public static <T> Reducer<OrderedSetX<T>,T> toOrderedSetX() {
        return Reducer.fromMonoid(ReactiveMonoids.orderedSetXConcat(), a -> OrderedSetX.singleton(Comparators.naturalOrderIdentityComparator(),a));
    }

    public static <T> Reducer<PersistentSetX<T>,T> toPersistentSetX() {
        return Reducer.fromMonoid(ReactiveMonoids.persistentSetXConcat(), a -> PersistentSetX.singleton(a));
    }


    public static <T> Reducer<LinkedListX<T>,T> toLinkedListX() {
        return Reducer.fromMonoid(ReactiveMonoids.linkedListXConcat(), a -> LinkedListX.singleton(a));
    }


    public static <T> Reducer<VectorX<T>,T> toVectorX() {
        return Reducer.fromMonoid(ReactiveMonoids.vectorXConcat(), a -> VectorX.singleton(a));
    }


    public static <T> Reducer<BagX<T>,T> toBagX() {
        return Reducer.fromMonoid(ReactiveMonoids.bagXConcat(), a -> BagX.singleton(a));
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



}
