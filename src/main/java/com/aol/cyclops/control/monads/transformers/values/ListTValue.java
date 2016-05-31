package com.aol.cyclops.control.monads.transformers.values;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMValue;
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
public class ListTValue<T> implements ListT<T>,
                                      TransformerSeq<T>,
                                      Publisher<T>{
   
   final AnyMValue<ListX<T>> run;

   private ListTValue(final AnyMValue<? extends List<T>> run){
       this.run = run.map(s->ListX.fromIterable(s));
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
   public <B> ListTValue<B> flatMapT(Function<? super T,ListTValue<B>> f){
     
       return of( run.map(list-> 
                                   list.flatMap(a-> f.apply(a)
                                                     .run
                                                     .stream())
                                       .flatMap(a->a)
                                       
                            )
               );
   }
   
   
   
   
   
   
   
   
   public boolean isSeqPresent(){
       return !run.isEmpty();
   }
   /**
	 * @return The wrapped AnyM
	 */
   public AnyMValue<ListX<T>> unwrap(){
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
   public ListTValue<T> peek(Consumer<? super T> peek){
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
   public ListTValue<T> filter(Predicate<? super T> test){
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
   public <B> ListTValue<B> map(Function<? super T,? extends B> f){
       return of(run.map(o-> (List<B>)ReactiveSeq.fromList(o).map(f).toList()));
   }
   public <B> ListTValue<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f) {
       return new ListTValue<B>(run.map(o -> ListX.fromIterable(o).flatMap(f)));

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
   public static <U, R> Function<ListTValue<U>, ListTValue<R>> lift(Function<? super U, ? extends R> fn) {
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
	public static <U1, U2, R> BiFunction<ListTValue<U1>, ListTValue<U2>, ListTValue<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an ListT from an AnyM that contains a monad type that contains type other than List
	 * The values in the underlying monad will be mapped to List<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an List
	 * @return ListT
	 */
   public static <A> ListTValue<A> fromAnyM(AnyMValue<A> anyM){
	   return of(anyM.map(Arrays::asList));
   }
   /**
	 * Construct an ListT from an AnyM that wraps a monad containing  Lists
	 * 
	 * @param monads AnyM that contains a monad wrapping an List
	 * @return ListT
	 */
   public static <A> ListTValue<A> of(AnyMValue<? extends List<A>> monads){
	   return new ListTValue<>(monads);
   }
   public static <A> ListTValue<A> of(List<A> monads){
       return ListT.fromOptional(Optional.of(monads));
   }

	/**
	 * Create a ListT from an AnyM that wraps a monad containing a Stream
	 * 
	 * @param monads
	 * @return
	 */
	public static <A> ListTValue<A> fromStream(AnyMValue<? extends Stream<A>> monads) {
		return of(monads.map(s -> s.collect(Collectors.toList())));
	}
   
	 public static <A,V extends MonadicValue<? extends List<A>>> ListTValue<A> fromValue(V monadicValue){
	       return of(AnyM.ofValue(monadicValue));
	   }
   
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
	    return String.format("ListTValue[%s]", run );
	}
   
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        
        return stream().iterator();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    public <U> ListTValue<U> unitIterator(Iterator<U> u) {
        return of(run.unit(ListX.fromIterable(()->u)));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> ListTValue<T> unit(T unit) {
        return of(run.unit(ListX.of(unit)));
    }
    
    @Override
    public ReactiveSeq<T> stream() {
       return run.stream().flatMap(i->i.stream());
    }
    @Override
    public <R> ListTValue<R> empty() {
       return of(run.empty());
    }
    public static<T>  ListTValue<T> emptyOptional() {
        return ListT.fromOptional(Optional.empty());
    }
    public boolean isListPresent() {
      return !run.isEmpty();
    }
    
    public List<T> get(){
        return run.get();
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
    public <T>ListTValue<T> unitAnyM(AnyM<Traversable<T>> traversable) {
        
        return of((AnyMValue)traversable.map(t->ListX.fromIterable(t)));
    }
    @Override
    public AnyM<? extends Traversable<T>> transformerStream() {
        
        return run;
    }
    @Override
    public int hashCode(){
        return run.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof ListTValue){
            return run.equals( ((ListTValue)o).run);
        }
        return false;
    }
}