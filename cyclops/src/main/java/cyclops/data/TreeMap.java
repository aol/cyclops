package cyclops.data;

import com.oath.cyclops.types.persistent.PersistentMap;
import com.oath.cyclops.hkt.Higher2;
import cyclops.companion.Comparators;
import cyclops.control.Option;
import cyclops.function.Function3;
import cyclops.function.Function4;
import com.oath.cyclops.hkt.DataWitness.treeMap;
import cyclops.data.base.RedBlackTree;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import lombok.experimental.Wither;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TreeMap<K,V> implements ImmutableMap<K,V> ,
                                            Higher2<treeMap,K,V>,
                                            Serializable{

    private static final long serialVersionUID = 1L;
    private final RedBlackTree.Tree<K,V> map;
    @Wither()
    private final Comparator<K> comparator;


    public ReactiveSeq<Tuple2<K,V>> stream(){
        return map.stream();
    }

    public static <K,V> TreeMap<K,V> empty(Comparator<K> comp){
        return new TreeMap<>( RedBlackTree.empty(comp),comp);
    }
    public static <K,V> TreeMap<K,V> of(Comparator<K> comp,K k,V v){
        return TreeMap.<K,V>empty(comp).put(k,v);
    }
    public static <K,V> TreeMap<K,V> of(Comparator<K> comp,K k1,V v1,K k2,V v2){
        return TreeMap.<K,V>empty(comp).put(k1,v1).put(k2,v2);
    }

    public static <K,V> TreeMap<K,V> fromStream(Stream<Tuple2<K,V>> stream, Comparator<K> comp){
        return ReactiveSeq.fromStream(stream).foldLeft(empty(comp),(m,t2)->m.put(t2._1(),t2._2()));
    }
    @Override
    public <R> TreeMap<K, R> mapValues(Function<? super V, ? extends R> map) {
        return fromStream(stream().map(t->t.map2(map)), comparator);
    }

    @Override
    public <R> TreeMap<R, V> mapKeys(Function<? super K, ? extends R> map) {
        ReactiveSeq<Tuple2<R, V>> s = stream().map(t -> {
            Tuple2<K, V> a = t;
            Tuple2<R, V> x = t.map1(map);
            return x;
        });


        TreeMap<R, V> x = fromStream(s, Comparators.naturalOrderIdentityComparator());
        return x;
    }


    @Override
    public <R1, R2> TreeMap<R1, R2> bimap(BiFunction<? super K, ? super V, ? extends Tuple2<R1, R2>> map) {
        return fromStream(stream().map(t->t.transform(map)), Comparators.naturalOrderIdentityComparator());
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper) {
        return fromStream(stream().concatMap(t->t.transform(mapper)), Comparators.naturalOrderIdentityComparator());
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> concatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return fromStream(stream().concatMap(t->t.transform(mapper)), Comparators.naturalOrderIdentityComparator());
    }


    public <R> TreeMap<R, V> mapKeys(Function<? super K, ? extends R> map,Comparator<R> comp) {
        ReactiveSeq<Tuple2<R, V>> s = stream().map(t -> {
            Tuple2<K, V> a = t;
            Tuple2<R, V> x = t.map1(map);
            return x;
        });


        TreeMap<R, V> x = fromStream(s, comp);
        return x;
    }



    public <R1, R2> TreeMap<R1, R2> bimap(BiFunction<? super K, ? super V, ? extends Tuple2<R1, R2>> map, Comparator<R1> comp) {
        return fromStream(stream().map(t->t.transform(map)),comp);
    }


    public <K2, V2> TreeMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper,Comparator<K2> comp) {
        return fromStream(stream().concatMap(t->t.transform(mapper)),comp);
    }


    public <K2, V2> TreeMap<K2, V2> concatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper,Comparator<K2> comp) {
        return fromStream(stream().concatMap(t->t.transform(mapper)),comp);
    }
    @Override
    public TreeMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return fromStream(stream().filter(predicate), comparator);
    }

    @Override
    public TreeMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return fromStream(stream().filter(t->predicate.test(t._1())), comparator);
    }

    @Override
    public TreeMap<K, V> filterValues(Predicate<? super V> predicate) {
        return fromStream(stream().filter(t->predicate.test(t._2())), comparator);
    }


    @Override
    public <R> TreeMap<K, R> map(Function<? super V, ? extends R> fn) {
        return fromStream(stream().map(t-> Tuple.tuple(t._1(),fn.apply(t._2()))), comparator);
    }

    @Override
    public <R1, R2> TreeMap<R1, R2> bimap(Function<? super K, ? extends R1> fn1, Function<? super V, ? extends R2> fn2) {
        return fromStream(stream().map(t-> Tuple.tuple(fn1.apply(t._1()),fn2.apply(t._2()))), Comparators.naturalOrderIdentityComparator());
    }

    public <R1, R2> TreeMap<R1, R2> bimap(Function<? super K, ? extends R1> fn1, Function<? super V, ? extends R2> fn2,Comparator<K> comp) {
        return fromStream(stream().map(t-> Tuple.tuple(fn1.apply(t._1()),fn2.apply(t._2()))), Comparators.naturalOrderIdentityComparator());
    }

    public static <K,V> TreeMap<K,V> fromMap(Comparator<K> comp, Map<K,V> map){
        Stream<Tuple2<K, V>> s = map.entrySet().stream().map(e -> Tuple.tuple(e.getKey(), e.getValue()));
        return new TreeMap<>(RedBlackTree.fromStream(comp,s),comp);
    }
    public static <K,V> TreeMap<K,V> fromMap(Comparator<K> comp, PersistentMap<K,V> map){
        if(map instanceof TreeMap){
            TreeMap<K,V> t = (TreeMap)map;
            return t.withComparator(comp);
        }
        return new TreeMap<>(RedBlackTree.fromStream(comp,map.stream()),comp);
    }

    public <KR,VR> TreeMap<KR,VR> bimap(Comparator<KR> comp, Function<? super K, ? extends KR> keyMapper, Function<? super V, ? extends VR> valueMapper){
        ReactiveSeq<? extends Tuple2<? extends KR, ? extends VR>> s = map.stream().map(t -> t.transform((k, v) -> Tuple.tuple(keyMapper.apply(k), valueMapper.apply(v))));
        return new TreeMap<>(RedBlackTree.fromStream(comp,s),comp);
    }


    @Override
    public TreeMap<K, V> put(K key, V value) {
        return new TreeMap<K,V>(map.plus(key,value), comparator);
    }

    @Override
    public TreeMap<K, V> put(Tuple2<K, V> keyAndValue) {
        return new TreeMap<K, V>(map.plus(keyAndValue._1(), keyAndValue._2()), comparator);
    }

    @Override
    public TreeMap<K, V> putAll(PersistentMap<? extends K, ? extends V> map) {
        return map.stream().foldLeft(this,(m,next)->m.put(next._1(),next._2()));
    }

    @Override
    public TreeMap<K, V> remove(K key) {
        return new TreeMap<>(map.minus(key), comparator);
    }

    @Override
    public TreeMap<K, V> removeAll(K... keys) {
        RedBlackTree.Tree<K,V> cur = map;
        for(K key : keys){
            cur = cur.minus(key);
        }
        return new TreeMap<>(cur, comparator);
    }

    @Override
    public boolean containsKey(K key) {
        return map.get(key).isPresent();
    }

    @Override
    public boolean contains(Tuple2<K, V> t) {
        return get(t._1()).filter(v-> Objects.equals(v,t._2())).isPresent();
    }

    public Option<V> get(K key){
        return map.get(key);
    }

    @Override
    public V getOrElse(K key, V alt) {
        return map.getOrElse(key,alt);
    }

    @Override
    public V getOrElseGet(K key, Supplier<? extends V> alt) {
        return map.getOrElseGet(key,alt);
    }

    @Override
    public int size() {
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
    public Iterator<Tuple2<K, V>> iterator() {
        return stream().iterator();
    }

    @Override
    public String toString(){
        return mkString();
    }

  @Override
  public TreeMap<K, V> filterNot(Predicate<? super Tuple2<K, V>> predicate) {
    return (TreeMap<K, V>)ImmutableMap.super.filterNot(predicate);
  }

  @Override
  public TreeMap<K, V> notNull() {
    return (TreeMap<K, V>)ImmutableMap.super.notNull();
  }

  @Override
  public TreeMap<K, V> peek(Consumer<? super V> c) {
    return (TreeMap<K, V>)ImmutableMap.super.peek(c);
  }





  @Override
  public TreeMap<K, V> bipeek(Consumer<? super K> c1, Consumer<? super V> c2) {
    return (TreeMap<K, V>)ImmutableMap.super.bipeek(c1,c2);
  }




  @Override
  public TreeMap<K, V> onEmpty(Tuple2<K, V> value) {
    return (TreeMap<K, V>)ImmutableMap.super.onEmpty(value);
  }

  @Override
  public TreeMap<K, V> onEmptyGet(Supplier<? extends Tuple2<K, V>> supplier) {
    return (TreeMap<K, V>)ImmutableMap.super.onEmptyGet(supplier);
  }



  @Override
  public TreeMap<K, V> onEmptySwitch(Supplier<? extends ImmutableMap<K, V>> supplier) {
    return (TreeMap<K, V>) ImmutableMap.super.onEmptySwitch(supplier);
  }

  @Override
  public <K1, K2, K3, K4, R1, R2, R3, R> TreeMap<K4, R> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2, Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Iterable<Tuple2<K3, R3>>> iterable3, Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, ? extends Tuple2<K4, R>> yieldingFunction) {
    return (TreeMap<K4, R>) ImmutableMap.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
  }

  @Override
  public <K1, K2, K3, K4, R1, R2, R3, R> TreeMap<K4, R> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2, Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Iterable<Tuple2<K3, R3>>> iterable3, Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, Boolean> filterFunction, Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, ? extends Tuple2<K4, R>> yieldingFunction) {
    return (TreeMap<K4, R>) ImmutableMap.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
  }

  @Override
  public <K1, K2, K3, R1, R2, R> TreeMap<K3, R> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2, Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Tuple2<K3, R>> yieldingFunction) {
    return (TreeMap<K3, R>) ImmutableMap.super.forEach3(iterable1,iterable2,yieldingFunction);
  }

  @Override
  public <K1, K2, K3, R1, R2, R> TreeMap<K3, R> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2, Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, Boolean> filterFunction, Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Tuple2<K3, R>> yieldingFunction) {
    return (TreeMap<K3, R>) ImmutableMap.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);

  }

  @Override
  public <K1, K2, R1, R> TreeMap<K2, R> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Tuple2<K2, R>> yieldingFunction) {
    return (TreeMap<K2, R>) ImmutableMap.super.forEach2(iterable1,yieldingFunction);

  }

  @Override
  public <K1, K2, R1, R> TreeMap<K2, R> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, Boolean> filterFunction, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Tuple2<K2, R>> yieldingFunction) {
    return (TreeMap<K2, R>) ImmutableMap.super.forEach2(iterable1,filterFunction,yieldingFunction);
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
        return map.stream().foldLeft(0,(acc,t2)-> acc+t2.hashCode());
  }
}
