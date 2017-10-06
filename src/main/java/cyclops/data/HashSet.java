package cyclops.data;


import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HashSet<T> implements  ImmutableSet<T>{
    private final HAMT.Node<T,T> map;

    public static <T> HashSet<T> empty(){
        return new HashSet<T>( HAMT.empty());
    }
    public static <T> HashSet<T> of(T... values){
        HAMT.Node<T, T> tree = HAMT.empty();
        for(T value : values){
            tree = tree.plus(0,value.hashCode(),value,value);
        }
        return new HashSet<>(tree);
    }
    public static <T> HashSet<T> fromStream(ReactiveSeq<T> stream){
        return stream.foldLeft(empty(),(m,t2)->m.plus(t2));
    }
    public static <T> HashSet<T> fromIterable(Iterable<T> it){
        return ReactiveSeq.fromIterable(it).foldLeft(empty(),(m, t2)->m.plus(t2));
    }


    public boolean contains(T value){
        return map.get(0,value.hashCode(),value).isPresent();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public HashSet<T> add(T value) {
        return new HashSet<>(map.plus(0,value.hashCode(),value,value));
    }

    @Override
    public HashSet<T> remove(T value) {
        return new HashSet<>(map.minus(0,value.hashCode(),value));
    }

    @Override
    public boolean isEmpty() {
        return map.size()==0;
    }

    @Override
    public <R> HashSet<R> map(Function<? super T, ? extends R> fn) {
        return fromStream(stream().map(fn));
    }

    @Override
    public <R> HashSet<R> flatMap(Function<? super T, ? extends ImmutableSet<? extends R>> fn) {
        return fromStream(stream().flatMapI(fn));
    }

    @Override
    public <R> HashSet<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return fromStream(stream().flatMapI(fn));
    }

    @Override
    public HashSet<T> filter(Predicate<? super T> predicate) {
        return fromStream(stream().filter(predicate));
    }

    @Override
    public <R> HashSet<R> unitStream(Stream<R> stream) {
        return HashSet.fromStream(ReactiveSeq.fromStream(stream));
    }

    @Override
    public <U> HashSet<U> unitIterator(Iterator<U> it) {
        return HashSet.fromIterable(()->it);
    }

    public HashSet<T> plus(T value){
        return new HashSet<>(map.plus(0,value.hashCode(),value,value));
    }
    public HashSet<T> minus(T value){
        return new HashSet<>(map.minus(0,value.hashCode(),value));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return map.stream().map(t->t._1());
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }
}
