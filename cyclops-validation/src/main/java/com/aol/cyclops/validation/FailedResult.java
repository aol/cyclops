package com.aol.cyclops.validation;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Store a failed validation result
 * 
 * @author johnmcclean
 *
 * @param <T> Input type
 * @param <E> Error Type
 */
@AllArgsConstructor
@ToString
public class FailedResult<T,E> implements ValidationResult<T,E>{
	
	private final E error;
	
	/**
	 * Construct a FailedResult
	 * <pre>
	 * {@code
	 *  ValidationResult  result = FailedResult.fail(validation.error);
	 * }</pre>
	 * @param error
	 * @return
	 */
	public static <T,E> ValidationResult<T,E> fail(E error){
		return new FailedResult<>(error);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.validation.ValidationResult#success()
	 */
	@Override
	public Optional<T> success() {
		return Optional.empty();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.validation.ValidationResult#failure()
	 */
	@Override
	public Optional<E> failure() {
		return Optional.of(error);
	}
}
