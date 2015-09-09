package com.aol.cyclops.matcher.builders;

import com.aol.cyclops.matcher.Extractor;

public interface ExtractionStep<T,R,X> {

	/**
	 * Select the elements from a Collection to be extracted and passed to an Action
	 * 
	 * @param extractor Extractor to extract elements / transform a collection
	 * @return Next step in the Case Builder process
	 */
	public <T,R> CollectionStep<R,X> thenExtract(Extractor<T,R> extractor);
	
	
}
