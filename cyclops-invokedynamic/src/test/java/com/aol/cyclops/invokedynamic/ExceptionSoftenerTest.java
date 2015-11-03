package com.aol.cyclops.invokedynamic;

import java.io.IOException;

import org.junit.Test;

public class ExceptionSoftenerTest {

	@Test(expected=IOException.class)
	public void checked() {
		throw ExceptionSoftener.throwSoftenedException(new IOException("hello"));
	}
	@Test(expected=Exception.class)
	public void checkedException() {
		throw ExceptionSoftener.throwSoftenedException(new Exception("hello"));
	}
	@Test(expected=RuntimeException.class)
	public void rumtime() {
		throw ExceptionSoftener.throwSoftenedException(new RuntimeException("hello"));
	}

}
