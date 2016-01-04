package com.aol.cyclops.invokedynamic;

import java.io.IOException;
import java.util.function.Supplier;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import static org.junit.Assert.*;
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
	
	private String get() throws IOException{
		return "hello";
	}
	@Test
	public void methodReference(){
		Supplier<String> supplier = ExceptionSoftener.softenSupplier(this::get);
		
		assertThat(supplier.get(),equalTo("hello"));
	}
	
	@Test
	public void softenCallable(){
		Supplier<String> supplier = ExceptionSoftener.softenCallable(this::get);
		
		assertThat(supplier.get(),equalTo("hello"));
	}
	


}
