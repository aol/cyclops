package com.aol.cyclops.validation;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A class that represents Validation Results from multiple validation events
 * 
 * @author johnmcclean
 *
 * @param <T>  Successful result type
 * @param <E>  Error type
 */
@Getter
@AllArgsConstructor
public class ValidationResults<T,E> {
	private final List<ValidationResult<T,E>> results;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return results.toString();
	}
	
}
