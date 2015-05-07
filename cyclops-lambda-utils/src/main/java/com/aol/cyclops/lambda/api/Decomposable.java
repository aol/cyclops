package com.aol.cyclops.lambda.api;

import java.util.stream.Collectors;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;

/**
 * trait / interface for Value Objects / Case Classes / Algebraic Data Types
 * 
 * unapply uses Reflection by default, but clients can override it if neccessary
 * 
 * @author johnmcclean
 *
 */
public interface Decomposable{
	
	
	/**
	 * @return Values of the fields of this Decomposable instance
	 */
	@SuppressWarnings("unchecked")
	default <T extends Iterable<? extends Object>> T unapply(){

		try {
			return (T)ReflectionCache.getField(this.getClass()).stream().map(f ->{
				try {
				
					return f.get(this);
				} catch (Exception e) {
					ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
					return null;
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			ExceptionSoftener.singleton.factory.getInstance()
					.throwSoftenedException(e);
			return (T)null;
		}
		
	}
	
	
}
