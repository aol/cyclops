package com.aol.cyclops.functions.fluent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.lambda.tuple.Tuple;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.functions.TriFunction;
import com.aol.cyclops.functions.fluent.FluentFunctions.FluentBiFunction;
import com.aol.cyclops.functions.fluent.FluentFunctions.FluentFunction;
import com.aol.cyclops.functions.fluent.FluentFunctions.FluentSupplier;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.trycatch.Try;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class FluentTriFunctionTest {

	@Before
	public void setup(){
		this.times =0;
	}
	int called;
	public int add(Integer a,Integer b,Integer c){
		called++;
		return a+b+c;
	}
	@Test
	public void testApply() {
		
		assertThat(FluentFunctions.of(this::add)
						.name("myFunction")
						.println()
						.apply(10,1,0),equalTo(11));
		
	}
	@Test
	public void testCache() {
		called=0;
		TriFunction<Integer,Integer,Integer,Integer> fn = FluentFunctions.of(this::add)
													  .name("myFunction")
													  .memoize();
		
		fn.apply(10,1,0);
		fn.apply(10,1,0);
		fn.apply(10,1,0);
		
		assertThat(called,equalTo(1));
		
		
	}
	@Test
	public void testCacheGuava() {
		Cache<Object, Integer> cache = CacheBuilder.newBuilder()
			       .maximumSize(1000)
			       .expireAfterWrite(10, TimeUnit.MINUTES)
			       .build();

		called=0;
		TriFunction<Integer,Integer,Integer,Integer> fn = FluentFunctions.of(this::add)
													  .name("myFunction")
													  .memoize((key,f)->cache.get(key,()->f.apply(key)));
		
		fn.apply(10,1,0);
		fn.apply(10,1,0);
		fn.apply(10,1,0);
		
		assertThat(called,equalTo(1));
		
		
	}
	int set;
	public boolean events(Integer i,Integer a, Integer c){
		return set==i;
	}
	@Test
	public void testBefore(){
		set = 0;
		assertTrue(FluentFunctions.of(this::events)
					   .before((a,b,c)->set=a)
					   .println()
					   .apply(10,1,100));
	}
	
	int in;
	boolean out;
	@Test
	public void testAfter(){
		set = 0;
		assertFalse(FluentFunctions.of(this::events)
					   .after((in1,in2,in3,out)->set=in1)
					   .println()
					   .apply(10,1,0));
		
		boolean result = FluentFunctions.of(this::events)
										.after((inA2,inB2,inB3,out2)->{ in=inA2; out=out2; } )
										.println()
										.apply(10,1,0);
		
		assertThat(in,equalTo(10));
		assertTrue(out==result);
	}
	@Test
	public void testAround(){
		set = 0;
		assertThat(FluentFunctions.of(this::add)
					   .around(advice->advice.proceed1(advice.param1+1))
					   .println()
					   .apply(10,1,0),equalTo(12));
		
		
	}
	
	int times =0;
	public String exceptionalFirstTime(String input,String input2,String input3) throws IOException{
		if(times==0){
			times++;
			throw new IOException();
		}
		return input + " world" + input2 + input3; 
	}
	
	@Test
	public void retry(){
		assertThat(FluentFunctions.ofChecked(this::exceptionalFirstTime)
					   .println()
					   .retry(2,500)
					   .apply("hello","woo!","h"),equalTo("hello worldwoo!h"));
	}
	
	@Test
	public void recover(){
		assertThat(FluentFunctions.ofChecked(this::exceptionalFirstTime)
						.recover(IOException.class, (in1,in2,in3)->in1+"boo!")
						.println()
						.apply("hello ","woo!","h"),equalTo("hello boo!"));
	}
	@Test(expected=IOException.class)
	public void recoverDont(){
		assertThat(FluentFunctions.ofChecked(this::exceptionalFirstTime)
						.recover(RuntimeException.class,(in1,in2,in3)->in1+"boo!")
						.println()
						.apply("hello ","woo!","h"),equalTo("hello boo!"));
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
		
		assertThat(FluentFunctions.of(this::add)	
						.iterate(1,2,3,(i)->Tuple.tuple(i,i,i))
						.limit(2)
						.toList().size(),equalTo(2));
	}

	@Test
	public void testMatches1(){
		assertThat(FluentFunctions.of(this::add)	
					   .matches(-1,c->c.hasValues(3).then(i->3))
					   .apply(1,1,1),equalTo(3));
	}

	@Test
	public void testMatches1Default(){
		assertThat(FluentFunctions.of(this::add)	
					   .matches(-1,c->c.hasValues(4).then(i->3))
					   .apply(1,1,1),equalTo(-1));
	}
	@Test
	public void testMatches2(){
		assertThat(FluentFunctions.of(this::add)	
					   .matches(-1,c->c.hasValues(4).then(i->30),
							   			c->c.hasValues(3).then(i->3))
					   .apply(1,1,1),equalTo(3));
	}

	@Test
	public void testMatches2Default(){
		assertThat(FluentFunctions.of(this::add)	
					   .matches(-1,c->c.hasValues(4).then(i->3),
							   		c->c.hasValues(103).then(i->8))
					   .apply(1,1,1),equalTo(-1));
	}
	@Test
	public void testMatches3(){
		assertThat(FluentFunctions.of(this::add)	
				   .matches(-1,c->c.hasValues(4).then(i->30),
						   			c->c.hasValues(8).then(i->32),
						   			c->c.hasValues(3).then(i->3))
				   .apply(1,1,1),equalTo(3));
	}

	@Test
	public void testMatches3Default(){
		assertThat(FluentFunctions.of(this::add)	
					   .matches(-1,c->c.hasValues(4).then(i->3),
							   		c->c.hasValues(8).then(i->32),
							   		c->c.hasValues(103).then(i->8))
					   .apply(1,1,1),equalTo(-1));
	}
	@Test
	public void testMatches4(){
		assertThat(FluentFunctions.of(this::add)	
				   .matches(-1,c->c.hasValues(4).then(i->30),
						   		c->c.hasValues(40).then(i->38),
						   			c->c.hasValues(8).then(i->32),
						   			c->c.hasValues(3).then(i->3))
				   .apply(1,1,1),equalTo(3));
	}

	@Test
	public void testMatches4Default(){
		assertThat(FluentFunctions.of(this::add)	
					   .matches(-1,c->c.hasValues(4).then(i->3),
							   		c->c.hasValues(40).then(i->38),
							   		c->c.hasValues(8).then(i->32),
							   		c->c.hasValues(103).then(i->8))
					   .apply(1,1,1),equalTo(-1));
	}
	@Test
	public void testMatches5(){
		assertThat(FluentFunctions.of(this::add)	
				   .matches(-1,c->c.hasValues(4).then(i->30),
						   		c->c.hasValues(5).then(i->50),
						   		c->c.hasValues(40).then(i->38),
						   			c->c.hasValues(8).then(i->32),
						   			c->c.hasValues(3).then(i->3))
				   .apply(1,1,1),equalTo(3));
	}

	@Test
	public void testMatches5Default(){
		assertThat(FluentFunctions.of(this::add)	
					   .matches(-1,c->c.hasValues(4).then(i->3),
							   		c->c.hasValues(5).then(i->50),
							   		c->c.hasValues(40).then(i->38),
							   		c->c.hasValues(8).then(i->32),
							   		c->c.hasValues(103).then(i->8))
					   .apply(1,1,1),equalTo(-1));
	}
	
	
	@Test
	public void testLift(){
		Integer nullValue = null;
		FluentFunctions.of(this::add)	
						.lift()
						.apply(Optional.ofNullable(nullValue),Optional.of(1));
	}
	@Test
	public void testLiftM(){
		
		AnyM<Integer> result = FluentFunctions.of(this::add)	
											  .liftM()
											  .apply(AnyM.streamOf(1,2,3,4),AnyM.ofNullable(1),AnyM.ofNullable(10));
		
		assertThat(result.asSequence().toList(),
					equalTo(Arrays.asList(12,13,14,15)));
	}
	@Test
	public void testTry(){
		
		Try<String,IOException> tried = FluentFunctions.ofChecked(this::exceptionalFirstTime)	
					   								   .liftTry(IOException.class)
					   								   .apply("hello","boo!","r");				  
		
		if(tried.isSuccess())
			fail("expecting failure");
		
	}
	Executor ex = Executors.newFixedThreadPool(1);
	@Test
	public void liftAsync(){
		assertThat(FluentFunctions.of(this::add)
						.liftAsync(ex)
						.apply(1,1,2)
						.join(),equalTo(4));
	}
	@Test
	public void async(){
		assertThat(FluentFunctions.of(this::add)
						.async(ex)
						.thenApply(f->f.apply(4,1,10))
						.join(),equalTo(15));
	}
	
	@Test
	public void testPartiallyApply3(){
		FluentSupplier<Integer> supplier = FluentFunctions.of(this::add)
														  .partiallyApply(3,1,2)
														  .println();
		assertThat(supplier.get(),equalTo(6));
	}
	@Test
	public void testPartiallyApply1(){
		FluentFunction<Integer,Integer> fn = FluentFunctions.of(this::add)
														  .partiallyApply(3,1)
														  .println();
		assertThat(fn.apply(1),equalTo(5));
	}
	@Test
	public void testPartiallyApply(){
		FluentBiFunction<Integer,Integer,Integer> fn = FluentFunctions.of(this::add)
														  .partiallyApply(3)
														  .println();
		assertThat(fn.apply(1,10),equalTo(14));
	}
	
	@Test
	public void curry(){
		assertThat(FluentFunctions.of(this::add)
		  .curry().apply(1).apply(2).apply(10),equalTo(13));
	}
}
