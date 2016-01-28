package com.aol.cyclops.collections.extensions;

import java.util.Collection;

import org.jooq.lambda.Collectable;
import org.pcollections.PSet;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.SequenceMCollectable;

public interface SetX<T> extends PSet<T>, CollectionX<T>{

	default PSet<T> toPSet(){
		return this;
	}
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}
	@Override
	default Collectable<T> collectable(){
		return stream();
	}
	
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plus(java.lang.Object)
	 */
	@Override
	public SetX<T> plus(T e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plusAll(java.util.Collection)
	 */
	@Override
	public SetX<T> plusAll(Collection<? extends T> list) ;
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minus(java.lang.Object)
	 */
	@Override
	public SetX<T> minus(Object e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minusAll(java.util.Collection)
	 */
	@Override
	public SetX<T> minusAll(Collection<?> list);
	

}
