package com.aol.cyclops.control.monads.transformers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.control.monads.transformers.values.FoldableTransformerSeq;
import com.aol.cyclops.control.monads.transformers.values.ListTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;



/**
 * Monad Transformer for Java Lists
 * 
 * ListT consists of an AnyM instance that in turns wraps anoter Monad type that contains an List
 * 
 * ListT<AnyM<*SOME_MONAD_TYPE*<List<T>>>>
 * 
 * ListT allows the deeply wrapped List to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public interface ListT<T>  extends  FoldableTransformerSeq<T> {
    
    public <R> ListT<R> unitIterator(Iterator<R> it);
    public <R> ListT<R> unit(R t);
    public <R> ListT<R> empty();
   
   /**
	 * @return The wrapped AnyM
	 */
   public AnyM<ListX<T>> unwrap();
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
   public ListT<T> peek(Consumer<? super T> peek);
   
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
   public ListT<T> filter(Predicate<? super T> test);
   
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
   public <B> ListT<B> map(Function<? super T,? extends B> f);
   public <B> ListT<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f);
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
   default <B> ListT<B> bind(Function<? super T,ListT<B>> f){
	  
	   return of( unwrap().map(stream-> ReactiveSeq.fromList(stream).flatMap(a-> f.apply(a).unwrap().stream()).flatMap(a->a.stream())
			   .toList()));
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
   public static <U, R> Function<ListT<U>, ListT<R>> lift(Function<? super U, ? extends R> fn) {
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
	public static <U1, U2, R> BiFunction<ListT<U1>, ListT<U2>, ListT<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an ListT from an AnyM that contains a monad type that contains type other than List
	 * The values in the underlying monad will be mapped to List<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an List
	 * @return ListT
	 */
   public static <A> ListT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(Arrays::asList));
   }
   /**
	 * Construct an ListT from an AnyM that wraps a monad containing  Lists
	 * 
	 * @param monads AnyM that contains a monad wrapping an List
	 * @return ListT
	 */
   public static <A> ListT<A> of(AnyM<List<A>> monads){
       return Matchables.anyM(monads).visit(v-> ListTValue.of(v), s->ListTSeq.of(s));
   }

	/**
	 * Create a ListT from an AnyM that wraps a monad containing a Stream
	 * 
	 * @param monads
	 * @return
	 */
	public static <A> ListT<A> fromStreamAnyM(AnyM<Stream<A>> monads) {
		return of(monads.map(s -> s.collect(Collectors.toList())));
	}
   
 
    public static <A> ListTValue<A> fromAnyMValue(AnyMValue<A> anyM) {
        return ListTValue.fromAnyM(anyM);
    }

    public static <A> ListTSeq<A> fromAnyMSeq(AnyMSeq<A> anyM) {
        return ListTSeq.fromAnyM(anyM);
    }

    
    public static <A> ListTSeq<A> fromIterable(
            Iterable<? extends List<A>> iterableOfLists) {
        return ListTSeq.of(AnyM.fromIterable(iterableOfLists));
    }

    public static <A> ListTSeq<A> fromStream(Stream<? extends List<A>> streamOfLists) {
        return ListTSeq.of(AnyM.fromStream(streamOfLists));
    }

    public static <A> ListTSeq<A> fromPublisher(
            Publisher<List<A>> publisherOfLists) {
        return ListTSeq.of(AnyM.fromPublisher(publisherOfLists));
    }

    public static <A, V extends MonadicValue<? extends List<A>>> ListTValue<A> fromValue(
            V monadicValue) {
        return ListTValue.fromValue(monadicValue);
    }

    public static <A> ListTValue<A> fromOptional(Optional<? extends List<A>> optional) {
        return ListTValue.of(AnyM.fromOptional(optional));
    }

    public static <A> ListTValue<A> fromFuture(CompletableFuture<? extends List<A>> future) {
        return ListTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A> ListTValue<A> fromIterablListue(
            Iterable<? extends List<A>> iterableOfLists) {
        return ListTValue.of(AnyM.fromIterableValue(iterableOfLists));
    }
    public static <T> ListTValue<T> emptyOptional(){
        return ListTValue.emptyOptional();
    }
    public static <T> ListTSeq<T> emptyList(){
        return ListTSeq.emptyList();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> ListT<U> cast(Class<? extends U> type) {
        return (ListT<U>)FoldableTransformerSeq.super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> ListT<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (ListT<R>)FoldableTransformerSeq.super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> ListT<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       return (ListT<R>)FoldableTransformerSeq.super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> ListT<U> ofType(Class<? extends U> type) {
        
        return (ListT<U>)FoldableTransformerSeq.super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default ListT<T> filterNot(Predicate<? super T> fn) {
       
        return (ListT<T>)FoldableTransformerSeq.super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    default ListT<T> notNull() {
       
        return (ListT<T>)FoldableTransformerSeq.super.notNull();
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#isSeqPresent()
     */
    @Override
    default boolean isSeqPresent() {
       
        return false;
    }

  
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default ListT<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (ListT<T>)FoldableTransformerSeq.super.combine(predicate, op);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycle(int)
     */
    @Override
    default ListT<T> cycle(int times) {
       
        return (ListT<T>)FoldableTransformerSeq.super.cycle(times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    default ListT<T> cycle(Monoid<T> m, int times) {
       
        return (ListT<T>)FoldableTransformerSeq.super.cycle(m, times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default ListT<T> cycleWhile(Predicate<? super T> predicate) {
       
        return (ListT<T>)FoldableTransformerSeq.super.cycleWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default ListT<T> cycleUntil(Predicate<? super T> predicate) {
       
        return (ListT<T>)FoldableTransformerSeq.super.cycleUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> ListT<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (ListT<R>)FoldableTransformerSeq.super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zipStream(java.util.stream.Stream)
     */
    @Override
    default <U> ListT<Tuple2<T, U>> zipStream(Stream<? extends U> other) {
       
        return (ListT)FoldableTransformerSeq.super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> ListT<Tuple2<T, U>> zip(Seq<? extends U> other) {
       
        return (ListT)FoldableTransformerSeq.super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> ListT<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (ListT)FoldableTransformerSeq.super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> ListT<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (ListT<Tuple4<T, T2, T3, T4>>)FoldableTransformerSeq.super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zipWithIndex()
     */
    @Override
    default ListT<Tuple2<T, Long>> zipWithIndex() {
       
        return (ListT<Tuple2<T, Long>>)FoldableTransformerSeq.super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sliding(int)
     */
    @Override
    default ListT<ListX<T>> sliding(int windowSize) {
       
        return (ListT<ListX<T>>)FoldableTransformerSeq.super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sliding(int, int)
     */
    @Override
    default ListT<ListX<T>> sliding(int windowSize, int increment) {
       
        return (ListT<ListX<T>>)FoldableTransformerSeq.super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> ListT<C> grouped(int size, Supplier<C> supplier) {
       
        return (ListT<C> )FoldableTransformerSeq.super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default ListT<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (ListT<ListX<T>>)FoldableTransformerSeq.super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    default ListT<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
       
        return (ListT<ListX<T>>)FoldableTransformerSeq.super.groupedStatefullyWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default ListT<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (ListT<ListX<T>>)FoldableTransformerSeq.super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> ListT<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
       
        return (ListT<C>)FoldableTransformerSeq.super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> ListT<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
       
        return (ListT<C>)FoldableTransformerSeq.super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(int)
     */
    @Override
    default ListT<ListX<T>> grouped(int groupSize) {
       
        return ( ListT<ListX<T>>)FoldableTransformerSeq.super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    default <K, A, D> ListT<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (ListT)FoldableTransformerSeq.super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(java.util.function.Function)
     */
    @Override
    default <K> ListT<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (ListT)FoldableTransformerSeq.super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#distinct()
     */
    @Override
    default ListT<T> distinct() {
       
        return (ListT<T>)FoldableTransformerSeq.super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    default ListT<T> scanLeft(Monoid<T> monoid) {
       
        return (ListT<T>)FoldableTransformerSeq.super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> ListT<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
       
        return (ListT<U>)FoldableTransformerSeq.super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    default ListT<T> scanRight(Monoid<T> monoid) {
       
        return (ListT<T>)FoldableTransformerSeq.super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> ListT<U> scanRight(U identity, BiFunction<? super T, ? super U,? extends U> combiner) {
       
        return (ListT<U>)FoldableTransformerSeq.super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sorted()
     */
    @Override
    default ListT<T> sorted() {
       
        return (ListT<T>)FoldableTransformerSeq.super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sorted(java.util.Comparator)
     */
    @Override
    default ListT<T> sorted(Comparator<? super T> c) {
       
        return (ListT<T>)FoldableTransformerSeq.super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#takeWhile(java.util.function.Predicate)
     */
    @Override
    default ListT<T> takeWhile(Predicate<? super T> p) {
       
        return (ListT<T>)FoldableTransformerSeq.super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#dropWhile(java.util.function.Predicate)
     */
    @Override
    default ListT<T> dropWhile(Predicate<? super T> p) {
       
        return (ListT<T>)FoldableTransformerSeq.super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#takeUntil(java.util.function.Predicate)
     */
    @Override
    default ListT<T> takeUntil(Predicate<? super T> p) {
       
        return (ListT<T>)FoldableTransformerSeq.super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#dropUntil(java.util.function.Predicate)
     */
    @Override
    default ListT<T> dropUntil(Predicate<? super T> p) {
       
        return (ListT<T>)FoldableTransformerSeq.super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#dropRight(int)
     */
    @Override
    default ListT<T> dropRight(int num) {
       
        return (ListT<T>)FoldableTransformerSeq.super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#takeRight(int)
     */
    @Override
    default ListT<T> takeRight(int num) {
       
        return (ListT<T>)FoldableTransformerSeq.super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skip(long)
     */
    @Override
    default ListT<T> skip(long num) {
       
        return (ListT<T>)FoldableTransformerSeq.super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skipWhile(java.util.function.Predicate)
     */
    @Override
    default ListT<T> skipWhile(Predicate<? super T> p) {
       
        return (ListT<T>)FoldableTransformerSeq.super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skipUntil(java.util.function.Predicate)
     */
    @Override
    default ListT<T> skipUntil(Predicate<? super T> p) {
       
        return (ListT<T>)FoldableTransformerSeq.super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limit(long)
     */
    @Override
    default ListT<T> limit(long num) {
       
        return (ListT<T>)FoldableTransformerSeq.super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limitWhile(java.util.function.Predicate)
     */
    @Override
    default ListT<T> limitWhile(Predicate<? super T> p) {
       
        return (ListT<T>)FoldableTransformerSeq.super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limitUntil(java.util.function.Predicate)
     */
    @Override
    default ListT<T> limitUntil(Predicate<? super T> p) {
       
        return (ListT<T>)FoldableTransformerSeq.super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#intersperse(java.lang.Object)
     */
    @Override
    default ListT<T> intersperse(T value) {
       
        return (ListT<T>)FoldableTransformerSeq.super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#reverse()
     */
    @Override
    default ListT<T> reverse() {
       
        return (ListT<T>)FoldableTransformerSeq.super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#shuffle()
     */
    @Override
    default ListT<T> shuffle() {
       
        return (ListT<T>)FoldableTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skipLast(int)
     */
    @Override
    default ListT<T> skipLast(int num) {
       
        return (ListT<T>)FoldableTransformerSeq.super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limitLast(int)
     */
    @Override
    default ListT<T> limitLast(int num) {
       
        return (ListT<T>)FoldableTransformerSeq.super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#onEmpty(java.lang.Object)
     */
    @Override
    default ListT<T> onEmpty(T value) {
       
        return (ListT<T>)FoldableTransformerSeq.super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default ListT<T> onEmptyGet(Supplier<? extends T> supplier) {
       
        return (ListT<T>)FoldableTransformerSeq.super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> ListT<T> onEmptyThrow(Supplier<? extends X> supplier) {
       
        return (ListT<T>)FoldableTransformerSeq.super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#shuffle(java.util.Random)
     */
    @Override
    default ListT<T> shuffle(Random random) {
       
        return (ListT<T>)FoldableTransformerSeq.super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#slice(long, long)
     */
    @Override
    default ListT<T> slice(long from, long to) {
       
        return (ListT<T>)FoldableTransformerSeq.super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> ListT<T> sorted(Function<? super T, ? extends U> function) {
        return (ListT)FoldableTransformerSeq.super.sorted(function);
    }
   
    
    
 
}