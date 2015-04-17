package com.aol.cyclops.matcher.builders;

import com.aol.cyclops.matcher.TypeSafePatternMatcher;
import com.aol.cyclops.matcher.PatternMatcher.ActionWithReturn;
import com.aol.cyclops.matcher.PatternMatcher.Extractor;

public interface ExtractionStep<T,R,X> {

	public Step<ActionWithReturn<R,X>,TypeSafePatternMatcher<T,X>> thenExtract(Extractor<T,R> extractor);
	
	
}
