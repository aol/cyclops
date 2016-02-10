package com.aol.cyclops.validation;

import java.util.Optional;

import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Xor;


/**
 * Interface that represents a Validation Result
 * 
 * @author johnmcclean
 *
 * @param <T> Successful Result Type
 * @param <E> Error type
 */
public interface ValidationResult<T,E>  {
	
	/**
	 * @return Optional : empty if failed, populated with the validation result if successful
	 */
	public Optional<T> success();
	/**
	 *  @return Optional : empty if successful, populated with the validation error if failed
	 */
	public Optional<E> failure();
	
	default Xor<E,T> toXor(){
		
		
		return success().isPresent() ? Xor.primary(success().get()) :  Xor.secondary(failure().get());
	}
	
}

