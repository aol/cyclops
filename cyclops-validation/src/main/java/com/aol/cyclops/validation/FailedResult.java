package com.aol.cyclops.validation;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class FailedResult<T,E> implements ValidationResult<T,E>{
	E error;
	public static <T,E> ValidationResult<T,E> fail(E error){
		return new FailedResult(error);
	}
	@Override
	public Optional<T> success() {
		return Optional.empty();
	}
	@Override
	public Optional<E> failure() {
		return Optional.of(error);
	}
}
