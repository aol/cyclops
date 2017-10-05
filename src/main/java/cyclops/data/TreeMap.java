package cyclops.data;

import cyclops.collections.immutable.PersistentMapX;
import cyclops.control.Maybe;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeMap<K,V> implements ImmutableMap<K,V> {
    private final RedBlackTree.Tree<K,V> map;
    private final Comparator<K> comp;


    public ReactiveSeq<Tuple2<K,V>> stream(){
        return map.stream();
    }

    public static <K,V> TreeMap<K,V> empty(Comparator<K> comp){
        return new TreeMap<>( RedBlackTree.empty(comp),comp);
    }

    public static <K,V> TreeMap<K,V> fromStream(ReactiveSeq<Tuple2<K,V>> stream, Comparator<K> comp){
        return stream.foldLeft(empty(comp),(m,t2)->m.put(t2.v1,t2.v2));
    }
    @Override
    public <R> TreeMap<K, R> mapValues(Function<? super V, ? extends R> map) {
        return fromStream(stream().map(t->t.map2(map)),comp);
    }

    @Override
    public <R> TreeMap<R, V> mapKeys(Function<? super K, ? extends R> map) {
        ReactiveSeq<Tuple2<R, V>> s = stream().map(t -> {
            Tuple2<K, V> a = t;
            Tuple2<R, V> x = t.map1(map);
            return x;
        });


        TreeMap<R, V> x = fromStream(s, cyclops.data.Comparators.naturalOrderIdentityComparator());
        return x;
    }


    @Override
    public <R1, R2> TreeMap<R1, R2> bimap(BiFunction<? super K, ? super V, ? extends Tuple2<R1, R2>> map) {
        return fromStream(stream().map(t->t.map(map)), cyclops.data.Comparators.naturalOrderIdentityComparator());
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper) {
        return fromStream(stream().flatMapI(t->t.map(mapper)), cyclops.data.Comparators.naturalOrderIdentityComparator());
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> flatMapI(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return fromStream(stream().flatMapI(t->t.map(mapper)), cyclops.data.Comparators.naturalOrderIdentityComparator());
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
        return fromStream(stream().map(t->t.map(map)),comp);
    }


    public <K2, V2> TreeMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper,Comparator<K2> comp) {
        return fromStream(stream().flatMapI(t->t.map(mapper)),comp);
    }


    public <K2, V2> TreeMap<K2, V2> flatMapI(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper,Comparator<K2> comp) {
        return fromStream(stream().flatMapI(t->t.map(mapper)),comp);
    }
    @Override
    public TreeMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return fromStream(stream().filter(predicate),comp);
    }

    @Override
    public TreeMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return fromStream(stream().filter(t->predicate.test(t.v1)),comp);
    }

    @Override
    public TreeMap<K, V> filterValues(Predicate<? super V> predicate) {
        return fromStream(stream().filter(t->predicate.test(t.v2)),comp);
    }


    @Override
    public <R> TreeMap<K, R> map(Function<? super V, ? extends R> fn) {
        return fromStream(stream().map(t-> Tuple.tuple(t.v1,fn.apply(t.v2))),comp);
    }

    @Override
    public <R1, R2> TreeMap<R1, R2> bimap(Function<? super K, ? extends R1> fn1, Function<? super V, ? extends R2> fn2) {
        return fromStream(stream().map(t-> Tuple.tuple(fn1.apply(t.v1),fn2.apply(t.v2))), cyclops.data.Comparators.naturalOrderIdentityComparator());
    }

    public <R1, R2> TreeMap<R1, R2> bimap(Function<? super K, ? extends R1> fn1, Function<? super V, ? extends R2> fn2,Comparator<K> comp) {
        return fromStream(stream().map(t-> Tuple.tuple(fn1.apply(t.v1),fn2.apply(t.v2))), cyclops.data.Comparators.naturalOrderIdentityComparator());
    }

    public static <K,V> TreeMap<K,V> fromMap(Comparator<K> comp, Map<K,V> map){
        Stream<Tuple2<K, V>> s = map.entrySet().stream().map(e -> Tuple.tuple(e.getKey(), e.getValue()));
        return new TreeMap<>(RedBlackTree.fromStream(comp,s),comp);
    }

    public <KR,VR> TreeMap<KR,VR> bimap(Comparator<KR> comp, Function<? super K, ? extends KR> keyMapper, Function<? super V, ? extends VR> valueMapper){
        ReactiveSeq<? extends Tuple2<? extends KR, ? extends VR>> s = map.stream().map(t -> t.map((k, v) -> Tuple.tuple(keyMapper.apply(k), valueMapper.apply(v))));
        return new TreeMap<>(RedBlackTree.fromStream(comp,s),comp);
    }


    @Override
    public PersistentMapX<K, V> persistentMapX() {
        return stream().to().persistentMapX(t->t.v1,t->t.v2);
    }

    @Override
    public TreeMap<K, V> put(K key, V value) {
        return new TreeMap<K,V>(map.plus(key,value),comp);
    }

    @Override
    public TreeMap<K, V> put(Tuple2<K, V> keyAndValue) {
        return new TreeMap<K, V>(map.plus(keyAndValue.v1, keyAndValue.v2), comp);
    }

    @Override
    public TreeMap<K, V> putAll(ImmutableMap<K, V> map) {
        return map.stream().foldLeft(this,(m,next)->m.put(next.v1,next.v2));
    }

    @Override
    public TreeMap<K, V> remove(K key) {
        return new TreeMap<>(map.minus(key),comp);
    }

    @Override
    public TreeMap<K, V> removeAll(K... keys) {
        RedBlackTree.Tree<K,V> cur = map;
        for(K key : keys){
            cur = map.minus(key);
        }
        return new TreeMap<>(cur,comp);
    }

    @Override
    public boolean containsKey(K key) {
        return map.get(key).isPresent();
    }

    @Override
    public boolean contains(Tuple2<K, V> t) {
        return get(t.v1).filter(v-> Objects.equals(v,t.v2)).isPresent();
    }

    public Maybe<V> get(K key){
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

    public TreeMap<K,V> plus(K key, V value){
        return new TreeMap<>(map.plus(key,value),comp);
    }
    public TreeMap<K,V> minus(K key){
        return new TreeMap<>(map.minus(key),comp);
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return stream().iterator();
    }
}
