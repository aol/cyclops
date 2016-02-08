package com.aol.cyclops.validation;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Store a Successful Validation Result
 * 
 * @author johnmcclean
 *
 * @param <T> Result type
 * @param <E> Error type
 */
@AllArgsConstructor
@ToString
public class SuccessfulResult<T,E> implements ValidationResult<T,E>{
	private final T result;
	/**
	 * Create a successful result instance with specified result
	 * 
	 * @param result : Successful Result
	 * @return New instance
	 */
	public static <T,E> ValidationResult<T,E> success(T result){
		return new SuccessfulResult<>(result);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.validation.ValidationResult#success()
	 */
	@Override
	public Optional<T> success() {
		
		return Optional.of(result);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.validation.ValidationResult#failure()
	 */
	@Override
	public Optional<E> failure() {
		return Optional.empty();
	}
	
}
