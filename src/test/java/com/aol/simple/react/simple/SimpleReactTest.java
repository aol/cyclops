package com.aol.simple.react.simple;


import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.simple.react.extractors.Extractors;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.stream.traits.BaseSimpleReactStream;


public class SimpleReactTest {
	
	@Test
	public void streamOfEmpty(){
		List value = BaseSimpleReactStream.empty().block();
		assertThat(value.size(),is(0));
	}
	@Test
	public void streamOfOne(){
		Integer value = BaseSimpleReactStream.of(1).first();
		assertThat(value,is(1));
	}
	@Test
	public void streamParallelOf(){
		BaseSimpleReactStream value = BaseSimpleReactStream.parallel(1,2);
		
		assertThat(value.getTaskExecutor(),is(ForkJoinPool.commonPool()));
	}
	@Test
	public void futureStreamIterator(){
		assertThat(BaseSimpleReactStream.simpleReactStream(Arrays.asList(1,2,3,4).iterator()).block().size(),is(4));
	}
	@Test
	public void futureStreamIterable(){
		assertThat(BaseSimpleReactStream.simpleReactStreamFromIterable(Arrays.asList(1,2,3,4)).block().size(),is(4));
	}
	
	@Test
	public void futureStreamTest(){
		assertThat(BaseSimpleReactStream.simpleReactStream((Stream)LazyFutureStream.of(1,2,3,4)).block().size(),is(4));
	}
	@Test
	public void futureStreamFromStreamTest(){
		assertThat(BaseSimpleReactStream.simpleReactStream(Stream.of(1,2,3,4)).block().size(),is(4));
	}
	@Test
	public void syncTest(){
		BaseSimpleReactStream stream = BaseSimpleReactStream.of(1,2,3,4).sync();
		assertThat(stream.isAsync(),is(false));
	}
	@Test
	public void asyncTest(){
		BaseSimpleReactStream stream = BaseSimpleReactStream.of(1,2,3,4).async();
		assertThat(stream.isAsync(),is(true));
	}
	@Test
	public void syncAndAsyncTest(){
		BaseSimpleReactStream stream = BaseSimpleReactStream.of(1,2,3,4).sync().async();
		assertThat(stream.isAsync(),is(true));
	}
	@Test
	public void asyncSyncTest(){
		BaseSimpleReactStream stream = BaseSimpleReactStream.of(1,2,3,4).async().sync();
		assertThat(stream.isAsync(),is(false));
	}
	
	@Test
	public void doOnEach(){
		String[] found = {""};
		String res = new SimpleReact().react(()->"hello")
										.doOnEach(it->{ found[0]=it;return "world";})
										.then(it->it+"!")
										.first();
		assertThat(found[0],is("hello"));
		assertThat(res,is("hello!"));
	}
	
	@Test
	public void whenChainEmptyBlockReturns(){
		new SimpleReact(new ForkJoinPool(1))
		.from(new ArrayList<>())
		.block();
	}

	@Test
	public void whenChainEmptyBlockReturnsWithBreakout(){
		new SimpleReact(new ForkJoinPool(1))
		.from(new ArrayList<>())
		.block(status->false);
	}
	
	
	@Test
	public void testLazyParameters(){
		
		ForkJoinPool fjp = new ForkJoinPool();
		assertThat(new LazyReact(fjp).getExecutor(),is(fjp));
	}
	@Test
	public void testEagetParameters(){
		ForkJoinPool fjp = new ForkJoinPool();
		assertThat(new SimpleReact(fjp).getExecutor(),is(fjp));
	}
	
	@Test
	public void testReact() throws InterruptedException, ExecutionException {

		List<CompletableFuture<Integer>> futures = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.with(it -> it * 100);

		assertThat(futures.get(0).get(), is(greaterThan(99)));
		
		new SimpleReact().fromStream(futures.stream()).block();

	}

