package cyclops.data;


import com.aol.cyclops2.types.traversable.Traversable;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TrieSet<T> implements ImmutableSet<T> {
    private final HashedPatriciaTrie.Node<T,T> map;
    public static <T> TrieSet<T> empty(){
        return new TrieSet<T>( HashedPatriciaTrie.empty());
    }

    public static <T> TrieSet<T> of(T... values){
        HashedPatriciaTrie.Node<T, T> tree = HashedPatriciaTrie.empty();
        for(T value : values){
            tree = tree.put(value.hashCode(),value,value);
        }
        return new TrieSet<>(tree);
    }
    public static <T> TrieSet<T> fromStream(ReactiveSeq<T> stream){
        return stream.foldLeft(empty(),(m,t2)->m.plus(t2));
    }
    public static <T> TrieSet<T> fromIterable(Iterable<T> it){
        return ReactiveSeq.fromIterable(it).foldLeft(empty(),(m, t2)->m.plus(t2));
    }

    public boolean contains(T value){
        return map.get(value.hashCode(),value).isPresent();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public TrieSet<T> add(T value) {
        return null;
    }

    @Override
    public TrieSet<T> remove(T value) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public <R> TrieSet<R> map(Function<? super T, ? extends R> fn) {
        return fromStream(stream().map(fn));
    }

    @Override
    public <R> TrieSet<R> flatMap(Function<? super T, ? extends ImmutableSet<? extends R>> fn) {
        return fromStream(stream().flatMapI(fn));
    }

    @Override
    public <R> TrieSet<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return fromStream(stream().flatMapI(fn));
    }

    @Override
    public TrieSet<T> filter(Predicate<? super T> predicate) {
        return fromStream(stream().filter(predicate));
    }

    @Override
    public <R> ImmutableSet<R> unitStream(Stream<R> stream) {
        return fromStream(ReactiveSeq.fromStream(stream));
    }

    @Override
    public <U> Traversable<U> unitIterator(Iterator<U> it) {
        return fromIterable(()->it);
    }

    public TrieSet<T> plus(T value){
        return new TrieSet<>(map.put(value.hashCode(),value,value));
    }
    public TrieSet<T> minus(T value){
        return new TrieSet<>(map.minus(value.hashCode(),value));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return map.stream().map(t->t.v1);
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }
}
