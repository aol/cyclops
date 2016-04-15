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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
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
public class ListTSeq<T> implements ListT<T>{
                                   
   final AnyMSeq<ListX<T>> run;

   private ListTSeq(final AnyMSeq<? extends List<T>> run){
       this.run = run.map(l->ListX.fromIterable(l));
   }
   /**
	 * @return The wrapped AnyM
	 */
   @Override
   public AnyMSeq<ListX<T>> unwrap(){
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
   public ListTSeq<T> peek(Consumer<? super T> peek){
	   return map(a-> {peek.accept(a); return a;});
     
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
   public ListTSeq<T> filter(Predicate<? super T> test){
       return of(run.map(stream-> ReactiveSeq.fromList(stream).filter(test).toList()));
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
   public <B> ListTSeq<B> map(Function<? super T,? extends B> f){
       return of(run.map(o-> (List<B>)ReactiveSeq.fromList(o).map(f).toList()));
   }
   @Override
   public <B> ListTSeq<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f) {
       return new ListTSeq<B>(run.map(o -> ListX.fromIterable(o).flatMap(f)));

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
   public <B> ListTSeq<B> flatMapT(Function<? super T,ListTSeq<B>> f){
	  
	   return of( run.map(stream-> ReactiveSeq.fromList(stream).flatMap(a-> f.apply(a).run.stream()).flatMap(a->a.stream())
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
   public static <U, R> Function<ListTSeq<U>, ListTSeq<R>> lift(Function<? super U, ? extends R> fn) {
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
	public static <U1, U2, R> BiFunction<ListTSeq<U1>, ListTSeq<U2>, ListTSeq<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an ListT from an AnyM that contains a monad type that contains type other than List
	 * The values in the underlying monad will be mapped to List<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an List
	 * @return ListT
	 */
   public static <A> ListTSeq<A> fromAnyM(AnyMSeq<A> anyM){
	   return of(anyM.map(Arrays::asList));
   }
   
   /**
	 * Construct an ListT from an AnyM that wraps a monad containing  Lists
	 * 
	 * @param monads AnyM that contains a monad wrapping an List
	 * @return ListT
	 */
   public static <A> ListTSeq<A> of(AnyMSeq<? extends List<A>> monads){
	   return new ListTSeq<>(monads);
   }
   public static <A> ListTSeq<A> of(List<A> monads){
       return ListT.fromIterable(ListX.of(monads));
   }
	/**
	 * Create a ListT from an AnyM that wraps a monad containing a Stream
	 * 
	 * @param monads
	 * @return
	 */
	public static <A> ListTSeq<A> fromStream(AnyMSeq<Stream<A>> monads) {
		return of(monads.map(s -> s.collect(Collectors.toList())));
	}
   
   
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return run.toString();
	}
   
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> ListTSeq<T> unit(T unit) {
        return of(run.unit(ListX.of(unit)));
    }
    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMapIterable(e->e);
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
    public <R> ListTSeq<R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->ListX.of(i)));
    }
    @Override
    public <R> ListT<R> empty() {
       return of(run.empty());
    }
    @Override
    public AnyM<? extends Foldable<T>> nestedFoldables() {
        return run;
       
    }
    @Override
    public AnyM<? extends CyclopsCollectable<T>> nestedCollectables() {
        return run;
       
    }
    @Override
    public <T>ListTSeq<T> unitAnyM(AnyM<Traversable<T>> traversable) {
        
        return of((AnyMSeq)traversable.map(t->ListX.fromIterable(t)));
    }
    @Override
    public AnyMSeq<? extends Traversable<T>> transformerStream() {
        
        return run;
    }
    
    public static <T> ListTSeq<T> emptyList(){
        return ListT.fromIterable(ListX.empty());
    }
    public boolean isSeqPresent() {
      return !run.isEmpty();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public ListTSeq<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (ListTSeq<T>)ListT.super.combine(predicate, op);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycle(int)
     */
    @Override
    public ListTSeq<T> cycle(int times) {
       
        return (ListTSeq<T>)ListT.super.cycle(times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public ListTSeq<T> cycle(Monoid<T> m, int times) {
       
        return (ListTSeq<T>)ListT.super.cycle(m, times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<T> cycleWhile(Predicate<? super T> predicate) {
       
        return (ListTSeq<T>)ListT.super.cycleWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<T> cycleUntil(Predicate<? super T> predicate) {
       
        return (ListTSeq<T>)ListT.super.cycleUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> ListTSeq<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (ListTSeq<R>)ListT.super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> ListTSeq<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (ListTSeq<Tuple2<T, U>>)ListT.super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> ListTSeq<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (ListTSeq<Tuple2<T, U>>)ListT.super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> ListTSeq<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (ListTSeq)ListT.super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> ListTSeq<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (ListTSeq<Tuple4<T, T2, T3, T4>>)ListT.super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#zipWithIndex()
     */
    @Override
    public ListTSeq<Tuple2<T, Long>> zipWithIndex() {
       
        return (ListTSeq<Tuple2<T, Long>>)ListT.super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sliding(int)
     */
    @Override
    public ListTSeq<ListX<T>> sliding(int windowSize) {
       
        return (ListTSeq<ListX<T>>)ListT.super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sliding(int, int)
     */
    @Override
    public ListTSeq<ListX<T>> sliding(int windowSize, int increment) {
       
        return (ListTSeq<ListX<T>>)ListT.super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListTSeq<C> grouped(int size, Supplier<C> supplier) {
       
        return (ListTSeq<C> )ListT.super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (ListTSeq<ListX<T>>)ListT.super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public ListTSeq<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
       
        return (ListTSeq<ListX<T>>)ListT.super.groupedStatefullyWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (ListTSeq<ListX<T>>)ListT.super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListTSeq<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
       
        return (ListTSeq<C>)ListT.super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> ListTSeq<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
       
        return (ListTSeq<C>)ListT.super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(int)
     */
    @Override
    public ListTSeq<ListX<T>> grouped(int groupSize) {
       
        return ( ListTSeq<ListX<T>>)ListT.super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> ListTSeq<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (ListTSeq)ListT.super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#grouped(java.util.function.Function)
     */
    @Override
    public <K> ListTSeq<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (ListTSeq)ListT.super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#distinct()
     */
    @Override
    public ListTSeq<T> distinct() {
       
        return (ListTSeq<T>)ListT.super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public ListTSeq<T> scanLeft(Monoid<T> monoid) {
       
        return (ListTSeq<T>)ListT.super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> ListTSeq<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (ListTSeq<U>)ListT.super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public ListTSeq<T> scanRight(Monoid<T> monoid) {
       
        return (ListTSeq<T>)ListT.super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> ListTSeq<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (ListTSeq<U>)ListT.super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sorted()
     */
    @Override
    public ListTSeq<T> sorted() {
       
        return (ListTSeq<T>)ListT.super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sorted(java.util.Comparator)
     */
    @Override
    public ListTSeq<T> sorted(Comparator<? super T> c) {
       
        return (ListTSeq<T>)ListT.super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#takeWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<T> takeWhile(Predicate<? super T> p) {
       
        return (ListTSeq<T>)ListT.super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#dropWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<T> dropWhile(Predicate<? super T> p) {
       
        return (ListTSeq<T>)ListT.super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#takeUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<T> takeUntil(Predicate<? super T> p) {
       
        return (ListTSeq<T>)ListT.super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#dropUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<T> dropUntil(Predicate<? super T> p) {
       
        return (ListTSeq<T>)ListT.super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#dropRight(int)
     */
    @Override
    public ListTSeq<T> dropRight(int num) {
       
        return (ListTSeq<T>)ListT.super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#takeRight(int)
     */
    @Override
    public ListTSeq<T> takeRight(int num) {
       
        return (ListTSeq<T>)ListT.super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skip(long)
     */
    @Override
    public ListTSeq<T> skip(long num) {
       
        return (ListTSeq<T>)ListT.super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skipWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<T> skipWhile(Predicate<? super T> p) {
       
        return (ListTSeq<T>)ListT.super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skipUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<T> skipUntil(Predicate<? super T> p) {
       
        return (ListTSeq<T>)ListT.super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limit(long)
     */
    @Override
    public ListTSeq<T> limit(long num) {
       
        return (ListTSeq<T>)ListT.super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limitWhile(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<T> limitWhile(Predicate<? super T> p) {
       
        return (ListTSeq<T>)ListT.super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limitUntil(java.util.function.Predicate)
     */
    @Override
    public ListTSeq<T> limitUntil(Predicate<? super T> p) {
       
        return (ListTSeq<T>)ListT.super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#intersperse(java.lang.Object)
     */
    @Override
    public ListTSeq<T> intersperse(T value) {
       
        return (ListTSeq<T>)ListT.super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#reverse()
     */
    @Override
    public ListTSeq<T> reverse() {
       
        return (ListTSeq<T>)ListT.super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#shuffle()
     */
    @Override
    public ListTSeq<T> shuffle() {
       
        return (ListTSeq<T>)ListT.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#skipLast(int)
     */
    @Override
    public ListTSeq<T> skipLast(int num) {
       
        return (ListTSeq<T>)ListT.super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#limitLast(int)
     */
    @Override
    public ListTSeq<T> limitLast(int num) {
       
        return (ListTSeq<T>)ListT.super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#onEmpty(java.lang.Object)
     */
    @Override
    public ListTSeq<T> onEmpty(T value) {
       
        return (ListTSeq<T>)ListT.super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public ListTSeq<T> onEmptyGet(Supplier<T> supplier) {
       
        return (ListTSeq<T>)ListT.super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> ListTSeq<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (ListTSeq<T>)ListT.super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#shuffle(java.util.Random)
     */
    @Override
    public ListTSeq<T> shuffle(Random random) {
       
        return (ListTSeq<T>)ListT.super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#slice(long, long)
     */
    @Override
    public ListTSeq<T> slice(long from, long to) {
       
        return (ListTSeq<T>)ListT.super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ListT#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> ListTSeq<T> sorted(Function<? super T, ? extends U> function) {
        return (ListTSeq)ListT.super.sorted(function);
    }
    
    
}