package cyclops.data;

import cyclops.collections.immutable.PersistentMapX;
import cyclops.control.Maybe;
import cyclops.stream.ReactiveSeq;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@AllArgsConstructor
public class HashMap<K,V> implements ImmutableMap<K,V>{
    private final HAMT.Node<K,V> map;

    public static <K,V> HashMap<K,V> empty(){
        return new HashMap<>(HAMT.empty());
    }

    public static <K,V> HashMap<K,V> fromStream(ReactiveSeq<Tuple2<K,V>> stream){
        return stream.foldLeft(empty(),(m,t2)->m.put(t2.v1,t2.v2));
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
        return fromStream(stream().map(t->t.map(map)));
    }

    @Override
    public <K2, V2> HashMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper) {
        return fromStream(stream().flatMapI(t->t.map(mapper)));
    }

    @Override
    public <K2, V2> HashMap<K2, V2> flatMapI(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return fromStream(stream().flatMapI(t->t.map(mapper)));
    }

    @Override
    public HashMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return fromStream(stream().filter(predicate));
    }

    @Override
    public HashMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return fromStream(stream().filter(t->predicate.test(t.v1)));
    }

    @Override
    public HashMap<K, V> filterValues(Predicate<? super V> predicate) {
        return fromStream(stream().filter(t->predicate.test(t.v2)));
    }

    @Override
    public <R> HashMap<K, R> map(Function<? super V, ? extends R> fn) {
        return fromStream(stream().map(t-> Tuple.tuple(t.v1,fn.apply(t.v2))));
    }

    @Override
    public <R1, R2> HashMap<R1, R2> bimap(Function<? super K, ? extends R1> fn1, Function<? super V, ? extends R2> fn2) {
        return fromStream(stream().map(t-> Tuple.tuple(fn1.apply(t.v1),fn2.apply(t.v2))));
    }

    @Override
    public PersistentMapX<K, V> persistentMapX() {
        return stream().to().persistentMapX(t->t.v1,t->t.v2);
    }

    public HashMap<K,V> put(K key, V value){
        return new HashMap<K,V>(map.plus(0,key.hashCode(),key,value));
    }

    @Override
    public HashMap<K, V> put(Tuple2<K, V> keyAndValue) {
        return put(keyAndValue.v1,keyAndValue.v2);
    }

    @Override
    public HashMap<K, V> putAll(ImmutableMap<K, V> map) {
       return map.stream().foldLeft(this,(m,next)->m.put(next.v1,next.v2));
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
        return get(t.v1).filter(v-> Objects.equals(v,t.v2)).isPresent();
    }

    public Maybe<V> get(K key){
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
}
