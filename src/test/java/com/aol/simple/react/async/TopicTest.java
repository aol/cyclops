package com.aol.simple.react.async;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.simple.react.SimpleReact;

public class TopicTest {

	@Test
	public void multipleSubscribersGetSameMessages() {
		Topic t = new Topic(new Queue());

		Stream<String> input = Stream.of("hello", "world");
		Stream<String> data1 = t.provideStream();
		Stream<String> data2 = t.provideStream();
		t.fromStream(input);

		assertThat(data1.limit(1).findFirst().get(), is("hello"));
		assertThat(data2.limit(2).reduce("", (acc, next) -> acc + ' ' + next),
				is(" hello world"));
	}

	@Test
	public void multipleSubscribersGetSameMessagesSimpleReact() throws InterruptedException, ExecutionException {
		Topic t = new Topic(new Queue());

		Stream<String> input = Stream.of("hello", "world");
		Stream<CompletableFuture<String>> data1 = t.provideStreamCompletableFutures();
		Stream<CompletableFuture<String>> data2 = t.provideStreamCompletableFutures();
		t.fromStream(input);
		

		new SimpleReact().react(()-> { sleep(400); t.close(); return 1;}); //Topic Manager
		
		ConcurrentMap store1 = new ConcurrentHashMap();
		ConcurrentMap store2 = new ConcurrentHashMap();
		Supplier s1 = () -> { new SimpleReact(false).fromStream(data1).then(it -> it + "*")
				.peek(it -> System.out.println(it))
				.peek(it -> store1.put(it, it)).run(); return 1; }; //Topic reader 1

		Supplier s2 = () -> { new SimpleReact(false).fromStream(data2).then(it -> it + "!")
				.peek(it -> store2.put(it, it)).run(); return 1;}; //Topic reader 2
		
				
		new SimpleReact().react(s1,s2).block();
		assertTrue(store1.containsKey("hello*"));
		assertTrue(store1.containsKey("world*"));

		
		

		assertTrue(store2.containsKey("hello!"));
		assertTrue(store2.containsKey("world!"));
		t.close();
	}
	private void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
