package cyclops.data;

import cyclops.collectionx.immutable.PersistentMapX;
import cyclops.control.Option;
import cyclops.data.base.HAMT;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@AllArgsConstructor
public class HashMap<K,V> implements ImmutableMap<K,V>{
    private final HAMT.Node<K,V> map;
    private static final long serialVersionUID = 1L;

    public static <K,V> HashMap<K,V> empty(){
        return new HashMap<>(HAMT.empty());
    }
    public static <K,V> HashMap<K,V> of(K k,V v){
        HashMap<K,V> res = empty();
        return res.put(k,v);
    }
    public static <K,V> HashMap<K,V> of(K k1,V v1,K k2, V v2){
        HashMap<K,V> res = empty();
        return res.put(k1,v1).put(k2,v2);
    }

    public static <K,V> HashMap<K,V> fromStream(Stream<Tuple2<K,V>> stream){
        return ReactiveSeq.fromStream(stream).foldLeft(empty(),(m,t2)->m.put(t2._1(),t2._2()));
    }

    public int size(){
        return map.size();
    }


    @Override
    public <K2, V2> DMap.Two<K, V, K2, V2> merge(ImmutableMap<K2, V2> one) {
        return DMap.two(this,one);
    }

    @Override
    public <K2, V2, K3, V3> DMap.Three<K, V, K2, V2, K3, V3> merge(DMap.Two<K2, V2, K3, V3> two) {
        return DMap.three(this,two.map1(),two.map2());
    }

    public ReactiveSeq<Tuple2<K,V>> stream(){
        return map.stream();
    }

    @Override
    public <R> HashMap<K, R> mapValues(Function<? super V, ? extends R> map) {
        return fromStream(stream().map(t->t.map2(map)));
    }

    @Override
    public <R> HashMap<R, V> mapKeys(Function<? super K, ? extends R> map) {
        return fromStream(stream().map(t->t.map1(map)));

    }

    @Override
    public <R1, R2> HashMap<R1, R2> bimap(BiFunction<? super K, ? super V, ? extends Tuple2<R1, R2>> map) {
        return fromStream(stream().map(t->t.transform(map)));
    }

    @Override
    public <K2, V2> HashMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper) {
        return fromStream(stream().flatMapI(t->t.transform(mapper)));
    }

    @Override
    public <K2, V2> HashMap<K2, V2> flatMapI(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return fromStream(stream().flatMapI(t->t.transform(mapper)));
    }

    @Override
    public HashMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return fromStream(stream().filter(predicate));
    }

    @Override
    public HashMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return fromStream(stream().filter(t->predicate.test(t._1())));
    }

    @Override
    public HashMap<K, V> filterValues(Predicate<? super V> predicate) {
        return fromStream(stream().filter(t->predicate.test(t._2())));
    }

    @Override
    public <R> HashMap<K, R> map(Function<? super V, ? extends R> fn) {
        return fromStream(stream().map(t-> Tuple.tuple(t._1(),fn.apply(t._2()))));
    }

    @Override
    public <R1, R2> HashMap<R1, R2> bimap(Function<? super K, ? extends R1> fn1, Function<? super V, ? extends R2> fn2) {
        return fromStream(stream().map(t-> Tuple.tuple(fn1.apply(t._1()),fn2.apply(t._2()))));
    }

    @Override
    public PersistentMapX<K, V> persistentMapX() {
        return stream().to().persistentMapX(t->t._1(),t->t._2());
    }

    public HashMap<K,V> put(K key, V value){
        return new HashMap<K,V>(map.plus(0,key.hashCode(),key,value));
    }

    @Override
    public HashMap<K, V> put(Tuple2<K, V> keyAndValue) {
        return put(keyAndValue._1(),keyAndValue._2());
    }

    @Override
    public HashMap<K, V> putAll(ImmutableMap<K, V> map) {
       return map.stream().foldLeft(this,(m,next)->m.put(next._1(),next._2()));
    }

    @Override
    public HashMap<K, V> remove(K key) {
        return new HashMap<>(map.minus(0,key.hashCode(),key));
    }


    @Override
    public HashMap<K, V> removeAll(K... keys) {
        HAMT.Node<K,V> cur = map;
        for(K key : keys){
            cur = map.minus(0,key.hashCode(),key);
        }
        return new HashMap<>(cur);
    }



    @Override
    public boolean containsKey(K key) {
        return map.get(0,key.hashCode(),key).isPresent();
    }



    @Override
    public boolean contains(Tuple2<K, V> t) {
        return get(t._1()).filter(v-> Objects.equals(v,t._2())).isPresent();
    }

    public Option<V> get(K key){
        return map.get(0,key.hashCode(),key);
    }

    @Override
    public V getOrElse(K key, V alt) {
        return map.getOrElse(0,key.hashCode(),key,alt);
    }

    @Override
    public V getOrElseGet(K key, Supplier<V> alt) {
        return map.getOrElseGet(0,key.hashCode(),key,alt);
    }

    public HashMap<K,V> minus(K key){
        return new HashMap<K,V>(map.minus(0,key.hashCode(),key));
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return stream().iterator();
    }

    @Override
    public String toString() {
        return mkString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashMap<?, ?> hashMap = (HashMap<?, ?>) o;
        return Objects.equals(map, hashMap.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}
