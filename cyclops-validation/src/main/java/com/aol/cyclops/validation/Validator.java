package com.aol.cyclops.validation;

import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import fj.data.Validation;
@AllArgsConstructor
public class Validator<T,R,E> {
	
	final Predicate<T> valid;
    final E error;
    final R result;
    
	public static <T,R,E> Validator<T,R,E> isValid(Predicate<T> valid, E error, R result){
		return new Validator<>(valid,error,result);
	}
	public static <T,R,E> Validator<T,R,E>  convert(Validation<E,T> validation){
		if(validation.isSuccess())
			return (Validator)isValid( t-> true,null,validation.success());
		
		return (Validator)isValid( t-> false,validation.fail(),null);
	}
	public boolean isValid(T input){
		return valid.test(input);
	}
	
	public CumulativeValidator<T,R,E> cumulative(){
		return new CumulativeValidator<>(this,null);
	}
	public SequentialValidator<T,R,E> sequential(){
		return new SequentialValidator<>(this,null);
	}
	
}
