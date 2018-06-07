package cyclops.reactive.companion;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.persistent.*;
import cyclops.companion.Reducers;
import cyclops.reactive.collections.mutable.MapX;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class for holding conversion methods between types
 * Use in conjunction with {@link To#to(Function)} for fluent conversions
 *
 * <pre>
 *     {@code
 *      LinkedList<Integer> list1 = Seq.of(1,2,3)
 *                                      .to(Converters::LinkedList);
ArrayList<Integer> list2 = Seq.of(1,2,3)
.to(Converters::ArrayList);
 *     }
 *
 * </pre>
 */
public  interface Converters {

    public static <K,V> HashMap<K,V> HashMap(MapX<K, V> vec){
        return vec.unwrapIfInstance(HashMap.class,
            ()-> vec.collect(Collectors.toMap(k->k._1(), v->v._2(),(a, b)->a,()->new HashMap<K,V>())));
    }
    public static <K,V> LinkedHashMap<K,V> LinkedHashMap(MapX<K, V> vec){
        return vec.unwrapIfInstance(LinkedHashMap.class,
            ()-> vec.collect(Collectors.toMap(k->k._1(),v->v._2(),(a,b)->a,()->new LinkedHashMap<K,V>())));
    }
    public static <K,V> ConcurrentHashMap<K,V> ConcurrentHashMap(MapX<K, V> vec){
        return vec.unwrapIfInstance(ConcurrentHashMap.class,
            ()-> vec.collect(Collectors.toMap(k->k._1(),v->v._2(),(a,b)->a,()->new ConcurrentHashMap<K,V>())));
    }
    public static <K extends Enum<K>,V> EnumMap<K,V> EnumHashMap(MapX<K, V> vec){
        return vec.unwrapIfInstance(EnumMap.class,
            ()-> new EnumMap<K,V>((Map<K,V>)vec.unwrap()));
    }
    public static <T> PersistentBag<T> PBag(CollectionX<T> vec){
        return vec.unwrapIfInstance(PersistentBag.class,
            ()-> Reducers.<T>toPersistentBag().foldMap(vec.stream()));
    }

    public static <T> PersistentSortedSet<T> POrderedSet(CollectionX<T> vec){
        return vec.unwrapIfInstance(PersistentList.class,
            ()->Reducers.<T>toPersistentSortedSet().foldMap(vec.stream()));
    }
    public static <T> PersistentQueue<T> PQueue(CollectionX<T> vec){
        return vec.unwrapIfInstance(PersistentList.class,
            ()->Reducers.<T>toPersistentQueue().foldMap(vec.stream()));
    }
    public static <T> PersistentSet<T> PSet(CollectionX<T> vec){
        return vec.unwrapIfInstance(PersistentList.class,
            ()->Reducers.<T>toPersistentSet().foldMap(vec.stream()));
    }
    public static <T> PersistentList<T> PStack(CollectionX<T> vec){
        return vec.unwrapIfInstance(PersistentList.class,
            ()->Reducers.<T>toPersistentList().foldMap(vec.stream()));
    }
    public static <T> PersistentList<T> PVector(CollectionX<T> vec){
        return vec.unwrapIfInstance(PersistentList.class,
            ()-> Reducers.<T>toPersistentList().foldMap(vec.stream()));
    }
    public static <T> LinkedList<T> LinkedList(CollectionX<T> vec){

        return vec.unwrapIfInstance(LinkedList.class,
            ()-> vec.collect(Collectors.toCollection(()->new LinkedList<T>())));
    }
    public static <T> ArrayDeque<T> ArrayDeque(CollectionX<T> vec){

        return vec.unwrapIfInstance(ArrayDeque.class,
            ()-> vec.collect(Collectors.toCollection(()->new ArrayDeque<T>())));
    }
    public static <T> ConcurrentLinkedDeque<T> ConcurrentLinkedDeque(CollectionX<T> vec){

        return vec.unwrapIfInstance(ConcurrentLinkedDeque.class,
            ()-> vec.collect(Collectors.toCollection(()->new ConcurrentLinkedDeque<T>())));
    }
    public static <T> LinkedBlockingDeque<T> LinkedBlockingDeque(CollectionX<T> vec){

        return vec.unwrapIfInstance(LinkedBlockingDeque.class,
            ()-> vec.collect(Collectors.toCollection(()->new LinkedBlockingDeque<T>())));
    }

    public static <T> ArrayList<T> ArrayList(CollectionX<T> vec){

        return vec.unwrapIfInstance(ArrayList.class,
            ()-> vec.collect(Collectors.toCollection(()->new ArrayList<T>())));
    }
    public static <T> CopyOnWriteArrayList<T> CopyOnWriteArrayList(CollectionX<T> vec){

        return vec.unwrapIfInstance(CopyOnWriteArrayList.class,
            ()-> vec.collect(Collectors.toCollection(()->new CopyOnWriteArrayList<T>())));
    }
    public static <T extends Delayed> DelayQueue<T> toDelayQueue(CollectionX<T> vec){

        return vec.unwrapIfInstance(DelayQueue.class,
            ()-> vec.collect(Collectors.toCollection(()->new DelayQueue<T>())));
    }
    public static <T> PriorityBlockingQueue<T> PriorityBlockingQueue(CollectionX<T> vec){

        return vec.unwrapIfInstance(PriorityBlockingQueue.class,
            ()-> vec.collect(Collectors.toCollection(()->new PriorityBlockingQueue<T>())));
    }
    public static <T> PriorityQueue<T> PriorityQueue(CollectionX<T> vec){

        return vec.unwrapIfInstance(PriorityQueue.class,
            ()-> vec.collect(Collectors.toCollection(()->new PriorityQueue<T>())));
    }
    public static <T> LinkedTransferQueue<T> LinkedTransferQueue(CollectionX<T> vec){

        return vec.unwrapIfInstance(LinkedTransferQueue.class,
            ()-> vec.collect(Collectors.toCollection(()->new LinkedTransferQueue<T>())));
    }
    public static <T> LinkedBlockingQueue<T> LinkedBlockingQueue(CollectionX<T> vec){

        return vec.unwrapIfInstance(LinkedBlockingQueue.class,
            ()-> vec.collect(Collectors.toCollection(()->new LinkedBlockingQueue<T>())));
    }
    public static <T> ConcurrentLinkedQueue<T> ConcurrentLinkedQueue(CollectionX<T> vec){

        return vec.unwrapIfInstance(ConcurrentLinkedQueue.class,
            ()-> vec.collect(Collectors.toCollection(()->new ConcurrentLinkedQueue<T>())));
    }
    public static <T> ArrayBlockingQueue<T> ArrayBlockingQueue(CollectionX<T> vec){

        return vec.unwrapIfInstance(ArrayBlockingQueue.class,
            ()-> vec.collect(Collectors.toCollection(()->new ArrayBlockingQueue<T>(vec.size()))));
    }

    public static <T> HashSet<T> HashSet(CollectionX<T> vec){
        return vec.unwrapIfInstance(HashSet.class,
            ()-> vec.collect(Collectors.toCollection(()->new HashSet<T>())));
    }
    public static <T> LinkedHashSet<T> LinkedHashSet(CollectionX<T> vec){
        return vec.unwrapIfInstance(LinkedHashSet.class,
            ()-> vec.collect(Collectors.toCollection(()->new LinkedHashSet<T>())));
    }
    public static <T> TreeSet<T> TreeSet(CollectionX<T> vec){
        return vec.unwrapIfInstance(TreeSet.class,
            ()-> vec.collect(Collectors.toCollection(()->new TreeSet<T>())));
    }
    public static <T> ConcurrentSkipListSet<T> ConcurrentSkipListSet(CollectionX<T> vec){
        return vec.unwrapIfInstance(ConcurrentSkipListSet.class,
            ()-> vec.collect(Collectors.toCollection(()->new ConcurrentSkipListSet<T>())));
    }
    public static <T> CopyOnWriteArraySet<T> CopyOnWriteArraySet(CollectionX<T> vec){
        return vec.unwrapIfInstance(CopyOnWriteArraySet.class,
            ()-> vec.collect(Collectors.toCollection(()->new CopyOnWriteArraySet<T>())));
    }

}
