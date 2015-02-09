package com.aol.simple.react.async;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.SimpleReact;
import com.aol.simple.react.Stage;

public class TopicTest {

	int count=0;
	int count1=100000;
	@Before
	public void setup(){
		count = 0;
		count1 = 100000;
	}

	@Test
	public void multipleSubscribersGetSameMessages() {
		Topic<String> topic = new Topic<>(new Queue<>());

		Stream<String> input = Stream.of("hello", "world");
		Stream<String> data1 = topic.stream();
		Stream<String> data2 = topic.stream();
		topic.fromStream(input);

		assertThat(data1.limit(1).findFirst().get(), is("hello"));
		assertThat(data2.limit(2).reduce("", (acc, next) -> acc + ' ' + next),
				is(" hello world"));
	}

	@Test
	public void multipleSubscribersGetSameMessagesSimpleReact() throws InterruptedException, ExecutionException {
		Topic<String> topic = new Topic<>(new Queue<>());

		Stream<String> input = Stream.of("hello", "world");
	
		
			
		//read from the topic concurrently in 2 threads
		
		 Stage<Collection<String>> stage = new SimpleReact(new ForkJoinPool(2))
			.react(()->SimpleReact.lazy()
				.fromStream(topic.streamCompletableFutures())
				.then(it -> it + "*")
				.<Collection<String>>run(()->new ArrayList<>() ),
				
				()->SimpleReact.lazy()
					.fromStream(topic.streamCompletableFutures())
					.then(it -> it + "!")
					.peek(it->sleep(10)) //make sure takes slightly longer to complete
					.<Collection<String>>run( ()->new HashSet<>() )
				
				);
		 
		sleep(50);//make sure streams are set up
		topic.fromStream(input);
		sleep(400); //wait until Topic has been read from
		topic.close();
		
		List<Collection<String>> result = stage.block();
		assertThat(result.get(0),is(ArrayList.class));
		assertThat(result.get(0),hasItem("hello*"));
		assertThat(result.get(0),hasItem("world*"));

		
		
		assertThat(result.get(1),is(HashSet.class));
		assertThat(result.get(1),hasItem("hello!"));
		assertThat(result.get(1),hasItem("world!"));
		
	}
	
	@Test @Ignore //too non-deterministic to run regularly - relying on population from competing threads
	public void mergingAndSplitting(){
		
		
		Topic<Integer> topic = new Topic<>();

		Stream<Integer> stream1 = topic.stream();
		Stream<Integer> stream2 = topic.stream();
		
		new SimpleReact().react(()-> topic.fromStream(Stream.generate(()->count++)));
		new SimpleReact().react(()-> topic.fromStream(Stream.generate(()->count1++)));

		
		
		
		for(Stream<Integer> stream : Arrays.asList(stream1,stream2)){
			List<Integer> result = stream.limit(1000)
										.peek(it->System.out.println(it))
										.collect(Collectors.toList());
			assertThat(result,hasItem(100000));
			assertThat(result,hasItem(0));
		}
	
	}
	
	@Test @Ignore //too non-deterministic to run regularly - relying on population from competing threads
	public void mergingAndSplittingSimpleReact(){
	
		
		Topic<Integer> topic = new Topic<>();
		
		 Stage<Collection<String>> stage = new SimpleReact(new ForkJoinPool(2))
			.react(()->SimpleReact.lazy()
				.fromStream(topic.streamCompletableFutures())
				.then(it -> it + "*")
				.<Collection<String>>run(()->new ArrayList<>() ),
				
				()->SimpleReact.lazy()
					.fromStream(topic.streamCompletableFutures())
					.then(it -> it + "!")
				
					.<Collection<String>>run( ()->new HashSet<>() )
				
				);
		
		 
		    sleep(50);//make sure streams are set up
			
		    new SimpleReact(new ForkJoinPool(1)).react(()-> topic.fromStream(Stream.generate(()->count++)));
			new SimpleReact(new ForkJoinPool(1)).react(()-> topic.fromStream(Stream.generate(()->count1++)));
			
			sleep(40); //wait until Topic has been read from
			System.out.println("Closing!");
			topic.close();
			System.out.println("Closed! Blocking..");
			List<Collection<String>> result = stage.block();
			System.out.println("Completed " + result.size());
			
		
			assertThat(extract1(result),hasItem("0*"));
			assertThat(extract1(result),hasItem("100000*"));

			
			assertThat(extract2(result),hasItem("0!"));
			assertThat(extract2(result),hasItem("100000!"));
		
		
	
	}
	
	private Collection<String> extract1(List<Collection<String>> result) {
		for(Collection next : result){
			if(next instanceof ArrayList)
				return next;
		}
		return null;
	}
	private Collection<String> extract2(List<Collection<String>> result) {
		for(Collection next : result){
			if(next instanceof HashSet)
				return next;
		}
		return null;
	}

	private int sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return i;
	}
}
