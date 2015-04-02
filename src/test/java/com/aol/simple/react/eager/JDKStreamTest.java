package com.aol.simple.react.eager;

import java.util.stream.Stream;

import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.stream.traits.EagerFutureStream.*;
import com.aol.simple.react.base.BaseJDKStreamTest;

public class JDKStreamTest extends BaseJDKStreamTest{

	protected <U> Stream<U> of(U... array){	
		return EagerFutureStream.of(array);
	}
	
	
}
