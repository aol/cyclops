package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.ExtendedTraversable;
import com.aol.cyclops.types.FilterableFunctor;
import com.aol.cyclops.types.IterableCollectable;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
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
public class ListTSeq<T> implements ListT<T>,
                                ConvertableSequence<T>,
                                ExtendedTraversable<T>,
                                Sequential<T>,
                                CyclopsCollectable<T>,
                                IterableCollectable<T>,
                                FilterableFunctor<T>,
                                ZippingApplicativable<T>,
                                Publisher<T>{
                                   
   final AnyMSeq<List<T>> run;

   private ListTSeq(final AnyMSeq<List<T>> run){
       this.run = run;
   }
   /**
	 * @return The wrapped AnyM
	 */
   @Override
   public AnyMSeq<List<T>> unwrap(){
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
   public static <A> ListTSeq<A> of(AnyMSeq<List<A>> monads){
	   return new ListTSeq<>(monads);
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

    @Override
    public void subscribe(Subscriber<? super T> s) {
       run.forEach(e->ListX.fromIterable(e).subscribe(s));   
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<T> collectable() {
       return this;
    } 
    public <R> ListTSeq<R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->ListX.of(i)));
    }
    
}