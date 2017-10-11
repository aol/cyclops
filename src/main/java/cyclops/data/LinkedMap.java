package cyclops.data;


import cyclops.collections.immutable.PersistentMapX;
import cyclops.control.Option;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkedMap<K,V> implements ImmutableMap<K,V>{

    private final ImmutableMap<K, V> map;
    private final BankersQueue<Tuple2<K, V>> order;
    public static <K,V> LinkedMap<K,V> empty(){
        return new LinkedMap<>(HashMap.empty(),BankersQueue.empty());
    }

    public static <K,V> LinkedMap<K,V> fromStream(ReactiveSeq<Tuple2<K,V>> stream){
        return stream.foldLeft(empty(),(m,t2)->m.put(t2._1(),t2._2()));
    }
    public Option<V> get(K key){
        return map.get(key);
    }

    @Override
    public V getOrElse(K key, V alt) {
        return map.getOrElse(key,alt);
    }

    @Override
    public V getOrElseGet(K key, Supplier<V> alt) {
        return map.getOrElseGet(key,alt);
    }

    @Override
    public int size() {
        return order.size();
    }

    @Override
    public <K2, V2> DMap.Two<K, V, K2, V2> merge(ImmutableMap<K2, V2> one) {
        return DMap.two(this,one);
    }

    @Override
    public <K2, V2, K3, V3> DMap.Three<K, V, K2, V2, K3, V3> merge(DMap.Two<K2, V2, K3, V3> two) {
        return DMap.three(this,two.map1(),two.map2());
    }

    @Override
    public ReactiveSeq<Tuple2<K, V>> stream() {
        return order.stream();
    }

    @Override
    public <R> ImmutableMap<K, R> mapValues(Function<? super V, ? extends R> map) {
        return fromStream(stream().map(t->t.map2(map)));
    }

    @Override
    public <R> ImmutableMap<R, V> mapKeys(Function<? super K, ? extends R> map) {
        return fromStream(stream().map(t->t.map1(map)));
    }

    @Override
    public <R1, R2> ImmutableMap<R1, R2> bimap(BiFunction<? super K, ? super V, ? extends Tuple2<R1, R2>> map) {
        return fromStream(stream().map(t->t.transform(map)));
    }

    @Override
    public <K2, V2> ImmutableMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper) {
        return fromStream(stream().flatMapI(t->t.transform(mapper)));
    }

    @Override
    public <K2, V2> ImmutableMap<K2, V2> flatMapI(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return fromStream(stream().flatMapI(t->t.transform(mapper)));
    }

    @Override
    public ImmutableMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return fromStream(stream().filter(predicate));
    }

    @Override
    public ImmutableMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return fromStream(stream().filter(t->predicate.test(t._1())));
    }

    @Override
    public ImmutableMap<K, V> filterValues(Predicate<? super V> predicate) {
        return fromStream(stream().filter(t->predicate.test(t._2())));
    }

    @Override
    public <R> ImmutableMap<K, R> map(Function<? super V, ? extends R> fn) {
        return fromStream(stream().map(t-> Tuple.tuple(t._1(),fn.apply(t._2()))));
    }

    @Override
    public <R1, R2> ImmutableMap<R1, R2> bimap(Function<? super K, ? extends R1> fn1, Function<? super V, ? extends R2> fn2) {
        return fromStream(stream().map(t-> Tuple.tuple(fn1.apply(t._1()),fn2.apply(t._2()))));
    }

    public boolean containsKey(K key){
        return map.containsKey(key);
    }

    @Override
    public boolean contains(Tuple2<K, V> t) {
        return map.contains(t);
    }

    @Override
    public PersistentMapX<K, V> persistentMapX() {
        return stream().to().persistentMapX(k -> k._1(), v -> v._2());
    }

    public LinkedMap<K, V> put(K key, V value) {
        BankersQueue<Tuple2<K, V>> newOrder = get(key).map(v -> order.replace(Tuple.tuple(key, v), Tuple.tuple(key, value)))
                .orElseGet(() -> order.enqueue(Tuple.tuple(key, value)));
        return new LinkedMap<>(map.put(key,value),newOrder);

    }

    @Override
    public ImmutableMap<K, V> put(Tuple2<K, V> keyAndValue) {
        return put(keyAndValue._1(),keyAndValue._2());
    }

    @Override
    public ImmutableMap<K, V> putAll(ImmutableMap<K, V> map) {
        ImmutableMap<K,V> res = map;
        for(Tuple2<K,V> t : map){
            res = res.put(t);
            order.enqueue(t);
        }
        return new LinkedMap<>(res,order);
    }

    public LinkedMap<K, V> remove(K key) {
       return containsKey(key) ? new LinkedMap<>(map.remove(key),BankersQueue.ofAll(order.lazySeq().removeFirst(t-> Objects.equals(key,t._1())))) : this;
    }

    @Override
    public LinkedMap<K, V> removeAll(K... keys) {
        LinkedMap<K,V> cur = this;
        for(K key : keys){
            cur = cur.remove(key);
        }
        return cur;
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return stream().iterator();
    }
}
