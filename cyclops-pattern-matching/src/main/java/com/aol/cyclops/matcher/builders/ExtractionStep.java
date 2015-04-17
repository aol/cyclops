package com.aol.cyclops.matcher.builders;

import com.aol.cyclops.matcher.TypeSafePatternMatcher;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;

public interface ExtractionStep<T,R,X> {

	public Step<R,X> thenExtract(Extractor<T,R> extractor);
	
	
}
