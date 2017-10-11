package cyclops.data;

import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.control.Option;
import cyclops.stream.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;


public interface BankersQueue<T> extends ImmutableQueue<T> {

    static <T> BankersQueue<T> fromStream(Stream<T> stream){
        return fromIterable(ReactiveSeq.fromStream(stream));
    }
    static <T> BankersQueue<T> fromIterable(Iterable<T> iterable){
        if(iterable instanceof BankersQueue)
            return (BankersQueue<T>)iterable;
        Iterator<T> it = iterable.iterator();
        BankersQueue<T> res = empty();
        while(it.hasNext()){
            res = res.enqueue(it.next());
        }
        return res;
    }
    default Tuple2<T,BankersQueue<T>> dequeue(T defaultValue){
        return visit(c->c.dequeue(),n->Tuple.tuple(defaultValue,this));
    }
    public static <T> BankersQueue<T> empty(){
        return Nil.Instance;
    }
    int size() ;
    boolean isEmpty();
    BankersQueue<T> enqueue(T value);
    <R> BankersQueue<R> map(Function<? super T, ? extends R> map);
    <R> BankersQueue<R> flatMap(Function<? super T, ? extends ImmutableQueue<? extends R>> fn);

    default Option<T> get(int n){
        return Option.none();
    }

    default ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(lazySeq());
    }

    @Override
    default BankersQueue<T> emptyUnit(){
        return empty();
    }

    @Override
    default <R> BankersQueue<R> unitStream(Stream<R> stream){
        return fromStream(stream);
    }

    BankersQueue<T> replace(T currentElement, T newElement);

    @Override
    default BankersQueue<T> removeFirst(Predicate<? super T> pred) {
        return fromStream(stream().filter(pred));
    }

    public static <T> BankersQueue<T> cons(T value){
        return new Cons<>(1, LazySeq.cons(value,()-> LazySeq.empty()),0, LazySeq.empty());
    }
    public static <T> BankersQueue<T> ofAll(ImmutableList<T> list){
        return new Cons<>(list.size(), list,0, list.emptyUnit());
    }
     LazySeq<T> lazySeq();


    static <T> BankersQueue<T> of(T... values) {
        BankersQueue<T> result = empty();
        for(T next : values){
            result = result.enqueue(next);
        }
        return result;
    }
     <R> R visit(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil<T>, ? extends R> fn2);

    default BankersQueue<T> tail(){
        return visit(cons-> cons,nil->nil);
    }
    @Override
    default BankersQueue<T> drop(long num) {
        BankersQueue<T> res= this;
        for(long i=0;i<num;i++){
            res = res.visit(c->c.tail(),nil->nil);

        }
        return  res;
    }

    @Override
    default BankersQueue<T> take(long num) {
        return unitStream(stream().take(num));
    }


    @Override
    BankersQueue<T> prepend(T value);
    @Override
    default BankersQueue<T> prependAll(Iterable<T> value){

            Iterator<T> it = value.iterator();
            BankersQueue<T> res= this;
            while(it.hasNext()){
                res = res.prepend(it.next());
            }
            return res;

    }
    @Override
    default BankersQueue<T> append(T value) {
        return enqueue(value);
    }

    @Override
    default BankersQueue<T> appendAll(Iterable<T> value) {

        Iterator<T> it = value.iterator();
        BankersQueue<T> res= this;
        while(it.hasNext()){
            res = res.enqueue(it.next());
        }
        return res;
    }

    @Override
    default BankersQueue<T> reverse() {
        return unitStream(stream().reverse());
    }

    @Override
    default BankersQueue<T> filter(Predicate<? super T> fn) {
        return unitStream(stream().filter(fn));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Cons<T> implements  BankersQueue<T>, ImmutableQueue.Some<T> {
        private final int sizeFront;
        private final ImmutableList<T> front;
        private final int sizeBack;
        private final ImmutableList<T> back;

        private Cons(ImmutableList<T> front, ImmutableList<T> back){
            this.sizeFront = front.size();
            this.sizeBack=back.size();
            this.front = front;
            this.back = back;
        }


       private static <T> BankersQueue<T> check(Cons<T> check) {
            if(check.sizeBack<check.sizeFront)
                return check;
           return new Cons((check.sizeFront + check.sizeBack), check.front.prependAll(check.back), 0, LazySeq.empty());
        }

        @Override
        public int size() {
            return sizeFront + sizeBack;
        }

        @Override
        public boolean isEmpty() {
            return size()>0;
        }



        @Override
        public BankersQueue<T> enqueue(T value) {
            return check(new Cons(sizeFront, front, sizeBack + 1, back.prepend(value)));
        }

        @Override
        public <R> BankersQueue<R> map(Function<? super T, ? extends R> map) {
            return check(new Cons(sizeFront,front.map(map),sizeBack,back.map(map)));
        }

        @Override
        public <R> BankersQueue<R> flatMap(Function<? super T, ? extends ImmutableQueue<? extends R>> fn) {
            return check(new Cons(sizeFront,front.flatMap(fn.andThen(q->q.lazySeq())),sizeBack,back.flatMap(fn.andThen(q->q.lazySeq()))));
        }

        @Override
        public <R> ImmutableQueue<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
            return check(new Cons(sizeFront,front.flatMapI(fn),sizeBack,back.flatMapI(fn)));

        }

        @Override
        public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn1.apply(this);
        }
        @Override
        public <R> R visit(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil<T>, ? extends R> fn2) {
            return fn1.apply(this);
        }

        @Override
        public ImmutableQueue<T> onEmpty(T value) {
            return this;
        }

        @Override
        public ImmutableQueue<T> onEmptyGet(Supplier<? extends T> supplier) {
            return this;
        }

        @Override
        public <X extends Throwable> ImmutableQueue<T> onEmptyThrow(Supplier<? extends X> supplier) {
            return this;
        }

        @Override
        public ImmutableQueue<T> onEmptySwitch(Supplier<? extends ImmutableQueue<T>> supplier) {
            return this;
        }

        public Tuple2<T,BankersQueue<T>> dequeue() {

            return front.fold(cons->cons.fold((head, tail)->Tuple.tuple(head,tail.fold(c->check(new Cons<>(sizeFront-1,tail,sizeBack,back)), n->Nil.Instance)))
                                 ,nil->{throw new RuntimeException("Unreachable!");});

        }
        public T head(){
            return front.fold(s->s.fold((h, t)->h), n->back.fold(s2->s2.fold( (h2, t2) ->h2), nil->{throw new RuntimeException("Unreachable!");}));
        }
        public BankersQueue<T> tail() {
            if(size()==1)
                return empty();
            if(sizeFront==0){
                return BankersQueue.ofAll(back.fold(s->s.fold((h, t)->t), n->n));
            }
            if(sizeFront==1){
                return ofAll(back);
            }
            return new BankersQueue.Cons<>(sizeFront-1,front.fold(s->s.fold((h, t)->t), n->n),sizeBack,back);



        }
        public BankersQueue<T> replace(T currentElement, T newElement) {
            ImmutableList<T> replaceF = front.replace(currentElement, newElement);
            ImmutableList<T> replaceB = back.replace(currentElement, newElement);
            return  front==replaceF && back==replaceB ? this : new Cons<>(replaceF, replaceB);
        }
       public Option<T> get(int n) {
           if (n < sizeFront)
               return front.get(sizeFront-n-1);
           else if (n < sizeFront + sizeBack) {
               int pos = n-sizeFront;
               return  back.get(sizeBack-pos-1);
           }
           return Option.none();

       }

        @Override
        public T getOrElse(int n, T alt) {
            if (n < sizeFront)
                return front.getOrElse(sizeFront-n-1,alt);
            else if (n < sizeFront + sizeBack) {
                int pos = n-sizeFront;
                return  back.getOrElse(sizeBack-pos-1,alt);
            }
            return alt;
        }

        @Override
        public T getOrElseGet(int n, Supplier<T> alt) {
            if (n < sizeFront)
                return front.getOrElse(sizeFront-n-1,alt.get());
            else if (n < sizeFront + sizeBack) {
                int pos = n-sizeFront;
                return  back.getOrElse(sizeBack-pos-1,alt.get());
            }
            return alt.get();
        }

        @Override
        public LazySeq<T> lazySeq() {
            return front.appendAll(back.reverse()).lazySeq();
        }

        @Override
        public BankersQueue<T> prepend(T value) {
            return new Cons<>(sizeFront+1,front.prepend(value),sizeBack,back);
        }


        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ImmutableQueue){
                ImmutableQueue<T> q = (ImmutableQueue<T>)obj;
                return q.linkdedListX().equals(linkdedListX());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return linkdedListX().hashCode();
        }

        public String toString(){
           return lazySeq().toString();
        }

        @Override
        public Tuple2<T, ImmutableQueue<T>> unapply() {
            Tuple2<T, ImmutableQueue<T>> x = (Tuple2)dequeue();
            return x;
        }
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class Nil<T> implements BankersQueue<T>,ImmutableQueue.None<T> {
        static Nil Instance = new Nil();

        public <R> R foldRight(R zero, BiFunction<? super T, ? super R, ? extends R> f) {
            return zero;
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
        public BankersQueue<T> enqueue(T value) {
            return cons(value);
        }

        @Override
        public <R> BankersQueue<R> map(Function<? super T, ? extends R> map) {
            return Instance;
        }

        @Override
        public <R> BankersQueue<R> flatMap(Function<? super T, ? extends ImmutableQueue<? extends R>> fn) {
            return Instance;
        }

        @Override
        public <R> ImmutableQueue<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
            return null;
        }
        @Override
        public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn2.apply(this);
        }
        @Override
        public <R> R visit(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil<T>, ? extends R> fn2) {
            return fn2.apply(this);
        }

        @Override
        public BankersQueue<T> prepend(T value) {
            return enqueue(value);
        }

        @Override
        public ImmutableQueue<T> onEmpty(T value) {
            return BankersQueue.of(value);
        }

        @Override
        public ImmutableQueue<T> onEmptyGet(Supplier<? extends T> supplier) {
            return BankersQueue.of(supplier.get());
        }

        @Override
        public <X extends Throwable> ImmutableQueue<T> onEmptyThrow(Supplier<? extends X> supplier) {
            throw ExceptionSoftener.throwSoftenedException(supplier.get());
        }

        @Override
        public ImmutableQueue<T> onEmptySwitch(Supplier<? extends ImmutableQueue<T>> supplier) {
            return supplier.get();
        }

        @Override
        public BankersQueue<T> replace(T currentElement, T newElement) {
            return this;
        }


        @Override
        public LazySeq<T> lazySeq() {
            return LazySeq.empty();
        }

        @Override
        public T getOrElse(int pos, T alt) {
            return alt;
        }

        @Override
        public T getOrElseGet(int pos, Supplier<T> alt) {
            return alt.get();
        }

    }

}
