package cyclops.collections.immutable;


import com.aol.cyclops2.data.collections.extensions.IndexedSequenceX;
import com.aol.cyclops2.data.collections.extensions.lazy.immutable.LazyPVectorX;
import com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX;
import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.async.Future;
import cyclops.control.Either;
import cyclops.typeclasses.*;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.control.lazy.Maybe;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.companion.Reducers;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.vectorX;
import cyclops.stream.ReactiveSeq;
import cyclops.control.lazy.Trampoline;
import cyclops.monads.transformers.ListT;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.types.recoverable.OnEmptySwitch;
import com.aol.cyclops2.types.foldable.To;
import cyclops.monads.WitnessType;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.stream.Spouts;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.experimental.UtilityClass;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple4;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

import static com.aol.cyclops2.types.foldable.Evaluation.LAZY;

/**
 * An eXtended Persistent Vector type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface VectorX<T> extends To<VectorX<T>>,
                                     PVector<T>,
                                     IndexedSequenceX<T>,
                                     LazyCollectionX<T>,
                                     OnEmptySwitch<T, 
                                     PVector<T>>,
                                     Comparable<T>,
                                     Higher<vectorX,T>{
    default Maybe<T> headMaybe(){
        return headAndTail().headMaybe();
    }
    default T head(){
        return headAndTail().head();
    }
    default VectorX<T> tail(){
        return headAndTail().tail().to().vectorX(Evaluation.LAZY);
    }
    VectorX<T> lazy();
    VectorX<T> eager();
    static <T> CompletableVectorX<T> completable(){
        return new CompletableVectorX<>();
    }

    static class CompletableVectorX<T> implements InvocationHandler {
        Future<VectorX<T>> future = Future.future();
        public boolean complete(PVector<T> result){
            return future.complete(VectorX.fromIterable(result));
        }

        public VectorX<T> asVectorX(){
            VectorX f = (VectorX) Proxy.newProxyInstance(VectorX.class.getClassLoader(),
                    new Class[] { VectorX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            VectorX<T> target = future.visit(l->l,t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }
    public static  <T> Kleisli<vectorX,VectorX<T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), VectorX::widen);
    }
    public static <T> Higher<vectorX, T> widen(VectorX<T> narrow) {
        return narrow;
    }
    public static  <T> Cokleisli<vectorX,T,VectorX<T>> kindCokleisli(){
        return Cokleisli.of(VectorX::narrowK);
    }
    public static <W1,T> Nested<vectorX,W1,T> nested(VectorX<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    default <W1> Product<vectorX,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    default <W1> Coproduct<W1,vectorX,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    default Active<vectorX,T> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }
    default <W2,R> Nested<vectorX,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }

    default <W extends WitnessType<W>> ListT<W, T> liftM(W witness) {
        return ListT.of(witness.adapter().unit(this));
    }

    /**
     * Widen a PVectorType nest inside another HKT encoded type
     *
     * @param list HTK encoded type containing  a PVector to widen
     * @return HKT encoded type with a widened PVector
     */
    public static <C2,T> Higher<C2, Higher<vectorX,T>> widen2(Higher<C2, VectorX<T>> list){
        //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
        //cast seems safer as Higher<PVectorType.vectorX,T> must be a PVectorType
        return (Higher)list;
    }
    /**
     * Convert the raw Higher Kinded Type for PVector types into the PVectorType type definition class
     *
     * @param list HKT encoded list into a PVectorType
     * @return PVectorType
     */
    public static <T> VectorX<T> narrowK(final Higher<vectorX, T> list) {
        return (VectorX<T>)list;
    }
    /**
     * Narrow a covariant VectorX
     * 
     * <pre>
     * {@code 
     *  VectorX<? extends Fruit> set = VectorX.of(apple,bannana);
     *  VectorX<Fruit> fruitSet = VectorX.narrow(set);
     * }
     * </pre>
     * 
     * @param vectorX to narrow generic type
     * @return OrderedSetX with narrowed type
     */
    public static <T> VectorX<T> narrow(final VectorX<? extends T> vectorX) {
        return (VectorX<T>) vectorX;
    }
    
    
    /**
     * Create a VectorX that contains the Integers between skip and take
     * 
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range VectorX
     */
    public static VectorX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end).to()
                          .vectorX(LAZY);
    }

    /**
     * Create a VectorX that contains the Longs between skip and take
     * 
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range VectorX
     */
    public static VectorX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end).to()
                          .vectorX(LAZY);
    }

    /**
     * Unfold a function into a VectorX
     * 
     * <pre>
     * {@code 
     *  VectorX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     * 
     * //(1,2,3,4,5)
     * 
     * }</code>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return VectorX generated by unfolder function
     */
    static <U, T> VectorX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder).to()
                          .vectorX(LAZY);
    }

    /**
     * Generate a VectorX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate VectorX elements
     * @return VectorX generated from the provided Supplier
     */
    public static <T> VectorX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit).to()
                          .vectorX(LAZY);
    }  
    /**
     * Generate a VectorX from the provided value up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Value for VectorX elements
     * @return VectorX generated from the provided Supplier
     */
    public static <T> VectorX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit).to()
                          .vectorX(LAZY);
    }

    /**
     * Create a VectorX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return VectorX generated by iterative application
     */
    public static <T> VectorX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit).to()
                          .vectorX(LAZY);

    }

    /**
     * Construct a PVector from the provided values 
     * 
     * <pre>
     * {@code 
     *  List<String> list = PVectors.of("a","b","c");
     *  
     *  // or
     *  
     *  PVector<String> list = PVectors.of("a","b","c");
     *  
     *  
     * }
     * </pre>
     * 
     * 
     * @param values To add to PVector
     * @return new PVector
     */
    public static <T> VectorX<T> of(final T... values) {
        return new LazyPVectorX<>(null,ReactiveSeq.of(values),Reducers.toPVector(), LAZY);
    }
    /**
     * 
     * Construct a VectorX from the provided Iterator
     * 
     * @param it Iterator to populate VectorX
     * @return Newly populated VectorX
     */
    public static <T> VectorX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }
    /**
     * <pre>
     * {@code 
     *     List<String> zero = PVectors.zero();
     *    //or
     *    
     *     PVector<String> zero = PVectors.zero();
     * }
     * </pre>
     * @return an zero PVector
     */
    public static <T> VectorX<T> empty() {
        return new LazyPVectorX<T>(
                                  TreePVector.empty(),null,Reducers.toPVector(), LAZY);
    }

    /**
     * Construct a PVector containing a singleUnsafe value
     * </pre>
     * {@code 
     *    List<String> singleUnsafe = PVectors.singleton("1");
     *    
     *    //or
     *    
     *    PVector<String> singleUnsafe = PVectors.singleton("1");
     * 
     * }
     * </pre>
     * 
     * @param value Active value for PVector
     * @return PVector with a singleUnsafe value
     */
    public static <T> VectorX<T> singleton(final T value) {
        return new LazyPVectorX<>(
                                  TreePVector.singleton(value),null,Reducers.toPVector(), LAZY);
    }

    /**
     * Construct a VectorX from an Publisher
     * 
     * @param publisher
     *            to construct VectorX from
     * @return VectorX
     */
    public static <T> VectorX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher).to()
                          .vectorX(LAZY);
    }

    public static <T> VectorX<T> fromIterable(final Iterable<T> iterable) {
        if (iterable instanceof VectorX)
            return (VectorX) iterable;
        if (iterable instanceof PVector)
            return new LazyPVectorX<>(
                                      (PVector) iterable,null,Reducers.toPVector(), LAZY);

        return new LazyPVectorX<>(null,
                ReactiveSeq.fromIterable(iterable),
                Reducers.toPVector(), LAZY);
    }
    VectorX<T> type(Reducer<? extends PVector<T>> reducer);

    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  VectorX<Integer> bag = vectorX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create VectorX from
     * @param <T> VectorX generated from Stream
     * @return
     */
    public static <T> VectorX<T> vectorX(ReactiveSeq<T> stream) {

        return new LazyPVectorX<T>(null,stream,Reducers.toPVector(), LAZY);
    }


    default AnyMSeq<vectorX,T> anyM(){
        return AnyM.fromVectorX(this);
    }

    /**
     * Reduce (immutable Collection) a Stream to a PVector
     * 
     * <pre>
     * {@code 
     *    PVector<Integer> list = PVectors.fromStream(Stream.of(1,2,3));
     * 
     *  //list = [1,2,3]
     * }</pre>
     * 
     * 
     * @param stream to convert to a PVector
     * @return
     */
    default <T> VectorX<T> fromStream(final ReactiveSeq<T> stream) {
        return Reducers.<T>toVectorX()
                       .mapReduce(stream);
    }

    /**
    * Combine two adjacent elements in a VectorX using the supplied BinaryOperator
    * This is a stateful grouping & reduction operation. The emitted of a combination may in turn be combined
    * with it's neighbor
    * <pre>
    * {@code 
    *  VectorX.of(1,1,2,3)
                 .combine((a, b)->a.equals(b),SemigroupK.intSum)
                 .listX()
                 
    *  //ListX(3,4) 
    * }</pre>
    * 
    * @param predicate Test to see if two neighbors should be joined
    * @param op Reducer to combine neighbors
    * @return Combined / Partially Reduced VectorX
    */
    @Override
    default VectorX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (VectorX<T>) LazyCollectionX.super.combine(predicate, op);
    }
    @Override
    default VectorX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (VectorX<T>)LazyCollectionX.super.combine(op,predicate);
    }
    @Override
    default VectorX<T> materialize() {
        return (VectorX<T>)LazyCollectionX.super.materialize();
    }
 

    @Override
    default VectorX<T> take(final long num) {

        return limit(num);
    }
    @Override
    default VectorX<T> drop(final long num) {

        return skip(num);
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> VectorX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (VectorX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> VectorX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
                                                BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                                Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
                                                Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (VectorX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> VectorX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (VectorX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> VectorX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
                                            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
                                            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        
        return (VectorX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> VectorX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                        BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (VectorX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> VectorX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
                                        BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                        BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        
        return (VectorX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }
    

    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     * 
     * <pre>
     * {@code 
     *   
     *     VectorX.of(1,2,3)
     *          .transform(i->i*2)
     *          .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *      
     *     //VectorX[12]
     * }
     * </pre>
     * 
     * 
     * @param fn mapping function
     * @return Transformed VectorX
     */
    default <R> VectorX<R> coflatMap(Function<? super VectorX<T>, ? extends R> fn){
       return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }



    @Override
    default <X> VectorX<X> from(final Collection<X> col) {
        return fromIterable(col);
    }

    //@Override
    default <T> Reducer<PVector<T>> monoid() {
        return Reducers.toPVector();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#reverse()
     */
    @Override
    default VectorX<T> reverse() {
        return (VectorX<T>) LazyCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> filter(final Predicate<? super T> pred) {
        return (VectorX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> VectorX<R> map(final Function<? super T, ? extends R> mapper) {

        return (VectorX<R>) LazyCollectionX.super.map(mapper);
    }

    @Override
    default <R> VectorX<R> unit(final Collection<R> col) {
        return fromIterable(col);
    }

    @Override
    default <R> VectorX<R> unit(final R value) {
        return singleton(value);
    }

   // @Override
    default <R> VectorX<R> emptyUnit() {
        return empty();
    }

    @Override
    default <R> VectorX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> VectorX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (VectorX<R>) LazyCollectionX.super.flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#limit(long)
     */
    @Override
    default VectorX<T> limit(final long num) {
        return (VectorX<T>) LazyCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#skip(long)
     */
    @Override
    default VectorX<T> skip(final long num) {
        return (VectorX<T>) LazyCollectionX.super.skip(num);
    }

    @Override
    default VectorX<T> takeRight(final int num) {
        return (VectorX<T>) LazyCollectionX.super.takeRight(num);
    }

    @Override
    default VectorX<T> dropRight(final int num) {
        return (VectorX<T>) LazyCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> takeWhile(final Predicate<? super T> p) {
        return (VectorX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> dropWhile(final Predicate<? super T> p) {
        return (VectorX<T>) LazyCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> takeUntil(final Predicate<? super T> p) {
        return (VectorX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> dropUntil(final Predicate<? super T> p) {
        return (VectorX<T>) LazyCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    default <R> VectorX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (VectorX<R>) LazyCollectionX.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#slice(long, long)
     */
    @Override
    default VectorX<T> slice(final long from, final long to) {
        return (VectorX<T>) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> VectorX<T> sorted(final Function<? super T, ? extends U> function) {
        return (VectorX<T>) LazyCollectionX.super.sorted(function);
    }

    @Override
    public VectorX<T> plus(T e);

    @Override
    public VectorX<T> plusAll(Collection<? extends T> list);

    @Override
    public VectorX<T> with(int i, T e);

    @Override
    public VectorX<T> plus(int i, T e);

    @Override
    public VectorX<T> plusAll(int i, Collection<? extends T> list);

    @Override
    public VectorX<T> minus(Object e);

    @Override
    public VectorX<T> minusAll(Collection<?> list);

    @Override
    public VectorX<T> minus(int i);

    @Override
    public VectorX<T> subList(int start, int end);

    @Override
    default VectorX<ListX<T>> grouped(final int groupSize) {
        return (VectorX<ListX<T>>) LazyCollectionX.super.grouped(groupSize);
    }


    @Override
    default <U> VectorX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (VectorX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> VectorX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (VectorX<R>) LazyCollectionX.super.zip(other, zipper);
    }



    @Override
    default <U, R> VectorX<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (VectorX<R>) LazyCollectionX.super.zipS(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#permutations()
     */
    @Override
    default VectorX<ReactiveSeq<T>> permutations() {

        return (VectorX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#combinations(int)
     */
    @Override
    default VectorX<ReactiveSeq<T>> combinations(final int size) {

        return (VectorX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#combinations()
     */
    @Override
    default VectorX<ReactiveSeq<T>> combinations() {
        return (VectorX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }

    @Override
    default VectorX<VectorX<T>> sliding(final int windowSize) {
        return (VectorX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default VectorX<VectorX<T>> sliding(final int windowSize, final int increment) {
        return (VectorX<VectorX<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default VectorX<T> scanLeft(final Monoid<T> monoid) {
        return (VectorX<T>) LazyCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> VectorX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (VectorX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default VectorX<T> scanRight(final Monoid<T> monoid) {
        return (VectorX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> VectorX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (VectorX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#plusInOrder(java.lang.Object)
     */
    @Override
    default VectorX<T> plusInOrder(final T e) {

        return (VectorX<T>) LazyCollectionX.super.plusInOrder(e);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#cycle(int)
     */
    @Override
    default VectorX<T> cycle(final long times) {

        return (VectorX<T>) LazyCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#cycle(com.aol.cyclops2.sequence.Monoid, int)
     */
    @Override
    default VectorX<T> cycle(final Monoid<T> m, final long times) {

        return (VectorX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (VectorX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (VectorX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#zipStream(java.util.stream.Stream)
     */
    @Override
    default <U> VectorX<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return (VectorX) LazyCollectionX.super.zipS(other);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> VectorX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (VectorX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> VectorX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
                                                             final Iterable<? extends T4> fourth) {

        return (VectorX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#zipWithIndex()
     */
    @Override
    default VectorX<Tuple2<T, Long>> zipWithIndex() {

        return (VectorX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#distinct()
     */
    @Override
    default VectorX<T> distinct() {

        return (VectorX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#sorted()
     */
    @Override
    default VectorX<T> sorted() {

        return (VectorX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default VectorX<T> sorted(final Comparator<? super T> c) {

        return (VectorX<T>) LazyCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> skipWhile(final Predicate<? super T> p) {

        return (VectorX<T>) LazyCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> skipUntil(final Predicate<? super T> p) {

        return (VectorX<T>) LazyCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> limitWhile(final Predicate<? super T> p) {

        return (VectorX<T>) LazyCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> limitUntil(final Predicate<? super T> p) {

        return (VectorX<T>) LazyCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default VectorX<T> intersperse(final T value) {

        return (VectorX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#shuffle()
     */
    @Override
    default VectorX<T> shuffle() {

        return (VectorX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#skipLast(int)
     */
    @Override
    default VectorX<T> skipLast(final int num) {

        return (VectorX<T>) LazyCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#limitLast(int)
     */
    @Override
    default VectorX<T> limitLast(final int num) {

        return (VectorX<T>) LazyCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default VectorX<T> onEmptySwitch(final Supplier<? extends PVector<T>> supplier) {
        if (this.isEmpty())
            return VectorX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default VectorX<T> onEmpty(final T value) {

        return (VectorX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default VectorX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (VectorX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> VectorX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (VectorX<T>) LazyCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#shuffle(java.util.Random)
     */
    @Override
    default VectorX<T> shuffle(final Random random) {

        return (VectorX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> VectorX<U> ofType(final Class<? extends U> type) {

        return (VectorX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default VectorX<T> filterNot(final Predicate<? super T> fn) {

        return (VectorX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#notNull()
     */
    @Override
    default VectorX<T> notNull() {

        return (VectorX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default VectorX<T> removeAllS(final Stream<? extends T> stream) {

        return (VectorX<T>) LazyCollectionX.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default VectorX<T> removeAllI(final Iterable<? extends T> it) {

        return (VectorX<T>) LazyCollectionX.super.removeAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default VectorX<T> removeAll(final T... values) {

        return (VectorX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default VectorX<T> retainAllI(final Iterable<? extends T> it) {

        return (VectorX<T>) LazyCollectionX.super.retainAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default VectorX<T> retainAllS(final Stream<? extends T> seq) {

        return (VectorX<T>) LazyCollectionX.super.retainAllS(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default VectorX<T> retainAll(final T... values) {

        return (VectorX<T>) LazyCollectionX.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.persistent.LazyCollectionX#cast(java.lang.Class)
     */
    @Override
    default <U> VectorX<U> cast(final Class<? extends U> type) {

        return (VectorX<U>) LazyCollectionX.super.cast(type);
    }


    @Override
    default <C extends Collection<? super T>> VectorX<C> grouped(final int size, final Supplier<C> supplier) {

        return (VectorX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    @Override
    default VectorX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (VectorX<ListX<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default VectorX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (VectorX<ListX<T>>) LazyCollectionX.super.groupedStatefullyUntil(predicate);
    }

    @Override
    default VectorX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (VectorX<ListX<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends Collection<? super T>> VectorX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (VectorX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends Collection<? super T>> VectorX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (VectorX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default <R> VectorX<R> retry(final Function<? super T, ? extends R> fn) {
        return (VectorX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> VectorX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (VectorX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> VectorX<R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (VectorX<R>)LazyCollectionX.super.flatMapS(fn);
    }

    @Override
    default <R> VectorX<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (VectorX<R>)LazyCollectionX.super.flatMapP(fn);
    }

    @Override
    default VectorX<T> prependS(Stream<? extends T> stream) {
        return (VectorX<T>)LazyCollectionX.super.prependS(stream);
    }

    @Override
    default VectorX<T> append(T... values) {
        return (VectorX<T>)LazyCollectionX.super.append(values);
    }

    @Override
    default VectorX<T> append(T value) {
        return (VectorX<T>)LazyCollectionX.super.append(value);
    }

    @Override
    default VectorX<T> prepend(T value) {
        return (VectorX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default VectorX<T> prepend(T... values) {
        return (VectorX<T>)LazyCollectionX.super.prepend(values);
    }

    @Override
    default VectorX<T> insertAt(int pos, T... values) {
        return (VectorX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default VectorX<T> deleteBetween(int start, int end) {
        return (VectorX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default VectorX<T> insertAtS(int pos, Stream<T> stream) {
        return (VectorX<T>)LazyCollectionX.super.insertAtS(pos,stream);
    }

    @Override
    default VectorX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (VectorX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> VectorX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (VectorX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }

    @Override
    default VectorX<T> plusLoop(int max, IntFunction<T> value) {
        return (VectorX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default VectorX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (VectorX<T>)LazyCollectionX.super.plusLoop(supplier);
    }
    @Override
    default VectorX<T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return (VectorX<T>)LazyCollectionX.super.zip(combiner,app);
    }

    @Override
    default <R> VectorX<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (VectorX<R>)LazyCollectionX.super.zipWith(fn);
    }

    @Override
    default <R> VectorX<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (VectorX<R>)LazyCollectionX.super.zipWithS(fn);
    }

    @Override
    default <R> VectorX<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (VectorX<R>)LazyCollectionX.super.zipWithP(fn);
    }

    @Override
    default <T2, R> VectorX<R> zipP(final Publisher<? extends T2> publisher, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (VectorX<R>)LazyCollectionX.super.zipP(publisher,fn);
    }



    @Override
    default <U> VectorX<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (VectorX)LazyCollectionX.super.zipP(other);
    }


    @Override
    default <S, U, R> VectorX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (VectorX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> VectorX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (VectorX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }


    /**
     * Companion class for creating Type Class instances for working with PVectors
     * @author johnmcclean
     *
     */
    @UtilityClass
    public static class Instances {

        public static InstanceDefinitions<vectorX> definitions(){
            return new InstanceDefinitions<vectorX>() {
                @Override
                public <T, R> Functor<vectorX> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<vectorX> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<vectorX> applicative() {
                    return Instances.zippingApplicative();
                }

                @Override
                public <T, R> Monad<vectorX> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<vectorX>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<vectorX>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<vectorX> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<vectorX>> monadPlus(Monoid<Higher<vectorX, T>> m) {
                    return Maybe.just(Instances.monadPlus((Monoid)m));
                }

                @Override
                public <C2, T> Traverse<vectorX> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<vectorX> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<vectorX>> comonad() {
                    return Maybe.nothing();
                }
                @Override
                public <T> Maybe<Unfoldable<vectorX>> unfoldable() {
                    return Maybe.just(Instances.unfoldable());
                }
            };
        }
        public static Unfoldable<vectorX> unfoldable(){
            return new Unfoldable<vectorX>() {
                @Override
                public <R, T> Higher<vectorX, R> unfold(T b, Function<? super T, Optional<Tuple2<R, T>>> fn) {
                    return VectorX.unfold(b,fn);
                }
            };
        }
        /**
         *
         * Transform a list, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  VectorX<Integer> list = PVectors.functor().transform(i->i*2, Arrays.asPVector(1,2,3));
         *
         *  //[2,4,6]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with PVectors
         * <pre>
         * {@code
         *   VectorX<Integer> list = PVectors.unit()
        .unit("hello")
        .applyHKT(h->PVectors.functor().transform((String v) ->v.length(), h))
        .convert(VectorX::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for PVectors
         */
        public static <T,R>Functor<vectorX> functor(){
            BiFunction<VectorX<T>,Function<? super T, ? extends R>,VectorX<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * VectorX<String> list = PVectors.unit()
        .unit("hello")
        .convert(VectorX::narrowK3);

        //Arrays.asPVector("hello"))
         *
         * }
         * </pre>
         *
         *
         * @return A factory for PVectors
         */
        public static  <T> Pure<vectorX> unit(){
            return General.<vectorX,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.VectorX.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asPVector;
         *
        PVectors.zippingApplicative()
        .ap(widen(asPVector(l1(this::multiplyByTwo))),widen(asPVector(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * VectorX<Function<Integer,Integer>> listFn =PVectors.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(VectorX::narrowK3);

        VectorX<Integer> list = PVectors.unit()
        .unit("hello")
        .applyHKT(h->PVectors.functor().transform((String v) ->v.length(), h))
        .applyHKT(h->PVectors.zippingApplicative().ap(listFn, h))
        .convert(VectorX::narrowK3);

        //Arrays.asPVector("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for PVectors
         */
        public static <T,R> Applicative<vectorX> zippingApplicative(){
            BiFunction<VectorX< Function<T, R>>,VectorX<T>,VectorX<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.VectorX.widen;
         * VectorX<Integer> list  = PVectors.monad()
        .flatMap(i->widen(VectorX.range(0,i)), widen(Arrays.asPVector(1,2,3)))
        .convert(VectorX::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    VectorX<Integer> list = PVectors.unit()
        .unit("hello")
        .applyHKT(h->PVectors.monad().flatMap((String v) ->PVectors.unit().unit(v.length()), h))
        .convert(VectorX::narrowK3);

        //Arrays.asPVector("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for PVectors
         */
        public static <T,R> Monad<vectorX> monad(){

            BiFunction<Higher<vectorX,T>,Function<? super T, ? extends Higher<vectorX,R>>,Higher<vectorX,R>> flatMap = Instances::flatMap;
            return General.monad(zippingApplicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  VectorX<String> list = PVectors.unit()
        .unit("hello")
        .applyHKT(h->PVectors.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(VectorX::narrowK3);

        //Arrays.asPVector("hello"));
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<vectorX> monadZero(){

            return General.monadZero(monad(), VectorX.empty());
        }
        public static <T,R> MonadRec<vectorX> monadRec(){

            return new MonadRec<vectorX>(){
                @Override
                public <T, R> Higher<vectorX, R> tailRec(T initial, Function<? super T, ? extends Higher<vectorX,? extends Either<T, R>>> fn) {
                    VectorX<Either<T, R>> next = VectorX.of(Either.left(initial));
                    boolean newValue[] = {false};
                    for(;;){
                        next = next.flatMap(e -> e.visit(s -> { newValue[0]=true; return narrowK(fn.apply(s)); }, p -> VectorX.of(e)));
                        if(!newValue[0])
                            break;
                    }
                    return Either.sequenceRight(next).map(l -> l.to().vectorX(LAZY)).orElse(VectorX.empty());

                }
            };
        }
        /**
         * <pre>
         * {@code
         *  VectorX<Integer> list = PVectors.<Integer>monadPlus()
        .plus(Arrays.asPVector()), Arrays.asPVector(10)))
        .convert(VectorX::narrowK3);
        //Arrays.asPVector(10))
         *
         * }
         * </pre>
         * @return Type class for combining PVectors by concatenation
         */
        public static <T> MonadPlus<vectorX> monadPlus(){
            Monoid<VectorX<T>> m = Monoid.of(VectorX.empty(), Instances::concat);
            Monoid<Higher<vectorX,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<VectorX<Integer>> m = Monoid.of(Arrays.asPVector()), (a,b)->a.isEmpty() ? b : a);
        VectorX<Integer> list = PVectors.<Integer>monadPlus(m)
        .plus(Arrays.asPVector(5)), Arrays.asPVector(10)))
        .convert(VectorX::narrowK3);
        //Arrays.asPVector(5))
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining PVectors
         * @return Type class for combining PVectors
         */
        public static <T> MonadPlus<vectorX> monadPlus(Monoid<VectorX<T>> m){
            Monoid<Higher<vectorX,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }

        /**
         * @return Type class for traversables with traverse / sequence operations
         */
        public static <C2,T> Traverse<vectorX> traverse(){
            BiFunction<Applicative<C2>,VectorX<Higher<C2, T>>,Higher<C2, VectorX<T>>> sequenceFn = (ap, list) -> {

                Higher<C2,VectorX<T>> identity = ap.unit(VectorX.empty());

                BiFunction<Higher<C2,VectorX<T>>,Higher<C2,T>,Higher<C2,VectorX<T>>> combineToPVector =   (acc, next) -> ap.apBiFn(ap.unit((a, b) ->a.plus(b)),acc,next);

                BinaryOperator<Higher<C2,VectorX<T>>> combinePVectors = (a, b)-> ap.apBiFn(ap.unit((l1, l2)-> l1.plusAll(l2)),a,b); ;

                return list.stream()
                        .reduce(identity,
                                combineToPVector,
                                combinePVectors);


            };
            BiFunction<Applicative<C2>,Higher<vectorX,Higher<C2, T>>,Higher<C2, Higher<vectorX,T>>> sequenceNarrow  =
                    (a,b) -> VectorX.widen2(sequenceFn.apply(a, VectorX.narrowK(b)));
            return General.traverse(zippingApplicative(), sequenceNarrow);
        }

        /**
         *
         * <pre>
         * {@code
         * int sum  = PVectors.foldable()
        .foldLeft(0, (a,b)->a+b, Arrays.asPVector(1,2,3,4)));

        //10
         *
         * }
         * </pre>
         *
         *
         * @return Type class for folding / reduction operations
         */
        public static <T> Foldable<vectorX> foldable(){
            return new Foldable<vectorX>() {
                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<vectorX, T> ds) {
                    return  VectorX.fromIterable(narrowK(ds)).foldRight(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<vectorX, T> ds) {
                    return  VectorX.fromIterable(narrowK(ds)).foldLeft(monoid);
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<vectorX, T> nestedA) {
                    Monoid<? extends R> m = mb;
                    Object x = narrowK(nestedA).map(fn).foldLeft((Monoid) m);
                    return (R)x;

                }
            };

        }

        private static  <T> VectorX<T> concat(PVector<T> l1, PVector<T> l2){

            return VectorX.fromIterable(l1.plusAll(l2));
        }
        private <T> VectorX<T> of(T value){
            return VectorX.of(value);
        }
        private static <T,R> VectorX<R> ap(VectorX<Function< T, R>> lt, VectorX<T> list){
            return VectorX.fromIterable(lt).zip(list,(a, b)->a.apply(b));
        }
        private static <T,R> Higher<vectorX,R> flatMap(Higher<vectorX,T> lt, Function<? super T, ? extends  Higher<vectorX,R>> fn){
            return VectorX.narrowK(lt).flatMap(fn.andThen(VectorX::narrowK));
        }
        private static <T,R> VectorX<R> map(VectorX<T> lt, Function<? super T, ? extends R> fn){
            return lt.map(fn);
        }
    }
    public static  <T,R> VectorX<R> tailRec(T initial, Function<? super T, ? extends VectorX<? extends Either<T, R>>> fn) {
        return ListX.tailRec(initial,fn).to().vectorX(Evaluation.LAZY);
    }
}
