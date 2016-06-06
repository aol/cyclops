package com.aol.cyclops.internal.monads;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 
 * Wrapper for Any Monad type
 * @see AnyMonads companion class for static helper methods
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@AllArgsConstructor(access=AccessLevel.PROTECTED)
public abstract class BaseAnyMImpl<T> {
	
	protected final Monad<T> monad;
	protected final Class initialType;
	
	public <R> R unwrap(){
	    return (R)monad.unwrap();
	//	return (R)new ComprehenderSelector().selectComprehender(initialType).unwrap(monad.unwrap());
	}
	protected <R> AnyM<R> fromIterable(Iterable<R> it){
        if(it instanceof AnyM)
            return (AnyM<R>)it;
        return AnyM.fromIterable(it);
    }
	protected <R> AnyM<R> fromPublisher(Publisher<R> it){
        if(it instanceof AnyM)
            return (AnyM<R>)it;
        return AnyM.fromPublisher(it);
    }
	
	

	public Monad monad(){
		return (Monad)monad;
	}
	
	protected   Monad<T>  filterInternal(Predicate<? super T> fn){
		return monad.filter(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	protected  <R> Monad<R> mapInternal(Function<? super T,? extends R> fn){
		return monad.map(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	protected   Monad<T>  peekInternal(Consumer<? super T> c) {
		return monad.peek(c);
	}
	
	
	/**
	 * Perform a looser typed flatMap / bind operation
	 * The return type can be another type other than the host type
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	*/
	protected <R> Monad<R> bindInternal(Function<? super T,?> fn){
		return monad.<R>bind(fn);
	
	} 
	protected <R> Monad<R> flatMapInternal(Function<? super T,? extends AnyM<? extends R>> fn) {
        try{
            return monad.bind(in -> fn.apply(in).unwrap()).map(this::takeFirst);
        }catch(GotoAsEmpty e){
            return (Monad)monad.empty();
        }
    }
	
	
	private static class GotoAsEmpty extends RuntimeException{

		@Override
		public synchronized Throwable fillInStackTrace() {
			return null;
		}
		
	}
	private <T> T takeFirst(Object o){
		if(o instanceof MaterializedList){
			if(((List)o).size()==0)
				throw new GotoAsEmpty();
			return (T)((List)o).get(0);
		}
		return (T)o;
	}
	

	
	/**
	 * join / flatten one level of a nested hierarchy
	 * 
	 * @return Flattened / joined one level
	 */
	protected <T1> Monad<T1> flattenInternal(){
		return monad.flatten();
		
	}
	
	 abstract Xor<AnyMValue<T>,AnyMSeq<T>> matchable();
	
	/**
	 * Aggregate the contents of this Monad and the supplied Monad 
	 * 
	 * <pre>{@code 
	 * 
	 * List<Integer> result = anyM(Stream.of(1,2,3,4))
	 * 							.aggregate(anyM(Optional.of(5)))
	 * 							.asSequence()
	 * 							.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
		}</pre>
	 * 
	 * @param next Monad to aggregate content with
	 * @return Aggregated Monad
	 */
	protected  AnyM<List<T>> aggregate(AnyM<T> next){
	    
	    return unit(Stream.concat(matchable().visit(value->value.toSequence(), seq->seq.stream()), 
	                                next.matchable().visit(value->value.toSequence(), seq->seq.stream())).collect(Collectors.toList()));
		
		
	}
	
	
	public void forEach(Consumer<? super T> action) {
		asSequence().forEach(action);	
	}
	
	
	

	
	
	/**
	 * Sequence the contents of a Monad.  e.g.
	 * Turn an <pre>
	 * 	{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
	 * 
	 * <pre>{@code
	 * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Integer>toSequence(c->c.stream())
											.collect(Collectors.toList());
		
		
		assertThat(list,hasItems(1,2,3,4,5,6));
		
	 * 
	 * }</pre>
	 * 
	 * @return A Sequence that wraps a Stream
	 */
	public <NT> ReactiveSeq<NT> toSequence(Function<? super T,? extends Stream<? extends NT>> fn){
		return monad.flatMapToStream((Function)fn)
					.sequence();
	}
	/**
	 *  <pre>{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
	 * Less type safe equivalent, but may be more accessible than toSequence(fn) i.e. 
	 * <pre>
	 * {@code 
	 *    toSequence(Function<T,Stream<NT>> fn)
	 *   }
	 *   </pre>
	 *  <pre>{@code
	 * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Integer>toSequence()
											.collect(Collectors.toList());
		
		
		
	 * 
	 * }</pre>
	
	 * @return A Sequence that wraps a Stream
	 */
	public <T> ReactiveSeq<T> toSequence(){
		return monad.streamedMonad().sequence();
	}
	
	
	/**
	 * Wrap this Monad's contents as a Sequence without disaggreating it. .e.
	 *  <pre>{@code Optional<List<Integer>>  into Stream<List<Integer>> }</pre>
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	public ReactiveSeq<T> asSequence(){
		return monad.sequence();
		
	}
	
	
	//	filterM((a: Int) => List(a > 2, a % 2 == 0), List(1, 2, 3), ListMonad),
	//List(List(3), Nil, List(2, 3), List(2), List(3),
	//	  Nil, List(2, 3), List(2))												
	
	public abstract <T> AnyM<T> unit(T value);
	public abstract <T> AnyM<T> empty();
	
	
	
	
	
	public   AnyMValue<T> reduceMValue(Monoid<AnyMValue<T>> reducer){
	    //  List(2, 8, 3, 1).foldLeftM(0) {binSmalls} -> Optional(14)
	    //  convert to list Optionals
	        return monad.reduceM(Monoid.of(reducer.zero().unwrap(), (a,b)-> reducer.combiner().apply(AnyM.ofValue(a), 
	                                     AnyM.ofValue(b)))).anyMValue();    
	    }
	
	
	public   AnyMSeq<T> reduceMSeq(Monoid<AnyMSeq<T>> reducer){
	//	List(2, 8, 3, 1).foldLeftM(0) {binSmalls} -> Optional(14)
	//	convert to list Optionals
	    return	 monad.reduceM(Monoid.of(reducer.zero().unwrap(), (a,b)-> reducer.combiner().apply(AnyM.ofSeq(a), 
                                 AnyM.ofSeq(b)))).anyMSeq()  ;		
	}
	
	public ReactiveSeq<T> stream(){
	//	if(this.monad.unwrap() instanceof Stream){
			return asSequence();
	//	}
		//return this.<T>toSequence();
	}
	
	
	@Override
    public String toString() {
        return String.format("AnyM(%s)", monad );
    }
	


	public T get() {
		return monad.get();
	}



	
	
}