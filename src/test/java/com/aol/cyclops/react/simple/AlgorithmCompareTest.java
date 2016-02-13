package com.aol.cyclops.react.simple;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import org.junit.Test;

import com.aol.cyclops.react.extractors.Extractors;
import com.aol.cyclops.react.stream.simple.SimpleReact;
import com.aol.cyclops.react.util.SimpleTimer;





public class AlgorithmCompareTest {

	@Test
	public void testFastest() throws InterruptedException, ExecutionException {

		ArrayList<Integer> arrayList = new ArrayList<>();
		LinkedList<Integer> linkedList = new LinkedList<>();
		for(int i=0;i<1001;i++){
			arrayList.add(i);
			linkedList.add(i);
		}
		SimpleTimer timer = new SimpleTimer();
		
		Result result = new SimpleReact()
		.<Result> react( () -> Result.builder().name("approach1").result(retrieval(arrayList)).build(), 
				() -> Result.builder().name("approach2").result(retrieval(linkedList)).build())
		.then(it -> it.withTime(timer.getElapsedNanoseconds()))
		.filter(it -> it.getResult()==1000)
		.blockAndExtract(Extractors.first());

		
		assertThat(result.getName(),is("approach1"));
	}
	
	

	@Test
	public void testFastestLessBlocking() throws InterruptedException, ExecutionException {

		ArrayList<Integer> arrayList = new ArrayList<>();
		LinkedList<Integer> linkedList = new LinkedList<>();
		for(int i=0;i<1001;i++){
			arrayList.add(i);
			linkedList.add(i);
		}
		SimpleTimer timer = new SimpleTimer();
		
		Result result = new SimpleReact()
		.<Result> react( () -> Result.builder().name("approach1 : arrayList").result(retrieval(arrayList)).build(), 
				() -> Result.builder().name("approach2 : linkedList").result(retrieval(linkedList)).build())
		.then(it -> it.withTime(timer.getElapsedNanoseconds()))
		.filter(it -> it.getResult()==1000)
		.first();

		
		assertThat(result.getName(),is("approach1 : arrayList"));
	}
	
	

	private int retrieval(List<Integer> list) {
		return list.get(1000);
	}



	@Wither
	@Getter
	@AllArgsConstructor
	@Builder
	static class Result{
		private final String name;
		private final int result;
		private final long time;
		
	}
	
	static class JavaResult {
	
		private final String name;
		private final int result;
		private final long time;
		
		
		
		public JavaResult(String name, int result, long time) {
			super();
			this.name = name;
			this.result = result;
			this.time = time;
		}
		
		static class Builder{
			private String name;
			private int result;
			private long time;
			
			public Builder name(String name){
				this.name = name;
				return this;
			}
			
			public Builder time(long time){
				this.time = time;
				return this;
			}
			
			public Builder result(int result){
				this.result = result;
				return this;
			}
			
			public JavaResult build(){
				return new JavaResult(name,result,time);
			}
		}
		
		public String getName() {
			return name;
		}
		public int getResult() {
			return result;
		}
		public long getTime() {
			return time;
		}
		public JavaResult withName(String name) {
			return new JavaResult.Builder().name(name).result(result).time(time).build();
		}
		public JavaResult withResult(int result) {
			return new JavaResult.Builder().name(name).result(result).time(time).build();
		}
		public JavaResult getTime(long time) {
			return new JavaResult.Builder().name(name).result(result).time(time).build();
		}
		
		
		
	}
	
}
