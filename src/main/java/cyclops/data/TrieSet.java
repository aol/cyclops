package cyclops.data;


import com.aol.cyclops2.data.collections.extensions.api.PSet;
import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Option;
import cyclops.control.anym.DataWitness;
import cyclops.control.anym.DataWitness.trieSet;
import cyclops.data.base.HashedPatriciaTrie;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrieSet<T> implements ImmutableSet<T>,Higher<trieSet,T>, Serializable{
    private static final long serialVersionUID = 1L;
    private final HashedPatriciaTrie.Node<T,T> map;
    public static <T> TrieSet<T> empty(){
        return new TrieSet<T>( HashedPatriciaTrie.empty());
    }

    static <U, T> TrieSet<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
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

    public boolean containsValue(T value){
        return map.get(value.hashCode(),value).isPresent();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public TrieSet<T> add(T value) {
        return plus(value);
    }

    @Override
    public TrieSet<T> removeValue(T value) {
        return fromStream(stream().filter(i->!Objects.equals(i,value)));
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
    public TrieSet<T> remove(T value){
        return new TrieSet<>(map.minus(value.hashCode(),value));
    }

    @Override
    public <R> TrieSet<R> unitIterable(Iterable<R> it) {
        return fromIterable(it);
    }

    @Override
    public ReactiveSeq<T> stream() {
        return map.stream().map(t->t._1());
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof PSet) || o==null)
            return false;
        PSet s = (PSet)o;
        for(T next : this){
            if(!s.containsValue(next))
                return false;
        }
        return size()==s.size();
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (T e : this)
            hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
        return hashCode;
    }

    @Override
    public String toString(){
        return stream().join(",","[","]");
    }
}
