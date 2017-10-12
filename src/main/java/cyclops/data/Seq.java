package cyclops.data;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.foldable.Evaluation;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.collectionx.mutable.ListX;
import cyclops.control.Option;
import cyclops.control.anym.Witness;
import cyclops.control.lazy.Trampoline;
import cyclops.control.Either;
import cyclops.control.anym.DataWitness.seq;
import cyclops.function.Monoid;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.*;
import java.util.stream.Stream;

//safe list implementation that does not support exceptional states
public interface Seq<T> extends ImmutableList<T>,
                                Folds<T>,
                                Filters<T>,
                                Transformable<T>,
                                Higher<seq,T> {


    @Override
    default <R> Seq<R> unitStream(Stream<R> stream){
        return fromStream(stream);
    }

    public static <R> Seq<R> fromStreamLazily(Stream<R> stream) {
        Iterator<R> it = stream.iterator();
        return fromIteratorLazily(it);
    }
    public static <R> Seq<R> fromIterableLazily(Iterable<R> it) {
        return fromIteratorLazily(it.iterator());
    }

    public static <R> Seq<R> fromIteratorLazily(Iterator<R> it) {
        if(it.hasNext()){
            return cons(it.next(),(Seq<R>) Proxy.newProxyInstance(ImmutableList.class.getClassLoader(),
                    new Class<?>[] { Seq.class },
                    (p, m, a) -> m.invoke(fromIteratorLazily(it), a)));
        }
        return Seq.empty();
    }

    default ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(iterable());
    }
    default LinkedListX<T> linkedListX(){
        return LinkedListX.fromIterable(iterable());
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

            next = next.flatMap(e -> e.visit(s -> {
                        newValue[0]=true;
                        return fromStream(fn.apply(s).stream());
                        },
                    p -> {
                        newValue[0]=false;
                        return Seq.of(e);
                    }));
            if(!newValue[0])
                break;

        }
        ListX<R> x = Either.sequenceRight(next.stream().to().listX(Evaluation.LAZY)).orElse(ListX.empty());
        return Seq.fromIterator(x.iterator());
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
        T result = null;
        Seq<T> l = this;
        for(int i=0;i<pos;i++){
           l = l.visit(c->c.tail,n->n);
           if(l instanceof Nil){ //short circuit
               return Option.none();
           }
        }
        return Option.ofNullable(l.visit(c->c.head, n->null));
    }
    default T getOrElse(int pos, T alt){
        T result = null;
        Seq<T> l = this;
        for(int i=0;i<pos;i++){
            l = l.visit(c->c.tail,n->n);
            if(l instanceof LazySeq.Nil){ //short circuit
                return alt;
            }
        }
        return l.visit(c->c.head,n->null);
    }
    default T getOrElseGet(int pos, Supplier<T> alt){
        T result = null;
        Seq<T> l = this;
        for(int i=0;i<pos;i++){
            l = l.visit(c->c.tail,n->n);
            if(l instanceof Seq.Nil){ //short circuit
                return alt.get();
            }
        }
        return l.visit(c->c.head,n->null);
    }
    default Seq<T> append(T value){
        return Seq.of(value).prependAll(this);
    }
    default Seq<T> appendAll(Iterable<T> it){
        Seq<T> value = fromIterable(it);
        return value.prependAll(this);
    }
    default Seq<T> prepend(T value){
        return cons(value,this);
    }
    default Seq<T> prependAll(Iterable<T> it){
        Seq<T> value = fromIterable(it);
        return value.fold(cons->
                        cons.foldRight(this,(a,b)->b.prepend(a))
                ,nil->this);
    }

    default Seq<T> take(final long num) {
        if( num <= 0)
           return Nil.Instance;
        if(num<1000) {
            return this.visit(cons -> cons(cons.head, cons.take(num - 1)), nil -> nil);
        }
        return fromStream(ReactiveSeq.fromIterable(this.iterable()).take(num));

    }
    default Seq<T> drop(final long num) {
        Seq<T> current = this;
        long pos = num;
        while (pos-- > 0 && !current.isEmpty()) {
            current = current.visit(c->c.tail,nil->nil);
        }
        return current;
    }
    default Seq<T> reverse() {
        Seq<T> res = empty();
        for (T a : iterable()) {
            res = res.prepend(a);
        }
        return res;
    }

    default Iterable<T> iterable(){
        return ()->new Iterator<T>() {
            Seq<T> current= Seq.this;
            @Override
            public boolean hasNext() {
                return current.fold(c->true, n->false);
            }

            @Override
            public T next() {
                return current.visit(c->{
                    current = c.tail;
                    return c.head;
                },n->null);
            }
        };
    }
    default  T foldRight(Monoid<T> m){
        return foldRight(m.zero(),(T a,T b)->m.apply(a,b));
    }
    default  T foldLeft(Monoid<T> m){
        return foldLeft(m.zero(),(T a,T b)->m.apply(a,b));
    }
    <R> R foldRight(R zero, BiFunction<? super T, ? super R, ? extends R> f);

    default <R> R foldLeft(R zero, BiFunction<R, ? super T, R> f){
        R acc= zero;
        for(T next : iterable()){
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
    default <R> Seq<R> map(Function<? super T, ? extends R> fn) {
        return foldRight(empty(), (a, l) -> l.prepend(fn.apply(a)));
    }
    default <R> Seq<R> flatMap(Function<? super T, ? extends ImmutableList<? extends R>> fn) {
         return foldRight(empty(), (a, l) -> {
             Seq<R> b = narrow(fn.apply(a).imSeq());
             return l.prependAll(b);
         });
    }
    default <R> Seq<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return foldRight(empty(), (a, l) -> {
            Seq<R> b = narrow(fromIterable(fn.apply(a)));
            return b.prependAll(l);
        });
    }

    static <T> Seq<T> narrow(Seq<? extends T> list){
        return (Seq<T>)list;
    }

    int size();

    boolean isEmpty();


    <R> R visit(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2);

    static <T> Seq<T> cons(T head, Seq<T> tail) {
        return Cons.cons(head,tail);
    }
    @Override
    default Seq<T> emptyUnit() {
        return empty();
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @EqualsAndHashCode(of={"head,tail"})
    public static class Cons<T> implements ImmutableList.Some<T>, Seq<T> {

        public final T head;
        public final Seq<T> tail;
        private final int size;

        public static <T> Cons<T> cons(T value, Seq<T> tail){
            return new Cons<>(value,tail,tail.size()+1);
        }

        @Override
        public Tuple2<T, ImmutableList<T>> unapply() {
            return Tuple.tuple(head,tail);
        }
        public boolean isEmpty(){
            return false;
        }

        @Override
        public T foldRight(T identity, BinaryOperator<T> accumulator) {
            class Step{
                public Trampoline<T> loop(Seq<T> s, Function<? super T, ? extends Trampoline<T>> fn){

                    return s.visit(c-> Trampoline.more(()->loop(c.tail, rem -> Trampoline.more(() -> fn.apply(accumulator.apply(c.head, rem))))), n->fn.apply(identity));

                }
            }
            return new Step().loop(this,i-> Trampoline.done(i)).result();
        }

        public <R> R foldRight(R zero, BiFunction<? super T, ? super R, ? extends R> f) {
            class Step{
                public Trampoline<R> loop(Seq<T> s, Function<? super R, ? extends Trampoline<R>> fn){

                    return s.visit(c-> Trampoline.more(()->loop(c.tail, rem -> Trampoline.more(() -> fn.apply(f.apply(c.head, rem))))), n->fn.apply(zero));

                }
            }
            return new Step().loop(this,i-> Trampoline.done(i)).result();
        }

        @Override
        public ImmutableList<T> tail() {
            return tail;
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
            return linkedListX().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Seq)
                return linkedListX().equals(((Seq)obj).linkedListX());
            return false;
        }

        @Override
        public String toString(){
            StringBuffer b = new StringBuffer("["+head);
            Iterator<T> it = tail.iterator();
            while(it.hasNext()){
                b.append(","+it.next());
            }
            b.append("]");
            return b.toString();

        }

        @Override
        public <R> R visit(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2) {
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


    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class Nil<T> implements Seq<T>, ImmutableList.None<T> {
        static Nil Instance = new Nil();
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
        public ImmutableList<T> onEmpty(T value) {
            return Seq.of(value);
        }

        @Override
        public ImmutableList<T> onEmptyGet(Supplier<? extends T> supplier) {
            return Seq.of(supplier.get());
        }

        @Override
        public <X extends Throwable> ImmutableList<T> onEmptyThrow(Supplier<? extends X> supplier) {
            throw ExceptionSoftener.throwSoftenedException(supplier.get());
        }

        @Override
        public ImmutableList<T> onEmptySwitch(Supplier<? extends ImmutableList<T>> supplier) {
            return supplier.get();
        }

        @Override
        public <R> R visit(Function<? super Cons<T>, ? extends R> fn1, Function<? super Nil, ? extends R> fn2) {
            return fn2.apply(this);
        }
    }

}
