package com.aol.cyclops.validation;

import java.util.Optional;


/**
 * Interface that represents a Validation Result
 * 
 * @author johnmcclean
 *
 * @param <T> Successful Result Type
 * @param <E> Error type
 */
public interface ValidationResult<T,E> {
	
	/**
	 * @return Optional : empty if failed, populated with the validation result if successful
	 */
	public Optional<? super T> success();
	/**
	 *  @return Optional : empty if successful, populated with the validation error if failed
	 */
	public Optional<? super E> failure();
	
}

