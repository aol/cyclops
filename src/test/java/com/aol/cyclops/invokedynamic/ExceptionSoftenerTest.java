package com.aol.cyclops.invokedynamic;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.function.Supplier;

import org.junit.Test;

import com.aol.cyclops.util.ExceptionSoftener;
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
	
	@Test(expected=IOException.class)
	public void testThrowif(){
		ExceptionSoftener.throwIf(new IOException("hello"), e-> e instanceof IOException);
	}
	@Test
	public void testThrowifFalse(){
		ExceptionSoftener.throwIf(new IOException("hello"), e-> e.getMessage()=="world");
	}
	boolean value = false;
	@Test
	public void testThrowOrHandle(){
		value = false;
		try{
			ExceptionSoftener.throwOrHandle(new IOException("hello"), e-> e instanceof IOException,c->this.value=true);
			fail("should not reach");
		}catch(Exception e){
			assertFalse(value);
		}
	}
	@Test
	public void testThrowifHandle(){
		value = false;
		try{
			ExceptionSoftener.throwOrHandle(new IOException("hello"), e-> e.getMessage()=="world",c->this.value=true);
			
		}catch(Exception e){
			assertTrue(value);
		}
		
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
