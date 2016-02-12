package com.aol.cyclops.types;

import java.util.Iterator;
import java.util.function.Function;

import org.jooq.lambda.Collectable;

import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.matcher2.Case;
import com.aol.cyclops.matcher2.Cases;
import com.aol.cyclops.matcher2.CheckValues;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.traits.ConvertableSequence;

public interface IterableFunctor<T> extends Iterable<T>,Functor<T>, Foldable<T>, Traversable<T>,
											ConvertableSequence<T>{

	
	<U> IterableFunctor<U> unitIterator(Iterator<U> U);
	<R> IterableFunctor<R>  map(Function<? super T,? extends R> fn);
	
	default  SequenceM<T> stream(){
		return SequenceM.fromIterable(this);
	}
	default  Collectable<T> collectable(){
		return stream().collectable();
	}
	
	
	
	
   
}
