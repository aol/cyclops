package com.aol.simple.react;

import java.util.Arrays;
import java.util.stream.Stream;

public class JDKLazyStreamTest extends BaseJDKStreamTest{

	<U> Stream<U> of(U... array){
	
		return ReactStream.lazy().reactToCollection(Arrays.asList(array));
	}
	
		
}
