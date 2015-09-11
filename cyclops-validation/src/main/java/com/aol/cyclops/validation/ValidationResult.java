package com.aol.cyclops.validation;

import java.util.Optional;


public interface ValidationResult<T,E> {
	
	public Optional<T> success();
	public Optional<E> failure();
	
}

