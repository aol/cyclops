package com.aol.simple.react.mixins;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import lombok.Getter;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.pipes.PipesToEagerStreams;
import com.aol.simple.react.stream.traits.EagerFutureStream;
public class EagerReactiveTest {
	

		
		@Test 
		public void testPipe() {
			Queue<String> queue=new Queue<String>();
			EagerFutureStream<String> stream = PipesToEagerStreams.streamIOBound(queue);
			new MyResource().queue(queue);
			queue.close();
			System.out.println("hello");
			assertThat(stream.limit(1).toList(),equalTo(Arrays.asList("world")));
			
		}
		@Test
		public void testCPUStreamIsInSyncMode() {
			
			MyResource resource = new MyResource();
			EagerFutureStream<String> lfs = resource.asyncCPUStream();
			
			assertFalse(lfs.isAsync());
			
		}
		@Test
		public void testIOStreamIsInSyncMode() {
			
			MyResource resource = new MyResource();
			EagerFutureStream<String> lfs = resource.asyncIOStream();
			
			assertFalse(lfs.isAsync());
			
		}
		@Test
		public void testFanoutAcrossThreadsIOBound() {
			
			MyResource resource = new MyResource();
			Set<Long> threadIds = resource.asyncIOFanout();
			
			assertTrue(threadIds.size()>0);
			
		}
		
		@Test
		public void testFanoutAcrossThreadsCPUBound() {
			if(Runtime.getRuntime().availableProcessors()>1)
				System.out.println("need at least 2 threads for this test");
			MyResource resource = new MyResource();
			Set<Long> threadIds = resource.asyncCPUFanout();
			
			assertTrue(threadIds.size()>0);
			
		}
		@Test
		public void testAsyncIO() {
			MyResource resource = new MyResource();
			resource.asyncIO();
			
			assertThat(resource.getVal(),equalTo("HELLO"));
			
		}
		@Test
		public void testAsyncCPU() {
			MyResource resource = new MyResource();
			resource.asyncIO();
			
			assertThat(resource.getVal(),equalTo("HELLO"));
			
		}
		@Test
		public void testAsync() {
			MyResource resource = new MyResource();
			resource.async();
			
			assertThat(resource.getVal(),equalTo("HELLO"));
			
		}
		@Test
		public void testSync() {
			MyResource resource = new MyResource();
			resource.sync();
			
			assertThat(resource.getVal(),equalTo("HELLO"));
			
		}
		static class MyResource implements EagerReactive{
			@Getter
			String val;
			
			
			public void queue(Queue<String> queue){
				queue.offer("world");
			}
			public EagerFutureStream<String> asyncIOStream(){
				List<String> collection = new ArrayList<>();
				for(int i=0;i<1000;i++)
					collection.add("hello");
				return this.ioStream().of(collection).map(String::toUpperCase);
			}
			public EagerFutureStream<String> asyncCPUStream(){
				List<String> collection = new ArrayList<>();
				for(int i=0;i<1000;i++)
					collection.add("hello");
				return this.cpuStream().of(collection).map(String::toUpperCase);
			}
			public Set<Long> asyncIOFanout(){
				List<String> collection = new ArrayList<>();
				for(int i=0;i<1000;i++)
					collection.add("hello");
				return this.ioStream().of(collection).map(str-> Thread.currentThread().getId()).toSet();
			}
			public Set<Long> asyncCPUFanout(){
				List<String> collection = new ArrayList<>();
				for(int i=0;i<1000;i++)
					collection.add("hello");
				return this.cpuStream().of(collection).map(str-> Thread.currentThread().getId()).toSet();
			}
			public void asyncIO(){
				this.ioStream().of("hello").map(String::toUpperCase).peek(str->val=str).block();
			}
			public void asyncCPU(){
				this.cpuStream().of("hello").map(String::toUpperCase).peek(str->val=str).block();
			}
			public void async(){
				this.sync(lr->lr.of("hello").map(String::toUpperCase).peek(str->val=str)).block();
			}
			public void sync(){
				this.sync(lr->lr.of("hello").map(String::toUpperCase).peek(str->val=str)).block();
			}
		}

	}

