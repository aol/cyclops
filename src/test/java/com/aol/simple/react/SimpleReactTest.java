package com.aol.simple.react;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;



public class SimpleReactTest {

	@Test
	public void testReact() throws InterruptedException, ExecutionException {
		
		
		List<CompletableFuture<Integer>> futures = new SimpleReact().<Integer, Integer> 
				react(() -> 1, () -> 2, () -> 3)
				.with((it) -> it * 100);
		
	
		
		assertThat(futures.get(0).get(), is(greaterThan(99)));

	}

	@Test
	public void testReactString() throws InterruptedException, ExecutionException {
		List<CompletableFuture<String>> futures = new SimpleReact().<Integer, String> react(() -> 1, () -> 2, () -> 3)
				.with((it) -> "*" + it);

		assertThat(futures.get(0).get(), is(containsString("*")));

	}
	
	
	

	@Test
	public void testReactChain() throws InterruptedException, ExecutionException {
		List<String> strings = new SimpleReact().<Integer, Integer> 
				react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100)
				.then((it) -> "*" + it)
				.block(status -> status.getCompleted()>1);

		assertThat(strings.get(0), is(containsString("*")));
		assertThat(Integer.valueOf(strings.get(0).substring(1)), is(greaterThan(99)));

	}
	
	
	@Test
	public void testAllOf() throws InterruptedException, ExecutionException {
		
		boolean blocked[] = {false};
		
		new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				
				.then(it -> {
					try {
						Thread.sleep(50000);
					} catch (Exception e) {
						
					}
					blocked[0] =true;
					return 10;
				})
				.allOf( it -> it.size());

		
		assertThat(blocked[0],is(false));
	}
	@Test
	public void testAllOfParallelStreams() throws InterruptedException, ExecutionException {
		
		List<Integer> result = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3, () -> 5)
				.<Integer>then(it -> {
					return it*200;
				}).then((Integer it) -> {
					if (it == 1000)
						throw new RuntimeException("boo!");
		
					return it;
				})
				.onFail(e -> 100)
				.allOf( it->{
					return it.parallelStream()
					.filter( f -> f>300)
					.map(m -> m-5)
					.reduce(0, (acc,next) -> acc+next); 
				}).block();

		
		assertThat(result.size(),is(1));
		assertThat(result.get(0),is(990));
	}
	
	
	@Test
	public void testBlockStreams() throws InterruptedException, ExecutionException {
		
		
		Integer result = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 200)
				.<Integer>block()
				.parallelStream()
				.filter( f -> f>300)
				.map(m -> m-5)
				.reduce(0, (acc,next) -> acc+next);
		
		
		assertThat(result,is(990));
	}
	
	@Test
	public void testBlock() throws InterruptedException, ExecutionException {
		
		
		List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100)
				.then((it) -> "*" + it)
				.block();

		assertThat(strings.size(),is(3));

	}
	
	@Test
	public void testOnFail() throws InterruptedException, ExecutionException {
		
		List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100)
				.then((it) -> {
					if (it == 100)
						throw new RuntimeException("boo!");
		
					return it;
				})
				.onFail(e -> 1)
				.then((it) -> "*" + it)
				.block();

		boolean foundOne[] = { false };
		strings.forEach((string) -> {
			assertThat(string, is(containsString("*")));
			if (Integer.valueOf(string.substring(1)) == 1)
				foundOne[0] = true;
		});

		assertThat(foundOne[0], is(true));

	}


	@Test
	public void testOnFailFirst() throws InterruptedException, ExecutionException {
		List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> { throw new RuntimeException("boo!");} )
				.onFail(e -> 1)
				.then((it) -> "*" + it)
				.block();

		boolean foundOne[] = { false };
		strings.forEach((string) -> {
			assertThat(string, is(containsString("*")));
			if (Integer.valueOf(string.substring(1)) == 1)
				foundOne[0] = true;
		});

		assertThat(foundOne[0], is(true));

	}

	@Test
	public void testCaptureNull() throws InterruptedException, ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100)
				.then((it) -> {
					if (it == 100)
						throw new RuntimeException("boo!");
		
					return it;
				})
				.onFail(e -> 1)
				.then((it) -> "*" + it)
				.capture(e -> error[0] = e)
				.block();

		boolean foundOne[] = { false };
		strings.forEach((string) -> {
			assertThat(string, is(containsString("*")));
			if (Integer.valueOf(string.substring(1)) == 1)
				foundOne[0] = true;
		});

		assertThat(foundOne[0], is(true));
		assertThat(error[0], is(nullValue()));

	}

	
	@Test
	public void testCapture() throws InterruptedException, ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");
		
					return it;
				})
				.onFail(e -> 1).then((it) -> "*" + it).then((it) -> {
					
					if ("*200".equals(it))
						throw new RuntimeException("boo!");
		
					return it;
				})
				.capture(e -> error[0] = e)
				.block();

		boolean foundTwoHundred[] = { false };
		strings.forEach((string) -> {
			assertThat(string, is(containsString("*")));
			if (Integer.valueOf(string.substring(1)) == 200)
				foundTwoHundred[0] = true;
		});

		assertThat(foundTwoHundred[0], is(false));
		assertThat(error[0], is(RuntimeException.class));

	}
	
	@Test
	public void testBreakout() throws InterruptedException, ExecutionException{
			Throwable[] error = {null};
			List<String> strings = new SimpleReact().<Integer,Integer>react(() -> 1,() -> 2,() -> 3)
					.then( (it) ->  it *100 )
					.then( (it) ->  {
						if(it==100)
							throw new RuntimeException("boo!");
						
						return	it;
					} )
					.onFail( e -> 1)
					.then( (it) -> "*" + it)
					.block( status -> status.getCompleted()>1);

				assertThat(strings.size(),is(2));
			
	}
	
	@Test
	public void testBreakoutException() throws InterruptedException, ExecutionException{
			Throwable[] error = {null};
			List<String> strings = new SimpleReact().<Integer,Integer>react(() -> 1,() -> 2,() -> 3)
					.then( (it) ->  it *100 )
					.then( (it) -> {
						
							throw new RuntimeException("boo!");
						
						})
					.capture(e -> error[0] = e)
					.block( status -> status.getCompleted()>1);

				assertThat(strings.size(),is(0));
				assertThat(error[0], is(RuntimeException.class));
	}
	
	@Test
	public void testBreakoutInEffective() throws InterruptedException, ExecutionException{
			Throwable[] error = {null};
			List<String> strings = new SimpleReact().<Integer,Integer>react(() -> 1,() -> 2,() -> 3)
					.then( (it) ->  it *100 )
					.then( (it) ->  {
						if(it==100)
							throw new RuntimeException("boo!");
						
						return	it;
					} )
					.onFail( e -> 1)
					.then( (it) -> "*" + it)
					.block( status -> status.getCompleted()>5);

				assertThat(strings.size(),is(3));
			
	}
}
