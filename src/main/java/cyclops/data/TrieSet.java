package cyclops.data;


import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import cyclops.stream.Generator;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TrieSet<T> implements ImmutableSet<T> {
    private final HashedPatriciaTrie.Node<T,T> map;
    public static <T> TrieSet<T> empty(){
        return new TrieSet<T>( HashedPatriciaTrie.empty());
    }

    static <U, T> TrieSet<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed,unfolder));
    }

    static <T> TrieSet<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,pred,f));

    }
    static <T> TrieSet<T> iterate(final T seed, final UnaryOperator<T> f,int max) {
        return fromStream(ReactiveSeq.iterate(seed,f).limit(max));

    }

    static <T, U> Tuple2<TrieSet<T>, TrieSet<U>> unzip(final TrieSet<Tuple2<T, U>> sequence) {
        return ReactiveSeq.unzip(sequence.stream()).transform((a, b)-> Tuple.tuple(fromStream(a),fromStream(b)));
    }
    static <T> TrieSet<T> generate(Supplier<T> s, int max){
        return fromStream(ReactiveSeq.generate(s).limit(max));
    }
    static <T> TrieSet<T> generate(Generator<T> s){
        return fromStream(ReactiveSeq.generate(s));
    }
    static TrieSet<Integer> range(final int start, final int end) {
        return TrieSet.fromStream(ReactiveSeq.range(start,end));

    }
    static TrieSet<Integer> range(final int start, final int step, final int end) {
        return TrieSet.fromStream(ReactiveSeq.range(start,step,end));

    }
    static TrieSet<Long> rangeLong(final long start, final long step, final long end) {
        return TrieSet.fromStream(ReactiveSeq.rangeLong(start,step,end));
    }


    static TrieSet<Long> rangeLong(final long start, final long end) {
        return TrieSet.fromStream(ReactiveSeq.rangeLong(start, end));

    }

    public static <T> TrieSet<T> of(T... values){
        HashedPatriciaTrie.Node<T, T> tree = HashedPatriciaTrie.empty();
        for(T value : values){
            tree = tree.put(value.hashCode(),value,value);
        }
        return new TrieSet<>(tree);
    }
    public static <T> TrieSet<T> fromStream(Stream<T> stream){
        return ReactiveSeq.fromStream(stream).foldLeft(empty(),(m,t2)->m.plus(t2));
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
    public <U> TrieSet<U> unitIterator(Iterator<U> it) {
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
        return map.stream().map(t->t._1());
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }
}
