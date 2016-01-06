package com.aol.cyclops.comprehensions;

import static fj.data.Option.none;
import static fj.data.Option.some;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.typed.Do;

import fj.data.Option;


	
	
public class ForComprehensionTest {
	
	
		@Test
		public void intstream() {
			
			Stream<Integer> res =Do.addStream(IntStream.range(1,3).boxed()) 
										 .yield( v1-> v1 + 1)
										 .unwrap();
			List<Integer> expected = Arrays.asList(2,3);
			
			
			
			assertThat(expected, equalTo( res.collect(Collectors.toList())));
		}
		
		
		
		
		
		@Test
		public void test5() {
			
			val some = some(1);
			Supplier<Option<Integer>> s = ()->some;
			List<Option<Integer>> list = Arrays.<Option<Integer>>asList(some(0), some(1),  none(),some(2), some(10));
			List<Option<Integer>> res =Do.add(list)
											.filter(v1 -> v1.filter( it -> it > 1).isSome())
											.yield( v1-> v1.map(it->it+3) )
											.unwrap();
				
				
			
			List<Option> expected = Arrays.asList( some(5), some(13));
			//println res
			assertThat(res, equalTo(expected));
		
	}
		
		@Test
		public void optionTest(){
			Option<Integer> one = Option.some(1);
			Option<Integer> empty = Option.none();
			BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
			
			Option result =  Do.add(one)
								.add(empty)
								.filter(v1-> v2-> v1>2)
								.yield(v1-> v2->f2.apply(v1, 10)).unwrap();
			
			System.out.println(result);
			assertTrue(result.isNone());

		}
		@Test
		public void optionTestLessTyping(){
			Option<Integer> one = Option.some(1);
			Option<Integer> empty = Option.none();
			BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
			
			Option<Integer> result =  Do.add(one)
							.add(empty)
							.filter(v1->v2->v1>2)
							.yield(v1->v2->f2.apply(v1, 10)).unwrap();
			
			System.out.println(result);
			assertTrue(result.isNone());

		}
	
}
