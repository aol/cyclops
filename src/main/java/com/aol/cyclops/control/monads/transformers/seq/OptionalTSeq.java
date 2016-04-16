package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
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
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.control.monads.transformers.values.ValueTransformerSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;


/**
 * Monad transformer for JDK Optional
 * 
 * OptionalT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Optional
 * 
 * OptionalT<AnyMSeq<*SOME_MONAD_TYPE*<Optional<T>>>>
 * 
 * OptionalT allows the deeply wrapped Optional to be manipulating within it's nested /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the Optional within
 */
public class OptionalTSeq<T> implements OptionalT<T>,
                                        ValueTransformerSeq<T>,
                                        Foldable<T>,
                                        ConvertableSequence<T>,
                                        CyclopsCollectable<T>,
                                        Sequential<T>{
   
   private final AnyMSeq<Optional<T>> run;
   
   
   private OptionalTSeq(final AnyMSeq<Optional<T>> run){
       this.run = run;
   }
   
	/**
	 * @return The wrapped AnyM
	 */
	public AnyMSeq<Optional<T>> unwrap() {
		return run;
	}
	@Override
    public <T>  OptionalTSeq<T> unitStream(ReactiveSeq<T> traversable) {
        return OptionalT.fromStream(traversable.map(Optional::of));
       
    }

	@Override
    public <T>  OptionalTSeq<T> unitAnyM(AnyM<Traversable<T>> traversable) {
        
        return of((AnyMSeq)traversable.map(t-> Maybe.fromIterable(t).toOptional() ));
    }
    @Override
    public AnyMSeq<? extends Traversable<T>> transformerStream() {
        
        return run.map(e->Maybe.fromOptional(e).toListX());
    }
	/**
	 * Peek at the current value of the Optional
	 * <pre>
	 * {@code 
	 *    OptionalT.of(AnyM.fromStream(Optional.of(10))
	 *             .peek(System.out::println);
	 *             
	 *     //prints 10        
	 * }
	 * </pre>
	 * 
	 * @param peek  Consumer to accept current value of Optional
	 * @return OptionalT with peek call
	 */
	public OptionalTSeq<T> peek(Consumer<? super T> peek) {
		return of(run.peek(opt -> opt.map(a -> {
			peek.accept(a);
			return a;
		})));
	}
   
	/**
	 * Filter the wrapped Optional
	 * <pre>
	 * {@code 
	 *    OptionalT.of(AnyM.fromStream(Optional.of(10))
	 *             .filter(t->t!=10);
	 *             
	 *     //OptionalT<AnyMSeq<Stream<Optional.empty>>>
	 * }
	 * </pre>
	 * @param test Predicate to filter the wrapped Optional
	 * @return OptionalT that applies the provided filter
	 */
	public OptionalTSeq<T> filter(Predicate<? super T> test) {
		return of(run.map(opt -> opt.filter(test)));
	}

	/**
	 * Map the wrapped Optional
	 * 
	 * <pre>
	 * {@code 
	 *  OptionalT.of(AnyM.fromStream(Optional.of(10))
	 *             .map(t->t=t+1);
	 *  
	 *  
	 *  //OptionalT<AnyMSeq<Stream<Optional[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped Optional
	 * @return OptionalT that applies the map function to the wrapped Optional
	 */
	@Override
	public <B> OptionalTSeq<B> map(Function<? super T,? extends B> f) {
		return new OptionalTSeq<B>(run.map(o -> o.map(f)));
	}

	/**
	 * Flat Map the wrapped Optional
	  * <pre>
	 * {@code 
	 *  OptionalT.of(AnyM.fromStream(Optional.of(10))
	 *             .flatMap(t->Optional.empty();
	 *  
	 *  
	 *  //OptionalT<AnyMSeq<Stream<Optional.empty>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return OptionalT that applies the flatMap function to the wrapped Optional
	 */
	public <B> OptionalTSeq<B> flatMapT(Function<? super T, OptionalTSeq<? extends B>> f) {

		return of(run.bind(opt -> {
			if (opt.isPresent())
				return f.apply(opt.get()).run.unwrap();
			return run.unit(Optional.<B> empty()).unwrap();
		}));

	}
	private static  <B> AnyMSeq<Optional<B>> narrow(AnyMSeq<Optional<? extends B>> run){
	       return (AnyMSeq)run;
	}
	@Override
	public <B> OptionalTSeq<B> flatMap(Function<? super T, ? extends MonadicValue<? extends B>> f) {
	     AnyMSeq<Optional<? extends B>> mapped=  run.map(o -> Maybe.fromOptional(o).flatMap(f).toOptional());
	     return of(narrow(mapped));
	}
	/**
	 * Lift a function into one that accepts and returns an OptionalT
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add null handling (via Optional) and iteration (via Stream) to an existing function
	 * <pre>
	 * {@code 
	 *  Function<Integer,Integer> add2 = i -> i+2;
		Function<OptionalT<Integer>, OptionalT<Integer>> optTAdd2 = OptionalT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
		List<Integer> results = optTAdd2.apply(OptionalT.of(streamOpt))
										.unwrap()
										.<Stream<Optional<Integer>>>unwrap()
										.filter(Optional::isPresent)
										.map(Optional::get)
										.collect(Collectors.toList());
		
		//Arrays.asList(3,4);
	 * 
	 * 
	 * }</pre>
	 * 
	 * 
	 * @param fn Function to enhance with functionality from Optional and another monad type
	 * @return Function that accepts and returns an OptionalT
	 */
	public static <U, R> Function<OptionalTSeq<U>, OptionalTSeq<R>> lift(Function<U, R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}

	/**
	 * Lift a BiFunction into one that accepts and returns  OptionalTs
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add null handling (via Optional), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
	 * to an existing function
	 * 
	 * <pre>
	 * {@code 
	 * BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<OptionalT<Integer>,OptionalT<Integer>, OptionalT<Integer>> optTAdd2 = OptionalT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
		
		CompletableFuture<Optional<Integer>> two = CompletableFuture.supplyAsync(()->Optional.of(2));
		AnyMSeq<Optional<Integer>> future=  AnyM.ofMonad(two);
		List<Integer> results = optTAdd2.apply(OptionalT.of(streamOpt),OptionalT.of(future))
										.unwrap()
										.<Stream<Optional<Integer>>>unwrap()
										.filter(Optional::isPresent)
										.map(Optional::get)
										.collect(Collectors.toList());
			//Arrays.asList(3,4);							
	  }
	  </pre>
	 * @param fn BiFunction to enhance with functionality from Optional and another monad type
	 * @return Function that accepts and returns an OptionalT
	 */
	public static <U1, U2, R> BiFunction<OptionalTSeq<U1>, OptionalTSeq<U2>, OptionalTSeq<R>> lift2(BiFunction<? super U1, ? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

	/**
	 * Construct an OptionalT from an AnyM that contains a monad type that contains type other than Optional
	 * The values in the underlying monad will be mapped to Optional<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Optional
	 * @return OptionalT
	 */
	public static <A> OptionalTSeq<A> fromAnyM(AnyMSeq<A> anyM) {
		return of(anyM.map(Optional::ofNullable));
	}
   
	/**
	 * Construct an OptionalT from an AnyM that wraps a monad containing  Optionals
	 * 
	 * @param monads AnyM that contains a monad wrapping an Optional
	 * @return OptionalT
	 */
	public static <A> OptionalTSeq<A> of(AnyMSeq<Optional<A>> monads) {
		return new OptionalTSeq<>(monads);
	}
	public static <A> OptionalTSeq<A> of(Optional<A> monads){
        return OptionalT.fromIterable(ListX.of(monads));
    }
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return run.toString();
	}

    public boolean isPresent() {
        return run.anyMatch(m -> m.isPresent());
    }
    
    public <R> OptionalTSeq<R> unit(R value) {
        return of(run.unit(Optional.of(value)));
    }

    public <R> OptionalTSeq<R> empty() {
        return of(run.unit(Optional.empty()));
    }
    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMapIterable(e->Maybe.fromOptional(e));
    }

    @Override
    public Iterator<T> iterator() {
       return stream().iterator();
    }

    
   
    public <R> OptionalTSeq<R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->Optional.of(i)));
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
     public static <T> OptionalTSeq<T> emptyList() {
         return OptionalT.fromIterable(ListX.of());
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
      */
     @Override
     public OptionalTSeq<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.combine(predicate, op);
     }
     
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(int)
      */
     @Override
     public OptionalTSeq<T> cycle(int times) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.cycle(times);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(com.aol.cyclops.Monoid, int)
      */
     @Override
     public OptionalTSeq<T> cycle(Monoid<T> m, int times) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.cycle(m, times);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleWhile(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<T> cycleWhile(Predicate<? super T> predicate) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.cycleWhile(predicate);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleUntil(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<T> cycleUntil(Predicate<? super T> predicate) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.cycleUntil(predicate);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
      */
     @Override
     public <U, R> OptionalTSeq<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        
         return (OptionalTSeq<R>)ValueTransformerSeq.super.zip(other, zipper);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipStream(java.util.stream.Stream)
      */
     @Override
     public <U> OptionalTSeq<Tuple2<T, U>> zipStream(Stream<U> other) {
        
         return (OptionalTSeq<Tuple2<T, U>>)ValueTransformerSeq.super.zipStream(other);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(org.jooq.lambda.Seq)
      */
     @Override
     public <U> OptionalTSeq<Tuple2<T, U>> zip(Seq<U> other) {
        
         return (OptionalTSeq<Tuple2<T, U>>)ValueTransformerSeq.super.zip(other);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
      */
     @Override
     public <S, U> OptionalTSeq<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
        
         return (OptionalTSeq)ValueTransformerSeq.super.zip3(second, third);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
      */
     @Override
     public <T2, T3, T4> OptionalTSeq<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
             Stream<T4> fourth) {
        
         return (OptionalTSeq<Tuple4<T, T2, T3, T4>>)ValueTransformerSeq.super.zip4(second, third, fourth);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipWithIndex()
      */
     @Override
     public OptionalTSeq<Tuple2<T, Long>> zipWithIndex() {
        
         return (OptionalTSeq<Tuple2<T, Long>>)ValueTransformerSeq.super.zipWithIndex();
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int)
      */
     @Override
     public OptionalTSeq<ListX<T>> sliding(int windowSize) {
        
         return (OptionalTSeq<ListX<T>>)ValueTransformerSeq.super.sliding(windowSize);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int, int)
      */
     @Override
     public OptionalTSeq<ListX<T>> sliding(int windowSize, int increment) {
        
         return (OptionalTSeq<ListX<T>>)ValueTransformerSeq.super.sliding(windowSize, increment);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int, java.util.function.Supplier)
      */
     @Override
     public <C extends Collection<? super T>> OptionalTSeq<C> grouped(int size, Supplier<C> supplier) {
        
         return (OptionalTSeq<C> )ValueTransformerSeq.super.grouped(size, supplier);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        
         return (OptionalTSeq<ListX<T>>)ValueTransformerSeq.super.groupedUntil(predicate);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedStatefullyWhile(java.util.function.BiPredicate)
      */
     @Override
     public OptionalTSeq<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
         return (OptionalTSeq<ListX<T>>)ValueTransformerSeq.super.groupedStatefullyWhile(predicate);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        
         return (OptionalTSeq<ListX<T>>)ValueTransformerSeq.super.groupedWhile(predicate);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
      */
     @Override
     public <C extends Collection<? super T>> OptionalTSeq<C> groupedWhile(Predicate<? super T> predicate,
             Supplier<C> factory) {
        
         return (OptionalTSeq<C>)ValueTransformerSeq.super.groupedWhile(predicate, factory);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
      */
     @Override
     public <C extends Collection<? super T>> OptionalTSeq<C> groupedUntil(Predicate<? super T> predicate,
             Supplier<C> factory) {
        
         return (OptionalTSeq<C>)ValueTransformerSeq.super.groupedUntil(predicate, factory);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int)
      */
     @Override
     public OptionalTSeq<ListX<T>> grouped(int groupSize) {
        
         return ( OptionalTSeq<ListX<T>>)ValueTransformerSeq.super.grouped(groupSize);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
      */
     @Override
     public <K, A, D> OptionalTSeq<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
             Collector<? super T, A, D> downstream) {
        
         return (OptionalTSeq)ValueTransformerSeq.super.grouped(classifier, downstream);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function)
      */
     @Override
     public <K> OptionalTSeq<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
        
         return (OptionalTSeq)ValueTransformerSeq.super.grouped(classifier);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#distinct()
      */
     @Override
     public OptionalTSeq<T> distinct() {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.distinct();
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(com.aol.cyclops.Monoid)
      */
     @Override
     public OptionalTSeq<T> scanLeft(Monoid<T> monoid) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.scanLeft(monoid);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
      */
     @Override
     public <U> OptionalTSeq<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
        
         return (OptionalTSeq<U>)ValueTransformerSeq.super.scanLeft(seed, function);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(com.aol.cyclops.Monoid)
      */
     @Override
     public OptionalTSeq<T> scanRight(Monoid<T> monoid) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.scanRight(monoid);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
      */
     @Override
     public <U> OptionalTSeq<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
        
         return (OptionalTSeq<U>)ValueTransformerSeq.super.scanRight(identity, combiner);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted()
      */
     @Override
     public OptionalTSeq<T> sorted() {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.sorted();
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.Comparator)
      */
     @Override
     public OptionalTSeq<T> sorted(Comparator<? super T> c) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.sorted(c);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeWhile(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<T> takeWhile(Predicate<? super T> p) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.takeWhile(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropWhile(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<T> dropWhile(Predicate<? super T> p) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.dropWhile(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeUntil(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<T> takeUntil(Predicate<? super T> p) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.takeUntil(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropUntil(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<T> dropUntil(Predicate<? super T> p) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.dropUntil(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropRight(int)
      */
     @Override
     public OptionalTSeq<T> dropRight(int num) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.dropRight(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeRight(int)
      */
     @Override
     public OptionalTSeq<T> takeRight(int num) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.takeRight(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skip(long)
      */
     @Override
     public OptionalTSeq<T> skip(long num) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.skip(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipWhile(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<T> skipWhile(Predicate<? super T> p) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.skipWhile(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipUntil(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<T> skipUntil(Predicate<? super T> p) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.skipUntil(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limit(long)
      */
     @Override
     public OptionalTSeq<T> limit(long num) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.limit(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitWhile(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<T> limitWhile(Predicate<? super T> p) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.limitWhile(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitUntil(java.util.function.Predicate)
      */
     @Override
     public OptionalTSeq<T> limitUntil(Predicate<? super T> p) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.limitUntil(p);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#intersperse(java.lang.Object)
      */
     @Override
     public OptionalTSeq<T> intersperse(T value) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.intersperse(value);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#reverse()
      */
     @Override
     public OptionalTSeq<T> reverse() {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.reverse();
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle()
      */
     @Override
     public OptionalTSeq<T> shuffle() {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.shuffle();
     }

     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipLast(int)
      */
     @Override
     public OptionalTSeq<T> skipLast(int num) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.skipLast(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitLast(int)
      */
     @Override
     public OptionalTSeq<T> limitLast(int num) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.limitLast(num);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmpty(java.lang.Object)
      */
     @Override
     public OptionalTSeq<T> onEmpty(T value) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.onEmpty(value);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyGet(java.util.function.Supplier)
      */
     @Override
     public OptionalTSeq<T> onEmptyGet(Supplier<T> supplier) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.onEmptyGet(supplier);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyThrow(java.util.function.Supplier)
      */
     @Override
     public <X extends Throwable> OptionalTSeq<T> onEmptyThrow(Supplier<X> supplier) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.onEmptyThrow(supplier);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle(java.util.Random)
      */
     @Override
     public OptionalTSeq<T> shuffle(Random random) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.shuffle(random);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#slice(long, long)
      */
     @Override
     public OptionalTSeq<T> slice(long from, long to) {
        
         return (OptionalTSeq<T>)ValueTransformerSeq.super.slice(from, to);
     }
     /* (non-Javadoc)
      * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.function.Function)
      */
     @Override
     public <U extends Comparable<? super U>> OptionalTSeq<T> sorted(Function<? super T, ? extends U> function) {
         return (OptionalTSeq)ValueTransformerSeq.super.sorted(function);
     }
}
