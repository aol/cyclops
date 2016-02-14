package com.aol.cyclops.react.lazy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.Do;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class ForComprehensionsTest {

	@Test
	public void forEach2(){
		
		
		

		assertThat(LazyFutureStream.of(1,2,3)
		         .forEach2(a->IntStream.range(0,10), 
		        		 a->b-> a+b)
		         .toList(),equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8, 
		        		 9, 10, 11, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}
	@Test
	public void intStreamSanityCheck() {
		Stream<Integer> s1 =  Do.addStream(Stream.of(1,2,3))
				.withBaseStream(u->IntStream.range(0,10))
				//.withStream(d->IntStream.range(0,10).boxed())
				.yield( a -> b-> a+b).unwrap();
		
		List<Integer> s = s1.collect(Collectors.toList());
		assertThat(s,
				equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8,
						9, 10, 11, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}
	@Test
	public void listSanityCheck(){
		List<Integer> s = Do.add(Arrays.asList(1,2,3))
				.withBaseStream(u->IntStream.range(0,10))
				.yield(a->b->a+b)
				.unwrap();
		assertThat(s,
				equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8,
						9, 10, 11, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));

		
	}
	@Test
	public void setSanityCheck(){
		Set<Integer> s = Do.add(new HashSet<>(Arrays.asList(1,2,3)))
				.withBaseStream(u->IntStream.range(0,10))
				.yield(a->b->a+b)
				.unwrap();
		assertThat(s,
				equalTo(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8,
						9, 10, 11, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12))));

		
	}
	@Test
	public void forEach2Filter(){

		
		
		assertThat(LazyFutureStream.of(1,2,3)
		         .forEach2(a->IntStream.range(0,10), 
		        		 a->b->a>2 && b<8,
		        		 a->b-> a+b)
		         .toList(),equalTo(Arrays.asList(3,4,5,6,7,8,9,10)));
	}
	@Test
	public void forEach3(){
		/**
Eclipse Mars Issue
		assertThat(LazyFutureStream.of(2,3)
		         .forEach3(a->IntStream.range(6,9),
		        		   a->b->IntStream.range(100,105),
		        		   a->b->c-> a+b+c)
		         .toList(),equalTo(Arrays.asList(108, 109, 110, 111, 112, 109, 110, 111, 112, 113, 110, 111, 112, 
		        		 113, 114, 109, 110, 111, 112, 113, 110, 111, 112, 113, 114, 111, 112, 113, 114, 115)));
**/
	}
	@Test
	public void forEach3Filter(){
		
		/**
Eclipse Mars Issue
		assertThat(LazyFutureStream.of(2,3)
		         .forEach3(a->IntStream.range(6,9),
		        		   a->b->IntStream.range(100,105),
		        		   a->b->c -> a==3,
		        		   a->b->c-> a+b+c)
		         .toList(),equalTo(Arrays.asList(109, 110, 111, 112, 113, 110, 111, 112, 113, 114, 111, 112, 113, 114, 115)));
	**/
	}
}