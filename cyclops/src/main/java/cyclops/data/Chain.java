package cyclops.data;

import com.oath.cyclops.internal.stream.OneShotStreamX;
import com.oath.cyclops.internal.stream.StreamX;
import com.oath.cyclops.internal.stream.spliterators.IteratableSpliterator;
import com.oath.cyclops.types.futurestream.Continuation;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentIndexed;
import com.oath.cyclops.types.persistent.PersistentList;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.control.Option;
import cyclops.control.Try;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Chain<T> implements ImmutableList<T>{


    public static abstract class NonEmptyChain<T> extends Chain<T> implements ImmutableList.Some<T>{
        @Override
        public NonEmptyChain<T> appendAll(Iterable<? extends T> value) {
            Chain<? extends T> w = wrap(value);
            return w.isEmpty() ? this : append(this,w);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
        @Override
        public <R> Chain<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
            return wrap(ReactiveSeq.fromIterable(this).concatMap(fn));
        }

        public <R> NonEmptyChain<R> flatMapNEC(Function<? super T, ? extends NonEmptyChain<? extends R>> fn) {
            return new Wrap(ReactiveSeq.fromIterable(this).concatMap(fn));
        }

        @Override
        public NonEmptyChain<T> prepend(T value) {
            return append(singleton(value),this);
        }
        @Override
        public NonEmptyChain<T> prependAll(Iterable< ? extends T> value) {
            return append(wrap(value),this);
        }
        @Override
        public NonEmptyChain<T> append(T value) {
            return append(this,singleton(value));
        }
        @Override
        public  <R> NonEmptyChain<R> map(Function<? super T, ? extends R> fn){
            return new Wrap(ReactiveSeq.fromIterable(this).map(fn));
        }

        @Override
        public NonEmptyChain<T> concat(Chain<T> b) {
            return append(this,b);
        }

        @Override
        public NonEmptyChain<T> insertStreamAt(int pos, Stream<T> stream) {
            return (NonEmptyChain<T>)super.insertStreamAt(pos, stream);
        }

        @Override
        public NonEmptyChain<T> prependStream(Stream<? extends T> stream) {
            return (NonEmptyChain<T>)super.prependStream(stream);
        }

        @Override
        public NonEmptyChain<T> peek(Consumer<? super T> c) {
            return (NonEmptyChain<T>)super.peek(c);
        }

        @Override
        public NonEmptyChain<ReactiveSeq<T>> permutations() {
            return (NonEmptyChain<ReactiveSeq<T>>)super.permutations();
        }

        @Override
        public NonEmptyChain<ReactiveSeq<T>> combinations(int size) {
            return (NonEmptyChain<ReactiveSeq<T>>)super.combinations(size);
        }

        @Override
        public NonEmptyChain<ReactiveSeq<T>> combinations() {
            return (NonEmptyChain<ReactiveSeq<T>>)super.combinations();
        }

        @Override
        public NonEmptyChain<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
            return (NonEmptyChain<T>)super.combine(predicate, op);
        }

        @Override
        public NonEmptyChain<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
            return (NonEmptyChain<T>)super.combine(op, predicate);
        }

        @Override
        public NonEmptyChain<T> cycle(long times) {
            return (NonEmptyChain<T>)super.cycle(times);
        }

        @Override
        public NonEmptyChain<T> cycle(Monoid<T> m, long times) {
            return (NonEmptyChain<T>)super.cycle(m, times);
        }

        @Override
        public NonEmptyChain<Seq<T>> sliding(int windowSize) {
            return (NonEmptyChain<Seq<T>>)super.sliding(windowSize);
        }

        @Override
        public NonEmptyChain<Seq<T>> sliding(int windowSize, int increment) {
            return (NonEmptyChain<Seq<T>>)super.sliding(windowSize, increment);
        }

        @Override
        public <C extends PersistentCollection<? super T>> NonEmptyChain<C> grouped(int size, Supplier<C> supplier) {
            return (NonEmptyChain<C>)super.grouped(size, supplier);
        }

        @Override
        public NonEmptyChain<Vector<T>> grouped(int groupSize) {
            return (NonEmptyChain<Vector<T>>)super.grouped(groupSize);
        }

        @Override
        public NonEmptyChain<T> distinct() {
            return (NonEmptyChain<T>)super.distinct();
        }

        @Override
        public NonEmptyChain<T> scanLeft(Monoid<T> monoid) {
            return (NonEmptyChain<T>)super.scanLeft(monoid);
        }

        @Override
        public <U> NonEmptyChain<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
            return (NonEmptyChain<U>)super.scanLeft(seed, function);
        }

        @Override
        public NonEmptyChain<T> scanRight(Monoid<T> monoid) {
            return (NonEmptyChain<T>)super.scanRight(monoid);
        }

        @Override
        public <U> NonEmptyChain<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
            return (NonEmptyChain<U>)super.scanRight(identity, combiner);
        }

        @Override
        public NonEmptyChain<T> sorted() {
            return (NonEmptyChain<T>)super.sorted();
        }

        @Override
        public NonEmptyChain<T> sorted(Comparator<? super T> c) {
            return (NonEmptyChain<T>)super.sorted(c);
        }

        @Override
        public NonEmptyChain<T> intersperse(T value) {
            return (NonEmptyChain<T>)super.intersperse(value);
        }

        @Override
        public NonEmptyChain<T> shuffle() {
            return (NonEmptyChain<T>)super.shuffle();
        }

        @Override
        public NonEmptyChain<T> shuffle(Random random) {
            return (NonEmptyChain<T>)super.shuffle(random);
        }

        @Override
        public <U extends Comparable<? super U>> NonEmptyChain<T> sorted(Function<? super T, ? extends U> function) {
            return (NonEmptyChain<T>)super.sorted(function);
        }

        @Override
        public NonEmptyChain<T> prependAll(T... values) {
            return (NonEmptyChain<T>)super.prependAll(values);
        }

        @Override
        public NonEmptyChain<T> insertAt(int pos, T... values) {
            return (NonEmptyChain<T>)super.insertAt(pos, values);
        }

        @Override
        public NonEmptyChain<T> plusAll(Iterable<? extends T> list) {
            return (NonEmptyChain<T>)super.plusAll(list);
        }

        @Override
        public NonEmptyChain<T> plus(T value) {
            return super.plus(value);
        }

        @Override
        public NonEmptyChain<T> insertAt(int pos, Iterable<? extends T> values) {
            return (NonEmptyChain<T>)super.insertAt(pos, values);
        }

        @Override
        public NonEmptyChain<T> insertAt(int i, T value) {
            return (NonEmptyChain<T>)super.insertAt(i, value);
        }

        @Override
        public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn1.apply(this);
        }

        @Override
        public NonEmptyChain<T> onEmpty(T value) {
            return this;
        }

        @Override
        public NonEmptyChain<T> onEmptyGet(Supplier<? extends T> supplier) {
            return this;
        }


        private Supplier<Integer> hash;
        @Override
        public int hashCode() {
            if(hash==null){
                Supplier<Integer> local = Memoize.memoizeSupplier(()->{
                    int hashCode = 1;
                    for (T e : this)
                        hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
                    return hashCode;
                });
                hash= local;
                return local.get();
            }
            return hash.get();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj==null)
                return false;
            if (obj == this)
                return true;
            if (obj instanceof Chain) {
                Chain<T> seq1 = this;
                Chain seq2 = (Chain) obj;
                if (seq1.size() != seq2.size()) {
                    return false;
                }
            }
            if(obj instanceof PersistentIndexed) {
                return equalToIteration((Iterable)obj);
            }
            return false;
        }
        @Override
        public NonEmptyChain<T> reverse() {
            return new Wrap(this::reverseIterator);
        }
        @Override
        public String toString(){

            Iterator<T> it = iterator();

            StringBuffer b = new StringBuffer("[" + it.next());
            while(it.hasNext()){
                b.append(", "+it.next());
            }
            b.append("]");
            return b.toString();

        }
        @Override
        public Iterator<T> iterator() {
            return new ChainIterator<T>(this);
        }

        @Override
        public <T2, R> Chain<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
            return wrap(Spouts.from(this).zip(fn,publisher));
        }

        @Override
        public Spliterator<T> spliterator() {
            return new IteratableSpliterator<>(this);
        }
    }

    public static <T> Chain<T> narrow(Chain<? extends T> broad) {
        return (Chain<T>)broad;
    }

    private final static EmptyChain EMPTY = new EmptyChain();

    public static <T> EmptyChain<T> empty(){
        return EMPTY;
    }

    public static <T> NonEmptyChain<T> of(T... values){
        return new Wrap<>(Arrays.asList(values));
    }
    public static <T> NonEmptyChain<T> singleton(T value){
        return new Singleton<T>(value);
    }
    public static <T> NonEmptyChain<T> append(NonEmptyChain<? extends T> left, NonEmptyChain<? extends T> right){
        return  new Append(left,right);
    }
    public static <T> NonEmptyChain<T> append(Chain<? extends T> left, NonEmptyChain<? extends T> right){
        return left.isEmpty() ?  (NonEmptyChain<T>)right : new Append((NonEmptyChain<T>)left,right);
    }
    public static <T> NonEmptyChain<T> append(NonEmptyChain<? extends T> left, Chain<? extends T> right){
        return right.isEmpty() ?  (NonEmptyChain<T>)left : new Append(left,(NonEmptyChain<T>)right);
    }
    public static <T> Chain<T> wrap(Iterable<T> it){

        Iterator<T> i = it.iterator();


        return  i.hasNext()  ? new Wrap(it) : empty();

    }
    public abstract Chain<T> concat(Chain<T> b);
    @Override
    public <R> Chain<R> unitStream(Stream<R> stream) {
        return wrap(ReactiveSeq.fromStream(stream));
    }

    public abstract boolean isEmpty();

    public abstract Iterator<T> iterator();

    @Override
    public <U, R> Chain<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        if(other instanceof ReactiveSeq && ! (other instanceof OneShotStreamX))
            return zip(ReactiveSeq.fromStream(other),zipper);
        else
            return zip(ReactiveSeq.fromStream(other).toList(),zipper);
    }

    @Override
    public <U> Chain<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {
        return zipWithStream(other,Tuple::tuple);
    }

    @Override
    public Chain<T> insertStreamAt(int pos, Stream<T> stream) {
        if(stream instanceof ReactiveSeq && ! (stream instanceof OneShotStreamX))
            return (Chain<T>)ImmutableList.super.insertStreamAt(pos,stream);
        else
            return insertAt(pos,ReactiveSeq.fromStream(stream).toList());
    }

    @Override
    public Chain<T> prependStream(Stream<? extends T> stream) {
        if(stream instanceof ReactiveSeq && ! (stream instanceof OneShotStreamX))
            return (Chain<T>)ImmutableList.super.prependStream(stream);
        else
            return (Chain<T>)prependAll(stream.collect(Collectors.toList()));
    }

    @Override
    public Chain<T> removeStream(Stream<? extends T> stream) {
        if(stream instanceof ReactiveSeq && ! (stream instanceof OneShotStreamX))
            return (Chain<T>)ImmutableList.super.removeStream(stream);
        else
            return (Chain<T>)ImmutableList.super.removeStream(ReactiveSeq.fromIterable(ReactiveSeq.fromStream(stream).toList()));
    }

    @Override
    public Chain<T> retainStream(Stream<? extends T> stream) {
        if(stream instanceof ReactiveSeq && ! (stream instanceof OneShotStreamX))
            return (Chain<T>)ImmutableList.super.retainStream(stream);
        else
            return (Chain<T>)ImmutableList.super.retainStream(ReactiveSeq.fromIterable(ReactiveSeq.fromStream(stream).toList()));
    }

    @Override
    public <R> Chain<R> unitIterable(Iterable<R> it) {
        return wrap(it);
    }

    @Override
    public EmptyChain<T> emptyUnit() {
        return empty();
    }

    @Override
    public Chain<T> drop(long num) {
        return wrap(ReactiveSeq.fromIterable(this).drop(num));
    }

    @Override
    public Chain<T> take(long num) {
        return wrap(ReactiveSeq.fromIterable(this).take(num));
    }

    @Override
    public NonEmptyChain<T> prepend(T value) {
        return append(singleton(value),this);
    }
    public abstract Chain<T> prependAll(Iterable< ? extends T> value);

    @Override
    public NonEmptyChain<T> append(T value) {
        return append(this,singleton(value));
    }

    @Override
    public abstract  Chain<T> appendAll(Iterable<? extends T> value);

    @Override
    public Chain<T> reverse() {
        return wrap(this::reverseIterator);
    }

    @Override
    public abstract Option<T> get(int pos);

    @Override
    public abstract T getOrElse(int pos, T alt);

    @Override
    public abstract T getOrElseGet(int pos, Supplier<? extends T> alt);

    @Override
    public abstract int size();


    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(this);
    }

    @Override
    public Chain<T> filter(Predicate<? super T> fn) {
        return wrap(stream().filter(fn));
    }

    @Override
    public abstract <R> Chain<R> map(Function<? super T, ? extends R> fn);

    @Override
    public <R> Chain<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
        return wrap(ReactiveSeq.fromIterable(this).concatMap(fn));
    }

    @Override
    public abstract  <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2);
    @Override
    public abstract  NonEmptyChain<T> onEmpty(T value);

    @Override
    public abstract NonEmptyChain<T> onEmptyGet(Supplier<? extends T> supplier) ;

    @Override
    public Chain<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier){
        return isEmpty() ? wrap(supplier.get()) : this;
    }

    @Override
    public <R> Chain<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return wrap(ReactiveSeq.fromIterable(this).concatMap(mapper));
    }

    @Override
    public <R> Chain<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return wrap(ReactiveSeq.fromIterable(this).mergeMap(fn));
    }

    @Override
    public <R> Chain<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
        return wrap(ReactiveSeq.fromIterable(this).mergeMap(maxConcurecy,fn));
    }




    @Override
    public Chain<T> replaceFirst(T currentElement, T newElement) {
        return (Chain<T>)ImmutableList.super.replaceFirst(currentElement,newElement);
    }

    @Override
    public Chain<T> removeFirst(Predicate<? super T> pred) {
        return (Chain<T>)ImmutableList.super.removeFirst(pred);
    }

    @Override
    public Chain<T>subList(int start, int end) {
        return (Chain<T>)ImmutableList.super.subList(start,end);
    }



    @Override
    public <U> Chain<U> ofType(Class<? extends U> type) {
        return (Chain<U>)ImmutableList.super.ofType(type);
    }

    @Override
    public Chain<T> filterNot(Predicate<? super T> predicate) {
        return (Chain<T>)ImmutableList.super.filterNot(predicate);
    }

    @Override
    public Chain<T> notNull() {
        return (Chain<T>)ImmutableList.super.notNull();
    }

    @Override
    public Chain<T> peek(Consumer<? super T> c) {
        return (Chain<T>)ImmutableList.super.peek(c);
    }



    @Override
    public Chain<T> tailOrElse(ImmutableList<T> tail) {
        return (Chain<T>)ImmutableList.super.tailOrElse(tail);
    }

    @Override
    public <R1, R2, R3, R> Chain<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Chain<R>)ImmutableList.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
    }

    @Override
    public <R1, R2, R3, R> Chain<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Chain<R>)ImmutableList.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
    }

    @Override
    public <R1, R2, R> Chain<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (Chain<R>)ImmutableList.super.forEach3(iterable1,iterable2,yieldingFunction);
    }

    @Override
    public <R1, R2, R> Chain<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (Chain<R>)ImmutableList.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);
    }

    @Override
    public <R1, R> Chain<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Chain<R>)ImmutableList.super.forEach2(iterable1,yieldingFunction);
    }

    @Override
    public <R1, R> Chain<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Chain<R>)ImmutableList.super.forEach2(iterable1,filterFunction,yieldingFunction);
    }

    @Override
    public Chain<T> removeAt(long pos) {
        return (Chain<T>)ImmutableList.super.removeAt(pos);
    }

    @Override
    public Chain<T> removeAll(T... values) {
        return (Chain<T>)ImmutableList.super.removeAll(values);
    }

    @Override
    public Chain<T> retainAll(Iterable<? extends T> it) {
        return (Chain<T>)ImmutableList.super.retainAll(it);
    }

    @Override
    public Chain<T> retainAll(T... values) {
        return (Chain<T>)ImmutableList.super.retainAll(values);
    }

    @Override
    public Chain<ReactiveSeq<T>> permutations() {
        return (Chain<ReactiveSeq<T>>)ImmutableList.super.permutations();
    }

    @Override
    public Chain<ReactiveSeq<T>> combinations(int size) {
        return (Chain<ReactiveSeq<T>>)ImmutableList.super.combinations(size);
    }

    @Override
    public Chain<ReactiveSeq<T>> combinations() {
        return (Chain<ReactiveSeq<T>>)ImmutableList.super.combinations();
    }

    @Override
    public <T2, R> Chain<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return (Chain<R>)ImmutableList.super.zip(fn,publisher);
    }

    @Override
    public <U> Chain<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (Chain)ImmutableList.super.zipWithPublisher(other);
    }

    @Override
    public <U> Chain<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (Chain)ImmutableList.super.zip(other);
    }

    @Override
    public <S, U, R> Chain<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (Chain<R>)ImmutableList.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> Chain<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Chain<R>)ImmutableList.super.zip4(second,third,fourth,fn);
    }

    @Override
    public Chain<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (Chain<T>)ImmutableList.super.combine(predicate,op);
    }

    @Override
    public Chain<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (Chain<T>)ImmutableList.super.combine(op,predicate);
    }

    @Override
    public Chain<T> cycle(long times) {
        return (Chain<T>)ImmutableList.super.cycle(times);
    }

    @Override
    public Chain<T> cycle(Monoid<T> m, long times) {
        return (Chain<T>)ImmutableList.super.cycle(m,times);
    }

    @Override
    public Chain<T> cycleWhile(Predicate<? super T> predicate) {
        return (Chain<T>)ImmutableList.super.cycleWhile(predicate);
    }

    @Override
    public Chain<T> cycleUntil(Predicate<? super T> predicate) {
        return (Chain<T>)ImmutableList.super.cycleUntil(predicate);
    }

    @Override
    public <U, R> Chain<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (Chain<R>)ImmutableList.super.zip(other,zipper);
    }

    @Override
    public <S, U> Chain<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (Chain)ImmutableList.super.zip3(second,third);
    }

    @Override
    public <T2, T3, T4> Chain<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (Chain)ImmutableList.super.zip4(second,third,fourth);
    }

    @Override
    public Chain<Tuple2<T, Long>> zipWithIndex() {
        return (Chain<Tuple2<T, Long>>)ImmutableList.super.zipWithIndex();
    }

    @Override
    public Chain<Seq<T>> sliding(int windowSize) {
        return (Chain<Seq<T>>)ImmutableList.super.sliding(windowSize);
    }

    @Override
    public Chain<Seq<T>> sliding(int windowSize, int increment) {
        return (Chain<Seq<T>>)ImmutableList.super.sliding(windowSize, increment);
    }

    @Override
    public <C extends PersistentCollection<? super T>> Chain<C> grouped(int size, Supplier<C> supplier) {
        return (Chain<C>)ImmutableList.super.grouped(size,supplier);
    }

    @Override
    public Chain<Vector<T>> groupedUntil(Predicate<? super T> predicate) {
        return (Chain<Vector<T>>)ImmutableList.super.groupedUntil(predicate);
    }

    @Override
    public Chain<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return (Chain<Vector<T>>)ImmutableList.super.groupedUntil(predicate);
    }

    @Override
    public Chain<Vector<T>> groupedWhile(Predicate<? super T> predicate) {
        return (Chain<Vector<T>>)ImmutableList.super.groupedWhile(predicate);
    }

    @Override
    public <C extends PersistentCollection<? super T>> Chain<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (Chain<C>)ImmutableList.super.groupedWhile(predicate,factory);
    }

    @Override
    public <C extends PersistentCollection<? super T>> Chain<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (Chain<C>)ImmutableList.super.groupedUntil(predicate,factory);
    }

    @Override
    public Chain<Vector<T>> grouped(int groupSize) {
        return (Chain<Vector<T>>)ImmutableList.super.grouped(groupSize);
    }

    @Override
    public Chain<T> distinct() {
        return (Chain<T>)ImmutableList.super.distinct();
    }

    @Override
    public Chain<T> scanLeft(Monoid<T> monoid) {
        return (Chain<T>)ImmutableList.super.scanLeft(monoid);
    }

    @Override
    public <U> Chain<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (Chain<U>)ImmutableList.super.scanLeft(seed,function);
    }

    @Override
    public Chain<T> scanRight(Monoid<T> monoid) {
        return (Chain<T>)ImmutableList.super.scanRight(monoid);
    }

    @Override
    public <U> Chain<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (Chain<U>)ImmutableList.super.scanRight(identity,combiner);
    }

    @Override
    public Chain<T> sorted() {
        return (Chain<T>)ImmutableList.super.sorted();
    }

    @Override
    public Chain<T> sorted(Comparator<? super T> c) {
        return (Chain<T>)ImmutableList.super.sorted(c);
    }

    @Override
    public Chain<T> takeWhile(Predicate<? super T> p) {
        return (Chain<T>)ImmutableList.super.takeWhile(p);
    }

    @Override
    public Chain<T> dropWhile(Predicate<? super T> p) {
        return (Chain<T>)ImmutableList.super.dropWhile(p);
    }

    @Override
    public Chain<T> takeUntil(Predicate<? super T> p) {
        return (Chain<T>)ImmutableList.super.takeUntil(p);
    }

    @Override
    public Chain<T> dropUntil(Predicate<? super T> p) {
        return (Chain<T>)ImmutableList.super.dropUntil(p);
    }

    @Override
    public Chain<T> dropRight(int num) {
        return (Chain<T>)ImmutableList.super.dropRight(num);
    }

    @Override
    public Chain<T> takeRight(int num) {
        return (Chain<T>)ImmutableList.super.takeRight(num);
    }

    @Override
    public Chain<T> intersperse(T value) {
        return (Chain<T>)ImmutableList.super.intersperse(value);
    }

    @Override
    public Chain<T> shuffle() {
        return (Chain<T>)ImmutableList.super.shuffle();
    }

    @Override
    public Chain<T> shuffle(Random random) {
        return (Chain<T>)ImmutableList.super.shuffle(random);
    }

    @Override
    public Chain<T> slice(long from, long to) {
        return (Chain<T>)ImmutableList.super.slice(from,to);
    }

    @Override
    public <U extends Comparable<? super U>> Chain<T> sorted(Function<? super T, ? extends U> function) {
        return (Chain<T>)ImmutableList.super.sorted(function);
    }



    @Override
    public NonEmptyChain<T> appendAll(T... values) {
        return (NonEmptyChain<T>)ImmutableList.super.appendAll(values);
    }

    @Override
    public NonEmptyChain<T> prependAll(T... values) {
        return (NonEmptyChain<T>)ImmutableList.super.prependAll(values);
    }

    @Override
    public Chain<T> insertAt(int pos, T... values) {
        return (Chain<T>)ImmutableList.super.insertAt(pos,values);
    }

    @Override
    public Chain<T> deleteBetween(int start, int end) {
        return (Chain<T>)ImmutableList.super.deleteBetween(start,end);
    }

    @Override
    public Chain<T> plusAll(Iterable<? extends T> list) {
        return appendAll(list);
    }

    @Override
    public NonEmptyChain<T> plus(T value) {
        return append(value);
    }

    @Override
    public Chain<T> removeValue(T value) {
        return (Chain<T>)ImmutableList.super.removeValue(value);
    }

    @Override
    public Chain<T> removeAll(Iterable<? extends T> value) {
        return (Chain<T>)ImmutableList.super.removeAll(value);
    }

    @Override
    public Chain<T> updateAt(int pos, T value) {
        return (Chain<T>)ImmutableList.super.updateAt(pos,value);
    }

    @Override
    public Chain<T> insertAt(int pos, Iterable<? extends T> values) {
        return (Chain<T>)ImmutableList.super.insertAt(pos,values);
    }

    @Override
    public Chain<T> insertAt(int i, T value) {
        return (Chain<T>)ImmutableList.super.insertAt(i,value);
    }



    public abstract  Iterator<T> reverseIterator();
    private static final class EmptyChain<T> extends Chain<T> implements ImmutableList.None<T>{

        @Override
        public Chain<T> concat(Chain<T> b) {
            return b.isEmpty() ? this : b;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
        @Override
        public Iterator<T> reverseIterator() {
            return iterator();
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new  NoSuchElementException();
                }
            };
        }

        @Override
        public Chain<T> appendAll(Iterable<? extends T> value) {
            Chain<? extends T> w = wrap(value);
            return w.isEmpty() ? this : narrow(w);
        }

        @Override
        public Option<T> get(int pos) {
            return Option.none();
        }

        @Override
        public T getOrElse(int pos, T alt) {
            return alt;
        }

        @Override
        public T getOrElseGet(int pos, Supplier<? extends T> alt) {
            return alt.get();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public <R> EmptyChain<R> map(Function<? super T, ? extends R> fn) {
            return EMPTY;
        }

        @Override
        public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn2.apply(this);
        }

        @Override
        public NonEmptyChain<T> onEmpty(T value) {
            return Chain.singleton(value);
        }

        @Override
        public NonEmptyChain<T> onEmptyGet(Supplier<? extends T> supplier) {
            return Chain.singleton(supplier.get());
        }



        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj==null)
                return false;
            if (obj == this)
                return true;

            if(obj instanceof PersistentList) {
                return ((PersistentList)obj).size()==0;
            }
            return false;
        }

        public String toString(){
            return "[]";
        }
        @Override
        public Chain<T> prependAll(Iterable< ? extends T> value) {

            Chain<T> t = narrow(wrap(value));
            return t.isEmpty() ? this : t;

        }

    }

    private static final class Singleton<T> extends NonEmptyChain<T> {
        public Singleton(T value) {
            this.value = value;
        }

        private final T value;

        @Override
        public NonEmptyChain<T> concat(Chain<T> b) {
            return b.isEmpty() ?  this : new Append(this,(NonEmptyChain<T>)b) ;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }


        @Override
        public NonEmptyChain<T> appendAll(Iterable<? extends T> values) {
            Chain<? extends T> w = wrap(values);
            return w.isEmpty() ? this : append(this,w);
        }
        public  <R> NonEmptyChain<R> map(Function<? super T, ? extends R> fn){
            return Chain.singleton(fn.apply(value));
        }

        @Override
        public Iterator<T> reverseIterator() {
            return iterator();
        }

        @Override
        public Option<T> get(int pos) {
            return pos == 0 ? Option.some(value) :  Option.none();
        }

        @Override
        public T getOrElse(int pos, T alt) {
            return pos == 0  ? value : alt;
        }

        @Override
        public T getOrElseGet(int pos, Supplier<? extends T> alt) {
            return pos == 0  ? value : alt.get();
        }

        @Override
        public int size() {
            return 1;
        }


        @Override
        public EmptyChain<T> tail() {
            return Chain.EMPTY;
        }

        @Override
        public T head() {
            return value;
        }

        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head(),tail());
        }
    }
    @AllArgsConstructor
    private static final class Append<T> extends NonEmptyChain<T>{
        private final NonEmptyChain<T> left;
        private final NonEmptyChain<T> right;

        @Override
        public NonEmptyChain<T> concat(Chain<T> b) {
            return b.isEmpty() ? this : new Append(this,(NonEmptyChain<T>)b);
        }



        @Override
        public boolean isEmpty() {
            return false;
        }
        @Override
        public Iterator<T> iterator() {
          return new ChainIterator<T>(this);
        }

        @Override
        public Option<T> get(int pos) {
            return LazySeq.fromIterable(this).get(pos);
        }

        @Override
        public T getOrElse(int pos, T alt) {
            return LazySeq.fromIterable(this).getOrElse(pos,alt);
        }

        @Override
        public T getOrElseGet(int pos, Supplier<? extends T> alt) {
            return LazySeq.fromIterable(this).getOrElseGet(pos,alt);
        }

        @Override
        public int size() {
            return ReactiveSeq.fromIterable(this).size();
        }

        @Override
        public Iterator<T> reverseIterator() {
            return new Iterator<T>() {
                Iterator<T> active = right.reverseIterator();
                boolean first = true;
                @Override
                public boolean hasNext() {
                    boolean res =  active.hasNext();
                    if(!res && first){
                        first = false;
                        active = left.reverseIterator();
                        res  = active.hasNext();
                    }
                    return res;

                }

                @Override
                public T next() {
                    return active.next();
                }
            };
        }

        @Override
        public ImmutableList<T> tail() {
            return drop(1);
        }

        @Override
        public T head() {
            return getOrElse(0,null);
        }

        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head(),tail());
        }
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Wrap<T> extends NonEmptyChain<T>{
        private final Iterable<T> it;

        @Override
        public NonEmptyChain<T> concat(Chain<T> b) {
            return b.isEmpty() ? this : new Append(this,(NonEmptyChain<T>)b) ;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }


        @Override
        public Option<T> get(int pos) {
            return LazySeq.fromIterable(it).get(pos);
        }

        @Override
        public T getOrElse(int pos, T alt) {
            return LazySeq.fromIterable(it).getOrElse(pos,alt);
        }

        @Override
        public T getOrElseGet(int pos, Supplier<? extends T> alt) {
            return LazySeq.fromIterable(it).getOrElseGet(pos,alt);
        }

        @Override
        public int size() {
            return ReactiveSeq.fromIterable(it).size();
        }

        @Override
        public Iterator<T> reverseIterator() {
            return LazySeq.fromIterable(it).reverse().iterator();
        }
        @Override
        public ImmutableList<T> tail() {
            return drop(1);
        }

        @Override
        public T head() {
            return getOrElse(0,null);
        }

        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head(),tail());
        }

        @Override
        public Iterator<T> iterator(){
            return it.iterator();
        }

        @Override
        public Spliterator<T> spliterator() {
            return new IteratableSpliterator<>(it);
        }
    }

     private static class ChainIterator<T> implements Iterator<T>{

            Vector<NonEmptyChain<T>> rights = Vector.empty();
            Iterator<T> current = null;
             Chain<T> c;

            public ChainIterator( Chain<T> chain ){
                this.c = chain.isEmpty() ? null : chain;
            }




            public boolean hasNext() {
                return (c != null) || (current != null && current.hasNext());
            }

            @Override
            public T next() {
                Supplier<T> go = ()-> {
                    boolean loop = true;

                    while(loop) {
                        loop = false;
                        if (current != null && current.hasNext()) {
                            return current.next();
                        }
                        current = null;
                        if (c instanceof Singleton) {
                            T value = ((Singleton<T>) c).value;
                            c = rights.foldLeft((a, b) -> append(b, a))
                                .orElse(null);
                            rights = Vector.empty();
                            return value;
                        }
                        if (c instanceof Append) {
                            rights = rights.plus(((Append<T>) c).right);
                            c = ((Append<T>) c).left;
                            loop = true;
                        }
                        if(c instanceof Wrap){
                            current = ((Wrap<T>)c).it.iterator();
                            c = rights.foldLeft((a, b) -> append(b, a))
                                .orElse(null);
                            rights = Vector.empty();

                            return current.next();

                        }
                        if(c instanceof EmptyChain || c == null){
                            throw new NoSuchElementException();
                        }
                    }
                    return null; //unreachable
                };
                return go.get();

            }

    }
}
