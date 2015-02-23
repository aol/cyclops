package com.aol.simple.react.lazy;

import java.util.Arrays;
import java.util.stream.Stream;

import com.aol.simple.react.base.BaseJDKStreamTest;
import com.aol.simple.react.stream.lazy.LazyFutureStream;

public class JDKLazyStreamTest extends BaseJDKStreamTest{

	public <U> Stream<U> of(U... array){
	 
		return LazyFutureStream.parallelBuilder().reactToCollection(Arrays.asList(array));
	}
	
		
}
