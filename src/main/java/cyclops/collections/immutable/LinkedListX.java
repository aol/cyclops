package cyclops.collections.immutable;


import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyLinkedListX;
import com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX;
import com.aol.cyclops2.hkt.Higher;
import cyclops.async.Future;
import cyclops.control.Eval;
import cyclops.control.Xor;

import cyclops.monads.Witness;
import cyclops.typeclasses.*;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.companion.Reducers;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.linkedListX;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import cyclops.monads.transformers.ListT;
import com.aol.cyclops2.data.collections.extensions.IndexedSequenceX;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.types.recoverable.OnEmptySwitch;
import com.aol.cyclops2.types.foldable.To;
import cyclops.monads.WitnessType;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.stream.Spouts;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.aol.cyclops2.types.foldable.Evaluation.LAZY;

/**
 * An eXtended Persistent List type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface LinkedListX<T> extends To<LinkedListX<T>>,
                                    PStack<T>,
                                    LazyCollectionX<T>,
                                    IndexedSequenceX<T>,
                                    OnEmptySwitch<T, PStack<T>>,
                                    Higher<linkedListX,T> {


    default Maybe<T> headMaybe(){
        return headAndTail().headMaybe();
    }
    default T head(){
        return headAndTail().head();
    }
    default LinkedListX<T> tail(){
        return headAndTail().tail().to().linkedListX(Evaluation.LAZY);
    }


    static <T> CompletableLinkedListX<T> completable(){
        return new CompletableLinkedListX<>();
    }

    static class CompletableLinkedListX<T> implements InvocationHandler {
        Future<LinkedListX<T>> future = Future.future();
        public boolean complete(PStack<T> result){
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
            LinkedListX<T> target = future.get();
            return method.invoke(target,args);
        }
    }
    public static  <T> Kleisli<linkedListX,LinkedListX<T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), LinkedListX::widen);
    }
    public static <T> Higher<linkedListX, T> widen(LinkedListX<T> narrow) {
        return narrow;
    }
    public static  <T> Cokleisli<linkedListX,T,LinkedListX<T>> kindCokleisli(){
        return Cokleisli.of(LinkedListX::narrowK);
    }
    public static <W1,T> Nested<linkedListX,W1,T> nested(LinkedListX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    default <W1> Product<linkedListX,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,linkedListX,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<linkedListX,T> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }
    default <W2,R> Nested<linkedListX,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }

    @Override
    LinkedListX<T> lazy();
    @Override
    LinkedListX<T> eager();

    /**
     * Widen a PStackType nest inside another HKT encoded type
     *
     * @param list HTK encoded type containing  a PStack to widen
     * @return HKT encoded type with a widened PStack
     */
    public static <C2,T> Higher<C2, Higher<linkedListX,T>> widen2(Higher<C2, LinkedListX<T>> list){
        //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
        //cast seems safer as Higher<PStackType.linkedListX,T> must be a PStackType
        return (Higher)list;
    }
    /**
     * Convert the raw Higher Kinded Type for PStack types into the PStackType type definition class
     *
     * @param list HKT encoded list into a PStackType
     * @return PStackType
     */
    public static <T> LinkedListX<T> narrowK(final Higher<linkedListX, T> list) {
        return (LinkedListX<T>)list;
    }

    default <W extends WitnessType<W>> ListT<W, T> liftM(W witness) {
        return ListT.of(witness.adapter().unit(this));
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
        return ReactiveSeq.range(start, end).to()
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
        return ReactiveSeq.rangeLong(start, end).to()
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
    static <U, T> LinkedListX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder).to()
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
                          .limit(limit).to()
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
                          .limit(limit).to()
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
                          .limit(limit).to()
                .linkedListX(Evaluation.LAZY);
    }


    LinkedListX<T> type(Reducer<? extends PStack<T>> reducer);

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
     * @param <T> LinkedListX generated from LazyList
     * @return
     */
    public static <T> LinkedListX<T> linkedListX(ReactiveSeq<T> stream) {
        return new LazyLinkedListX<T>(null,stream,Reducers.toPStack(),Evaluation.LAZY);
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
                                 ReactiveSeq.of(values),Reducers.toPStack(), Evaluation.LAZY);
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
        return Spouts.from((Publisher<T>) publisher).to()
                .linkedListX(Evaluation.LAZY);
    }

    public static <T> LinkedListX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof LinkedListX)
            return (LinkedListX) iterable;
        if (iterable instanceof PStack)
            return new LazyLinkedListX<T>(
                    (PStack) iterable,null,Reducers.toPStack(),Evaluation.LAZY);

        return new LazyLinkedListX<>(null,ReactiveSeq.fromIterable(iterable),Reducers.toPStack(),Evaluation.LAZY);

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
                                 ConsPStack.empty(),null,Reducers.toPStack(),Evaluation.LAZY);
    }

    /**
     * Construct a PStack containing a singleUnsafe value
     * </pre>
     * {@code 
     *    List<String> singleUnsafe = PStacks.singleton("1");
     *    
     *    //or
     *    
     *    PStack<String> singleUnsafe = PStacks.singleton("1");
     * 
     * }
     * </pre>
     * 
     * @param value Active value for PVector
     * @return PVector with a singleUnsafe value
     */
    public static <T> LinkedListX<T> singleton(final T value){
        return new LazyLinkedListX<>(
                                 ConsPStack.singleton(value),null,Reducers.toPStack(),Evaluation.LAZY);
    }

    /**
     * Reduce (immutable Collection) a LazyList to a PStack, note for efficiency reasons,
     * the emitted PStack is reversed.
     * 
     * 
     * <pre>
     * {@code 
     *    PStack<Integer> list = PStacks.fromStream(LazyList.of(1,2,3));
     * 
     *  //list = [3,2,1]
     * }</pre>
     * 
     * 
     * @param stream to convert to a PVector
     * @return
     */
    default <T> LinkedListX<T> fromStream(final ReactiveSeq<T> stream) {
        return Reducers.<T>toLinkedListX()
                       .mapReduce(stream);
    }

    default AnyMSeq<linkedListX,T> anyM(){
        return AnyM.fromLinkedListX(this);
    }
    @Override
    default LinkedListX<T> materialize() {
        return (LinkedListX<T>)LazyCollectionX.super.materialize();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> LinkedListX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                    BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                    Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                    Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (LinkedListX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> LinkedListX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                    BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                    Fn3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                    Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                    Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (LinkedListX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> LinkedListX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (LinkedListX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> LinkedListX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                                BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                                Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (LinkedListX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> LinkedListX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (LinkedListX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> LinkedListX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                            BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (LinkedListX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }
    
    @Override
    default LinkedListX<T> take(final long num) {

        return limit(num);
    }
    @Override
    default LinkedListX<T> drop(final long num) {

        return skip(num);
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
    default <R> LinkedListX<R> unit(final Collection<R> col) {

        return fromIterable(col);
    }

    @Override
    default <R> LinkedListX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> LinkedListX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

    //@Override
    default <R> LinkedListX<R> emptyUnit() {

        return LinkedListX.<R> empty();
    }



    @Override
    default LinkedListX<T> plusInOrder(final T e) {
        return plus(size(), e);
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    @Override
    default <X> LinkedListX<X> from(final Collection<X> col) {

        return fromIterable(col);
    }

    //@Override
    default <T> Reducer<PStack<T>> monoid() {
        return Reducers.toPStack();

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#reverse()
     */
    @Override
    default LinkedListX<T> reverse() {
        PStack<T> reversed = ConsPStack.empty();
        final Iterator<T> it = iterator();
        while (it.hasNext())
            reversed = reversed.plus(0, it.next());
        return fromIterable(reversed);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> filter(final Predicate<? super T> pred) {
        return (LinkedListX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#map(java.util.function.Function)
     */
    @Override
    default <R> LinkedListX<R> map(final Function<? super T, ? extends R> mapper) {
        return (LinkedListX<R>) LazyCollectionX.super.map(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> LinkedListX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (LinkedListX) LazyCollectionX.super.flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#limit(long)
     */
    @Override
    default LinkedListX<T> limit(final long num) {

        return (LinkedListX) LazyCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#skip(long)
     */
    @Override
    default LinkedListX<T> skip(final long num) {

        return (LinkedListX) LazyCollectionX.super.skip(num);
    }

    @Override
    default LinkedListX<T> takeRight(final int num) {
        return (LinkedListX<T>) LazyCollectionX.super.takeRight(num);
    }

    @Override
    default LinkedListX<T> dropRight(final int num) {
        return (LinkedListX<T>) LazyCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> takeWhile(final Predicate<? super T> p) {

        return (LinkedListX) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> dropWhile(final Predicate<? super T> p) {

        return (LinkedListX) LazyCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> takeUntil(final Predicate<? super T> p) {

        return (LinkedListX) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> dropUntil(final Predicate<? super T> p) {
        return (LinkedListX) LazyCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> LinkedListX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (LinkedListX) LazyCollectionX.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#slice(long, long)
     */
    @Override
    default LinkedListX<T> slice(final long from, final long to) {
        return (LinkedListX) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> LinkedListX<T> sorted(final Function<? super T, ? extends U> function) {
        return (LinkedListX) LazyCollectionX.super.sorted(function);
    }

    @Override
    public LinkedListX<T> minusAll(Collection<?> list);

    @Override
    public LinkedListX<T> minus(Object remove);

    /**
     * @param i
     * @param e
     * @return
     * @see org.pcollections.PStack#with(int, java.lang.Object)
     */
    @Override
    public LinkedListX<T> with(int i, T e);

    /**
     * @param i
     * @param e
     * @return
     * @see org.pcollections.PStack#plus(int, java.lang.Object)
     */
    @Override
    public LinkedListX<T> plus(int i, T e);

    @Override
    public LinkedListX<T> plus(T e);

    @Override
    public LinkedListX<T> plusAll(Collection<? extends T> list);

    /**
     * @param i
     * @param list
     * @return
     * @see org.pcollections.PStack#plusAll(int, java.util.Collection)
     */
    @Override
    public LinkedListX<T> plusAll(int i, Collection<? extends T> list);

    /**
     * @param i
     * @return
     * @see org.pcollections.PStack#minus(int)
     */
    @Override
    public LinkedListX<T> minus(int i);

    @Override
    public LinkedListX<T> subList(int start, int end);

    @Override
    default LinkedListX<ListX<T>> grouped(final int groupSize) {
        return (LinkedListX<ListX<T>>) LazyCollectionX.super.grouped(groupSize);
    }

    @Override
    default <K, A, D> LinkedListX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {
        return (LinkedListX) LazyCollectionX.super.grouped(classifier, downstream);
    }

    @Override
    default <K> LinkedListX<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return (LinkedListX) LazyCollectionX.super.grouped(classifier);
    }

    @Override
    default <U> LinkedListX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (LinkedListX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> LinkedListX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (LinkedListX<R>) LazyCollectionX.super.zip(other, zipper);
    }


    @Override
    default <U, R> LinkedListX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (LinkedListX<R>) LazyCollectionX.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#permutations()
     */
    @Override
    default LinkedListX<ReactiveSeq<T>> permutations() {

        return (LinkedListX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#combinations(int)
     */
    @Override
    default LinkedListX<ReactiveSeq<T>> combinations(final int size) {

        return (LinkedListX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#combinations()
     */
    @Override
    default LinkedListX<ReactiveSeq<T>> combinations() {

        return (LinkedListX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }

    @Override
    default LinkedListX<VectorX<T>> sliding(final int windowSize) {
        return (LinkedListX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default LinkedListX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return (LinkedListX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize, increment);
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
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#cycle(int)
     */
    @Override
    default LinkedListX<T> cycle(final long times) {

        return (LinkedListX<T>) LazyCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default LinkedListX<T> cycle(final Monoid<T> m, final long times) {

        return (LinkedListX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (LinkedListX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (LinkedListX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#zipStream(java.util.reactiveStream.LazyList)
     */
    @Override
    default <U> LinkedListX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (LinkedListX) LazyCollectionX.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#zip3(java.util.reactiveStream.LazyList, java.util.reactiveStream.LazyList)
     */
    @Override
    default <S, U> LinkedListX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (LinkedListX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#zip4(java.util.reactiveStream.LazyList, java.util.reactiveStream.LazyList, java.util.reactiveStream.LazyList)
     */
    @Override
    default <T2, T3, T4> LinkedListX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                                 final Iterable<? extends T4> fourth) {

        return (LinkedListX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#zipWithIndex()
     */
    @Override
    default LinkedListX<Tuple2<T, Long>> zipWithIndex() {

        return (LinkedListX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#distinct()
     */
    @Override
    default LinkedListX<T> distinct() {

        return (LinkedListX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#sorted()
     */
    @Override
    default LinkedListX<T> sorted() {

        return (LinkedListX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default LinkedListX<T> sorted(final Comparator<? super T> c) {

        return (LinkedListX<T>) LazyCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> skipWhile(final Predicate<? super T> p) {

        return (LinkedListX<T>) LazyCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> skipUntil(final Predicate<? super T> p) {

        return (LinkedListX<T>) LazyCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> limitWhile(final Predicate<? super T> p) {

        return (LinkedListX<T>) LazyCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> limitUntil(final Predicate<? super T> p) {

        return (LinkedListX<T>) LazyCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default LinkedListX<T> intersperse(final T value) {

        return (LinkedListX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#shuffle()
     */
    @Override
    default LinkedListX<T> shuffle() {

        return (LinkedListX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#skipLast(int)
     */
    @Override
    default LinkedListX<T> skipLast(final int num) {

        return (LinkedListX<T>) LazyCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#limitLast(int)
     */
    @Override
    default LinkedListX<T> limitLast(final int num) {

        return (LinkedListX<T>) LazyCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default LinkedListX<T> onEmptySwitch(final Supplier<? extends PStack<T>> supplier) {
        if (this.isEmpty())
            return LinkedListX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default LinkedListX<T> onEmpty(final T value) {

        return (LinkedListX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default LinkedListX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (LinkedListX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> LinkedListX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (LinkedListX<T>) LazyCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#shuffle(java.util.Random)
     */
    @Override
    default LinkedListX<T> shuffle(final Random random) {

        return (LinkedListX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> LinkedListX<U> ofType(final Class<? extends U> type) {

        return (LinkedListX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default LinkedListX<T> filterNot(final Predicate<? super T> fn) {

        return (LinkedListX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#notNull()
     */
    @Override
    default LinkedListX<T> notNull() {

        return (LinkedListX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#removeAll(java.util.reactiveStream.LazyList)
     */
    @Override
    default LinkedListX<T> removeAllS(final Stream<? extends T> stream) {

        return (LinkedListX<T>) LazyCollectionX.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default LinkedListX<T> removeAllI(final Iterable<? extends T> it) {

        return (LinkedListX<T>) LazyCollectionX.super.removeAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default LinkedListX<T> removeAll(final T... values) {

        return (LinkedListX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default LinkedListX<T> retainAllI(final Iterable<? extends T> it) {

        return (LinkedListX<T>) LazyCollectionX.super.retainAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#retainAllI(java.util.reactiveStream.LazyList)
     */
    @Override
    default LinkedListX<T> retainAllS(final Stream<? extends T> seq) {

        return (LinkedListX<T>) LazyCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default LinkedListX<T> retainAll(final T... values) {

        return (LinkedListX<T>) LazyCollectionX.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#cast(java.lang.Class)
     */
    @Override
    default <U> LinkedListX<U> cast(final Class<? extends U> type) {

        return (LinkedListX<U>) LazyCollectionX.super.cast(type);
    }


    @Override
    default <C extends Collection<? super T>> LinkedListX<C> grouped(final int size, final Supplier<C> supplier) {

        return (LinkedListX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    @Override
    default LinkedListX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (LinkedListX<ListX<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default LinkedListX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (LinkedListX<ListX<T>>) LazyCollectionX.super.groupedStatefullyUntil(predicate);
    }

    @Override
    default LinkedListX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (LinkedListX<ListX<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> LinkedListX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (LinkedListX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends Collection<? super T>> LinkedListX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (LinkedListX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default <R> LinkedListX<R> retry(final Function<? super T, ? extends R> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> LinkedListX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (LinkedListX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> LinkedListX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> LinkedListX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.flatMapP(fn);
    }

    @Override
    default LinkedListX<T> prependS(Stream<? extends T> stream) {
        return (LinkedListX<T>)LazyCollectionX.super.prependS(stream);
    }

    @Override
    default LinkedListX<T> append(T... values) {
        return (LinkedListX<T>)LazyCollectionX.super.append(values);
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
    default LinkedListX<T> prepend(T... values) {
        return (LinkedListX<T>)LazyCollectionX.super.prepend(values);
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
    default LinkedListX<T> insertAtS(int pos, Stream<T> stream) {
        return (LinkedListX<T>)LazyCollectionX.super.insertAtS(pos,stream);
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
    default LinkedListX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (LinkedListX<T>)LazyCollectionX.super.plusLoop(supplier);
    }

    @Override
    default LinkedListX<T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return (LinkedListX<T>)LazyCollectionX.super.zip(combiner,app);
    }

    @Override
    default <R> LinkedListX<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.zipWith(fn);
    }

    @Override
    default <R> LinkedListX<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.zipWithS(fn);
    }

    @Override
    default <R> LinkedListX<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.zipWithP(fn);
    }

    @Override
    default <T2, R> LinkedListX<R> zipP(final Publisher<? extends T2> publisher, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.zipP(publisher,fn);
    }



    @Override
    default <U> LinkedListX<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (LinkedListX)LazyCollectionX.super.zipP(other);
    }


    @Override
    default <S, U, R> LinkedListX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (LinkedListX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> LinkedListX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (LinkedListX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }

    /**
     * Companion class for creating Type Class instances for working with PStacks
     * @author johnmcclean
     *
     */
    @UtilityClass
    public static class Instances {

        public static InstanceDefinitions<linkedListX> definitions(){
            return new InstanceDefinitions<linkedListX>() {
                @Override
                public <T, R> Functor<linkedListX> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<linkedListX> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<linkedListX> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<linkedListX> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<linkedListX>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<linkedListX>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<linkedListX> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<linkedListX>> monadPlus(Monoid<Higher<linkedListX, T>> m) {
                    return Maybe.just(Instances.monadPlus((Monoid)m));
                }

                @Override
                public <C2, T> Traverse<linkedListX> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<linkedListX> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<linkedListX>> comonad() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<Unfoldable<linkedListX>> unfoldable() {
                    return Maybe.just(Instances.unfoldable());
                }
            };
        }
        public static Unfoldable<linkedListX> unfoldable(){
            return new Unfoldable<linkedListX>() {
                @Override
                public <R, T> Higher<linkedListX, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return LinkedListX.unfold(b,fn);
                }
            };
        }
        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  LinkedListX<Integer> list = PStacks.functor().map(i->i*2, LinkedListX.widen(Arrays.asPStack(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with PStacks
         * <pre>
         * {@code
         *   LinkedListX<Integer> list = PStacks.unit()
        .unit("hello")
        .applyHKT(h->PStacks.functor().map((String v) ->v.length(), h))
        .convert(LinkedListX::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for PStacks
         */
        public static <T,R>Functor<linkedListX> functor(){
            BiFunction<LinkedListX<T>,Function<? super T, ? extends R>,LinkedListX<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * LinkedListX<String> list = PStacks.unit()
        .unit("hello")
        .convert(LinkedListX::narrowK3);

        //Arrays.asPStack("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for PStacks
         */
        public static <T> Pure<linkedListX> unit(){
            return General.<linkedListX,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.LinkedListX.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asPStack;
         *
        PStacks.zippingApplicative()
        .ap(widen(asPStack(l1(this::multiplyByTwo))),widen(asPStack(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * LinkedListX<Function<Integer,Integer>> listFn =PStacks.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(LinkedListX::narrowK3);

        LinkedListX<Integer> list = PStacks.unit()
        .unit("hello")
        .applyHKT(h->PStacks.functor().map((String v) ->v.length(), h))
        .applyHKT(h->PStacks.zippingApplicative().ap(listFn, h))
        .convert(LinkedListX::narrowK3);

        //Arrays.asPStack("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for PStacks
         */
        public static <T,R> Applicative<linkedListX> zippingApplicative(){
            BiFunction<LinkedListX< Function<T, R>>,LinkedListX<T>,LinkedListX<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.LinkedListX.widen;
         * LinkedListX<Integer> list  = PStacks.monad()
        .flatMap(i->widen(LinkedListX.range(0,i)), widen(Arrays.asPStack(1,2,3)))
        .convert(LinkedListX::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    LinkedListX<Integer> list = PStacks.unit()
        .unit("hello")
        .applyHKT(h->PStacks.monad().flatMap((String v) ->PStacks.unit().unit(v.length()), h))
        .convert(LinkedListX::narrowK3);

        //Arrays.asPStack("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for PStacks
         */
        public static <T,R> Monad<linkedListX> monad(){

            BiFunction<Higher<linkedListX,T>,Function<? super T, ? extends Higher<linkedListX,R>>,Higher<linkedListX,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  LinkedListX<String> list = PStacks.unit()
        .unit("hello")
        .applyHKT(h->PStacks.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(LinkedListX::narrowK3);

        //Arrays.asPStack("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<linkedListX> monadZero(){

            return General.monadZero(monad(), LinkedListX.empty());
        }
        /**
         * <pre>
         * {@code
         *  LinkedListX<Integer> list = PStacks.<Integer>monadPlus()
        .plus(LinkedListX.widen(Arrays.asPStack()), LinkedListX.widen(Arrays.asPStack(10)))
        .convert(LinkedListX::narrowK3);
        //Arrays.asPStack(10))
         *
         * }
         * </pre>
         * @return Type class for combining PStacks by concatenation
         */
        public static <T> MonadPlus<linkedListX> monadPlus(){
            Monoid<LinkedListX<T>> m = Monoid.of(LinkedListX.empty(), Instances::concat);
            Monoid<Higher<linkedListX,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        public static <T,R> MonadRec<linkedListX> monadRec(){

            return new MonadRec<linkedListX>(){
                @Override
                public <T, R> Higher<linkedListX, R> tailRec(T initial, Function<? super T, ? extends Higher<linkedListX,? extends Xor<T, R>>> fn) {
                    return LinkedListX.tailRec(initial,fn.andThen(LinkedListX::narrowK));
                }
            };
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<LinkedListX<Integer>> m = Monoid.of(LinkedListX.widen(Arrays.asPStack()), (a,b)->a.isEmpty() ? b : a);
        LinkedListX<Integer> list = PStacks.<Integer>monadPlus(m)
        .plus(LinkedListX.widen(Arrays.asPStack(5)), LinkedListX.widen(Arrays.asPStack(10)))
        .convert(LinkedListX::narrowK3);
        //Arrays.asPStack(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining PStacks
         * @return Type class for combining PStacks
         */
        public static <T> MonadPlus<linkedListX> monadPlus(Monoid<LinkedListX<T>> m){
            Monoid<Higher<linkedListX,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<linkedListX> traverse(){
            BiFunction<Applicative<C2>,LinkedListX<Higher<C2, T>>,Higher<C2, LinkedListX<T>>> sequenceFn = (ap, list) -> {

                Higher<C2,LinkedListX<T>> identity = ap.unit(LinkedListX.empty());

                BiFunction<Higher<C2,LinkedListX<T>>,Higher<C2,T>,Higher<C2,LinkedListX<T>>> combineToPStack =   (acc, next) -> ap.apBiFn(ap.unit((a, b) ->a.plus(b)),acc,next);

                BinaryOperator<Higher<C2,LinkedListX<T>>> combinePStacks = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> l1.plusAll(l2)),a,b); ;


                return list.stream()
                           .reverse()
                           .reduce(identity,
                                combineToPStack,
                                combinePStacks);


            };
            BiFunction<Applicative<C2>,Higher<linkedListX,Higher<C2, T>>,Higher<C2, Higher<linkedListX,T>>> sequenceNarrow  =
                    (a,b) -> LinkedListX.widen2(sequenceFn.apply(a, LinkedListX.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = PStacks.foldable()
        .foldLeft(0, (a,b)->a+b, LinkedListX.widen(Arrays.asPStack(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T,R> Foldable<linkedListX> foldable(){
            BiFunction<Monoid<T>,Higher<linkedListX,T>,T> foldRightFn =  (m, l)-> LinkedListX.narrowK(l).foldRight(m);
            BiFunction<Monoid<T>,Higher<linkedListX,T>,T> foldLeftFn = (m, l)-> LinkedListX.narrowK(l).reduce(m);
            Fn3<Monoid<R>, Function<T, R>, Higher<linkedListX, T>, R> foldMapFn = (m,f,l)->narrowK(l).map(f).foldLeft(m);

            return General.foldable(foldRightFn, foldLeftFn,foldMapFn);
        }

        private static  <T> LinkedListX<T> concat(PStack<T> l1, PStack<T> l2){

            return LinkedListX.fromIterable(l1.plusAll(l2));
        }
        private <T> LinkedListX<T> of(T value){
            return LinkedListX.singleton(value);
        }
        private static <T,R> LinkedListX<R> ap(LinkedListX<Function< T, R>> lt, LinkedListX<T> list){
            return LinkedListX.fromIterable(lt).zip(list,(a, b)->a.apply(b));
        }
        private static <T,R> Higher<linkedListX,R> flatMap(Higher<linkedListX,T> lt, Function<? super T, ? extends  Higher<linkedListX,R>> fn){
            return LinkedListX.fromIterable(LinkedListX.narrowK(lt)).flatMap(fn.andThen(LinkedListX::narrowK));
        }
        private static <T,R> LinkedListX<R> map(LinkedListX<T> lt, Function<? super T, ? extends R> fn){
            return LinkedListX.fromIterable(lt).map(fn);
        }
    }

    public static  <T,R> LinkedListX<R> tailRec(T initial, Function<? super T, ? extends LinkedListX<? extends Xor<T, R>>> fn) {
       return ListX.tailRec(initial,fn).to().linkedListX(Evaluation.LAZY);
    }
}
