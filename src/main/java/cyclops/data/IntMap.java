package cyclops.data;


import com.aol.cyclops2.types.foldable.Evaluation;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.collections.immutable.VectorX;
import cyclops.control.Maybe;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntMap<T> implements ImmutableList<T>{


    private final IntPatriciaTrie.Node<T> intMap;
    private final int size;

    public static <T> IntMap<T> fromStream(Stream<T> seq){
        return fromIterable(ReactiveSeq.fromStream(seq));
    }

    public static <T> IntMap<T> fromIterable(Iterable<T> iterable){
        Iterator<T> it = iterable.iterator();
        IntPatriciaTrie.Node<T> tree = IntPatriciaTrie.empty();
        int count = 0;
        while(it.hasNext()){
            T next = it.next();
            tree.put(count,count,next);
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
    public IntMap<T> prependAll(Iterable<T> value) {
        return unitStream(stream().prepend(value));
    }

    @Override
    public IntMap<T> append(T value) {
        return this.plus(value);
    }

    @Override
    public IntMap<T> appendAll(Iterable<T> value) {
        Iterator<T> it = value.iterator();
        IntPatriciaTrie.Node<T> tree = this.intMap;
        int count = 0;
        while(it.hasNext()){
            T next = it.next();
            tree.put(count,count,next);
            count++;
        }
        return new IntMap<T>(tree,count+size);
    }

    @Override
    public IntMap<T> reverse() {
        return unitStream(stream().reverse());
    }

    public Maybe<T> get(int index){
        return intMap.get(index,index);
    }
    public T getOrElse(int index,T value){
        return intMap.getOrElse(index,index,value);
    }

    @Override
    public T getOrElseGet(int pos, Supplier<T> alt) {
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
    public <X extends Throwable> ImmutableList<T> onEmptyThrow(Supplier<? extends X> supplier) {
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

    class IntMapSome extends IntMap<T> implements ImmutableList.Some<T>{

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
        public ImmutableList<T> prependAll(Iterable<T> value) {
            return empty();
        }

        @Override
        public ImmutableList<T> append(T value) {
            return empty();
        }

        @Override
        public ImmutableList<T> appendAll(Iterable<T> value) {
            return empty();
        }

        @Override
        public ImmutableList<T> reverse() {
            return empty();
        }

        @Override
        public Maybe<T> get(int pos) {
            return Maybe.none();
        }

        @Override
        public T getOrElse(int pos, T alt) {
            return alt;
        }

        @Override
        public T getOrElseGet(int pos, Supplier<T> alt) {
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

}

