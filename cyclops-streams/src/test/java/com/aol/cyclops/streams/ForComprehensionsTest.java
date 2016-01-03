package com.aol.cyclops.streams;

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

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.sequence.SequenceM;

public class ForComprehensionsTest {

	@Test
	public void forEach2(){
		
		
		
//		System.out.println(s.collect(Collectors.toList()));
		assertThat(SequenceM.of(1,2,3)
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

		
		
		assertThat(SequenceM.of(1,2,3)
		         .forEach2(a->IntStream.range(0,10), 
		        		 a->b->a>2 && b<8,
		        		 a->b-> a+b)
		         .toList(),equalTo(Arrays.asList(3,4,5,6,7,8,9,10)));
	}
}
