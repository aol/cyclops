package com.aol.cyclops.types.stream;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.IterableFilterable;

public interface JoolManipulation<T> extends IterableFilterable<T>, Seq<T>{
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.IterableFilterable#removeAll(java.util.stream.Stream)
	 */
	default ReactiveSeq<T> removeAll(Stream<T> stream){
		return (ReactiveSeq<T>)(IterableFilterable.super.removeAll(stream));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.IterableFilterable#removeAll(java.lang.Iterable)
	 */
	default  ReactiveSeq<T> removeAll(Iterable<T> it){
		return (ReactiveSeq<T>)(IterableFilterable.super.removeAll(it));
	}
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#removeAll(org.jooq.lambda.Seq)
	 */
	default  ReactiveSeq<T> removeAll(Seq<T> seq){
		return (ReactiveSeq<T>)(IterableFilterable.super.removeAll((Stream)seq));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.IterableFilterable#removeAll(java.lang.Object[])
	 */
	default  ReactiveSeq<T> removeAll(T... values){
		return (ReactiveSeq<T>)(IterableFilterable.super.removeAll(values));
		
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.IterableFilterable#retainAll(java.lang.Iterable)
	 */
	default  ReactiveSeq<T> retainAll(Iterable<T> it){
	  
		return (ReactiveSeq<T>)(IterableFilterable.super.retainAll(it));
	}
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#retainAll(org.jooq.lambda.Seq)
	 */
	default  ReactiveSeq<T> retainAll(Seq<T> seq){
		return (ReactiveSeq<T>)(IterableFilterable.super.retainAll((Stream)seq));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.IterableFilterable#retainAll(java.util.stream.Stream)
	 */
	default  ReactiveSeq<T> retainAll(Stream<T> stream){
		return (ReactiveSeq<T>)(IterableFilterable.super.retainAll(stream));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.IterableFilterable#retainAll(java.lang.Object[])
	 */
	default  ReactiveSeq<T> retainAll(T... values){
		return (ReactiveSeq<T>)(IterableFilterable.super.retainAll(values));
	}

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
	 */
	@Override
	default ReactiveSeq<T> filterNot(Predicate<? super T> fn) {
		
		return (ReactiveSeq<T>)IterableFilterable.super.filterNot(fn);
	}

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Filterable#notNull()
	 */
	@Override
	default ReactiveSeq<T> notNull() {
		
		return (ReactiveSeq<T>)IterableFilterable.super.notNull();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
	 */
	@Override
	default <U> ReactiveSeq<U> ofType(Class<U> type) {
		
		return (ReactiveSeq<U>)IterableFilterable.super.ofType(type);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
	 */
	@Override
	ReactiveSeq<T> filter(Predicate<? super T> fn) ;
	
	
}
