package cyclops.data;


import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.persistent.PersistentIndexed;
import com.aol.cyclops2.types.persistent.PersistentList;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.foldable.Evaluation;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.collectionx.immutable.VectorX;
import cyclops.collectionx.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.control.Either;
import cyclops.control.anym.DataWitness.lazySeq;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

//safe LazyList (Stream) that does not support exceptional states
public interface LazySeq<T> extends  ImmutableList<T>,
                                    Folds<T>,
                                    Filters<T>,
                                    Transformable<T>,
                                    Higher<lazySeq,T> {


    @Override
    default<R> LazySeq<R> unitIterable(Iterable<R> it){
        if(it instanceof LazySeq){
            return (LazySeq<R>)it;
        }
        return fromIterable(it);
    }

    @Override
    default boolean containsValue(T value) {
        return ImmutableList.super.containsValue(value);
    }
    static <R> LazySeq<R> narrow(LazySeq<? extends R> rs) {
        return (LazySeq<R>)rs;
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


    @Override
    default LazySeq<T> removeAll(Iterable<? extends T> list) {
        return (LazySeq<T>)ImmutableList.super.removeAllI(list);
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
    LazySeq<T> append(Supplier<LazySeq<T>> list);
    @Override
    default LazySeq<T> removeAll(T... values) {
        return (LazySeq<T>)ImmutableList.super.removeAll(values);
    }


    default LazySeq<T> plusAll(Iterable<? extends T> t){
        return prependAll((Iterable<T>)t);
    }

    @Override
    default LazySeq<T> insertAt(int pos, Iterable<? extends T> list) {
        return (LazySeq<T>)ImmutableList.super.insertAt(pos,list);
    }

    LazySeq<T> updateAt(int i, T value);
    LazySeq<T> removeAt(final int i);

    default LazySeq<T> insertAt(final int i, final T e){
        return (LazySeq<T>)ImmutableList.super.insertAt(i,e);
    }

    @Override
    default LazySeq<T> removeValue(T value) {
        return (LazySeq<T>) ImmutableList.super.removeValue(value);
    }

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
        if(pos<0)
            return Option.none();
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
        if(pos<0)
            return alt;
        T result = null;
        LazySeq<T> l = this;
        for(int i=0;i<pos;i++){
            l = l.visit(c->c.tail.get(),n->n);
            if(l instanceof Nil){ //short circuit
                return alt;
            }
        }
        return l.visit(c->c.head,n->alt);
    }
    default T getOrElseGet(int pos, Supplier<? extends T> alt){
        if(pos<0)
            return alt.get();
        T result = null;
        LazySeq<T> l = this;
        for(int i=0;i<pos;i++){
            l = l.visit(c->c.tail.get(),n->n);
            if(l instanceof Nil){ //short circuit
                return alt.get();
            }
        }
        return l.visit(c->c.head,n->alt.get());
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

    default LazySeq<T> prependAll(Iterable<? extends T> it){
        LazySeq<T> value = narrow(fromIterable(it));
        return value.fold(cons->
                        cons.foldRight(this,(a,b)->b.prepend(a))
                ,nil->this);
    }
    default LazySeq<T> append(T append) {
        return appendAll(LazySeq.of(append));

    }


    default LazySeq<T> appendAll(Iterable<? extends T> it) {
        LazySeq<T> append = narrow(fromIterable(it));
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


    @Override
    default LazySeq<T> subList(int start, int end) {
        return (LazySeq<T>)ImmutableList.super.subList(start,end);
    }

    @Override
    default <U> LazySeq<U> ofType(Class<? extends U> type) {
        return (LazySeq<U>)ImmutableList.super.ofType(type);
    }

    @Override
    default LazySeq<T> filterNot(Predicate<? super T> predicate) {
        return (LazySeq<T>)ImmutableList.super.filterNot(predicate);
    }

    @Override
    default LazySeq<T> notNull() {
        return (LazySeq<T>)ImmutableList.super.notNull();
    }

    @Override
    default LazySeq<T> peek(Consumer<? super T> c) {
        return (LazySeq<T>)ImmutableList.super.peek(c);
    }

    @Override
    default <R> LazySeq<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (LazySeq<R>)ImmutableList.super.trampoline(mapper);
    }

    @Override
    default LazySeq<T> removeAllS(Stream<? extends T> stream) {
        return (LazySeq<T>)ImmutableList.super.removeAllS(stream);
    }

    @Override
    default LazySeq<T> retainAllI(Iterable<? extends T> it) {
        return (LazySeq<T>)ImmutableList.super.retainAllI(it);
    }

    @Override
    default LazySeq<T> retainAllS(Stream<? extends T> stream) {
        return (LazySeq<T>)ImmutableList.super.retainAllS(stream);
    }

    @Override
    default LazySeq<T> retainAll(T... values) {
        return (LazySeq<T>)ImmutableList.super.retainAll(values);
    }

    @Override
    default LazySeq<ReactiveSeq<T>> permutations() {
        return (LazySeq<ReactiveSeq<T>>)ImmutableList.super.permutations();
    }

    @Override
    default LazySeq<ReactiveSeq<T>> combinations(int size) {
        return (LazySeq<ReactiveSeq<T>>)ImmutableList.super.combinations(size);
    }

    @Override
    default LazySeq<ReactiveSeq<T>> combinations() {
        return (LazySeq<ReactiveSeq<T>>)ImmutableList.super.combinations();
    }

    @Override
    default LazySeq<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (LazySeq<T>)ImmutableList.super.zip(combiner,app);
    }

    @Override
    default <R> LazySeq<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (LazySeq<R>)ImmutableList.super.zipWith(fn);
    }

    @Override
    default <R> LazySeq<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (LazySeq<R>)ImmutableList.super.zipWithS(fn);
    }

    @Override
    default <R> LazySeq<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (LazySeq<R>)ImmutableList.super.zipWithP(fn);
    }

    @Override
    default <T2, R> LazySeq<R> zipP(Publisher<? extends T2> publisher, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (LazySeq<R>)ImmutableList.super.zipP(publisher,fn);
    }

    @Override
    default <U, R> LazySeq<R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (LazySeq<R>)ImmutableList.super.zipS(other,zipper);
    }

    @Override
    default <U> LazySeq<Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return (LazySeq)ImmutableList.super.zipP(other);
    }

    @Override
    default <U> LazySeq<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (LazySeq)ImmutableList.super.zip(other);
    }

    @Override
    default <S, U, R> LazySeq<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (LazySeq<R>)ImmutableList.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> LazySeq<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (LazySeq<R>)ImmutableList.super.zip4(second,third,fourth,fn);
    }

    @Override
    default <U> LazySeq<U> unitIterator(Iterator<U> it) {
        return fromIterable(()->it);
    }

    @Override
    default LazySeq<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (LazySeq<T>)ImmutableList.super.combine(predicate,op);
    }

    @Override
    default LazySeq<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (LazySeq<T>)ImmutableList.super.combine(op,predicate);
    }

    @Override
    default LazySeq<T> cycle(long times) {
        return (LazySeq<T>)ImmutableList.super.cycle(times);
    }

    @Override
    default LazySeq<T> cycle(Monoid<T> m, long times) {
        return (LazySeq<T>)ImmutableList.super.cycle(m,times);
    }

    @Override
    default LazySeq<T> cycleWhile(Predicate<? super T> predicate) {
        return (LazySeq<T>) ImmutableList.super.cycleWhile(predicate);
    }

    @Override
    default LazySeq<T> cycleUntil(Predicate<? super T> predicate) {
        return (LazySeq<T>) ImmutableList.super.cycleUntil(predicate);
    }

    @Override
    default <U, R> LazySeq<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (LazySeq<R>) ImmutableList.super.zip(other,zipper);
    }

    @Override
    default <S, U> LazySeq<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (LazySeq) ImmutableList.super.zip3(second,third);
    }

    @Override
    default <T2, T3, T4> LazySeq<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (LazySeq) ImmutableList.super.zip4(second,third,fourth);
    }

    @Override
    default LazySeq<Tuple2<T, Long>> zipWithIndex() {
        return (LazySeq<Tuple2<T,Long>>) ImmutableList.super.zipWithIndex();
    }

    @Override
    default LazySeq<VectorX<T>> sliding(int windowSize) {
        return (LazySeq<VectorX<T>>) ImmutableList.super.sliding(windowSize);
    }

    @Override
    default LazySeq<VectorX<T>> sliding(int windowSize, int increment) {
        return (LazySeq<VectorX<T>>) ImmutableList.super.sliding(windowSize,increment);
    }

    @Override
    default <C extends Collection<? super T>> LazySeq<C> grouped(int size, Supplier<C> supplier) {
        return (LazySeq<C>) ImmutableList.super.grouped(size,supplier);
    }

    @Override
    default LazySeq<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return (LazySeq<ListX<T>>) ImmutableList.super.groupedUntil(predicate);
    }

    @Override
    default LazySeq<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return (LazySeq<ListX<T>>) ImmutableList.super.groupedStatefullyUntil(predicate);
    }

    @Override
    default <U> LazySeq<Tuple2<T, U>> zipS(Stream<? extends U> other) {
        return (LazySeq) ImmutableList.super.zipS(other);
    }

    @Override
    default LazySeq<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return (LazySeq<ListX<T>>) ImmutableList.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> LazySeq<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (LazySeq<C>) ImmutableList.super.groupedWhile(predicate,factory);
    }

    @Override
    default <C extends Collection<? super T>> LazySeq<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (LazySeq<C>) ImmutableList.super.groupedUntil(predicate,factory);
    }

    @Override
    default LazySeq<ListX<T>> grouped(int groupSize) {
        return (LazySeq<ListX<T>>) ImmutableList.super.grouped(groupSize);
    }

    @Override
    default LazySeq<T> distinct() {
        return (LazySeq<T>) ImmutableList.super.distinct();
    }

    @Override
    default LazySeq<T> scanLeft(Monoid<T> monoid) {
        return (LazySeq<T>) ImmutableList.super.scanLeft(monoid);
    }

    @Override
    default <U> LazySeq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (LazySeq<U>) ImmutableList.super.scanLeft(seed,function);
    }

    @Override
    default LazySeq<T> scanRight(Monoid<T> monoid) {
        return (LazySeq<T>) ImmutableList.super.scanRight(monoid);
    }

    @Override
    default <U> LazySeq<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (LazySeq<U>) ImmutableList.super.scanRight(identity,combiner);
    }

    @Override
    default LazySeq<T> sorted() {
        return (LazySeq<T>) ImmutableList.super.sorted();
    }

    @Override
    default LazySeq<T> sorted(Comparator<? super T> c) {
        return (LazySeq<T>) ImmutableList.super.sorted(c);
    }



    @Override
    default LazySeq<T> takeUntil(Predicate<? super T> p) {
        return (LazySeq<T>) ImmutableList.super.takeUntil(p);
    }

    @Override
    default LazySeq<T> dropUntil(Predicate<? super T> p) {
        return (LazySeq<T>) ImmutableList.super.dropUntil(p);
    }

    @Override
    default LazySeq<T> dropRight(int num) {
        return (LazySeq<T>) ImmutableList.super.dropRight(num);
    }

    @Override
    default LazySeq<T> takeRight(int num) {
        return (LazySeq<T>) ImmutableList.super.takeRight(num);
    }

    @Override
    default LazySeq<T> skip(long num) {
        return (LazySeq<T>) ImmutableList.super.skip(num);
    }

    @Override
    default LazySeq<T> skipWhile(Predicate<? super T> p) {
        return (LazySeq<T>) ImmutableList.super.skipWhile(p);
    }

    @Override
    default LazySeq<T> skipUntil(Predicate<? super T> p) {
        return (LazySeq<T>) ImmutableList.super.skipUntil(p);
    }

    @Override
    default LazySeq<T> limit(long num) {
        return (LazySeq<T>) ImmutableList.super.limit(num);
    }

    @Override
    default LazySeq<T> limitWhile(Predicate<? super T> p) {
        return (LazySeq<T>) ImmutableList.super.limitWhile(p);
    }

    @Override
    default LazySeq<T> limitUntil(Predicate<? super T> p) {
        return (LazySeq<T>) ImmutableList.super.limitUntil(p);
    }

    @Override
    default LazySeq<T> intersperse(T value) {
        return (LazySeq<T>) ImmutableList.super.intersperse(value);
    }

    @Override
    default LazySeq<T> shuffle() {
        return (LazySeq<T>) ImmutableList.super.shuffle();
    }

    @Override
    default LazySeq<T> skipLast(int num) {
        return (LazySeq<T>) ImmutableList.super.skipLast(num);
    }

    @Override
    default LazySeq<T> limitLast(int num) {
        return (LazySeq<T>) ImmutableList.super.limitLast(num);
    }

    @Override
    default LazySeq<T> shuffle(Random random) {
        return (LazySeq<T>) ImmutableList.super.shuffle(random);
    }

    @Override
    default LazySeq<T> slice(long from, long to) {
        return (LazySeq<T>) ImmutableList.super.slice(from,to);
    }

    @Override
    LazySeq<T> onEmpty(T value);

    @Override
    LazySeq<T> onEmptyGet(Supplier<? extends T> supplier);

    @Override
    <X extends Throwable> LazySeq<T> onEmptyThrow(Supplier<? extends X> supplier);

    @Override
    default <R> LazySeq<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return flatMapI(mapper);
    }

    @Override
    default LazySeq<T> prependS(Stream<? extends T> stream) {
        return (LazySeq<T>) ImmutableList.super.prependS(stream);
    }

    @Override
    default LazySeq<T> append(T... values) {
        return (LazySeq<T>) ImmutableList.super.append(values);
    }

    @Override
    default LazySeq<T> prependAll(T... values) {
        return (LazySeq<T>) ImmutableList.super.prependAll(values);
    }

    @Override
    default LazySeq<T> deleteBetween(int start, int end) {
        return (LazySeq<T>) ImmutableList.super.deleteBetween(start,end);
    }

    @Override
    default LazySeq<T> insertAtS(int pos, Stream<T> stream) {
        return (LazySeq<T>) ImmutableList.super.insertAtS(pos,stream);
    }

    @Override
    default LazySeq<T> recover(Function<? super Throwable, ? extends T> fn) {
        return this;
    }

    @Override
    default <EX extends Throwable> LazySeq<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return this;
    }

    @Override
    default LazySeq<T> prepend(Iterable<? extends T> value) {
        return (LazySeq<T>) ImmutableList.super.prepend(value);
    }

    @Override
    default <U extends Comparable<? super U>> LazySeq<T> sorted(Function<? super T, ? extends U> function) {
        return (LazySeq<T>) ImmutableList.super.sorted(function);
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
            if(obj instanceof PersistentIndexed) {
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

        public LazySeq<T> removeAt(final int i) {
            if (i == 0)
                return tail.get();
            return cons(head,()->tail.get().removeAt(i-1));
        }


        @Override
        public LazySeq<T> updateAt(int i, T value) {

            if(i==0) {
                if(Objects.equals(head,value))
                    return this;
                return cons(value, tail);
            }
            if(i>1000){
                LazySeq<T> front = take(i);
                LazySeq<T> back = drop(i);

                return back.prepend(value).prependAll(front);
            }


            return cons(head, ()->tail.get().updateAt(i-1, value));
        }

        public LazySeq<T> insertAt(final int i, final T value) {
            if(i==0)
                return prepend(value);
            if(i<1000)
                return cons(head, ()->tail.get().insertAt(i-1, value));
            return LazySeq.super.insertAt(i,value);
        }

        @Override
        public LazySeq<T> insertAt(int i, Iterable<? extends T> list) {
            if(i==0)
                return prependAll(list);
            if(i<1000)
                return cons(head, ()->tail.get().insertAt(i-1, list));
            return LazySeq.super.insertAt(i,list);

        }

        @Override
        public LazySeq<T> removeValue(T e) {
            return removeAll(e);
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
        public LazySeq<T> onEmpty(T value) {
            return LazySeq.of(value);
        }

        @Override
        public LazySeq<T> onEmptyGet(Supplier<? extends T> supplier) {
            return LazySeq.of(supplier.get());
        }

        @Override
        public <X extends Throwable> LazySeq<T> onEmptyThrow(Supplier<? extends X> supplier) {
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

            if(obj instanceof PersistentList) {
                return ((PersistentList)obj).size()==0;
            }
            return false;
        }

        @Override
        public LazySeq<T> plusAll(Iterable<? extends T> t) {
            return fromIterable((Iterable<T>)t);
        }

        @Override
        public LazySeq<T> updateAt(int i, T value) {
            return this;
        }

        @Override
        public LazySeq<T> removeAt(int i) {
            return this;
        }


        public LazySeq<T> insertAt(final int i, final T e) {
            return plus(e);
        }

        @Override
        public LazySeq<T> insertAt(int i, Iterable<? extends T> list) {
            return fromIterable((Iterable<T>)list);
        }

        @Override
        public LazySeq<T> removeValue(T e) {
            return this;
        }

    }

}
