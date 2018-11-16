package cyclops.reactive.collections.mutable;

import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.data.ReactiveWitness.list;
import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.lazy.LazyListX;
import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;
import com.oath.cyclops.data.collections.extensions.standard.MutableSequenceX;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Future;
import cyclops.control.*;

import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.types.foldable.To;

import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.data.tuple.Tuple.tuple;


/**
 * An eXtended List type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this ListX
 */
public interface ListX<T> extends To<ListX<T>>,
                                  List<T>,
                                  LazyCollectionX<T>,
                                  MutableSequenceX<T>,
                                  Comparable<T>,
                                  OnEmptySwitch<T, List<T>>,
                                  Higher<list,T> {


    default Tuple2<ListX<T>, ListX<T>> splitAt(int n) {
        materialize();
        return Tuple.tuple(take(n), drop(n));
    }
    default Tuple2<ListX<T>, ListX<T>> span(Predicate<? super T> pred) {
        return tuple(takeWhile(pred), dropWhile(pred));
    }

    default Tuple2<ListX<T>,ListX<T>> splitBy(Predicate<? super T> test) {
        return span(test.negate());
    }
    default Tuple2<ListX<T>, ListX<T>> partition(final Predicate<? super T> splitter) {

        return tuple(filter(splitter), filter(splitter.negate()));

    }
    @Override
    default Object[] toArray(){
        return LazyCollectionX.super.toArray();
    }

    @Override
    default <T1> T1[] toArray(T1[] a){
        return LazyCollectionX.super.toArray(a);
    }

    public static <T> ListX<T> defer(Supplier<ListX<T>> s){
      return of(s)
            .map(Supplier::get)
            .concatMap(l->l);
    }

    static <T> CompletableListX<T> completable(){
        return new CompletableListX<>();
    }

    static class CompletableListX<T> implements InvocationHandler{
        Future<ListX<T>> future = Future.future();
        public boolean complete(List<T> result){
            return future.complete(ListX.fromIterable(result));
        }

        public ListX<T> asListX(){
            ListX f = (ListX) Proxy.newProxyInstance(ListX.class.getClassLoader(),
                    new Class[] { ListX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            ListX<T> target = future.fold(l->l, t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }

    ListX<T> lazy();
    ListX<T> eager();

    /**
     * Convert the raw Higher Kinded Type for ListX types into the ListX type definition class
     *
     * @param list HKT encoded list into a ListX
     * @return ListX
     */
    public static <T> ListX<T> narrowK(final Higher<list, T> list) {
        return (ListX<T>)list;
    }







    /**
     * Create a ListX that contains the Integers between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range ListX
     */
    public static ListX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end).to(ReactiveConvertableSequence::converter)
                          .listX(Evaluation.LAZY);
    }

    /**
     * Create a ListX that contains the Longs between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range ListX
     */
    public static ListX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end).to(ReactiveConvertableSequence::converter)
                          .listX(Evaluation.LAZY);
    }

    /**
     * Unfold a function into a ListX
     *
     * <pre>
     * {@code
     *  ListX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     *
     * //(1,2,3,4,5)
     *
     * }</pre>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return ListX generated by unfolder function
     */
    static <U, T> ListX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .to(ReactiveConvertableSequence::converter)
                          .listX(Evaluation.LAZY);
    }
    /**
     * Generate a ListX from the provided value up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Value for ListX elements
     * @return ListX generated from the provided Supplier
     */
    public static <T> ListX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit)
                          .to(ReactiveConvertableSequence::converter)
                          .listX(Evaluation.LAZY);
    }

    /**
     * Generate a ListX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate ListX elements
     * @return ListX generated from the provided Supplier
     */
    public static <T> ListX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .to(ReactiveConvertableSequence::converter)
                          .listX(Evaluation.LAZY);
    }

    /**
     * Create a ListX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return ListX generated by iterative application
     */
    public static <T> ListX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .to(ReactiveConvertableSequence::converter)
                          .listX(Evaluation.LAZY);

    }
    @Override
    default ListX<T> materialize() {
        return (ListX<T>)LazyCollectionX.super.materialize();
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> ListX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> ListX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> ListX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> ListX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> ListX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> ListX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (ListX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }



    /**
     * @return A JDK 8 Collector for converting Streams into ListX instances
     */
    static <T> Collector<T, ?, ListX<T>> listXCollector() {
        return Collectors.toCollection(() -> ListX.of());
    }

    /**
     * @return An Array List Collector
     */
    static <T> Collector<T, ?, List<T>> defaultCollector() {
        return Collectors.toCollection(() -> new ArrayList<>());
    }

    /**
     * @return Unmodifiable array list collector
     */
    static <T> Collector<T, ?, List<T>> immutableCollector() {
        return Collectors.collectingAndThen(defaultCollector(), (final List<T> d) -> Collections.unmodifiableList(d));

    }

    /**
     * @return Construct an zero ListX
     */
    public static <T> ListX<T> empty() {
        return fromIterable((List<T>) defaultCollector().supplier()
                                                        .get());
    }

    /**
     * Construct a ListX from the provided values
     *
     * <pre>
     * {@code
     *     ListX<Integer> deque = ListX.of(1,2,3,4);
     *
     * }</pre>
     *
     *
     *
     * @param values to construct a Deque from
     * @return DequeX
     */
    @SafeVarargs
    public static <T> ListX<T> of(final T... values) {

        return new LazyListX<T>(null,
                ReactiveSeq.of(values),
                defaultCollector());
    }
    public static <T> ListX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }

    public static <T> ListX<T> singleton(final T value) {
        return ListX.<T> of(value);
    }

    /**
     * Construct a ListX from an Publisher
     *
     * @param publisher
     *            to construct ListX from
     * @return ListX
     */
    public static <T> ListX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher).to(ReactiveConvertableSequence::converter)
                          .listX(Evaluation.LAZY);
    }
    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  ListX<Integer> list = listX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create ListX from
     * @param <T> ListX generated from Stream
     * @return
     */
    public static <T> ListX<T> listX(ReactiveSeq<T> stream){
        return new LazyListX<T>(null,
                stream,
                defaultCollector());
    }

    ListX<T> type(Collector<T, ?, List<T>> collector);
    public static <T> ListX<T> fromIterable(final Iterable<T> it) {
        if (it instanceof ListX)
            return (ListX<T>) it;
        if (it instanceof List)
            return new LazyListX<T>(
                                    (List<T>) it, null,defaultCollector());

        return new LazyListX<T>(null,
                                ReactiveSeq.fromIterable(it),
                                           defaultCollector());
    }

    public static <T> ListX<T> fromIterable(final Collector<T, ?, List<T>> collector, final Iterable<T> it) {
        if (it instanceof ListX)
            return ((ListX<T>) it).withCollector(collector);
        if (it instanceof List)
            return new LazyListX<T>(
                                    (List<T>) it,null,collector);
        return new LazyListX<T>(null,
                                ReactiveSeq.fromIterable(it),
                                collector);
    }
    @Override
    default ListX<T> take(final long num) {

        return (ListX<T>) LazyCollectionX.super.take(num);
    }
    @Override
    default ListX<T> drop(final long num) {

        return (ListX<T>) LazyCollectionX.super.drop(num);
    }
    ListX<T> withCollector(Collector<T, ?, List<T>> collector);

    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *      ListX.of(1,2,3)
     *           .map(i->i*2)
     *           .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *      //ListX[12]
     * }
     * </pre>
     *
     *
     * @param fn mapping function
     * @return Transformed List
     */
    default <R> ListX<R> coflatMap(Function<? super ListX<T>, ? extends R> fn){
        return fn.andThen(r ->  this.<R>unit(r))
                 .apply(this);
    }



    @Override
    default <R> ListX<R> unit(final Iterable<R> col) {
        return fromIterable(col);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Pure#unit(java.lang.Object)
     */
    @Override
    default <R> ListX<R> unit(final R value) {
        return singleton(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.IterableFunctor#unitIterable(java.util.Iterator)
     */
    @Override
    default <R> ListX<R> unitIterable(final Iterable<R> it) {
        return fromIterable(it);
    }




    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    /**
     * @return A Collector to generate a List
     */
    public <T> Collector<T, ?, List<T>> getCollector();


    @Override
    default <T1> ListX<T1> from(final Iterable<T1> c) {
        return ListX.<T1> fromIterable(getCollector(), c);
    }

    @Override
    default <X> ListX<X> fromStream(final ReactiveSeq<X> stream) {
        return new LazyListX<>(null,ReactiveSeq.fromStream(stream), getCollector());
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#reverse()
     */
    @Override
    default ListX<T> reverse() {

        return (ListX<T>) LazyCollectionX.super.reverse();
    }

    /**
     * Combine two adjacent elements in a ListX using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code
     *  ListX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),SemigroupK.intSum)
                   .to()
                   .listX(Evaluation.LAZY)

     *  //ListX(3,4)
     * }</pre>
     *
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced ListX
     */
    @Override
    default ListX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (ListX<T>) LazyCollectionX.super.combine(predicate, op);
    }

    @Override
    default ListX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (ListX<T>)LazyCollectionX.super.combine(op,predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default ListX<T> filter(final Predicate<? super T> pred) {

        return (ListX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> ListX<R> map(final Function<? super T, ? extends R> mapper) {

        return (ListX<R>) LazyCollectionX.super.<R> map(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> ListX<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (ListX<R>) LazyCollectionX.super.concatMap(mapper);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#slice(long, long)
     */
    @Override
    default ListX<T> slice(final long from, final long to) {
        return (ListX<T>) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> ListX<T> sorted(final Function<? super T, ? extends U> function) {

        return (ListX<T>) LazyCollectionX.super.sorted(function);
    }


    @Override
    default ListX<Vector<T>> grouped(final int groupSize) {
        return (ListX<Vector<T>>) LazyCollectionX.super.grouped(groupSize);
    }


    @Override
    default <U> ListX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (ListX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> ListX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListX<R>) LazyCollectionX.super.zip(other, zipper);
    }



    @Override
    default ListX<Seq<T>> sliding(final int windowSize) {
        return (ListX<Seq<T>>) LazyCollectionX.super.sliding(windowSize);
    }


    @Override
    default ListX<Seq<T>> sliding(final int windowSize, final int increment) {
        return (ListX<Seq<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }


    @Override
    default ListX<T> scanLeft(final Monoid<T> monoid) {
        return (ListX<T>) LazyCollectionX.super.scanLeft(monoid);
    }


    @Override
    default <U> ListX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (ListX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }


    @Override
    default ListX<T> scanRight(final Monoid<T> monoid) {
        return (ListX<T>) LazyCollectionX.super.scanRight(monoid);
    }


    @Override
    default <U> ListX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (ListX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }


    @Override
    default ListX<T> insertAt(final int i, final T element) {
        return from(stream()
                            .insertAt(i, element));
    }


    @Override
    public ListX<T> subList(int start, int end);

    default ListX<T> plus(final T e) {
        add(e);
        return this;
    }


    @Override
    default ListX<T> plusAll(final Iterable<? extends T> list) {
        for(T next : list)
            add(next);
        return this;
    }

    /* (non-Javadoc)
     * @see MutableSequenceX#removeAt(int)
     */
    @Override
    default ListX<T> removeAt(final int pos) {
        remove(pos);
        return this;
    }


    @Override
    default ListX<T> removeValue(final T e) {
        this.remove(e);
        return this;
    }


    @Override
    default ListX<T> removeAll(final Iterable<? extends T> list) {
        for(T next : list){
            this.removeValue(next);
        }
        return this;
    }



    @Override
    default ListX<T> insertAt(final int i, final Iterable<? extends T> list) {
        return fromStream(stream().insertAt(i,list));
    }

    @Override
    int size();

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.FluentCollectionX#plusInOrder(java.lang.Object)
     */
    @Override
    default ListX<T> plusInOrder(final T e) {

        return (ListX<T>) MutableSequenceX.super.plusInOrder(e);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default ListX<T> peek(final Consumer<? super T> c) {

        return (ListX<T>) LazyCollectionX.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#cycle(int)
     */
    @Override
    default ListX<T> cycle(final long times) {

        return (ListX<T>) LazyCollectionX.super.cycle(times);
    }

    @Override
    default ListX<T> cycle(final Monoid<T> m, final long times) {

        return (ListX<T>) LazyCollectionX.super.cycle(m, times);
    }

    @Override
    default ListX<T> cycleWhile(final Predicate<? super T> predicate) {
        return (ListX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }


    @Override
    default ListX<T> cycleUntil(final Predicate<? super T> predicate) {
        return (ListX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> ListX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (ListX) LazyCollectionX.super.zipWithStream(other);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> ListX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (ListX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> ListX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (ListX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#zipWithIndex()
     */
    @Override
    default ListX<Tuple2<T, Long>> zipWithIndex() {

        return (ListX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#sorted()
     */
    @Override
    default ListX<T> sorted() {

        return (ListX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default ListX<T> sorted(final Comparator<? super T> c) {

        return (ListX<T>) LazyCollectionX.super.sorted(c);
    }



    default ListX<T> dropWhile(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.dropWhile(p);
    }


    default ListX<T> dropUntil(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.dropUntil(p);
    }

    @Override
    default ListX<T> shuffle() {

        return (ListX<T>) LazyCollectionX.super.shuffle();
    }


    default ListX<T> dropRight(final int num) {

        return (ListX<T>) LazyCollectionX.super.dropRight(num);
    }


    @Override
    default ListX<T> shuffle(final Random random) {

        return (ListX<T>) LazyCollectionX.super.shuffle(random);
    }


    @Override
    default ListX<ReactiveSeq<T>> permutations() {

        return (ListX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#combinations(int)
     */
    @Override
    default ListX<ReactiveSeq<T>> combinations(final int size) {

        return (ListX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#combinations()
     */
    @Override
    default ListX<ReactiveSeq<T>> combinations() {

        return (ListX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#distinct()
     */
    @Override
    default ListX<T> distinct() {

        return (ListX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limitWhile(java.util.function.Predicate)
     */
    default ListX<T> takeWhile(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limitUntil(java.util.function.Predicate)
     */
    default ListX<T> takeUntil(final Predicate<? super T> p) {

        return (ListX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default ListX<T> intersperse(final T value) {

        return (ListX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limitLast(int)
     */
    default ListX<T> takeRight(final int num) {

        return (ListX<T>) LazyCollectionX.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default ListX<T> onEmpty(final T value) {

        return (ListX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default ListX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (ListX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#onEmptyError(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> ListX<T> onEmptyError(final Supplier<? extends X> supplier) {

        return (ListX<T>) LazyCollectionX.super.onEmptyError(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> ListX<U> ofType(final Class<? extends U> type) {

        return (ListX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default ListX<T> filterNot(final Predicate<? super T> fn) {

        return (ListX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#notNull()
     */
    @Override
    default ListX<T> notNull() {

        return (ListX<T>) LazyCollectionX.super.notNull();
    }


    @Override
    default ListX<T> removeStream(final Stream<? extends T> stream) {

        return (ListX<T>) LazyCollectionX.super.removeStream(stream);
    }

    @Override
    default ListX<T> removeAll(CollectionX<? extends T> it){
        return removeAll((Iterable<T>)it);
    }



    @Override
    default ListX<T> removeAll(final T... values) {

        return (ListX<T>) LazyCollectionX.super.removeAll(values);
    }


    @Override
    default ListX<T> retainAll(final Iterable<? extends T> it) {

        return (ListX<T>) LazyCollectionX.super.retainAll(it);
    }


    @Override
    default ListX<T> retainStream(final Stream<? extends T> seq) {

        return (ListX<T>) LazyCollectionX.super.retainStream(seq);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default ListX<T> retainAll(final T... values) {

        return (ListX<T>) LazyCollectionX.super.retainAll(values);
    }


    @Override
    default <C extends PersistentCollection<? super T>> ListX<C> grouped(final int size, final Supplier<C> supplier) {

        return (ListX<C>) LazyCollectionX.super.grouped(size, supplier);
    }


    @Override
    default ListX<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (ListX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }


    @Override
    default ListX<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (ListX<Vector<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }


    @Override
    default <C extends PersistentCollection<? super T>> ListX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }


    @Override
    default <C extends PersistentCollection<? super T>> ListX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }


    @Override
    default ListX<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return (ListX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }





    /* (non-Javadoc)
     * @see com.oath.cyclops.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default ListX<T> onEmptySwitch(final Supplier<? extends List<T>> supplier) {
        if (isEmpty())
            return ListX.fromIterable(supplier.get());
        return this;
    }

    @Override
    default int compareTo(final T o) {
        if (o instanceof List) {
            final List l = (List) o;
            if (this.size() == l.size()) {
                final Iterator i1 = iterator();
                final Iterator i2 = l.iterator();
                if (i1.hasNext()) {
                    if (i2.hasNext()) {
                        final int comp = Comparator.<Comparable> naturalOrder()
                                .compare((Comparable) i1.next(), (Comparable) i2.next());
                        if (comp != 0)
                            return comp;
                    }
                    return 1;
                } else {
                    if (i2.hasNext())
                        return -1;
                    else
                        return 0;
                }
            }
            return this.size() - ((List) o).size();
        } else
            return 1;

    }


    @Override
    default <R> ListX<R> retry(final Function<? super T, ? extends R> fn) {
        return (ListX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> ListX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (ListX<R>)LazyCollectionX.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <R> ListX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (ListX<R>)LazyCollectionX.super.flatMap(fn);
    }

    @Override
    default <R> ListX<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (ListX<R>)LazyCollectionX.super.mergeMap(fn);
    }

    @Override
    default ListX<T> prependStream(Stream<? extends T> stream) {
        return (ListX<T>)LazyCollectionX.super.prependStream(stream);
    }

    @Override
    default ListX<T> appendAll(T... values) {
        return (ListX<T>)LazyCollectionX.super.appendAll(values);
    }

    @Override
    default ListX<T> append(T value) {
        return (ListX<T>)LazyCollectionX.super.append(value);
    }

    @Override
    default ListX<T> prepend(T value) {
        return (ListX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default ListX<T> prependAll(T... values) {
        return (ListX<T>)LazyCollectionX.super.prependAll(values);
    }

    @Override
    default ListX<T> insertAt(int pos, T... values) {
        return (ListX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default ListX<T> deleteBetween(int start, int end) {
        return (ListX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default ListX<T> insertStreamAt(int pos, Stream<T> stream) {
        return (ListX<T>)LazyCollectionX.super.insertStreamAt(pos,stream);
    }

    @Override
    default ListX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (ListX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> ListX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (ListX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }

    @Override
    default ListX<T> plusLoop(int max, IntFunction<T> value) {
        return (ListX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default ListX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (ListX<T>)LazyCollectionX.super.plusLoop(supplier);
    }


    /**
     * Narrow a covariant List
     *
     * <pre>
     * {@code
     * ListX<? extends Fruit> list = ListX.of(apple,bannana);
     * ListX<Fruit> fruitList = ListX.narrow(list);
     * }
     * </pre>
     *
     * @param listX to narrow generic type
     * @return ListX with narrowed type
     */
    public static <T> ListX<T> narrow(final ListX<? extends T> listX) {
        return (ListX<T>) listX;
    }

  @Override
    default <T2, R> ListX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return (ListX<R>)LazyCollectionX.super.zip(fn, publisher);
    }



    @Override
    default <U> ListX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (ListX)LazyCollectionX.super.zipWithPublisher(other);
    }


    @Override
    default <S, U, R> ListX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (ListX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> ListX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (ListX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }
    /**
     * Intercalate
     *
     * @param listOfLists List of lists which this IndexedSequenceX instance will be intercalated into.
     * @return List with current IndexedSequenceX inserted between each List.
     */
    default ListX<T> intercalate(List<? extends List<? extends T>> listOfLists) {
        ListX thisListX = this.to().listX();
        if (listOfLists.isEmpty()) {
            return thisListX;
        } else {
            ListX listOfListsX = ListX.fromIterable(listOfLists);
            return listOfListsX.intersperse(thisListX).concatMap(x -> x);
        }
    }
    public static  <T,R> ListX<R> tailRec(T initial, Function<? super T, ? extends Iterable<? extends Either<T, R>>> fn) {
        ListX<Either<T, R>> lazy = ListX.of(Either.left(initial));
        ListX<Either<T, R>> next = lazy.eager();
        boolean newValue[] = {true};
        for(;;){

            next = next.concatMap(e -> e.fold(s -> {
                        newValue[0]=true;
                        return fn.apply(s); },
                    p -> {
                        newValue[0]=false;
                        return ListX.of(e);
                    }));
            if(!newValue[0])
                break;

        }
        return Either.sequenceRight(next)
                     .orElse(ReactiveSeq.empty())
                     .to(ReactiveConvertableSequence::converter)
                     .listX(Evaluation.LAZY);
    }
}
