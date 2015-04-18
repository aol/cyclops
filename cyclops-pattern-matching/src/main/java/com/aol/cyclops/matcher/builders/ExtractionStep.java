package com.aol.cyclops.matcher.builders;

import com.aol.cyclops.matcher.PatternMatcher.Extractor;

public interface ExtractionStep<T,R,X> {

	public <T,R> Step<R,X> thenExtract(Extractor<T,R> extractor);
	
	
}
