package com.aol.simple.react;

import java.util.Arrays;
import java.util.stream.Stream;

import com.aol.simple.react.stream.LazyFutureStream;

public class JDKLazyStreamTest extends BaseJDKStreamTest{

	<U> Stream<U> of(U... array){
	 
		return LazyFutureStream.parallelBuilder().reactToCollection(Arrays.asList(array));
	}
	
		
}
