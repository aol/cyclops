package cyclops.data;


import cyclops.control.Option;
import cyclops.data.base.RedBlackTree;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;


public final class TreeSet<T> implements ImmutableSortedSet<T>{
    private final RedBlackTree.Tree<T,T> map;
    private final Comparator<? super T> comp;

    private static final long serialVersionUID = 1L;

    public TreeSet(RedBlackTree.Tree<T, T> map, Comparator<? super T> comp) {
        this.map = RedBlackTree.rootIsBlack(map);
        this.comp = comp;
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

    public boolean contains(T value){
        return map.get(value).isPresent();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public ImmutableSortedSet<T> add(T value) {
        return new TreeSet<>(map.plus(value,value),comp);
    }

    @Override
    public TreeSet<T> remove(T value) {
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
    public TreeSet<T> minus(T value){
        return new TreeSet<>(map.minus(value),comp);
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
        return stream().limitLast(1).findOne();
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
        if(o instanceof ImmutableSet) {
            ImmutableSet<T> set = (ImmutableSet<T>) o;
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
}
