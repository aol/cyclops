package cyclops.data;


import com.aol.cyclops2.data.collections.extensions.api.PSet;
import cyclops.control.Option;
import cyclops.data.base.HAMT;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class HashSet<T> implements  ImmutableSet<T>, PSet<T>, Serializable {
    private static final long serialVersionUID = 1L;
    private final HAMT.Node<T,T> map;

    public static <T> HashSet<T> empty(){
        return new HashSet<T>( HAMT.empty());
    }
    public static <T> HashSet<T> singleton(T value){
        HAMT.Node<T, T> tree = HAMT.empty();
        tree = tree.plus(0,value.hashCode(),value,value);
        return new HashSet<>(tree);
    }
    public static <T> HashSet<T> of(T... values){
        HAMT.Node<T, T> tree = HAMT.empty();
        for(T value : values){
            tree = tree.plus(0,value.hashCode(),value,value);
        }
        return new HashSet<>(tree);
    }
    static <U, T> HashSet<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed,unfolder));
    }

    static <T> HashSet<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,pred,f));

    }
    static <T> HashSet<T> iterate(final T seed, final UnaryOperator<T> f,int max) {
        return fromStream(ReactiveSeq.iterate(seed,f).limit(max));

    }

    static <T, U> Tuple2<HashSet<T>, HashSet<U>> unzip(final HashSet<Tuple2<T, U>> sequence) {
        return ReactiveSeq.unzip(sequence.stream()).transform((a, b)-> Tuple.tuple(fromStream(a),fromStream(b)));
    }
    static <T> HashSet<T> generate(Supplier<T> s, int max){
        return fromStream(ReactiveSeq.generate(s).limit(max));
    }
    static <T> HashSet<T> generate(Generator<T> s){
        return fromStream(ReactiveSeq.generate(s));
    }
    static HashSet<Integer> range(final int start, final int end) {
        return HashSet.fromStream(ReactiveSeq.range(start,end));

    }
    static HashSet<Integer> range(final int start, final int step, final int end) {
        return HashSet.fromStream(ReactiveSeq.range(start,step,end));

    }
    static HashSet<Long> rangeLong(final long start, final long step, final long end) {
        return HashSet.fromStream(ReactiveSeq.rangeLong(start,step,end));
    }


    static HashSet<Long> rangeLong(final long start, final long end) {
        return HashSet.fromStream(ReactiveSeq.rangeLong(start, end));

    }

    public static <T> HashSet<T> fromStream(Stream<T> stream){
        return ReactiveSeq.fromStream(stream).foldLeft(empty(),(m,t2)->m.plus(t2));
    }
    public static <T> HashSet<T> fromIterable(Iterable<T> it){
        return ReactiveSeq.fromIterable(it).foldLeft(empty(),(m, t2)->m.plus(t2));
    }


    public boolean containsValue(T value){
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
    public HashSet<T> removeValue(T value) {
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

    @Override
    public HashSet<T> plusAll(Iterable<? extends T> list) {
        HashSet<T> res = this;
        for(T next : list){
            res = res.plus(next);
        }
        return res;
    }


    @Override
    public HashSet<T> removeAll(Iterable<? extends T> list) {
        HashSet<T> res = this;
        for(T next : list){
            res = this.removeValue(next);
        }
        return res;
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
       return size()==size();
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
