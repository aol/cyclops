package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad Transformer for Java Sets
 * 
 * SetT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Set
 * <pre>
 * {@code 
 * SetT<AnyM<*SOME_MONAD_TYPE*<Set<T>>>>
 * }</pre>
 * 
 * SetT allows the deeply wrapped Set to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public class SetTSeq<T> implements SetT<T> {

    final AnyMSeq<SetX<T>> run;

    private SetTSeq(final AnyMSeq<? extends Set<T>> run) {
        this.run = run.map(s -> SetX.fromIterable(s));
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyMSeq<SetX<T>> unwrap() {
        return run;
    }

    /**
     * Peek at the current value of the Set
     * <pre>
     * {@code 
     *    SetT.of(AnyM.fromStream(Arrays.asSet(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of Set
     * @return SetT with peek call
     */
    public SetTSeq<T> peek(Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });

    }

    /**
     * Filter the wrapped Set
     * <pre>
     * {@code 
     *    SetT.of(AnyM.fromStream(Arrays.asSet(10,11))
     *             .filter(t->t!=10);
     *             
     *     //SetT<AnyM<Stream<Set[11]>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped Set
     * @return SetT that applies the provided filter
     */
    public SetTSeq<T> filter(Predicate<? super T> test) {
        return of(run.map(stream -> ReactiveSeq.fromIterable(stream)
                                               .filter(test)
                                               .toSet()));
    }

    /**
     * Map the wrapped Set
     * 
     * <pre>
     * {@code 
     *  SetT.of(AnyM.fromStream(Arrays.asSet(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //SetT<AnyM<Stream<Set[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Set
     * @return SetT that applies the map function to the wrapped Set
     */
    public <B> SetTSeq<B> map(Function<? super T, ? extends B> f) {
        return of(run.map(o -> (Set<B>) ReactiveSeq.fromIterable(o)
                                                   .map(f)
                                                   .toSet()));
    }

    /**
     * Flat Map the wrapped Set
      * <pre>
     * {@code 
     *  SetT.of(AnyM.fromStream(Arrays.asSet(10))
     *             .flatMap(t->Set.empty();
     *  
     *  
     *  //SetT<AnyM<Stream<Set.empty>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return SetT that applies the flatMap function to the wrapped Set
     */
    public <B> SetTSeq<B> flatMapT(Function<? super T, SetTSeq<B>> f) {

        return of(run.map(stream -> ReactiveSeq.fromIterable(stream)
                                               .flatMap(a -> f.apply(a).run.stream())
                                               .flatMap(a -> a.stream())
                                               .toSet()));
    }

    public <B> SetTSeq<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f) {
        return new SetTSeq<B>(
                              run.map(o -> SetX.fromIterable(o)
                                               .flatMap(f)));

    }

    /**
     * Lift a function into one that accepts and returns an SetT
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling (via Set) and iteration (via Stream) to an existing function
     * <pre>
     * {@code 
     * Function<Integer,Integer> add2 = i -> i+2;
    	Function<SetT<Integer>, SetT<Integer>> optTAdd2 = SetT.lift(add2);
    	
    	Stream<Integer> nums = Stream.of(1,2);
    	AnyM<Stream<Integer>> stream = AnyM.ofMonad(asSet(nums));
    	
    	Set<Integer> results = optTAdd2.apply(SetT.fromStream(stream))
    									.unwrap()
    									.<Optional<Set<Integer>>>unwrap().get();
    	
    	
    	//asSet(3,4);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from Set and another monad type
     * @return Function that accepts and returns an SetT
     */
    public static <U, R> Function<SetTSeq<U>, SetTSeq<R>> lift(Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  SetTs
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling (via Set), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
     * to an existing function
     * 
     * <pre>
     * {@code 
     *BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<SetT<Integer>,SetT<Integer>, SetT<Integer>> optTAdd2 = SetT.lift2(add);
    	
    	Streamable<Integer> threeValues = Streamable.of(1,2,3);
    	AnyM<Integer> stream = AnyM.fromStreamable(threeValues);
    	AnyM<Set<Integer>> streamOpt = stream.map(this::asSet);
    	
    	CompletableFuture<Set<Integer>> two = CompletableFuture.completedFuture(asSet(2));
    	AnyM<Set<Integer>> future=  AnyM.fromCompletableFuture(two);
    	Set<Integer> results = optTAdd2.apply(SetT.of(streamOpt),SetT.of(future))
    									.unwrap()
    									.<Stream<Set<Integer>>>unwrap()
    									.flatMap(i->i.stream())
    									.collect(Collectors.toSet());
    		//asSet(3,4);							
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Set and another monad type
     * @return Function that accepts and returns an SetT
     */
    public static <U1, U2, R> BiFunction<SetTSeq<U1>, SetTSeq<U2>, SetTSeq<R>> lift2(BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an SetT from an AnyM that contains a monad type that contains type other than Set
     * The values in the underlying monad will be mapped to Set<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Set
     * @return SetT
     */
    public static <A> SetTSeq<A> fromAnyM(AnyMSeq<A> anyM) {
        return of(anyM.map(SetTSeq::asSet));
    }

    private static <T> Set<T> asSet(T... elements) {
        return new HashSet<T>(
                              Arrays.asList(elements));
    }

    /**
     * Construct an SetT from an AnyM that wraps a monad containing  Sets
     * 
     * @param monads AnyM that contains a monad wrapping an Set
     * @return SetT
     */
    public static <A> SetTSeq<A> of(AnyMSeq<? extends Set<A>> monads) {
        return new SetTSeq<>(
                             monads);
    }

    public static <A> SetTSeq<A> of(Set<A> monads) {
        return SetT.fromIterable(SetX.of(monads));
    }

    /**
     * Create a SetT from an AnyM that wraps a monad containing a Stream
     * 
     * @param monads
     * @return
     */
    public static <A> SetTSeq<A> fromStream(AnyMSeq<Stream<A>> monads) {
        return of(monads.map(s -> s.collect(Collectors.toSet())));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return String.format("SetTSeq[%s]", run);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> SetTSeq<T> unit(T unit) {
        return of(run.unit(SetX.of(unit)));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream()
                  .flatMapIterable(e -> e);
    }

    @Override
    public Iterator<T> iterator() {

        return stream().iterator();
    }

    public <R> SetTSeq<R> unitIterator(Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> SetX.of(i)));
    }

    @Override
    public <R> SetTSeq<R> empty() {
        return of(run.empty());
    }

    @Override
    public AnyM<? extends IterableFoldable<T>> nestedFoldables() {
        return run;

    }

    @Override
    public AnyM<? extends CyclopsCollectable<T>> nestedCollectables() {
        return run;

    }

    @Override
    public <T> SetTSeq<T> unitAnyM(AnyM<Traversable<T>> traversable) {

        return of((AnyMSeq) traversable.map(t -> SetX.fromIterable(t)));
    }

    @Override
    public AnyMSeq<? extends Traversable<T>> transformerStream() {

        return run;
    }

    public static <T> SetTSeq<T> emptySet() {
        return SetT.fromIterable(SetX.empty());
    }

    public boolean isSeqPresent() {
        return !run.isEmpty();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public SetTSeq<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {

        return (SetTSeq<T>) SetT.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#cycle(int)
     */
    @Override
    public SetTSeq<T> cycle(int times) {

        return (SetTSeq<T>) SetT.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public SetTSeq<T> cycle(Monoid<T> m, int times) {

        return (SetTSeq<T>) SetT.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<T> cycleWhile(Predicate<? super T> predicate) {

        return (SetTSeq<T>) SetT.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<T> cycleUntil(Predicate<? super T> predicate) {

        return (SetTSeq<T>) SetT.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> SetTSeq<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (SetTSeq<R>) SetT.super.zip(other, zipper);
    }

    @Override
    public <U, R> SetTSeq<R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (SetTSeq<R>) SetT.super.zip(other, zipper);
    }

    @Override
    public <U, R> SetTSeq<R> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (SetTSeq<R>) SetT.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> SetTSeq<Tuple2<T, U>> zip(Stream<? extends U> other) {

        return (SetTSeq) SetT.super.zip(other);
    }

    @Override
    public <U> SetTSeq<Tuple2<T, U>> zip(Iterable<? extends U> other) {

        return (SetTSeq) SetT.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> SetTSeq<Tuple2<T, U>> zip(Seq<? extends U> other) {

        return (SetTSeq) SetT.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> SetTSeq<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {

        return (SetTSeq) SetT.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> SetTSeq<Tuple4<T, T2, T3, T4>> zip4(Stream<? extends T2> second, Stream<? extends T3> third, Stream<? extends T4> fourth) {

        return (SetTSeq) SetT.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#zipWithIndex()
     */
    @Override
    public SetTSeq<Tuple2<T, Long>> zipWithIndex() {

        return (SetTSeq<Tuple2<T, Long>>) SetT.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#sliding(int)
     */
    @Override
    public SetTSeq<ListX<T>> sliding(int windowSize) {

        return (SetTSeq<ListX<T>>) SetT.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#sliding(int, int)
     */
    @Override
    public SetTSeq<ListX<T>> sliding(int windowSize, int increment) {

        return (SetTSeq<ListX<T>>) SetT.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> SetTSeq<C> grouped(int size, Supplier<C> supplier) {

        return (SetTSeq<C>) SetT.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<ListX<T>> groupedUntil(Predicate<? super T> predicate) {

        return (SetTSeq<ListX<T>>) SetT.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    public SetTSeq<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (SetTSeq<ListX<T>>) SetT.super.groupedStatefullyUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<ListX<T>> groupedWhile(Predicate<? super T> predicate) {

        return (SetTSeq<ListX<T>>) SetT.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> SetTSeq<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {

        return (SetTSeq<C>) SetT.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> SetTSeq<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {

        return (SetTSeq<C>) SetT.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#grouped(int)
     */
    @Override
    public SetTSeq<ListX<T>> grouped(int groupSize) {

        return (SetTSeq<ListX<T>>) SetT.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> SetTSeq<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {

        return (SetTSeq) SetT.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#grouped(java.util.function.Function)
     */
    @Override
    public <K> SetTSeq<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {

        return (SetTSeq) SetT.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#distinct()
     */
    @Override
    public SetTSeq<T> distinct() {

        return (SetTSeq<T>) SetT.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public SetTSeq<T> scanLeft(Monoid<T> monoid) {

        return (SetTSeq<T>) SetT.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> SetTSeq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {

        return (SetTSeq<U>) SetT.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public SetTSeq<T> scanRight(Monoid<T> monoid) {

        return (SetTSeq<T>) SetT.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> SetTSeq<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (SetTSeq<U>) SetT.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#sorted()
     */
    @Override
    public SetTSeq<T> sorted() {

        return (SetTSeq<T>) SetT.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#sorted(java.util.Comparator)
     */
    @Override
    public SetTSeq<T> sorted(Comparator<? super T> c) {

        return (SetTSeq<T>) SetT.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#takeWhile(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<T> takeWhile(Predicate<? super T> p) {

        return (SetTSeq<T>) SetT.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#dropWhile(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<T> dropWhile(Predicate<? super T> p) {

        return (SetTSeq<T>) SetT.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#takeUntil(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<T> takeUntil(Predicate<? super T> p) {

        return (SetTSeq<T>) SetT.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#dropUntil(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<T> dropUntil(Predicate<? super T> p) {

        return (SetTSeq<T>) SetT.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#dropRight(int)
     */
    @Override
    public SetTSeq<T> dropRight(int num) {

        return (SetTSeq<T>) SetT.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#takeRight(int)
     */
    @Override
    public SetTSeq<T> takeRight(int num) {

        return (SetTSeq<T>) SetT.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#skip(long)
     */
    @Override
    public SetTSeq<T> skip(long num) {

        return (SetTSeq<T>) SetT.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#skipWhile(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<T> skipWhile(Predicate<? super T> p) {

        return (SetTSeq<T>) SetT.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#skipUntil(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<T> skipUntil(Predicate<? super T> p) {

        return (SetTSeq<T>) SetT.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#limit(long)
     */
    @Override
    public SetTSeq<T> limit(long num) {

        return (SetTSeq<T>) SetT.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#limitWhile(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<T> limitWhile(Predicate<? super T> p) {

        return (SetTSeq<T>) SetT.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#limitUntil(java.util.function.Predicate)
     */
    @Override
    public SetTSeq<T> limitUntil(Predicate<? super T> p) {

        return (SetTSeq<T>) SetT.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#intersperse(java.lang.Object)
     */
    @Override
    public SetTSeq<T> intersperse(T value) {

        return (SetTSeq<T>) SetT.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#reverse()
     */
    @Override
    public SetTSeq<T> reverse() {

        return (SetTSeq<T>) SetT.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#shuffle()
     */
    @Override
    public SetTSeq<T> shuffle() {

        return (SetTSeq<T>) SetT.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#skipLast(int)
     */
    @Override
    public SetTSeq<T> skipLast(int num) {

        return (SetTSeq<T>) SetT.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#limitLast(int)
     */
    @Override
    public SetTSeq<T> limitLast(int num) {

        return (SetTSeq<T>) SetT.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#onEmpty(java.lang.Object)
     */
    @Override
    public SetTSeq<T> onEmpty(T value) {

        return (SetTSeq<T>) SetT.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public SetTSeq<T> onEmptyGet(Supplier<? extends T> supplier) {

        return (SetTSeq<T>) SetT.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> SetTSeq<T> onEmptyThrow(Supplier<? extends X> supplier) {

        return (SetTSeq<T>) SetT.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#shuffle(java.util.Random)
     */
    @Override
    public SetTSeq<T> shuffle(Random random) {

        return (SetTSeq<T>) SetT.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#slice(long, long)
     */
    @Override
    public SetTSeq<T> slice(long from, long to) {

        return (SetTSeq<T>) SetT.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.SetT#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> SetTSeq<T> sorted(Function<? super T, ? extends U> function) {
        return (SetTSeq) SetT.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SetTSeq) {
            return run.equals(((SetTSeq) o).run);
        }
        return false;
    }
}