package com.aol.simple.react.async;

import static com.aol.simple.react.stream.eager.EagerFutureStream.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.aol.simple.react.stream.api.FutureStream;
import com.aol.simple.react.stream.simple.SimpleReact;

public class SignalTest {

	@Before
	public void setup(){
		found =0;
	}
	int found =0;
	public synchronized void incrementFound(){
		found++;
	}
	
	@Test
	public void signalFromStream(){
		Signal<Integer> q = Signal.topicBackedSignal();
		Stream<Integer> stage =q.getDiscrete().stream().limit(2);
		q.fromStream(Stream.of(1,1,1,2,2));
		
		 
		int sum  = stage.map(it -> it*100).reduce(0, (acc,n) -> acc+n);
		
		 assertThat(sum,is(300));
	}
	
	@Test
	public void signalDiscrete3(){
		try{
			Signal<Integer> q = Signal.queueBackedSignal();

			new SimpleReact().react(
					() -> q.set(1), 
					() -> q.set(2),
					()-> { 
						sleep(20); 
						return q.set(4); 
					}, 
					()-> { 
						sleep(400); 
						q.getDiscrete().close(); 
						return 1;
					});
			
			parallel().fromStream(q.getDiscrete().streamCompletableFutures())
					.then(it -> "*" +it)
					.peek(it -> incrementFound())
					.peek(it -> System.out.println(it))
					.run(() -> new ArrayList<String>());
		} finally{
			assertThat(found, is(3));
		}
	}
	
	@Test
	public void signalDiscrete1(){
		try{
			Signal<Integer> q = Signal.queueBackedSignal();
			
			
			new SimpleReact().react(() -> q.set(1), ()-> q.set(1),()-> {sleep(200); return q.set(1); }, ()-> { sleep(400); q.getDiscrete().close(); return 1;});
			
			
			
			parallel().fromStream(q.getDiscrete().streamCompletableFutures())
					.then(it -> "*" +it)
					.peek(it -> incrementFound())
					.peek(it -> System.out.println(it))
					.runOnCurrent();
			
			
			
			
				
		}finally{
			assertThat(found,is(1));
		}
		
		
	}
	@Test
	public void signalContinuous3(){
		try{
			Signal<Integer> q =Signal.queueBackedSignal();
			
			
			new SimpleReact().react(() -> q.set(1), ()-> q.set(1),()-> {sleep(200); return q.set(1); }, ()-> { sleep(400); q.close(); return 1;});
			
			
			
			parallel().fromStream(q.getContinuous().streamCompletableFutures())
					.then(it -> "*" +it)
					.peek(it -> incrementFound())
					.peek(it -> System.out.println(it))
					.runOnCurrent();
			
			
			
			
				
		}finally{
			assertThat(found,is(3));
		}
		
		
	}
	
	@Test
	public void testDiscreteMultipleStreamsQueue(){
		 Signal<Integer> s = Signal.queueBackedSignal();
		 s.set(1);
		 s.set(2);
		s.getDiscrete().stream().limit( 1);
		s.getDiscrete().stream().limit( 1);
	}
	@Test
	public void testContinuousMultipleStreamsQueue(){
		 Signal<Integer> s = Signal.queueBackedSignal();
		 s.set(1);
		 s.set(2);
		s.getContinuous().stream().limit( 1);
		s.getContinuous().stream().limit( 1);
	}
	@Test
	public void testDiscreteMultipleStreamsTopic(){
		 Signal<Integer> s = Signal.topicBackedSignal();
		 s.set(1);
		 s.set(2);
		s.getDiscrete().stream().limit( 1);
		s.getDiscrete().stream().limit( 1);
	}
	@Test
	public void testContinuousMultipleStreamsTopic(){
		 Signal<Integer> s = Signal.topicBackedSignal();
		 s.set(1);
		 s.set(2);
		s.getContinuous().stream().limit( 1);
		s.getContinuous().stream().limit( 1);
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