	@Test
	public void testReactList() throws InterruptedException, ExecutionException {

		List<CompletableFuture<Integer>> futures = new SimpleReact()
				.<Integer> reactCollection(Arrays.asList(() -> 1, () -> 2, () -> 3))
				.with(it -> it * 100);

		assertThat(futures.get(0).get(), is(greaterThan(99)));
		
		new SimpleReact().fromStream(futures.stream()).block();

	}
	@Test
	public void testMultithreading() throws InterruptedException, ExecutionException {
		
		
		 Set<Long> threads = new SimpleReact(new ForkJoinPool(10))
				.<Integer> react(() -> 1, () -> 2, () -> 3,() -> 3,() -> 3,() -> 3,() -> 3)
				.peek(it -> sleep(50l))
				.then(it -> Thread.currentThread().getId())
				.block(Collectors.toSet());

		assertThat(threads.size(), is(greaterThan(1)));

	}

	private void sleep(long l) {
		try {
			Thread.sleep(l);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testReactString() throws InterruptedException,
			ExecutionException {
		List<CompletableFuture<String>> futures = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.with(it -> "*" + it);

		System.out.println(futures.get(0).get());
		assertThat(futures.get(0).get(), is(containsString("*")));
		
		new SimpleReact().fromStream(futures.stream()).block();

	}

	@Test
	public void testReactChain() throws InterruptedException,
			ExecutionException {
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> "*" + it)
				.block(status -> status.getCompleted() > 1);

		assertThat(strings.get(0), is(containsString("*")));
		assertThat(Integer.valueOf(strings.get(0).substring(1)),
				is(greaterThan(99)));

	}

	
	@Test
	public void testGenericExtract() throws InterruptedException, ExecutionException {

		Set<Integer> result = new SimpleReact()
		.<Integer> react(() -> 1, () -> 2, () -> 3, () -> 5)
		.then( it -> it*100)
		.<Set<Integer>,Set<Integer>>allOf(Collectors.toSet(), (Set<Integer> it) -> {
			
			assertThat (it,instanceOf( Set.class));
			return it;
		}).capture(e -> e.printStackTrace())
		.blockAndExtract(Extractors.last(), status -> false);

		assertThat(result.size(),is(4));
	}
	

	

	@Test
	public void testOnFail() throws InterruptedException, ExecutionException {

		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1).then((it) -> "*" + it).block();

		boolean foundOne[] = { false };
		strings.forEach((string) -> {
			assertThat(string, is(containsString("*")));
			if (Integer.valueOf(string.substring(1)) == 1)
				foundOne[0] = true;
		});

		assertThat(foundOne[0], is(true));

	}

	@Test
	public void testOnFailFirst() throws InterruptedException,
			ExecutionException {
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1,() -> 2,(Supplier<Integer>) () -> {
					throw new RuntimeException("boo!");
				}).onFail(e -> 1).then((it) -> "*" + it).block();

		boolean foundOne[] = { false };
		strings.forEach((string) -> {
			assertThat(string, is(containsString("*")));
			if (Integer.valueOf(string.substring(1)) == 1)
				foundOne[0] = true;
		});

