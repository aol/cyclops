package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.control.monads.transformers.values.ValueTransformerSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad Transformer for Java  CompletableFutures nested within Sequential or non-scalar data types (e.g. Lists, Streams etc)
 * 
 * CompletableFutureT allows the deeply wrapped CompletableFuture to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <A> Type of data stored inside the nested CompletableFutures
 */
public class CompletableFutureTSeq<A>
        implements CompletableFutureT<A>, ValueTransformerSeq<A>, IterableFoldable<A>, ConvertableSequence<A>, CyclopsCollectable<A>, Sequential<A> {

    private final AnyMSeq<CompletableFuture<A>> run;

    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyMSeq<CompletableFuture<A>> unwrap() {
        return run;
    }

    private CompletableFutureTSeq(final AnyMSeq<CompletableFuture<A>> run) {
        this.run = run;
    }

    /**
    * Filter the wrapped Maybe
    * 
    * <pre>
    * {@code 
    *    MaybeT.of(AnyM.fromStream(Maybe.of(10))
    *             .filter(t->t!=10);
    *             
    *     //MaybeT<AnyMSeq<Stream<Maybe.empty>>>
    * }
    * </pre>
    * 
    * @param test
    *            Predicate to filter the wrapped Maybe
    * @return MaybeT that applies the provided filter
    */
    @Override
    public MaybeTSeq<A> filter(final Predicate<? super A> test) {
        return MaybeTSeq.of(run.map(opt -> FutureW.of(opt)
                                                  .filter(test)));
    }

    /**
     * Peek at the current value of the CompletableFuture
     * <pre>
     * {@code 
     *    CompletableFutureT.of(AnyM.fromStream(Arrays.asCompletableFuture(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of CompletableFuture
     * @return CompletableFutureT with peek call
     */
    @Override
    public CompletableFutureTSeq<A> peek(final Consumer<? super A> peek) {

        return of(run.peek(future -> future.thenApply(a -> {
            peek.accept(a);
            return a;
        })));
    }

    /**
     * Map the wrapped CompletableFuture
     * 
     * <pre>
     * {@code 
     *  CompletableFutureT.of(AnyM.fromStream(Arrays.asCompletableFuture(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //CompletableFutureT<AnyMSeq<Stream<CompletableFuture[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped CompletableFuture
     * @return CompletableFutureT that applies the map function to the wrapped CompletableFuture
     */
    @Override
    public <B> CompletableFutureTSeq<B> map(final Function<? super A, ? extends B> f) {
        return new CompletableFutureTSeq<B>(
                                            run.map(o -> o.thenApply(f)));
    }

    /**
     * Flat Map the wrapped CompletableFuture
      * <pre>
     * {@code 
     *  CompletableFutureT.of(AnyM.fromStream(Arrays.asCompletableFuture(10))
     *             .flatMap(t->CompletableFuture.completedFuture(20));
     *  
     *  
     *  //CompletableFutureT<AnyMSeq<Stream<CompletableFuture[20]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return CompletableFutureT that applies the flatMap function to the wrapped CompletableFuture
     */

    public <B> CompletableFutureTSeq<B> flatMapT(final Function<? super A, CompletableFutureTSeq<B>> f) {
        return of(run.map(future -> future.thenCompose(a -> f.apply(a).run.stream()
                                                                          .toList()
                                                                          .get(0))));
    }

    private static <B> AnyMSeq<CompletableFuture<B>> narrow(final AnyMSeq<CompletableFuture<? extends B>> run) {
        return (AnyMSeq) run;
    }

    @Override
    public <B> CompletableFutureTSeq<B> flatMap(final Function<? super A, ? extends MonadicValue<? extends B>> f) {

        final AnyMSeq<CompletableFuture<? extends B>> mapped = run.map(o -> FutureW.of(o)
                                                                                   .flatMap(f)
                                                                                   .getFuture());
        return of(narrow(mapped));

    }

    /**
     * Lift a function into one that accepts and returns an CompletableFutureT
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling  / iteration (via CompletableFuture) and iteration (via Stream) to an existing function
     * <pre>
     * {@code 
        Function<Integer,Integer> add2 = i -> i+2;
    	Function<CompletableFutureT<Integer>, CompletableFutureT<Integer>> optTAdd2 = CompletableFutureT.lift(add2);
    	
    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> stream = AnyM.fromStream(withNulls);
    	AnyMSeq<CompletableFuture<Integer>> streamOpt = stream.map(CompletableFuture::completedFuture);
    	List<Integer> results = optTAdd2.apply(CompletableFutureT.of(streamOpt))
    									.unwrap()
    									.<Stream<CompletableFuture<Integer>>>unwrap()
    									.map(CompletableFuture::join)
    									.collect(Collectors.toList());
    	
    	
    	//CompletableFuture.completedFuture(List[3,4]);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from CompletableFuture and another monad type
     * @return Function that accepts and returns an CompletableFutureT
     */
    public static <U, R> Function<CompletableFutureTSeq<U>, CompletableFutureTSeq<R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  CompletableFutureTs
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling / iteration (via CompletableFuture), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
     * to an existing function
     * 
     * <pre>
     * {@code 
    	BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<CompletableFutureT<Integer>,CompletableFutureT<Integer>,CompletableFutureT<Integer>> optTAdd2 = CompletableFutureT.lift2(add);
    	
    	Stream<Integer> withNulls = Stream.of(1,2,3);
    	AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
    	AnyMSeq<CompletableFuture<Integer>> streamOpt = stream.map(CompletableFuture::completedFuture);
    	
    	CompletableFuture<CompletableFuture<Integer>> two = CompletableFuture.completedFuture(CompletableFuture.completedFuture(2));
    	AnyMSeq<CompletableFuture<Integer>> future=  AnyM.fromCompletableFuture(two);
    	List<Integer> results = optTAdd2.apply(CompletableFutureT.of(streamOpt),CompletableFutureT.of(future))
    									.unwrap()
    									.<Stream<CompletableFuture<Integer>>>unwrap()
    									.map(CompletableFuture::join)
    									.collect(Collectors.toList());
    									
    		//CompletableFuture.completedFuture(List[3,4,5]);						
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from CompletableFuture and another monad type
     * @return Function that accepts and returns an CompletableFutureT
     */
    public static <U1, U2, R> BiFunction<CompletableFutureTSeq<U1>, CompletableFutureTSeq<U2>, CompletableFutureTSeq<R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an CompletableFutureT from an AnyM that contains a monad type that contains type other than CompletableFuture
     * The values in the underlying monad will be mapped to CompletableFuture<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an CompletableFuture
     * @return CompletableFutureT
     */
    public static <A> CompletableFutureTSeq<A> fromAnyM(final AnyMSeq<A> anyM) {
        return of(anyM.map(CompletableFuture::completedFuture));
    }

    /**
     * Construct an CompletableFutureT from an AnyM that wraps a monad containing  CompletableFutures
     * 
     * @param monads AnyM that contains a monad wrapping an CompletableFuture
     * @return CompletableFutureT
     */
    public static <A> CompletableFutureTSeq<A> of(final AnyMSeq<CompletableFuture<A>> monads) {
        return new CompletableFutureTSeq<>(
                                           monads);
    }

    public static <A> CompletableFutureTSeq<A> of(final CompletableFuture<A> monads) {
        return CompletableFutureT.fromIterable(ListX.of(monads));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("CompletableFutureTSeq[%s]", run);
    }

    @Override
    public ReactiveSeq<A> stream() {
        return run.stream()
                  .map(cf -> cf.join());
    }

    @Override
    public Iterator<A> iterator() {
        return stream().iterator();
    }

    @Override
    public <T> CompletableFutureTSeq<T> unitStream(final ReactiveSeq<T> traversable) {
        return CompletableFutureT.fromStream(traversable.map(CompletableFuture::completedFuture));

    }

    @Override
    public <T> CompletableFutureTSeq<T> unitAnyM(final AnyM<Traversable<T>> traversable) {

        return of((AnyMSeq) traversable.map(t -> FutureW.fromIterable(t)
                                                        .toCompletableFuture()));
    }

    @Override
    public AnyMSeq<? extends Traversable<A>> transformerStream() {

        return run.map(cf -> FutureW.of(cf)
                                    .toListX());
    }

    public <R> CompletableFutureTSeq<R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> CompletableFuture.completedFuture(i)));
    }

    @Override
    public <R> CompletableFutureTSeq<R> unit(final R value) {
        return of(run.unit(CompletableFuture.completedFuture(value)));
    }

    @Override
    public <R> CompletableFutureTSeq<R> empty() {
        return of(run.unit(new CompletableFuture<R>()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<A> collectable() {
        return stream();
    }

    @Override
    public boolean isSeqPresent() {
        return !run.isEmpty();
    }

    public static <T> CompletableFutureTSeq<T> emptyList() {
        return CompletableFutureT.fromIterable(ListX.of());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public CompletableFutureTSeq<A> combine(final BiPredicate<? super A, ? super A> predicate, final BinaryOperator<A> op) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(int)
     */
    @Override
    public CompletableFutureTSeq<A> cycle(final int times) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public CompletableFutureTSeq<A> cycle(final Monoid<A> m, final int times) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<A> cycleWhile(final Predicate<? super A> predicate) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<A> cycleUntil(final Predicate<? super A> predicate) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> CompletableFutureTSeq<R> zip(final Iterable<? extends U> other, final BiFunction<? super A, ? super U, ? extends R> zipper) {

        return (CompletableFutureTSeq<R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    @Override
    public <U, R> CompletableFutureTSeq<R> zip(final Seq<? extends U> other, final BiFunction<? super A, ? super U, ? extends R> zipper) {

        return (CompletableFutureTSeq<R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    @Override
    public <U, R> CompletableFutureTSeq<R> zip(final Stream<? extends U> other, final BiFunction<? super A, ? super U, ? extends R> zipper) {

        return (CompletableFutureTSeq<R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> CompletableFutureTSeq<Tuple2<A, U>> zip(final Stream<? extends U> other) {

        return (CompletableFutureTSeq) ValueTransformerSeq.super.zip(other);
    }

    @Override
    public <U> CompletableFutureTSeq<Tuple2<A, U>> zip(final Iterable<? extends U> other) {

        return (CompletableFutureTSeq) ValueTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> CompletableFutureTSeq<Tuple2<A, U>> zip(final Seq<? extends U> other) {

        return (CompletableFutureTSeq) ValueTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> CompletableFutureTSeq<Tuple3<A, S, U>> zip3(final Stream<? extends S> second, final Stream<? extends U> third) {

        return (CompletableFutureTSeq) ValueTransformerSeq.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> CompletableFutureTSeq<Tuple4<A, T2, T3, T4>> zip4(final Stream<? extends T2> second, final Stream<? extends T3> third,
            final Stream<? extends T4> fourth) {

        return (CompletableFutureTSeq) ValueTransformerSeq.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipWithIndex()
     */
    @Override
    public CompletableFutureTSeq<Tuple2<A, Long>> zipWithIndex() {

        return (CompletableFutureTSeq<Tuple2<A, Long>>) ValueTransformerSeq.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int)
     */
    @Override
    public CompletableFutureTSeq<ListX<A>> sliding(final int windowSize) {

        return (CompletableFutureTSeq<ListX<A>>) ValueTransformerSeq.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int, int)
     */
    @Override
    public CompletableFutureTSeq<ListX<A>> sliding(final int windowSize, final int increment) {

        return (CompletableFutureTSeq<ListX<A>>) ValueTransformerSeq.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super A>> CompletableFutureTSeq<C> grouped(final int size, final Supplier<C> supplier) {

        return (CompletableFutureTSeq<C>) ValueTransformerSeq.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<ListX<A>> groupedUntil(final Predicate<? super A> predicate) {

        return (CompletableFutureTSeq<ListX<A>>) ValueTransformerSeq.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    public CompletableFutureTSeq<ListX<A>> groupedStatefullyUntil(final BiPredicate<ListX<? super A>, ? super A> predicate) {

        return (CompletableFutureTSeq<ListX<A>>) ValueTransformerSeq.super.groupedStatefullyUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<ListX<A>> groupedWhile(final Predicate<? super A> predicate) {

        return (CompletableFutureTSeq<ListX<A>>) ValueTransformerSeq.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super A>> CompletableFutureTSeq<C> groupedWhile(final Predicate<? super A> predicate, final Supplier<C> factory) {

        return (CompletableFutureTSeq<C>) ValueTransformerSeq.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super A>> CompletableFutureTSeq<C> groupedUntil(final Predicate<? super A> predicate, final Supplier<C> factory) {

        return (CompletableFutureTSeq<C>) ValueTransformerSeq.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int)
     */
    @Override
    public CompletableFutureTSeq<ListX<A>> grouped(final int groupSize) {

        return (CompletableFutureTSeq<ListX<A>>) ValueTransformerSeq.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, T, D> CompletableFutureTSeq<Tuple2<K, D>> grouped(final Function<? super A, ? extends K> classifier,
            final Collector<? super A, T, D> downstream) {

        return (CompletableFutureTSeq) ValueTransformerSeq.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function)
     */
    @Override
    public <K> CompletableFutureTSeq<Tuple2<K, Seq<A>>> grouped(final Function<? super A, ? extends K> classifier) {

        return (CompletableFutureTSeq) ValueTransformerSeq.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#distinct()
     */
    @Override
    public CompletableFutureTSeq<A> distinct() {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public CompletableFutureTSeq<A> scanLeft(final Monoid<A> monoid) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> CompletableFutureTSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super A, ? extends U> function) {

        return (CompletableFutureTSeq<U>) ValueTransformerSeq.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public CompletableFutureTSeq<A> scanRight(final Monoid<A> monoid) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> CompletableFutureTSeq<U> scanRight(final U identity, final BiFunction<? super A, ? super U, ? extends U> combiner) {

        return (CompletableFutureTSeq<U>) ValueTransformerSeq.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted()
     */
    @Override
    public CompletableFutureTSeq<A> sorted() {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.Comparator)
     */
    @Override
    public CompletableFutureTSeq<A> sorted(final Comparator<? super A> c) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<A> takeWhile(final Predicate<? super A> p) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<A> dropWhile(final Predicate<? super A> p) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<A> takeUntil(final Predicate<? super A> p) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<A> dropUntil(final Predicate<? super A> p) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropRight(int)
     */
    @Override
    public CompletableFutureTSeq<A> dropRight(final int num) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeRight(int)
     */
    @Override
    public CompletableFutureTSeq<A> takeRight(final int num) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skip(long)
     */
    @Override
    public CompletableFutureTSeq<A> skip(final long num) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<A> skipWhile(final Predicate<? super A> p) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<A> skipUntil(final Predicate<? super A> p) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limit(long)
     */
    @Override
    public CompletableFutureTSeq<A> limit(final long num) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<A> limitWhile(final Predicate<? super A> p) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    public CompletableFutureTSeq<A> limitUntil(final Predicate<? super A> p) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#intersperse(java.lang.Object)
     */
    @Override
    public CompletableFutureTSeq<A> intersperse(final A value) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#reverse()
     */
    @Override
    public CompletableFutureTSeq<A> reverse() {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle()
     */
    @Override
    public CompletableFutureTSeq<A> shuffle() {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipLast(int)
     */
    @Override
    public CompletableFutureTSeq<A> skipLast(final int num) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitLast(int)
     */
    @Override
    public CompletableFutureTSeq<A> limitLast(final int num) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    public CompletableFutureTSeq<A> onEmpty(final A value) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public CompletableFutureTSeq<A> onEmptyGet(final Supplier<? extends A> supplier) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> CompletableFutureTSeq<A> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle(java.util.Random)
     */
    @Override
    public CompletableFutureTSeq<A> shuffle(final Random random) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#slice(long, long)
     */
    @Override
    public CompletableFutureTSeq<A> slice(final long from, final long to) {

        return (CompletableFutureTSeq<A>) ValueTransformerSeq.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> CompletableFutureTSeq<A> sorted(final Function<? super A, ? extends U> function) {
        return (CompletableFutureTSeq) ValueTransformerSeq.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof CompletableFutureTSeq) {
            return run.equals(((CompletableFutureTSeq) o).run);
        }
        return false;
    }
}