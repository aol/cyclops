package com.aol.cyclops.types;

import java.util.stream.Collectors;

import com.aol.cyclops.control.ExceptionSoftener;
import com.aol.cyclops.internal.invokedynamic.ReflectionCache;

/**
 * trait / interface for Value Objects / Case Classes / Algebraic Data Types
 * 
 * unapply uses Reflection by default, but clients can override it if neccessary
 * 
 * @author johnmcclean
 *
 */
public interface Decomposable{
	
	
	default  Object unwrap(){
		return this;
	}
	/**
	 * @return Values of the fields of this Decomposable instance
	 */
	@SuppressWarnings("unchecked")
	default <I extends Iterable<?>> I unapply(){
		
		if(unwrap() instanceof Iterable)
			return (I)unwrap();
		try {
			
			return (I)ReflectionCache.getFields(unwrap().getClass()).stream().map(f ->{
				try {
				
					return f.get(unwrap());
				} catch (Exception e) {
					throw ExceptionSoftener.throwSoftenedException(e);
					
				
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			throw ExceptionSoftener.throwSoftenedException(e);
			
		}
		
	}
	
	
}
