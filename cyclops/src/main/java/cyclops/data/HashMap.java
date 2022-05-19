package cyclops.data;

import com.oath.cyclops.types.persistent.PersistentMap;
import cyclops.control.Option;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.data.base.HAMT;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;



@AllArgsConstructor
public final class HashMap<K,V> implements ImmutableMap<K,V>,PersistentMap<K,V>, Serializable {

    private final HAMT.Node<K,V> map;
    private static final long serialVersionUID = 1L;

    public static <K,V> HashMap<K,V> empty(){
        return new HashMap<>(HAMT.empty());
    }
    public static <K,V> HashMap<K,V> of(K k,V v){
        HashMap<K,V> res = empty();

        return res.put(k,v);
    }

    public static <K,V> HashMap<K,V> fromMap(Map<K,V> map){
        HashMap<K,V> res = empty();
        for(Map.Entry<K,V> next : map.entrySet()){
            res = res.put(next.getKey(),next.getValue());
        }
        return res;
    }
    public static <K,V> HashMap<K,V> fromMap(PersistentMap<K,V> map){
        if(map instanceof HashMap){
            return (HashMap)map;
        }
        HashMap<K,V> res = empty();
        for(Tuple2<K,V> next : map){
            res = res.put(next._1(),next._2());
        }
        return res;
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
        return fromStream(stream().concatMap(t->t.transform(mapper)));
    }

    @Override
    public <K2, V2> HashMap<K2, V2> concatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return fromStream(stream().concatMap(t->t.transform(mapper)));
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
    public HashMap<K,V> put(K key, V value){
        return new HashMap<K,V>(map.plus(0,key.hashCode(),key,value));
    }

    @Override
    public HashMap<K, V> put(Tuple2<K, V> keyAndValue) {
        return put(keyAndValue._1(),keyAndValue._2());
    }



    @Override
    public HashMap<K, V> remove(K key) {
        return new HashMap<>(map.minus(0,key.hashCode(),key));
    }


    @Override
    public HashMap<K, V> removeAll(K... keys) {
        HAMT.Node<K,V> cur = map;
        for(K key : keys){
            cur = cur.minus(0,key.hashCode(),key);
        }
        return new HashMap<>(cur);
    }

    @Override
    public boolean isEmpty() {
        return size()==0;
    }


    @Override
    public boolean containsKey(K key) {
        return map.get(0,key.hashCode(),key).isPresent();
    }



    @Override
    public boolean contains(Tuple2<K, V> t) {
        return this.get(t._1()).filter(v-> Objects.equals(v,t._2())).isPresent();
    }


    public Option<V> get(K key){
        return map.get(0,key.hashCode(),key);
    }

    @Override
    public V getOrElse(K key, V alt) {
        return map.getOrElse(0,key.hashCode(),key,alt);
    }

    @Override
    public V getOrElseGet(K key, Supplier<? extends V> alt) {
        return map.getOrElseGet(0,key.hashCode(),key,alt);
    }


    @Override
    public HashMap<K, V> putAll(PersistentMap<? extends K, ? extends V> map) {
        HashMap<K,V> res = this;
        for(Tuple2<? extends K, ? extends V> e : map){
            res = res.put(e._1(),e._2());
        }
        return res;
    }



    @Override
    public HashMap<K, V> removeAllKeys(Iterable<? extends K> keys) {
        HashMap<K,V> res = this;
        for(K e : keys){
            res = res.remove(e);
        }
        return res;
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
        return map.streamNaturalOrder().foldLeft(0,(acc,t2)-> acc+t2.hashCode());
    }

    public static <K, V> HashMap<K,V> narrow(HashMap<? extends K, ? extends V> map) {
        return (HashMap<K,V>)map;
    }

  @Override
  public HashMap<K, V> filterNot(Predicate<? super Tuple2<K, V>> predicate) {
    return (HashMap<K, V>)ImmutableMap.super.filterNot(predicate);
  }

  @Override
  public HashMap<K, V> notNull() {
    return (HashMap<K, V>)ImmutableMap.super.notNull();
  }

  @Override
  public HashMap<K, V> peek(Consumer<? super V> c) {
    return (HashMap<K, V>)ImmutableMap.super.peek(c);
  }


  @Override
  public HashMap<K, V> bipeek(Consumer<? super K> c1, Consumer<? super V> c2) {
    return (HashMap<K, V>)ImmutableMap.super.bipeek(c1,c2);
  }




  @Override
  public HashMap<K, V> onEmpty(Tuple2<K, V> value) {
    return (HashMap<K, V>)ImmutableMap.super.onEmpty(value);
  }

  @Override
  public HashMap<K, V> onEmptyGet(Supplier<? extends Tuple2<K, V>> supplier) {
    return (HashMap<K, V>)ImmutableMap.super.onEmptyGet(supplier);
  }



  @Override
  public HashMap<K, V> onEmptySwitch(Supplier<? extends ImmutableMap<K, V>> supplier) {
    return (HashMap<K, V>) ImmutableMap.super.onEmptySwitch(supplier);
  }

  @Override
  public <K1, K2, K3, K4, R1, R2, R3, R> HashMap<K4, R> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2, Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Iterable<Tuple2<K3, R3>>> iterable3, Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, ? extends Tuple2<K4, R>> yieldingFunction) {
    return (HashMap<K4, R>) ImmutableMap.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
  }

  @Override
  public <K1, K2, K3, K4, R1, R2, R3, R> HashMap<K4, R> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2, Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Iterable<Tuple2<K3, R3>>> iterable3, Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, Boolean> filterFunction, Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, ? extends Tuple2<K4, R>> yieldingFunction) {
    return (HashMap<K4, R>) ImmutableMap.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
  }

  @Override
  public <K1, K2, K3, R1, R2, R> HashMap<K3, R> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2, Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Tuple2<K3, R>> yieldingFunction) {
    return (HashMap<K3, R>) ImmutableMap.super.forEach3(iterable1,iterable2,yieldingFunction);
  }

  @Override
  public <K1, K2, K3, R1, R2, R> HashMap<K3, R> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2, Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, Boolean> filterFunction, Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Tuple2<K3, R>> yieldingFunction) {
    return (HashMap<K3, R>) ImmutableMap.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);

  }

  @Override
  public <K1, K2, R1, R> HashMap<K2, R> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Tuple2<K2, R>> yieldingFunction) {
    return (HashMap<K2, R>) ImmutableMap.super.forEach2(iterable1,yieldingFunction);

  }

  @Override
  public <K1, K2, R1, R> HashMap<K2, R> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, Boolean> filterFunction, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Tuple2<K2, R>> yieldingFunction) {
    return (HashMap<K2, R>) ImmutableMap.super.forEach2(iterable1,filterFunction,yieldingFunction);
  }

  public String printHAMT(){
      return this.map.toString();
  }
}
