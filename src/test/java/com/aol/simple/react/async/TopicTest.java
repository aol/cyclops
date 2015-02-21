package com.aol.simple.react.async;

import static com.aol.simple.react.stream.eager.EagerFutureStream.*;
import static org.hamcrest.Matchers.*;
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

import com.aol.simple.react.stream.api.FutureStream;
import com.aol.simple.react.stream.simple.SimpleReact;

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
		
		 FutureStream<Collection<String>> stage = new SimpleReact(new ForkJoinPool(2))
			.react(()->parallel()
				.fromStream(topic.stream())
				.then(it -> it + "*")
				.<Collection<String>>run(()->new ArrayList<>() ),
				
				()->parallel()
					.fromStream(topic.stream())
					.then(it -> it + "!")
					.peek(it->sleep(10)) //make sure takes slightly longer to complete
					.<Collection<String>>run( ()->new HashSet<>() )
				
				);
		 
		sleep(50);//make sure streams are set up
		topic.fromStream(input);
		sleep(400); //wait until Topic has been read from
		topic.close();
		
		List<Collection<String>> result = stage.block();
		assertThat(result.get(0),instanceOf(ArrayList.class));
		assertThat(result.get(0),hasItem("hello*"));
		assertThat(result.get(0),hasItem("world*"));

		
		
		assertThat(result.get(1),instanceOf(HashSet.class));
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
	@Test 
	public void simpleMergingAndSplitting(){
		
		
		Topic<Integer> topic = new Topic<>();

		Stream<Integer> stream1 = topic.stream();
		Stream<Integer> stream2 = topic.stream();
		
		topic.offer(count);
		topic.offer(count1);

		
		
		
		for(Stream<Integer> stream : Arrays.asList(stream1,stream2)){
			List<Integer> result = stream.limit(2)
										.peek(it->System.out.println(it))
										.collect(Collectors.toList());
			assertThat(result,hasItem(100000));
			assertThat(result,hasItem(0));
		}
	
	}
	
	@Test @Ignore //too non-deterministic to run regularly - relying on population from competing threads
	public void mergingAndSplittingSimpleReact(){
	
		
		Topic<Integer> topic = new Topic<>();
		
		 FutureStream<Collection<String>> stage = new SimpleReact(new ForkJoinPool(2))
			.react(()->parallel()
				.fromStream(topic.streamCompletableFutures())
				.then(it -> it + "*")
				.<Collection<String>>run(()->new ArrayList<>() ),
				
				()->parallel()
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
	
	@Test 
	public void simpleMergingAndSplittingSimpleReact(){
	
		
		Topic<Integer> topic = new Topic<>();
		
		 FutureStream<Collection<String>> stage = new SimpleReact(new ForkJoinPool(2))
			.react(()->parallel()
				.fromStream(topic.stream())
				.then(it -> it + "*")
				.<Collection<String>>run(()->new ArrayList<>() ),
				
				()->parallel()
					.fromStream(topic.stream())
					.then(it -> it + "!")
				
					.<Collection<String>>run( ()->new HashSet<>() )
				
				);
		
		 
		    sleep(50);//make sure streams are set up
			
		    topic.offer(count);
		    topic.offer(count1);
			
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
	
	@Test
	public void multipleQueues(){
		Topic<Integer> topic = new Topic<>();
		topic.stream();
		topic.stream(); //3 Queues
		
		assertThat(topic.getDistributor().getSubscribers().size(),is(2));
		assertThat(topic.getStreamToQueue().size(),is(2));
	}
	@Test
	public void disconnectStreams(){
		Topic<Integer> topic = new Topic<>();
		Stream s1 = topic.stream();
		Stream s2 = topic.stream(); //3 Queues
		
		
		topic.disconnect(s1);
		assertThat(topic.getDistributor().getSubscribers().size(),is(1));
		assertThat(topic.getStreamToQueue().size(),is(1));
	}
	@Test
	public void disconnectAllStreams(){
		Topic<Integer> topic = new Topic<>();
		Stream s1 = topic.stream();
		Stream s2 = topic.stream(); //3 Queues
		
		
		topic.disconnect(s1);
		topic.disconnect(s2);
		assertThat(topic.getDistributor().getSubscribers().size(),is(0));
		assertThat(topic.getStreamToQueue().size(),is(0));
	}
	@Test
	public void disconnectAllStreamsAndReconnect(){
		Topic<Integer> topic = new Topic<>();
		Stream s1 = topic.stream();
		Stream s2 = topic.stream(); //3 Queues
		
		
		topic.disconnect(s1);
		topic.disconnect(s2);
		assertThat(topic.getDistributor().getSubscribers().size(),is(0));
		assertThat(topic.getStreamToQueue().size(),is(0));
		
		topic.stream();
		
		assertThat(topic.getDistributor().getSubscribers().size(),is(1));
		assertThat(topic.getStreamToQueue().size(),is(1));
	}
	
	@Test 
	public void testSizeSignal(){
		
		Topic<Integer> t = new Topic<Integer>();
		Signal<Integer> s = t.getSizeSignal();
		
		t.offer(1);
		t.offer(1);
		t.offer(1);
		t.stream().limit(3).forEach(it -> System.out.println(it)); //drain the queue
		t.offer(1); //queue size is 1
		sleep(50); 
	   List<Integer> sizes = s.getDiscrete().stream().limit(7).collect(Collectors.toList());
	   assertThat(sizes.get(0),is(1));
	   assertThat(sizes.get(1),is(2));
	   assertThat(sizes.get(2),is(3));
	   assertThat(sizes.get(3),is(2));
	   assertThat(sizes.get(4),is(1));
	   assertThat(sizes.get(5),is(0));
	   assertThat(sizes.get(6),is(1));
	   
	  
	   
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
