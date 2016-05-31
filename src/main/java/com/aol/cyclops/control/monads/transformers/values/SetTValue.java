package com.aol.cyclops.control.monads.transformers.values;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;



/**
 * Monad Transformer for Java Sets
 * 
 * SetT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Set
 * 
 * SetT<AnyM<*SOME_MONAD_TYPE*<Set<T>>>>
 * 
 * SetT allows the deeply wrapped Set to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public class SetTValue<T>  implements SetT<T>,
                                    ConvertableSequence<T>,
                                    TransformerSeq<T>,
                                    Publisher<T>{
   
   final AnyMValue<SetX<T>> run;

   private SetTValue(final AnyMValue<? extends Set<T>> run){
       this.run = run.map(s->SetX.fromIterable(s));
   }
   public boolean isSeqPresent(){
       return !run.isEmpty();
   }
   /**
	 * @return The wrapped AnyM
	 */
   public AnyMValue<SetX<T>> unwrap(){
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
   public SetTValue<T> peek(Consumer<? super T> peek){
	   return map(a-> {peek.accept(a); return a;});
     
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
   public SetTValue<T> filter(Predicate<? super T> test){
       return of(run.map(stream-> ReactiveSeq.fromIterable(stream).filter(test).toSet()));
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
   public <B> SetTValue<B> map(Function<? super T,? extends B> f){
       return of(run.map(o-> (Set<B>)ReactiveSeq.fromIterable(o).map(f).toSet()));
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
   public <B> SetTValue<B> flatMapT(Function<? super T,SetTValue<B>> f){
	  
	   return of( run.map(stream-> ReactiveSeq.fromIterable(stream).flatMap(a-> f.apply(a).run.stream()).flatMap(a->a.stream())
			   .toSet()));
   }
   public <B> SetTValue<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f) {
       return new SetTValue<B>(run.map(o -> SetX.fromIterable(o).flatMap(f)));

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
   public static <U, R> Function<SetTValue<U>, SetTValue<R>> lift(Function<? super U,? extends R> fn) {
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
	public static <U1, U2, R> BiFunction<SetTValue<U1>, SetTValue<U2>, SetTValue<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an SetT from an AnyM that contains a monad type that contains type other than Set
	 * The values in the underlying monad will be mapped to Set<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Set
	 * @return SetT
	 */
   public static <A> SetTValue<A> fromAnyM(AnyMValue<A> anyM){
	   return of(anyM.map(SetTValue::asSet));
   }
   
   private static <T> Set<T> asSet(T... elements){
	   	return new HashSet<T>(Arrays.asList(elements));
   }
   /**
	 * Construct an SetT from an AnyM that wraps a monad containing  Sets
	 * 
	 * @param monads AnyM that contains a monad wrapping an Set
	 * @return SetT
	 */
   public static <A> SetTValue<A> of(AnyMValue<? extends Set<A>> monads){
	   return new SetTValue<>(monads);
   }
   public static <A> SetTValue<A> of(Set<A> monads){
       return SetT.fromOptional(Optional.of(monads));
   }
   public boolean isSetPresent() {
       return !run.isEmpty();
   }
   public Set<T> get(){
       return run.get();
   }
	/**
	 * Create a SetT from an AnyM that wraps a monad containing a Stream
	 * 
	 * @param monads
	 * @return
	 */
	public static <A> SetTValue<A> fromStream(AnyMValue<Stream<A>> monads) {
		return of(monads.map(s -> s.collect(Collectors.toSet())));
	}
   
	public static <A,V extends MonadicValue<? extends Set<A>>> SetTValue<A> fromValue(V monadicValue){
	       return of(AnyM.ofValue(monadicValue));
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
    public <U> SetTValue<U> unitIterator(Iterator<U> u) {
        return of(run.unit(SetX.fromIterable(()->u)));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> SetTValue<T> unit(T unit) {
        return of(run.unit(SetX.of(unit)));
    }
    
    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMap(i->i.stream());    
    }
    
    @Override
    public <R> SetTValue<R> empty() {
       return of(run.empty());
    }
    
    public static<T>  SetTValue<T> emptyOptional() {
        return SetT.fromOptional(Optional.empty());
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
    public <T> SetTValue<T> unitAnyM(AnyM<Traversable<T>> traversable) {
        
        return of((AnyMValue)traversable.map(t->SetX.fromIterable(t)));
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
        if(o instanceof SetTValue){
            return run.equals( ((SetTValue)o).run);
        }
        return false;
    }
}