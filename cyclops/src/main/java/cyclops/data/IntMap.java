package cyclops.data;


import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentIndexed;
import com.oath.cyclops.types.persistent.PersistentList;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Eval;
import cyclops.control.Option;
import com.oath.cyclops.hkt.DataWitness.intMap;
import cyclops.data.base.IntPatriciaTrie;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.reactivestreams.Publisher;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntMap<T> implements ImmutableList<T>,Serializable,Higher<intMap,T> {


    @Override
    public<R> IntMap<R> unitIterable(Iterable<R> it){
        if(it instanceof IntMap){
            return (IntMap<R>)it;
        }
        return fromIterable(it);
    }


    static <T> IntMap<T> narrow(IntMap<? extends T> list){
        return (IntMap<T>)list;
    }

    private final IntPatriciaTrie.Node<T> intMap;
    private final int size;
    private final Eval<Integer> hash = Eval.later(()->calcHash());
    static <T> IntMap<T> fill(T t, int max){
        return IntMap.fromStream(ReactiveSeq.fill(t).take(max));
    }
    static <U, T> IntMap<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed,unfolder));
    }

    static <T> IntMap<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,pred,f));

    }
    static <T> IntMap<T> iterate(final T seed, final UnaryOperator<T> f,int max) {
        return fromStream(ReactiveSeq.iterate(seed,f).limit(max));

    }

    static <T, U> Tuple2<IntMap<T>, IntMap<U>> unzip(final LazySeq<Tuple2<T, U>> sequence) {
        return ReactiveSeq.unzip(sequence.stream()).transform((a, b)->Tuple.tuple(fromStream(a),fromStream(b)));
    }
    static <T> IntMap<T> generate(Supplier<T> s, int max){
        return fromStream(ReactiveSeq.generate(s).limit(max));
    }

    static IntMap<Integer> range(final int start, final int end) {
        return IntMap.fromStream(ReactiveSeq.range(start,end));

    }
    static IntMap<Integer> range(final int start, final int step, final int end) {
        return IntMap.fromStream(ReactiveSeq.range(start,step,end));

    }
    static IntMap<Long> rangeLong(final long start, final long step, final long end) {
        return IntMap.fromStream(ReactiveSeq.rangeLong(start,step,end));
    }


    static IntMap<Long> rangeLong(final long start, final long end) {
        return IntMap.fromStream(ReactiveSeq.rangeLong(start, end));

    }
    public static <T> IntMap<T> fromStream(Stream<T> it){
        return fromIterable(()->it.iterator());
    }


    public static <T> IntMap<T> fromIterable(Iterable<T> iterable){
        if(iterable instanceof IntMap){
            return (IntMap<T>)iterable;
        }
        Iterator<T> it = iterable.iterator();
        IntPatriciaTrie.Node<T> tree = IntPatriciaTrie.empty();
        int count = 0;
        while(it.hasNext()){
            T next = it.next();
            tree = tree.put(count,count,next);
            count++;
        }
        return new IntMap<T>(tree,count);
    }
    public static <T> IntMap<T> empty(){
        IntPatriciaTrie.Node<T> tree = IntPatriciaTrie.empty();

        return new IntMap<T>(tree,0);
    }

    public static <T> IntMap<T> of(T... values){
        IntPatriciaTrie.Node<T> tree = IntPatriciaTrie.empty();
        for(int i=0;i<values.length;i++){
            tree = tree.put(i,i,values[i]);
        }
        return new IntMap<T>(tree,values.length);
    }
    @Override
    public Iterator<T> iterator(){
        return stream().iterator();
    }
    public IntMap<T> plus(T value){
        return new IntMap<>(intMap.put(size,size,value),size+1);
    }

    @Override
    public <R> IntMap<R> unitStream(Stream<R> stream) {
        return fromStream(stream);
    }

    @Override
    public IntMap<T> emptyUnit() {
        return empty();
    }

    @Override
    public IntMap<T> drop(long num) {
        return unitStream(stream().drop(num));
    }

    @Override
    public IntMap<T> take(long num) {
        return unitStream(stream().take(num));
    }

    @Override
    public IntMap<T> prepend(T value) {
        return unitStream(stream().prepend(value));
    }

    @Override
    public IntMap<T> append(T value) {
        return plus(value);
    }

    @Override
    public IntMap<T> prependAll(Iterable<? extends T> value) {
        return unitStream(stream().prependAll(value));
    }



    @Override
    public IntMap<T> appendAll(Iterable<? extends T> value) {
        Iterator<? extends  T> it = value.iterator();
        IntPatriciaTrie.Node<T> tree = this.intMap;
        int count = size;
        while(it.hasNext()){
            T next = it.next();
            tree =tree.put(count,count,next);
            count++;
        }
        return new IntMap<T>(tree,count);
    }

    @Override
    public IntMap<T> reverse() {
        return unitStream(stream().reverse());
    }

    public Option<T> get(int index){
        return intMap.get(index,index);
    }
    public T getOrElse(int index, T value){
        return intMap.getOrElse(index,index,value);
    }

    @Override
    public T getOrElseGet(int pos, Supplier<? extends T> alt) {
        return intMap.getOrElseGet(pos,pos,alt);
    }

    int calcSize(){
        return intMap.size();
    }
    public int size(){
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size==0;
    }


    public ReactiveSeq<T> stream(){
        return intMap.stream();
    }

  @Override
  public <R1, R2, R3, R> IntMap<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (IntMap< R>) ImmutableList.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
  }

  @Override
  public <R1, R2, R3, R> IntMap<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (IntMap< R>) ImmutableList.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
  }

  @Override
  public <R1, R2, R> IntMap<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (IntMap< R>) ImmutableList.super.forEach3(iterable1,iterable2,yieldingFunction);
  }

  @Override
  public <R1, R2, R> IntMap<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (IntMap< R>) ImmutableList.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);
  }

  @Override
  public <R1, R> IntMap<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (IntMap< R>) ImmutableList.super.forEach2(iterable1,yieldingFunction);
  }

  @Override
  public <R1, R> IntMap<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (IntMap< R>) ImmutableList.super.forEach2(iterable1,filterFunction,yieldingFunction);
  }

    @Override
    public IntMap<T> filter(Predicate<? super T> fn) {
        return unitStream(stream().filter(fn));
    }

    @Override
    public <R> IntMap<R> map(Function<? super T, ? extends R> fn) {
        return unitStream(stream().map(fn));
    }

    @Override
    public <R> IntMap<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
        return unitStream(stream().concatMap(fn));
    }

    @Override
    public <R> IntMap<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return unitStream(stream().concatMap(fn));
    }

    @Override
    public <R> IntMap<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
      return unitStream(stream().mergeMap(fn));
    }

    @Override
    public <R> IntMap<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
      return unitStream(stream().mergeMap(maxConcurecy,fn));
    }

  @Override
    public IntMap<T> replaceFirst(T currentElement, T newElement) {
        return (IntMap<T>)ImmutableList.super.replaceFirst(currentElement,newElement);
    }

    @Override
    public IntMap<T> subList(int start, int end) {
        return (IntMap<T>)ImmutableList.super.subList(start,end);
    }

    @Override
    public <U> IntMap<U> ofType(Class<? extends U> type) {
        return (IntMap<U>)ImmutableList.super.ofType(type);
    }

    @Override
    public IntMap<T> filterNot(Predicate<? super T> predicate) {
        return (IntMap<T>)ImmutableList.super.filterNot(predicate);
    }

    @Override
    public IntMap<T> notNull() {
        return (IntMap<T>)ImmutableList.super.notNull();
    }

    @Override
    public IntMap<T> peek(Consumer<? super T> c) {
        return (IntMap<T>)ImmutableList.super.peek(c);
    }


    @Override
    public IntMap<T> removeStream(Stream<? extends T> stream) {
        return (IntMap<T>)ImmutableList.super.removeStream(stream);
    }

    @Override
    public IntMap<T> retainAll(Iterable<? extends T> it) {
        return (IntMap<T>)ImmutableList.super.retainAll(it);
    }

    @Override
    public IntMap<T> retainStream(Stream<? extends T> stream) {
        return (IntMap<T>)ImmutableList.super.retainStream(stream);
    }

    @Override
    public IntMap<T> retainAll(T... values) {
        return (IntMap<T>)ImmutableList.super.retainAll(values);
    }

    @Override
    public IntMap<ReactiveSeq<T>> permutations() {
        return (IntMap<ReactiveSeq<T>>)ImmutableList.super.permutations();
    }

    @Override
    public IntMap<ReactiveSeq<T>> combinations(int size) {
        return (IntMap<ReactiveSeq<T>>)ImmutableList.super.combinations(size);
    }

    @Override
    public IntMap<ReactiveSeq<T>> combinations() {
        return (IntMap<ReactiveSeq<T>>)ImmutableList.super.combinations();
    }

  @Override
    public <T2, R> IntMap<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return (IntMap<R>)ImmutableList.super.zip(fn, publisher);
    }

    @Override
    public <U, R> IntMap<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (IntMap<R>)ImmutableList.super.zipWithStream(other,zipper);
    }

    @Override
    public <U> IntMap<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (IntMap)ImmutableList.super.zipWithPublisher(other);
    }

    @Override
    public <U> IntMap<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (IntMap)ImmutableList.super.zip(other);
    }

    @Override
    public <S, U, R> IntMap<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (IntMap<R>)ImmutableList.super.zip3(second,third,fn3);
    }

    @Override
    public <T2, T3, T4, R> IntMap<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (IntMap<R>)ImmutableList.super.zip4(second,third,fourth,fn);
    }


    @Override
    public IntMap<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (IntMap<T>)ImmutableList.super.combine(predicate,op);
    }

    @Override
    public IntMap<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (IntMap<T>)ImmutableList.super.combine(op,predicate);
    }

    @Override
    public IntMap<T> cycle(long times) {
        return (IntMap<T>)ImmutableList.super.cycle(times);
    }

    @Override
    public IntMap<T> cycle(Monoid<T> m, long times) {
        return (IntMap<T>)ImmutableList.super.cycle(m,times);
    }

    @Override
    public IntMap<T> cycleWhile(Predicate<? super T> predicate) {
        return (IntMap<T>) ImmutableList.super.cycleWhile(predicate);
    }

    @Override
    public IntMap<T> cycleUntil(Predicate<? super T> predicate) {
        return (IntMap<T>) ImmutableList.super.cycleUntil(predicate);
    }

    @Override
    public <U, R> IntMap<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (IntMap<R>) ImmutableList.super.zip(other,zipper);
    }

    @Override
    public <S, U> IntMap<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (IntMap) ImmutableList.super.zip3(second,third);
    }

    @Override
    public <T2, T3, T4> IntMap<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (IntMap) ImmutableList.super.zip4(second,third,fourth);
    }

    @Override
    public IntMap<Tuple2<T, Long>> zipWithIndex() {
        return (IntMap<Tuple2<T,Long>>) ImmutableList.super.zipWithIndex();
    }

    @Override
    public IntMap<Seq<T>> sliding(int windowSize) {
        return (IntMap<Seq<T>>) ImmutableList.super.sliding(windowSize);
    }

    @Override
    public IntMap<Seq<T>> sliding(int windowSize, int increment) {
        return (IntMap<Seq<T>>) ImmutableList.super.sliding(windowSize,increment);
    }

    @Override
    public <C extends PersistentCollection<? super T>> IntMap<C> grouped(int size, Supplier<C> supplier) {
        return (IntMap<C>) ImmutableList.super.grouped(size,supplier);
    }

    @Override
    public IntMap<Vector<T>> groupedUntil(Predicate<? super T> predicate) {
        return (IntMap<Vector<T>>) ImmutableList.super.groupedUntil(predicate);
    }

    @Override
    public IntMap<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return (IntMap<Vector<T>>) ImmutableList.super.groupedUntil(predicate);
    }

    @Override
    public <U> IntMap<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {
        return (IntMap) ImmutableList.super.zipWithStream(other);
    }

    @Override
    public IntMap<Vector<T>> groupedWhile(Predicate<? super T> predicate) {
        return (IntMap<Vector<T>>) ImmutableList.super.groupedWhile(predicate);
    }

    @Override
    public <C extends PersistentCollection<? super T>> IntMap<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (IntMap<C>) ImmutableList.super.groupedWhile(predicate,factory);
    }

    @Override
    public <C extends PersistentCollection<? super T>> IntMap<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (IntMap<C>) ImmutableList.super.groupedUntil(predicate,factory);
    }

    @Override
    public IntMap<Vector<T>> grouped(int groupSize) {
        return (IntMap<Vector<T>>) ImmutableList.super.grouped(groupSize);
    }

    @Override
    public IntMap<T> distinct() {
        return (IntMap<T>) ImmutableList.super.distinct();
    }

    @Override
    public IntMap<T> scanLeft(Monoid<T> monoid) {
        return (IntMap<T>) ImmutableList.super.scanLeft(monoid);
    }

    @Override
    public <U> IntMap<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (IntMap<U>) ImmutableList.super.scanLeft(seed,function);
    }

    @Override
    public IntMap<T> scanRight(Monoid<T> monoid) {
        return (IntMap<T>) ImmutableList.super.scanRight(monoid);
    }

    @Override
    public <U> IntMap<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (IntMap<U>) ImmutableList.super.scanRight(identity,combiner);
    }

    @Override
    public IntMap<T> sorted() {
        return (IntMap<T>) ImmutableList.super.sorted();
    }

    @Override
    public IntMap<T> sorted(Comparator<? super T> c) {
        return (IntMap<T>) ImmutableList.super.sorted(c);
    }

    @Override
    public IntMap<T> takeWhile(Predicate<? super T> p) {
        return (IntMap<T>) ImmutableList.super.takeWhile(p);
    }

    @Override
    public IntMap<T> dropWhile(Predicate<? super T> p) {
        return (IntMap<T>) ImmutableList.super.dropWhile(p);
    }

    @Override
    public IntMap<T> takeUntil(Predicate<? super T> p) {
        return (IntMap<T>) ImmutableList.super.takeUntil(p);
    }

    @Override
    public IntMap<T> dropUntil(Predicate<? super T> p) {
        return (IntMap<T>) ImmutableList.super.dropUntil(p);
    }

    @Override
    public IntMap<T> dropRight(int num) {
        return (IntMap<T>) ImmutableList.super.dropRight(num);
    }

    @Override
    public IntMap<T> takeRight(int num) {
        return (IntMap<T>) ImmutableList.super.takeRight(num);
    }


    @Override
    public IntMap<T> intersperse(T value) {
        return (IntMap<T>) ImmutableList.super.intersperse(value);
    }

    @Override
    public IntMap<T> shuffle() {
        return (IntMap<T>) ImmutableList.super.shuffle();
    }

    @Override
    public IntMap<T> shuffle(Random random) {
        return (IntMap<T>) ImmutableList.super.shuffle(random);
    }

    @Override
    public IntMap<T> slice(long from, long to) {
        return (IntMap<T>) ImmutableList.super.slice(from,to);
    }


    @Override
    public IntMap<T> prependStream(Stream<? extends T> stream) {
        return (IntMap<T>) ImmutableList.super.prependStream(stream);
    }

    @Override
    public IntMap<T> appendAll(T... values) {
        return (IntMap<T>) ImmutableList.super.appendAll(values);
    }

    @Override
    public IntMap<T> prependAll(T... values) {
        return (IntMap<T>) ImmutableList.super.prependAll(values);
    }

    @Override
    public IntMap<T> deleteBetween(int start, int end) {
        return (IntMap<T>) ImmutableList.super.deleteBetween(start,end);
    }

    @Override
    public IntMap<T> insertStreamAt(int pos, Stream<T> stream) {
        return (IntMap<T>) ImmutableList.super.insertStreamAt(pos,stream);
    }



    @Override
    public <U extends Comparable<? super U>> IntMap<T> sorted(Function<? super T, ? extends U> function) {
        return (IntMap<T>) ImmutableList.super.sorted(function);
    }

    @Override
    public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
        return isEmpty() ? fn2.apply(IntMapNone.empty()) :  fn1.apply(new IntMapSome(this));
    }

    @Override
    public IntMap<T> onEmpty(T value) {
        if(isEmpty()){
            return of(value);
        }
        return this;

    }

    @Override
    public IntMap<T> onEmptyGet(Supplier<? extends T> supplier) {
        if(isEmpty()){
            return of(supplier.get());
        }
        return this;
    }


    @Override
    public ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
        if(isEmpty()){
            return supplier.get();
        }
        return this;
    }

    @Override
    public IntMap<T> removeValue(T value) {
        return removeFirst(e->Objects.equals(e,value));
    }

    @Override
    public IntMap<T> removeFirst(Predicate<? super T> pred) {
        return (IntMap<T>)ImmutableList.super.removeFirst(pred);
    }



    @Override
    public IntMap<T> removeAt(long pos) {
        int i = (int)pos;
        if(i<0 || i>=size())
            return this;
        return  new IntMap<>(intMap.minus(i,i),size-1);
    }

    @Override
    public IntMap<T> insertAt(int pos, T... values) {
        return (IntMap<T> )ImmutableList.super.insertAt(pos,values);
    }

    @Override
    public IntMap<T> insertAt(int pos, Iterable<? extends T> values) {
        return (IntMap<T> )ImmutableList.super.insertAt(pos,values);
    }

    @Override
    public IntMap<T> insertAt(int i, T value) {
        return (IntMap<T> )ImmutableList.super.insertAt(i,value);
    }

    class IntMapSome extends IntMap<T> implements ImmutableList.Some<T>, PersistentList<T> {

        public IntMapSome(IntMap<T> vec) {
            super(vec.intMap, vec.size);
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
        public IntMapSome reverse() {
            ImmutableList<T> vec = IntMap.this.reverse();
            IntMap rev = (IntMap)vec;
            return rev.new IntMapSome(rev);
        }

        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head(),tail());
        }

    }

    static class IntMapNone<T> implements ImmutableList.None<T>{
        static IntMap.IntMapNone Instance = new IntMap.IntMapNone();

        public static <T> IntMap.IntMapNone<T> empty(){
            return Instance;
        }
        @Override
        public <R> ImmutableList<R> unitStream(Stream<R> stream) {
            return empty();
        }

        @Override
        public<R> IntMap<R> unitIterable(Iterable<R> it){
            if(it instanceof IntMap){
                return (IntMap<R>)it;
            }
            return fromIterable(it);
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
        public ImmutableList<T> append(T value) {
            return of(value);
        }

        @Override
        public ImmutableList<T> prependAll(Iterable<? extends T> value) {
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
        public <R> ImmutableList<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn) {
            return empty();
        }

        @Override
        public <R> ImmutableList<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
          return empty();
        }

        @Override
        public <R> ImmutableList<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
          return empty();
        }

      @Override
        public ImmutableList<T> onEmpty(T value) {
            return IntMap.of(value);
        }

        @Override
        public ImmutableList<T> onEmptyGet(Supplier<? extends T> supplier) {
            return IntMap.of(supplier.get());
        }


        @Override
        public ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
            return supplier.get();
        }

    }
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof PersistentIndexed) || o==null)
            return false;
        return equalToDirectAccess((Iterable<T>)o);

    }

    @Override
    public String toString(){
        return stream().join(", ","[","]");
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
    private Object writeReplace() {
        return new Proxy(this);
    }
    private Object readResolve() throws InvalidObjectException {
        throw new InvalidObjectException("Use Serialization Proxy instead.");
    }
    @AllArgsConstructor
    private static final class Proxy<T> implements Serializable {

        private static final long serialVersionUID = 1L;
        IntMap<T> v;

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
            IntMap<T> res = empty();
            for (int i = 0; i < size; i++) {
                T n = (T) s.readObject();
                res =  res.append(n);
            }
            v=res;
        }
    }

}

