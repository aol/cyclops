package cyclops.data;


import com.oath.cyclops.types.foldable.Folds;
import com.oath.cyclops.types.functor.Transformable;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentIndexed;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DifferenceList<T> implements Folds<T>,
                                          Transformable<T>,
                                          ImmutableList<T>{


    private final Function<ImmutableList<T>,Trampoline<ImmutableList<T>>> appending;

    public static <T> DifferenceList<T> of(LazySeq<T> list){
        return new DifferenceList<>(l-> Trampoline.done(list.appendAll(l)));
    }
    public static <T> DifferenceList<T> fromIterable(Iterable<T> it){
        if(it instanceof DifferenceList)
            return (DifferenceList<T>)it;
        return of(LazySeq.fromIterable(it));
    }
    public static <T> DifferenceList<T> fromStream(Stream<T> stream){
        return of(LazySeq.fromStream(stream));
    }
    public static <T> DifferenceList<T> of(T... values){
        return  of(LazySeq.of(values));
    }
    public static <T> DifferenceList<T> empty(){
        return new DifferenceList<>(l-> Trampoline.done(l));
    }
    public static <U, T> DifferenceList<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed,unfolder));
    }
    public static <T> DifferenceList<T> generate(Supplier<T> s){
        return fromStream(ReactiveSeq.generate(s));
    }
    public static <T> DifferenceList<T> generate(Supplier<T> s,long times){
        return fromStream(ReactiveSeq.generate(s).limit(times));
    }

    public static <T> DifferenceList<T> iterate(final T seed, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,f));

    }
    public static <T> DifferenceList<T> iterate(final T seed, final UnaryOperator<T> f,long times) {
        return fromStream(ReactiveSeq.iterate(seed,f).limit(times));

    }
    public static <T> DifferenceList<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,pred,f));

    }
    public static DifferenceList<Integer> range(final int start, final int end) {
        return of(LazySeq.range(start,end));

    }
    public static DifferenceList<Integer> range(final int start, final int step, final int end) {
        return of(LazySeq.range(start,step,end));

    }
    public static DifferenceList<Long> rangeLong(final long start, final long step, final long end) {
        return of(LazySeq.rangeLong(start,step,end));
    }

    public static DifferenceList<Long> rangeLong(final long start, final long end) {
        return of(LazySeq.rangeLong(start,end));
    }


    public <R> DifferenceList<R> map(Function<? super T, ? extends R> fn){
        return new DifferenceList<>(l-> Trampoline.done(run().map(fn)));
    }

    @Override
    public DifferenceList<T> peek(Consumer<? super T> c) {
        return (DifferenceList<T>)ImmutableList.super.peek(c);
    }



    public <R> DifferenceList<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn){
        return new DifferenceList<>(l-> Trampoline.done(run().flatMap(fn.andThen(DifferenceList::resolve))));
    }



    private static <T> ImmutableList<T> resolve(ImmutableList<T> immutableList){
        if(immutableList instanceof DifferenceList){
            return ((DifferenceList<T>) immutableList).run();
        }
        return immutableList;
    }
    public ImmutableList<T> run(){
        return  appending.apply(LazySeq.empty()).result();
    }

    public DifferenceList<T> prepend(DifferenceList<T> prepend) {
        return prepend.append(this);
    }
    public DifferenceList<T> append(DifferenceList<T> append) {
        Function<ImmutableList<T>, Trampoline<ImmutableList<T>>> appending2 = append.appending;
      return new DifferenceList<T>(l-> appending2.apply(l).flatMap(l2->Trampoline.more(()->appending.apply(l2))));
    }
    public DifferenceList<T> append(T append) {

        return append(DifferenceList.of(append));
    }
    public DifferenceList<T> prepend(T prepend) {
        return of(run().lazySeq().prepend(prepend));
    }

    public DifferenceList<T> memoize(){
        return new DifferenceList<>(Memoize.memoizeFunction(this.appending));
    }

    @Override
    public <R> DifferenceList<R> unitStream(Stream<R> stream) {
        return fromStream(stream);
    }

    @Override
    public <R> DifferenceList<R> unitIterable(Iterable<R> it) {
        return fromIterable(it);
    }

    @Override
    public DifferenceList<T> emptyUnit() {
        return empty();
    }


    @Override
    public DifferenceList<T> replaceFirst(T currentElement, T newElement) {
        return of(LazySeq.fromIterable(ImmutableList.super.replaceFirst(currentElement,newElement)));
    }

    @Override
    public DifferenceList<T> removeFirst(Predicate<? super T> pred) {
        return (DifferenceList<T>)ImmutableList.super.removeFirst(pred);
    }

    @Override
    public DifferenceList<T> subList(int start, int end) {
        return (DifferenceList<T>)ImmutableList.super.subList(start,end);
    }


    @Override
    public DifferenceList<T> drop(long num) {
        return fromStream(stream().drop(num));
    }

    @Override
    public DifferenceList<T> take(long num) {
        return fromStream(stream().take(num));
    }




    @Override
    public DifferenceList<T> appendAll(Iterable<? extends T> value) {
        DifferenceList<T> res = this;
        for(T next : value){
            res = res.append(next);
        }
        return res;
    }

    @Override
    public DifferenceList<T> reverse() {
        return fromStream(stream().reverse());
    }

    @Override
    public Option<T> get(int pos) {
        return run().get(pos);
    }

    @Override
    public T getOrElse(int pos, T alt) {
        return run().getOrElse(pos,alt);
    }

    @Override
    public T getOrElseGet(int pos, Supplier<? extends T> alt) {
        return run().getOrElseGet(pos,alt);
    }


    @Override
    public int size() {
        return run().size();
    }

    @Override
    public boolean isEmpty() {
        return run().isEmpty();
    }


    @Override
    public <U> DifferenceList<U> ofType(Class<? extends U> type) {
        return (DifferenceList<U>)ImmutableList.super.ofType(type);
    }

    @Override
    public DifferenceList<T> filterNot(Predicate<? super T> predicate) {
        return (DifferenceList<T>)ImmutableList.super.filterNot(predicate);
    }

    @Override
    public DifferenceList<T> notNull() {
        return (DifferenceList<T>)ImmutableList.super.notNull();
    }

    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(run());
    }

    @Override
    public DifferenceList<T> filter(Predicate<? super T> fn) {
        return new DifferenceList<>(l-> Trampoline.done(run().filter(fn)));
    }

    @Override
    public Iterator<T> iterator() {
        return run().iterator();
    }

    @Override
    public <R> R fold(BiFunction<? super T, ? super ImmutableList<T>, ? extends R> fn, Supplier<? extends R> alt) {
        return run().fold(fn,alt);
    }

    @Override
    public DifferenceList<T> onEmpty(T value) {
        return fold(s->this,__->of(value));
    }

    @Override
    public DifferenceList<T> onEmptyGet(Supplier<? extends T> supplier) {
        return fold(s->this,__->of(supplier.get()));
    }

    @Override
    public ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
        return null;
    }


    @Override
    public ImmutableList<T> tailOrElse(ImmutableList<T> tail) {
        return fold(s->this,__->tail);
    }

    @Override
    public <R1, R2, R3, R> DifferenceList<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (DifferenceList<R>)ImmutableList.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
    }

    @Override
    public <R1, R2, R3, R> DifferenceList<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (DifferenceList<R>)ImmutableList.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
    }

    @Override
    public <R1, R2, R> DifferenceList<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (DifferenceList<R>)ImmutableList.super.forEach3(iterable1,iterable2,yieldingFunction);

    }

    @Override
    public <R1, R2, R> DifferenceList<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (DifferenceList<R>)ImmutableList.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);

    }

    @Override
    public <R1, R> DifferenceList<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (DifferenceList<R>)ImmutableList.super.forEach2(iterable1,yieldingFunction);
    }

    @Override
    public <R1, R> DifferenceList<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (DifferenceList<R>)ImmutableList.super.forEach2(iterable1,filterFunction,yieldingFunction);
    }

    @Override
    public DifferenceList<T> removeStream(Stream<? extends T> stream) {
        return (DifferenceList<T>)ImmutableList.super.removeStream(stream);
    }

    @Override
    public DifferenceList<T> removeAt(long pos) {
        return (DifferenceList<T>)ImmutableList.super.removeAt(pos);
    }

    @Override
    public DifferenceList<T> removeAll(T... values) {
        return (DifferenceList<T>)ImmutableList.super.removeAll(values);
    }

    @Override
    public DifferenceList<T> retainAll(Iterable<? extends T> it) {
        return (DifferenceList<T>)ImmutableList.super.retainAll(it);
    }

    @Override
    public DifferenceList<T> retainStream(Stream<? extends T> stream) {
        return (DifferenceList<T>)ImmutableList.super.retainStream(stream);
    }

    @Override
    public DifferenceList<T> retainAll(T... values) {
        return (DifferenceList<T>)ImmutableList.super.retainAll(values);
    }

    @Override
    public DifferenceList<ReactiveSeq<T>> permutations() {
        return (DifferenceList<ReactiveSeq<T>>)ImmutableList.super.permutations();
    }

    @Override
    public DifferenceList<ReactiveSeq<T>> combinations(int size) {
        return (DifferenceList<ReactiveSeq<T>>)ImmutableList.super.combinations(size);
    }

    @Override
    public DifferenceList<ReactiveSeq<T>> combinations() {
        return (DifferenceList<ReactiveSeq<T>>)ImmutableList.super.combinations();
    }

    @Override
    public <T2, R> DifferenceList<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return (DifferenceList<R>)ImmutableList.super.zip(fn,publisher);
    }

    @Override
    public <U, R> DifferenceList<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (DifferenceList<R>)ImmutableList.super.zipWithStream(other,zipper);
    }

    @Override
    public <U> DifferenceList<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        ImmutableList<? extends Tuple2<T, ? extends U>> x = ImmutableList.super.zipWithPublisher(other);
        return (DifferenceList<Tuple2<T, U>>)x;
    }

    @Override
    public <U> DifferenceList<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        ImmutableList<? extends Tuple2<T, ? extends U>> x = ImmutableList.super.zip(other);
        return (DifferenceList<Tuple2<T,U>>) x;
    }

    @Override
    public <S, U, R> DifferenceList<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (DifferenceList<R>)ImmutableList.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> DifferenceList<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (DifferenceList<R>)ImmutableList.super.zip4(second,third,fourth,fn);
    }

    @Override
    public DifferenceList<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (DifferenceList<T>)ImmutableList.super.combine(predicate,op);
    }

    @Override
    public DifferenceList<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (DifferenceList<T>)ImmutableList.super.combine(op,predicate);
    }

    @Override
    public DifferenceList<T> cycle(long times) {
        return (DifferenceList<T>)ImmutableList.super.cycle(times);
    }

    @Override
    public DifferenceList<T> cycle(Monoid<T> m, long times) {
        return (DifferenceList<T>)ImmutableList.super.cycle(m,times);
    }

    @Override
    public DifferenceList<T> cycleWhile(Predicate<? super T> predicate) {
        return (DifferenceList<T>)ImmutableList.super.cycleWhile(predicate);
    }

    @Override
    public DifferenceList<T> cycleUntil(Predicate<? super T> predicate) {
        return (DifferenceList<T>)ImmutableList.super.cycleUntil(predicate);
    }

    @Override
    public <U, R> DifferenceList<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (DifferenceList<R>)ImmutableList.super.zip(other,zipper);
    }

    @Override
    public <S, U> DifferenceList<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        ImmutableList<? extends Tuple3<T, ? extends S, ? extends U>> x = ImmutableList.super.zip3(second, third);
        return (DifferenceList<Tuple3<T, S, U>>)x;
    }

    @Override
    public <T2, T3, T4> DifferenceList<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        ImmutableList<? extends Tuple4<T, ? extends T2, ? extends T3, ? extends T4>> x = ImmutableList.super.zip4(second, third, fourth);
        return (DifferenceList<Tuple4<T, T2, T3, T4>>)x;
    }

    @Override
    public DifferenceList<Tuple2<T, Long>> zipWithIndex() {
        return (DifferenceList<Tuple2<T, Long>>)ImmutableList.super.zipWithIndex();
    }

    @Override
    public DifferenceList<Seq<T>> sliding(int windowSize) {
        return (DifferenceList<Seq<T>>)ImmutableList.super.sliding(windowSize);
    }

    @Override
    public DifferenceList<Seq<T>> sliding(int windowSize, int increment) {
        return (DifferenceList<Seq<T>>)ImmutableList.super.sliding(windowSize,increment);
    }

    @Override
    public <C extends PersistentCollection<? super T>> DifferenceList<C> grouped(int size, Supplier<C> supplier) {
        return (DifferenceList<C>)ImmutableList.super.grouped(size,supplier);
    }

    @Override
    public DifferenceList<Vector<T>> groupedUntil(Predicate<? super T> predicate) {
        return (DifferenceList<Vector<T>>)ImmutableList.super.groupedUntil(predicate);
    }

    @Override
    public DifferenceList<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return (DifferenceList<Vector<T>>)ImmutableList.super.groupedUntil(predicate);
    }

    @Override
    public <U> DifferenceList<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {
        ImmutableList<? extends Tuple2<T, ? extends U>> x = ImmutableList.super.zipWithStream(other);
        return (DifferenceList<Tuple2<T, U>>)x;
    }

    @Override
    public DifferenceList<Vector<T>> groupedWhile(Predicate<? super T> predicate) {
        return (DifferenceList<Vector<T>>)ImmutableList.super.groupedWhile(predicate);
    }

    @Override
    public <C extends PersistentCollection<? super T>> DifferenceList<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (DifferenceList<C>)ImmutableList.super.groupedWhile(predicate,factory);
    }

    @Override
    public <C extends PersistentCollection<? super T>> DifferenceList<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (DifferenceList<C>)ImmutableList.super.groupedUntil(predicate,factory);
    }

    @Override
    public DifferenceList<Vector<T>> grouped(int groupSize) {
        return (DifferenceList<Vector<T>>)ImmutableList.super.grouped(groupSize);
    }

    @Override
    public DifferenceList<T> distinct() {
        return (DifferenceList<T>)ImmutableList.super.distinct();
    }

    @Override
    public DifferenceList<T> scanLeft(Monoid<T> monoid) {
        return (DifferenceList<T>)ImmutableList.super.scanLeft(monoid);
    }

    @Override
    public <U> DifferenceList<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (DifferenceList<U>)ImmutableList.super.scanLeft(seed,function);
    }

    @Override
    public DifferenceList<T> scanRight(Monoid<T> monoid) {
        return (DifferenceList<T>)ImmutableList.super.scanRight(monoid);
    }

    @Override
    public <U> DifferenceList<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (DifferenceList<U>)ImmutableList.super.scanRight(identity,combiner);
    }

    @Override
    public DifferenceList<T> sorted() {
        return (DifferenceList<T>)ImmutableList.super.sorted();
    }

    @Override
    public DifferenceList<T> sorted(Comparator<? super T> c) {
        return (DifferenceList<T>)ImmutableList.super.sorted(c);
    }

    @Override
    public DifferenceList<T> takeWhile(Predicate<? super T> p) {
        return (DifferenceList<T>)ImmutableList.super.takeWhile(p);
    }

    @Override
    public DifferenceList<T> dropWhile(Predicate<? super T> p) {
        return (DifferenceList<T>)ImmutableList.super.dropWhile(p);
    }

    @Override
    public DifferenceList<T> takeUntil(Predicate<? super T> p) {
        return (DifferenceList<T>)ImmutableList.super.takeUntil(p);
    }

    @Override
    public DifferenceList<T> dropUntil(Predicate<? super T> p) {
        return (DifferenceList<T>)ImmutableList.super.dropUntil(p);
    }

    @Override
    public DifferenceList<T> dropRight(int num) {
        return (DifferenceList<T>)ImmutableList.super.dropRight(num);
    }

    @Override
    public DifferenceList<T> takeRight(int num) {
        return (DifferenceList<T>)ImmutableList.super.takeRight(num);
    }


    @Override
    public DifferenceList<T> intersperse(T value) {
        return (DifferenceList<T>)ImmutableList.super.intersperse(value);
    }

    @Override
    public DifferenceList<T> shuffle() {
        return (DifferenceList<T>)ImmutableList.super.shuffle();
    }

    @Override
    public DifferenceList<T> shuffle(Random random) {
        return (DifferenceList<T>)ImmutableList.super.shuffle(random);
    }

    @Override
    public DifferenceList<T> slice(long from, long to) {
        return (DifferenceList<T>)ImmutableList.super.slice(from,to);
    }

    @Override
    public <U extends Comparable<? super U>> DifferenceList<T> sorted(Function<? super T, ? extends U> function) {
        return (DifferenceList<T>)ImmutableList.super.sorted(function);
    }



    @Override
    public <R> DifferenceList<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatMap(mapper.andThen(DifferenceList::fromIterable));
    }

    @Override
    public <R> DifferenceList<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
         return fromIterable(stream().mergeMap(fn));
    }

    @Override
    public <R> DifferenceList<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
        return fromIterable(stream().mergeMap(maxConcurecy,fn));
    }

    @Override
    public DifferenceList<T> prependStream(Stream<? extends T> stream) {
        return (DifferenceList<T>)ImmutableList.super.prependStream(stream);
    }

    @Override
    public DifferenceList<T> appendAll(T... values) {
        return (DifferenceList<T>)ImmutableList.super.appendAll(values);
    }

    @Override
    public DifferenceList<T> prependAll(T... values) {
        return (DifferenceList<T>)ImmutableList.super.prependAll(values);
    }

    @Override
    public DifferenceList<T> insertAt(int pos, T... values) {
        return (DifferenceList<T>)ImmutableList.super.insertAt(pos,values);
    }

    @Override
    public DifferenceList<T> deleteBetween(int start, int end) {
        return (DifferenceList<T>)ImmutableList.super.deleteBetween(start,end);
    }

    @Override
    public DifferenceList<T> insertStreamAt(int pos, Stream<T> stream) {
        return (DifferenceList<T>)ImmutableList.super.insertStreamAt(pos,stream);
    }

    @Override
    public DifferenceList<T> plusAll(Iterable<? extends T> list) {
        return (DifferenceList<T>)ImmutableList.super.plusAll(list);
    }

    @Override
    public DifferenceList<T> plus(T value) {
        return (DifferenceList<T>)ImmutableList.super.plus(value);
    }

    @Override
    public DifferenceList<T> removeValue(T value) {
        return (DifferenceList<T>)ImmutableList.super.removeValue(value);
    }


    @Override
    public DifferenceList<T> removeAll(Iterable<? extends T> value) {
        return (DifferenceList<T>)ImmutableList.super.removeAll(value);
    }

    @Override
    public DifferenceList<T> prependAll(Iterable<? extends T> value) {
        return (DifferenceList<T>)ImmutableList.super.prependAll(value);
    }

    @Override
    public DifferenceList<T> updateAt(int pos, T value) {
        return (DifferenceList<T>)ImmutableList.super.updateAt(pos,value);
    }

    @Override
    public DifferenceList<T> insertAt(int pos, Iterable<? extends T> values) {
        return (DifferenceList<T>)ImmutableList.super.insertAt(pos,values);
    }

    @Override
    public DifferenceList<T> insertAt(int i, T value) {
        return (DifferenceList<T>)ImmutableList.super.insertAt(i,value);
    }
    @Override
    public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
        return run().fold(fn1,fn2);
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (T next : this)
            hashCode = 31*hashCode + (next==null ? 0 : next.hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null)
            return false;
        if (obj == this)
            return true;
        if(obj instanceof PersistentIndexed) {
            return equalToIteration((Iterable)obj);
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuffer b = new StringBuffer("[");
        Iterator<T> it = iterator();
        if(it.hasNext()){
            b.append(it.next());
        }
        while(it.hasNext()){
            b.append(", "+it.next());
        }
        b.append("]");
        return b.toString();

    }
}
