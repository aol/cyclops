package cyclops.data;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.foldable.Evaluation;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.types.traversable.IterableX;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.collectionx.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.lazy.Trampoline;
import cyclops.control.Either;
import cyclops.control.anym.DataWitness.lazySeq;
import cyclops.function.Memoize;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import lombok.val;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

//safe LazyList (Stream) that does not support exceptional states
public interface LazySeq<T> extends  ImmutableList<T>,
                                    Folds<T>,
                                    Filters<T>,
                                    Transformable<T>,
                                    Higher<lazySeq,T> {


    static <R> LazySeq<R> narrow(LazySeq<? extends R> rs) {
        return (LazySeq<R>)rs;
    }
    default <R> LazySeq<R> unitStream(Stream<R> stream){
        return fromStream(stream);
    }

    default ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(this);
    }
    default LinkedListX<T> linkedListX(){
        return LinkedListX.fromIterable(this);
    }

    static  <T,R> LazySeq<R> tailRec(T initial, Function<? super T, ? extends LazySeq<? extends Either<T, R>>> fn) {
        LazySeq<Either<T, R>> next = LazySeq.of(Either.left(initial));

        boolean newValue[] = {true};
        for(;;){

            next = next.flatMap(e -> e.visit(s -> {
                        newValue[0]=true;
                        return fromStream(fn.apply(s).stream()); },
                    p -> {
                        newValue[0]=false;
                        return LazySeq.of(e);
                    }));
            if(!newValue[0])
                break;

        }
        ListX<R> x = Either.sequenceRight(next.stream().to().listX(Evaluation.LAZY)).orElse(ListX.empty());
        return LazySeq.fromIterator(x.iterator());
    }
    static <T> LazySeq<T> fill(T t){
        return LazySeq.fromStream(ReactiveSeq.fill(t));
    }
    static <U, T> LazySeq<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed,unfolder));
    }
    static <T> LazySeq<T> iterate(final T seed, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,f));

    }
    static <T> LazySeq<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,pred,f));

    }
    static <T> LazySeq<T> iterate(final T seed, final UnaryOperator<T> f,long times) {
        return fromStream(ReactiveSeq.iterate(seed,f).limit(times));

    }
    static <T> LazySeq<T> deferred(Supplier<? extends Iterable<? extends T>> lazy){
        return fromStream(ReactiveSeq.of(1).flatMapI(i->lazy.get()));
    }
    static <T, U> Tuple2<LazySeq<T>, LazySeq<U>> unzip(final LazySeq<Tuple2<T, U>> sequence) {
       return ReactiveSeq.unzip(sequence.stream()).transform((a, b)->Tuple.tuple(fromStream(a),fromStream(b)));
    }
    static <T> LazySeq<T> generate(Supplier<T> s){
        return fromStream(ReactiveSeq.generate(s));
    }
    static <T> LazySeq<T> generate(Supplier<T> s,long times){
        return fromStream(ReactiveSeq.generate(s).limit(times));
    }
    static <T> LazySeq<T> generate(Generator<T> s){
        return fromStream(ReactiveSeq.generate(s));
    }
     static LazySeq<Integer> range(final int start, final int end) {
        return LazySeq.fromStream(ReactiveSeq.range(start,end));

    }
    static LazySeq<Integer> range(final int start, final int step, final int end) {
       return LazySeq.fromStream(ReactiveSeq.range(start,step,end));

    }
    static LazySeq<Long> rangeLong(final long start, final long step, final long end) {
        return LazySeq.fromStream(ReactiveSeq.rangeLong(start,step,end));
    }


    static LazySeq<Long> rangeLong(final long start, final long end) {
        return LazySeq.fromStream(ReactiveSeq.rangeLong(start,end));

    }
    LazySeq<T> append(Supplier<LazySeq<T>> list);
    /**
     *
     * Stream over the values of an enum
     * <pre>
     *     {@code
     *     LazyList.enums(Days.class)
    .printOut();
     *     }
     *
     *     Monday
     *     Tuesday
     *     Wednesday
     *     Thursday
     *     Friday
     *     Saturday
     *     Sunday
     * </pre>
     *
     * @param c Enum to process
     * @param <E> Enum type
     * @return Stream over enum
     */
    static <E extends Enum<E>> LazySeq<E> enums(Class<E> c){
        return LazySeq.fromStream(ReactiveSeq.enums(c));
    }

    /**
     *
     * Stream over the values of an enum
     * <pre>
     *     {@code
     *     LazyList.enums(Days.class,Days.Wednesday)
    .printOut();
     *     }
     *
     *     Wednesday
     *     Thursday
     *     Friday
     *     Saturday
     *     Sunday
     * </pre>
     * @param c Enum to process
     * @param start Start value
     * @param <E> Enum type
     * @return Stream over enum
     */
    static <E extends Enum<E>> LazySeq<E> enums(Class<E> c, E start){
        return LazySeq.fromStream(ReactiveSeq.enums(c,start));
    }
    /**
     *
     * Stream over the values of an enum
     * <pre>
     *     {@code
     *     LazyList.enums(Days.class,Days.Wednesday,Days.Friday)
    .printOut();
     *     }
     *
     *     Wednesday
     *     Thursday
     *     Friday
     * </pre>
     * @param c Enum to process
     * @param start Start value
     * @param end End value
     * @param <E> Enum type
     * @return Stream over enum
     */
    static <E extends Enum<E>> LazySeq<E> enumsFromTo(Class<E> c, E start, E end){
       return LazySeq.fromStream(ReactiveSeq.enumsFromTo(c,start,end));
    }
    /**
     *
     * Stream over the values of an enum
     * <pre>
     *     {@code
     *     LazyList.enums(Days.class,Days.Monday,Days.Wednesday,Days.Friday)
    .printOut();
     *     }
     *     Monday
     *     Wednesday
     *     Friday
     * </pre>
     * @param c Enum to process
     * @param start Start value
     * @param step Values for which the Distance from start in terms of the enum ordinal determines the stepping function
     * @param end End value
     * @param <E> Enum type
     * @return Stream over enum
     */
    static <E extends Enum<E>> LazySeq<E> enums(Class<E> c, E start, E step, E end){
       return LazySeq.fromStream(ReactiveSeq.enums(c,start,step,end));

    }
    static <T> LazySeq<T> fromIterable(Iterable<T> it){
        if(it instanceof LazySeq)
            return (LazySeq<T>)it;
        return fromIterator(it.iterator());
    }
    static <T> LazySeq<T> fromIterator(Iterator<T> it){
        return it.hasNext() ? cons(it.next(), () -> fromIterator(it)) : empty();
    }
    static <T> LazySeq<T> fromStream(Stream<T> stream){
        Iterator<T> t = stream.iterator();
        return t.hasNext() ? cons(t.next(),()->fromIterator(t)) : empty();
    }
    static <T> LazySeq<T> of(T... value){
        LazySeq<T> result = empty();
        for(int i=value.length;i>0;i--){
            result = result.prepend(value[i-1]);
        }
        return result;
    }
    static <T> LazySeq<T> empty(){
        return Nil.Instance;
    }

    default Tuple2<LazySeq<T>, LazySeq<T>> span(Predicate<? super T> pred) {
        return Tuple.tuple(takeWhile(pred), dropWhile(pred));
    }
    default Tuple2<LazySeq<T>,LazySeq<T>> splitBy(Predicate<? super T> test) {
        return span(test.negate());
    }
    default LazySeq<LazySeq<T>> split(Predicate<? super T> test) {
        LazySeq<T> next = dropWhile(test);
        Tuple2<LazySeq<T>, LazySeq<T>> split = next.splitBy(test);
        return next.visit(c->cons(split._1(),()->split._2().split(test)),n->n);
    }
    default LazySeq<T> take(final long n) {
        if( n <= 0)
            return LazySeq.Nil.Instance;
        if(n<1000) {
            return this.visit(cons -> cons(cons.head, ()->cons.take(n - 1)), nil -> nil);
        }
        return fromStream(ReactiveSeq.fromIterable(this).take(n));

    }
    default LazySeq<T> takeWhile(Predicate<? super T> p) {
        return visit(c->{
            if(p.test(c.head())){
                return cons(c.head,()->c.tail.get().takeWhile(p));
            }else{
                return empty();
            }
        },n->this);
    }
    default LazySeq<T> dropWhile(Predicate<? super T> p) {
        LazySeq<T> current = this;
        boolean[] found = {false};
        while(!found[0] && !current.isEmpty()){
            LazySeq<T> active = current;
            current =  current.visit(c->{
                if(!p.test(c.head)){
                    found[0]=true;
                    return active;

                }
                return c.tail.get();

            },empty->empty);
        }
        return current;
    }
    default LazySeq<T> drop(final long num) {
        LazySeq<T> current = this;
        long pos = num;
        while (pos-- > 0 && !current.isEmpty()) {
            current = current.visit(c->c.tail.get(),nil->nil);
        }
        return current;
    }
    default LazySeq<T> reverse() {
        LazySeq<T> res = empty();
        for (T a : this) {
            res = res.prepend(a);
        }
        return res;
    }
    default Tuple2<LazySeq<T>,LazySeq<T>> duplicate(){
        return Tuple.tuple(this,this);
    }
    default <R1, R2> Tuple2<LazySeq<R1>, LazySeq<R2>> unzip(Function<? super T, Tuple2<? extends R1, ? extends R2>> fn) {
        Tuple2<LazySeq<R1>, LazySeq<Tuple2<? extends R1, ? extends R2>>> x = map(fn).duplicate().map1(s -> s.map(Tuple2::_1));
        return x.map2(s -> s.map(Tuple2::_2));
    }

    @Override
    default ImmutableList<T> emptyUnit(){
        return empty();
    }

    default LazySeq<T> replace(T currentElement, T newElement) {
        LazySeq<T> preceding = empty();
        LazySeq<T> tail = this;
        while(!tail.isEmpty()){
            LazySeq<T> ref=  preceding;
            LazySeq<T> tailRef = tail;
            Tuple3<LazySeq<T>, LazySeq<T>, Boolean> t3 = tail.visit(c -> {
                if (Objects.equals(c.head, currentElement))
                    return Tuple.tuple(ref, tailRef, true);
                return Tuple.tuple(ref.prepend(c.head), c.tail.get(), false);
            }, nil -> Tuple.tuple(ref, tailRef, true));

            preceding = t3._1();
            tail = t3._2();
            if(t3._3())
                break;

        }

        LazySeq<T> start = preceding;
        return tail.visit(cons->cons.tail.get().prepend(newElement).prependAll(start),nil->this);

    }


    default Option<T> get(int pos){
        T result = null;
        ImmutableList<T> l = this;
        for(int i=0;i<pos;i++){
           l = l.fold(c->c.tail(), n->n);
           if(l instanceof Nil){ //short circuit
               return Option.none();
           }
        }
        return Option.ofNullable(l.fold(c->c.head(), n->null));
    }


    default T getOrElse(int pos, T alt){
        T result = null;
        LazySeq<T> l = this;
        for(int i=0;i<pos;i++){
            l = l.visit(c->c.tail.get(),n->n);
            if(l instanceof Nil){ //short circuit
                return alt;
            }
        }
        return l.visit(c->c.head,n->null);
    }
    default T getOrElseGet(int pos, Supplier<T> alt){
        T result = null;
        LazySeq<T> l = this;
        for(int i=0;i<pos;i++){
            l = l.visit(c->c.tail.get(),n->n);
            if(l instanceof Nil){ //short circuit
                return alt.get();
            }
        }
        return l.visit(c->c.head,n->null);
    }
    default LazySeq<T> prepend(T value){
        return cons(value,()->this);
    }

    @Override
    default LazySeq<T> removeFirst(Predicate<? super T> pred) {
        return (LazySeq<T>)ImmutableList.super.removeFirst(pred);
    }

    @Override
    default LazySeq<T> plus(T value) {
        return prepend(value);
    }

    default LazySeq<T> prependAll(Iterable<T> it){
        LazySeq<T> value = fromIterable(it);
        return value.fold(cons->
                        cons.foldRight(this,(a,b)->b.prepend(a))
                ,nil->this);
    }
    default LazySeq<T> append(T append) {
        return appendAll(LazySeq.of(append));

    }


    default LazySeq<T> appendAll(Iterable<T> it) {
        LazySeq<T> append = fromIterable(it);
        return this.visit(cons->{
            return append.visit(c2->{
                return cons(cons.head,()->cons.tail.get().appendAll(append));
            },n2->this);
        },nil->append);

    }
    default <R> R foldLeft(R zero, BiFunction<R, ? super T, R> f){
        R acc= zero;
        for(T next : this){
            acc= f.apply(acc,next);
        }
        return acc;
    }

    default String mkString(){
        return stream().join(",","[","]");
    }

    <R> R foldRight(R zero, BiFunction<? super T, ? super R, ? extends R> f);

    default LazySeq<T> filter(Predicate<? super T> pred){
        return foldRight(empty(),(a,l)->{
            if(pred.test(a)){
                return l.prepend(a);
            }
            return l;
        });
    }
    default <R> LazySeq<R> map(Function<? super T, ? extends R> fn) {
        return foldRight(empty(), (a, l) -> l.prepend(fn.apply(a)));
    }

    default <R> LazySeq<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
        return this.visit(cons->{
            LazySeq<R> l1 = LazySeq.narrow(LazySeq.fromIterable(fn.apply(cons.head)));
            return l1.appendAll(cons.tail.get().flatMap(a -> fn.apply(a)));
        },nil->empty());
    }



    default <R> LazySeq<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return this.visit(cons->{
            LazySeq<R> l1 = LazySeq.narrow(LazySeq.fromIterable(fn.apply(cons.head)));
            return l1.appendAll(cons.tail.get().flatMap(a -> fromIterable(fn.apply(a))));
        },nil->empty());
    }
    <R> R visit(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2);
    LazySeq<T> cycle();
    int size();

    boolean isEmpty();
    static <T> LazySeq<T> lazy(T head, Supplier<ImmutableList<T>> tail) {
        return Cons.cons(head,()->tail.get().lazySeq());
    }
    static <T> LazySeq<T> cons(T head, Supplier<LazySeq<T>> tail) {
        return Cons.cons(head,tail);
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Cons<T>  implements LazySeq<T>, ImmutableList.Some<T> {

        public final T head;
        public final Supplier<LazySeq<T>> tail;

        public static <T> Cons<T> cons(T value, Supplier<LazySeq<T>> tail){
            return new Cons<>(value,Memoize.memoizeSupplier(tail));
        }

        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head,tail.get());
        }
        public boolean isEmpty(){
            return false;
        }

        public <R> R foldRight(R zero,BiFunction<? super T, ? super R, ? extends R> f) {
            class Step{
                public Trampoline<R> loop(ImmutableList<T> s, Function<? super R, ? extends Trampoline<R>> fn){

                    return s.fold(c-> Trampoline.more(()->loop(c.tail(), rem -> Trampoline.more(() -> fn.apply(f.apply(c.head(), rem))))), n->fn.apply(zero));

                }
            }
            return new Step().loop(this,i-> Trampoline.done(i)).result();
        }
        public <R> LazySeq<R> scanRight(R zero,BiFunction<? super T, ? super R, ? extends R> f) {
            Tuple2<R, LazySeq<R>> t2 = Tuple.tuple(zero, LazySeq.of(zero));
            return  foldRight(t2, (a, b) -> {
                R b2 = f.apply(a, b._1());
                return Tuple.tuple(b2, LazySeq.cons(b2, ()->b._2()));
            })._2();
        }

        @Override
        public Cons<T> cycle() {
            return append(()->this);
        }


        public LazySeq<T> tail(){
            return tail.get();
        }
        public T head(){
            return head;
        }
        public int size(){
            int result =1;
            ImmutableList<T> current[] = new LazySeq[1];
            current[0]=tail.get();
            while(true){
               int toAdd =current[0].fold(c->{
                    current[0]=c.tail();
                    return 1;
                },n->0);
                result+=toAdd;
                if(toAdd==0)
                    break;
            }
            return result;
        }
        @Override
        public Cons<T> append(Supplier<LazySeq<T>> list) {
            return cons(head,()->tail.get().append(list));
        }

        @Override
        public Cons<T> reverse() {
            return (Cons<T>)LazySeq.super.reverse();
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
            if(obj instanceof ImmutableList) {
                return equalToIteration((Iterable)obj);
            }
            return false;
        }

        @Override
        public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn1.apply(this);
        }

        @Override
        public Cons<T> onEmpty(T value) {
            return this;
        }

        @Override
        public Cons<T> onEmptyGet(Supplier<? extends T> supplier) {
            return this;
        }

        @Override
        public <X extends Throwable> Cons<T> onEmptyThrow(Supplier<? extends X> supplier) {
            return this;
        }

        @Override
        public Cons<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
            return this;
        }

        public <R> R visit(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2) {
            return fn1.apply(this);
        }
        public String toString(){
            return "{"+head+"...}";
        }
    }

    public class Nil<T> implements LazySeq<T>, ImmutableList.None<T> {
        static Nil Instance = new Nil();

        @Override
        public LazySeq<T> append(Supplier<LazySeq<T>> list) {
            return list.get();
        }

        @Override
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
        public <R> R fold(Function<? super Some<T>, ? extends R> fn1, Function<? super None<T>, ? extends R> fn2) {
            return fn2.apply(this);
        }

        @Override
        public ImmutableList<T> onEmpty(T value) {
            return LazySeq.of(value);
        }

        @Override
        public ImmutableList<T> onEmptyGet(Supplier<? extends T> supplier) {
            return LazySeq.of(supplier.get());
        }

        @Override
        public <X extends Throwable> ImmutableList<T> onEmptyThrow(Supplier<? extends X> supplier) {
            throw ExceptionSoftener.throwSoftenedException(supplier.get());
        }

        @Override
        public ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
            return supplier.get();
        }

        public <R> R visit(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2) {
            return fn2.apply(this);
        }
        public String toString(){
            return mkString();
        }


        public Nil<T> cycle(){
            return this;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj==null)
                return false;
            if (obj == this)
                return true;

            if(obj instanceof ImmutableList) {
                return ((ImmutableList)obj).size()==0;
            }
            return false;
        }
    }

}
