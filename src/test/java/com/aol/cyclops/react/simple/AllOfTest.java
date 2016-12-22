package com.aol.cyclops.react.simple;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Test;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

import cyclops.async.SimpleReact;


public class AllOfTest {

	@Test
	public void allOf(){
		List<HashPMap<String, List<Integer>>> result =
		
				SimpleReact.sequentialBuilder().ofAsync(()->1,()->2,()->3)
		 									 .then(it->it+100)
		 									 .peek(System.out::println)
		 									 .allOf((List<Integer> c)-> { System.out.println(c);return HashTreePMap.singleton("numbers",c);})
		 									 .peek(map -> System.out.println(map))
		 									 .block(Collectors.toList());
		 
		 assertThat(result.size(),is(1));
	}
	
	@Test
	public void testAllOfFailure(){
		new SimpleReact().ofAsync(()-> { throw new RuntimeException();},()->"hello",()->"world")
				//.onFail(it -> it.getMessage())
				.capture(e -> 
				  e.printStackTrace())
				.peek(it -> 
				System.out.println(it))
				.allOf((List<String> data) -> {
					System.out.println(data);
						return "hello"; }).block();
	}
	@Test
	public void testAllOfCompletableFutureOneFailsContinue(){
		List<String> urls = Arrays.asList("hello","world","2");
		List<String> result = new SimpleReact().fromStream(urls.stream()
				.<CompletableFuture<String>>map(it ->  handle(it)))
				
				.capture(e -> 
				  e.printStackTrace())
				.peek(it -> 
				System.out.println(it))
				.allOf((List<String> data) -> {
					System.out.println(data);
						return data; }).block().firstValue();
		
		assertThat(result.size(),is(2));
	}
	@Test
	public void testAllOfCompletableOnFail(){
		List<String> urls = Arrays.asList("hello","world","2");
		List<String> result = new SimpleReact().fromStream(urls.stream()
				.<CompletableFuture<String>>map(it ->  handle(it)))
				.onFail(it ->"hello")
				.capture(e -> 
				  e.printStackTrace())
				.peek(it -> 
				System.out.println(it))
				.allOf((List<String> data) -> {
					System.out.println(data);
						return data; }).block().firstValue();
		
		assertThat(result.size(),is(3));
	}
	@Test
	public void testAllOfCompletableFilter(){
		List<String> urls = Arrays.asList("hello","world","2");
		List<String> result = new SimpleReact().fromStream(urls.stream()
				.map(it ->  handle(it)))
				.onFail(it ->"hello")
				.filter(it-> !"2".equals(it))
				.capture(e -> 
				  e.printStackTrace())
				.peek(it -> 
				System.out.println(it))
				.allOf((List<String> data) -> {
					System.out.println(data);
						return data; }).block().firstValue();
		
		System.out.println(result);
		assertThat(result.size(),is(2));
		assertThat(result,hasItem("hello"));
		assertThat(result,hasItem("world"));
	}
	@Test
	public void testBlockompletableFuture(){
		List<String> urls = Arrays.asList("hello","world","2");
		List<String> result = new SimpleReact().fromStream(urls.stream()
				.<CompletableFuture<String>>map(it ->  handle(it)))
				
				.capture(e -> 
				  e.printStackTrace())
				.peek(it -> 
				System.out.println(it))
				.block();
		
		assertThat(result.size(),is(2));
	}
	
	private CompletableFuture<String> handle(String it) {
		if("hello".equals(it))
		{
			 CompletableFuture f= new CompletableFuture();
			 f.completeExceptionally(new RuntimeException());
			 return f;
		}
		return CompletableFuture.completedFuture(it);
	}
	@Test
	public void testAllOfToSet() throws InterruptedException, ExecutionException {

		Set<Integer> result = (Set<Integer>)new SimpleReact()
		.<Integer> ofAsync(() -> 1, () -> 2, () -> 3, () -> 5)
		.then( it -> it*100)
		.allOf(Collectors.toSet(), it -> {
			assertThat (it,instanceOf( Set.class));
			return it;
		}).block().firstValue();

		assertThat(result.size(),is(4));
	}

	
	
	@Test
	public void testAllOfParallelStreams() throws InterruptedException,
			ExecutionException {

		Integer result = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3, () -> 5)
				.<Integer> then(it -> {
					return it * 200;
				})
				.then((Integer it) -> {
					if (it == 1000)
						throw new RuntimeException("boo!");

					return it;
				})
				.onFail(e -> 100)
				.allOf(it -> {
					
					return it.parallelStream().filter(f -> f > 300)
							.map(m -> m - 5)
							.reduce(0, (acc, next) -> acc + next);
				}).block(Collectors.reducing(0, (acc,next)-> next));

	
		assertThat(result, is(990));
	}
	
	@Test
	public void testAllOfParallelStreamsSkip() throws InterruptedException,
			ExecutionException {

		List<Integer> result = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3, () -> 5)
				.<Integer> then(it -> {
					return it * 200;
				})
				.then((Integer it) -> {
					if (it == 1000)
						throw new RuntimeException("boo!");

					return it;
				})
				.onFail(e -> 100)
				.allOf(it -> {
					
					return it.parallelStream().skip(1).limit(3).collect(Collectors.toList());
				}).block().firstValue();

	
		assertThat(result.size(), is(3));
	}
	
	@Test
	public void testAllOfParallelStreamsSameForkJoinPool() throws InterruptedException,
			ExecutionException {
		Set<String> threadGroup = Collections.synchronizedSet(new TreeSet());
		Integer result = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3, () -> 5)
				.<Integer> then(it -> {
					threadGroup.add(Thread.currentThread().getThreadGroup().getName());
					return it * 200;
				})
				.then((Integer it) -> {
					if (it == 1000)
						throw new RuntimeException("boo!");

					return it;
				})
				.onFail(e -> 100)
				.allOf(it -> {
					
					return it.parallelStream().filter(f -> f > 300)
							.map(m ->{ threadGroup.add(Thread.currentThread().getThreadGroup().getName());return m - 5; })
							.reduce(0, (acc, next) -> acc + next);
				}).block(Collectors.reducing(0, (acc,next)-> next));

	
		assertThat(threadGroup.size(), is(1));
	}

	@Test
	public void testAllOf() throws InterruptedException, ExecutionException {

		boolean blocked[] = { false };

		new SimpleReact().<Integer> ofAsync(() -> 1)

		.then(it -> {
			try {
				Thread.sleep(10);
			} catch (Exception e) {

			}
			blocked[0] = true;
			return 10;
		}).allOf(it -> it.size());

		assertThat(blocked[0], is(false));
	}
	
}
