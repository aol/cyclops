package com.aol.cyclops.functions.fluent;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.aol.cyclops.types.anyM.Witness;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import cyclops.function.FluentFunctions;
import cyclops.function.FluentFunctions.FluentSupplier;
import com.aol.cyclops.control.Try;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class FluentFunctionTest {

	@Before
	public void setup(){
		this.times =0;
	}
	int called;
	public int addOne(Integer i ){
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
	@Test
	public void testCacheGuava() {
		Cache<Object, Integer> cache = CacheBuilder.newBuilder()
			       .maximumSize(1000)
			       .expireAfterWrite(10, TimeUnit.MINUTES)
			       .build();

		called=0;
		Function<Integer,Integer> fn = FluentFunctions.of(this::addOne)
													  .name("myFunction")
													  .memoize((key,f)->cache.get(key,()->f.apply(key)));
		
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
	
	public Integer exceptionalLessThanZero(Integer input) throws IOException{
		if(input < 0){
			throw new IOException();
		}
		return input * 2; 
	}
	
	public Integer exceptionalLessThanZeroWithBiParams(Integer input1, Integer input2) throws IOException{
		if(input1 + input2 < 0){
			throw new IOException();
		}
		return input1 * input2; 
	}
	
	public Integer exceptionalLessThanZeroWithTriParams(Integer input1, Integer input2, Integer input3) throws IOException{
		if(input1 + input2 + input3 < 0){
			throw new IOException();
		}
		return input1 * input2 * input3; 
	}
	
	@Test
	public void retry(){
		assertThat(FluentFunctions.ofChecked(this::exceptionalFirstTime)
					   .println()
					   .retry(2,500)
					   .apply("hello"),equalTo("hello world"));
	}
	
	@Test
	public void fluentFunctionVisitEvent(){
		CompletableFuture<Integer> future = new CompletableFuture<>();
		Integer i = FluentFunctions.ofChecked(this::exceptionalLessThanZero).visitEvent(value-> future.complete(value), error-> future.completeExceptionally(error)).apply(10);
		assertThat(i ,equalTo(20));
	}
	
	@Test(expected=Exception.class)
	public void fluentFunctionVisitEventexceptional(){
		CompletableFuture<Integer> future = new CompletableFuture<>();
		Integer i = FluentFunctions.ofChecked(this::exceptionalLessThanZero).visitEvent(value-> future.complete(value), error-> future.completeExceptionally(error)).apply(-10);
		assertThat(i ,equalTo(20));
	}
	
	@Test
	public void fluentBiFunctionVisitEvent(){
		CompletableFuture<Integer> future = new CompletableFuture<>();
		Integer i = FluentFunctions.ofChecked(this::exceptionalLessThanZeroWithBiParams).visitEvent(value-> future.complete(value), error-> future.completeExceptionally(error)).apply(10, 10);
		assertThat(i ,equalTo(100));
	}
	
	@Test(expected=Exception.class)
	public void fluentBiFunctionVisitEventexceptional(){
		CompletableFuture<Integer> future = new CompletableFuture<>();
		Integer i = FluentFunctions.ofChecked(this::exceptionalLessThanZeroWithBiParams).visitEvent(value-> future.complete(value), error-> future.completeExceptionally(error)).apply(-10, 9);
		assertThat(i ,equalTo(20));
	}
	
	@Test
	public void fluentTriFunctionVisitEvent(){
		CompletableFuture<Integer> future = new CompletableFuture<>();
		Integer i = FluentFunctions.ofChecked(this::exceptionalLessThanZeroWithTriParams).visitEvent(value-> future.complete(value), error-> future.completeExceptionally(error)).apply(10, 10, 10);
		assertThat(i ,equalTo(1000));
	}
	
	@Test(expected=Exception.class)
	public void fluentTriFunctionVisitEventexceptional(){
		CompletableFuture<Integer> future = new CompletableFuture<>();
		Integer i = FluentFunctions.ofChecked(this::exceptionalLessThanZeroWithTriParams).visitEvent(value-> future.complete(value), error-> future.completeExceptionally(error)).apply(-10, 1, 1);
		assertThat(i ,equalTo(20));
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
	public void testLift(){
		Integer nullValue = null;
		FluentFunctions.of(this::addOne)	
						.lift()
						.apply(1);
	}
	@Test
	public void testLiftM(){
		
		AnyM<Witness.stream,Integer> result = FluentFunctions.of(this::addOne)
											  .<Witness.stream>liftF()
											  .apply(AnyM.streamOf(1,2,3,4));
		
		assertThat(result.stream().toList(),
					equalTo(Arrays.asList(2,3,4,5)));
	}
	@Test
	public void testTry(){
		
		Try<String,IOException> tried = FluentFunctions.ofChecked(this::exceptionalFirstTime)	
					   								   .liftTry(IOException.class)
					   								   .apply("hello");				  
		
		if(tried.isSuccess())
			fail("expecting failure");
		
	}
	Executor ex = Executors.newFixedThreadPool(1);
	@Test
	public void liftAsync(){
		assertThat(FluentFunctions.of(this::addOne)
						.liftAsync(ex)
						.apply(1)
						.join(),equalTo(2));
	}
	@Test
	public void async(){
		assertThat(FluentFunctions.of(this::addOne)
						.async(ex)
						.thenApply(f->f.apply(4))
						.join(),equalTo(5));
	}
	
	@Test
	public void testPartiallyApply(){
		FluentSupplier<Integer> supplier = FluentFunctions.of(this::addOne)
														  .partiallyApply(3)
														  .println();
		supplier.get();
	}
	
    @Test
    public void memoizeWithCache() throws InterruptedException {
        
        Cache<Object, Integer> cache = CacheBuilder.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .build();
        
        int[] requests = new int[1];
        
        Function<Integer, Integer> addOne = i -> { 
            requests[0]++;
            return i + 1;
        };
        Function fn = FluentFunctions.of(addOne).name("myFunction")
                .memoize((key, f) -> cache.get(key, () -> f.apply(key)));

        fn.apply(10);
        fn.apply(10);
        
        Thread.sleep(2000);
        
        fn.apply(10);

        assertEquals(2, requests[0]);
        
    }
}
