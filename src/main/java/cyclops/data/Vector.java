package cyclops.data;


import com.aol.cyclops2.types.foldable.Evaluation;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.collections.immutable.VectorX;
import cyclops.control.Maybe;
import cyclops.stream.ReactiveSeq;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@AllArgsConstructor
public class Vector<T> implements ImmutableList<T>{
    private final BAMT.NestedArray<T> root;
    private final BAMT.ActiveTail<T> tail;
    private final int size;

    public static <T> Vector<T> empty(){
        return new Vector<>(new BAMT.Zero<>(),BAMT.ActiveTail.emptyTail(),0);
    }
    public static <T> Vector<T> fromIterable(Iterable<T> it){
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
    public <X extends Throwable> Vector<T> onEmptyThrow(Supplier<? extends X> supplier) {
        if(size()!=0)
            return this;
        throw ExceptionSoftener.throwSoftenedException(supplier.get());
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
    public <R> ImmutableList<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
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
        return false;
    }

    public Vector<T> plus(T t){
        if(tail.size()<32) {
            return new Vector<T>(root,tail.append(t),size+1);
        }else{
            return new Vector<T>(root.append(tail),BAMT.ActiveTail.tail(t),size+1);
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
            return new Vector<T>(this.root,tail.dropRight(Math.max(32-(int)num,0)),(int)num);
        }
        return unitStream(stream().take(num));
    }

    @Override
    public ImmutableList<T> prepend(T value) {
        return unitStream(stream().prepend(value));
    }

    @Override
    public ImmutableList<T> prependAll(Iterable<T> value) {
        return unitStream(stream().prepend(value));
    }

    @Override
    public Vector<T> append(T value) {
        return plus(value);
    }

    @Override
    public Vector<T> appendAll(Iterable<T> value) {
        Vector<T> vec = this;

        for(T next : value){
            vec = vec.plus(next);
        }
        return vec;
    }

    @Override
    public ImmutableList<T> reverse() {
            return unitStream(stream().reverse());
    }

    public Maybe<T> get(int pos){
        if(pos<0||pos>=size){
            return Maybe.none();
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
    public T getOrElseGet(int pos, Supplier<T> alt) {
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
            return Vector.of(value);
        }

        @Override
        public ImmutableList<T> onEmptyGet(Supplier<? extends T> supplier) {
            return Vector.of(supplier.get());
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