		assertThat(foundOne[0], is(true));

	}

	@Test
	public void testCaptureNull() throws InterruptedException,
			ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1).then((it) -> "*" + it)
				.capture(e -> error[0] = e).block();

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
		List<String> strings = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100).then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1).then((it) -> "*" + it).then((it) -> {

					if ("*200".equals(it))
						throw new RuntimeException("boo!");

					return it;
				}).capture(e -> error[0] = e).block();

		boolean foundTwoHundred[] = { false };
		strings.forEach((string) -> {
			assertThat(string, is(containsString("*")));
			if (Integer.valueOf(string.substring(1)) == 200)
				foundTwoHundred[0] = true;
		});

		assertThat(foundTwoHundred[0], is(false));
		assertThat(error[0], instanceOf(RuntimeException.class));

	}

	

	volatile int counter = 0;
	
	@Test
	public void testLargeChain(){
		BaseSimpleReactStream builder= new SimpleReact().react(() -> "Hello", () -> "World"); 
		 for(int i =0;i<1000;i++){
			 builder = builder.then( input -> input + " " + counter++);
		 }
		 List<String> results = builder.block();
		 assertThat(results.get(0).length(),greaterThan(100));
	}
	@Test
	public void testSeparatedChains(){
		 BaseSimpleReactStream<String> orgBuilder= new SimpleReact().react(() -> "Hello", () -> "World");//.split(2); 
		 BaseSimpleReactStream builder = orgBuilder;
		 for(int i =0;i<1000;i++){
			 builder = builder.then( input -> input + " " + counter++);
		 }
		 List<String> results = orgBuilder.block();
		 assertThat(results.get(0),is("Hello"));
		 
		 List<String> completeResults =builder.block();
		 assertThat( completeResults.get(0).length(),greaterThan(100));
	}
	@Test
	public void testReactMixedTypes(){
		List list = new ArrayList();
		List<Object> result = new SimpleReact().react(() -> "Hello",()-> list).block();
		assertThat(result.size(),is(2));
		assertThat(result,hasItem("Hello"));
		assertThat(result,hasItem(list));
	
	}
	@Test
	public void testThenMixedTypes(){
		List list = new ArrayList();
		Map responses = new HashMap();
		responses.put("Hello", (byte) 4);
		responses.put(list,true);
		
		List<Object> result = new SimpleReact().react(() -> "Hello",()-> list).then( it -> responses.get(it)).block();
		assertThat(result.size(),is(2));
		
		assertThat(result,hasItem((byte)4));
		assertThat(result,hasItem(true));
	
	}
	
	@Test
	public void testReactPrimitive(){
		List<Boolean> result = new SimpleReact().react(() -> true,()->true).block();
		assertThat(result.size(),is(2));
		assertThat(result.get(0),is(true));
	
	}
	@Test
	public void testThenPrimitive(){
		List<Boolean> result = new SimpleReact().react(() -> 1,()-> 1).then(it -> true).block();
		assertThat(result.size(),is(2));
		assertThat(result.get(0),is(true));
	
	}
	@Test
	public void testReactNull(){
		List<String> result = new SimpleReact().react(() -> null,()-> "Hello").block();
		assertThat(result.size(),is(2));
	
	}
	@Test
	public void testThenNull(){
		List<String> result = new SimpleReact().react(() -> "World",()-> "Hello").then( in -> (String)null).block();
		assertThat(result.size(),is(2));
		assertThat(result.get(0),is(nullValue()));
	
	}
	@Test
	public void testReactExceptionRecovery(){
		List<String> result = new SimpleReact()
									.react(() -> {throw new RuntimeException();},()-> "Hello")
									.onFail( e ->{ System.out.println(e);return "World";})
									.block();
		
		assertThat(result.size(),is(2));
	
	}
	
	@Test
	public void testCustomExecutor() {
		ExecutorService executor = mock(ExecutorService.class);
		doAnswer((invocation) -> {
			((Runnable) invocation.getArguments()[0]).run();
			return null;
		}).when(executor).execute(any(Runnable.class));
		
		new SimpleReact(executor).react(() -> "Hello", () -> "World").block();
		verify(executor, times(2)).execute(any(Runnable.class));
	}
	
	@Test
    public void testBlockInterruption() {
        final AtomicBoolean isRunning = new AtomicBoolean(true);
        final CountDownLatch startBarier = new CountDownLatch(1);

        final BaseSimpleReactStream<Integer> stage = new SimpleReact().<Integer>react(
                () -> 1,
                () -> 2,
                () -> 3
                ).then((it) -> {
                    try {
                       Thread.sleep(it * 5000);
                    } catch (InterruptedException e) {
                        System.err.println("InterruptedException");
                        Thread.currentThread().interrupt();
                    }
                    return it * 100;
                });

        Thread t = new Thread(() -> {
            while (isRunning.get()) { //worker thread termination condition
                startBarier.countDown();
                try {
                    while (true) { //random condition
                        stage.block();
                       // Thread.sleep(2 * 5000);
                    }
                } catch (Exception e) {
                    System.err.println("InterruptedException " + e.getMessage());
                }
            }
        });

        t.start();

        try {
            startBarier.await();
            isRunning.getAndSet(false);
            t.interrupt();
            t.join();
        } catch (InterruptedException e) {
            //you know I don't care
        }
    }
}
