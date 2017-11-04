package cyclops.data;


import com.oath.cyclops.types.Zippable;
import com.oath.cyclops.types.persistent.PersistentSet;
import com.oath.cyclops.hkt.Higher;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.monads.DataWitness.trieSet;
import cyclops.data.base.HashedPatriciaTrie;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrieSet<T> implements ImmutableSet<T>,
                                         Higher<trieSet,T>,
                                         Serializable{
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
        if(!(o instanceof PersistentSet) || o==null)
            return false;
        PersistentSet s = (PersistentSet)o;
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

    public TrieSet<T> take(final long n) {
        return (TrieSet<T>)ImmutableSet.super.take(n);

    }
    public TrieSet<T> takeWhile(Predicate<? super T> p) {
        return (TrieSet<T>)ImmutableSet.super.takeWhile(p);
    }
    public TrieSet<T> dropWhile(Predicate<? super T> p) {
        return (TrieSet<T>)ImmutableSet.super.dropWhile(p);
    }
    public TrieSet<T> drop(final long num) {
        return (TrieSet<T>)ImmutableSet.super.drop(num);
    }
    public TrieSet<T> reverse() {
        return (TrieSet<T>)ImmutableSet.super.reverse();
    }
    public Tuple2<TrieSet<T>,TrieSet<T>> duplicate(){
        return Tuple.tuple(this,this);
    }
    public <R1, R2> Tuple2<TrieSet<R1>, TrieSet<R2>> unzip(Function<? super T, Tuple2<? extends R1, ? extends R2>> fn) {
        Tuple2<TrieSet<R1>, TrieSet<Tuple2<? extends R1, ? extends R2>>> x = map(fn).duplicate().map1(s -> s.map(Tuple2::_1));
        return x.map2(s -> s.map(Tuple2::_2));
    }


    @Override
    public TrieSet<T> removeFirst(Predicate<? super T> pred) {
        return (TrieSet<T>)ImmutableSet.super.removeFirst(pred);
    }


    public TrieSet<T> append(T append) {
        return add(append);
    }


    public TrieSet<T> appendAll(Iterable<? extends T> it) {
        TrieSet<T> s = this;
        for(T next : it){
            s= s.add(next);
        }
        return s;


    }
    public <R> R foldLeft(R zero, BiFunction<R, ? super T, R> f){
        R acc= zero;
        for(T next : this){
            acc= f.apply(acc,next);
        }
        return acc;
    }

    @Override
    public <U> TrieSet<U> ofType(Class<? extends U> type) {
        return (TrieSet<U>)ImmutableSet.super.ofType(type);
    }

    @Override
    public TrieSet<T> filterNot(Predicate<? super T> predicate) {
        return (TrieSet<T>)ImmutableSet.super.filterNot(predicate);
    }

    @Override
    public TrieSet<T> notNull() {
        return (TrieSet<T>)ImmutableSet.super.notNull();
    }

    @Override
    public TrieSet<T> peek(Consumer<? super T> c) {
        return (TrieSet<T>)ImmutableSet.super.peek(c);
    }

    @Override
    public <R> TrieSet<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (TrieSet<R>)ImmutableSet.super.trampoline(mapper);
    }

    @Override
    public TrieSet<T> removeAllS(Stream<? extends T> stream) {
        return (TrieSet<T>)ImmutableSet.super.removeAllS(stream);
    }

    @Override
    public TrieSet<T> retainAllI(Iterable<? extends T> it) {
        return (TrieSet<T>)ImmutableSet.super.retainAllI(it);
    }

    @Override
    public TrieSet<T> retainAllS(Stream<? extends T> stream) {
        return (TrieSet<T>)ImmutableSet.super.retainAllS(stream);
    }

    @Override
    public TrieSet<T> retainAll(T... values) {
        return (TrieSet<T>)ImmutableSet.super.retainAll(values);
    }

    @Override
    public TrieSet<ReactiveSeq<T>> permutations() {
        return (TrieSet<ReactiveSeq<T>>)ImmutableSet.super.permutations();
    }

    @Override
    public TrieSet<ReactiveSeq<T>> combinations(int size) {
        return (TrieSet<ReactiveSeq<T>>)ImmutableSet.super.combinations(size);
    }

    @Override
    public TrieSet<ReactiveSeq<T>> combinations() {
        return (TrieSet<ReactiveSeq<T>>)ImmutableSet.super.combinations();
    }

    @Override
    public TrieSet<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (TrieSet<T>)ImmutableSet.super.zip(combiner,app);
    }

    @Override
    public <R> TrieSet<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (TrieSet<R>)ImmutableSet.super.zipWith(fn);
    }

    @Override
    public <R> TrieSet<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (TrieSet<R>)ImmutableSet.super.zipWithS(fn);
    }

    @Override
    public <R> TrieSet<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (TrieSet<R>)ImmutableSet.super.zipWithP(fn);
    }

    @Override
    public <T2, R> TrieSet<R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (TrieSet<R>)ImmutableSet.super.zipP(publisher,fn);
    }

    @Override
    public <U, R> TrieSet<R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (TrieSet<R>)ImmutableSet.super.zipS(other,zipper);
    }

    @Override
    public <U> TrieSet<Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return (TrieSet)ImmutableSet.super.zipP(other);
    }

    @Override
    public <U> TrieSet<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (TrieSet)ImmutableSet.super.zip(other);
    }

    @Override
    public <S, U, R> TrieSet<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (TrieSet<R>)ImmutableSet.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> TrieSet<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (TrieSet<R>)ImmutableSet.super.zip4(second,third,fourth,fn);
    }

    @Override
    public TrieSet<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (TrieSet<T>)ImmutableSet.super.combine(predicate,op);
    }

    @Override
    public TrieSet<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (TrieSet<T>)ImmutableSet.super.combine(op,predicate);
    }

    @Override
    public TrieSet<T> cycle(long times) {
        return (TrieSet<T>)ImmutableSet.super.cycle(times);
    }

    @Override
    public TrieSet<T> cycle(Monoid<T> m, long times) {
        return (TrieSet<T>)ImmutableSet.super.cycle(m,times);
    }

    @Override
    public TrieSet<T> cycleWhile(Predicate<? super T> predicate) {
        return (TrieSet<T>) ImmutableSet.super.cycleWhile(predicate);
    }

    @Override
    public TrieSet<T> cycleUntil(Predicate<? super T> predicate) {
        return (TrieSet<T>) ImmutableSet.super.cycleUntil(predicate);
    }

    @Override
    public <U, R> TrieSet<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (TrieSet<R>) ImmutableSet.super.zip(other,zipper);
    }

    @Override
    public <S, U> TrieSet<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (TrieSet) ImmutableSet.super.zip3(second,third);
    }

    @Override
    public <T2, T3, T4> TrieSet<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (TrieSet) ImmutableSet.super.zip4(second,third,fourth);
    }

    @Override
    public TrieSet<Tuple2<T, Long>> zipWithIndex() {
        return (TrieSet<Tuple2<T,Long>>) ImmutableSet.super.zipWithIndex();
    }

    @Override
    public TrieSet<VectorX<T>> sliding(int windowSize) {
        return (TrieSet<VectorX<T>>) ImmutableSet.super.sliding(windowSize);
    }

    @Override
    public TrieSet<VectorX<T>> sliding(int windowSize, int increment) {
        return (TrieSet<VectorX<T>>) ImmutableSet.super.sliding(windowSize,increment);
    }

    @Override
    public <C extends Collection<? super T>> TrieSet<C> grouped(int size, Supplier<C> supplier) {
        return (TrieSet<C>) ImmutableSet.super.grouped(size,supplier);
    }

    @Override
    public TrieSet<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return (TrieSet<ListX<T>>) ImmutableSet.super.groupedUntil(predicate);
    }

    @Override
    public TrieSet<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return (TrieSet<ListX<T>>) ImmutableSet.super.groupedStatefullyUntil(predicate);
    }

    @Override
    public <U> TrieSet<Tuple2<T, U>> zipS(Stream<? extends U> other) {
        return (TrieSet) ImmutableSet.super.zipS(other);
    }

    @Override
    public TrieSet<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return (TrieSet<ListX<T>>) ImmutableSet.super.groupedWhile(predicate);
    }

    @Override
    public <C extends Collection<? super T>> TrieSet<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (TrieSet<C>) ImmutableSet.super.groupedWhile(predicate,factory);
    }

    @Override
    public <C extends Collection<? super T>> TrieSet<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (TrieSet<C>) ImmutableSet.super.groupedUntil(predicate,factory);
    }

    @Override
    public TrieSet<ListX<T>> grouped(int groupSize) {
        return (TrieSet<ListX<T>>) ImmutableSet.super.grouped(groupSize);
    }

    @Override
    public TrieSet<T> distinct() {
        return (TrieSet<T>) ImmutableSet.super.distinct();
    }

    @Override
    public TrieSet<T> scanLeft(Monoid<T> monoid) {
        return (TrieSet<T>) ImmutableSet.super.scanLeft(monoid);
    }

    @Override
    public <U> TrieSet<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (TrieSet<U>) ImmutableSet.super.scanLeft(seed,function);
    }

    @Override
    public TrieSet<T> scanRight(Monoid<T> monoid) {
        return (TrieSet<T>) ImmutableSet.super.scanRight(monoid);
    }

    @Override
    public <U> TrieSet<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (TrieSet<U>) ImmutableSet.super.scanRight(identity,combiner);
    }

    @Override
    public TrieSet<T> sorted() {
        return (TrieSet<T>) ImmutableSet.super.sorted();
    }

    @Override
    public TrieSet<T> sorted(Comparator<? super T> c) {
        return (TrieSet<T>) ImmutableSet.super.sorted(c);
    }



    @Override
    public TrieSet<T> takeUntil(Predicate<? super T> p) {
        return (TrieSet<T>) ImmutableSet.super.takeUntil(p);
    }

    @Override
    public TrieSet<T> dropUntil(Predicate<? super T> p) {
        return (TrieSet<T>) ImmutableSet.super.dropUntil(p);
    }

    @Override
    public TrieSet<T> dropRight(int num) {
        return (TrieSet<T>) ImmutableSet.super.dropRight(num);
    }

    @Override
    public TrieSet<T> takeRight(int num) {
        return (TrieSet<T>) ImmutableSet.super.takeRight(num);
    }

    @Override
    public TrieSet<T> skip(long num) {
        return (TrieSet<T>) ImmutableSet.super.skip(num);
    }

    @Override
    public TrieSet<T> skipWhile(Predicate<? super T> p) {
        return (TrieSet<T>) ImmutableSet.super.skipWhile(p);
    }

    @Override
    public TrieSet<T> skipUntil(Predicate<? super T> p) {
        return (TrieSet<T>) ImmutableSet.super.skipUntil(p);
    }

    @Override
    public TrieSet<T> limit(long num) {
        return (TrieSet<T>) ImmutableSet.super.limit(num);
    }

    @Override
    public TrieSet<T> limitWhile(Predicate<? super T> p) {
        return (TrieSet<T>) ImmutableSet.super.limitWhile(p);
    }

    @Override
    public TrieSet<T> limitUntil(Predicate<? super T> p) {
        return (TrieSet<T>) ImmutableSet.super.limitUntil(p);
    }

    @Override
    public TrieSet<T> intersperse(T value) {
        return (TrieSet<T>) ImmutableSet.super.intersperse(value);
    }

    @Override
    public TrieSet<T> shuffle() {
        return (TrieSet<T>) ImmutableSet.super.shuffle();
    }

    @Override
    public TrieSet<T> skipLast(int num) {
        return (TrieSet<T>) ImmutableSet.super.skipLast(num);
    }

    @Override
    public TrieSet<T> limitLast(int num) {
        return (TrieSet<T>) ImmutableSet.super.limitLast(num);
    }

    @Override
    public TrieSet<T> shuffle(Random random) {
        return (TrieSet<T>) ImmutableSet.super.shuffle(random);
    }

    @Override
    public TrieSet<T> slice(long from, long to) {
        return (TrieSet<T>) ImmutableSet.super.slice(from,to);
    }



    @Override
    public <R> TrieSet<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatMapI(mapper);
    }

    @Override
    public TrieSet<T> prependS(Stream<? extends T> stream) {
        return (TrieSet<T>) ImmutableSet.super.prependS(stream);
    }

    @Override
    public TrieSet<T> append(T... values) {
        return (TrieSet<T>) ImmutableSet.super.append(values);
    }

    @Override
    public TrieSet<T> prependAll(T... values) {
        return (TrieSet<T>) ImmutableSet.super.prependAll(values);
    }

    @Override
    public TrieSet<T> deleteBetween(int start, int end) {
        return (TrieSet<T>) ImmutableSet.super.deleteBetween(start,end);
    }

    @Override
    public TrieSet<T> insertAtS(int pos, Stream<T> stream) {
        return (TrieSet<T>) ImmutableSet.super.insertAtS(pos,stream);
    }

    @Override
    public TrieSet<T> recover(Function<? super Throwable, ? extends T> fn) {
        return this;
    }

    @Override
    public <EX extends Throwable> TrieSet<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return this;
    }

    @Override
    public TrieSet<T> prepend(Iterable<? extends T> value) {
        return (TrieSet<T>) ImmutableSet.super.prepend(value);
    }

    @Override
    public <U extends Comparable<? super U>> TrieSet<T> sorted(Function<? super T, ? extends U> function) {
        return (TrieSet<T>) ImmutableSet.super.sorted(function);
    }
    public String mkString(){
        return stream().join(",","[","]");
    }
    @Override
    public <R> TrieSet<R> retry(Function<? super T, ? extends R> fn) {
        return (TrieSet<R>) ImmutableSet.super.retry(fn);
    }

    @Override
    public <R> TrieSet<R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (TrieSet<R>) ImmutableSet.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    public TrieSet<T> onEmpty(T value) {
        return (TrieSet<T>) ImmutableSet.super.onEmpty(value);
    }

    @Override
    public TrieSet<T> onEmptyGet(Supplier<? extends T> supplier) {
        return (TrieSet<T>) ImmutableSet.super.onEmptyGet(supplier);
    }

    @Override
    public TrieSet<T> removeAllI(Iterable<? extends T> it) {
        return (TrieSet<T>) ImmutableSet.super.removeAllI(it);
    }

    @Override
    public TrieSet<T> removeAll(T... values) {
        return (TrieSet<T>) ImmutableSet.super.removeAll(values);
    }

    @Override
    public TrieSet<T> prepend(T value) {
        return (TrieSet<T>) ImmutableSet.super.prepend(value);
    }

    @Override
    public TrieSet<T> removeAt(long pos) {
        return (TrieSet<T>) ImmutableSet.super.removeAt(pos);
    }

    @Override
    public TrieSet<T> removeAt(int pos) {
        return (TrieSet<T>) ImmutableSet.super.removeAt(pos);
    }

    @Override
    public TrieSet<T> prependAll(Iterable<? extends T> value) {
        return (TrieSet<T>) ImmutableSet.super.prependAll(value);
    }

    @Override
    public TrieSet<T> updateAt(int pos, T value) {
        return (TrieSet<T>) ImmutableSet.super.updateAt(pos,value);
    }

    @Override
    public TrieSet<T> insertAt(int pos, Iterable<? extends T> values) {
        return (TrieSet<T>) ImmutableSet.super.insertAt(pos,values);
    }

  @Override
  public <R1, R2, R3, R> TrieSet<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (TrieSet< R>) ImmutableSet.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
  }

  @Override
  public <R1, R2, R3, R> TrieSet<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (TrieSet< R>) ImmutableSet.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
  }

  @Override
  public <R1, R2, R> TrieSet<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (TrieSet< R>) ImmutableSet.super.forEach3(iterable1,iterable2,yieldingFunction);
  }

  @Override
  public <R1, R2, R> TrieSet<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (TrieSet< R>) ImmutableSet.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);
  }

  @Override
  public <R1, R> TrieSet<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (TrieSet< R>) ImmutableSet.super.forEach2(iterable1,yieldingFunction);
  }

  @Override
  public <R1, R> TrieSet<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (TrieSet< R>) ImmutableSet.super.forEach2(iterable1,filterFunction,yieldingFunction);
  }

    @Override
    public TrieSet<T> insertAt(int i, T value) {
        return (TrieSet<T>) ImmutableSet.super.insertAt(i,value);
    }

    @Override
    public TrieSet<T> insertAt(int pos, T... values) {
        return (TrieSet<T>) ImmutableSet.super.insertAt(pos,values);
    }

}
