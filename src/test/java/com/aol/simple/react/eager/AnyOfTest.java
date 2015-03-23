package com.aol.simple.react.eager;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerReact;

public class AnyOfTest {

	@Test
	public void testAnyOfFailure(){
		new EagerReact().react(()-> { throw new RuntimeException();},()->"hello",()->"world")
				.capture(e -> 
				  e.printStackTrace())
				.peek(it -> 
				System.out.println(it))
				.anyOf(data -> {
					System.out.println(data);
						return "hello"; }).block();
	}
	@Test
	public void testAnyOfCompletableFutureOnFailRecovers(){
		List<String> urls = Arrays.asList("hello","world","2");
		List<String> result = new EagerReact().fromStream(urls.stream()
				.<CompletableFuture<String>>map(it ->  handle(it)))
				
				.capture(e -> 
				  e.printStackTrace()).onFail(e -> "woot!")

				.anyOf(data -> {
					System.out.println(data);
						return data; }).block();
		
		assertThat(result.size(),is(1));
	}	
	@Test
	public void testAnyOfCompletableExceptionally(){
		List<String> urls = Arrays.asList("hello","world","2");
		List<String> result = new EagerReact().fromStream(urls.stream()
				.<CompletableFuture<String>>map(it ->  handle(it)))
				
				.capture(e -> 
				  e.printStackTrace())

				.anyOf(data -> {
					System.out.println(data);
						return data; }).block();
		
		assertThat(result.size(),is(0));
	}
	@Test
	public void testAnyOfCompletableOnFail(){
		List<String> urls = Arrays.asList("hello","world","2");
		String result = new EagerReact().fromStream(urls.stream()
				.<CompletableFuture<String>>map(it ->  handle(it)))
				.onFail(it ->"hello")
				.capture(e -> 
				  e.printStackTrace())
				.peek(it -> 
				System.out.println(it))
				.anyOf(data -> {
					System.out.println(data);
						return data; }).first();
		
		assertThat(urls,hasItem(result));
	}
	@Test @Ignore
	public void testAnyOfCompletableFilter(){
		List<String> urls = Arrays.asList("hello","world","2");
		String result = new EagerReact().fromStream(urls.stream()
				.<CompletableFuture<String>>map(it ->  handle(it)))
				.onFail(it ->"hello")
				.filter(it-> !"2".equals(it))
				.capture(e -> 
				  e.printStackTrace())
				.peek(it -> 
				System.out.println(it))
				.anyOf(data -> {
					System.out.println(data);
						return data; }).first();
		
		assertThat(urls,hasItem(result));
		
	}
	@Test
	public void testAnyOfCompletableFilterNoTarget(){
	
		String result = new EagerReact().of("hello","world","2")
				.filter(it-> !"23".equals(it))
				.peek(it -> 
				System.out.println(it))
				.anyOf(data -> {
					System.out.println(data);
						return data; }).first();
		
		assertThat(result,is(notNullValue()));
		
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
	public void testAnyOf() throws InterruptedException, ExecutionException {

		boolean blocked[] = { false };

		new EagerReact().<Integer> react(() -> 1)

		.then(it -> {
			try {
				Thread.sleep(10);
			} catch (Exception e) {

			}
			blocked[0] = true;
			return 10;
		}).anyOf(it -> it);

		assertThat(blocked[0], is(false));
	}
}
