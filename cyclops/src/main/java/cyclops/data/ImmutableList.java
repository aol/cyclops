package cyclops.data;

import com.oath.cyclops.types.foldable.Contains;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentList;
import com.oath.cyclops.matching.Deconstruct.Deconstruct2;
import com.oath.cyclops.matching.Sealed2;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.recoverable.OnEmptyError;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.types.traversable.Traversable;

import cyclops.control.Option;
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
import java.util.function.*;
import java.util.stream.Stream;



public interface ImmutableList<T> extends Sealed2<ImmutableList.Some<T>,ImmutableList.None<T>>,
                                          IterableX<T>,
                                          Contains<T>,
                                          Comparable<T>,
                                          PersistentList<T>,
                                          OnEmptySwitch<ImmutableList<T>,ImmutableList<T>>,
                                            OnEmptyError<T, ImmutableList<T>>,
                                          To<ImmutableList<T>> {

    <R> ImmutableList<R> unitStream(Stream<R> stream);
    <R> ImmutableList<R> unitIterable(Iterable<R> it);


    ImmutableList<T> emptyUnit();
    @Override
    default int compareTo(final T o) {
        if (o instanceof ImmutableList) {
            final ImmutableList l = (ImmutableList) o;
            if (this.size() == l.size()) {
                final Iterator i1 = iterator();
                final Iterator i2 = l.iterator();
                if (i1.hasNext()) {
                    if (i2.hasNext()) {
                        final int comp = Comparator.<Comparable> naturalOrder()
                            .compare((Comparable) i1.next(), (Comparable) i2.next());
                        if (comp != 0)
                            return comp;
                    }
                    return 1;
                } else {
                    if (i2.hasNext())
                        return -1;
                    else
                        return 0;
                }
            }
            return this.size() - ((ImmutableList) o).size();
        } else
            return 1;

    }

    @Deprecated//internal method (to be hidden in future release)
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



    default ImmutableList<T> replaceFirst(T currentElement, T newElement){
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


    default ImmutableList<T> subList(int start, int end){
        if(start>0)
            return drop(start).take(end-start);
        return take(end);
    }
    default LazySeq<T> lazySeq(){
        if(this instanceof LazySeq){
            return (LazySeq<T>)this;
        }
        return LazySeq.fromIterable(this);

    }
    default Seq<T> seq(){
        if(this instanceof Seq){
            return (Seq<T>)this;
        }
        Seq<T> s = Seq.empty();
        for(T next :  reverse()){
            s = s.plus(next);
        }
        return s;

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





    <R> ImmutableList<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn);



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

            private T head(Some<T> some){
                current = some.tail();
                return some.head();
            }
            @Override
            public T next() {
                return current.fold(list-> head(list), nil->null);
            }
        };
    }

    default <R>  R fold(BiFunction<? super T, ? super ImmutableList<T>, ? extends R> fn, Supplier<? extends R> alt){
        return fold(s->fn.apply(s.head(),s.tail()),n->alt.get());
    }

    @Override
    ImmutableList<T> onEmpty(T value);

    @Override
    ImmutableList<T> onEmptyGet(Supplier<? extends T> supplier);

    @Override
    default <X extends Throwable> Try<ImmutableList<T>, X> onEmptyTry(Supplier<? extends X> supplier){
        return isEmpty() ? Try.failure(supplier.get()) : Try.success(this);
    }

    default T last(T alt){
        return getOrElse(size()-1,alt);
    }
    @Override
    ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier);
    default Tuple2<ImmutableList<T>, ImmutableList<T>> partition(final Predicate<? super T> splitter) {

        return Tuple.tuple(filter(splitter), filter(splitter.negate()));

    }
    default Tuple2<ImmutableList<T>, ImmutableList<T>> span(Predicate<? super T> pred) {
        return Tuple.tuple(takeWhile(pred), dropWhile(pred));
    }

    default Tuple2<ImmutableList<T>,ImmutableList<T>> splitBy(Predicate<? super T> test) {
        return span(test.negate());
    }

    default ImmutableList<T> tailOrElse(ImmutableList<T> tail){
        return fold(s->s.tail(),nil->tail);
    }

    static interface Some<T> extends Deconstruct2<T,ImmutableList<T>>, ImmutableList<T> {
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

        return this.concatMap(in -> {

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

        return this.concatMap(in -> {

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

        return this.concatMap(in -> {

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

        return this.concatMap(in -> {

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

        return this.concatMap(in-> {

            Iterable<? extends R1> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }


    default <R1, R> ImmutableList<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1,
                                              BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                              BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return this.concatMap(in-> {

            Iterable<? extends R1> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                    .filter(in2-> filterFunction.apply(in,in2))
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }

    @Override
    default ImmutableList<T> removeStream(Stream<? extends T> stream) {
        return unitStream(stream().removeStream(stream));
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
    default ImmutableList<T> retainAll(Iterable<? extends T> it) {
        return unitStream(stream().retainAll(it));
    }

    @Override
    default ImmutableList<T> retainStream(Stream<? extends T> stream) {
        return unitStream(stream().retainStream(stream));
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
    default <T2, R> ImmutableList<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return unitStream(stream().zip(fn, publisher));
    }

    default <U, R> ImmutableList<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return unitStream(stream().zipWithStream(other,zipper));
    }

    @Override
    default <U> ImmutableList<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return unitStream(stream().zipWithPublisher(other));
    }

    @Override
    default <U> ImmutableList<Tuple2<T, U>> zip(Iterable<? extends U> other) {
       return zip(other,Tuple::tuple);

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
        return zipWithStream(ReactiveSeq.rangeLong(0,Long.MAX_VALUE));
       // return unitStream(stream().zipWithIndex());
    }

    @Override
    default ImmutableList<Seq<T>> sliding(int windowSize) {
        return unitStream(stream().sliding(windowSize));
    }

    @Override
    default ImmutableList<Seq<T>> sliding(int windowSize, int increment) {
        return unitStream(stream().sliding(windowSize,increment));
    }

    @Override
    default <C extends PersistentCollection<? super T>> ImmutableList<C> grouped(int size, Supplier<C> supplier) {
        return unitStream(stream().grouped(size,supplier));
    }

    @Override
    default ImmutableList<Vector<T>> groupedUntil(Predicate<? super T> predicate) {
        return unitStream(stream().groupedUntil(predicate));
    }

    @Override
    default ImmutableList<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return unitStream(stream().groupedUntil(predicate));
    }

    default <U> ImmutableList<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {
        return zip(ReactiveSeq.fromStream(other));

    }

    @Override
    default ImmutableList<Vector<T>> groupedWhile(Predicate<? super T> predicate) {
        return unitStream(stream().groupedWhile(predicate));
    }

    @Override
    default <C extends PersistentCollection<? super T>> ImmutableList<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return unitStream(stream().groupedWhile(predicate,factory));
    }

    @Override
    default <C extends PersistentCollection<? super T>> ImmutableList<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return unitStream(stream().groupedUntil(predicate,factory));
    }

    @Override
    default ImmutableList<Vector<T>> grouped(int groupSize) {
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
        if(num>size())
            return emptyUnit();
        return unitStream(stream().dropRight(num));
    }

    @Override
    default ImmutableList<T> takeRight(int num) {
        return unitStream(stream().takeRight(num));
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
    <R> ImmutableList<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

    @Override
    <R> ImmutableList<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn);

    @Override
    <R> ImmutableList<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn);

  @Override
    default ImmutableList<T> prependStream(Stream<? extends T> stream) {
        return unitStream(stream().prependStream(stream));
    }

    @Override
    default ImmutableList<T> appendAll(T... values) {
        ImmutableList<T> res = this;
        for(T t : values){
            res = res.append(t);
        }
        return res;
    }

    @Override
    default ImmutableList<T> prependAll(T... values) {
        ImmutableList<T> res = this;
        for(int i=values.length-1;i>=0;i--){
            res = res.prepend(values[i]);
        }

        return res;
    }

    @Override
    default ImmutableList<T> insertAt(int pos, T... values) {
        if(pos==0)
            return prependAll(values);
        if(pos>=size())
            return appendAll(values);
        return unitIterable(IterableX.super.insertAt(pos,values));
    }

    @Override
    default ImmutableList<T> deleteBetween(int start, int end) {
        return unitStream(stream().deleteBetween(start,end));
    }

    @Override
    default ImmutableList<T> insertStreamAt(int pos, Stream<T> stream) {
        return unitStream(stream().insertStreamAt(pos,stream));
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
        return removeFirst(e->Objects.equals(e,value));
    }


    @Override
    default ImmutableList<T> removeAll(Iterable<? extends T> value) {
        return unitIterable(IterableX.super.removeAll(value));
    }

    default ImmutableList<T> prependAll(Iterable<? extends T> value) {
        return unitIterable(IterableX.super.prependAll(value));
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
