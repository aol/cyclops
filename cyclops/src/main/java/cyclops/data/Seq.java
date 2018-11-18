package cyclops.data;


import com.oath.cyclops.hkt.DataWitness.seq;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.foldable.Folds;
import com.oath.cyclops.types.functor.Transformable;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.persistent.PersistentIndexed;
import com.oath.cyclops.types.persistent.PersistentList;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//safe list implementation that does not support exceptional states
public interface Seq<T> extends ImmutableList<T>,
                                Folds<T>,
                                Filters<T>,
                                Transformable<T>,
                                PersistentList<T>,
                                 Serializable,
                                Higher<seq,T> {

    static <T> Collector<T, List<T>, Seq<T>> collector() {
        Collector<T, ?, List<T>> c  = Collectors.toList();
        return Collectors.<T, List<T>, Iterable<T>,Seq<T>>collectingAndThen((Collector)c,Seq::fromIterable);
    }
    default T headOrElse(T head){
        return foldSeq(s->s.head(), nil->head);
    }
    default T headOrElseGet(Supplier<? extends T> head){
        return foldSeq(s->s.head(), nil->head.get());
    }
    default Seq<T> tailOrElse(Seq<T> tail){
        return foldSeq(s->s.tail(), nil->tail);
    }
    default Seq<T> tailOrElseGet(Supplier<? extends Seq<T>> tail){
        return foldSeq(s->s.tail(), nil->tail.get());
    }

    @Override
    default<R> Seq<R> unitIterable(Iterable<R> it){
        if(it instanceof Seq){
            return (Seq<R>)it;
        }
        return fromIterable(it);
    }
    default Seq<T> plusAll(Iterable<? extends T> t){
        return prependAll((Iterable<T>)t);
    }
    Seq<T> updateAt(int i, T value);
    Seq<T> removeAt(final long i);
    default Seq<T> plus(T value){
        return prepend(value);
    }
    default Seq<T> insertAt(final int i, final T e){
        return (Seq<T>)ImmutableList.super.insertAt(i,e);
    }

    default Seq<T> insertAt(int i, Iterable<? extends T> list){
        return (Seq<T>)ImmutableList.super.insertAt(i,list);
    }

    @Override
    Seq<T> removeValue(T e);
    @Override
    default Seq<T> removeAll(Iterable<? extends T> list) {
        return (Seq<T>) ImmutableList.super.removeAll(list);
    }

    @Override
    default Seq<T> removeFirst(Predicate<? super T> pred){
        return (Seq<T>)ImmutableList.super.removeFirst(pred);
    }
    @Override
    default <R> Seq<R> unitStream(Stream<R> stream){
        return fromStream(stream);
    }


    @Override
    default Seq<T> insertAt(int pos, T... values) {
        return (Seq<T>)ImmutableList.super.insertAt(pos,values);
    }
    default Seq<T> removeAll(T... values){
        return (Seq<T>)ImmutableList.super.removeAll(values);
    }

    default ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(this);
    }
    default LazySeq<T> linkedSeq(){
        return LazySeq.fromIterable(this);
    }

    static <T> Seq<T> fromIterable(Iterable<T> it){
        if(it instanceof Seq)
            return (Seq<T>)it;
        return fromIterator(it.iterator());
    }
      static  <T,R> Seq<R> tailRec(T initial, Function<? super T, ? extends Seq<? extends Either<T, R>>> fn) {
        Seq<Either<T, R>> next = Seq.of(Either.left(initial));

        boolean newValue[] = {true};
        for(;;){

            next = next.flatMap(e -> e.fold(s -> {
                        newValue[0]=true;
                        return fn.apply(s);
                        },
                    p -> {
                        newValue[0]=false;
                        return Seq.of(e);
                    }));
            if(!newValue[0])
                break;

        }
        return Seq.fromStream(Either.sequenceRight(next).orElse(ReactiveSeq.empty()));
    }
    static <T> Seq<T> fill(T t, int max){
        return Seq.fromStream(ReactiveSeq.fill(t).take(max));
    }
    static <U, T> Seq<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return fromStream(ReactiveSeq.unfold(seed,unfolder));
    }

    static <T> Seq<T> iterate(final T seed, Predicate<? super T> pred, final UnaryOperator<T> f) {
        return fromStream(ReactiveSeq.iterate(seed,pred,f));

    }
    static <T> Seq<T> iterate(final T seed, final UnaryOperator<T> f,int max) {
        return fromStream(ReactiveSeq.iterate(seed,f).limit(max));

    }

    static <T, U> Tuple2<Seq<T>, Seq<U>> unzip(final LazySeq<Tuple2<T, U>> sequence) {
        return ReactiveSeq.unzip(sequence.stream()).transform((a, b)->Tuple.tuple(fromStream(a),fromStream(b)));
    }
    static <T> Seq<T> generate(Supplier<T> s, int max){
        return fromStream(ReactiveSeq.generate(s).limit(max));
    }
    static <T> Seq<T> generate(Generator<T> s){
        return fromStream(ReactiveSeq.generate(s));
    }
    static Seq<Integer> range(final int start, final int end) {
        return Seq.fromStream(ReactiveSeq.range(start,end));

    }
    static Seq<Integer> range(final int start, final int step, final int end) {
        return Seq.fromStream(ReactiveSeq.range(start,step,end));

    }
    static Seq<Long> rangeLong(final long start, final long step, final long end) {
        return Seq.fromStream(ReactiveSeq.rangeLong(start,step,end));
    }


    static Seq<Long> rangeLong(final long start, final long end) {
        return Seq.fromStream(ReactiveSeq.rangeLong(start,end));

    }

    static <T> Seq<T> of(T... value){
        Seq<T> result = empty();
        for(int i=value.length;i>0;i--){
            result = result.prepend(value[i-1]);
        }
        return result;
    }
    static <T> Seq<T> fromIterator(Iterator<T> it){
        List<T> values = new ArrayList<>();
        while(it.hasNext()){
          values.add(it.next());
        }
        Seq<T> result = empty();
        for(int i=values.size();i>0;i--){
            result = result.prepend(values.get(i-1));
        }
        return result;
    }
    static <T> Seq<T> fromStream(Stream<T> stream){
        Iterator<T> t = stream.iterator();
       return t.hasNext() ? cons(t.next(),fromIterator(t)) : empty();
    }
    static <T> Seq<T> empty(){
        return Nil.Instance;
    }

    default Option<T> get(final int pos){
        if(pos<0)
            return Option.none();
        T result = null;
        Seq<T> l = this;
        for(int i=0;i<pos;i++){
           l = l.foldSeq(c->c.tail, n->n);
           if(l instanceof Nil){ //short circuit
               return Option.none();
           }
        }
        return Option.ofNullable(l.foldSeq(c->c.head, n->null));
    }
    default T getOrElse(int pos, T alt){
        if(pos<0)
            return alt;
        T result = null;
        Seq<T> l = this;
        for(int i=0;i<pos;i++){
            l = l.foldSeq(c->c.tail, n->n);
            if(l instanceof Seq.Nil){ //short circuit
                return alt;
            }

        }
        return l.foldSeq(c->c.head, n->alt);
    }
    default T getOrElseGet(int pos, Supplier<? extends T> alt){
        if(pos<0)
            return alt.get();
        T result = null;
        Seq<T> l = this;
        for(int i=0;i<pos;i++){
            l = l.foldSeq(c->c.tail, n->n);
            if(l instanceof Seq.Nil){ //short circuit
                return alt.get();
            }
        }
        return l.foldSeq(c->c.head, n->alt.get());
    }
    default Seq<T> append(T value){
        return Seq.of(value).prependAll(this);
    }
    default Seq<T> appendAll(Iterable<? extends T> it){
        Seq<T> value = narrow(fromIterable(it));
        return value.prependAll(this);
    }
    default Seq<T> prepend(T value){
        return Cons.cons(value,this);
    }
    default Seq<T> prependAll(Iterable<? extends T> it){
      return (Seq<T>)ImmutableList.super.prependAll(it);
    }

    default Seq<T> take(final long num) {
        if( num <= 0)
           return Nil.Instance;

        return fromStream(ReactiveSeq.fromIterable(this).take(num));

    }
    default Seq<T> drop(final long num) {
        Seq<T> current = this;
        long pos = num;
        while (pos-- > 0 && !current.isEmpty()) {
            current = current.foldSeq(c->c.tail, nil->nil);
        }
        return current;
    }
    default Seq<T> reverse() {
        Seq<T> res = empty();
        for (T a : this) {
            res = res.prepend(a);
        }
        return res;
    }
    @Override
    default Iterator<T> iterator() {
        final Seq<T> host= Seq.this;
        return new Iterator<T>() {

            Seq<T> active= host;
            Seq<T> empty = empty();

            @Override
            public boolean hasNext() {
                return !active.isEmpty();
            }

            @Override
            public T next() {
                if(active instanceof Cons){
                    Cons<T> cons = (Cons<T>)active;
                    active = cons.tail;
                    return cons.head;
                }
                return null;
               /** T result = active instanceof  Cons ? ((Cons<T>)active).head : null;
                active =active instanceof  Cons ? ((Cons<T>)active).tail : empty;
                return result;
                **/
            }
        };
    }


    @Override
    default Seq<T> replaceFirst(T currentElement, T newElement) {
        return (Seq<T>)ImmutableList.super.replaceFirst(currentElement,newElement);
    }

    @Override
    default Seq<T> subList(int start, int end) {
        return (Seq<T>)ImmutableList.super.subList(start,end);
    }

    @Override
    default <U> Seq<U> ofType(Class<? extends U> type) {
        return (Seq<U>)ImmutableList.super.ofType(type);
    }

    @Override
    default Seq<T> filterNot(Predicate<? super T> predicate) {
        return (Seq<T>)ImmutableList.super.filterNot(predicate);
    }

    @Override
    default Seq<T> notNull() {
        return (Seq<T>)ImmutableList.super.notNull();
    }

    @Override
    default Seq<T> peek(Consumer<? super T> c) {
        return (Seq<T>)ImmutableList.super.peek(c);
    }


    public static <T> Seq<T> narrowK(final Higher<seq, T> list) {
      return (Seq<T>)list;
    }
    public static <C2,T> Higher<C2, Higher<seq,T>> widen2(Higher<C2, Seq<T>> list){
      return (Higher)list;
    }
    @Override
    default Seq<T> removeStream(Stream<? extends T> stream) {
        return (Seq<T>)ImmutableList.super.removeStream(stream);
    }

    @Override
    default Seq<T> retainAll(Iterable<? extends T> it) {
        return (Seq<T>)ImmutableList.super.retainAll(it);
    }

    @Override
    default Seq<T> retainStream(Stream<? extends T> stream) {
        return (Seq<T>)ImmutableList.super.retainStream(stream);
    }

    @Override
    default Seq<T> retainAll(T... values) {
        return (Seq<T>)ImmutableList.super.retainAll(values);
    }

    @Override
    default Seq<ReactiveSeq<T>> permutations() {
        return (Seq<ReactiveSeq<T>>)ImmutableList.super.permutations();
    }

    @Override
    default Seq<ReactiveSeq<T>> combinations(int size) {
        return (Seq<ReactiveSeq<T>>)ImmutableList.super.combinations(size);
    }

    @Override
    default Seq<ReactiveSeq<T>> combinations() {
        return (Seq<ReactiveSeq<T>>)ImmutableList.super.combinations();
    }

  @Override
    default <T2, R> Seq<R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> publisher) {
        return (Seq<R>)ImmutableList.super.zip(fn, publisher);
    }

    @Override
    default <U, R> Seq<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (Seq<R>)ImmutableList.super.zipWithStream(other,zipper);
    }

    @Override
    default <U> Seq<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return (Seq)ImmutableList.super.zipWithPublisher(other);
    }

    @Override
    default <U> Seq<Tuple2<T, U>> zip(Iterable<? extends U> other) {
        return (Seq)ImmutableList.super.zip(other);
    }

    @Override
    default <S, U, R> Seq<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (Seq<R>)ImmutableList.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> Seq<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (Seq<R>)ImmutableList.super.zip4(second,third,fourth,fn);
    }



    @Override
    default Seq<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return (Seq<T>)ImmutableList.super.combine(predicate,op);
    }

    @Override
    default Seq<T> combine(Monoid<T> op, BiPredicate<? super T, ? super T> predicate) {
        return (Seq<T>)ImmutableList.super.combine(op,predicate);
    }

    @Override
    default Seq<T> cycle(long times) {
        return (Seq<T>)ImmutableList.super.cycle(times);
    }

    @Override
    default Seq<T> cycle(Monoid<T> m, long times) {
        return (Seq<T>)ImmutableList.super.cycle(m,times);
    }

    @Override
    default Seq<T> cycleWhile(Predicate<? super T> predicate) {
        return (Seq<T>) ImmutableList.super.cycleWhile(predicate);
    }

    @Override
    default Seq<T> cycleUntil(Predicate<? super T> predicate) {
        return (Seq<T>) ImmutableList.super.cycleUntil(predicate);
    }

    @Override
    default <U, R> Seq<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (Seq<R>) ImmutableList.super.zip(other,zipper);
    }

    @Override
    default <S, U> Seq<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return (Seq) ImmutableList.super.zip3(second,third);
    }

    @Override
    default <T2, T3, T4> Seq<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return (Seq) ImmutableList.super.zip4(second,third,fourth);
    }

    @Override
    default Seq<Tuple2<T, Long>> zipWithIndex() {
        return (Seq<Tuple2<T,Long>>) ImmutableList.super.zipWithIndex();
    }

    @Override
    default Seq<Seq<T>> sliding(int windowSize) {
        return (Seq<Seq<T>>) ImmutableList.super.sliding(windowSize);
    }


    @Override
    default Seq<Seq<T>> sliding(int windowSize, int increment) {
        return (Seq<Seq<T>>) ImmutableList.super.sliding(windowSize,increment);
    }

    @Override
    default <C extends PersistentCollection<? super T>> Seq<C> grouped(int size, Supplier<C> supplier) {
        return (Seq<C>) ImmutableList.super.grouped(size,supplier);
    }

    @Override
    default Seq<Vector<T>> groupedUntil(Predicate<? super T> predicate) {
        return (Seq<Vector<T>>) ImmutableList.super.groupedUntil(predicate);
    }

    @Override
    default Seq<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return (Seq<Vector<T>>) ImmutableList.super.groupedUntil(predicate);
    }

    @Override
    default <U> Seq<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {
        return (Seq) ImmutableList.super.zipWithStream(other);
    }

    @Override
    default Seq<Vector<T>> groupedWhile(Predicate<? super T> predicate) {
        return (Seq<Vector<T>>) ImmutableList.super.groupedWhile(predicate);
    }

    @Override
    default <C extends PersistentCollection<? super T>> Seq<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return (Seq<C>) ImmutableList.super.groupedWhile(predicate,factory);
    }

    @Override
    default <C extends PersistentCollection<? super T>> Seq<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return (Seq<C>) ImmutableList.super.groupedUntil(predicate,factory);
    }

    @Override
    default Seq<Vector<T>> grouped(int groupSize) {
        return (Seq<Vector<T>>) ImmutableList.super.grouped(groupSize);
    }

    @Override
    default Seq<T> distinct() {
        return (Seq<T>) ImmutableList.super.distinct();
    }

    @Override
    default Seq<T> scanLeft(Monoid<T> monoid) {
        return (Seq<T>) ImmutableList.super.scanLeft(monoid);
    }

    @Override
    default <U> Seq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return (Seq<U>) ImmutableList.super.scanLeft(seed,function);
    }

    @Override
    default Seq<T> scanRight(Monoid<T> monoid) {
        return (Seq<T>) ImmutableList.super.scanRight(monoid);
    }

    @Override
    default <U> Seq<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (Seq<U>) ImmutableList.super.scanRight(identity,combiner);
    }

    @Override
    default Seq<T> sorted() {
        return (Seq<T>) ImmutableList.super.sorted();
    }

    @Override
    default Seq<T> sorted(Comparator<? super T> c) {
        return (Seq<T>) ImmutableList.super.sorted(c);
    }

    @Override
    default Seq<T> takeWhile(Predicate<? super T> p) {
        return (Seq<T>) ImmutableList.super.takeWhile(p);
    }

    @Override
    default Seq<T> dropWhile(Predicate<? super T> p) {
        return (Seq<T>) ImmutableList.super.dropWhile(p);
    }

    @Override
    default Seq<T> takeUntil(Predicate<? super T> p) {
        return (Seq<T>) ImmutableList.super.takeUntil(p);
    }

    @Override
    default Seq<T> dropUntil(Predicate<? super T> p) {
        return (Seq<T>) ImmutableList.super.dropUntil(p);
    }

    @Override
    default Seq<T> dropRight(int num) {
        return (Seq<T>) ImmutableList.super.dropRight(num);
    }

    @Override
    default Seq<T> takeRight(int num) {
        return (Seq<T>) ImmutableList.super.takeRight(num);
    }


    @Override
    default Seq<T> tailOrElse(ImmutableList<T> tail) {
        ImmutableList<T> list = ImmutableList.super.tailOrElse(tail);
        return Seq.fromIterable(list);
    }


    @Override
    default Seq<T> intersperse(T value) {
        return (Seq<T>) ImmutableList.super.intersperse(value);
    }

    @Override
    default Seq<T> shuffle() {
        return (Seq<T>) ImmutableList.super.shuffle();
    }


    @Override
    default Seq<T> shuffle(Random random) {
        return (Seq<T>) ImmutableList.super.shuffle(random);
    }

    @Override
    default Seq<T> slice(long from, long to) {
        return (Seq<T>) ImmutableList.super.slice(from,to);
    }

    @Override
    Seq<T> onEmpty(T value);

    @Override
    Seq<T> onEmptyGet(Supplier<? extends T> supplier);
    @Override
    <R> Seq<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

    @Override
    <R> Seq<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn);

    @Override
    <R> Seq<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn);

  @Override
    default Seq<T> prependStream(Stream<? extends T> stream) {
        return (Seq<T>) ImmutableList.super.prependStream(stream);
    }

    @Override
    default Seq<T> appendAll(T... values) {
        return (Seq<T>) ImmutableList.super.appendAll(values);
    }

    @Override
    default Seq<T> prependAll(T... values) {
        return (Seq<T>) ImmutableList.super.prependAll(values);
    }

    @Override
    default Seq<T> deleteBetween(int start, int end) {
        return (Seq<T>) ImmutableList.super.deleteBetween(start,end);
    }

    @Override
    default Seq<T> insertStreamAt(int pos, Stream<T> stream) {
        return (Seq<T>) ImmutableList.super.insertStreamAt(pos,stream);
    }



    @Override
    default <U extends Comparable<? super U>> Seq<T> sorted(Function<? super T, ? extends U> function) {
        return (Seq<T>) ImmutableList.super.sorted(function);
    }

    default  T foldRight(Monoid<T> m){
        return foldRight(m.zero(),(T a,T b)->m.apply(a,b));
    }
    default  T foldLeft(Monoid<T> m){
        return foldLeft(m.zero(),(T a,T b)->m.apply(a,b));
    }
    default <R> R foldRight(R zero, BiFunction<? super T, ? super R, ? extends R> f){
      return (R) ImmutableList.super.foldRight(zero,f);
    }

    default <R> R foldLeft(R zero, BiFunction<R, ? super T, R> f){
        R acc= zero;
        for(T next : this){
            acc= f.apply(acc,next);
        }
        return acc;
    }
    default Seq<T> filter(Predicate<? super T> pred){
        return foldRight(empty(),(a,l)->{
            if(pred.test(a)){
                return l.prepend(a);
            }
            return l;
        });
    }
    <R> Seq<R> map(Function<? super T, ? extends R> fn);

    default <R> Seq<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
         return concatMap(fn);
    }


    static <T> Seq<T> narrow(Seq<? extends T> list){
        return (Seq<T>)list;
    }

    int size();

    @Override
    default boolean containsValue(T value) {
        return stream().filter(i->Objects.equals(i,value)).findFirst().isPresent();
    }

    boolean isEmpty();


    <R> R foldSeq(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2);

    static <T> Seq<T> cons(T head, Seq<T> tail) {
        return Cons.cons(head,tail);
    }
    @Override
    default Seq<T> emptyUnit() {
        return empty();
    }



  @Override
  default <R1, R2, R3, R> Seq<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (Seq< R>) ImmutableList.super.forEach4(iterable1,iterable2,iterable3,yieldingFunction);
  }

  @Override
  default <R1, R2, R3, R> Seq<R> forEach4(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3, Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
    return (Seq< R>) ImmutableList.super.forEach4(iterable1,iterable2,iterable3,filterFunction,yieldingFunction);
  }

  @Override
  default <R1, R2, R> Seq<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (Seq< R>) ImmutableList.super.forEach3(iterable1,iterable2,yieldingFunction);
  }

  @Override
  default <R1, R2, R> Seq<R> forEach3(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2, Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
    return (Seq< R>) ImmutableList.super.forEach3(iterable1,iterable2,filterFunction,yieldingFunction);
  }

  @Override
  default <R1, R> Seq<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (Seq< R>) ImmutableList.super.forEach2(iterable1,yieldingFunction);
  }

  @Override
  default <R1, R> Seq<R> forEach2(Function<? super T, ? extends Iterable<R1>> iterable1, BiFunction<? super T, ? super R1, Boolean> filterFunction, BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
    return (Seq< R>) ImmutableList.super.forEach2(iterable1,filterFunction,yieldingFunction);
  }
    default Either<Integer,Seq<T>> set(int pos, T value) {
        if (pos < 0 || pos >= size()) {
            return Either.left(size());
        }
        return Either.right(updateAt(pos, value));
    }

    default Either<Integer,Seq<T>> delete(int pos){
        if(pos<0||pos>=size()){
            return Either.left(size());
        }
        return Either.right(removeAt(pos));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(of={"head,tail"})
    public static final class Cons<T> implements ImmutableList.Some<T>, Seq<T>, Serializable {
        private static final long serialVersionUID = 1L;
        public final T head;
        public final Seq<T> tail;
        private final int size;
        private Supplier<Integer> hash;

        public static <T> Cons<T> cons(T value, Seq<T> tail){
            return new Cons<>(value,tail,tail.size()+1,null);
        }


        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head,tail);
        }
        public boolean isEmpty(){
            return false;
        }


        @Override
        public <R> Seq<R> map(Function<? super T, ? extends R> fn) {
          Seq<R> res = empty();
          for (T t : this) {
            res = res.prepend(fn.apply(t));
          }
          return res.reverse();
        }

        @Override
        public Seq<T> tail() {
            return tail;
        }
        public Seq<T> removeAt(final long i) {
            if (size == 0)
                return this;
            if (i == 0)
                return tail;

            if(i>1000)
                return fromStream(stream().removeAt(i));
            Seq<T> removed = tail.removeAt(i-1);
            return cons(head,removed);
        }




        @Override
        public Seq<T> updateAt(int i, T value) {

            if(i==0) {
                if(Objects.equals(head,value))
                    return this;
                return cons(value, tail);
            }
            if(i>1000){
                Seq<T> front = take(i);
                Seq<T> back = drop(i);

                return back.prepend(value).prependAll(front);
            }
            Seq<T> replaced = tail.updateAt(i-1, value);

            return cons(head,replaced);
        }

        public Seq<T> insertAt(final int i, final T value) {
            if(i==0)
                return prepend(value);
            if(i<1000)
                return cons(head, tail.insertAt(i-1, value));
            return Seq.super.insertAt(i,value);

        }

        @Override
        public Seq<T> insertAt(int i, Iterable<? extends T> list) {
            if(i==0)
                return prependAll(list);
            if(i<1000)
                return cons(head, tail.insertAt(i-1, list));
            return Seq.super.insertAt(i,list);


        }

        @Override
        public Seq<T> removeValue(T e) {
            return removeAll(e);
        }

        @Override
        public T head() {
            return head;
        }



        @Override
        public Cons<T> reverse() {
            return (Cons<T>)Seq.super.reverse();
        }
        public int size(){
            return size;
        }
        @Override
        public int hashCode() {
            if(hash==null){
                Supplier<Integer> local = Memoize.memoizeSupplier(()->{
                    int hashCode = 1;
                    for (T e : this)
                        hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
                    return hashCode;
                });
                hash= local;
                return local.get();
            }
            return hash.get();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj==null)
                return false;
            if (obj == this)
                return true;
             if (obj instanceof Seq) {
                 Seq<T> seq1 = this;
                 Seq seq2 = (Seq) obj;
                 if (seq1.size() != seq2.size()) {
                     return false;
                 }
             }
             if(obj instanceof PersistentIndexed) {
                 return equalToIteration((Iterable)obj);
             }
             return false;
        }

        @Override
        public String toString(){
            StringBuffer b = new StringBuffer("["+head);
            Iterator<T> it = tail.iterator();
            while(it.hasNext()){
                b.append(", "+it.next());
            }
            b.append("]");
            return b.toString();

        }

        @Override
        public <R> R foldSeq(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2) {
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
        public <R> Seq<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
          Seq<R> res = empty();
          for (T t : this) {
            for(R r : mapper.apply(t)) {
              res = res.prepend(r);
            }
          }
          return res.reverse();
        }

      @Override
      public <R> Seq<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return fromStream(stream().mergeMap(fn));
      }

      @Override
      public <R> Seq<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
        return fromStream(stream().mergeMap(maxConcurecy,fn));
      }

      @Override
        public Cons<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
            return this;
        }


    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public final class Nil<T> implements Seq<T>, ImmutableList.None<T> {
        static final Nil Instance = new Nil();
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
        public Seq<T> onEmpty(T value) {
            return Seq.of(value);
        }

        @Override
        public <R> Seq<R> map(Function<? super T, ? extends R> fn) {
          return empty();
        }

        @Override
        public Seq<T> onEmptyGet(Supplier<? extends T> supplier) {
            return Seq.of(supplier.get());
        }

        @Override
        public <R> Seq<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
          return empty();
        }

        @Override
        public <R> Seq<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
          return empty();
        }

        @Override
        public <R> Seq<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn) {
          return empty();
        }

      @Override
        public ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
            return supplier.get();
        }

        @Override
        public <R> R foldSeq(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2) {
            return fn2.apply(this);
        }

        @Override
        public Seq<T> plusAll(Iterable<? extends T> t) {
            return fromIterable((Iterable<T>)t);
        }

        @Override
        public Seq<T> updateAt(int i, T value) {
            return this;
        }

        @Override
        public Seq<T> removeAt(long i) {
            return this;
        }


        public Seq<T> insertAt(final int i, final T e) {
            return plus(e);
        }

        @Override
        public Seq<T> insertAt(int i, Iterable<? extends T> list) {
            return fromIterable((Iterable<T>)list);
        }

        @Override
        public Seq<T> removeValue(T e) {
            return this;
        }

        @Override
        public String toString(){
            return "[]";
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
      private Object readResolve() {
        return Instance;
      }
    }

  public static <T> Higher<seq, T> widen(Seq<T> narrow) {
    return narrow;
  }

}
