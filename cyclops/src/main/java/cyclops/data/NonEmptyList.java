package cyclops.data;


import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentList;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.matching.Deconstruct.Deconstruct2;
import cyclops.control.Option;
import com.oath.cyclops.hkt.DataWitness.nonEmptyList;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of={"head,tail"})
public class NonEmptyList<T> implements Deconstruct2<T,ImmutableList<T>>,
                                        ImmutableList<T>,
                                        ImmutableList.Some<T>,
                                        Higher<nonEmptyList,T> {

    private final T head;
    private final ImmutableList<T> tail;

    @Override
    public<R> ImmutableList<R> unitIterable(Iterable<R> it){
        if(it instanceof NonEmptyList){
            return (NonEmptyList<R>)it;
        }
        return this.unitIterator(it.iterator());
    }

    public ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(this);
    }
    public LazySeq<T> linkedSeq(){
        return LazySeq.fromIterable(this);
    }
    public static <T> NonEmptyList<T> of(T head, T... value){
        LazySeq<T> list = LazySeq.of(value);
        return cons(head,list);
    }
    public static <T> NonEmptyList<T> of(T head){
        LazySeq<T> list = LazySeq.empty();
        return cons(head,list);
    }

    public static <T> NonEmptyList<T> eager(T head, T... value){
        Seq<T> list = Seq.of(value);
        return cons(head,list);
    }
    public static <T> NonEmptyList<T> eager(T head){
        Seq<T> list = Seq.empty();
        return cons(head,list);
    }
    public static <T> NonEmptyList<T> of(T head, ImmutableList<T> list){
        return cons(head,list);
    }


    public Option<T> get(int pos){
        if(pos==0)
            return Option.of(head);
        return tail.get(pos-1);

    }

    @Override
    public T getOrElse(int pos, T alt) {
        return get(pos).orElse(alt);
    }

    @Override
    public T getOrElseGet(int pos, Supplier<? extends T> alt) {
        return get(pos).orElseGet(alt);
    }

    public LazySeq<T> lazySeq(){
        return LazySeq.lazy(head,()->tail);
    }

    @Override
    public <R> ImmutableList<R> unitStream(Stream<R> stream) {
        Iterator<R> it = stream.iterator();
        return this.unitIterator(it);
    }


    private <R> ImmutableList<R> unitIterator(Iterator<R> it) {

        if(it.hasNext()){
            return cons(it.next(), LazySeq.fromIterator(it));
        }
        return LazySeq.empty();
    }

    @Override
    public ImmutableList<T> emptyUnit() {
        return Seq.empty();
    }

    @Override
    public ImmutableList<T> drop(long num) {
        if(num>=size())
            return Seq.empty();

        return unitStream(stream().drop(num));
    }

    @Override
    public ImmutableList<T> take(long num) {
        if(num<=0){
            return LazySeq.empty();
        }
        return cons(head,tail.take(num-1));
    }

    public NonEmptyList<T> prepend(T value){
        return cons(value, lazySeq());
    }


    @Override
    public NonEmptyList<T> replaceFirst(T currentElement, T newElement){
        return ImmutableList.Some.super.replaceFirst(currentElement,newElement)
                            .fold(cons->cons(cons.head(),cons.tail()),nil->this);
    }


    @Override
    public NonEmptyList<T> insertAt(int pos, T... values) {
        return (NonEmptyList<T>)ImmutableList.Some.super.insertAt(pos,values);
    }

    @Override
    public NonEmptyList<T> plusAll(Iterable<? extends T> list) {
        return (NonEmptyList<T>)ImmutableList.Some.super.plusAll(list);
    }

    @Override
    public NonEmptyList<T> plus(T value) {
        return (NonEmptyList<T>)ImmutableList.Some.super.plus(value);
    }

    @Override
    public NonEmptyList<T> updateAt(int pos, T value) {
        return (NonEmptyList<T>)ImmutableList.Some.super.updateAt(pos,value);
    }

    @Override
    public NonEmptyList<T> insertAt(int pos, Iterable<? extends T> values) {
        return (NonEmptyList<T>)ImmutableList.Some.super.insertAt(pos,values);
    }

    @Override
    public NonEmptyList<T> insertAt(int i, T value) {
        return (NonEmptyList<T>)ImmutableList.Some.super.insertAt(i,value);
    }

    @Override
    public NonEmptyList<T> prependAll(Iterable<? extends T> value) {
        LazySeq<T> list = LazySeq.narrow(lazySeq().prependAll(value));
        return cons(list.fold(c->c.head(), nil->null),list.drop(1));
    }

    @Override
    public NonEmptyList<T> append(T value) {
        return of(head, tail.append(value));
    }

    @Override
    public NonEmptyList<T> appendAll(Iterable<? extends T> value) {
        return of(head,tail.appendAll(value));
    }

    @Override
    public ImmutableList<T> tail() {
        return tail;
    }

    @Override
    public T head() {
        return head;
    }

    @Override
    public NonEmptyList<T> reverse() {
        return of(head).prependAll(tail);
    }

    public NonEmptyList<T> prependAll(NonEmptyList<T> value){
        return value.prependAll(this);
    }

    public ImmutableList<T> filter(Predicate<? super T> pred){
        return lazySeq().filter(pred);
    }


    public <R> NonEmptyList<R> map(Function<? super T, ? extends R> fn) {
        return NonEmptyList.of(fn.apply(head),tail.map(fn));
    }

    @Override
    public NonEmptyList<T> peek(Consumer<? super T> c) {
        return (NonEmptyList<T>)ImmutableList.Some.super.peek(c);
    }


    @Override
    public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
        return fn1.apply(this);
    }

    @Override
    public NonEmptyList<T> onEmpty(T value) {
        return this;
    }

    @Override
    public NonEmptyList<T> onEmptyGet(Supplier<? extends T> supplier) {
        return this;
    }

    @Override
    public NonEmptyList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
        return this;
    }


    @Override
    public <R> ImmutableList<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
        return lazySeq().flatMap(fn);
    }

    @Override
    public <R> ImmutableList<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return lazySeq().concatMap(fn);
    }

    @Override
    public <R> ImmutableList<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
      return lazySeq().mergeMap(fn);
    }

    @Override
    public <R> ImmutableList<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
      return lazySeq().mergeMap(maxConcurecy,fn);
    }

  public <R> NonEmptyList<R> flatMapNel(Function<? super T, ? extends NonEmptyList<R>> fn) {
        return fn.apply(head).appendAll(tail.flatMap(fn));

    }



    public <R> R foldRight(R zero,BiFunction<? super T, ? super R, ? extends R> f) {
        return lazySeq().foldRight(zero,f);

    }

    public  T reduce(BiFunction<? super T, ? super T, ? extends T> f){
        Iterator<T> it = iterator();
        T res = it.next();

        while(it.hasNext()){
            T next = it.next();
            res= f.apply(res,next);
        }
        return res;
    }
    public <R> R foldLeft(R zero,BiFunction<R, ? super T, R> f) {
        return lazySeq().foldLeft(zero,f);
    }

    public int size(){
        return 1+tail.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public static <T> NonEmptyList<T> cons(T value, ImmutableList<T> tail){
        return new NonEmptyList<>(value,tail);
    }

    @Override
    public String toString() {
        return stream().join(", ","[","]");
    }

    @Override
    public Tuple2<T, ImmutableList<T>> unapply() {
        return Tuple.tuple(head,tail);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if(o instanceof PersistentList){
            PersistentList<T> im =(PersistentList<T>)o;
            return equalToIteration(im);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), head, tail);
    }

    @Override
    public NonEmptyList<ReactiveSeq<T>> permutations() {
        return (NonEmptyList<ReactiveSeq<T>>)ImmutableList.Some.super.permutations();
    }

    @Override
    public NonEmptyList<ReactiveSeq<T>> combinations(int size) {
        return (NonEmptyList<ReactiveSeq<T>>)ImmutableList.Some.super.combinations(size);
    }

    @Override
    public NonEmptyList<ReactiveSeq<T>> combinations() {
        return (NonEmptyList<ReactiveSeq<T>>)ImmutableList.Some.super.combinations();
    }


    @Override
    public NonEmptyList<Tuple2<T, Long>> zipWithIndex() {
        return (NonEmptyList<Tuple2<T,Long>>) ImmutableList.Some.super.zipWithIndex();
    }

    @Override
    public NonEmptyList<Seq<T>> sliding(int windowSize) {
        return (NonEmptyList<Seq<T>>) ImmutableList.Some.super.sliding(windowSize);
    }

    @Override
    public NonEmptyList<Seq<T>> sliding(int windowSize, int increment) {
        return (NonEmptyList<Seq<T>>) ImmutableList.Some.super.sliding(windowSize,increment);
    }

    @Override
    public <C extends PersistentCollection<? super T>> NonEmptyList<C> grouped(int size, Supplier<C> supplier) {
        return (NonEmptyList<C>) ImmutableList.Some.super.grouped(size,supplier);
    }

    @Override
    public NonEmptyList<Vector<T>> groupedUntil(Predicate<? super T> predicate) {
        return (NonEmptyList<Vector<T>>) ImmutableList.Some.super.groupedUntil(predicate);
    }

    @Override
    public NonEmptyList<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return (NonEmptyList<Vector<T>>) ImmutableList.Some.super.groupedUntil(predicate);
    }


    @Override
    public NonEmptyList<Vector<T>> groupedWhile(Predicate<? super T> predicate) {
        return (NonEmptyList<Vector<T>>) ImmutableList.Some.super.groupedWhile(predicate);
    }

    @Override
    public <C extends PersistentCollection<? super T>> NonEmptyList<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (NonEmptyList<C>) ImmutableList.Some.super.groupedWhile(predicate,factory);
    }

    @Override
    public <C extends PersistentCollection<? super T>> NonEmptyList<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (NonEmptyList<C>) ImmutableList.Some.super.groupedUntil(predicate,factory);
    }

    @Override
    public NonEmptyList<Vector<T>> grouped(int groupSize) {
        return (NonEmptyList<Vector<T>>) ImmutableList.Some.super.grouped(groupSize);
    }

    @Override
    public NonEmptyList<T> distinct() {
        return (NonEmptyList<T>) ImmutableList.Some.super.distinct();
    }

    @Override
    public NonEmptyList<T> scanLeft(Monoid<T> monoid) {
        return (NonEmptyList<T>) ImmutableList.Some.super.scanLeft(monoid);
    }

    @Override
    public <U> NonEmptyList<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (NonEmptyList<U>) ImmutableList.Some.super.scanLeft(seed,function);
    }

    @Override
    public NonEmptyList<T> scanRight(Monoid<T> monoid) {
        return (NonEmptyList<T>) ImmutableList.Some.super.scanRight(monoid);
    }

    @Override
    public <U> NonEmptyList<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (NonEmptyList<U>) ImmutableList.Some.super.scanRight(identity,combiner);
    }

    @Override
    public NonEmptyList<T> sorted() {
        return (NonEmptyList<T>) ImmutableList.Some.super.sorted();
    }

    @Override
    public NonEmptyList<T> sorted(Comparator<? super T> c) {
        return (NonEmptyList<T>) ImmutableList.Some.super.sorted(c);
    }




    @Override
    public NonEmptyList<T> intersperse(T value) {
        return (NonEmptyList<T>) ImmutableList.Some.super.intersperse(value);
    }

    @Override
    public NonEmptyList<T> shuffle() {
        return (NonEmptyList<T>) ImmutableList.Some.super.shuffle();
    }



    @Override
    public NonEmptyList<T> shuffle(Random random) {
        return (NonEmptyList<T>) ImmutableList.Some.super.shuffle(random);
    }

    @Override
    public Iterator<T> iterator(){
        return new Iterator<T>() {
            ImmutableList<T> current= NonEmptyList.this;
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
    public NonEmptyList<T> prependStream(Stream<? extends T> stream) {
        return (NonEmptyList<T>) ImmutableList.Some.super.prependStream(stream);
    }

    @Override
    public NonEmptyList<T> appendAll(T... values) {
        return (NonEmptyList<T>) ImmutableList.Some.super.appendAll(values);
    }

    @Override
    public NonEmptyList<T> prependAll(T... values) {
        return (NonEmptyList<T>) ImmutableList.Some.super.prependAll(values);
    }

    @Override
    public NonEmptyList<T> insertStreamAt(int pos, Stream<T> stream) {
        return (NonEmptyList<T>) ImmutableList.Some.super.insertStreamAt(pos,stream);
    }




    @Override
    public <U extends Comparable<? super U>> NonEmptyList<T> sorted(Function<? super T, ? extends U> function) {
        return (NonEmptyList<T>) ImmutableList.Some.super.sorted(function);
    }
}
