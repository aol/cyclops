package com.aol.cyclops.functions.fluent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

public class FunctionsTest {

	@Before
	public void setup(){
		this.times =0;
	}
	int called;
	public int addOne(int i ){
		called++;
		return i+1;
	}
	@Test
	public void testApply() {
		
		assertThat(FluentFunctions.of(this::addOne)
						.name("myFunction")
						.println()
						.apply(10),equalTo(11));
		
	}
	@Test
	public void testCache() {
		called=0;
		Function<Integer,Integer> fn = FluentFunctions.of(this::addOne)
													  .name("myFunction")
													  .memoize();
		
		fn.apply(10);
		fn.apply(10);
		fn.apply(10);
		
		assertThat(called,equalTo(1));
		
		
	}
	int set;
	public boolean events(Integer i){
		return set==i;
	}
	@Test
	public void testBefore(){
		set = 0;
		assertTrue(FluentFunctions.of(this::events)
					   .before(i->set=i)
					   .println()
					   .apply(10));
	}
	
	int in;
	boolean out;
	@Test
	public void testAfter(){
		set = 0;
		assertFalse(FluentFunctions.of(this::events)
					   .after((in,out)->set=in)
					   .println()
					   .apply(10));
		
		boolean result = FluentFunctions.of(this::events)
										.after((in2,out2)->{ in=in2; out=out2; } )
										.println()
										.apply(10);
		
		assertThat(in,equalTo(10));
		assertFalse(out);
	}
	@Test
	public void testAround(){
		set = 0;
		assertThat(FluentFunctions.of(this::addOne)
					   .around(advice->advice.proceed(advice.param+1))
					   .println()
					   .apply(10),equalTo(12));
		
		
	}
	
	int times =0;
	public String exceptionalFirstTime(String input) throws IOException{
		if(times==0){
			times++;
			throw new IOException();
		}
		return input + " world"; 
	}
	
	@Test
	public void retry(){
		assertThat(FluentFunctions.ofChecked(this::exceptionalFirstTime)
					   .println()
					   .retry(2,500)
					   .apply("hello"),equalTo("hello world"));
	}
	
	@Test
	public void recover(){
		FluentFunctions.ofChecked(this::exceptionalFirstTime)
						.recover(IOException.class, in->in+"boo!")
						.println()
						.apply("hello ");
	}

}
