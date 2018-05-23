package cyclops.data;


import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentSortedSet;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

public interface ImmutableSortedSet<T> extends ImmutableSet<T>, PersistentSortedSet<T> {



    @Override
    default ReactiveSeq<T> stream() {
        return ImmutableSet.super.stream();
    }

    @Override
    default ImmutableSortedSet<T> plus(T e){
        return append(e);
    }

    @Override
    default ImmutableSortedSet<T> plusAll(Iterable<? extends T> list){
        ImmutableSortedSet<T> set = this;
        for(T next : list){
            set = set.plus(next);
        }
        return set;
    }

    @Override
    default ImmutableSortedSet<T> removeAll(Iterable<? extends T> list){
        return unitStream(stream().removeAll(list));
    }


    Comparator<? super T> comparator();
    ImmutableSortedSet<T> subSet(T fromElement, T toElement);

    Option<T> first();
    Option<T> last();

    ImmutableSortedSet<T> drop(int num);
    ImmutableSortedSet<T> take(int num);

    @Override
    default <U> ImmutableSortedSet<U> ofType(Class<? extends U> type) {
        return (ImmutableSortedSet<U>)ImmutableSet.super.ofType(type);
    }

    @Override
    default ImmutableSortedSet<T> filterNot(Predicate<? super T> predicate) {
        return (ImmutableSortedSet<T>)ImmutableSet.super.filterNot(predicate);
    }

    @Override
    default ImmutableSortedSet<T> notNull() {
        return (ImmutableSortedSet<T>)ImmutableSet.super.notNull();
    }

    @Override
    default ImmutableSortedSet<T> peek(Consumer<? super T> c) {
        return (ImmutableSortedSet<T>)ImmutableSet.super.peek(c);
    }



    @Override
    ImmutableSortedSet<T> add(T value);

    @Override
    ImmutableSortedSet<T> removeValue(T value);



    @Override
    <R> ImmutableSortedSet<R> map(Function<? super T, ? extends R> fn);

    @Override
    <R> ImmutableSortedSet<R> flatMap(Function<? super T, ? extends ImmutableSet<? extends R>> fn);

