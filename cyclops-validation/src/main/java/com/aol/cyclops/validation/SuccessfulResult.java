package com.aol.cyclops.validation;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class SuccessfulResult<T,E> implements ValidationResult<T,E>{
	T result;
	public static <T,E> ValidationResult<T,E> success(T result){
		return new SuccessfulResult(result);
	}
	@Override
	public Optional<T> success() {
		
		return Optional.of(result);
	}
	@Override
	public Optional<E> failure() {
		return Optional.empty();
	}
	
}
