package com.aol.cyclops.react.mixins;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;

import org.junit.Test;

import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.react.async.pipes.Pipes;
import com.aol.cyclops.react.async.pipes.PipesToLazyStreams;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.futurestream.mixins.LazyReactive;
public class LazyReactiveTest {
	

		@Test
		public void testNoPipe() {
			Pipes.clear();
			assertFalse(new MyResource().queue().isPresent());
		}
		@Test
		public void testPipe() {
			LazyFutureStream<String> stream = PipesToLazyStreams.registerForIO("hello", new Queue<String>());
			assertTrue(new MyResource().queue().isPresent());
			
			assertThat(stream.limit(1).toList(),equalTo(Arrays.asList("world")));
			
		}
		@Test
		public void testCPUStreamIsInSyncMode() {
			
			MyResource resource = new MyResource();
			LazyFutureStream<String> lfs = resource.asyncCPUStream();
			
			assertFalse(lfs.isAsync());
			
		}
		@Test
		public void testIOStreamIsInSyncMode() {
			
			MyResource resource = new MyResource();
			LazyFutureStream<String> lfs = resource.asyncIOStream();
			
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
		static class MyResource implements LazyReactive{
			@Getter
			String val;
			
			
			public Optional<Adapter<String>> queue(){
				return this.enqueue("hello","world");
			}
			public LazyFutureStream<String> asyncIOStream(){
				List<String> collection = new ArrayList<>();
				for(int i=0;i<1000;i++)
					collection.add("hello");
				return this.ioStream().of(collection).map(String::toUpperCase);
			}
			public LazyFutureStream<String> asyncCPUStream(){
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
				List l = this.ioStream().of("hello").map(String::toUpperCase).peek(str->val=str).block();
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

