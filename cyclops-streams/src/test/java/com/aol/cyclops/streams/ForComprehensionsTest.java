package com.aol.cyclops.streams;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.sequence.SequenceM;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ForComprehensionsTest {

	@Test
	public void forEach2(){
		Stream<Double> s1 =  Do.addStream(Stream.of(1,2,3))
				.withBaseStream(u->IntStream.range(0,10))
				//.withStream(d->IntStream.range(0,10).boxed())
				.yield( a -> b-> a+b).unwrap();
		System.out.println(s1.collect(Collectors.toList()));
		
		List<Integer> s = Do.add(Arrays.asList(1,2,3))
								.withBaseStream(u->IntStream.range(0,10))
								.yield(a->b->a+b)
								.unwrap();
		
		System.out.println(s);
//		System.out.println(s.collect(Collectors.toList()));
		assertThat(SequenceM.of(1,2,3)
		         .forEach2(a->IntStream.range(0,10), 
		        		 a->b-> a+b)
		         .toList(),equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8, 
		        		 9, 10, 11, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
	}
}
