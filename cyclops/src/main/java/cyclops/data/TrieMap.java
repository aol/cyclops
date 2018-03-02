package cyclops.data;


import com.oath.cyclops.types.persistent.PersistentMap;
import com.oath.cyclops.hkt.Higher2;
import cyclops.control.Option;
import com.oath.cyclops.hkt.DataWitness.trieMap;
import cyclops.data.base.HashedPatriciaTrie;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrieMap<K,V> implements  ImmutableMap<K,V>,
                                            Higher2<trieMap,K,V>,
                                            Serializable{

    private static final long serialVersionUID = 1L;
    HashedPatriciaTrie.Node<K,V> map;

    public static <K,V> TrieMap<K,V> fromStream(Stream<Tuple2<K,V>> stream){
        return ReactiveSeq.fromStream(stream).foldLeft(empty(),(m,t2)->m.put(t2._1(),t2._2()));
    }
    public static <K,V> TrieMap<K,V> of(K k,V v){
        TrieMap<K,V> res = empty();
        return res.put(k,v);
    }
    public static <K,V> TrieMap<K,V> of(K k1,V v1,K k2, V v2){
        TrieMap<K,V> res = empty();
        return res.put(k1,v1).put(k2,v2);
    }

    public static <K,V> TrieMap<K,V> empty(){
        return new TrieMap<>(HashedPatriciaTrie.empty());
    }
    @Override
    public TrieMap<K,V> put(K key, V value){
        return new TrieMap<>(map.put(key.hashCode(),key,value));
    }


    @Override
    public TrieMap<K, V> put(Tuple2<K, V> keyAndValue) {
        return put(keyAndValue._1(),keyAndValue._2());
    }

    @Override
    public TrieMap<K, V> putAll(PersistentMap<? extends K,? extends  V> map) {
        return map.stream().foldLeft(this,(m,next)->m.put(next._1(),next._2()));
    }

    @Override
    public TrieMap<K, V> remove(K key) {
        return new TrieMap<>(map.minus(key.hashCode(),key));
    }

    @Override
    public TrieMap<K, V> removeAll(K[] keys) {
        HashedPatriciaTrie.Node<K,V> cur = map;
        for(K key : keys){
            cur = map.minus(key.hashCode(),key);
        }
        return new TrieMap<>(cur);
    }

    @Override
    public boolean containsKey(K key) {
        return map.get(key.hashCode(),key).isPresent();
    }

    @Override
    public boolean contains(Tuple2<K, V> t) {
        return get(t._1()).filter(v-> Objects.equals(v,t._2())).isPresent();
    }

    public Option<V> get(K key){
        return map.get(key.hashCode(),key);
    }
    public V getOrElse(K key,V alt){
        return map.getOrElse(key.hashCode(),key,alt);
    }

    @Override
    public V getOrElseGet(K key, Supplier<? extends V> alt) {
        return map.getOrElseGet(key.hashCode(),key,alt);
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

    @Override
    public ReactiveSeq<Tuple2<K, V>> stream() {
        return map.stream();
    }

    @Override
    public <R> TrieMap<K, R> mapValues(Function<? super V, ? extends R> map) {
        return fromStream(stream().map(t->t.map2(map)));
    }

    @Override
    public <R> TrieMap<R, V> mapKeys(Function<? super K, ? extends R> map) {
        return fromStream(stream().map(t->t.map1(map)));
    }

    @Override
    public <R1, R2> TrieMap<R1, R2> bimap(BiFunction<? super K, ? super V, ? extends Tuple2<R1, R2>> map) {
        return fromStream(stream().map(t->t.transform(map)));
    }

    @Override
    public <K2, V2> TrieMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper) {
        return fromStream(stream().concatMap(t->t.transform(mapper)));
    }

    @Override
    public <K2, V2> TrieMap<K2, V2> concatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return fromStream(stream().concatMap(t->t.transform(mapper)));
    }

    @Override
    public TrieMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return fromStream(stream().filter(predicate));
    }

    @Override
    public TrieMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return fromStream(stream().filter(t->predicate.test(t._1())));
    }

    @Override
    public TrieMap<K, V> filterValues(Predicate<? super V> predicate) {
        return fromStream(stream().filter(t->predicate.test(t._2())));
    }

    @Override
    public <R> TrieMap<K, R> map(Function<? super V, ? extends R> fn) {
        return fromStream(stream().map(t-> Tuple.tuple(t._1(),fn.apply(t._2()))));
    }

    @Override
    public <R1, R2> TrieMap<R1, R2> bimap(Function<? super K, ? extends R1> fn1, Function<? super V, ? extends R2> fn2) {
        return fromStream(stream().map(t->t.bimap(fn1,fn2)));
    }

    @Override
    public String toString(){
        return mkString();
    }
    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return stream().iterator();
    }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null)
      return false;

    if(o instanceof PersistentMap){
      PersistentMap<K,V> m = (PersistentMap<K,V>)o;
      return equalTo(m);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(map);
  }
}
