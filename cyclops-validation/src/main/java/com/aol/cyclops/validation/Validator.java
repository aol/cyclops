package com.aol.cyclops.validation;

import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import fj.data.Validation;
@AllArgsConstructor
public class Validator<T,R,E> {
	
	final Predicate<T> valid;
    final E error;
    final R result;
    
	public static <T,R,E> Validator<T,R,E> of(Predicate<T> valid, E error, R result){
		return new Validator<>(valid,error,result);
	}
	public static <T,R,E> Validator<T,R,E>  convert(Validation<E,R> validation){
		if(validation.isSuccess())
			return (Validator)of( t-> true,null,validation.success());
		
		return (Validator)of( t-> false,validation.fail(),null);
	}
	public boolean isValid(T input){
		return valid.test(input);
	}
	
	public CumulativeValidator<T,R,E> cumulative(){
		return new CumulativeValidator<>(this,null);
	}
	
}
