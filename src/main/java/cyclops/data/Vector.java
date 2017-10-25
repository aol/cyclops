package cyclops.data;


import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.persistent.PersistentIndexed;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.monads.DataWitness.vector;
import cyclops.data.base.BAMT;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.reactivestreams.Publisher;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

@AllArgsConstructor
public class Vector<T> implements ImmutableList<T>,
                                  Serializable,
                                 Higher<vector,T> {

    private final BAMT.NestedArray<T> root;
    private final BAMT.ActiveTail<T> tail;
    private final int size;
    private final Supplier<Integer> hash = Memoize.memoizeSupplier(() -> calcHash());

    @Override
    public Vector<T> plusAll(Iterable<? extends T> list) {
        return ( Vector<T>)appendAll((Iterable<T>)list);
    }

    @Override
    public boolean containsValue(T value) {
        return stream().filter(i->Objects.equals(i,value)).findFirst().isPresent();
    }
    @Override
    public<R> Vector<R> unitIterable(Iterable<R> it){
        if(it instanceof Vector){
            return (Vector<R>)it;
        }
        return fromIterable(it);
    }


    @Override
    public Vector<T> removeValue(T e) {
        return removeFirst(i-> Objects.equals(i,e));
    }

    @Override
    public Vector<T> removeAll(Iterable<? extends T> list) {
        return removeAllI(list);
    }
    @Override
    public Vector<T> removeAllI(Iterable<? extends T> it) {
        return (Vector<T>)ImmutableList.super.removeAllI(it);
    }
    @Override
    public Vector<T> removeAt(int i) {
        return (Vector<T>)ImmutableList.super.removeAt(i);
    }
    @Override
    public Vector<T> removeAt(long pos) {
        return unitStream(stream().removeAt(pos));
    }
    @Override
    public Vector<T> insertAt(int pos, T... values) {
        return (Vector<T>)ImmutableList.super.insertAt(pos,values);
    }
    @Override
    public Vector<T> insertAt(int i, T e){
        return (Vector<T>)ImmutableList.super.insertAt(i,e);
    }

    @Override
    public Vector<T> insertAt(int pos, Iterable<? extends T> values) {
        return (Vector<T>)ImmutableList.super.insertAt(pos,values);
    }
    @Override
    public Vector<T> insertAt(int pos, ReactiveSeq<? extends T> values) {
        return (Vector<T>)ImmutableList.super.insertAt(pos,values);
    }
    public Vector<T> plusAll(int i, Iterable<? extends T> values){
        return insertAt(i,values);
    }

    public static <T> Vector<T> empty(){
        return new Vector<>(new BAMT.Zero<>(),BAMT.ActiveTail.emptyTail(),0);
    }

    public static <T> Vector<T> fill(T t, int max){
        return Vector.fromStream(ReactiveSeq.fill(t).take(max));
    }

    public static <U, T> Vector<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed,unfolder));
    }

    public static <T> Vector<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,pred,f));

    }
    public static <T> Vector<T> iterate(final T seed, final UnaryOperator<T> f,int max) {
        return fromStream(ReactiveSeq.iterate(seed,f).limit(max));

    }

    public static <T, U> Tuple2<Vector<T>, Vector<U>> unzip(final Vector<Tuple2<T, U>> sequence) {
        return ReactiveSeq.unzip(sequence.stream()).transform((a, b)->Tuple.tuple(fromStream(a),fromStream(b)));
    }
    public static <T> Vector<T> generate(Supplier<T> s, int max){
        return fromStream(ReactiveSeq.generate(s).limit(max));
    }
    public static <T> Vector<T> generate(Generator<T> s){
        return fromStream(ReactiveSeq.generate(s));
    }
    public static Vector<Integer> range(final int start, final int end) {
        return Vector.fromStream(ReactiveSeq.range(start,end));

    }
    public static Vector<Integer> range(final int start, final int step, final int end) {
        return Vector.fromStream(ReactiveSeq.range(start,step,end));

    }
    public static Vector<Long> rangeLong(final long start, final long step, final long end) {
        return Vector.fromStream(ReactiveSeq.rangeLong(start,step,end));
    }


    public static Vector<Long> rangeLong(final long start, final long end) {
        return Vector.fromStream(ReactiveSeq.rangeLong(start, end));

    }
    public static <T> Vector<T> fromStream(Stream<T> it){
        return fromIterable(()->it.iterator());
    }
    public static <T> Vector<T> fromIterable(Iterable<T> it){
        if(it instanceof Vector){
            return (Vector<T>)it;
        }
        Vector<T> res = empty();
        for(T next : it){
            res = res.plus(next);
        }
        return res;
    }
    public static <T> Vector<T> of(T... value){
        Vector<T> res = empty();
        for(T next : value){
            res = res.plus(next);
        }
        return res;
    }


    public Vector<T> removeFirst(Predicate<? super T> pred) {
        return (Vector<T>)ImmutableList.super.removeFirst(pred);
    }
    public VectorX<T> vectorX(){
        return stream().to().vectorX(Evaluation.LAZY);
    }
    public ReactiveSeq<T> stream(){
        return ReactiveSeq.concat(root.stream(),tail.stream());
    }

    public Vector<T> filter(Predicate<? super T> pred){
        return fromIterable(stream().filter(pred));
    }

    public <R> Vector<R> map(Function<? super T, ? extends R> fn){
        return fromIterable(stream().map(fn));
    }

    private Object writeReplace() {
        return new Proxy(this);
    }
    private Object readResolve() throws InvalidObjectException {
        throw new InvalidObjectException("Use Serialization Proxy instead.");
    }

    @Override
    public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
        return size()==0? fn2.apply(VectorNone.empty()) : fn1.apply(this.new VectorSome(this));
    }

    @Override
    public Vector<T> onEmpty(T value) {
        return size()==0? Vector.of(value) : this;
    }

    @Override
    public Vector<T> onEmptyGet(Supplier<? extends T> supplier) {
        return size()==0? Vector.of(supplier.get()) : this;
    }


    @Override
    public Vector<T> replaceFirst(T currentElement, T newElement) {
        return (Vector<T>)ImmutableList.super.replaceFirst(currentElement,newElement);
    }

    @Override
    public <U> Vector<U> ofType(Class<? extends U> type) {
        return (Vector<U>)ImmutableList.super.ofType(type);
    }

    @Override
    public Vector<T> filterNot(Predicate<? super T> predicate) {
        return (Vector<T>)ImmutableList.super.filterNot(predicate);
    }

    @Override
    public Vector<T> notNull() {
        return (Vector<T>)ImmutableList.super.notNull();
    }

    @Override
    public Vector<T> peek(Consumer<? super T> c) {
        return (Vector<T>)ImmutableList.super.peek(c);
    }

    @Override
    public <R> Vector<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (Vector<R>)ImmutableList.super.trampoline(mapper);
    }

    @Override
    public Vector<T> removeAllS(Stream<? extends T> stream) {
        return (Vector<T>)ImmutableList.super.removeAllS(stream);
    }

    @Override
    public Vector<T> retainAllI(Iterable<? extends T> it) {
        return (Vector<T>)ImmutableList.super.retainAllI(it);
    }

    @Override
    public Vector<T> retainAllS(Stream<? extends T> stream) {
        return (Vector<T>)ImmutableList.super.retainAllS(stream);
    }

    @Override
    public Vector<T> retainAll(T... values) {
        return (Vector<T>)ImmutableList.super.retainAll(values);
    }

    @Override
    public Vector<ReactiveSeq<T>> permutations() {
        return (Vector<ReactiveSeq<T>>)ImmutableList.super.permutations();
    }

    @Override
    public Vector<ReactiveSeq<T>> combinations(int size) {
        return (Vector<ReactiveSeq<T>>)ImmutableList.super.combinations(size);
    }

    @Override
    public Vector<ReactiveSeq<T>> combinations() {
        return (Vector<ReactiveSeq<T>>)ImmutableList.super.combinations();
    }

    @Override
    public Vector<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (Vector<T>)ImmutableList.super.zip(combiner,app);
    }

    @Override
    public <R> Vector<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (Vector<R>)ImmutableList.super.zipWith(fn);
    }

    @Override
    public <R> Vector<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (Vector<R>)ImmutableList.super.zipWithS(fn);
    }

    @Override
    public <R> Vector<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (Vector<R>)ImmutableList.super.zipWithP(fn);
    }

    @Override
    public <T2, R> Vector<R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (Vector<R>)ImmutableList.super.zipP(publisher,fn);
    }

    @Override
    public <U, R> Vector<R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (Vector<R>)ImmutableList.super.zipS(other,zipper);
    }

    @Override
    public <U> Vector<Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return (Vector)ImmutableList.super.zipP(other);
    }

    @Override
    public <U> Vector<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (Vector)ImmutableList.super.zip(other);
    }

    @Override
    public <S, U, R> Vector<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (Vector<R>)ImmutableList.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> Vector<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Vector<R>)ImmutableList.super.zip4(second,third,fourth,fn);
    }

    @Override
    public <U> Vector<U> unitIterator(Iterator<U> it) {
        return fromIterable(()->it);
    }

    @Override
    public Vector<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (Vector<T>)ImmutableList.super.combine(predicate,op);
    }

    @Override
    public Vector<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (Vector<T>)ImmutableList.super.combine(op,predicate);
    }

    @Override
    public Vector<T> cycle(long times) {
        return (Vector<T>)ImmutableList.super.cycle(times);
    }

    @Override
    public Vector<T> cycle(Monoid<T> m, long times) {
        return (Vector<T>)ImmutableList.super.cycle(m,times);
    }

    @Override
    public Vector<T> cycleWhile(Predicate<? super T> predicate) {
        return (Vector<T>) ImmutableList.super.cycleWhile(predicate);
    }

    @Override
    public Vector<T> cycleUntil(Predicate<? super T> predicate) {
        return (Vector<T>) ImmutableList.super.cycleUntil(predicate);
    }

    @Override
    public <U, R> Vector<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (Vector<R>) ImmutableList.super.zip(other,zipper);
    }

    @Override
    public <S, U> Vector<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (Vector) ImmutableList.super.zip3(second,third);
    }

    @Override
    public <T2, T3, T4> Vector<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (Vector) ImmutableList.super.zip4(second,third,fourth);
    }

    @Override
    public Vector<Tuple2<T, Long>> zipWithIndex() {
        return (Vector<Tuple2<T,Long>>) ImmutableList.super.zipWithIndex();
    }

    @Override
    public Vector<VectorX<T>> sliding(int windowSize) {
        return (Vector<VectorX<T>>) ImmutableList.super.sliding(windowSize);
    }

    @Override
    public Vector<VectorX<T>> sliding(int windowSize, int increment) {
        return (Vector<VectorX<T>>) ImmutableList.super.sliding(windowSize,increment);
    }

    @Override
    public <C extends Collection<? super T>> Vector<C> grouped(int size, Supplier<C> supplier) {
        return (Vector<C>) ImmutableList.super.grouped(size,supplier);
    }

    @Override
    public Vector<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return (Vector<ListX<T>>) ImmutableList.super.groupedUntil(predicate);
    }

    @Override
    public Vector<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return (Vector<ListX<T>>) ImmutableList.super.groupedStatefullyUntil(predicate);
    }

    @Override
    public <U> Vector<Tuple2<T, U>> zipS(Stream<? extends U> other) {
        return (Vector) ImmutableList.super.zipS(other);
    }

    @Override
    public Vector<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return (Vector<ListX<T>>) ImmutableList.super.groupedWhile(predicate);
    }

    @Override
    public <C extends Collection<? super T>> Vector<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (Vector<C>) ImmutableList.super.groupedWhile(predicate,factory);
    }

    @Override
    public <C extends Collection<? super T>> Vector<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (Vector<C>) ImmutableList.super.groupedUntil(predicate,factory);
    }

    @Override
    public Vector<ListX<T>> grouped(int groupSize) {
        return (Vector<ListX<T>>) ImmutableList.super.grouped(groupSize);
    }

    @Override
    public Vector<T> distinct() {
        return (Vector<T>) ImmutableList.super.distinct();
    }

    @Override
    public Vector<T> scanLeft(Monoid<T> monoid) {
        return (Vector<T>) ImmutableList.super.scanLeft(monoid);
    }

    @Override
    public <U> Vector<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (Vector<U>) ImmutableList.super.scanLeft(seed,function);
    }

    @Override
    public Vector<T> scanRight(Monoid<T> monoid) {
        return (Vector<T>) ImmutableList.super.scanRight(monoid);
    }

    @Override
    public <U> Vector<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (Vector<U>) ImmutableList.super.scanRight(identity,combiner);
    }

    @Override
    public Vector<T> sorted() {
        return (Vector<T>) ImmutableList.super.sorted();
    }

    @Override
    public Vector<T> sorted(Comparator<? super T> c) {
        return (Vector<T>) ImmutableList.super.sorted(c);
    }

    @Override
    public Vector<T> takeWhile(Predicate<? super T> p) {
        return (Vector<T>) ImmutableList.super.takeWhile(p);
    }

    @Override
    public Vector<T> dropWhile(Predicate<? super T> p) {
        return (Vector<T>) ImmutableList.super.dropWhile(p);
    }

    @Override
    public Vector<T> takeUntil(Predicate<? super T> p) {
        return (Vector<T>) ImmutableList.super.takeUntil(p);
    }

    @Override
    public Vector<T> dropUntil(Predicate<? super T> p) {
        return (Vector<T>) ImmutableList.super.dropUntil(p);
    }


    @Override
    public Vector<T> skip(long num) {
        return (Vector<T>) ImmutableList.super.skip(num);
    }

    @Override
    public Vector<T> skipWhile(Predicate<? super T> p) {
        return (Vector<T>) ImmutableList.super.skipWhile(p);
    }

    @Override
    public Vector<T> skipUntil(Predicate<? super T> p) {
        return (Vector<T>) ImmutableList.super.skipUntil(p);
    }

    @Override
    public Vector<T> limit(long num) {
        return (Vector<T>) ImmutableList.super.limit(num);
    }

    @Override
    public Vector<T> limitWhile(Predicate<? super T> p) {
        return (Vector<T>) ImmutableList.super.limitWhile(p);
    }

    @Override
    public Vector<T> limitUntil(Predicate<? super T> p) {
        return (Vector<T>) ImmutableList.super.limitUntil(p);
    }

    @Override
    public Vector<T> intersperse(T value) {
        return (Vector<T>) ImmutableList.super.intersperse(value);
    }

    @Override
    public Vector<T> shuffle() {
        return (Vector<T>) ImmutableList.super.shuffle();
    }

    @Override
    public Vector<T> skipLast(int num) {
        return (Vector<T>) ImmutableList.super.skipLast(num);
    }

    @Override
    public Vector<T> limitLast(int num) {
        return (Vector<T>) ImmutableList.super.limitLast(num);
    }

    @Override
    public Vector<T> shuffle(Random random) {
        return (Vector<T>) ImmutableList.super.shuffle(random);
    }

    @Override
    public Vector<T> slice(long from, long to) {
        return (Vector<T>) ImmutableList.super.slice(from,to);
    }


    @Override
    public <R> Vector<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatMapI(mapper);
    }

    @Override
    public Vector<T> prependS(Stream<? extends T> stream) {
        return (Vector<T>) ImmutableList.super.prependS(stream);
    }

    @Override
    public Vector<T> append(T... values) {
        return (Vector<T>) ImmutableList.super.append(values);
    }

    @Override
    public Vector<T> prependAll(T... values) {
        return (Vector<T>) ImmutableList.super.prependAll(values);
    }

    @Override
    public Vector<T> deleteBetween(int start, int end) {
        return (Vector<T>) ImmutableList.super.deleteBetween(start,end);
    }

    @Override
    public Vector<T> insertAtS(int pos, Stream<T> stream) {
        return (Vector<T>) ImmutableList.super.insertAtS(pos,stream);
    }

    @Override
    public Vector<T> recover(Function<? super Throwable, ? extends T> fn) {
        return this;
    }

    @Override
    public <EX extends Throwable> Vector<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return this;
    }

    @Override
    public Vector<T> prepend(Iterable<? extends T> value) {
        return (Vector<T>) ImmutableList.super.prepend(value);
    }

    @Override
    public <U extends Comparable<? super U>> Vector<T> sorted(Function<? super T, ? extends U> function) {
        return (Vector<T>) ImmutableList.super.sorted(function);
    }

    @Override
    public Vector<T> updateAt(int pos, T value) {
        return (Vector<T>)ImmutableList.super.updateAt(pos,value);
    }

    @Override
    public ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
        if(size()!=0)
            return this;
        return supplier.get();
    }

    public <R> Vector<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn){
        return fromIterable(stream().flatMapI(fn));
    }

    @Override
    public <R> Vector<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return fromIterable(stream().flatMapI(fn));
    }

    public Vector<T> set(int pos, T value){
        if(pos<0||pos>=size){
            return this;
        }
        int tailStart = size-tail.size();
        if(pos>=tailStart){
            return new Vector<T>(root,tail.set(pos-tailStart,value),size);
        }
        return new Vector<>(root.match(z->z, p->p.set(pos,value)),tail,size);
    }

    public int size(){
        return size;
    }



    @Override
    public boolean isEmpty() {
        return size==0;
    }

    public Vector<T> plus(T t){
        if(tail.size()<32) {
            return new Vector<T>(root,tail.append(t),size+1);
        }else{
            return new Vector<T>(root.append(tail),BAMT.ActiveTail.tail(t),size+1);
        }
    }
    @AllArgsConstructor
    private static final class Proxy<T> implements Serializable {

        private static final long serialVersionUID = 1L;
        Vector<T> v;

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeInt(v.size());
            Iterator<T> it = v.iterator();
            while(it.hasNext()){
                s.writeObject(it.next());
            }
        }
        private Object readResolve() {
            return v;
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            final int size = s.readInt();
            Vector<T> res = empty();
            for (int i = 0; i < size; i++) {
                T n = (T) s.readObject();
                res = res.append(n);
            }
            v=res;
        }
    }

    @Override
    public <R> Vector<R> unitStream(Stream<R> stream) {
        return fromIterable(ReactiveSeq.fromStream(stream));
    }

    @Override
    public Vector<T> emptyUnit() {
        return empty();
    }

    public Vector<T> takeRight(int num){
        if(num<=0)
            return empty();
        if(num>=size())
            return this;
        if(num==tail.size())
            return new Vector<>(new BAMT.Zero<>(),tail,num);
        if(num<tail.size()){
            BAMT.ActiveTail<T> newTail = tail.takeRight(num);
            return new Vector<>(new BAMT.Zero<>(),newTail,newTail.size());
        }
        return (Vector<T>)ImmutableList.super.dropRight(num);
    }

    public Vector<T> dropRight(int num){
        if(num<=0)
            return this;
        if(num>=size())
            return empty();
        if(tail.size()==1){
            return new Vector<>(this.root,BAMT.ActiveTail.emptyTail(),size()-1).drop(num-1);
        }
        if(tail.size()>0){
            return new Vector<>(this.root,tail.dropRight(num),size()-(Math.max(tail.size(),num))).dropRight(num-tail.size());
        }
        return unitStream(stream().dropRight(num));
    }
    @Override
    public Vector<T> drop(long num) {
        if(num<=0)
            return this;
        if(num>=size())
            return empty();
        if(size()<32){
            return new Vector<>(this.root,tail.drop((int)num),size()-1);
        }
        return unitStream(stream().drop(num));
    }

    @Override
    public Vector<T> take(long num) {
        if(num<=0)
            return empty();
        if(num>=size())
            return this;
        if(size()<32){
            return new Vector<T>(this.root,tail.dropRight(Math.max(tail.size()-(int)num,0)),(int)num);
        }
        return unitStream(stream().take(num));
    }

    @Override
    public ImmutableList<T> prepend(T value) {
        return unitStream(stream().prepend(value));
    }

    @Override
    public ImmutableList<T> prependAll(Iterable<? extends T> value) {
        return unitStream(stream().prepend(value));
    }

    @Override
    public Vector<T> append(T value) {
        return plus(value);
    }

    @Override
    public Vector<T> appendAll(Iterable<? extends T> value) {
        Vector<T> vec = this;

        for(T next : value){
            vec = vec.plus(next);
        }
        return vec;
    }
    public Vector<T> subList(int start, int end){
        return drop(start).take(end-start);
    }

    @Override
    public ImmutableList<T> reverse() {
            return unitStream(stream().reverse());
    }

    public Option<T> get(int pos){
        if(pos<0||pos>=size){
            return Option.none();
        }
        int tailStart = size-tail.size();
        if(pos>=tailStart){
            return tail.get(pos-tailStart);
        }
        return ((BAMT.PopulatedArray<T>)root).get(pos);

    }

    @Override
    public T getOrElse(int pos, T alt) {
        if(pos<0||pos>=size){
            return alt;
        }
        int tailStart = size-tail.size();
        if(pos>=tailStart){
            return tail.getOrElse(pos-tailStart,alt);
        }
        return ((BAMT.PopulatedArray<T>)root).getOrElse(pos,alt);
    }

    @Override
    public T getOrElseGet(int pos, Supplier<? extends T> alt) {
        if(pos<0||pos>=size){
            return alt.get();
        }
        int tailStart = size-tail.size();
        if(pos>=tailStart){
            return tail.getOrElse(pos-tailStart,alt.get());
        }
        return ((BAMT.PopulatedArray<T>)root).getOrElse(pos,alt.get());
    }

    class VectorSome extends Vector<T> implements ImmutableList.Some<T>{

        public VectorSome(Vector<T> vec) {
            super(vec.root, vec.tail, vec.size);
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
        public Some<T> reverse() {
            ImmutableList<T> vec = Vector.this.reverse();
            Vector<T> rev = (Vector<T>)vec;
            return rev.new VectorSome(rev);
        }

        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head(),tail());
        }
    }

    static class VectorNone<T> implements ImmutableList.None<T>{
        static VectorNone Instance = new VectorNone();

        @Override
        public<R> Vector<R> unitIterable(Iterable<R> it){
            if(it instanceof Vector){
                return (Vector<R>)it;
            }
            return fromIterable(it);
        }

        public static <T> VectorNone<T> empty(){
            return Instance;
        }
        @Override
        public <R> ImmutableList<R> unitStream(Stream<R> stream) {
            return empty();
        }

        @Override
        public ImmutableList<T> emptyUnit() {
            return empty();
        }

        @Override
        public ImmutableList<T> drop(long num) {
            return empty();
        }

        @Override
        public ImmutableList<T> take(long num) {
            return empty();
        }

        @Override
        public ImmutableList<T> prepend(T value) {
            return empty();
        }

        @Override
        public ImmutableList<T> prependAll(Iterable<? extends T> value) {
            return empty();
        }

        @Override
        public ImmutableList<T> append(T value) {
            return empty();
        }

        @Override
        public ImmutableList<T> appendAll(Iterable<? extends T> value) {
            return empty();
        }

        @Override
        public ImmutableList<T> reverse() {
            return empty();
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
        public boolean isEmpty() {
            return true;
        }

        @Override
        public ReactiveSeq<T> stream() {
            return ReactiveSeq.empty();
        }

        @Override
        public ImmutableList<T> filter(Predicate<? super T> fn) {
            return empty();
        }

        @Override
        public <R> ImmutableList<R> map(Function<? super T, ? extends R> fn) {
            return empty();
        }

        @Override
        public <R> ImmutableList<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
            return empty();
        }

        @Override
        public <R> ImmutableList<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
            return empty();
        }

        @Override
        public ImmutableList<T> onEmpty(T value) {
            return Vector.of(value);
        }

        @Override
        public ImmutableList<T> onEmptyGet(Supplier<? extends T> supplier) {
            return Vector.of(supplier.get());
        }


        @Override
        public ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
            return supplier.get();
        }
    }

    @Override
    public String toString() {
        return stream().join(",","[","]");
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof PersistentIndexed) || o==null)
            return false;
        return equalToDirectAccess((Iterable<T>)o);

    }

    private int calcHash() {
        int hashCode = 1;
        for (T e : this)
            hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
        return hashCode;
    }
    @Override
    public int hashCode() {
       return hash.get();
    }
}
