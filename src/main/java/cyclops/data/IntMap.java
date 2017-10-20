package cyclops.data;


import com.aol.cyclops2.data.collections.extensions.api.PIndexed;
import com.aol.cyclops2.data.collections.extensions.api.PStack;
import com.aol.cyclops2.types.foldable.Evaluation;
import com.aol.cyclops2.types.traversable.IterableX;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.collectionx.immutable.VectorX;
import cyclops.control.Option;
import cyclops.control.lazy.Eval;
import cyclops.data.base.IntPatriciaTrie;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntMap<T> implements ImmutableList<T> {


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
    static <T> IntMap<T> generate(Generator<T> s){
        return fromStream(ReactiveSeq.generate(s));
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
    public IntMap<T> prependAll(Iterable<? extends T> value) {
        return unitStream(stream().prepend(value));
    }

    @Override
    public IntMap<T> append(T value) {
        return this.plus(value);
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
    public T getOrElse(int index,T value){
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

    public VectorX<T> vectorX(){
        return stream().to().vectorX(Evaluation.LAZY);
    }
    public ReactiveSeq<T> stream(){
        return intMap.stream();
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
        return unitStream(stream().flatMapI(fn));
    }

    @Override
    public <R> IntMap<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return unitStream(stream().flatMapI(fn));
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
    public <X extends Throwable> IntMap<T> onEmptyThrow(Supplier<? extends X> supplier) {
        if(isEmpty()){
            throw ExceptionSoftener.throwSoftenedException(supplier.get());
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
        return  fromStream(stream().filter(i-> !Objects.equals(i,value)));
    }


    @Override
    public IntMap<T> removeAt(int i) {
        if(i<0 || i>=size())
            return this;
        return  new IntMap<>(intMap.minus(i,i),size-1);
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

    class IntMapSome extends IntMap<T> implements ImmutableList.Some<T>, PStack<T>{

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
            return IntMap.of(value);
        }

        @Override
        public ImmutableList<T> onEmptyGet(Supplier<? extends T> supplier) {
            return IntMap.of(supplier.get());
        }

        @Override
        public <X extends Throwable> ImmutableList<T> onEmptyThrow(Supplier<? extends X> supplier) {
            throw ExceptionSoftener.throwSoftenedException(supplier.get());
        }

        @Override
        public ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
            return supplier.get();
        }

    }
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof PIndexed) || o==null)
            return false;
        return equalToDirectAccess((Iterable<T>)o);

    }

    @Override
    public String toString(){
        return stream().join(",","[","]");
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

