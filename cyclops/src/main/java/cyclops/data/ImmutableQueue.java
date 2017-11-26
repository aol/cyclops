package cyclops.data;

import com.oath.cyclops.types.persistent.PersistentQueue;
import com.oath.cyclops.matching.Deconstruct.Deconstruct2;
import com.oath.cyclops.matching.Sealed2;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.recoverable.OnEmptyError;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.control.Try;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;

import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;


public interface ImmutableQueue<T> extends Sealed2<ImmutableQueue.Some<T>,ImmutableQueue.None<T>>,
                                            OnEmptySwitch<ImmutableQueue<T>, ImmutableQueue<T>>,
                                            OnEmptyError<T, ImmutableQueue<T>>,
                                            IterableX<T>, To<ImmutableQueue<T>>, PersistentQueue<T> {



    @Override
    default ImmutableQueue<T> plus(T e){
        return append(e);
    }

    @Override
    default ImmutableQueue<T> removeValue(T value) {
        return unitStream(stream().removeAll(value));
    }

    @Override
    default ImmutableQueue<T> plusAll(Iterable<? extends T> list){
        ImmutableQueue<T> set = this;
        for(T next : list){
            set = set.plus(next);
        }
        return set;
    }


    <R> ImmutableQueue<R> unitStream(Stream<R> stream);
    <R> ImmutableQueue<R> unitIterable(Iterable<R> it);

    ImmutableQueue<T> emptyUnit();

    default ImmutableQueue<T> replace(T currentElement, T newElement){
        ImmutableQueue<T> preceding = emptyUnit();
        ImmutableQueue<T> tail = this;
        while(!tail.isEmpty()){
            ImmutableQueue<T> ref=  preceding;
            ImmutableQueue<T> tailRef = tail;
            Tuple3<ImmutableQueue<T>, ImmutableQueue<T>, Boolean> t3 = tail.fold(c -> {
                if (Objects.equals(c.head(), currentElement))
                    return Tuple.tuple(ref, tailRef, true);
                return Tuple.tuple(ref.prepend(c.head()), c.tail(), false);
            }, nil -> Tuple.tuple(ref, tailRef, true));

            preceding = t3._1();
            tail = t3._2();
            if(t3._3())
                break;

        }

        ImmutableQueue<T> start = preceding;
        return tail.fold(cons->cons.tail().prepend(newElement).prependAll(start), nil->this);
    }
    default ImmutableQueue<T> removeFirst(Predicate<? super T> pred){
        ImmutableQueue<T> res[] = new ImmutableQueue[]{emptyUnit()};
        ImmutableQueue<T> rem = this;
        boolean[] found = {false};
        do {
            rem = rem.fold(s -> {
                return s.fold((head, tail2) -> {
                    found[0] = pred.test(head);
                    if(!found[0]) {
                        res[0] = res[0].prepend(head);
                        return tail2;
                    }
                    return tail2;
                });
            }, n -> n);
        }while(!rem.isEmpty() && !found[0]);

        ImmutableQueue<T> ar = rem.fold(s -> s.fold((h, t) -> t), n -> n);
        return res[0].foldLeft(ar, (a,b)->a.prepend(b));

    }
    default LinkedListX<T> linkdedListX(){
        return stream().to().linkedListX(Evaluation.LAZY);
    }

    default ImmutableQueue<T> subList(int start, int end){
        return drop(start).take(end-start);
    }
    default LazySeq<T> lazySeq(){

        return fold(c->LazySeq.lazy(c.head(),()->c.tail().lazySeq()), nil->LazySeq.empty());
    }
    default Seq<T> imSeq(){

        return fold(c->Seq.cons(c.head(),c.tail().imSeq()), nil->Seq.empty());
    }



    default Tuple2<ImmutableQueue<T>, ImmutableQueue<T>> splitAt(int n) {
        return Tuple.tuple(take(n), drop(n));
    }

    default Zipper<T> focusAt(int pos, T alt){
        Tuple2<ImmutableQueue<T>, ImmutableQueue<T>> t2 = splitAt(pos);
        T value = t2._2().fold(c -> c.head(), n -> alt);
        ImmutableQueue<T> right= t2._2().fold(c->c.tail(), n->null);
        return Zipper.of(t2._1().lazySeq(),value, right.lazySeq());
    }
    default Option<Zipper<T>> focusAt(int pos){
        Tuple2<ImmutableQueue<T>, ImmutableQueue<T>> t2 = splitAt(pos);
        Option<T> value = t2._2().fold(c -> Option.some(c.head()), n -> Option.none());
        return value.map(l-> {
            ImmutableQueue<T> right = t2._2().fold(c -> c.tail(), n -> null);
            return Zipper.of(t2._1().lazySeq(), l, right.lazySeq());
        });
    }

    ImmutableQueue<T> drop(long num);
    ImmutableQueue<T> take(long num);


    ImmutableQueue<T> prepend(T value);
    ImmutableQueue<T> prependAll(Iterable<? extends T> value);

    ImmutableQueue<T> append(T value);
    ImmutableQueue<T> appendAll(Iterable<? extends T> value);

    ImmutableQueue<T> reverse();

    Option<T> get(int pos);
    T getOrElse(int pos, T alt);
    T getOrElseGet(int pos, Supplier<? extends T> alt);
    int size();
    default boolean containsValue(T value){
        return stream().filter(o-> Objects.equals(value,o)).findFirst().isPresent();
    }
    boolean isEmpty();

    @Override
    default <U> ImmutableQueue<U> ofType(Class<? extends U> type) {
        return (ImmutableQueue<U>)IterableX.super.ofType(type);
    }

    @Override
    default ImmutableQueue<T> filterNot(Predicate<? super T> predicate) {
        return (ImmutableQueue<T>)IterableX.super.filterNot(predicate);
    }

    @Override
    default ImmutableQueue<T> notNull() {
        return (ImmutableQueue<T>)IterableX.super.notNull();
    }

    @Override
    ReactiveSeq<T> stream();


    @Override
    ImmutableQueue<T> filter(Predicate<? super T> fn);


    @Override
    <R> ImmutableQueue<R> map(Function<? super T, ? extends R> fn);

    @Override
    default ImmutableQueue<T> peek(Consumer<? super T> c) {
        return (ImmutableQueue<T>)IterableX.super.peek(c);
    }

    @Override
    default <R> ImmutableQueue<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (ImmutableQueue<R>)IterableX.super.trampoline(mapper);
    }

    @Override
    default <R> ImmutableQueue<R> retry(Function<? super T, ? extends R> fn) {
        return (ImmutableQueue<R>)IterableX.super.retry(fn);
    }

    @Override
    default <R> ImmutableQueue<R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (ImmutableQueue<R>)IterableX.super.retry(fn,retries,delay,timeUnit);
    }

    <R> ImmutableQueue<R> flatMap(Function<? super T, ? extends ImmutableQueue<? extends R>> fn);
    <R> ImmutableQueue<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn);

    @Override
    <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2);
    @Override
    default Iterator<T> iterator() {
        return new Iterator<T>() {
            ImmutableQueue<T> current= ImmutableQueue.this;
            @Override
            public boolean hasNext() {
                return current.fold(c->true, n->false);
            }

            @Override
            public T next() {
                return current.fold(c->{
                    current = c.tail();
                    return c.head();
                },n->null);
            }
        };
    }

    @Override
    default <R> ImmutableQueue<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatMapI(mapper);
    }

    @Override
    ImmutableQueue<T> onEmpty(T value);

    @Override
    ImmutableQueue<T> onEmptyGet(Supplier<? extends T> supplier);

    @Override
    default <X extends Throwable> Try<ImmutableQueue<T>, X> onEmptyTry(Supplier<? extends X> supplier){
        return isEmpty() ? Try.failure(supplier.get()) : Try.success(this);
    }

    @Override
    ImmutableQueue<T> onEmptySwitch(Supplier<? extends ImmutableQueue<T>> supplier);

    public static interface Some<T> extends Deconstruct2<T,ImmutableQueue<T>>, ImmutableQueue<T> {
        T head();
        ImmutableQueue<T> tail();
        @Override
        default <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2){
            return fn1.apply(this);
        }
    }
    public interface None<T> extends ImmutableQueue<T> {
        @Override
        default <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2){
            return fn2.apply(this);
        }

    }



    default <R1, R2, R3, R> ImmutableQueue<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1,
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

    default <R1, R2, R3, R> ImmutableQueue<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1,
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


    default <R1, R2, R> ImmutableQueue<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1,
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


    default <R1, R2, R> ImmutableQueue<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1,
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


    default <R1, R> ImmutableQueue<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1,
                                               BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMapI(in-> {

            Iterable<? extends R1> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }


    default <R1, R> ImmutableQueue<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1,
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
    default ImmutableQueue<T> removeAllS(Stream<? extends T> stream) {
        return unitStream(stream().removeAllS(stream));
    }

    default ImmutableQueue<T> removeAll(Iterable<? extends T> it) {
        return unitStream(stream().removeAll(it));
    }

    @Override
    default ImmutableQueue<T> removeAll(T... values) {
        return unitStream(stream().removeAll(values));
    }

    @Override
    default ImmutableQueue<T> retainAllI(Iterable<? extends T> it) {
        return unitStream(stream().retainAllI(it));
    }

    @Override
    default ImmutableQueue<T> retainAllS(Stream<? extends T> stream) {
        return unitStream(stream().retainAllS(stream));
    }

    @Override
    default ImmutableQueue<T> retainAll(T... values) {
        return unitStream(stream().retainAll(values));
    }



    @Override
    default ImmutableQueue<ReactiveSeq<T>> permutations() {
        return unitStream(stream().permutations());
    }

    @Override
    default ImmutableQueue<ReactiveSeq<T>> combinations(int size) {
        return unitStream(stream().combinations(size));
    }

    @Override
    default ImmutableQueue<ReactiveSeq<T>> combinations() {
        return unitStream(stream().combinations());
    }

  @Override
    default <T2, R> ImmutableQueue<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return unitStream(stream().zip(fn, publisher));
    }

    default <U, R> ImmutableQueue<R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return unitStream(stream().zipWithStream(other,zipper));
    }

    @Override
    default <U> ImmutableQueue<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return unitStream(stream().zipWithPublisher(other));
    }

    @Override
    default <U> ImmutableQueue<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return unitStream(stream().zip(other));
    }

    @Override
    default <S, U, R> ImmutableQueue<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return unitStream(stream().zip3(second,third,fn3));
    }

    @Override
    default <T2, T3, T4, R> ImmutableQueue<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return unitStream(stream().zip4(second,third,fourth,fn));
    }

    @Override
    default <U> ImmutableQueue<U> unitIterator(Iterator<U> it){
        return unitStream(ReactiveSeq.fromIterator(it));
    }


    @Override
    default ImmutableQueue<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return unitStream(stream().combine(predicate,op));
    }

    @Override
    default ImmutableQueue<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return unitStream(stream().combine(op,predicate));
    }

    @Override
    default ImmutableQueue<T> cycle(long times) {
        return unitStream(stream().cycle(times));
    }

    @Override
    default ImmutableQueue<T> cycle(Monoid<T> m, long times) {
        return unitStream(stream().cycle(m,times));
    }

    @Override
    default ImmutableQueue<T> cycleWhile(Predicate<? super T> predicate) {
        return unitStream(stream().cycleWhile(predicate));
    }

    @Override
    default ImmutableQueue<T> cycleUntil(Predicate<? super T> predicate) {
        return unitStream(stream().cycleUntil(predicate));
    }

    @Override
    default <U, R> ImmutableQueue<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return unitStream(stream().zip(other,zipper));
    }

    @Override
    default <S, U> ImmutableQueue<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return unitStream(stream().zip3(second,third));
    }

    @Override
    default <T2, T3, T4> ImmutableQueue<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return unitStream(stream().zip4(second,third,fourth));
    }

    @Override
    default ImmutableQueue<Tuple2<T, Long>> zipWithIndex() {
        return unitStream(stream().zipWithIndex());
    }

    @Override
    default ImmutableQueue<VectorX<T>> sliding(int windowSize) {
        return unitStream(stream().sliding(windowSize));
    }

    @Override
    default ImmutableQueue<VectorX<T>> sliding(int windowSize, int increment) {
        return unitStream(stream().sliding(windowSize,increment));
    }

    @Override
    default <C extends Collection<? super T>> ImmutableQueue<C> grouped(int size, Supplier<C> supplier) {
        return unitStream(stream().grouped(size,supplier));
    }

    @Override
    default ImmutableQueue<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return unitStream(stream().groupedUntil(predicate));
    }

    @Override
    default ImmutableQueue<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return unitStream(stream().groupedStatefullyUntil(predicate));
    }

    default <U> ImmutableQueue<Tuple2<T, U>> zipS(Stream<? extends U> other) {
        return unitStream(stream().zipWithStream(other));
    }

    @Override
    default ImmutableQueue<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return unitStream(stream().groupedWhile(predicate));
    }

    @Override
    default <C extends Collection<? super T>> ImmutableQueue<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return unitStream(stream().groupedWhile(predicate,factory));
    }

    @Override
    default <C extends Collection<? super T>> ImmutableQueue<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return unitStream(stream().groupedUntil(predicate,factory));
    }

    @Override
    default ImmutableQueue<ListX<T>> grouped(int groupSize) {
        return unitStream(stream().grouped(groupSize));
    }



    @Override
    default ImmutableQueue<T> distinct() {
        return unitStream(stream().distinct());
    }

    @Override
    default ImmutableQueue<T> scanLeft(Monoid<T> monoid) {
        return scanLeft(monoid.zero(),monoid);
    }

    @Override
    default <U> ImmutableQueue<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return unitStream(stream().scanLeft(seed,function));
    }

    @Override
    default ImmutableQueue<T> scanRight(Monoid<T> monoid) {
        return scanRight(monoid.zero(),monoid);
    }

    @Override
    default <U> ImmutableQueue<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return unitStream(stream().scanRight(identity,combiner));
    }

    @Override
    default ImmutableQueue<T> sorted() {
        return unitStream(stream().sorted());
    }

    @Override
    default ImmutableQueue<T> sorted(Comparator<? super T> c) {
        return unitStream(stream().sorted(c));
    }

    @Override
    default ImmutableQueue<T> takeWhile(Predicate<? super T> p) {
        return unitStream(stream().takeWhile(p));
    }

    @Override
    default ImmutableQueue<T> dropWhile(Predicate<? super T> p) {
        return unitStream(stream().dropWhile(p));
    }

    @Override
    default ImmutableQueue<T> takeUntil(Predicate<? super T> p) {
        return unitStream(stream().takeUntil(p));
    }

    @Override
    default ImmutableQueue<T> dropUntil(Predicate<? super T> p) {
        return unitStream(stream().dropUntil(p));
    }

    @Override
    default ImmutableQueue<T> dropRight(int num) {
        return unitStream(stream().dropRight(num));
    }

    @Override
    default ImmutableQueue<T> takeRight(int num) {
        return unitStream(stream().takeRight(num));
    }



    @Override
    default ImmutableQueue<T> skip(long num) {
        return unitStream(stream().skip(num));
    }

    @Override
    default ImmutableQueue<T> skipWhile(Predicate<? super T> p) {
        return unitStream(stream().skipWhile(p));
    }

    @Override
    default ImmutableQueue<T> skipUntil(Predicate<? super T> p) {
        return unitStream(stream().skipUntil(p));
    }



    @Override
    default ImmutableQueue<T> limit(long num) {
        return unitStream(stream().limit(num));
    }

    @Override
    default ImmutableQueue<T> limitWhile(Predicate<? super T> p) {
        return unitStream(stream().limitWhile(p));
    }

    @Override
    default ImmutableQueue<T> limitUntil(Predicate<? super T> p) {
        return unitStream(stream().limitUntil(p));
    }

    @Override
    default ImmutableQueue<T> intersperse(T value) {
        return unitStream(stream().intersperse(value));
    }

    @Override
    default ImmutableQueue<T> shuffle() {
        return unitStream(stream().shuffle());
    }

    @Override
    default ImmutableQueue<T> skipLast(int num) {
        return unitStream(stream().skipLast(num));
    }

    @Override
    default ImmutableQueue<T> limitLast(int num) {
        return unitStream(stream().limitLast(num));
    }

    @Override
    default ImmutableQueue<T> shuffle(Random random) {
        return unitStream(stream().shuffle(random));
    }

    @Override
    default ImmutableQueue<T> slice(long from, long to) {
        return unitStream(stream().slice(from,to));
    }

    @Override
    default <U extends Comparable<? super U>> ImmutableQueue<T> sorted(Function<? super T, ? extends U> function) {
        return unitStream(stream().sorted(function));
    }

    @Override
    default Traversable<T> traversable() {
        return stream();
    }

    @Override
    default ImmutableQueue<T> prependS(Stream<? extends T> stream) {
        return unitStream(stream().prependS(stream));
    }

    @Override
    default ImmutableQueue<T> insertAt(int pos, T... values) {
        if(pos==0)
            return prependAll(values);
        if(pos>=size())
            return append(values);
        return unitStream(stream().insertAt(pos,values));
    }
    @Override
    default ImmutableQueue<T> insertAt(int pos, T values) {
        if(pos==0)
            return prependAll(values);
        if(pos>=size())
            return append(values);
        return unitStream(stream().insertAt(pos,values));
    }

    @Override
    default ImmutableQueue<T> append(T... values) {
        ImmutableQueue<T> res = this;
        for(T t : values){
            res = res.append(t);
        }
        return res;
    }

    @Override
    default ImmutableQueue<T> prependAll(T... values) {
        ImmutableQueue<T> res = this;
        for(T t : values){
            res = res.prepend(t);
        }
        return res;
    }


    @Override
    default ImmutableQueue<T> deleteBetween(int start, int end) {
        return unitStream(stream().deleteBetween(start,end));
    }

    @Override
    default ImmutableQueue<T> insertAtS(int pos, Stream<T> stream) {
        return unitStream(stream().insertAtS(pos,stream));
    }

    @Override
    default ImmutableQueue<T> recover(Function<? super Throwable, ? extends T> fn) {
        return unitStream(stream().recover(fn));
    }

    @Override
    default <EX extends Throwable> ImmutableQueue<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return unitStream(stream().recover(exceptionClass,fn));
    }

    @Override
    ImmutableQueue<T> minus();

    @Override
    default ImmutableQueue<T> removeAt(long pos) {
        return unitStream(stream().removeAt(pos));
    }

    @Override
    default ImmutableQueue<T> removeAt(int pos) {
        return unitStream(stream().removeAt(pos));
    }

    @Override
    default ImmutableQueue<T> prepend(Iterable<? extends T> value) {
        return unitStream(stream().prepend(value));
    }

    @Override
    default ImmutableQueue<T> updateAt(int pos, T value) {
        return unitStream(stream().updateAt(pos,value));
    }



    @Override
    default ImmutableQueue<T> insertAt(int pos, Iterable<? extends T> values) {
        return unitIterable(IterableX.super.insertAt(pos,values));
    }
}
