package cyclops.data;


import com.oath.cyclops.types.persistent.PersistentSet;
import com.oath.cyclops.types.Zippable;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.recoverable.OnEmptyError;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.control.Trampoline;
import cyclops.control.Try;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

public interface ImmutableSet<T> extends OnEmptySwitch<ImmutableSet<T>,ImmutableSet<T>>,PersistentSet<T>,
                                        OnEmptyError<T, ImmutableSet<T>>,
                                         IterableX<T>{

    <R> ImmutableSet<R> unitIterable(Iterable<R> it);
    @Override
    default ReactiveSeq<T> stream() {
        return IterableX.super.stream();
    }

    @Override
    default ImmutableSet<T> plus(T e){
        return append(e);
    }

    @Override
    default ImmutableSet<T> plusAll(Iterable<? extends T> list){
        ImmutableSet<T> set = this;
        for(T next : list){
            set = set.plus(next);
        }
        return set;
    }

    @Override
    default ImmutableSet<T> removeAll(Iterable<? extends T> list){
        return unitStream(stream().removeAllI(list));
    }

    @Override
    default <U> ImmutableSet<U> ofType(Class<? extends U> type) {
        return (ImmutableSet<U>)IterableX.super.ofType(type);
    }

    @Override
    default ImmutableSet<T> filterNot(Predicate<? super T> predicate) {
        return (ImmutableSet<T>)IterableX.super.filterNot(predicate);
    }

    @Override
    default ImmutableSet<T> notNull() {
        return (ImmutableSet<T>)IterableX.super.notNull();
    }

    @Override
    default ImmutableSet<T> peek(Consumer<? super T> c) {
        return (ImmutableSet<T>)IterableX.super.peek(c);
    }

    @Override
    default <R> ImmutableSet<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (ImmutableSet<R>)IterableX.super.trampoline(mapper);
    }

    @Override
    default <R> ImmutableSet<R> retry(Function<? super T, ? extends R> fn) {
        return (ImmutableSet<R>)IterableX.super.retry(fn);
    }

    @Override
    default <R> ImmutableSet<R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (ImmutableSet<R>)IterableX.super.retry(fn,retries,delay,timeUnit);
    }

    default PersistentSetX<T> persistentSetX(){
        return stream().to().persistentSetX(Evaluation.LAZY);
    }
    boolean containsValue(T value);
    int size();
    ImmutableSet<T> add(T value);
    ImmutableSet<T> removeValue(T value);
    boolean isEmpty();

    <R> ImmutableSet<R> map(Function<? super T, ? extends R> fn);
    <R> ImmutableSet<R> flatMap(Function<? super T, ? extends ImmutableSet<? extends R>> fn);
    <R> ImmutableSet<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn);

    @Override
    default <R> ImmutableSet<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatMapI(mapper);
    }

    ImmutableSet<T> filter(Predicate<? super T> predicate);

    default <R1, R2, R3, R> ImmutableSet<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1,
                                                     BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return this.flatMapI(in -> {

            ReactiveSeq<R1> a = ReactiveSeq.fromIterable(iterable1.apply(in));
            return a.flatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                return b.flatMap(inb -> {
                    ReactiveSeq<R3> c = ReactiveSeq.fromIterable(iterable3.apply(in, ina, inb));
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    default <R1, R2, R3, R> ImmutableSet<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1,
                                                     BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2,
                                                     Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                     Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return this.flatMapI(in -> {

            ReactiveSeq<R1> a = ReactiveSeq.fromIterable(iterable1.apply(in));
            return a.flatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                return b.flatMap(inb -> {
                    ReactiveSeq<R3> c = ReactiveSeq.fromIterable(iterable3.apply(in, ina, inb));
                    return c.filter(in2 -> filterFunction.apply(in, ina, inb, in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }
    default <R1, R2, R> ImmutableSet<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1,
                                                 BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapI(in -> {

            Iterable<R1> a = iterable1.apply(in);
            return ReactiveSeq.fromIterable(a)
                    .flatMap(ina -> {
                        ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                        return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
                    });

        });
    }


    default <R1, R2, R> ImmutableSet<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1,
                                                 BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2,
                                                 Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                 Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return this.flatMapI(in -> {

            Iterable<R1> a = iterable1.apply(in);
            return ReactiveSeq.fromIterable(a)
                    .flatMap(ina -> {
                        ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                        return b.filter(in2 -> filterFunction.apply(in, ina, in2))
                                .map(in2 -> yieldingFunction.apply(in, ina, in2));
                    });

        });
    }


    default <R1, R> ImmutableSet<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1,
                                             BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMapI(in-> {

            Iterable<? extends R1> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }


    default <R1, R> ImmutableSet<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1,
                                             BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                             BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMapI(in-> {

            Iterable<? extends R1> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                    .filter(in2-> filterFunction.apply(in,in2))
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }


    @Override
    default ImmutableSet<T> onEmpty(T value){
        if(size()==0){
            return add(value);
        }
        return this;
    }

    @Override
    default ImmutableSet<T> onEmptyGet(Supplier<? extends T> supplier){
        return onEmpty(supplier.get());
    }

    @Override
    default <X extends Throwable> Try<ImmutableSet<T>, X> onEmptyTry(Supplier<? extends X> supplier){
        return isEmpty() ? Try.failure(supplier.get()) : Try.success(this);
    }

    @Override
    default OnEmptySwitch<ImmutableSet<T>, ImmutableSet<T>> onEmptySwitch(Supplier<? extends ImmutableSet<T>> supplier){
        if(size()==0)
            return supplier.get();
        return this;
    }

    <R> ImmutableSet<R> unitStream(Stream<R> stream);
    @Override
    default ImmutableSet<T> removeAllS(Stream<? extends T> stream) {
        return unitStream(stream().removeAllS(stream));
    }

    @Override
    default ImmutableSet<T> removeAllI(Iterable<? extends T> it) {
        return unitStream(stream().removeAllI(it));
    }

    @Override
    default ImmutableSet<T> removeAll(T... values) {
        return unitStream(stream().removeAll(values));
    }

    @Override
    default ImmutableSet<T> retainAllI(Iterable<? extends T> it) {
        return unitStream(stream().retainAllI(it));
    }

    @Override
    default ImmutableSet<T> retainAllS(Stream<? extends T> stream) {
        return unitStream(stream().retainAllS(stream));
    }

    @Override
    default ImmutableSet<T> retainAll(T... values) {
        return unitStream(stream().retainAll(values));
    }

    @Override
    default ImmutableSet<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return unitStream(stream().zip(combiner,app));
    }

    @Override
    default <R> ImmutableSet<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return unitStream(stream().zipWith(fn));
    }

    @Override
    default <R> ImmutableSet<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return unitStream(stream().zipWithS(fn));
    }

    @Override
    default <R> ImmutableSet<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return unitStream(stream().zipWithP(fn));
    }

    @Override
    default <T2, R> ImmutableSet<R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return unitStream(stream().zipP(publisher,fn));
    }

    @Override
    default <U, R> ImmutableSet<R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return unitStream(stream().zipS(other,zipper));
    }

    @Override
    default <U> ImmutableSet<Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return unitStream(stream().zipP(other));
    }

    @Override
    default <U> ImmutableSet<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return unitStream(stream().zip(other));
    }

    @Override
    default <S, U, R> ImmutableSet<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return unitStream(stream().zip3(second,third,fn3));
    }

    @Override
    default <T2, T3, T4, R> ImmutableSet<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return unitStream(stream().zip4(second,third,fourth,fn));
    }

    @Override
    <U> ImmutableSet<U> unitIterator(Iterator<U> U);

    @Override
    default ImmutableSet<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return unitStream(stream().combine(predicate,op));
    }

    @Override
    default ImmutableSet<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return unitStream(stream().combine(op,predicate));
    }

    @Override
    default ImmutableSet<T> cycle(long times) {
        return unitStream(stream().cycle(times));
    }

    @Override
    default ImmutableSet<T> cycle(Monoid<T> m, long times) {
        return unitStream(stream().cycle(m,times));
    }

    @Override
    default ImmutableSet<T> cycleWhile(Predicate<? super T> predicate) {
        return unitStream(stream().cycleWhile(predicate));
    }

    @Override
    default ImmutableSet<T> cycleUntil(Predicate<? super T> predicate) {
        return unitStream(stream().cycleUntil(predicate));
    }

    @Override
    default <U, R> ImmutableSet<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return unitStream(stream().zip(other,zipper));
    }

    @Override
    default <S, U> ImmutableSet<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return unitStream(stream().zip3(second,third));
    }

    @Override
    default <T2, T3, T4> ImmutableSet<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return unitStream(stream().zip4(second,third,fourth));
    }

    @Override
    default ImmutableSet<Tuple2<T, Long>> zipWithIndex() {
        return unitStream(stream().zipWithIndex());
    }

    @Override
    default ImmutableSet<VectorX<T>> sliding(int windowSize) {
        return unitStream(stream().sliding(windowSize));
    }

    @Override
    default ImmutableSet<VectorX<T>> sliding(int windowSize, int increment) {
        return unitStream(stream().sliding(windowSize,increment));
    }

    @Override
    default <C extends Collection<? super T>> ImmutableSet<C> grouped(int size, Supplier<C> supplier) {
        return unitStream(stream().grouped(size,supplier));
    }

    @Override
    default ImmutableSet<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return unitStream(stream().groupedUntil(predicate));
    }

    @Override
    default ImmutableSet<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return unitStream(stream().groupedStatefullyUntil(predicate));
    }

    @Override
    default <U> ImmutableSet<Tuple2<T, U>> zipS(Stream<? extends U> other) {
        return unitStream(stream().zipS(other));
    }

    @Override
    default ImmutableSet<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return unitStream(stream().groupedWhile(predicate));
    }

    @Override
    default <C extends Collection<? super T>> ImmutableSet<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return unitStream(stream().groupedWhile(predicate,factory));
    }

    @Override
    default <C extends Collection<? super T>> ImmutableSet<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return unitStream(stream().groupedUntil(predicate,factory));
    }

    @Override
    default ImmutableSet<ListX<T>> grouped(int groupSize) {
        return unitStream(stream().grouped(groupSize));
    }


    @Override
    default ImmutableSet<T> distinct() {
        return unitStream(stream().distinct());
    }

    @Override
    default ImmutableSet<T> scanLeft(Monoid<T> monoid) {
        return unitStream(stream().scanLeft(monoid));
    }

    @Override
    default <U> ImmutableSet<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return unitStream(stream().scanLeft(seed,function));
    }

    @Override
    default ImmutableSet<T> scanRight(Monoid<T> monoid) {
        return unitStream(stream().scanRight(monoid));
    }

    @Override
    default <U> ImmutableSet<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return unitStream(stream().scanRight(identity,combiner));
    }

    @Override
    default ImmutableSet<T> sorted() {
        return unitStream(stream().sorted());
    }

    @Override
    default ImmutableSet<T> sorted(Comparator<? super T> c) {
        return unitStream(stream().sorted(c));
    }

    @Override
    default ImmutableSet<T> takeWhile(Predicate<? super T> p) {
        return unitStream(stream().takeWhile(p));
    }

    @Override
    default ImmutableSet<T> dropWhile(Predicate<? super T> p) {
        return unitStream(stream().dropWhile(p));
    }

    @Override
    default ImmutableSet<T> takeUntil(Predicate<? super T> p) {
        return unitStream(stream().takeUntil(p));
    }

    @Override
    default ImmutableSet<T> dropUntil(Predicate<? super T> p) {
        return unitStream(stream().dropUntil(p));
    }

    @Override
    default ImmutableSet<T> dropRight(int num) {
        return unitStream(stream().dropRight(num));
    }

    @Override
    default ImmutableSet<T> takeRight(int num) {
        return unitStream(stream().takeRight(num));
    }

    @Override
    default ImmutableSet<T> drop(long num) {
        return unitStream(stream().drop(num));
    }

    @Override
    default ImmutableSet<T> skip(long num) {
        return unitStream(stream().skip(num));
    }

    @Override
    default ImmutableSet<T> skipWhile(Predicate<? super T> p) {
        return unitStream(stream().skipWhile(p));
    }

    @Override
    default ImmutableSet<T> skipUntil(Predicate<? super T> p) {
        return unitStream(stream().skipUntil(p));
    }

    @Override
    default ImmutableSet<T> take(long num) {
        return unitStream(stream().take(num));
    }

    @Override
    default ImmutableSet<T> limit(long num) {
        return unitStream(stream().limit(num));
    }

    @Override
    default ImmutableSet<T> limitWhile(Predicate<? super T> p) {
        return unitStream(stream().limitWhile(p));
    }

    @Override
    default ImmutableSet<T> limitUntil(Predicate<? super T> p) {
        return unitStream(stream().limitUntil(p));
    }

    @Override
    default ImmutableSet<T> intersperse(T value) {
        return unitStream(stream().intersperse(value));
    }

    @Override
    default ImmutableSet<T> reverse() {
        return unitStream(stream().reverse());
    }

    @Override
    default ImmutableSet<T> shuffle() {
        return unitStream(stream().shuffle());
    }

    @Override
    default ImmutableSet<T> skipLast(int num) {
        return unitStream(stream().skipLast(num));
    }

    @Override
    default ImmutableSet<T> limitLast(int num) {
        return unitStream(stream().limitLast(num));
    }

    @Override
    default ImmutableSet<T> shuffle(Random random) {
        return unitStream(stream().shuffle(random));
    }

    @Override
    default ImmutableSet<T> slice(long from, long to) {
        return unitStream(stream().slice(from,to));
    }

    @Override
    default <U extends Comparable<? super U>> ImmutableSet<T> sorted(Function<? super T, ? extends U> function) {
        return unitStream(stream().sorted(function));
    }

    @Override
    default Traversable<T> traversable() {
        return stream();
    }

    @Override
    default ImmutableSet<T> prependS(Stream<? extends T> stream) {
        return unitStream(stream().prependS(stream));
    }

    @Override
    default ImmutableSet<T> append(T... values) {
        return unitStream(stream().append(values));
    }

    @Override
    default ImmutableSet<T> append(T value) {
        return unitStream(stream().append(value));
    }

    @Override
    default ImmutableSet<T> prepend(T value) {
        return unitStream(stream().prepend(value));
    }

    @Override
    default ImmutableSet<T> prependAll(T... values) {
        return unitStream(stream().prependAll(values));
    }

    @Override
    default ImmutableSet<T> deleteBetween(int start, int end) {
        return unitStream(stream().deleteBetween(start,end));
    }

    @Override
    default ImmutableSet<T> insertAtS(int pos, Stream<T> stream) {
        return unitStream(stream().insertAtS(pos,stream));
    }

    @Override
    default ImmutableSet<T> recover(Function<? super Throwable, ? extends T> fn) {
        return unitStream(stream().recover(fn));
    }

    @Override
    default <EX extends Throwable> ImmutableSet<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return unitStream(stream().recover(exceptionClass,fn));
    }

    @Override
    default ImmutableSet<ReactiveSeq<T>> permutations() {
        return unitStream(stream().permutations());
    }

    @Override
    default ImmutableSet<ReactiveSeq<T>> combinations(int size) {
        return unitStream(stream().combinations(size));
    }

    @Override
    default ImmutableSet<ReactiveSeq<T>> combinations() {
        return unitStream(stream().combinations());
    }

    @Override
    default ImmutableSet<T> removeAt(long pos) {
        return unitStream(stream().removeAt(pos));
    }

    @Override
    default ImmutableSet<T> removeAt(int pos) {
        return unitStream(stream().removeAt(pos));
    }

    @Override
    default ImmutableSet<T> removeFirst(Predicate<? super T> pred) {
        return unitStream(stream().removeFirst(pred));
    }

    @Override
    default ImmutableSet<T> appendAll(Iterable<? extends T> value) {
        return unitStream(stream().appendAll(value));
    }

    @Override
    default ImmutableSet<T> prependAll(Iterable<? extends T> value) {
        return unitStream(stream().prependAll(value));
    }

    @Override
    default ImmutableSet<T> prepend(Iterable<? extends T> value) {
        return unitStream(stream().prepend(value));
    }

    @Override
    default ImmutableSet<T> updateAt(int pos, T value) {
        return unitStream(stream().updateAt(pos,value));
    }


    @Override
    default ImmutableSet<T> insertAt(int pos, Iterable<? extends T> values) {
        return plusAll(values);
    }

    @Override
    default ImmutableSet<T> insertAt(int i, T value) {
        return plus(value);
    }
    @Override
    default ImmutableSet<T> insertAt(int pos, T... values) {

        ImmutableSet<T> res=  this;
        for(T next : values){
            res = res.plus(next);
        }
        return res;
    }
}
