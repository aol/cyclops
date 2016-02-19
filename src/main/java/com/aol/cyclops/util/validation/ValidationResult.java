package com.aol.cyclops.util.validation;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Xor;

import lombok.AllArgsConstructor;
import lombok.ToString;


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
	public Maybe<T> success();
	/**
	 *  @return Optional : empty if successful, populated with the validation error if failed
	 */
	public Maybe<E> failure();
	
	default Xor<E,T> toXor(){
		
		
		return success().isPresent() ? Xor.primary(success().get()) :  Xor.secondary(failure().get());
	}
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
	public static class SuccessfulResult<T,E> implements ValidationResult<T,E>{
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
		public Maybe<T> success() {
			
			return Maybe.of(result);
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.validation.ValidationResult#failure()
		 */
		@Override
		public Maybe<E> failure() {
			return Maybe.none();
		}
		
	}
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
		public Maybe<T> success() {
			return Maybe.none();
		}
		/* (non-Javadoc)
		 * @see com.aol.cyclops.validation.ValidationResult#failure()
		 */
		@Override
		public Maybe<E> failure() {
			return Maybe.of(error);
		}
	}
	
}

