package com.aol.cyclops.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import fj.data.Validation;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

@AllArgsConstructor
@Wither
public class CumulativeValidator<T,R,E> {

	private final Validator<T,R,E> validation;
	private final CumulativeValidator<T,R,E> next;

	public static <T,R,E> CumulativeValidator<T,R,E> validate(Predicate<T> valid, E error, R result){
		return new CumulativeValidator(Validator.isValid(valid, error, result),null);
	}
	public  CumulativeValidator<T,R,E> isValid(Predicate<T> valid, E error, R result){
		return new CumulativeValidator(Validator.isValid(valid, error, result),null);
	}
	public static <T,R,E> CumulativeValidator<T,R,E> isValid(Validation<E,T> validation){
		return new CumulativeValidator(Validator.convert(validation),null);
	}

	public ValidationResults accumulate(){
		return accumulate(null);
	}
	public ValidationResults accumulate(T input){
		List<ValidationResult> results = new ArrayList<>();
		final ValidationResult result;
		if(validation.isValid(input))
			result = SuccessfulResult.success(validation.result);
		else
			result = FailedResult.fail(validation.error);
		
		results.add(result);
		if(next!=null)
			results.addAll(next.accumulate(input).getResults());
		return new ValidationResults(results);
		
	}
	public ValidationResults accumulateUntilFail(T input){
		List<ValidationResult> results = new ArrayList<>();
		final ValidationResult result;
		if(validation.isValid(input))
			result = SuccessfulResult.success(validation.result);
		else{
			result = FailedResult.fail(validation.error);
			results.add(result);
			return  new ValidationResults(results);
		}
		
		results.add(result);
		if(next!=null)
			results.addAll(next.accumulate(input).getResults());
		return new ValidationResults(results);
		
	}
	public CumulativeValidator<T,R,E> add(Validation<E,T> validation){
		if(next==null)
           		return this.withNext(new CumulativeValidator<T,R,E>(Validator.convert(validation),null));
		else
			return next.add(Validator.convert(validation));
	}
	
	public CumulativeValidator<T,R,E> add(Validator<T,R,E> validation){
		if(next==null)
           		return this.withNext(new CumulativeValidator<T,R,E>(validation,null));
		else
			return next.add(validation);
	}
}

