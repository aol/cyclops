package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.values.FoldableTransformerSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.To;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad Transformer for Java Lists nested within Sequential or non-scalar data types (e.g. Lists, Streams etc)
 * 
 * ListT allows the deeply wrapped List to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Lists
 */
public class ListTSeq<W extends WitnessType,T> implements To<ListT<T>>,
                                                          FoldableTransformerSeq<W,T> {

    final AnyM<W,ListX<T>> run;

    private ListTSeq(final AnyM<W,? extends List<T>> run) {
        this.run = run.map(l -> ListX.fromIterable(l));
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyM<W,ListX<T>> unwrap() {
        return run;
    }

    /**
     * Peek at the current value of the List
     * <pre>
     * {@code 
     *    ListT.of(AnyM.fromStream(Arrays.asList(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of List
     * @return ListT with peek call
     */
    @Override
    public ListTSeq<W,T> peek(final Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });

    }

    /**
     * Filter the wrapped List
     * <pre>
     * {@code 
     *    ListT.of(AnyM.fromStream(Arrays.asList(10,11))
     *             .filter(t->t!=10);
     *             
     *     //ListT<AnyM<Stream<List[11]>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped List
     * @return ListT that applies the provided filter
     */
    @Override
    public ListTSeq<W,T> filter(final Predicate<? super T> test) {
        return of(run.map(stream -> ReactiveSeq.fromList(stream)
                                               .filter(test)
                                               .toList()));
    }

    /**
     * Map the wrapped List
     * 
     * <pre>
     * {@code 
     *  ListT.of(AnyM.fromStream(Arrays.asList(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //ListT<AnyM<Stream<List[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped List
     * @return ListT that applies the map function to the wrapped List
     */
    @Override
    public <B> ListTSeq<W,B> map(final Function<? super T, ? extends B> f) {
        return of(run.map(o -> (List<B>) ReactiveSeq.fromList(o)
                                                    .map(f)
                                                    .toList()));
    }

    @Override
    public <B> ListTSeq<W,B> flatMap(final Function<? super T, ? extends Iterable<? extends B>> f) {
        return new ListTSeq<W,B>(
                               run.map(o -> ListX.fromIterable(o)
                                                 .flatMap(f)));

    }

    /**
     * Flat Map the wrapped List
      * <pre>
     * {@code 
     *  ListT.of(AnyM.fromStream(Arrays.asList(10))
     *             .flatMap(t->List.empty();
     *  
     *  
     *  //ListT<AnyM<Stream<List.empty>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return ListT that applies the flatMap function to the wrapped List
     */
    public <B> ListTSeq<W,B> flatMapT(final Function<? super T, ListTSeq<W,B>> f) {

        return of(run.map(list -> list.flatMap(a -> f.apply(a).run.stream())
                                      .flatMap(a -> a.stream())));
    }

    /**
     * Lift a function into one that accepts and returns an ListT
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling (via List) and iteration (via Stream) to an existing function
     * <pre>
     * {@code 
     * Function<Integer,Integer> add2 = i -> i+2;
    	Function<ListT<Integer>, ListT<Integer>> optTAdd2 = ListT.lift(add2);
    	
    	Stream<Integer> nums = Stream.of(1,2);
    	AnyM<Stream<Integer>> stream = AnyM.ofMonad(Arrays.asList(nums));
    	
    	List<Integer> results = optTAdd2.apply(ListT.fromStream(stream))
    									.unwrap()
    									.<Optional<List<Integer>>>unwrap().get();
    	
    	
    	//Arrays.asList(3,4);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from List and another monad type
     * @return Function that accepts and returns an ListT
     */
    public static <W extends WitnessType,U, R> Function<ListTSeq<W,U>, ListTSeq<W,R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  ListTs
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling (via List), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
     * to an existing function
     * 
     * <pre>
     * {@code 
     *BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<ListT<Integer>,ListT<Integer>, ListT<Integer>> optTAdd2 = ListT.lift2(add);
    	
    	Streamable<Integer> threeValues = Streamable.of(1,2,3);
    	AnyM<Integer> stream = AnyM.fromStreamable(threeValues);
    	AnyM<List<Integer>> streamOpt = stream.map(Arrays::asList);
    	
    	CompletableFuture<List<Integer>> two = CompletableFuture.completedFuture(Arrays.asList(2));
    	AnyM<List<Integer>> future=  AnyM.fromCompletableFuture(two);
    	List<Integer> results = optTAdd2.apply(ListT.of(streamOpt),ListT.of(future))
    									.unwrap()
    									.<Stream<List<Integer>>>unwrap()
    									.flatMap(i->i.stream())
    									.collect(Collectors.toList());
    		//Arrays.asList(3,4);							
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from List and another monad type
     * @return Function that accepts and returns an ListT
     */
    public static <W extends WitnessType,U1, U2, R> BiFunction<ListTSeq<W,U1>, ListTSeq<W,U2>, ListTSeq<W,R>> lift2(final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an ListT from an AnyM that contains a monad type that contains type other than List
     * The values in the underlying monad will be mapped to List<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an List
     * @return ListT
     */
    public static <W extends WitnessType,A> ListTSeq<W,A> fromAnyM(final AnyM<W,A> anyM) {
        return of(anyM.map(Arrays::asList));
    }

    /**
     * Construct an ListT from an AnyM that wraps a monad containing  Lists
     * 
     * @param monads AnyM that contains a monad wrapping an List
     * @return ListT
     */
    public static <W extends WitnessType,A> ListTSeq<W,A> of(final AnyM<W,? extends List<A>> monads) {
        return new ListTSeq<>(
                              monads);
    }

    

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("ListT[%s]", run);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    public <T> ListTSeq<W,T> unit(final T unit) {
        return of(run.unit(ListX.of(unit)));
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

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     
    @Override
    public Collectable<T> collectable() {
       return this;
    } */
    @Override
    public <R> ListTSeq<W,R> unitIterator(final Iterator<R> it) {  
        return of(run.unitIterator(it)
                     .map(i -> ListX.of(i)));
    }

    @Override
    public <R> ListTSeq<W,R> empty() {
        return of(run.empty());
    }

    @Override
    public AnyM<W,? extends IterableFoldable<T>> nestedFoldables() {
        return run;

    }

    @Override
    public AnyM<W,? extends CyclopsCollectable<T>> nestedCollectables() {
        return run;

    }

    @Override
    public <T> ListTSeq<W,T> unitAnyM(final AnyM<W,Traversable<T>> traversable) {

        return of((AnyMSeq) traversable.map(t -> ListX.fromIterable(t)));
    }

    @Override
    public AnyM<W,? extends Traversable<T>> transformerStream() {

        return run;
    }

    public static <W extends WitnessType,T> ListTSeq<W,T> emptyList(W witness) { 
        return of(witness.<W>adapter().unit(ListX.empty()));
    }

    @Override
    public boolean isSeqPresent() {
        return !run.isEmpty();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public ListTSeq<W,T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycle(int)
     */
    @Override
    public ListTSeq<W,T> cycle(final int times) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public ListTSeq<W,T> cycle(final Monoid<T> m, final int times) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,T> cycleWhile(final Predicate<? super T> predicate) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,T> cycleUntil(final Predicate<? super T> predicate) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> ListTSeq<W,R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListTSeq<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.ListT#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    public <U, R> ListTSeq<W,R> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListTSeq<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.ListT#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    public <U, R> ListTSeq<W,R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (ListTSeq<W,R>) FoldableTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> ListTSeq<W,Tuple2<T, U>> zip(final Stream<? extends U> other) {

        return (ListTSeq) FoldableTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.ListT#zip(java.lang.Iterable)
     */
    @Override
    public <U> ListTSeq<W,Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (ListTSeq) FoldableTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> ListTSeq<W,Tuple2<T, U>> zip(final Seq<? extends U> other) {

        return (ListTSeq) FoldableTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> ListTSeq<W,Tuple3<T, S, U>> zip3(final Stream<? extends S> second, final Stream<? extends U> third) {

        return (ListTSeq) FoldableTransformerSeq.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> ListTSeq<W,Tuple4<T, T2, T3, T4>> zip4(final Stream<? extends T2> second, final Stream<? extends T3> third,
            final Stream<? extends T4> fourth) {

        return (ListTSeq) FoldableTransformerSeq.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zipWithIndex()
     */
    @Override
    public ListTSeq<W,Tuple2<T, Long>> zipWithIndex() {

        return (ListTSeq<W,Tuple2<T, Long>>) FoldableTransformerSeq.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sliding(int)
     */
    @Override
    public ListTSeq<W,ListX<T>> sliding(final int windowSize) {

        return (ListTSeq<W,ListX<T>>) FoldableTransformerSeq.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sliding(int, int)
     */
    @Override
    public ListTSeq<W,ListX<T>> sliding(final int windowSize, final int increment) {

        return (ListTSeq<W,ListX<T>>) FoldableTransformerSeq.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListTSeq<W,C> grouped(final int size, final Supplier<C> supplier) {

        return (ListTSeq<W,C>) FoldableTransformerSeq.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (ListTSeq<W,ListX<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    public ListTSeq<W,ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (ListTSeq<W,ListX<T>>) FoldableTransformerSeq.super.groupedStatefullyUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (ListTSeq<W,ListX<T>>) FoldableTransformerSeq.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListTSeq<W,C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListTSeq<W,C>) FoldableTransformerSeq.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListTSeq<W,C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (ListTSeq<W,C>) FoldableTransformerSeq.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(int)
     */
    @Override
    public ListTSeq<W,ListX<T>> grouped(final int groupSize) {

        return (ListTSeq<W,ListX<T>>) FoldableTransformerSeq.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> ListTSeq<W,Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {

        return (ListTSeq) FoldableTransformerSeq.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(java.util.function.Function)
     */
    @Override
    public <K> ListTSeq<W,Tuple2<K, Seq<T>>> grouped(final Function<? super T, ? extends K> classifier) {

        return (ListTSeq) FoldableTransformerSeq.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#distinct()
     */
    @Override
    public ListTSeq<W,T> distinct() {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public ListTSeq<W,T> scanLeft(final Monoid<T> monoid) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> ListTSeq<W,U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return (ListTSeq<W,U>) FoldableTransformerSeq.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public ListTSeq<W,T> scanRight(final Monoid<T> monoid) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> ListTSeq<W,U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (ListTSeq<W,U>) FoldableTransformerSeq.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sorted()
     */
    @Override
    public ListTSeq<W,T> sorted() {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sorted(java.util.Comparator)
     */
    @Override
    public ListTSeq<W,T> sorted(final Comparator<? super T> c) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#takeWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,T> takeWhile(final Predicate<? super T> p) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#dropWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,T> dropWhile(final Predicate<? super T> p) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#takeUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,T> takeUntil(final Predicate<? super T> p) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#dropUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,T> dropUntil(final Predicate<? super T> p) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#dropRight(int)
     */
    @Override
    public ListTSeq<W,T> dropRight(final int num) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#takeRight(int)
     */
    @Override
    public ListTSeq<W,T> takeRight(final int num) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skip(long)
     */
    @Override
    public ListTSeq<W,T> skip(final long num) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skipWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,T> skipWhile(final Predicate<? super T> p) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skipUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,T> skipUntil(final Predicate<? super T> p) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limit(long)
     */
    @Override
    public ListTSeq<W,T> limit(final long num) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limitWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,T> limitWhile(final Predicate<? super T> p) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limitUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<W,T> limitUntil(final Predicate<? super T> p) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#intersperse(java.lang.Object)
     */
    @Override
    public ListTSeq<W,T> intersperse(final T value) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#reverse()
     */
    @Override
    public ListTSeq<W,T> reverse() {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#shuffle()
     */
    @Override
    public ListTSeq<W,T> shuffle() {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skipLast(int)
     */
    @Override
    public ListTSeq<W,T> skipLast(final int num) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limitLast(int)
     */
    @Override
    public ListTSeq<W,T> limitLast(final int num) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#onEmpty(java.lang.Object)
     */
    @Override
    public ListTSeq<W,T> onEmpty(final T value) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public ListTSeq<W,T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> ListTSeq<W,T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#shuffle(java.util.Random)
     */
    @Override
    public ListTSeq<W,T> shuffle(final Random random) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#slice(long, long)
     */
    @Override
    public ListTSeq<W,T> slice(final long from, final long to) {

        return (ListTSeq<W,T>) FoldableTransformerSeq.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> ListTSeq<W,T> sorted(final Function<? super T, ? extends U> function) {
        return (ListTSeq) FoldableTransformerSeq.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ListTSeq) {
            return run.equals(((ListTSeq) o).run);
        }
        return false;
    }

}