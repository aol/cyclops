package cyclops.data;

import com.aol.cyclops2.data.collections.extensions.api.PStack;
import com.aol.cyclops2.matching.Deconstruct.Deconstruct2;
import com.aol.cyclops2.matching.Sealed2;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.foldable.Evaluation;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.recoverable.OnEmptySwitch;
import com.aol.cyclops2.types.traversable.IterableX;
import com.aol.cyclops2.types.traversable.Traversable;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.collectionx.immutable.VectorX;
import cyclops.collectionx.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.lazy.Trampoline;
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

import static cyclops.matching.Api.*;


public interface ImmutableList<T> extends Sealed2<ImmutableList.Some<T>,ImmutableList.None<T>>,
                                          IterableX<T>,PStack<T>,
                                          OnEmptySwitch<ImmutableList<T>,ImmutableList<T>>,
                                          To<ImmutableList<T>> {

    <R> ImmutableList<R> unitStream(Stream<R> stream);
    <R> ImmutableList<R> unitIterable(Iterable<R> it);


    ImmutableList<T> emptyUnit();

    default boolean equalToDirectAccess(Iterable<T> iterable){
        int size = size();
        Iterator<T> it = iterable.iterator();
        for(int i=0;i<size;i++){
            T at = this.getOrElse(i,null);
            if(!it.hasNext())
                return false;
            if(!Objects.equals(at,it.next()))
                return false;
        }
        return !it.hasNext();
    }



    default ImmutableList<T> replace(T currentElement, T newElement){
        ImmutableList<T> preceding = emptyUnit();
        ImmutableList<T> tail = this;
        while(!tail.isEmpty()){
            ImmutableList<T> ref=  preceding;
            ImmutableList<T> tailRef = tail;
            Tuple3<ImmutableList<T>, ImmutableList<T>, Boolean> t3 = tail.fold(c -> {
                if (Objects.equals(c.head(), currentElement))
                    return Tuple.tuple(ref, tailRef, true);
                return Tuple.tuple(ref.prepend(c.head()), c.tail(), false);
            }, nil -> Tuple.tuple(ref, tailRef, true));

            preceding = t3._1();
            tail = t3._2();
            if(t3._3())
                break;

        }

        ImmutableList<T> start = preceding;
        return tail.fold(cons->cons.tail().prepend(newElement).prependAll(start), nil->this);
    }
    default ImmutableList<T> removeFirst(Predicate<? super T> pred){
        return unitStream(stream().removeFirst(pred));

    }
    default LinkedListX<T> linkdedListX(){
        return stream().to().linkedListX(Evaluation.LAZY);
    }

    default ImmutableList<T> subList(int start, int end){
        return drop(start).take(end-start);
    }
    default LazySeq<T> lazySeq(){
        if(this instanceof LazySeq){
            return (LazySeq<T>)this;
        }
        return fold(c->LazySeq.lazy(c.head(),()->c.tail().lazySeq()), nil->LazySeq.empty());
    }
    default Seq<T> imSeq(){
        if(this instanceof Seq){
            return (Seq<T>)this;
        }
        return fold(c->Seq.cons(c.head(),c.tail().imSeq()), nil->Seq.empty());
    }
    default Option<NonEmptyList<T>> nonEmptyList(){
        return Option.ofNullable(fold(c->NonEmptyList.cons(c.head(),c.tail()), nil->null));
    }


    default Tuple2<ImmutableList<T>, ImmutableList<T>> splitAt(int n) {
        return Tuple.tuple(take(n), drop(n));
    }

    default Zipper<T> focusAt(int pos, T alt){
        Tuple2<ImmutableList<T>, ImmutableList<T>> t2 = splitAt(pos);
        T value = t2._2().fold(c -> c.head(), n -> alt);
        ImmutableList<T> right= t2._2().fold(c->c.tail(), n->null);
        return Zipper.of(t2._1(),value, right);
    }
    default Option<Zipper<T>> focusAt(int pos){
        Tuple2<ImmutableList<T>, ImmutableList<T>> t2 = splitAt(pos);
        Option<T> value = t2._2().fold(c -> Option.some(c.head()), n -> Option.none());
        return value.map(l-> {
            ImmutableList<T> right = t2._2().fold(c -> c.tail(), n -> null);
            return Zipper.of(t2._1(), l, right);
        });
    }

    ImmutableList<T> drop(long num);
    ImmutableList<T> take(long num);


    ImmutableList<T> prepend(T value);
    ImmutableList<T> prependAll(Iterable<? extends T> value);

    ImmutableList<T> append(T value);
    ImmutableList<T> appendAll(Iterable<? extends T> value);

    ImmutableList<T> reverse();

    Option<T> get(int pos);
    T getOrElse(int pos, T alt);
    T getOrElseGet(int pos, Supplier<? extends T> alt);
    int size();
    default boolean containsValue(T value){
        return stream().filter(o-> Objects.equals(value,o)).findFirst().isPresent();
    }
    boolean isEmpty();

    @Override
    default <U> ImmutableList<U> ofType(Class<? extends U> type) {
        return (ImmutableList<U>)IterableX.super.ofType(type);
    }

    @Override
    default ImmutableList<T> filterNot(Predicate<? super T> predicate) {
        return (ImmutableList<T>)IterableX.super.filterNot(predicate);
    }

    @Override
    default ImmutableList<T> notNull() {
        return (ImmutableList<T>)IterableX.super.notNull();
    }

    @Override
    ReactiveSeq<T> stream();


    @Override
    ImmutableList<T> filter(Predicate<? super T> fn);


    @Override
    <R> ImmutableList<R> map(Function<? super T, ? extends R> fn);

    @Override
    default ImmutableList<T> peek(Consumer<? super T> c) {
        return (ImmutableList<T>)IterableX.super.peek(c);
    }

    @Override
    default <R> ImmutableList<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (ImmutableList<R>)IterableX.super.trampoline(mapper);
    }

    @Override
    default <R> ImmutableList<R> retry(Function<? super T, ? extends R> fn) {
        return (ImmutableList<R>)IterableX.super.retry(fn);
    }

    @Override
    default <R> ImmutableList<R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (ImmutableList<R>)IterableX.super.retry(fn,retries,delay,timeUnit);
    }

    <R> ImmutableList<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn);
    //@TODO removeValue
    <R> ImmutableList<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn);

    @Override
    <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2);
    @Override
    default Iterator<T> iterator() {
        return new Iterator<T>() {
            ImmutableList<T> current= ImmutableList.this;
            @Override
            public boolean hasNext() {
                return current.fold(c->true, n->false);
            }

            @Override
            public T next() {
               return MatchType(current).of(Case(list->{
                                                        current = list.tail();
                                                        return list.head();
                                                    }),
                                Case(nil->null));

            }
        };
    }

    @Override
    ImmutableList<T> onEmpty(T value);

    @Override
    ImmutableList<T> onEmptyGet(Supplier<? extends T> supplier);

    @Override
    <X extends Throwable> ImmutableList<T> onEmptyThrow(Supplier<? extends X> supplier);

    default T last(T alt){
        return getOrElse(size()-1,alt);
    }
    @Override
    ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier);


    public static interface Some<T> extends Deconstruct2<T,ImmutableList<T>>, ImmutableList<T> {
        ImmutableList<T> tail();
        T head();
        Some<T> reverse();

        @Override
        default <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2){
            return fn1.apply(this);
        }
    }
    public interface None<T> extends ImmutableList<T> {
        @Override
        default <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2){
            return fn2.apply(this);
        }

    }



    default <R1, R2, R3, R> ImmutableList<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1,
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

    default <R1, R2, R3, R> ImmutableList<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1,
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


    default <R1, R2, R> ImmutableList<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1,
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


    default <R1, R2, R> ImmutableList<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1,
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


    default <R1, R> ImmutableList<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1,
                                              BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return this.flatMapI(in-> {

            Iterable<? extends R1> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }


    default <R1, R> ImmutableList<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1,
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
    default ImmutableList<T> removeAllS(Stream<? extends T> stream) {
        return unitStream(stream().removeAllS(stream));
    }

    @Override
    default ImmutableList<T> removeAllI(Iterable<? extends T> it) {
        return unitStream(stream().removeAllI(it));
    }
    @Override
    default ImmutableList<T> removeAt(long pos) {
        return unitStream(stream().removeAt(pos));
    }

    @Override
    default ImmutableList<T> removeAll(T... values) {
        return unitStream(stream().removeAll(values));
    }

    @Override
    default ImmutableList<T> retainAllI(Iterable<? extends T> it) {
        return unitStream(stream().retainAllI(it));
    }

    @Override
    default ImmutableList<T> retainAllS(Stream<? extends T> stream) {
        return unitStream(stream().retainAllS(stream));
    }

    @Override
    default ImmutableList<T> retainAll(T... values) {
        return unitStream(stream().retainAll(values));
    }



    @Override
    default ImmutableList<ReactiveSeq<T>> permutations() {
        return unitStream(stream().permutations());
    }

    @Override
    default ImmutableList<ReactiveSeq<T>> combinations(int size) {
        return unitStream(stream().combinations(size));
    }

    @Override
    default ImmutableList<ReactiveSeq<T>> combinations() {
        return unitStream(stream().combinations());
    }

    @Override
    default ImmutableList<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return unitStream(stream().zip(combiner,app));
    }

    @Override
    default <R> ImmutableList<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return unitStream(stream().zipWith(fn));
    }

    @Override
    default <R> ImmutableList<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return unitStream(stream().zipWithS(fn));
    }

    @Override
    default <R> ImmutableList<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return unitStream(stream().zipWithP(fn));
    }

    @Override
    default <T2, R> ImmutableList<R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return unitStream(stream().zipP(publisher,fn));
    }

    @Override
    default <U, R> ImmutableList<R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return unitStream(stream().zipS(other,zipper));
    }

    @Override
    default <U> ImmutableList<Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return unitStream(stream().zipP(other));
    }

    @Override
    default <U> ImmutableList<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return unitStream(stream().zip(other));
    }

    @Override
    default <S, U, R> ImmutableList<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return unitStream(stream().zip3(second,third,fn3));
    }

    @Override
    default <T2, T3, T4, R> ImmutableList<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return unitStream(stream().zip4(second,third,fourth,fn));
    }

    @Override
    default <U> ImmutableList<U> unitIterator(Iterator<U> it){
        return unitStream(ReactiveSeq.fromIterator(it));
    }


    @Override
    default ImmutableList<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return unitStream(stream().combine(predicate,op));
    }

    @Override
    default ImmutableList<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return unitStream(stream().combine(op,predicate));
    }

    @Override
    default ImmutableList<T> cycle(long times) {
        return unitStream(stream().cycle(times));
    }

    @Override
    default ImmutableList<T> cycle(Monoid<T> m, long times) {
        return unitStream(stream().cycle(m,times));
    }

    @Override
    default ImmutableList<T> cycleWhile(Predicate<? super T> predicate) {
        return unitStream(stream().cycleWhile(predicate));
    }

    @Override
    default ImmutableList<T> cycleUntil(Predicate<? super T> predicate) {
        return unitStream(stream().cycleUntil(predicate));
    }

    @Override
    default <U, R> ImmutableList<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return unitStream(stream().zip(other,zipper));
    }

    @Override
    default <S, U> ImmutableList<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return unitStream(stream().zip3(second,third));
    }

    @Override
    default <T2, T3, T4> ImmutableList<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return unitStream(stream().zip4(second,third,fourth));
    }

    @Override
    default ImmutableList<Tuple2<T, Long>> zipWithIndex() {
        return unitStream(stream().zipWithIndex());
    }

    @Override
    default ImmutableList<VectorX<T>> sliding(int windowSize) {
        return unitStream(stream().sliding(windowSize));
    }

    @Override
    default ImmutableList<VectorX<T>> sliding(int windowSize, int increment) {
        return unitStream(stream().sliding(windowSize,increment));
    }

    @Override
    default <C extends Collection<? super T>> ImmutableList<C> grouped(int size, Supplier<C> supplier) {
        return unitStream(stream().grouped(size,supplier));
    }

    @Override
    default ImmutableList<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return unitStream(stream().groupedUntil(predicate));
    }

    @Override
    default ImmutableList<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return unitStream(stream().groupedStatefullyUntil(predicate));
    }

    @Override
    default <U> ImmutableList<Tuple2<T, U>> zipS(Stream<? extends U> other) {
        return unitStream(stream().zipS(other));
    }

    @Override
    default ImmutableList<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return unitStream(stream().groupedWhile(predicate));
    }

    @Override
    default <C extends Collection<? super T>> ImmutableList<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return unitStream(stream().groupedWhile(predicate,factory));
    }

    @Override
    default <C extends Collection<? super T>> ImmutableList<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return unitStream(stream().groupedUntil(predicate,factory));
    }

    @Override
    default ImmutableList<ListX<T>> grouped(int groupSize) {
        return unitStream(stream().grouped(groupSize));
    }


    @Override
    default ImmutableList<T> distinct() {
        return unitStream(stream().distinct());
    }

    @Override
    default ImmutableList<T> scanLeft(Monoid<T> monoid) {
        return scanLeft(monoid.zero(),monoid);
    }

    @Override
    default <U> ImmutableList<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return unitStream(stream().scanLeft(seed,function));
    }

    @Override
    default ImmutableList<T> scanRight(Monoid<T> monoid) {
        return scanRight(monoid.zero(),monoid);
    }

    @Override
    default <U> ImmutableList<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return unitStream(stream().scanRight(identity,combiner));
    }

    @Override
    default ImmutableList<T> sorted() {
        return unitStream(stream().sorted());
    }

    @Override
    default ImmutableList<T> sorted(Comparator<? super T> c) {
        return unitStream(stream().sorted(c));
    }

    @Override
    default ImmutableList<T> takeWhile(Predicate<? super T> p) {
        return unitStream(stream().takeWhile(p));
    }

    @Override
    default ImmutableList<T> dropWhile(Predicate<? super T> p) {
        return unitStream(stream().dropWhile(p));
    }

    @Override
    default ImmutableList<T> takeUntil(Predicate<? super T> p) {
        return unitStream(stream().takeUntil(p));
    }

    @Override
    default ImmutableList<T> dropUntil(Predicate<? super T> p) {
        return unitStream(stream().dropUntil(p));
    }

    @Override
    default ImmutableList<T> dropRight(int num) {
        return unitStream(stream().dropRight(num));
    }

    @Override
    default ImmutableList<T> takeRight(int num) {
        return unitStream(stream().takeRight(num));
    }



    @Override
    default ImmutableList<T> skip(long num) {
        return unitStream(stream().skip(num));
    }

    @Override
    default ImmutableList<T> skipWhile(Predicate<? super T> p) {
        return unitStream(stream().skipWhile(p));
    }

    @Override
    default ImmutableList<T> skipUntil(Predicate<? super T> p) {
        return unitStream(stream().skipUntil(p));
    }



    @Override
    default ImmutableList<T> limit(long num) {
        return unitStream(stream().limit(num));
    }

    @Override
    default ImmutableList<T> limitWhile(Predicate<? super T> p) {
        return unitStream(stream().limitWhile(p));
    }

    @Override
    default ImmutableList<T> limitUntil(Predicate<? super T> p) {
        return unitStream(stream().limitUntil(p));
    }

    @Override
    default ImmutableList<T> intersperse(T value) {
        return unitStream(stream().intersperse(value));
    }

    @Override
    default ImmutableList<T> shuffle() {
        return unitStream(stream().shuffle());
    }

    @Override
    default ImmutableList<T> skipLast(int num) {
        return unitStream(stream().skipLast(num));
    }

    @Override
    default ImmutableList<T> limitLast(int num) {
        return unitStream(stream().limitLast(num));
    }

    @Override
    default ImmutableList<T> shuffle(Random random) {
        return unitStream(stream().shuffle(random));
    }

    @Override
    default ImmutableList<T> slice(long from, long to) {
        return unitStream(stream().slice(from,to));
    }

    @Override
    default <U extends Comparable<? super U>> ImmutableList<T> sorted(Function<? super T, ? extends U> function) {
        return unitStream(stream().sorted(function));
    }

    @Override
    default Traversable<T> traversable() {
        return stream();
    }

    @Override
    default <R> IterableX<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatMapI(mapper);
    }

    @Override
    default ImmutableList<T> prependS(Stream<? extends T> stream) {
        return unitStream(stream().prependS(stream));
    }

    @Override
    default ImmutableList<T> append(T... values) {
        ImmutableList<T> res = this;
        for(T t : values){
            res = res.append(t);
        }
        return res;
    }

    @Override
    default ImmutableList<T> prependAll(T... values) {
        ImmutableList<T> res = this;
        for(T t : values){
            res = res.prepend(t);
        }
        return res;
    }

    @Override
    default ImmutableList<T> insertAt(int pos, T... values) {
        if(pos==0)
            return prependAll(values);
        if(pos>=size())
            return append(values);
        return unitStream(stream().insertAt(pos,values));
    }

    @Override
    default ImmutableList<T> deleteBetween(int start, int end) {
        return unitStream(stream().deleteBetween(start,end));
    }

    @Override
    default ImmutableList<T> insertAtS(int pos, Stream<T> stream) {
        return unitStream(stream().insertAtS(pos,stream));
    }

    @Override
    default ImmutableList<T> recover(Function<? super Throwable, ? extends T> fn) {
        return unitStream(stream().recover(fn));
    }

    @Override
    default <EX extends Throwable> ImmutableList<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return unitStream(stream().recover(exceptionClass,fn));
    }

    @Override
    default ImmutableList<T> plusAll(Iterable<? extends T> list) {
        return unitIterable(IterableX.super.plusAll(list));
    }

    @Override
    default ImmutableList<T> plus(T value) {
        return unitIterable(IterableX.super.plus(value));
    }

    @Override
    default ImmutableList<T> removeValue(T value) {
        return unitIterable(IterableX.super.removeValue(value));
    }

    @Override
    default ImmutableList<T> removeAt(int pos) {
        return unitIterable(IterableX.super.removeAt(pos));
    }

    @Override
    default ImmutableList<T> removeAll(Iterable<? extends T> value) {
        return unitIterable(IterableX.super.removeAll(value));
    }

    @Override
    default ImmutableList<T> prepend(Iterable<? extends T> value) {
        return unitIterable(IterableX.super.prepend(value));
    }

    @Override
    default ImmutableList<T> updateAt(int pos, T value) {
        return unitIterable(IterableX.super.updateAt(pos,value));
    }

    @Override
    default ImmutableList<T> insertAt(int pos, Iterable<? extends T> values) {
        return unitIterable(IterableX.super.insertAt(pos,values));
    }

    @Override
    default ImmutableList<T> insertAt(int i, T value) {
        return unitIterable(IterableX.super.insertAt(i,value));
    }
}
