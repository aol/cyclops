package com.aol.simple.react;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.aol.simple.react.async.Signal;

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
	public void signalDiscrete3(){
		try{
			Signal<Integer> q = new Signal<Integer>();
			
			
			new SimpleReact().react(() -> q.set(1), ()-> q.set(2),()-> {sleep(200); return q.set(4); }, ()-> { sleep(400); q.getDiscrete().setOpen(false); return 1;});
			
			
			
			new SimpleReact(false).fromStream(q.getDiscrete().dequeueForSimpleReact())
					.then(it -> "*" +it)
					.peek(it -> incrementFound())
					.peek(it -> System.out.println(it))
					.run();
			
			
			
			
				
		}finally{
			assertThat(found,is(3));
		}
		
		
	}
	@Test
	public void signalDiscrete1(){
		try{
			Signal<Integer> q = new Signal<Integer>();
			
			
			new SimpleReact().react(() -> q.set(1), ()-> q.set(1),()-> {sleep(200); return q.set(1); }, ()-> { sleep(400); q.getDiscrete().setOpen(false); return 1;});
			
			
			
			new SimpleReact(false).fromStream(q.getDiscrete().dequeueForSimpleReact())
					.then(it -> "*" +it)
					.peek(it -> incrementFound())
					.peek(it -> System.out.println(it))
					.run();
			
			
			
			
				
		}finally{
			assertThat(found,is(1));
		}
		
		
	}
	@Test
	public void signalContinuous3(){
		try{
			Signal<Integer> q = new Signal<Integer>();
			
			
			new SimpleReact().react(() -> q.set(1), ()-> q.set(1),()-> {sleep(200); return q.set(1); }, ()-> { sleep(400); q.close(); return 1;});
			
			
			
			new SimpleReact(false).fromStream(q.getContinuous().dequeueForSimpleReact())
					.then(it -> "*" +it)
					.peek(it -> incrementFound())
					.peek(it -> System.out.println(it))
					.run();
			
			
			
			
				
		}finally{
			assertThat(found,is(3));
		}
		
		
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
