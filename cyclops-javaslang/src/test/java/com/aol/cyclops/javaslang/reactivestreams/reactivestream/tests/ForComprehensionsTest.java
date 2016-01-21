package com.aol.cyclops.javaslang.reactivestreams.reactivestream.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import javaslang.collection.List;

import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;

public class ForComprehensionsTest {

	@Test
	public void forEach2(){
		
		
		

		assertThat(ReactiveStream.of(1,2,3)
		         .forEach2(a->ReactiveStream.range(0, 10), 
		        		 a->b-> a+b)
		         .toList(),equalTo(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8, 
		        		 9, 10, 11, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}
	
	@Test
	public void forEach2Filter(){

		
		
		assertThat(ReactiveStream.of(1,2,3)
		         .forEach2(a->ReactiveStream.range(0, 10), 
		        		 a->b->a>2 && b<8,
		        		 a->b-> a+b)
		         .toList(),equalTo(List.of(3,4,5,6,7,8,9,10)));
	}
	@Test
	public void forEach3(){
		

		assertThat(ReactiveStream.of(2,3)
		         .forEach3(a->ReactiveStream.range(6, 9),
		        		   a->b->ReactiveStream.range(100, 105),
		        		   a->b->c-> a+b+c)
		         .toList(),equalTo(List.of(108, 109, 110, 111, 112, 109, 110, 111, 112, 113, 110, 111, 112, 
		        		 113, 114, 109, 110, 111, 112, 113, 110, 111, 112, 113, 114, 111, 112, 113, 114, 115)));
	}
	@Test
	public void forEach3Filter(){
		

		assertThat(ReactiveStream.of(2,3)
		         .forEach3(a->ReactiveStream.range(6, 9),
		        		   a->b->ReactiveStream.range(100, 105),
		        		   a->b->c -> a==3,
		        		   a->b->c-> a+b+c)
		         .toList(),equalTo(List.of(109, 110, 111, 112, 113, 110, 111, 112, 113, 114, 111, 112, 113, 114, 115)));
	}
}
