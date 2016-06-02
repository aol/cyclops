package com.aol.cyclops.control.monads.transformers.seq;


import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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

import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.FutureWT;
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
 * Monad Transformer for Java  FutureWs
 * 
 * FutureWT consists of an AnyM instance that in turns wraps anoter Monad type that contains an FutureW
 * 
 * FutureWT<AnyMSeq<*SOME_MONAD_TYPE*<FutureW<T>>>>
 * 
 * FutureWT allows the deeply wrapped FutureW to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public class FutureWTSeq<A> implements FutureWT<A>, 
                                        ValueTransformerSeq<A>,
                                        IterableFoldable<A>,
                                        ConvertableSequence<A>,
                                        CyclopsCollectable<A>,
                                        Sequential<A>{
                                                
   
   private final AnyMSeq<FutureW<A>> run;
   /**
	 * @return The wrapped AnyM
	 */
   public AnyMSeq<FutureW<A>> unwrap(){
	   return run;
   }
   private FutureWTSeq(final AnyMSeq<FutureW<A>> run){
       this.run = run;
   }
   @Override
   public <T>  FutureWTSeq<T> unitStream(ReactiveSeq<T> traversable) {
       return FutureWT.fromStream(traversable.map(FutureW::ofResult));
      
   }

   @Override
   public <T>  FutureWTSeq<T> unitAnyM(AnyM<Traversable<T>> traversable) {
       
       return of((AnyMSeq)traversable.map(t->FutureW.fromIterable(t)));
   }
   @Override
   public AnyMSeq<? extends Traversable<A>> transformerStream() {
       
       return run.map(f->f.toListX());
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
   public MaybeTSeq<A> filter(Predicate<? super A> test) {
       return MaybeTSeq.of(run.map(opt -> opt.filter(test)));
   }
   /**
	 * Peek at the current value of the FutureW
	 * <pre>
	 * {@code 
	 *    FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
	 *             .peek(System.out::println);
	 *             
	 *     //prints 10        
	 * }
	 * </pre>
	 * 
	 * @param peek  Consumer to accept current value of FutureW
	 * @return FutureWT with peek call
	 */
   public FutureWTSeq<A> peek(Consumer<? super A> peek){
	  return of(run.peek(future-> future.map(a->{peek.accept(a); return a;})));
   }
   /**
	 * Map the wrapped FutureW
	 * 
	 * <pre>
	 * {@code 
	 *  FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
	 *             .map(t->t=t+1);
	 *  
	 *  
	 *  //FutureWT<AnyMSeq<Stream<FutureW[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped FutureW
	 * @return FutureWT that applies the map function to the wrapped FutureW
	 */   
   public <B> FutureWTSeq<B> map(Function<? super A,? extends B> f){
       return new FutureWTSeq<B>(run.map(o-> o.map(f)));
   }
   /**
	 * Flat Map the wrapped FutureW
	  * <pre>
	 * {@code 
	 *  FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
	 *             .flatMap(t->FutureW.completedFuture(20));
	 *  
	 *  
	 *  //FutureWT<AnyMSeq<Stream<FutureW[20]>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return FutureWT that applies the flatMap function to the wrapped FutureW
	 */

   public <B> FutureWTSeq<B> flatMapT(Function<? super A,FutureWTSeq<B>> f){
	   return of(run.map(future-> future.flatMap(a-> f.apply(a).run.stream().toList().get(0))));
   }
   private static  <B> AnyMSeq<FutureW<B>> narrow(AnyMSeq<FutureW<? extends B>> run){
       return (AnyMSeq)run;
   }
   public <B> FutureWTSeq<B> flatMap(Function<? super A,? extends MonadicValue<? extends B>> f){
      
       AnyMSeq<FutureW<? extends B>> mapped=  run.map(o -> o.flatMap(f));
       return of(narrow(mapped));
     
   }
   /**
	 * Lift a function into one that accepts and returns an FutureWT
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add list handling  / iteration (via FutureW) and iteration (via Stream) to an existing function
	 * <pre>
	 * {@code 
	    Function<Integer,Integer> add2 = i -> i+2;
		Function<FutureWT<Integer>, FutureWT<Integer>> optTAdd2 = FutureWT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyMSeq<Integer> stream = AnyM.fromStream(withNulls);
		AnyMSeq<FutureW<Integer>> streamOpt = stream.map(FutureW::completedFuture);
		List<Integer> results = optTAdd2.apply(FutureWT.of(streamOpt))
										.unwrap()
										.<Stream<FutureW<Integer>>>unwrap()
										.map(FutureW::join)
										.collect(Collectors.toList());
		
		
		//FutureW.completedFuture(List[3,4]);
	 * 
	 * 
	 * }</pre>
	 * 
	 * 
	 * @param fn Function to enhance with functionality from FutureW and another monad type
	 * @return Function that accepts and returns an FutureWT
	 */   
   public static <U, R> Function<FutureWTSeq<U>, FutureWTSeq<R>> lift(Function<? super U,? extends R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}
   /**
  	 * Lift a BiFunction into one that accepts and returns  FutureWTs
  	 * This allows multiple monad types to add functionality to existing functions and methods
  	 * 
  	 * e.g. to add list handling / iteration (via FutureW), iteration (via Stream)  and asynchronous execution (FutureW) 
  	 * to an existing function
  	 * 
  	 * <pre>
  	 * {@code 
  		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<FutureWT<Integer>,FutureWT<Integer>,FutureWT<Integer>> optTAdd2 = FutureWT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<FutureW<Integer>> streamOpt = stream.map(FutureW::completedFuture);
		
		FutureW<FutureW<Integer>> two = FutureW.completedFuture(FutureW.completedFuture(2));
		AnyMSeq<FutureW<Integer>> future=  AnyM.fromFutureW(two);
		List<Integer> results = optTAdd2.apply(FutureWT.of(streamOpt),FutureWT.of(future))
										.unwrap()
										.<Stream<FutureW<Integer>>>unwrap()
										.map(FutureW::join)
										.collect(Collectors.toList());
										
  			//FutureW.completedFuture(List[3,4,5]);						
  	  }
  	  </pre>
  	 * @param fn BiFunction to enhance with functionality from FutureW and another monad type
  	 * @return Function that accepts and returns an FutureWT
  	 */
	public static <U1, U2, R> BiFunction<FutureWTSeq<U1>, FutureWTSeq<U2>, FutureWTSeq<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an FutureWT from an AnyM that contains a monad type that contains type other than FutureW
	 * The values in the underlying monad will be mapped to FutureW<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an FutureW
	 * @return FutureWT
	 */
   public static <A> FutureWTSeq<A> fromAnyM(AnyMSeq<A> anyM){
	   return of(anyM.map(FutureW::ofResult));
   }
   /**
	 * Construct an FutureWT from an AnyM that wraps a monad containing  FutureWs
	 * 
	 * @param monads AnyM that contains a monad wrapping an FutureW
	 * @return FutureWT
	 */   
   public static <A> FutureWTSeq<A> of(AnyMSeq<FutureW<A>> monads){
	   return new FutureWTSeq<>(monads);
   }
   
   public static <A> FutureWTSeq<A> of(FutureW<A> monads){
       return FutureWT.fromIterable(ListX.of(monads));
   }
   
   
   /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
	    return String.format("FutureTSeq[%s]", run );
	}
	
    
    
    @Override
    public ReactiveSeq<A> stream() {
        return run.stream().map(cf->cf.get());
    }

    @Override
    public Iterator<A> iterator() {
       return stream().iterator();
    }


    
    public <R> FutureWTSeq<R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->FutureW.ofResult(i)));
    }
  
    public <R> FutureWTSeq<R> unit(R value){
       return of(run.unit(FutureW.ofResult(value)));
    }
    public <R> FutureWTSeq<R> empty(){
        return of(run.unit(FutureW.empty()));
     }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<A> collectable() {
        return stream();
    }
    public boolean isSeqPresent() {
       return !run.isEmpty();
    }
    public static <T> FutureWTSeq<T> emptyList() {
        return FutureWT.fromIterable(ListX.of());
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public FutureWTSeq<A> combine(BiPredicate<? super A, ? super A> predicate, BinaryOperator<A> op) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.combine(predicate, op);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(int)
     */
    @Override
    public FutureWTSeq<A> cycle(int times) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.cycle(times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public FutureWTSeq<A> cycle(Monoid<A> m, int times) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.cycle(m, times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<A> cycleWhile(Predicate<? super A> predicate) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.cycleWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<A> cycleUntil(Predicate<? super A> predicate) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.cycleUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> FutureWTSeq<R> zip(Iterable<? extends U> other, BiFunction<? super A, ? super U, ? extends R> zipper) {
       
        return (FutureWTSeq<R>)ValueTransformerSeq.super.zip(other, zipper);
    }
    @Override
    public <U, R> FutureWTSeq<R> zip(Stream<? extends U> other, BiFunction<? super A, ? super U, ? extends R> zipper) {
       
        return (FutureWTSeq<R>)ValueTransformerSeq.super.zip(other, zipper);
    }
    @Override
    public <U, R> FutureWTSeq<R> zip(Seq<? extends U> other, BiFunction<? super A, ? super U, ? extends R> zipper) {
       
        return (FutureWTSeq<R>)ValueTransformerSeq.super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> FutureWTSeq<Tuple2<A, U>> zip(Stream<? extends U> other) {
       
        return (FutureWTSeq)ValueTransformerSeq.super.zip(other);
    }
    @Override
    public <U> FutureWTSeq<Tuple2<A, U>> zip(Iterable<? extends U> other) {
       
        return (FutureWTSeq)ValueTransformerSeq.super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> FutureWTSeq<Tuple2<A, U>> zip(Seq<? extends U> other) {
       
        return (FutureWTSeq)ValueTransformerSeq.super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> FutureWTSeq<Tuple3<A, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (FutureWTSeq)ValueTransformerSeq.super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> FutureWTSeq<Tuple4<A, T2, T3, T4>> zip4(Stream<? extends T2> second, Stream<? extends T3> third,
            Stream<? extends T4> fourth) {
       
        return (FutureWTSeq)ValueTransformerSeq.super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipWithIndex()
     */
    @Override
    public FutureWTSeq<Tuple2<A, Long>> zipWithIndex() {
       
        return (FutureWTSeq<Tuple2<A, Long>>)ValueTransformerSeq.super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int)
     */
    @Override
    public FutureWTSeq<ListX<A>> sliding(int windowSize) {
       
        return (FutureWTSeq<ListX<A>>)ValueTransformerSeq.super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int, int)
     */
    @Override
    public FutureWTSeq<ListX<A>> sliding(int windowSize, int increment) {
       
        return (FutureWTSeq<ListX<A>>)ValueTransformerSeq.super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super A>> FutureWTSeq<C> grouped(int size, Supplier<C> supplier) {
       
        return (FutureWTSeq<C> )ValueTransformerSeq.super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<ListX<A>> groupedUntil(Predicate<? super A> predicate) {
       
        return (FutureWTSeq<ListX<A>>)ValueTransformerSeq.super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public FutureWTSeq<ListX<A>> groupedStatefullyWhile(BiPredicate<ListX<? super A>, ? super A> predicate) {
       
        return (FutureWTSeq<ListX<A>>)ValueTransformerSeq.super.groupedStatefullyWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<ListX<A>> groupedWhile(Predicate<? super A> predicate) {
       
        return (FutureWTSeq<ListX<A>>)ValueTransformerSeq.super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super A>> FutureWTSeq<C> groupedWhile(Predicate<? super A> predicate,
            Supplier<C> factory) {
       
        return (FutureWTSeq<C>)ValueTransformerSeq.super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super A>> FutureWTSeq<C> groupedUntil(Predicate<? super A> predicate,
            Supplier<C> factory) {
       
        return (FutureWTSeq<C>)ValueTransformerSeq.super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int)
     */
    @Override
    public FutureWTSeq<ListX<A>> grouped(int groupSize) {
       
        return ( FutureWTSeq<ListX<A>>)ValueTransformerSeq.super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, T, D> FutureWTSeq<Tuple2<K, D>> grouped(Function<? super A, ? extends K> classifier,
            Collector<? super A, T, D> downstream) {
       
        return (FutureWTSeq)ValueTransformerSeq.super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function)
     */
    @Override
    public <K> FutureWTSeq<Tuple2<K, Seq<A>>> grouped(Function<? super A, ? extends K> classifier) {
       
        return (FutureWTSeq)ValueTransformerSeq.super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#distinct()
     */
    @Override
    public FutureWTSeq<A> distinct() {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public FutureWTSeq<A> scanLeft(Monoid<A> monoid) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> FutureWTSeq<U> scanLeft(U seed, BiFunction<? super U, ? super A, ? extends U> function) {
       
        return (FutureWTSeq<U>)ValueTransformerSeq.super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public FutureWTSeq<A> scanRight(Monoid<A> monoid) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> FutureWTSeq<U> scanRight(U identity, BiFunction<? super A, ? super U,? extends U> combiner) {
       
        return (FutureWTSeq<U>)ValueTransformerSeq.super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted()
     */
    @Override
    public FutureWTSeq<A> sorted() {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.Comparator)
     */
    @Override
    public FutureWTSeq<A> sorted(Comparator<? super A> c) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<A> takeWhile(Predicate<? super A> p) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<A> dropWhile(Predicate<? super A> p) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<A> takeUntil(Predicate<? super A> p) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<A> dropUntil(Predicate<? super A> p) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropRight(int)
     */
    @Override
    public FutureWTSeq<A> dropRight(int num) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeRight(int)
     */
    @Override
    public FutureWTSeq<A> takeRight(int num) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skip(long)
     */
    @Override
    public FutureWTSeq<A> skip(long num) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<A> skipWhile(Predicate<? super A> p) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<A> skipUntil(Predicate<? super A> p) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limit(long)
     */
    @Override
    public FutureWTSeq<A> limit(long num) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<A> limitWhile(Predicate<? super A> p) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    public FutureWTSeq<A> limitUntil(Predicate<? super A> p) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#intersperse(java.lang.Object)
     */
    @Override
    public FutureWTSeq<A> intersperse(A value) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#reverse()
     */
    @Override
    public FutureWTSeq<A> reverse() {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle()
     */
    @Override
    public FutureWTSeq<A> shuffle() {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipLast(int)
     */
    @Override
    public FutureWTSeq<A> skipLast(int num) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitLast(int)
     */
    @Override
    public FutureWTSeq<A> limitLast(int num) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    public FutureWTSeq<A> onEmpty(A value) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public FutureWTSeq<A> onEmptyGet(Supplier<? extends A> supplier) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> FutureWTSeq<A> onEmptyThrow(Supplier<? extends X> supplier) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle(java.util.Random)
     */
    @Override
    public FutureWTSeq<A> shuffle(Random random) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#slice(long, long)
     */
    @Override
    public FutureWTSeq<A> slice(long from, long to) {
       
        return (FutureWTSeq<A>)ValueTransformerSeq.super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> FutureWTSeq<A> sorted(Function<? super A, ? extends U> function) {
        return (FutureWTSeq)ValueTransformerSeq.super.sorted(function);
    }
    @Override
    public int hashCode(){
        return run.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof FutureWTSeq){
            return run.equals( ((FutureWTSeq)o).run);
        }
        return false;
    }
 
}