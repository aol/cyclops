package cyclops.data;


import com.oath.cyclops.types.Zippable;
import com.oath.cyclops.types.persistent.PersistentSet;
import com.oath.cyclops.hkt.Higher;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import com.oath.cyclops.hkt.DataWitness.treeSet;
import cyclops.data.base.RedBlackTree;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;


public final class TreeSet<T> implements ImmutableSortedSet<T>,
                                         Higher<treeSet,T>,
                                         Serializable {

    private final RedBlackTree.Tree<T,T> map;
    private final Comparator<? super T> comp;

    private static final long serialVersionUID = 1L;

    public TreeSet(RedBlackTree.Tree<T, T> map, Comparator<? super T> comp) {
        this.map = RedBlackTree.rootIsBlack(map);
        this.comp = comp;
    }
    public static <T extends Comparable<? super T>>  TreeSet<T> empty(){
        return new TreeSet<T>( RedBlackTree.empty(Comparators.naturalComparator()),Comparators.naturalComparator());
    }
    public static <T extends Comparable<? super T>>  TreeSet<T> singleton(T value){
        return new TreeSet<T>( RedBlackTree.empty(Comparators.naturalComparator()),Comparators.naturalComparator()).plus(value);
    }
    public static <T>  TreeSet<T> singleton(Comparator<? super T> comp,T value){
        return new TreeSet<T>( RedBlackTree.empty(comp),comp).plus(value);
    }
    public static <T> TreeSet<T> empty(Comparator<? super T> comp){
        return new TreeSet<T>( RedBlackTree.empty(comp),comp);
    }
    public static <T> TreeSet<T> fromStream(Stream<T> stream, Comparator<? super T> comp){
        return ReactiveSeq.fromStream(stream).foldLeft(empty(comp),(m,t2)->m.plus(t2));
    }
    public static <T> TreeSet<T> fromIterable(Iterable<T> it,Comparator<? super T> comp){
        return ReactiveSeq.fromIterable(it).foldLeft(empty(comp),(m, t2)->m.plus(t2));
    }

    static <U, T> TreeSet<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed,unfolder),Comparators.naturalOrderIdentityComparator());
    }

    static <T> TreeSet<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,pred,f),Comparators.naturalOrderIdentityComparator());

    }
    static <T> TreeSet<T> iterate(final T seed, final UnaryOperator<T> f,int max) {
        return fromStream(ReactiveSeq.iterate(seed,f).limit(max),Comparators.naturalOrderIdentityComparator());

    }

    static <T, U> Tuple2<TreeSet<T>, TreeSet<U>> unzip(final TreeSet<Tuple2<T, U>> sequence) {
        return ReactiveSeq.unzip(sequence.stream()).transform((a, b)-> Tuple.tuple(fromStream(a,Comparators.naturalOrderIdentityComparator()),fromStream(b,Comparators.naturalOrderIdentityComparator())));
    }
    static <T> TreeSet<T> generate(Supplier<T> s, int max){
        return fromStream(ReactiveSeq.generate(s).limit(max),Comparators.naturalOrderIdentityComparator());
    }
    static <T> TreeSet<T> generate(Generator<T> s){
        return fromStream(ReactiveSeq.generate(s),Comparators.naturalOrderIdentityComparator());
    }
    static TreeSet<Integer> range(final int start, final int end) {
        return TreeSet.fromStream(ReactiveSeq.range(start,end),Comparator.naturalOrder());

    }
    static TreeSet<Integer> range(final int start, final int step, final int end) {
        return TreeSet.fromStream(ReactiveSeq.range(start,step,end),Comparator.naturalOrder());

    }
    static TreeSet<Long> rangeLong(final long start, final long step, final long end) {
        return TreeSet.fromStream(ReactiveSeq.rangeLong(start,step,end),Comparator.naturalOrder());
    }


    static TreeSet<Long> rangeLong(final long start, final long end) {
        return TreeSet.fromStream(ReactiveSeq.rangeLong(start, end),Comparator.<Long>naturalOrder());

    }

    @Override
    public <R> TreeSet<R> unitIterable(Iterable<R> it) {
        return fromIterable(it,Comparators.naturalOrderIdentityComparator());
    }
    public ReactiveSeq<T> stream(){
        return map.stream().map(t->t._1());
    }

    public static <T> TreeSet<T> of(Comparator<? super T> comp, T... values){
        RedBlackTree.Tree<T, T> tree = RedBlackTree.empty(comp);
        for(T value : values){
            tree = RedBlackTree.rootIsBlack(tree.plus(value,value));

        }
        return new TreeSet<>(tree,comp);
    }
    public static <T> TreeSet<T> fromSortedSet(SortedSet<T> set, Comparator<? super T> comp){
        Stream<Tuple2<T,T>> s = set.stream().map(e -> Tuple.tuple(e,e));
        return new TreeSet<T>(RedBlackTree.fromStream(set.comparator(),s),comp);
    }

    public boolean containsValue(T value){
        return map.get(value).isPresent();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public TreeSet<T> add(T value) {
        return new TreeSet<>(map.plus(value,value),comp);
    }

    @Override
    public TreeSet<T> removeValue(T value) {
        return new TreeSet<>(map.minus(value),comp);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public <R> TreeSet<R> map(Function<? super T, ? extends R> fn) {
        return fromStream(stream().map(fn), Comparators.naturalOrderIdentityComparator());
    }

    public <R> TreeSet<R> map(Function<? super T, ? extends R> fn, Comparator<? super R> comp) {
        return fromStream(stream().map(fn), comp);
    }

    @Override
    public <R> TreeSet<R> flatMap(Function<? super T, ? extends ImmutableSet<? extends R>> fn) {
        return fromStream(stream().flatMapI(fn), Comparators.naturalOrderIdentityComparator());
    }
    public <R> TreeSet<R> flatMap(Function<? super T, ? extends ImmutableSet<? extends R>> fn,Comparator<? super R> comp) {
        return fromStream(stream().flatMapI(fn), comp);
    }

    @Override
    public <R> TreeSet<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return fromStream(stream().flatMapI(fn), Comparators.naturalOrderIdentityComparator());
    }
    public <R> TreeSet<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn,Comparator<? super R> comp) {
        return fromStream(stream().flatMapI(fn), comp);
    }

    @Override
    public TreeSet<T> filter(Predicate<? super T> predicate) {
        return fromStream(stream().filter(predicate), Comparators.naturalOrderIdentityComparator());
    }

    @Override
    public <R> TreeSet<R> unitStream(Stream<R> stream) {
        return fromStream(ReactiveSeq.fromStream(stream),Comparators.naturalOrderIdentityComparator());
    }

    @Override
    public ImmutableSortedSet<T> unitStream(Stream<T> stream, Comparator<? super T> comp) {
        return fromStream(ReactiveSeq.fromStream(stream),comp);
    }

    @Override
    public <U> TreeSet<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it),Comparators.naturalOrderIdentityComparator());
    }


    public TreeSet<T> plus(T value){

        return new TreeSet<>(map.plus(value,value),comp);
    }

    @Override
    public TreeSet<T> plusAll(Iterable<? extends T> list) {
        TreeSet<T> res = this;
        for(T next : list){
            res = res.plus(next);
        }
        return res;
    }



    @Override
    public TreeSet<T> removeAll(Iterable<? extends T> list) {
        RedBlackTree.Tree<T, T> local = map;
        for(T next : list)
            local = local.minus(next);
        return new TreeSet<>(local,comp);
    }

    @Override
    public Option<T> get(int index) {
        return stream().elementAt(index);
    }




    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comp;
    }

    @Override
    public ImmutableSortedSet<T> subSet(T fromElement, T toElement) {

       return fromStream(stream().dropWhile(e-> !Objects.equals(e,fromElement))
                                 .takeUntil(e->Objects.equals(e,toElement)),comp);

    }


    @Override
    public Option<T> first() {
        return Option.fromIterable(this);
    }

    @Override
    public Option<T> last() {
        return stream().limitLast(1).takeOne();
    }

    @Override
    public ImmutableSortedSet<T> drop(int num) {
        return fromStream(stream().drop(num),comp);
    }

    @Override
    public ImmutableSortedSet<T> take(int num) {
        return fromStream(stream().take(num),comp);
    }

    public String printTree(){
        return map.tree();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if(o instanceof PersistentSet) {
            PersistentSet<T> set = (PersistentSet<T>) o;
            return equalToIteration(set);

        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (Object element : this)
            result = 31 * result + (element == null ? 0 : element.hashCode());

        return result;
    }

    @Override
    public String toString() {
        return stream().join(",","[","]");
    }

    public TreeSet<T> take(final long n) {
        return (TreeSet<T>)ImmutableSortedSet.super.take(n);

    }
    public TreeSet<T> takeWhile(Predicate<? super T> p) {
        return (TreeSet<T>)ImmutableSortedSet.super.takeWhile(p);
    }
    public TreeSet<T> dropWhile(Predicate<? super T> p) {
        return (TreeSet<T>)ImmutableSortedSet.super.dropWhile(p);
    }
    public TreeSet<T> drop(final long num) {
        return (TreeSet<T>)ImmutableSortedSet.super.drop(num);
    }
    public TreeSet<T> reverse() {
        return (TreeSet<T>)ImmutableSortedSet.super.reverse();
    }
    public Tuple2<TreeSet<T>,TreeSet<T>> duplicate(){
        return Tuple.tuple(this,this);
    }
    public <R1, R2> Tuple2<TreeSet<R1>, TreeSet<R2>> unzip(Function<? super T, Tuple2<? extends R1, ? extends R2>> fn) {
        Tuple2<TreeSet<R1>, TreeSet<Tuple2<? extends R1, ? extends R2>>> x = map(fn).duplicate().map1(s -> s.map(Tuple2::_1));
        return x.map2(s -> s.map(Tuple2::_2));
    }


    @Override
    public TreeSet<T> removeFirst(Predicate<? super T> pred) {
        return (TreeSet<T>)ImmutableSortedSet.super.removeFirst(pred);
    }


    public TreeSet<T> append(T append) {
        return add(append);
    }


    public TreeSet<T> appendAll(Iterable<? extends T> it) {
        TreeSet<T> s = this;
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
    public <U> TreeSet<U> ofType(Class<? extends U> type) {
        return (TreeSet<U>)ImmutableSortedSet.super.ofType(type);
    }

    @Override
    public TreeSet<T> filterNot(Predicate<? super T> predicate) {
        return (TreeSet<T>)ImmutableSortedSet.super.filterNot(predicate);
    }

    @Override
    public TreeSet<T> notNull() {
        return (TreeSet<T>)ImmutableSortedSet.super.notNull();
    }

    @Override
    public TreeSet<T> peek(Consumer<? super T> c) {
        return (TreeSet<T>)ImmutableSortedSet.super.peek(c);
    }

    @Override
    public <R> TreeSet<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (TreeSet<R>)ImmutableSortedSet.super.trampoline(mapper);
    }

    @Override
    public TreeSet<T> removeAllS(Stream<? extends T> stream) {
        return (TreeSet<T>)ImmutableSortedSet.super.removeAllS(stream);
    }

    @Override
    public TreeSet<T> retainAllI(Iterable<? extends T> it) {
        return (TreeSet<T>)ImmutableSortedSet.super.retainAllI(it);
    }

    @Override
    public TreeSet<T> retainAllS(Stream<? extends T> stream) {
        return (TreeSet<T>)ImmutableSortedSet.super.retainAllS(stream);
    }

    @Override
    public TreeSet<T> retainAll(T... values) {
        return (TreeSet<T>)ImmutableSortedSet.super.retainAll(values);
    }

    @Override
    public TreeSet<ReactiveSeq<T>> permutations() {
        return (TreeSet<ReactiveSeq<T>>)ImmutableSortedSet.super.permutations();
    }

    @Override
    public TreeSet<ReactiveSeq<T>> combinations(int size) {
        return (TreeSet<ReactiveSeq<T>>)ImmutableSortedSet.super.combinations(size);
    }

    @Override
    public TreeSet<ReactiveSeq<T>> combinations() {
        return (TreeSet<ReactiveSeq<T>>)ImmutableSortedSet.super.combinations();
    }

    @Override
    public TreeSet<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (TreeSet<T>)ImmutableSortedSet.super.zip(combiner,app);
    }

    @Override
    public <R> TreeSet<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (TreeSet<R>)ImmutableSortedSet.super.zipWith(fn);
    }

    @Override
    public <R> TreeSet<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (TreeSet<R>)ImmutableSortedSet.super.zipWithS(fn);
    }

    @Override
    public <R> TreeSet<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (TreeSet<R>)ImmutableSortedSet.super.zipWithP(fn);
    }

    @Override
    public <T2, R> TreeSet<R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (TreeSet<R>)ImmutableSortedSet.super.zipP(publisher,fn);
    }

    @Override
    public <U, R> TreeSet<R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (TreeSet<R>)ImmutableSortedSet.super.zipS(other,zipper);
    }

    @Override
    public <U> TreeSet<Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return (TreeSet)ImmutableSortedSet.super.zipP(other);
    }

    @Override
    public <U> TreeSet<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (TreeSet)ImmutableSortedSet.super.zip(other);
    }

    @Override
    public <S, U, R> TreeSet<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (TreeSet<R>)ImmutableSortedSet.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> TreeSet<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (TreeSet<R>)ImmutableSortedSet.super.zip4(second,third,fourth,fn);
    }

    @Override
    public TreeSet<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (TreeSet<T>)ImmutableSortedSet.super.combine(predicate,op);
    }

    @Override
    public TreeSet<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (TreeSet<T>)ImmutableSortedSet.super.combine(op,predicate);
    }

    @Override
    public TreeSet<T> cycle(long times) {
        return (TreeSet<T>)ImmutableSortedSet.super.cycle(times);
    }

    @Override
    public TreeSet<T> cycle(Monoid<T> m, long times) {
        return (TreeSet<T>)ImmutableSortedSet.super.cycle(m,times);
    }

    @Override
    public TreeSet<T> cycleWhile(Predicate<? super T> predicate) {
        return (TreeSet<T>) ImmutableSortedSet.super.cycleWhile(predicate);
    }

    @Override
    public TreeSet<T> cycleUntil(Predicate<? super T> predicate) {
        return (TreeSet<T>) ImmutableSortedSet.super.cycleUntil(predicate);
    }

    @Override
    public <U, R> TreeSet<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (TreeSet<R>) ImmutableSortedSet.super.zip(other,zipper);
    }

    @Override
    public <S, U> TreeSet<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (TreeSet) ImmutableSortedSet.super.zip3(second,third);
    }

    @Override
    public <T2, T3, T4> TreeSet<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (TreeSet) ImmutableSortedSet.super.zip4(second,third,fourth);
    }

    @Override
    public TreeSet<Tuple2<T, Long>> zipWithIndex() {
        return (TreeSet<Tuple2<T,Long>>) ImmutableSortedSet.super.zipWithIndex();
    }

    @Override
    public TreeSet<VectorX<T>> sliding(int windowSize) {
        return (TreeSet<VectorX<T>>) ImmutableSortedSet.super.sliding(windowSize);
    }

    @Override
    public TreeSet<VectorX<T>> sliding(int windowSize, int increment) {
        return (TreeSet<VectorX<T>>) ImmutableSortedSet.super.sliding(windowSize,increment);
    }

    @Override
    public <C extends Collection<? super T>> TreeSet<C> grouped(int size, Supplier<C> supplier) {
        return (TreeSet<C>) ImmutableSortedSet.super.grouped(size,supplier);
    }

    @Override
    public TreeSet<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return (TreeSet<ListX<T>>) ImmutableSortedSet.super.groupedUntil(predicate);
    }

    @Override
    public TreeSet<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return (TreeSet<ListX<T>>) ImmutableSortedSet.super.groupedStatefullyUntil(predicate);
    }

    @Override
    public <U> TreeSet<Tuple2<T, U>> zipS(Stream<? extends U> other) {
        return (TreeSet) ImmutableSortedSet.super.zipS(other);
    }

    @Override
    public TreeSet<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return (TreeSet<ListX<T>>) ImmutableSortedSet.super.groupedWhile(predicate);
    }

    @Override
    public <C extends Collection<? super T>> TreeSet<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (TreeSet<C>) ImmutableSortedSet.super.groupedWhile(predicate,factory);
    }

    @Override
    public <C extends Collection<? super T>> TreeSet<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (TreeSet<C>) ImmutableSortedSet.super.groupedUntil(predicate,factory);
    }

    @Override
    public TreeSet<ListX<T>> grouped(int groupSize) {
        return (TreeSet<ListX<T>>) ImmutableSortedSet.super.grouped(groupSize);
    }

    @Override
    public TreeSet<T> distinct() {
        return (TreeSet<T>) ImmutableSortedSet.super.distinct();
    }

    @Override
    public TreeSet<T> scanLeft(Monoid<T> monoid) {
        return (TreeSet<T>) ImmutableSortedSet.super.scanLeft(monoid);
    }

    @Override
    public <U> TreeSet<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (TreeSet<U>) ImmutableSortedSet.super.scanLeft(seed,function);
    }

    @Override
    public TreeSet<T> scanRight(Monoid<T> monoid) {
        return (TreeSet<T>) ImmutableSortedSet.super.scanRight(monoid);
    }

    @Override
    public <U> TreeSet<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (TreeSet<U>) ImmutableSortedSet.super.scanRight(identity,combiner);
    }

    @Override
    public TreeSet<T> sorted() {
        return (TreeSet<T>) ImmutableSortedSet.super.sorted();
    }

    @Override
    public TreeSet<T> sorted(Comparator<? super T> c) {
        return (TreeSet<T>) ImmutableSortedSet.super.sorted(c);
    }



    @Override
    public TreeSet<T> takeUntil(Predicate<? super T> p) {
        return (TreeSet<T>) ImmutableSortedSet.super.takeUntil(p);
    }

    @Override
    public TreeSet<T> dropUntil(Predicate<? super T> p) {
        return (TreeSet<T>) ImmutableSortedSet.super.dropUntil(p);
    }

    @Override
    public TreeSet<T> dropRight(int num) {
        return (TreeSet<T>) ImmutableSortedSet.super.dropRight(num);
    }

    @Override
    public TreeSet<T> takeRight(int num) {
        return (TreeSet<T>) ImmutableSortedSet.super.takeRight(num);
    }

    @Override
    public TreeSet<T> skip(long num) {
        return (TreeSet<T>) ImmutableSortedSet.super.skip(num);
    }

    @Override
    public TreeSet<T> skipWhile(Predicate<? super T> p) {
        return (TreeSet<T>) ImmutableSortedSet.super.skipWhile(p);
    }

    @Override
    public TreeSet<T> skipUntil(Predicate<? super T> p) {
        return (TreeSet<T>) ImmutableSortedSet.super.skipUntil(p);
    }

    @Override
    public TreeSet<T> limit(long num) {
        return (TreeSet<T>) ImmutableSortedSet.super.limit(num);
    }

    @Override
    public TreeSet<T> limitWhile(Predicate<? super T> p) {
        return (TreeSet<T>) ImmutableSortedSet.super.limitWhile(p);
    }

    @Override
    public TreeSet<T> limitUntil(Predicate<? super T> p) {
        return (TreeSet<T>) ImmutableSortedSet.super.limitUntil(p);
    }

    @Override
    public TreeSet<T> intersperse(T value) {
        return (TreeSet<T>) ImmutableSortedSet.super.intersperse(value);
    }

    @Override
    public TreeSet<T> shuffle() {
        return (TreeSet<T>) ImmutableSortedSet.super.shuffle();
    }

    @Override
    public TreeSet<T> skipLast(int num) {
        return (TreeSet<T>) ImmutableSortedSet.super.skipLast(num);
    }

    @Override
    public TreeSet<T> limitLast(int num) {
        return (TreeSet<T>) ImmutableSortedSet.super.limitLast(num);
    }

    @Override
    public TreeSet<T> shuffle(Random random) {
        return (TreeSet<T>) ImmutableSortedSet.super.shuffle(random);
    }

    @Override
    public TreeSet<T> slice(long from, long to) {
        return (TreeSet<T>) ImmutableSortedSet.super.slice(from,to);
    }



    @Override
    public <R> TreeSet<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatMapI(mapper);
    }

    @Override
    public TreeSet<T> prependS(Stream<? extends T> stream) {
        return (TreeSet<T>) ImmutableSortedSet.super.prependS(stream);
    }

    @Override
    public TreeSet<T> append(T... values) {
        return (TreeSet<T>) ImmutableSortedSet.super.append(values);
    }

    @Override
    public TreeSet<T> prependAll(T... values) {
        return (TreeSet<T>) ImmutableSortedSet.super.prependAll(values);
    }

    @Override
    public TreeSet<T> deleteBetween(int start, int end) {
        return (TreeSet<T>) ImmutableSortedSet.super.deleteBetween(start,end);
    }

    @Override
    public TreeSet<T> insertAtS(int pos, Stream<T> stream) {
        return (TreeSet<T>) ImmutableSortedSet.super.insertAtS(pos,stream);
    }

    @Override
    public TreeSet<T> recover(Function<? super Throwable, ? extends T> fn) {
        return this;
    }

    @Override
    public <EX extends Throwable> TreeSet<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return this;
    }

    @Override
    public TreeSet<T> prepend(Iterable<? extends T> value) {
        return (TreeSet<T>) ImmutableSortedSet.super.prepend(value);
    }

    @Override
    public <U extends Comparable<? super U>> TreeSet<T> sorted(Function<? super T, ? extends U> function) {
        return (TreeSet<T>) ImmutableSortedSet.super.sorted(function);
    }
    public String mkString(){
        return stream().join(",","[","]");
    }
    @Override
    public <R> TreeSet<R> retry(Function<? super T, ? extends R> fn) {
        return (TreeSet<R>) ImmutableSortedSet.super.retry(fn);
    }

    @Override
    public <R> TreeSet<R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (TreeSet<R>) ImmutableSortedSet.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    public TreeSet<T> onEmpty(T value) {
        return (TreeSet<T>) ImmutableSortedSet.super.onEmpty(value);
    }

    @Override
    public TreeSet<T> onEmptyGet(Supplier<? extends T> supplier) {
        return (TreeSet<T>) ImmutableSortedSet.super.onEmptyGet(supplier);
    }

    @Override
    public TreeSet<T> removeAllI(Iterable<? extends T> it) {
        return (TreeSet<T>) ImmutableSortedSet.super.removeAllI(it);
    }

    @Override
    public TreeSet<T> removeAll(T... values) {
        return (TreeSet<T>) ImmutableSortedSet.super.removeAll(values);
    }

    @Override
    public TreeSet<T> prepend(T value) {
        return (TreeSet<T>) ImmutableSortedSet.super.prepend(value);
    }

    @Override
    public TreeSet<T> removeAt(long pos) {
        return (TreeSet<T>) ImmutableSortedSet.super.removeAt(pos);
    }

    @Override
    public TreeSet<T> removeAt(int pos) {
        return (TreeSet<T>) ImmutableSortedSet.super.removeAt(pos);
    }

    @Override
    public TreeSet<T> prependAll(Iterable<? extends T> value) {
        return (TreeSet<T>) ImmutableSortedSet.super.prependAll(value);
    }

  @Override
  public <R1, R2, R3, R> TreeSet<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (TreeSet< R>) ImmutableSortedSet.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
  }

  @Override
  public <R1, R2, R3, R> TreeSet<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (TreeSet< R>) ImmutableSortedSet.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
  }

  @Override
  public <R1, R2, R> TreeSet<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (TreeSet< R>) ImmutableSortedSet.super.forEach3(iterable1,iterable2,yieldingFunction);
  }

  @Override
  public <R1, R2, R> TreeSet<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (TreeSet< R>) ImmutableSortedSet.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);
  }

  @Override
  public <R1, R> TreeSet<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (TreeSet< R>) ImmutableSortedSet.super.forEach2(iterable1,yieldingFunction);
  }

  @Override
  public <R1, R> TreeSet<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (TreeSet< R>) ImmutableSortedSet.super.forEach2(iterable1,filterFunction,yieldingFunction);
  }
    @Override
    public TreeSet<T> updateAt(int pos, T value) {
        return (TreeSet<T>) ImmutableSortedSet.super.updateAt(pos,value);
    }

    @Override
    public TreeSet<T> insertAt(int pos, Iterable<? extends T> values) {
        return (TreeSet<T>) ImmutableSortedSet.super.insertAt(pos,values);
    }

    @Override
    public TreeSet<T> insertAt(int i, T value) {
        return (TreeSet<T>) ImmutableSortedSet.super.insertAt(i,value);
    }

    @Override
    public TreeSet<T> insertAt(int pos, T... values) {
        return (TreeSet<T>) ImmutableSortedSet.super.insertAt(pos,values);
    }

}
