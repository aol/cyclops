package cyclops.data;

import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentQueue;
import com.oath.cyclops.hkt.Higher;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import com.oath.cyclops.hkt.DataWitness.bankersQueue;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public interface BankersQueue<T> extends ImmutableQueue<T>, Higher<bankersQueue,T>, Serializable {

    static <T> Collector<T, List<T>, BankersQueue<T>> collector() {
        Collector<T, ?, List<T>> c  = Collectors.toList();
        return Collectors.<T, List<T>, Iterable<T>,BankersQueue<T>>collectingAndThen((Collector)c,BankersQueue::fromIterable);
    }
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
    static <T> BankersQueue<T> fromIterator(Iterator<T> it){
      return fromIterable(()->it);
    }

    @Override
    default <R> BankersQueue<R> unitIterable(Iterable<R> it){
        return fromIterable(it);
    }

    default Tuple2<T,BankersQueue<T>> dequeue(T defaultValue){
        return foldBankersQueue(c->c.dequeue(), n->Tuple.tuple(defaultValue,this));
    }
    public static <T> BankersQueue<T> empty(){
        return Nil.Instance;
    }
    int size() ;

    @Override
    default boolean containsValue(T value) {
        return ImmutableQueue.super.containsValue(value);
    }



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
        return fromStream(stream().removeFirst(pred));
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
     <R> R foldBankersQueue(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil<T>, ? extends R> fn2);

    default BankersQueue<T> tail(){
        return foldBankersQueue(cons-> cons, nil->nil);
    }
    @Override
    default BankersQueue<T> drop(long num) {
        if(num<=0)
            return this;
        if(num>=size())
            return empty();
        return this.foldBankersQueue(c -> {
            ImmutableList<T> newFront = c.front.drop(num);
            ImmutableList<T> newBack = c.back.dropRight((int) num - c.front.size());
            if (newFront.size() > 0 || newBack.size() > 0)
                return new Cons<>(newFront, newBack);
            return empty();
        }, nil -> nil);
    }

    @Override
    default BankersQueue<T> take(long num) {
        return unitStream(stream().take(num));
    }


    @Override
    BankersQueue<T> prepend(T value);
    @Override
    default BankersQueue<T> prependAll(Iterable<? extends T> value){

            Iterator<? extends T> it = ReactiveSeq.fromIterable(value).reverse().iterator();
            BankersQueue<T> res= this;
            while(it.hasNext()){
                res = res.prepend(it.next());
            }
            return res;

    }

    @Override
    default Iterator<T> iterator() {
        return lazySeq().iterator();
    }

    @Override
    default BankersQueue<T> append(T value) {
        return enqueue(value);
    }

    @Override
    default BankersQueue<T> appendAll(Iterable<? extends T> value) {

        Iterator<? extends T> it = value.iterator();
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

    @Override
    default BankersQueue<T> minus() {
        return dequeue(null)._2();
    }

    @Override
    default BankersQueue<T> plusAll(Iterable<? extends T> list) {
        return appendAll(list);
    }

    @Override
    default BankersQueue<T> removeValue(T e) {
        return removeAll(e);
    }


    @Override
    default BankersQueue<T> removeAll(T... values) {
        return (BankersQueue<T>)ImmutableQueue.super.removeAll(values);
    }
    default BankersQueue<T> removeAll(Iterable<? extends T> it){
        return (BankersQueue<T>) ImmutableQueue.super.removeAll(it);
    }

    @Override
    default BankersQueue<T> plus(T value){
        return enqueue(value);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Cons<T> implements  BankersQueue<T>, ImmutableQueue.Some<T> {
        private static final long serialVersionUID = 1L;
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
            if(check.sizeBack<=check.sizeFront)
                return check;
           return new Cons((check.sizeFront + check.sizeBack), check.front.appendAll(check.back.reverse()), 0, LazySeq.empty());
        }

        @Override
        public int size() {
            return sizeFront + sizeBack;
        }

        @Override
        public boolean isEmpty() {
            return size()==0;
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
        public <R> BankersQueue<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn) {
            return check(new Cons(sizeFront,front.concatMap(fn),sizeBack,back.concatMap(fn)));

        }

        @Override
        public <R> BankersQueue<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
          return fromStream(stream().mergeMap(fn));
        }

        @Override
        public <R> BankersQueue<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
          return fromStream(stream().mergeMap(maxConcurecy,fn));
        }

      @Override
        public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn1.apply(this);
        }
        @Override
        public <R> R foldBankersQueue(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil<T>, ? extends R> fn2) {
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
        public ImmutableQueue<T> onEmptySwitch(Supplier<? extends ImmutableQueue<T>> supplier) {
            return this;
        }

        public Tuple2<T,BankersQueue<T>> dequeue() {

            return front.fold(cons->cons.fold((head, tail)->Tuple.tuple(head,tail()))
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
            ImmutableList<T> replaceF = front.replaceFirst(currentElement, newElement);
            ImmutableList<T> replaceB = back.replaceFirst(currentElement, newElement);
            return  front==replaceF && back==replaceB ? this : new Cons<>(replaceF, replaceB);
        }
       public Option<T> get(int n) {
            if(n<0)
                return Option.none();
           if (n < sizeFront)
               return front.get(n);
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
        public T getOrElseGet(int n, Supplier<? extends T> alt) {
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
            if(obj instanceof PersistentQueue) {
                return equalToIteration((Iterable)obj);
            }
            return false;
        }


        @Override
        public String toString(){
            return seq().toString();
        }



        @Override
        public Tuple2<T, ImmutableQueue<T>> unapply() {
            Tuple2<T, ImmutableQueue<T>> x = (Tuple2)dequeue();
            return x;
        }
    }
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public final class Nil<T> implements BankersQueue<T>,ImmutableQueue.None<T> {
        private static final long serialVersionUID = 1L;
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
        public <R> BankersQueue<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn) {
            return Instance;
        }

        @Override
        public <R> BankersQueue<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
          return Instance;
        }

        @Override
        public <R> BankersQueue<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
          return Instance;
        }

      @Override
        public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn2.apply(this);
        }
        @Override
        public <R> R foldBankersQueue(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil<T>, ? extends R> fn2) {
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
        public T getOrElseGet(int pos, Supplier<? extends T> alt) {
            return alt.get();
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PersistentQueue){
                return ((PersistentQueue)obj).size()==0;
            }
            return false;
        }

        @Override
        public String toString(){
            return seq().toString();
        }
        private Object readResolve() {
          return Instance;
       }
    }


    default BankersQueue<T> takeWhile(Predicate<? super T> p) {
        return (BankersQueue<T>)ImmutableQueue.super.takeWhile(p);
    }
    default BankersQueue<T> dropWhile(Predicate<? super T> p) {
        return (BankersQueue<T>)ImmutableQueue.super.dropWhile(p);
    }

    default Tuple2<BankersQueue<T>,BankersQueue<T>> duplicate(){
        return Tuple.tuple(this,this);
    }
    default <R1, R2> Tuple2<BankersQueue<R1>, BankersQueue<R2>> unzip(Function<? super T, Tuple2<? extends R1, ? extends R2>> fn) {
        Tuple2<BankersQueue<R1>, BankersQueue<Tuple2<? extends R1, ? extends R2>>> x = map(fn).duplicate().map1(s -> s.map(Tuple2::_1));
        return x.map2(s -> s.map(Tuple2::_2));
    }



    default <R> R foldLeft(R zero, BiFunction<R, ? super T, R> f){
        R acc= zero;
        for(T next : this){
            acc= f.apply(acc,next);
        }
        return acc;
    }
    @Override
    <R> BankersQueue<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn);

    @Override
    <R> BankersQueue<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn);

    @Override
    <R> BankersQueue<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn);

  @Override
    default <U> BankersQueue<U> ofType(Class<? extends U> type) {
        return (BankersQueue<U>)ImmutableQueue.super.ofType(type);
    }

    @Override
    default BankersQueue<T> filterNot(Predicate<? super T> predicate) {
        return (BankersQueue<T>)ImmutableQueue.super.filterNot(predicate);
    }

    @Override
    default BankersQueue<T> notNull() {
        return (BankersQueue<T>)ImmutableQueue.super.notNull();
    }

    @Override
    default BankersQueue<T> peek(Consumer<? super T> c) {
        return (BankersQueue<T>)ImmutableQueue.super.peek(c);
    }



    @Override
    default BankersQueue<T> removeStream(Stream<? extends T> stream) {
        return (BankersQueue<T>)ImmutableQueue.super.removeStream(stream);
    }

    @Override
    default BankersQueue<T> retainAll(Iterable<? extends T> it) {
        return (BankersQueue<T>)ImmutableQueue.super.retainAll(it);
    }

    @Override
    default BankersQueue<T> retainStream(Stream<? extends T> stream) {
        return (BankersQueue<T>)ImmutableQueue.super.retainStream(stream);
    }

    @Override
    default BankersQueue<T> retainAll(T... values) {
        return (BankersQueue<T>)ImmutableQueue.super.retainAll(values);
    }

    @Override
    default BankersQueue<ReactiveSeq<T>> permutations() {
        return (BankersQueue<ReactiveSeq<T>>)ImmutableQueue.super.permutations();
    }

    @Override
    default BankersQueue<ReactiveSeq<T>> combinations(int size) {
        return (BankersQueue<ReactiveSeq<T>>)ImmutableQueue.super.combinations(size);
    }

    @Override
    default BankersQueue<ReactiveSeq<T>> combinations() {
        return (BankersQueue<ReactiveSeq<T>>)ImmutableQueue.super.combinations();
    }

  @Override
    default <T2, R> BankersQueue<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return (BankersQueue<R>)ImmutableQueue.super.zip(fn, publisher);
    }

    @Override
    default <U, R> BankersQueue<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (BankersQueue<R>)ImmutableQueue.super.zipWithStream(other,zipper);
    }

    @Override
    default <U> BankersQueue<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (BankersQueue)ImmutableQueue.super.zipWithPublisher(other);
    }

    @Override
    default <U> BankersQueue<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (BankersQueue)ImmutableQueue.super.zip(other);
    }

    @Override
    default <S, U, R> BankersQueue<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (BankersQueue<R>)ImmutableQueue.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> BankersQueue<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (BankersQueue<R>)ImmutableQueue.super.zip4(second,third,fourth,fn);
    }

    @Override
    default BankersQueue<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (BankersQueue<T>)ImmutableQueue.super.combine(predicate,op);
    }

    @Override
    default BankersQueue<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (BankersQueue<T>)ImmutableQueue.super.combine(op,predicate);
    }

    @Override
    default BankersQueue<T> cycle(long times) {
        return (BankersQueue<T>)ImmutableQueue.super.cycle(times);
    }

    @Override
    default BankersQueue<T> cycle(Monoid<T> m, long times) {
        return (BankersQueue<T>)ImmutableQueue.super.cycle(m,times);
    }

    @Override
    default BankersQueue<T> cycleWhile(Predicate<? super T> predicate) {
        return (BankersQueue<T>) ImmutableQueue.super.cycleWhile(predicate);
    }

    @Override
    default BankersQueue<T> cycleUntil(Predicate<? super T> predicate) {
        return (BankersQueue<T>) ImmutableQueue.super.cycleUntil(predicate);
    }

    @Override
    default <U, R> BankersQueue<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (BankersQueue<R>) ImmutableQueue.super.zip(other,zipper);
    }

    @Override
    default <S, U> BankersQueue<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (BankersQueue) ImmutableQueue.super.zip3(second,third);
    }

    @Override
    default <T2, T3, T4> BankersQueue<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (BankersQueue) ImmutableQueue.super.zip4(second,third,fourth);
    }

    @Override
    default BankersQueue<Tuple2<T, Long>> zipWithIndex() {
        return (BankersQueue<Tuple2<T,Long>>) ImmutableQueue.super.zipWithIndex();
    }

    @Override
    default BankersQueue<Seq<T>> sliding(int windowSize) {
        return (BankersQueue<Seq<T>>) ImmutableQueue.super.sliding(windowSize);
    }

    @Override
    default BankersQueue<Seq<T>> sliding(int windowSize, int increment) {
        return (BankersQueue<Seq<T>>) ImmutableQueue.super.sliding(windowSize,increment);
    }

    @Override
    default <C extends PersistentCollection<? super T>> BankersQueue<C> grouped(int size, Supplier<C> supplier) {
        return (BankersQueue<C>) ImmutableQueue.super.grouped(size,supplier);
    }

    @Override
    default BankersQueue<Vector<T>> groupedUntil(Predicate<? super T> predicate) {
        return (BankersQueue<Vector<T>>) ImmutableQueue.super.groupedUntil(predicate);
    }

    @Override
    default BankersQueue<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return (BankersQueue<Vector<T>>) ImmutableQueue.super.groupedUntil(predicate);
    }

    @Override
    default <U> BankersQueue<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {
        return (BankersQueue) ImmutableQueue.super.zipWithStream(other);
    }

    @Override
    default BankersQueue<Vector<T>> groupedWhile(Predicate<? super T> predicate) {
        return (BankersQueue<Vector<T>>) ImmutableQueue.super.groupedWhile(predicate);
    }

    @Override
    default <C extends PersistentCollection<? super T>> BankersQueue<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (BankersQueue<C>) ImmutableQueue.super.groupedWhile(predicate,factory);
    }

    @Override
    default <C extends PersistentCollection<? super T>> BankersQueue<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (BankersQueue<C>) ImmutableQueue.super.groupedUntil(predicate,factory);
    }

    @Override
    default BankersQueue<Vector<T>> grouped(int groupSize) {
        return (BankersQueue<Vector<T>>) ImmutableQueue.super.grouped(groupSize);
    }

    @Override
    default BankersQueue<T> distinct() {
        return (BankersQueue<T>) ImmutableQueue.super.distinct();
    }

    @Override
    default BankersQueue<T> scanLeft(Monoid<T> monoid) {
        return (BankersQueue<T>) ImmutableQueue.super.scanLeft(monoid);
    }

    @Override
    default <U> BankersQueue<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (BankersQueue<U>) ImmutableQueue.super.scanLeft(seed,function);
    }

    @Override
    default BankersQueue<T> scanRight(Monoid<T> monoid) {
        return (BankersQueue<T>) ImmutableQueue.super.scanRight(monoid);
    }

    @Override
    default <U> BankersQueue<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (BankersQueue<U>) ImmutableQueue.super.scanRight(identity,combiner);
    }

    @Override
    default BankersQueue<T> sorted() {
        return (BankersQueue<T>) ImmutableQueue.super.sorted();
    }

    @Override
    default BankersQueue<T> sorted(Comparator<? super T> c) {
        return (BankersQueue<T>) ImmutableQueue.super.sorted(c);
    }



    @Override
    default BankersQueue<T> takeUntil(Predicate<? super T> p) {
        return (BankersQueue<T>) ImmutableQueue.super.takeUntil(p);
    }

    @Override
    default BankersQueue<T> dropUntil(Predicate<? super T> p) {
        return (BankersQueue<T>) ImmutableQueue.super.dropUntil(p);
    }

    @Override
    default BankersQueue<T> dropRight(int num) {
        return (BankersQueue<T>) ImmutableQueue.super.dropRight(num);
    }

    @Override
    default BankersQueue<T> takeRight(int num) {
        return (BankersQueue<T>) ImmutableQueue.super.takeRight(num);
    }



    @Override
    default BankersQueue<T> intersperse(T value) {
        return (BankersQueue<T>) ImmutableQueue.super.intersperse(value);
    }

    @Override
    default BankersQueue<T> shuffle() {
        return (BankersQueue<T>) ImmutableQueue.super.shuffle();
    }

    @Override
    default BankersQueue<T> shuffle(Random random) {
        return (BankersQueue<T>) ImmutableQueue.super.shuffle(random);
    }

    @Override
    default BankersQueue<T> slice(long from, long to) {
        return (BankersQueue<T>) ImmutableQueue.super.slice(from,to);
    }


    @Override
    default BankersQueue<T> prependStream(Stream<? extends T> stream) {
        return (BankersQueue<T>) ImmutableQueue.super.prependStream(stream);
    }

    @Override
    default BankersQueue<T> appendAll(T... values) {
        return (BankersQueue<T>) ImmutableQueue.super.appendAll(values);
    }

    @Override
    default BankersQueue<T> prependAll(T... values) {
        return (BankersQueue<T>) ImmutableQueue.super.prependAll(values);
    }

    @Override
    default BankersQueue<T> deleteBetween(int start, int end) {
        return (BankersQueue<T>) ImmutableQueue.super.deleteBetween(start,end);
    }

    @Override
    default BankersQueue<T> insertStreamAt(int pos, Stream<T> stream) {
        return (BankersQueue<T>) ImmutableQueue.super.insertStreamAt(pos,stream);
    }



    @Override
    default <U extends Comparable<? super U>> BankersQueue<T> sorted(Function<? super T, ? extends U> function) {
        return (BankersQueue<T>) ImmutableQueue.super.sorted(function);
    }
    default String mkString(){
        return stream().join(",","[","]");
    }


  @Override
  default <R1, R2, R3, R> BankersQueue<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (BankersQueue< R>) ImmutableQueue.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
  }

  @Override
  default <R1, R2, R3, R> BankersQueue<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (BankersQueue< R>) ImmutableQueue.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
  }

  @Override
  default <R1, R2, R> BankersQueue<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (BankersQueue< R>) ImmutableQueue.super.forEach3(iterable1,iterable2,yieldingFunction);
  }

  @Override
  default <R1, R2, R> BankersQueue<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (BankersQueue< R>) ImmutableQueue.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);
  }

  @Override
  default <R1, R> BankersQueue<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (BankersQueue< R>) ImmutableQueue.super.forEach2(iterable1,yieldingFunction);
  }

  @Override
  default <R1, R> BankersQueue<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (BankersQueue< R>) ImmutableQueue.super.forEach2(iterable1,filterFunction,yieldingFunction);
  }

    @Override
    default BankersQueue<T> removeAt(long pos) {
        return (BankersQueue<T>) ImmutableQueue.super.removeAt(pos);
    }

    @Override
    default BankersQueue<T> removeAt(int pos) {
        return (BankersQueue<T>) ImmutableQueue.super.removeAt(pos);
    }


    @Override
    default BankersQueue<T> updateAt(int pos, T value) {
        return (BankersQueue<T>) ImmutableQueue.super.updateAt(pos,value);
    }

    @Override
    default BankersQueue<T> insertAt(int pos, Iterable<? extends T> values) {
        return (BankersQueue<T>) ImmutableQueue.super.insertAt(pos,values);
    }

    @Override
    default BankersQueue<T> insertAt(int i, T value) {
        return (BankersQueue<T>) ImmutableQueue.super.insertAt(i,value);
    }

    @Override
    default BankersQueue<T> insertAt(int pos, T... values) {
        return (BankersQueue<T>) ImmutableQueue.super.insertAt(pos,values);
    }
}
