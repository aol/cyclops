package com.aol.cyclops.functions.fluent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
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
		assertTrue(out==result);
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
		assertThat(FluentFunctions.ofChecked(this::exceptionalFirstTime)
						.recover(IOException.class, in->in+"boo!")
						.println()
						.apply("hello "),equalTo("hello boo!"));
	}
	@Test(expected=IOException.class)
	public void recoverDont(){
		assertThat(FluentFunctions.ofChecked(this::exceptionalFirstTime)
						.recover(RuntimeException.class, in->in+"boo!")
						.println()
						.apply("hello "),equalTo("hello boo!"));
	}
	
	public String gen(String input){
		return input+System.currentTimeMillis();
	}
	@Test
	public void generate(){
		assertThat(FluentFunctions.of(this::gen)
						.println()
						.generate("next element")
						.onePer(1, TimeUnit.SECONDS)
						.limit(2)
						.toList().size(),equalTo(2));
	}
	
	@Test
	public void iterate(){
		assertThat(FluentFunctions.of(this::addOne)	
						.iterate(1,i->i)
						.limit(2)
						.toList().size(),equalTo(2));
	}

	@Test
	public void testMatches1(){
		assertThat(FluentFunctions.of(this::addOne)	
					   .matches(-1,c->c.hasValues(2).then(i->3))
					   .apply(1),equalTo(3));
	}

	@Test
	public void testMatches1Default(){
		assertThat(FluentFunctions.of(this::addOne)	
					   .matches(-1,c->c.hasValues(4).then(i->3))
					   .apply(1),equalTo(-1));
	}
	@Test
	public void testMatches2(){
		assertThat(FluentFunctions.of(this::addOne)	
					   .matches(-1,c->c.hasValues(4).then(i->30),
							   			c->c.hasValues(2).then(i->3))
					   .apply(1),equalTo(3));
	}

	@Test
	public void testMatches2Default(){
		assertThat(FluentFunctions.of(this::addOne)	
					   .matches(-1,c->c.hasValues(4).then(i->3),
							   		c->c.hasValues(103).then(i->8))
					   .apply(1),equalTo(-1));
	}
	@Test
	public void testMatches3(){
		assertThat(FluentFunctions.of(this::addOne)	
				   .matches(-1,c->c.hasValues(4).then(i->30),
						   			c->c.hasValues(8).then(i->32),
						   			c->c.hasValues(2).then(i->3))
				   .apply(1),equalTo(3));
	}

	@Test
	public void testMatches3Default(){
		assertThat(FluentFunctions.of(this::addOne)	
					   .matches(-1,c->c.hasValues(4).then(i->3),
							   		c->c.hasValues(8).then(i->32),
							   		c->c.hasValues(103).then(i->8))
					   .apply(1),equalTo(-1));
	}
	@Test
	public void testMatches4(){
		assertThat(FluentFunctions.of(this::addOne)	
				   .matches(-1,c->c.hasValues(4).then(i->30),
						   		c->c.hasValues(40).then(i->38),
						   			c->c.hasValues(8).then(i->32),
						   			c->c.hasValues(2).then(i->3))
				   .apply(1),equalTo(3));
	}

	@Test
	public void testMatches4Default(){
		assertThat(FluentFunctions.of(this::addOne)	
					   .matches(-1,c->c.hasValues(4).then(i->3),
							   		c->c.hasValues(40).then(i->38),
							   		c->c.hasValues(8).then(i->32),
							   		c->c.hasValues(103).then(i->8))
					   .apply(1),equalTo(-1));
	}
	@Test
	public void testMatches5(){
		assertThat(FluentFunctions.of(this::addOne)	
				   .matches(-1,c->c.hasValues(4).then(i->30),
						   		c->c.hasValues(5).then(i->50),
						   		c->c.hasValues(40).then(i->38),
						   			c->c.hasValues(8).then(i->32),
						   			c->c.hasValues(2).then(i->3))
				   .apply(1),equalTo(3));
	}

	@Test
	public void testMatches5Default(){
		assertThat(FluentFunctions.of(this::addOne)	
					   .matches(-1,c->c.hasValues(4).then(i->3),
							   		c->c.hasValues(5).then(i->50),
							   		c->c.hasValues(40).then(i->38),
							   		c->c.hasValues(8).then(i->32),
							   		c->c.hasValues(103).then(i->8))
					   .apply(1),equalTo(-1));
	}
}
