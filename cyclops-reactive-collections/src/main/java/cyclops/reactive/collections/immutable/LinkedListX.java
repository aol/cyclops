package cyclops.reactive.collections.immutable;

import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.data.ReactiveWitness.linkedListX;
import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.lazy.immutable.LazyLinkedListX;
import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.ReactiveReducers;
import cyclops.control.Future;
import cyclops.control.*;

import cyclops.data.Seq;
import com.oath.cyclops.types.foldable.Evaluation;
import cyclops.data.Vector;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.companion.Reducers;
import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.data.collections.extensions.IndexedSequenceX;
import cyclops.reactive.collections.mutable.ListX;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.types.foldable.To;

import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import com.oath.cyclops.types.persistent.PersistentList;
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

import static cyclops.data.tuple.Tuple.tuple;

/**
 * An eXtended Persistent List type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface LinkedListX<T> extends To<LinkedListX<T>>,
                                      PersistentList<T>,
                                      LazyCollectionX<T>,
                                      IndexedSequenceX<T>,
                                      OnEmptySwitch<T, PersistentList<T>>,
                                      Higher<linkedListX,T> {



    public static <T> LinkedListX<T> defer(Supplier<LinkedListX<T>> s){
      return of(s)
        .map(Supplier::get)
        .concatMap(l->l);
    }

    default Tuple2<LinkedListX<T>, LinkedListX<T>> splitAt(int n) {
        materialize();
        return tuple(take(n), drop(n));
    }

    default Tuple2<LinkedListX<T>, LinkedListX<T>> span(Predicate<? super T> pred) {
        return tuple(takeWhile(pred), dropWhile(pred));
    }

    default Tuple2<LinkedListX<T>,LinkedListX<T>> splitBy(Predicate<? super T> test) {
        return span(test.negate());
    }
    default Tuple2<LinkedListX<T>, LinkedListX<T>> partition(final Predicate<? super T> splitter) {

        return tuple(filter(splitter), filter(splitter.negate()));

    }

    @Override
    default boolean isEmpty() {
        return PersistentList.super.isEmpty();
    }



    static <T> CompletableLinkedListX<T> completable(){
        return new CompletableLinkedListX<>();
    }

    static class CompletableLinkedListX<T> implements InvocationHandler {
        Future<LinkedListX<T>> future = Future.future();
        public boolean complete(PersistentList<T> result){
            return future.complete(LinkedListX.fromIterable(result));
        }

        public LinkedListX<T> asLinkedListX(){
            LinkedListX f = (LinkedListX) Proxy.newProxyInstance(LinkedListX.class.getClassLoader(),
                    new Class[] { LinkedListX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            LinkedListX<T> target = future.fold(l->l, t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }


    @Override
    LinkedListX<T> lazy();
    @Override
    LinkedListX<T> eager();


    public static <C2,T> Higher<C2, Higher<linkedListX,T>> widen2(Higher<C2, LinkedListX<T>> list){

        return (Higher)list;
    }
    public static <T> Higher<linkedListX, T> widen(LinkedListX<T> narrow) {
      return narrow;
    }

    public static <T> LinkedListX<T> narrowK(final Higher<linkedListX, T> list) {
        return (LinkedListX<T>)list;
    }


    /**
     * Narrow a covariant LinkedListX
     *
     * <pre>
     * {@code
     *  LinkedListX<? extends Fruit> set = LinkedListX.of(apple,bannana);
     *  LinkedListX<Fruit> fruitSet = LinkedListX.narrow(set);
     * }
     * </pre>
     *
     * @param stackX to narrow generic type
     * @return OrderedSetX with narrowed type
     */
    public static <T> LinkedListX<T> narrow(final LinkedListX<? extends T> stackX) {
        return (LinkedListX<T>) stackX;
    }

    /**
     * Create a LinkedListX that contains the Integers between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range LinkedListX
     */
    public static LinkedListX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end).to(ReactiveConvertableSequence::converter)
                .linkedListX(Evaluation.LAZY);
    }

    /**
     * Create a LinkedListX that contains the Longs between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range LinkedListX
     */
    public static LinkedListX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end).to(ReactiveConvertableSequence::converter)
                .linkedListX(Evaluation.LAZY);
    }

    /**
     * Unfold a function into a LinkedListX
     *
     * <pre>
     * {@code
     *  LinkedListX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     *
     * //(1,2,3,4,5)
     *
     * }</code>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return LinkedListX generated by unfolder function
     */
    static <U, T> LinkedListX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder).to(ReactiveConvertableSequence::converter)
                .linkedListX(Evaluation.LAZY);
    }

    /**
     * Generate a LinkedListX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate LinkedListX elements
     * @return LinkedListX generated from the provided Supplier
     */
    public static <T> LinkedListX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit).to(ReactiveConvertableSequence::converter)
                .linkedListX(Evaluation.LAZY);
    }

    /**
     * Generate a LinkedListX from the provided value up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Value for LinkedListX elements
     * @return LinkedListX generated from the provided Supplier
     */
    public static <T> LinkedListX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit).to(ReactiveConvertableSequence::converter)
                .linkedListX(Evaluation.LAZY);
    }

    /**
     * Create a LinkedListX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return LinkedListX generated by iterative application
     */
    public static <T> LinkedListX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit).to(ReactiveConvertableSequence::converter)
                .linkedListX(Evaluation.LAZY);
    }


    LinkedListX<T> type(Reducer<? extends PersistentList<T>,T> reducer);

    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  LinkedListX<Integer> bag = linkedListX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create LinkedListX from
     * @param <T> LinkedListX generated from Stream
     * @return
     */
    public static <T> LinkedListX<T> linkedListX(ReactiveSeq<T> stream) {
        return new LazyLinkedListX<T>(null,stream,Reducers.toPersistentList(),Evaluation.LAZY);
    }



    /**
     * Construct a Persistent LinkedList from the provided values
     *
     * <pre>
     * {@code
     *  List<String> list = LinkedListX.of("a","b","c");
     *
     *  // or
     *
     *  PStack<String> list = LinkedListX.of("a","b","c");
     *
     *
     * }
     * </pre>
     *
     *
     * @param values To add to PStack
     * @return new PStack
     */
    @SafeVarargs
    public static <T> LinkedListX<T> of(final T... values) {
        return new LazyLinkedListX<>(null,
                                 ReactiveSeq.of(values),Reducers.toPersistentList(), Evaluation.LAZY);
    }
    /**
     *
     * Construct a LinkedListX from the provided Iterator
     *
     * @param it Iterator to populate LinkedListX
     * @return Newly populated LinkedListX
     */
    public static <T> LinkedListX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }
    /**
     * Construct a LinkedListX from an Publisher
     *
     * @param publisher
     *            to construct LinkedListX from
     * @return LinkedListX
     */
    public static <T> LinkedListX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher).to(ReactiveConvertableSequence::converter)
                .linkedListX(Evaluation.LAZY);
    }

    public static <T> LinkedListX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof LinkedListX)
            return (LinkedListX) iterable;
        if (iterable instanceof PersistentList)
            return new LazyLinkedListX<T>(
                    (PersistentList) iterable,null,Reducers.toPersistentList(),Evaluation.LAZY);

        return new LazyLinkedListX<>(null,ReactiveSeq.fromIterable(iterable),Reducers.toPersistentList(),Evaluation.LAZY);

    }






    /**
     * <pre>
     * {@code
     *     List<String> zero = PStack.zero();
     *    //or
     *
     *     PStack<String> zero = PStack.zero();
     * }
     * </pre>
     * @return an zero PStack
     */
    public static <T> LinkedListX<T> empty() {
        return new LazyLinkedListX<>(
                                 Seq.empty(),null,Reducers.toPersistentList(),Evaluation.LAZY);
    }

    /**
     * Construct a PStack containing a single value
     * </pre>
     * {@code
     *    List<String> single = PStacks.singleton("1");
     *
     *    //or
     *
     *    PStack<String> single = PStacks.singleton("1");
     *
     * }
     * </pre>
     *
     * @param value Active value for PVector
     * @return PVector with a single value
     */
    public static <T> LinkedListX<T> singleton(final T value){
        return new LazyLinkedListX<>(
                                 Seq.of(value),null,Reducers.toPersistentList(),Evaluation.LAZY);
    }

    /**
     * Reduce (immutable Collection) a Stream to a PStack, note for efficiency reasons,
     * the emitted PStack is reversed.
     *
     *
     * <pre>
     * {@code
     *    PStack<Integer> list = PStacks.fromStream(Stream.of(1,2,3));
     *
     *  //list = [3,2,1]
     * }</pre>
     *
     *
     * @param stream to convert to a PVector
     * @return
     */
    default <T> LinkedListX<T> fromStream(final ReactiveSeq<T> stream) {
        return ReactiveReducers.<T>toLinkedListX()
                       .foldMap(stream);
    }

    @Override
    default LinkedListX<T> materialize() {
        return (LinkedListX<T>)LazyCollectionX.super.materialize();
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> LinkedListX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                    BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                    Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (LinkedListX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> LinkedListX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                    BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                    Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                    Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (LinkedListX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> LinkedListX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (LinkedListX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> LinkedListX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (LinkedListX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> LinkedListX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (LinkedListX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> LinkedListX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                            BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (LinkedListX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }


    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *     LinkedListX.of(1,2,3)
     *          .map(i->i*2)
     *          .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *     //LinkedListX[12]
     * }
     * </pre>
     *
     *
     * @param fn mapping function
     * @return Transformed LinkedListX
     */
    default <R> LinkedListX<R> coflatMap(Function<? super LinkedListX<T>, ? extends R> fn){
       return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }
    /**
    * Combine two adjacent elements in a LinkedListX using the supplied BinaryOperator
    * This is a stateful grouping & reduction operation. The emitted of a combination may in turn be combined
    * with it's neighbor
    * <pre>
    * {@code
    *  LinkedListX.of(1,1,2,3)
                 .combine((a, b)->a.equals(b),SemigroupK.intSum)
                 .listX()

    *  //ListX(3,4)
    * }</pre>
    *
    * @param predicate Test to see if two neighbors should be joined
    * @param op Reducer to combine neighbors
    * @return Combined / Partially Reduced LinkedListX
    */
    @Override
    default LinkedListX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (LinkedListX<T>) LazyCollectionX.super.combine(predicate, op);
    }

    @Override
    default LinkedListX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (LinkedListX<T>)LazyCollectionX.super.combine(op,predicate);
    }



    @Override
    default <R> LinkedListX<R> unit(final Iterable<R> col) {

        return fromIterable(col);
    }

    @Override
    default <R> LinkedListX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> LinkedListX<R> unitIterable(final Iterable<R> it) {
        return fromIterable(it);
    }

    //@Override
    default <R> LinkedListX<R> emptyUnit() {

        return LinkedListX.<R> empty();
    }



    @Override
    default LinkedListX<T> plusInOrder(final T e) {
        return insertAt(size(), e);
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    @Override
    default <X> LinkedListX<X> from(final Iterable<X> col) {

        return fromIterable(col);
    }

    //@Override
    default <T> Reducer<PersistentList<T>,T> monoid() {
        return Reducers.toPersistentList();

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#reverse()
     */
    @Override
    default LinkedListX<T> reverse() {
        PersistentList<T> reversed = Seq.empty();
        final Iterator<T> it = iterator();
        while (it.hasNext())
            reversed = reversed.insertAt(0, it.next());
        return fromIterable(reversed);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> filter(final Predicate<? super T> pred) {
        return (LinkedListX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> LinkedListX<R> map(final Function<? super T, ? extends R> mapper) {
        return (LinkedListX<R>) LazyCollectionX.super.map(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> LinkedListX<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (LinkedListX) LazyCollectionX.super.concatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limit(long)
     */
    default LinkedListX<T> take(final long num) {

        return (LinkedListX) LazyCollectionX.super.take(num);
    }


    default LinkedListX<T> drop(final long num) {

        return (LinkedListX) LazyCollectionX.super.drop(num);
    }





    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#slice(long, long)
     */
    @Override
    default LinkedListX<T> slice(final long from, final long to) {
        return (LinkedListX) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> LinkedListX<T> sorted(final Function<? super T, ? extends U> function) {
        return (LinkedListX) LazyCollectionX.super.sorted(function);
    }

    @Override
    public LinkedListX<T> removeAll(Iterable<? extends T> list);

    @Override
    public LinkedListX<T> removeValue(T remove);


    @Override
    public LinkedListX<T> updateAt(int i, T e);


    @Override
    public LinkedListX<T> insertAt(int i, T e);

    @Override
    public LinkedListX<T> plus(T e);

    @Override
    public LinkedListX<T> plusAll(Iterable<? extends T> list);


    @Override
    public LinkedListX<T> insertAt(int i, Iterable<? extends T> list);


    @Override
    public LinkedListX<T> removeAt(long i);



    @Override
    default LinkedListX<Vector<T>> grouped(final int groupSize) {
        return (LinkedListX<Vector<T>>) LazyCollectionX.super.grouped(groupSize);
    }

    @Override
    default boolean containsValue(T item) {
        return LazyCollectionX.super.containsValue(item);
    }
    @Override
    default <U> LinkedListX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (LinkedListX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> LinkedListX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (LinkedListX<R>) LazyCollectionX.super.zip(other, zipper);
    }


  /* (non-Javadoc)
   * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#permutations()
   */
    @Override
    default LinkedListX<ReactiveSeq<T>> permutations() {

        return (LinkedListX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#combinations(int)
     */
    @Override
    default LinkedListX<ReactiveSeq<T>> combinations(final int size) {

        return (LinkedListX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#combinations()
     */
    @Override
    default LinkedListX<ReactiveSeq<T>> combinations() {

        return (LinkedListX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }

    @Override
    default LinkedListX<Seq<T>> sliding(final int windowSize) {
        return (LinkedListX<Seq<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default LinkedListX<Seq<T>> sliding(final int windowSize, final int increment) {
        return (LinkedListX<Seq<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default <U> LinkedListX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (LinkedListX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default <U> LinkedListX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (LinkedListX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    @Override
    default LinkedListX<T> scanLeft(final Monoid<T> monoid) {

        return (LinkedListX<T>) LazyCollectionX.super.scanLeft(monoid);

    }

    @Override
    default LinkedListX<T> scanRight(final Monoid<T> monoid) {
        return (LinkedListX<T>) LazyCollectionX.super.scanRight(monoid);

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycle(int)
     */
    @Override
    default LinkedListX<T> cycle(final long times) {

        return (LinkedListX<T>) LazyCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycle(com.oath.cyclops.sequence.Monoid, int)
     */
    @Override
    default LinkedListX<T> cycle(final Monoid<T> m, final long times) {

        return (LinkedListX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (LinkedListX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (LinkedListX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zipStream(java.util.stream.Stream)
     */
    @Override
    default <U> LinkedListX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (LinkedListX) LazyCollectionX.super.zipWithStream(other);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> LinkedListX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (LinkedListX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> LinkedListX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                 final Iterable<? extends T4> fourth) {

        return (LinkedListX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#zipWithIndex()
     */
    @Override
    default LinkedListX<Tuple2<T, Long>> zipWithIndex() {

        return (LinkedListX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#distinct()
     */
    @Override
    default LinkedListX<T> distinct() {

        return (LinkedListX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted()
     */
    @Override
    default LinkedListX<T> sorted() {

        return (LinkedListX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default LinkedListX<T> sorted(final Comparator<? super T> c) {

        return (LinkedListX<T>) LazyCollectionX.super.sorted(c);
    }


    default LinkedListX<T> dropWhile(final Predicate<? super T> p) {

        return (LinkedListX<T>) LazyCollectionX.super.dropWhile(p);
    }


    default LinkedListX<T> dropUntil(final Predicate<? super T> p) {

        return (LinkedListX<T>) LazyCollectionX.super.dropUntil(p);
    }


    default LinkedListX<T> takeWhile(final Predicate<? super T> p) {

        return (LinkedListX<T>) LazyCollectionX.super.takeWhile(p);
    }


    default LinkedListX<T> takeUntil(final Predicate<? super T> p) {

        return (LinkedListX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default LinkedListX<T> intersperse(final T value) {

        return (LinkedListX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#shuffle()
     */
    @Override
    default LinkedListX<T> shuffle() {

        return (LinkedListX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#skipLast(int)
     */
    default LinkedListX<T> dropRight(final int num) {

        return (LinkedListX<T>) LazyCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#limitLast(int)
     */
    default LinkedListX<T> takeRight(final int num) {

        return (LinkedListX<T>) LazyCollectionX.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default LinkedListX<T> onEmptySwitch(final Supplier<? extends PersistentList<T>> supplier) {
        if (this.isEmpty())
            return LinkedListX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default LinkedListX<T> onEmpty(final T value) {

        return (LinkedListX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default LinkedListX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (LinkedListX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#onEmptyError(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> LinkedListX<T> onEmptyError(final Supplier<? extends X> supplier) {

        return (LinkedListX<T>) LazyCollectionX.super.onEmptyError(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#shuffle(java.util.Random)
     */
    @Override
    default LinkedListX<T> shuffle(final Random random) {

        return (LinkedListX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> LinkedListX<U> ofType(final Class<? extends U> type) {

        return (LinkedListX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> filterNot(final Predicate<? super T> fn) {

        return (LinkedListX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#notNull()
     */
    @Override
    default LinkedListX<T> notNull() {

        return (LinkedListX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default LinkedListX<T> removeStream(final Stream<? extends T> stream) {

        return (LinkedListX<T>) LazyCollectionX.super.removeStream(stream);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default LinkedListX<T> removeAll(final T... values) {

        return (LinkedListX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default LinkedListX<T> retainAll(final Iterable<? extends T> it) {

        return (LinkedListX<T>) LazyCollectionX.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default LinkedListX<T> retainStream(final Stream<? extends T> seq) {

        return (LinkedListX<T>) LazyCollectionX.super.retainStream(seq);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default LinkedListX<T> retainAll(final T... values) {

        return (LinkedListX<T>) LazyCollectionX.super.retainAll(values);
    }



    @Override
    default <C extends PersistentCollection<? super T>> LinkedListX<C> grouped(final int size, final Supplier<C> supplier) {

        return (LinkedListX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    @Override
    default LinkedListX<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (LinkedListX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default LinkedListX<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return (LinkedListX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default LinkedListX<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (LinkedListX<Vector<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends PersistentCollection<? super T>> LinkedListX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (LinkedListX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends PersistentCollection<? super T>> LinkedListX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (LinkedListX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default <R> LinkedListX<R> retry(final Function<? super T, ? extends R> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> LinkedListX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (LinkedListX<R>)LazyCollectionX.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <R> LinkedListX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.flatMap(fn);
    }

    @Override
    default <R> LinkedListX<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.mergeMap(fn);
    }
    @Override
    default <R> LinkedListX<R> mergeMap(int maxConcurency, Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.mergeMap(maxConcurency,fn);
    }


    @Override
    default LinkedListX<T> removeFirst(Predicate<? super T> pred) {
        return (LinkedListX<T>)LazyCollectionX.super.removeFirst(pred);
    }

    @Override
    default LinkedListX<T> appendAll(Iterable<? extends T> value) {
        return (LinkedListX<T>)LazyCollectionX.super.appendAll(value);
    }

    @Override
    default LinkedListX<T> prependAll(Iterable<? extends T> value) {
        return (LinkedListX<T>)LazyCollectionX.super.prependAll(value);
    }


    @Override
    default LinkedListX<T> insertAt(int pos, ReactiveSeq<? extends T> values) {
        return (LinkedListX<T>)LazyCollectionX.super.insertAt(pos,values);
    }


    @Override
    default LinkedListX<T> prependStream(Stream<? extends T> stream) {
        return (LinkedListX<T>)LazyCollectionX.super.prependStream(stream);
    }

    @Override
    default LinkedListX<T> appendAll(T... values) {
        return (LinkedListX<T>)LazyCollectionX.super.appendAll(values);
    }

    @Override
    default LinkedListX<T> append(T value) {
        return (LinkedListX<T>)LazyCollectionX.super.append(value);
    }

    @Override
    default LinkedListX<T> prepend(T value) {
        return (LinkedListX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default LinkedListX<T> prependAll(T... values) {
        return (LinkedListX<T>)LazyCollectionX.super.prependAll(values);
    }

    @Override
    default LinkedListX<T> insertAt(int pos, T... values) {
        return (LinkedListX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default LinkedListX<T> deleteBetween(int start, int end) {
        return (LinkedListX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default LinkedListX<T> insertStreamAt(int pos, Stream<T> stream) {
        return (LinkedListX<T>)LazyCollectionX.super.insertStreamAt(pos,stream);
    }

    @Override
    default LinkedListX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (LinkedListX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> LinkedListX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (LinkedListX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }

    @Override
    default LinkedListX<T> plusLoop(int max, IntFunction<T> value) {
        return (LinkedListX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default LinkedListX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (LinkedListX<T>)LazyCollectionX.super.plusLoop(supplier);
    }

  @Override
    default <T2, R> LinkedListX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return (LinkedListX<R>)LazyCollectionX.super.zip(fn, publisher);
    }



    @Override
    default <U> LinkedListX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (LinkedListX)LazyCollectionX.super.zipWithPublisher(other);
    }


    @Override
    default <S, U, R> LinkedListX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (LinkedListX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> LinkedListX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }
    @Override
    default LinkedListX<T> removeAll(CollectionX<? extends T> it) {
        return removeAll((Iterable<T>)it);
    }




    public static  <T,R> LinkedListX<R> tailRec(T initial, Function<? super T, ? extends LinkedListX<? extends Either<T, R>>> fn) {
       return ListX.tailRec(initial,fn).to().linkedListX(Evaluation.LAZY);
    }
}
