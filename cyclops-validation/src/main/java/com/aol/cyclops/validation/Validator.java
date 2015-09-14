package com.aol.cyclops.validation;

import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import fj.data.Validation;
/**
 * Class that represents a Lazy Validation
 * 
 * @author johnmcclean
 *
 * @param <T>
 * @param <R>
 * @param <E>
 */
@AllArgsConstructor
public class Validator<T,R,E> {
	
	private final Predicate<T> valid;
	final E error;
	final R result;
    
	/**
	 * Construct a Validator
	 * <pre>
	 * {@code 
	 * 	ValidationResults<String,String> results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
												.add(Validator.of((User user)->user.email!=null, "user email null","email ok"))
												.accumulate(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(2));
	 * 
	 * }</pre>
	 * 
	 * @param valid Validation Predicate to check
	 * @param error Error to return if validation predicate fails
	 * @param result Result to return if validation predicate succeeds
	 * @return Validator
	 */
	public static <T,R,E> Validator<T,R,E> of(Predicate<T> valid, E error, R result){
		return new Validator<>(valid,error,result);
	}
	/**
	 * Convert a FunctionalJava Validation result to a Cyclops Validator instance
	 * 
	 * @param validation FunctionalJava Validation result to convert
	 * @return Cyclops Validator
	 */
	public static <T,R,E> Validator<T,R,E>  convert(Validation<E,R> validation){
		if(validation.isSuccess())
			return (Validator)of( t-> true,null,validation.success());
		
		return (Validator)of( t-> false,validation.fail(),null);
	}
	/**
	 * @param input to test validation against
	 * @return true if valid
	 */
	public boolean isValid(T input){
		return valid.test(input);
	}
	
	/**
	 * @return A CumulativeValidator containing this Validator
	 */
	public CumulativeValidator<T,R,E> cumulative(){
		return new CumulativeValidator<>(this,null);
	}
	
}
