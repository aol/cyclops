package com.aol.cyclops.types.stream;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.util.function.Memoize;

import lombok.AllArgsConstructor;

/**
 * A class that represents a lazily constructed Head and Tail from a Stream
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@AllArgsConstructor
public class HeadAndTail<T> {
	private final Supplier<T> head;
	private final Supplier<SequenceM<T>> tail;
	private final Supplier<Boolean> isHead;
	
	
	public HeadAndTail(Iterator<T> it){
		isHead = Memoize.memoizeSupplier(()-> it.hasNext());
		head = Memoize.memoizeSupplier(()-> {
				if(isHead.get())
					return it.next();
				throw new NoSuchElementException();
		});
		tail = Memoize.memoizeSupplier(()->{
				if(isHead.get())
					head.get();
				else
					return SequenceM.empty();
				return SequenceM.fromIterator(it);
		});
		
	}
	
	/**
	 * @return true if the head is present
	 */
	public boolean isHeadPresent(){
		return isHead.get();
	}
	public T head() {
		return head.get();
	}
	/**
	 * @return Optional.empty if the head is not present, otherwise an Optional containing the head
	 */
	public Optional<T> headOptional(){
		return isHeadPresent()?   Optional.of(head()) : Optional.empty();

	}
	public Maybe<T> headMaybe(){
		return isHeadPresent()? Maybe.fromEvalOf(Eval.later(head)) : Maybe.none() ;

	}
	/**
	 * @return A Stream containing the Head if present
	 */
	public SequenceM<T> headStream(){
		return isHeadPresent()?    SequenceM.of(head).map(Supplier::get) : SequenceM.empty();
	}

	/**
	 * @return The tail
	 */
	public SequenceM<T> tail() {
		return tail.get();
	}
	
	
	
}