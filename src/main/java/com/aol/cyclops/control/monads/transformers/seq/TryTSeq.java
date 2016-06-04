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
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Try.Success;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.control.monads.transformers.values.ValueTransformerSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;


/**
 * Monad transformer for JDK Try
 * 
 * TryT consists of an AnyM instance that in turns wraps another Monad type that contains an Try
 * 
 * TryT<AnyMSeq<*SOME_MONAD_TYPE*<Try<T>>>>
 * 
 * TryT allows the deeply wrapped Try to be manipulating within it's nested /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the Try within
 */
public class TryTSeq<T,X extends Throwable> implements TryT<T,X>,
                                                        ValueTransformerSeq<T>,
                                                        IterableFoldable<T>,
                                                        ConvertableSequence<T>,
                                                        CyclopsCollectable<T>,
                                                        Sequential<T>{
                                                       
   private final AnyMSeq<Try<T,X>> run;
   
   
   private TryTSeq(final AnyMSeq<Try<T,X>> run){
       this.run = run;
   }
   
	/**
	 * @return The wrapped AnyM
	 */
   @Override
	public AnyMSeq<Try<T,X>> unwrap() {
		return run;
	}

   
	/**
	 * Peek at the current value of the Try
	 * <pre>
	 * {@code 
	 *    TryT.of(AnyM.fromStream(Try.success(10))
	 *             .peek(System.out::println);
	 *             
	 *     //prints 10        
	 * }
	 * </pre>
	 * 
	 * @param peek  Consumer to accept current value of Try
	 * @return TryT with peek call
	 */
   @Override
	public TryTSeq<T,X> peek(Consumer<? super T> peek) {
		return of(run.peek(opt -> opt.map(a -> {
			peek.accept(a);
			return a;
		})));
	}
   
	/**
	 * Filter the wrapped Try
	 * <pre>
	 * {@code 
	 *    TryT.of(AnyM.fromStream(Try.success(10))
	 *             .filter(t->t!=10);
	 *             
	 *     //TryT<AnyMSeq<Stream<Optional.empty>>>
	 * }
	 * </pre>
	 * @param test Predicate to filter the wrapped Try
	 * @return OptionalT that applies the provided filter
	 */
   @Override
	public MaybeTSeq<T> filter(Predicate<? super T> test) {
		return MaybeTSeq.of(run.map(opt -> opt.filter(test)));
	}

	/**
	 * Map the wrapped Try
	 * 
	 * <pre>
	 * {@code 
	 *  TryT.of(AnyM.fromStream(Try.success(10))
	 *             .map(t->t=t+1);
	 *  
	 *  
	 *  //TryT<AnyMSeq<Stream<Success[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped Try
	 * @return TryT that applies the map function to the wrapped Try
	 */
   @Override
	public <B> TryTSeq<B,X> map(Function<? super T,? extends B> f) {
		return new TryTSeq<B,X>(run.map(o -> o.map(f)));
	}

	/**
	 * Flat Map the wrapped Try
	  * <pre>
	 * {@code 
	 *  TryT.of(AnyM.fromStream(Try.success(10))
	 *             .flatMap(t->Try.failure(new Exception());
	 *  
	 *  
	 *  //TryT<AnyMSeq<Stream<Failure[Excption]>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return TryT that applies the flatMap function to the wrapped Try
	 */
	public <B> TryTSeq<B,X> flatMapT(Function<? super T, TryTSeq<B,X>> f) {

		return of(run.bind(opt -> {
			if (opt.isSuccess())
				return f.apply(opt.get()).run.unwrap();
			Try<B,X> ret = (Try)opt;
			return run.unit(ret).unwrap();
		}));

	}
	@Override
    public <B> TryTSeq<B,X> flatMap(Function<? super T, ? extends Try<B,X>> f) {

        return new TryTSeq<B,X>(run.map(o -> o.flatMap(f)));

    }
	@Override
    public <T>  TryTSeq<T,X> unitStream(ReactiveSeq<T> traversable) {
        return TryT.fromStream(traversable.map(Try::success));
       
    }

	@Override
    public <T>  TryTSeq<T,X> unitAnyM(AnyM<Traversable<T>> traversable) {
        
        return of((AnyMSeq)traversable.map(t->Try.fromIterable(t)));
    }
    @Override
    public AnyMSeq<? extends Traversable<T>> transformerStream() {
        
        return run.map(t->t.toListX());
    }
	/**
	 * Lift a function into one that accepts and returns an TryT
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add Exception Handling (via Try) and iteration (via Stream) to an existing function
	 * <pre>
	 * {@code 
	 *  Function<Integer,Integer> add2 = i -> i+2;
		Function<TryT<Integer,RuntimeException>, TryT<Integer,RuntimeException>> optTAdd2 = TryT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<Try<Integer,RuntimeException>> streamOpt = stream.map(this::toTry);
		List<Integer> results = optTAdd2.apply(TryT.of(streamOpt))
										.unwrap()
										.<Stream<Try<Integer,RuntimeException>>>unwrap()
										.filter(Try::isSuccess)
										.map(Try::get)
										.collect(Collectors.toList());
		
		//Arrays.asList(3,4);
	 * 
	 * 
	 * }</pre>
	 * 
	 * 
	 * @param fn Function to enhance with functionality from Try and another monad type
	 * @return Function that accepts and returns an TryT
	 */
	public static <U, R, X extends Throwable> Function<TryTSeq<U,X>, TryTSeq<R,X>> lift(Function<? super U,? extends R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}

	/**
	 * Lift a BiFunction into one that accepts and returns  TryTs
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add Exception handling (via Try), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
	 * to an existing function
	 * 
	 * <pre>
	 * {@code 
	BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<TryT<Integer,RuntimeException>,TryT<Integer,RuntimeException>, TryT<Integer,RuntimeException>> optTAdd2 = TryT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<Try<Integer,RuntimeException>> streamOpt = stream.map(this::toTry);
		
		CompletableFuture<Try<Integer,RuntimeException>> two = CompletableFuture.completedFuture(Try.of(2));
		AnyMSeq<Try<Integer,RuntimeException>> future=  AnyM.ofMonad(two);
		List<Integer> results = optTAdd2.apply(TryT.of(streamOpt),TryT.of(future))
										.unwrap()
										.<Stream<Try<Integer,RuntimeException>>>unwrap()
										.filter(Try::isSuccess)
										.map(Try::get)
										.collect(Collectors.toList());
			//Arrays.asList(3,4);							
	  }
	  </pre>
	 * @param fn BiFunction to enhance with functionality from Try and another monad type
	 * @return Function that accepts and returns an TryT
	 */
	public static <U1, U2, R, X extends Throwable> BiFunction<TryTSeq<U1,X>, TryTSeq<U2,X>, TryTSeq<R,X>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

	/**
	 * Construct an TryT from an AnyM that contains a monad type that contains type other than Try
	 * The values in the underlying monad will be mapped to Try<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Try
	 * @return TryT
	 */
	@SuppressWarnings("unchecked")
	public static <A, X extends Throwable> TryTSeq<A,X> fromAnyM(AnyMSeq<A> anyM) {
		return (TryTSeq<A, X>) of(anyM.map(Try::success));
	}
   
	/**
	 * Construct an TryT from an AnyM that wraps a monad containing  Trys
	 * 
	 * @param monads AnyM that contains a monad wrapping an Try
	 * @return TryT
	 */
	public static <A,X extends Throwable> TryTSeq<A,X> of(AnyMSeq<Try<A,X>> monads) {
		return new TryTSeq<>(monads);
	}
	public static <A,X extends Throwable> TryTSeq<A,X> of(Try<A,X> monads){
        return TryT.fromIterable(ListX.of(monads));
    }
   
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
	    return String.format("TryTSeq[%s]", run );
	}
   
    
    
    
    public boolean isSuccess(){
        return run.allMatch(t->t.isSuccess());
    }
    public boolean isFailure(){
        return run.allMatch(t->t.isFailure());
    }

 

  
    public <R> R visit(Function<? super T,? extends R> success, 
            Function<? super X,? extends R> failure,Monoid<R> combiner){
        return run.map(t->t.visit(success, failure)).reduce(combiner);   
    }
    
   
    

   
    
    public <R> TryTSeq<R,X> unit(R value){
       return of(run.unit(Try.success(value)));
    }
    public <R> TryTSeq<R,X> empty(){
        return of(run.unit(Try.failure(null)));
     }
    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMapIterable(e->e);
    }

    @Override
    public Iterator<T> iterator() {
       return stream().iterator();
    }

   
    public <R> TryTSeq<R,X> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->Try.success(i)));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<T> collectable() {
        return stream();
    }
    public boolean isSeqPresent() {
        return !run.isEmpty();
     }
     public static <T,X extends Throwable> TryTSeq<T,X> emptyList() {
         return TryT.fromIterable(ListX.of());
     }
     
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
      */
     @Override
     public TryTSeq<T,X> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.combine(predicate, op);
     }
     
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(int)
      */
     @Override
     public TryTSeq<T,X> cycle(int times) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.cycle(times);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(com.aol.cyclops.Monoid, int)
      */
     @Override
     public TryTSeq<T,X> cycle(Monoid<T> m, int times) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.cycle(m, times);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleWhile(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<T,X> cycleWhile(Predicate<? super T> predicate) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.cycleWhile(predicate);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleUntil(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<T,X> cycleUntil(Predicate<? super T> predicate) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.cycleUntil(predicate);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
      */
     @Override
     public <U, R> TryTSeq<R,X> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        
         return (TryTSeq<R,X>)ValueTransformerSeq.super.zip(other, zipper);
     }
     @Override
     public <U, R> TryTSeq<R,X> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        
         return (TryTSeq<R,X>)ValueTransformerSeq.super.zip(other, zipper);
     }
     @Override
     public <U, R> TryTSeq<R,X> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        
         return (TryTSeq<R,X>)ValueTransformerSeq.super.zip(other, zipper);
     }
     
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(org.jooq.lambda.Seq)
      */
     @Override
     public <U> TryTSeq<Tuple2<T, U>,X> zip(Stream<? extends U> other) {
        
         return (TryTSeq)ValueTransformerSeq.super.zip(other);
     }
     @Override
     public <U> TryTSeq<Tuple2<T, U>,X> zip(Seq<? extends U> other) {
        
         return (TryTSeq)ValueTransformerSeq.super.zip(other);
     }
     @Override
     public <U> TryTSeq<Tuple2<T, U>,X> zip(Iterable<? extends U> other) {
        
         return (TryTSeq)ValueTransformerSeq.super.zip(other);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
      */
     @Override
     public <S, U> TryTSeq<Tuple3<T, S, U>,X> zip3(Stream<? extends S> second, Stream<? extends U> third) {
        
         return (TryTSeq)ValueTransformerSeq.super.zip3(second, third);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
      */
     @Override
     public <T2, T3, T4> TryTSeq<Tuple4<T, T2, T3, T4>,X> zip4(Stream<? extends T2> second, Stream<? extends T3> third,
             Stream<? extends T4> fourth) {
        
         return (TryTSeq)ValueTransformerSeq.super.zip4(second, third, fourth);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipWithIndex()
      */
     @Override
     public TryTSeq<Tuple2<T, Long>,X> zipWithIndex() {
        
         return (TryTSeq<Tuple2<T, Long>,X>)ValueTransformerSeq.super.zipWithIndex();
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int)
      */
     @Override
     public TryTSeq<ListX<T>,X> sliding(int windowSize) {
        
         return (TryTSeq<ListX<T>,X>)ValueTransformerSeq.super.sliding(windowSize);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int, int)
      */
     @Override
     public TryTSeq<ListX<T>,X> sliding(int windowSize, int increment) {
        
         return (TryTSeq<ListX<T>,X>)ValueTransformerSeq.super.sliding(windowSize, increment);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int, java.util.function.Supplier)
      */
     @Override
     public <C extends Collection<? super T>> TryTSeq<C,X> grouped(int size, Supplier<C> supplier) {
        
         return (TryTSeq<C,X> )ValueTransformerSeq.super.grouped(size, supplier);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<ListX<T>,X> groupedUntil(Predicate<? super T> predicate) {
        
         return (TryTSeq<ListX<T>,X>)ValueTransformerSeq.super.groupedUntil(predicate);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedStatefullyWhile(java.util.function.BiPredicate)
      */
     @Override
     public TryTSeq<ListX<T>,X> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
         return (TryTSeq<ListX<T>,X>)ValueTransformerSeq.super.groupedStatefullyWhile(predicate);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<ListX<T>,X> groupedWhile(Predicate<? super T> predicate) {
        
         return (TryTSeq<ListX<T>,X>)ValueTransformerSeq.super.groupedWhile(predicate);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
      */
     @Override
     public <C extends Collection<? super T>> TryTSeq<C,X> groupedWhile(Predicate<? super T> predicate,
             Supplier<C> factory) {
        
         return (TryTSeq<C,X>)ValueTransformerSeq.super.groupedWhile(predicate, factory);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
      */
     @Override
     public <C extends Collection<? super T>> TryTSeq<C,X> groupedUntil(Predicate<? super T> predicate,
             Supplier<C> factory) {
        
         return (TryTSeq<C,X>)ValueTransformerSeq.super.groupedUntil(predicate, factory);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int)
      */
     @Override
     public TryTSeq<ListX<T>,X> grouped(int groupSize) {
        
         return ( TryTSeq<ListX<T>,X>)ValueTransformerSeq.super.grouped(groupSize);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
      */
     @Override
     public <K, A, D> TryTSeq<Tuple2<K, D>,X> grouped(Function<? super T, ? extends K> classifier,
             Collector<? super T, A, D> downstream) {
        
         return (TryTSeq)ValueTransformerSeq.super.grouped(classifier, downstream);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function)
      */
     @Override
     public <K> TryTSeq<Tuple2<K, Seq<T>>,X> grouped(Function<? super T, ? extends K> classifier) {
        
         return (TryTSeq)ValueTransformerSeq.super.grouped(classifier);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#distinct()
      */
     @Override
     public TryTSeq<T,X> distinct() {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.distinct();
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(com.aol.cyclops.Monoid)
      */
     @Override
     public TryTSeq<T,X> scanLeft(Monoid<T> monoid) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.scanLeft(monoid);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
      */
     @Override
     public <U> TryTSeq<U,X> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        
         return (TryTSeq<U,X>)ValueTransformerSeq.super.scanLeft(seed, function);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(com.aol.cyclops.Monoid)
      */
     @Override
     public TryTSeq<T,X> scanRight(Monoid<T> monoid) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.scanRight(monoid);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
      */
     @Override
     public <U> TryTSeq<U,X> scanRight(U identity, BiFunction<? super T, ? super U,? extends U> combiner) {
        
         return (TryTSeq<U,X>)ValueTransformerSeq.super.scanRight(identity, combiner);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted()
      */
     @Override
     public TryTSeq<T,X> sorted() {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.sorted();
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.Comparator)
      */
     @Override
     public TryTSeq<T,X> sorted(Comparator<? super T> c) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.sorted(c);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeWhile(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<T,X> takeWhile(Predicate<? super T> p) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.takeWhile(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropWhile(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<T,X> dropWhile(Predicate<? super T> p) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.dropWhile(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeUntil(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<T,X> takeUntil(Predicate<? super T> p) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.takeUntil(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropUntil(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<T,X> dropUntil(Predicate<? super T> p) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.dropUntil(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropRight(int)
      */
     @Override
     public TryTSeq<T,X> dropRight(int num) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.dropRight(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeRight(int)
      */
     @Override
     public TryTSeq<T,X> takeRight(int num) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.takeRight(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skip(long)
      */
     @Override
     public TryTSeq<T,X> skip(long num) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.skip(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipWhile(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<T,X> skipWhile(Predicate<? super T> p) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.skipWhile(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipUntil(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<T,X> skipUntil(Predicate<? super T> p) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.skipUntil(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limit(long)
      */
     @Override
     public TryTSeq<T,X> limit(long num) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.limit(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitWhile(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<T,X> limitWhile(Predicate<? super T> p) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.limitWhile(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitUntil(java.util.function.Predicate)
      */
     @Override
     public TryTSeq<T,X> limitUntil(Predicate<? super T> p) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.limitUntil(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#intersperse(java.lang.Object)
      */
     @Override
     public TryTSeq<T,X> intersperse(T value) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.intersperse(value);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#reverse()
      */
     @Override
     public TryTSeq<T,X> reverse() {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.reverse();
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle()
      */
     @Override
     public TryTSeq<T,X> shuffle() {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.shuffle();
     }

     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipLast(int)
      */
     @Override
     public TryTSeq<T,X> skipLast(int num) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.skipLast(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitLast(int)
      */
     @Override
     public TryTSeq<T,X> limitLast(int num) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.limitLast(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmpty(java.lang.Object)
      */
     @Override
     public TryTSeq<T,X> onEmpty(T value) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.onEmpty(value);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyGet(java.util.function.Supplier)
      */
     @Override
     public TryTSeq<T,X> onEmptyGet(Supplier<? extends T> supplier) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.onEmptyGet(supplier);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyThrow(java.util.function.Supplier)
      */
     @Override
     public <X extends Throwable> TryTSeq<T,X> onEmptyThrow(Supplier<? extends X> supplier) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.onEmptyThrow(supplier);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle(java.util.Random)
      */
     @Override
     public TryTSeq<T,X> shuffle(Random random) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.shuffle(random);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#slice(long, long)
      */
     @Override
     public TryTSeq<T,X> slice(long from, long to) {
        
         return (TryTSeq<T,X>)ValueTransformerSeq.super.slice(from, to);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.function.Function)
      */
     @Override
     public <U extends Comparable<? super U>> TryTSeq<T,X> sorted(Function<? super T, ? extends U> function) {
         return (TryTSeq)ValueTransformerSeq.super.sorted(function);
     }
     @Override
     public int hashCode(){
         return run.hashCode();
     }
     
     @Override
     public boolean equals(Object o){
         if(o instanceof TryTSeq){
             return run.equals( ((TryTSeq)o).run);
         }
         return false;
     }
    
}
