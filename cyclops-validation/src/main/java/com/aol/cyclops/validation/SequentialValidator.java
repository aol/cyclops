package com.aol.cyclops.validation;

import java.util.function.Predicate;

import fj.data.Validation;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SequentialValidator<T, R, E> {

	private final Validator<T, R, E> validation;
	private final SequentialValidator<R, ?, E> seq;

	public  ValidationResult sequence(){
		return sequence(null);
	}
	public  ValidationResult sequence(T input) {

		final ValidationResult result;
		if (validation.isValid(input))
			result = SuccessfulResult.success(validation.result);
		else
			return FailedResult.fail(validation.error);

		if (seq != null)
			return seq.sequence(validation.result);
		return result;

	}
	public <NT> SequentialValidator<NT, T, E> add(Validation<E, NT> validation) {
		return new SequentialValidator<NT, T, E>(Validator.convert(validation), this);
	}
	

	// T -> R
	public <NT> SequentialValidator<NT, T, E> add(
			Validator<NT, T, E> validation) {

		return new SequentialValidator<NT, T, E>(validation, this);
	}

	public static <T, R, E> SequentialValidator<T, R, E> isValid(
			Predicate<T> valid, E error, R result) {
		return new SequentialValidator(
				Validator.isValid(valid, error, result), null);
	}
	public static <T, R, E> SequentialValidator<T, R, E> isValid(Validation<E,T> validation) {
		return new SequentialValidator(
				Validator.convert(validation), null);
	}

}
