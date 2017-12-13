package cyclops.data;


import com.oath.cyclops.types.persistent.PersistentSet;
import com.oath.cyclops.hkt.Higher;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import com.oath.cyclops.hkt.DataWitness.hashSet;
import cyclops.data.base.HAMT;
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
import lombok.Getter;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class HashSet<T> implements  ImmutableSet<T>,Higher<hashSet,T> , Serializable {
    private static final long serialVersionUID = 1L;
    @Getter
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
    public static <U, T> HashSet<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed,unfolder));
    }

    public static <T> HashSet<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,pred,f));

    }
    public static <T> HashSet<T> iterate(final T seed, final UnaryOperator<T> f,int max) {
        return fromStream(ReactiveSeq.iterate(seed,f).limit(max));

    }

    public static <T, U> Tuple2<HashSet<T>, HashSet<U>> unzip(final HashSet<Tuple2<T, U>> sequence) {
        return ReactiveSeq.unzip(sequence.stream()).transform((a, b)-> Tuple.tuple(fromStream(a),fromStream(b)));
    }
    public static <T> HashSet<T> generate(Supplier<T> s, int max){
        return fromStream(ReactiveSeq.generate(s).limit(max));
    }
    public static <T> HashSet<T> generate(Generator<T> s){
        return fromStream(ReactiveSeq.generate(s));
    }
    public static HashSet<Integer> range(final int start, final int end) {
        return HashSet.fromStream(ReactiveSeq.range(start,end));

    }
    public static HashSet<Integer> range(final int start, final int step, final int end) {
        return HashSet.fromStream(ReactiveSeq.range(start,step,end));

    }
    public static HashSet<Long> rangeLong(final long start, final long step, final long end) {
        return HashSet.fromStream(ReactiveSeq.rangeLong(start,step,end));
    }


    public static HashSet<Long> rangeLong(final long start, final long end) {
        return HashSet.fromStream(ReactiveSeq.rangeLong(start, end));

    }

    public static <T> HashSet<T> fromStream(Stream<T> stream){
        return ReactiveSeq.fromStream(stream).foldLeft(empty(),(m,t2)->m.plus(t2));
    }
    public static <T> HashSet<T> fromIterable(Iterable<T> it){
        return ReactiveSeq.fromIterable(it).foldLeft(empty(),(m, t2)->m.plus(t2));
    }
    public static <T> HashSet<T> fromIterator(Iterator<T> it){
      return fromIterable((()->it));
    }

    @Override
    public <R> HashSet<R> unitIterable(Iterable<R> it) {
        return fromIterable(it);
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
        return fromStream(stream().concatMap(fn));
    }

    @Override
    public <R> HashSet<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return fromStream(stream().concatMap(fn));
    }
  @Override
  public <R1, R2, R3, R> HashSet<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (HashSet< R>) ImmutableSet.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
  }

  @Override
  public <R1, R2, R3, R> HashSet<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (HashSet< R>) ImmutableSet.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
  }

  @Override
  public <R1, R2, R> HashSet<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (HashSet< R>) ImmutableSet.super.forEach3(iterable1,iterable2,yieldingFunction);
  }

  @Override
  public <R1, R2, R> HashSet<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (HashSet< R>) ImmutableSet.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);
  }

  @Override
  public <R1, R> HashSet<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (HashSet< R>) ImmutableSet.super.forEach2(iterable1,yieldingFunction);
  }

  @Override
  public <R1, R> HashSet<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (HashSet< R>) ImmutableSet.super.forEach2(iterable1,filterFunction,yieldingFunction);
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
            res = res.removeValue(next);
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

    public HashSet<T> take(final long n) {
        return (HashSet<T>)ImmutableSet.super.take(n);

    }
    public HashSet<T> takeWhile(Predicate<? super T> p) {
        return (HashSet<T>)ImmutableSet.super.takeWhile(p);
    }
    public HashSet<T> dropWhile(Predicate<? super T> p) {
       return (HashSet<T>)ImmutableSet.super.dropWhile(p);
    }
    public HashSet<T> drop(final long num) {
        return (HashSet<T>)ImmutableSet.super.drop(num);
    }
    public HashSet<T> reverse() {
        return (HashSet<T>)ImmutableSet.super.reverse();
    }
    public Tuple2<HashSet<T>,HashSet<T>> duplicate(){
        return Tuple.tuple(this,this);
    }
    public <R1, R2> Tuple2<HashSet<R1>, HashSet<R2>> unzip(Function<? super T, Tuple2<? extends R1, ? extends R2>> fn) {
        Tuple2<HashSet<R1>, HashSet<Tuple2<? extends R1, ? extends R2>>> x = map(fn).duplicate().map1(s -> s.map(Tuple2::_1));
        return x.map2(s -> s.map(Tuple2::_2));
    }


    @Override
    public HashSet<T> removeFirst(Predicate<? super T> pred) {
        return (HashSet<T>)ImmutableSet.super.removeFirst(pred);
    }


    public HashSet<T> appendAll(T append) {
        return add(append);
    }


    public HashSet<T> appendAll(Iterable<? extends T> it) {
        HashSet<T> s = this;
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
    public <U> HashSet<U> ofType(Class<? extends U> type) {
        return (HashSet<U>)ImmutableSet.super.ofType(type);
    }

    @Override
    public HashSet<T> filterNot(Predicate<? super T> predicate) {
        return (HashSet<T>)ImmutableSet.super.filterNot(predicate);
    }

    @Override
    public HashSet<T> notNull() {
        return (HashSet<T>)ImmutableSet.super.notNull();
    }

    @Override
    public HashSet<T> peek(Consumer<? super T> c) {
        return (HashSet<T>)ImmutableSet.super.peek(c);
    }

    @Override
    public <R> HashSet<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (HashSet<R>)ImmutableSet.super.trampoline(mapper);
    }

    @Override
    public HashSet<T> removeStream(Stream<? extends T> stream) {
        return (HashSet<T>)ImmutableSet.super.removeStream(stream);
    }

    @Override
    public HashSet<T> retainAll(Iterable<? extends T> it) {
        return (HashSet<T>)ImmutableSet.super.retainAll(it);
    }

    @Override
    public HashSet<T> retainStream(Stream<? extends T> stream) {
        return (HashSet<T>)ImmutableSet.super.retainStream(stream);
    }

    @Override
    public HashSet<T> retainAll(T... values) {
        return (HashSet<T>)ImmutableSet.super.retainAll(values);
    }

    @Override
    public HashSet<ReactiveSeq<T>> permutations() {
        return (HashSet<ReactiveSeq<T>>)ImmutableSet.super.permutations();
    }

    @Override
    public HashSet<ReactiveSeq<T>> combinations(int size) {
        return (HashSet<ReactiveSeq<T>>)ImmutableSet.super.combinations(size);
    }

    @Override
    public HashSet<ReactiveSeq<T>> combinations() {
        return (HashSet<ReactiveSeq<T>>)ImmutableSet.super.combinations();
    }

  @Override
    public <T2, R> HashSet<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return (HashSet<R>)ImmutableSet.super.zip(fn, publisher);
    }

    @Override
    public <U, R> HashSet<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (HashSet<R>)ImmutableSet.super.zipWithStream(other,zipper);
    }

    @Override
    public <U> HashSet<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (HashSet)ImmutableSet.super.zipWithPublisher(other);
    }

    @Override
    public <U> HashSet<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (HashSet)ImmutableSet.super.zip(other);
    }

    @Override
    public <S, U, R> HashSet<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (HashSet<R>)ImmutableSet.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> HashSet<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (HashSet<R>)ImmutableSet.super.zip4(second,third,fourth,fn);
    }

    @Override
    public HashSet<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (HashSet<T>)ImmutableSet.super.combine(predicate,op);
    }

    @Override
    public HashSet<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (HashSet<T>)ImmutableSet.super.combine(op,predicate);
    }

    @Override
    public HashSet<T> cycle(long times) {
        return (HashSet<T>)ImmutableSet.super.cycle(times);
    }

    @Override
    public HashSet<T> cycle(Monoid<T> m, long times) {
        return (HashSet<T>)ImmutableSet.super.cycle(m,times);
    }

    @Override
    public HashSet<T> cycleWhile(Predicate<? super T> predicate) {
        return (HashSet<T>) ImmutableSet.super.cycleWhile(predicate);
    }

    @Override
    public HashSet<T> cycleUntil(Predicate<? super T> predicate) {
        return (HashSet<T>) ImmutableSet.super.cycleUntil(predicate);
    }

    @Override
    public <U, R> HashSet<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (HashSet<R>) ImmutableSet.super.zip(other,zipper);
    }

    @Override
    public <S, U> HashSet<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (HashSet) ImmutableSet.super.zip3(second,third);
    }

    @Override
    public <T2, T3, T4> HashSet<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (HashSet) ImmutableSet.super.zip4(second,third,fourth);
    }

    @Override
    public HashSet<Tuple2<T, Long>> zipWithIndex() {
        return (HashSet<Tuple2<T,Long>>) ImmutableSet.super.zipWithIndex();
    }

    @Override
    public HashSet<VectorX<T>> sliding(int windowSize) {
        return (HashSet<VectorX<T>>) ImmutableSet.super.sliding(windowSize);
    }

    @Override
    public HashSet<VectorX<T>> sliding(int windowSize, int increment) {
        return (HashSet<VectorX<T>>) ImmutableSet.super.sliding(windowSize,increment);
    }

    @Override
    public <C extends Collection<? super T>> HashSet<C> grouped(int size, Supplier<C> supplier) {
        return (HashSet<C>) ImmutableSet.super.grouped(size,supplier);
    }

    @Override
    public HashSet<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return (HashSet<ListX<T>>) ImmutableSet.super.groupedUntil(predicate);
    }

    @Override
    public HashSet<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return (HashSet<ListX<T>>) ImmutableSet.super.groupedStatefullyUntil(predicate);
    }

    @Override
    public <U> HashSet<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {
        return (HashSet) ImmutableSet.super.zipWithStream(other);
    }

    @Override
    public HashSet<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return (HashSet<ListX<T>>) ImmutableSet.super.groupedWhile(predicate);
    }

    @Override
    public <C extends Collection<? super T>> HashSet<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (HashSet<C>) ImmutableSet.super.groupedWhile(predicate,factory);
    }

    @Override
    public <C extends Collection<? super T>> HashSet<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (HashSet<C>) ImmutableSet.super.groupedUntil(predicate,factory);
    }

    @Override
    public HashSet<ListX<T>> grouped(int groupSize) {
        return (HashSet<ListX<T>>) ImmutableSet.super.grouped(groupSize);
    }

    @Override
    public HashSet<T> distinct() {
        return (HashSet<T>) ImmutableSet.super.distinct();
    }

    @Override
    public HashSet<T> scanLeft(Monoid<T> monoid) {
        return (HashSet<T>) ImmutableSet.super.scanLeft(monoid);
    }

    @Override
    public <U> HashSet<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (HashSet<U>) ImmutableSet.super.scanLeft(seed,function);
    }

    @Override
    public HashSet<T> scanRight(Monoid<T> monoid) {
        return (HashSet<T>) ImmutableSet.super.scanRight(monoid);
    }

    @Override
    public <U> HashSet<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (HashSet<U>) ImmutableSet.super.scanRight(identity,combiner);
    }

    @Override
    public HashSet<T> sorted() {
        return (HashSet<T>) ImmutableSet.super.sorted();
    }

    @Override
    public HashSet<T> sorted(Comparator<? super T> c) {
        return (HashSet<T>) ImmutableSet.super.sorted(c);
    }



    @Override
    public HashSet<T> takeUntil(Predicate<? super T> p) {
        return (HashSet<T>) ImmutableSet.super.takeUntil(p);
    }

    @Override
    public HashSet<T> dropUntil(Predicate<? super T> p) {
        return (HashSet<T>) ImmutableSet.super.dropUntil(p);
    }

    @Override
    public HashSet<T> dropRight(int num) {
        return (HashSet<T>) ImmutableSet.super.dropRight(num);
    }

    @Override
    public HashSet<T> takeRight(int num) {
        return (HashSet<T>) ImmutableSet.super.takeRight(num);
    }

    @Override
    public HashSet<T> skip(long num) {
        return (HashSet<T>) ImmutableSet.super.skip(num);
    }

    @Override
    public HashSet<T> skipWhile(Predicate<? super T> p) {
        return (HashSet<T>) ImmutableSet.super.skipWhile(p);
    }

    @Override
    public HashSet<T> skipUntil(Predicate<? super T> p) {
        return (HashSet<T>) ImmutableSet.super.skipUntil(p);
    }

    @Override
    public HashSet<T> limit(long num) {
        return (HashSet<T>) ImmutableSet.super.limit(num);
    }

    @Override
    public HashSet<T> limitWhile(Predicate<? super T> p) {
        return (HashSet<T>) ImmutableSet.super.limitWhile(p);
    }

    @Override
    public HashSet<T> limitUntil(Predicate<? super T> p) {
        return (HashSet<T>) ImmutableSet.super.limitUntil(p);
    }

    @Override
    public HashSet<T> intersperse(T value) {
        return (HashSet<T>) ImmutableSet.super.intersperse(value);
    }

    @Override
    public HashSet<T> shuffle() {
        return (HashSet<T>) ImmutableSet.super.shuffle();
    }

    @Override
    public HashSet<T> skipLast(int num) {
        return (HashSet<T>) ImmutableSet.super.skipLast(num);
    }

    @Override
    public HashSet<T> limitLast(int num) {
        return (HashSet<T>) ImmutableSet.super.limitLast(num);
    }

    @Override
    public HashSet<T> shuffle(Random random) {
        return (HashSet<T>) ImmutableSet.super.shuffle(random);
    }

    @Override
    public HashSet<T> slice(long from, long to) {
        return (HashSet<T>) ImmutableSet.super.slice(from,to);
    }


    @Override
    public HashSet<T> prependStream(Stream<? extends T> stream) {
        return (HashSet<T>) ImmutableSet.super.prependStream(stream);
    }

    @Override
    public HashSet<T> appendAll(T... values) {
        return (HashSet<T>) ImmutableSet.super.appendAll(values);
    }

    @Override
    public HashSet<T> prependAll(T... values) {
        return (HashSet<T>) ImmutableSet.super.prependAll(values);
    }

    @Override
    public HashSet<T> deleteBetween(int start, int end) {
        return (HashSet<T>) ImmutableSet.super.deleteBetween(start,end);
    }

    @Override
    public HashSet<T> insertStreamAt(int pos, Stream<T> stream) {
        return (HashSet<T>) ImmutableSet.super.insertStreamAt(pos,stream);
    }

    @Override
    public HashSet<T> recover(Function<? super Throwable, ? extends T> fn) {
        return this;
    }

    @Override
    public <EX extends Throwable> HashSet<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return this;
    }


    @Override
    public <U extends Comparable<? super U>> HashSet<T> sorted(Function<? super T, ? extends U> function) {
        return (HashSet<T>) ImmutableSet.super.sorted(function);
    }
    public String mkString(){
        return stream().join(",","[","]");
    }

    @Override
    public <R> HashSet<R> retry(Function<? super T, ? extends R> fn) {
        return (HashSet<R>) ImmutableSet.super.retry(fn);
    }

    @Override
    public <R> HashSet<R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (HashSet<R>) ImmutableSet.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    public HashSet<T> onEmpty(T value) {
        return (HashSet<T>) ImmutableSet.super.onEmpty(value);
    }

    @Override
    public HashSet<T> onEmptyGet(Supplier<? extends T> supplier) {
        return (HashSet<T>) ImmutableSet.super.onEmptyGet(supplier);
    }



    @Override
    public HashSet<T> removeAll(T... values) {
        return (HashSet<T>) ImmutableSet.super.removeAll(values);
    }

    @Override
    public HashSet<T> prepend(T value) {
        return (HashSet<T>) ImmutableSet.super.prepend(value);
    }

    @Override
    public HashSet<T> removeAt(long pos) {
        return (HashSet<T>) ImmutableSet.super.removeAt(pos);
    }

    @Override
    public HashSet<T> removeAt(int pos) {
        return (HashSet<T>) ImmutableSet.super.removeAt(pos);
    }

    @Override
    public HashSet<T> prependAll(Iterable<? extends T> value) {
        return (HashSet<T>) ImmutableSet.super.prependAll(value);
    }

    @Override
    public HashSet<T> updateAt(int pos, T value) {
        return (HashSet<T>) ImmutableSet.super.updateAt(pos,value);
    }

    @Override
    public HashSet<T> insertAt(int pos, Iterable<? extends T> values) {
        return (HashSet<T>) ImmutableSet.super.insertAt(pos,values);
    }

    @Override
    public HashSet<T> insertAt(int i, T value) {
        return (HashSet<T>) ImmutableSet.super.insertAt(i,value);
    }

    @Override
    public HashSet<T> insertAt(int pos, T... values) {
        return (HashSet<T>) ImmutableSet.super.insertAt(pos,values);
    }
}