    @Override
    <R> ImmutableSortedSet<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn);

    @Override
    <R> ImmutableSortedSet<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn);

    @Override
    <R> ImmutableSortedSet<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn);

    @Override
    ImmutableSortedSet<T> filter(Predicate<? super T> predicate);

    @Override
    default <R1, R2, R3, R> ImmutableSortedSet<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (ImmutableSortedSet<R>)ImmutableSet.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
    }

    @Override
    default <R1, R2, R3, R> ImmutableSortedSet<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (ImmutableSortedSet<R>)ImmutableSet.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
    }

    @Override
    default <R1, R2, R> ImmutableSortedSet<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (ImmutableSortedSet<R>)ImmutableSet.super.forEach3(iterable1,iterable2,yieldingFunction);
    }

    @Override
    default <R1, R2, R> ImmutableSortedSet<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (ImmutableSortedSet<R>)ImmutableSet.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);

    }

    @Override
    default <R1, R> ImmutableSortedSet<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (ImmutableSortedSet<R>)ImmutableSet.super.forEach2(iterable1,yieldingFunction);
    }

    @Override
    default <R1, R> ImmutableSortedSet<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (ImmutableSortedSet<R>)ImmutableSet.super.forEach2(iterable1,filterFunction,yieldingFunction);
    }

    @Override
    default ImmutableSortedSet<T> onEmpty(T value) {
        return (ImmutableSortedSet<T>)ImmutableSet.super.onEmpty(value);
    }

    @Override
    default ImmutableSortedSet<T> onEmptyGet(Supplier<? extends T> supplier) {
        return (ImmutableSortedSet<T>)ImmutableSet.super.onEmptyGet(supplier);
    }



    @Override
    <R> ImmutableSortedSet<R> unitStream(Stream<R> stream);
    ImmutableSortedSet<T> unitStream(Stream<T> stream, Comparator<? super T> comp);

    @Override
    default ImmutableSortedSet<T> removeStream(Stream<? extends T> stream) {
        return unitStream(stream().removeStream(stream),comparator());
    }



    @Override
    default ImmutableSortedSet<T> removeAll(T... values) {
        return unitStream(stream().removeAll(values),comparator());
    }

    @Override
    default ImmutableSortedSet<T> retainAll(Iterable<? extends T> it) {
        return unitStream(stream().retainAll(it),comparator());
    }

    @Override
    default ImmutableSortedSet<T> retainStream(Stream<? extends T> stream) {
        return unitStream(stream().retainStream(stream),comparator());
    }

    @Override
    default ImmutableSortedSet<T> retainAll(T... values) {
        return unitStream(stream().retainAll(values),comparator());
    }

  @Override
    default <T2, R> ImmutableSortedSet<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return unitStream(stream().zip(fn, publisher));
    }

    @Override
    default <U, R> ImmutableSortedSet<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return unitStream(stream().zipWithStream(other,zipper));
    }

    @Override
    default <U> ImmutableSortedSet<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return unitStream(stream().zipWithPublisher(other));
    }

    @Override
    default <U> ImmutableSortedSet<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return unitStream(stream().zip(other));
    }

    @Override
    default <S, U, R> ImmutableSortedSet<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return unitStream(stream().zip3(second,third,fn3));
    }

    @Override
    default <T2, T3, T4, R> ImmutableSortedSet<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return unitStream(stream().zip4(second,third,fourth,fn));
    }

    @Override
    <U> ImmutableSortedSet<U> unitIterator(Iterator<U> U);

    @Override
    default ImmutableSortedSet<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return unitStream(stream().combine(predicate,op),comparator());
    }

    @Override
    default ImmutableSortedSet<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return unitStream(stream().combine(op,predicate),comparator());
    }

    @Override
    default ImmutableSortedSet<T> cycle(long times) {
        return unitStream(stream().cycle(times),comparator());
    }

    @Override
    default ImmutableSortedSet<T> cycle(Monoid<T> m, long times) {
        return unitStream(stream().cycle(m,times),comparator());
    }

    @Override
    default ImmutableSortedSet<T> cycleWhile(Predicate<? super T> predicate) {
        return unitStream(stream().cycleWhile(predicate),comparator());
    }

    @Override
    default ImmutableSortedSet<T> cycleUntil(Predicate<? super T> predicate) {
        return unitStream(stream().cycleUntil(predicate),comparator());
    }

    @Override
    default <U, R> ImmutableSortedSet<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return unitStream(stream().zip(other,zipper));
    }

    @Override
    default <S, U> ImmutableSortedSet<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return unitStream(stream().zip3(second,third));
    }

    @Override
    default <T2, T3, T4> ImmutableSortedSet<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return unitStream(stream().zip4(second,third,fourth));
    }

    @Override
    default ImmutableSortedSet<Tuple2<T, Long>> zipWithIndex() {
        return unitStream(stream().zipWithIndex());
    }

    @Override
    default ImmutableSortedSet<Seq<T>> sliding(int windowSize) {
        return unitStream(stream().sliding(windowSize));
    }

    @Override
    default ImmutableSortedSet<Seq<T>> sliding(int windowSize, int increment) {
        return unitStream(stream().sliding(windowSize,increment));
    }

    @Override
    default <C extends PersistentCollection<? super T>> ImmutableSortedSet<C> grouped(int size, Supplier<C> supplier) {
        return unitStream(stream().grouped(size,supplier));
    }

    @Override
    default IterableX<Vector<T>> groupedUntil(Predicate<? super T> predicate) {
        return unitStream(stream().groupedUntil(predicate));
    }

    @Override
    default ImmutableSortedSet<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return unitStream(stream().groupedUntil(predicate));
    }

    @Override
    default <U> ImmutableSortedSet<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {
        return unitStream(stream().zipWithStream(other));
    }

    @Override
    default ImmutableSortedSet<Vector<T>> groupedWhile(Predicate<? super T> predicate) {
        return unitStream(stream().groupedWhile(predicate));
    }

    @Override
    default <C extends PersistentCollection<? super T>> ImmutableSortedSet<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return unitStream(stream().groupedWhile(predicate,factory));
    }

    @Override
    default <C extends PersistentCollection<? super T>> ImmutableSortedSet<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return unitStream(stream().groupedUntil(predicate,factory));
    }

    @Override
    default ImmutableSortedSet<Vector<T>> grouped(int groupSize) {
        return unitStream(stream().grouped(groupSize));
    }

    @Override
    default ImmutableSortedSet<T> distinct() {
        return unitStream(stream().distinct(),comparator());
    }

    @Override
    default ImmutableSortedSet<T> scanLeft(Monoid<T> monoid) {
        return unitStream(stream().scanLeft(monoid),comparator());
    }

    @Override
    default <U> ImmutableSortedSet<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return unitStream(stream().scanLeft(seed,function));
    }

    @Override
    default ImmutableSortedSet<T> scanRight(Monoid<T> monoid) {
        return unitStream(stream().scanRight(monoid),comparator());
    }

    @Override
    default <U> ImmutableSortedSet<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return unitStream(stream().scanRight(identity, combiner));
    }

    @Override
    default ImmutableSortedSet<T> sorted() {
        return unitStream(stream().sorted(),comparator());
    }

    @Override
    default ImmutableSortedSet<T> sorted(Comparator<? super T> c) {
        return unitStream(stream().sorted(c),comparator());
    }

    @Override
    default ImmutableSortedSet<T> takeWhile(Predicate<? super T> p) {
        return unitStream(stream().takeWhile(p),comparator());
    }

    @Override
    default ImmutableSortedSet<T> dropWhile(Predicate<? super T> p) {
        return unitStream(stream().dropWhile(p),comparator());
    }

    @Override
    default ImmutableSortedSet<T> takeUntil(Predicate<? super T> p) {
        return unitStream(stream().takeUntil(p),comparator());
    }

    @Override
    default ImmutableSortedSet<T> dropUntil(Predicate<? super T> p) {
        return unitStream(stream().dropUntil(p),comparator());
    }

    @Override
    default ImmutableSortedSet<T> dropRight(int num) {
        return unitStream(stream().dropRight(num),comparator());
    }

    @Override
    default ImmutableSortedSet<T> takeRight(int num) {
        return unitStream(stream().takeRight(num),comparator());
    }

    @Override
    default ImmutableSortedSet<T> drop(long num) {
        return unitStream(stream().drop(num),comparator());
    }

    @Override
    default ImmutableSortedSet<T> skip(long num) {
        return unitStream(stream().skip(num),comparator());
    }

    @Override
    default ImmutableSortedSet<T> skipWhile(Predicate<? super T> p) {
        return unitStream(stream().skipWhile(p),comparator());
    }

    @Override
    default ImmutableSortedSet<T> skipUntil(Predicate<? super T> p) {
        return unitStream(stream().skipUntil(p),comparator());
    }

    @Override
    default ImmutableSortedSet<T> take(long num) {
        return unitStream(stream().take(num),comparator());
    }

    @Override
    default ImmutableSortedSet<T> limit(long num) {
        return unitStream(stream().limit(num),comparator());
    }

    @Override
    default ImmutableSortedSet<T> limitWhile(Predicate<? super T> p) {
        return unitStream(stream().limitWhile(p),comparator());
    }

    @Override
    default ImmutableSortedSet<T> limitUntil(Predicate<? super T> p) {
        return unitStream(stream().limitUntil(p),comparator());
    }

    @Override
    default ImmutableSortedSet<T> intersperse(T value) {
        return unitStream(stream().intersperse(value),comparator());
    }

    @Override
    default ImmutableSortedSet<T> reverse() {
        return unitStream(stream().reverse(),comparator());
    }

    @Override
    default ImmutableSortedSet<T> shuffle() {
        return unitStream(stream().shuffle(),comparator());
    }

    @Override
    default ImmutableSortedSet<T> skipLast(int num) {
        return unitStream(stream().skipLast(num),comparator());
    }

    @Override
    default ImmutableSortedSet<T> limitLast(int num) {
        return unitStream(stream().limitLast(num),comparator());
    }

    @Override
    default ImmutableSortedSet<T> shuffle(Random random) {
        return unitStream(stream().shuffle(random),comparator());
    }

    @Override
    default ImmutableSortedSet<T> slice(long from, long to) {
        return unitStream(stream().slice(from,to),comparator());
    }

    @Override
    default <U extends Comparable<? super U>> ImmutableSortedSet<T> sorted(Function<? super T, ? extends U> function) {
        return unitStream(stream().sorted(function),comparator());
    }

    @Override
    default Traversable<T> traversable() {
        return stream();
    }

    @Override
    default ImmutableSortedSet<T> prependStream(Stream<? extends T> stream) {
        return unitStream(stream().prependStream(stream),comparator());
    }

    @Override
    default ImmutableSortedSet<T> appendAll(T... values) {
        return unitStream(stream().appendAll(values),comparator());
    }

    @Override
    default ImmutableSortedSet<T> append(T value) {
        return unitStream(stream().append(value),comparator());
    }

    @Override
    default ImmutableSortedSet<T> prepend(T value) {
        return unitStream(stream().prepend(value),comparator());
    }

    @Override
    default ImmutableSortedSet<T> prependAll(T... values) {
        return unitStream(stream().prependAll(values),comparator());
    }

    @Override
    default ImmutableSortedSet<T> insertAt(int pos, T... values) {
        return unitStream(stream().insertAt(pos,values),comparator());
    }

    @Override
    default ImmutableSortedSet<T> deleteBetween(int start, int end) {
        return unitStream(stream().deleteBetween(start,end),comparator());
    }

    @Override
    default ImmutableSortedSet<T> insertStreamAt(int pos, Stream<T> stream) {
        return unitStream(stream().insertStreamAt(pos,stream),comparator());
    }

    @Override
    default ImmutableSortedSet<T> recover(Function<? super Throwable, ? extends T> fn) {
        return unitStream(stream().recover(fn),comparator());
    }

    @Override
    default <EX extends Throwable> ImmutableSortedSet<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return unitStream(stream().recover(exceptionClass, fn),comparator());
    }

    @Override
    default ImmutableSortedSet<ReactiveSeq<T>> permutations() {
        return unitStream(stream().permutations());
    }

    @Override
    default ImmutableSortedSet<ReactiveSeq<T>> combinations(int size) {
        return unitStream(stream().combinations(size));
    }

    @Override
    default ImmutableSortedSet<ReactiveSeq<T>> combinations() {
        return unitStream(stream().combinations());
    }
}
