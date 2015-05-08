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

import com.aol.cyclops.comprehensions.LessTypingForComprehension1.Vars1;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Vars2;
import com.aol.cyclops.comprehensions.LessTypingForComprehension3.Vars3;

import fj.data.Option;


	
	
public class ForComprehensionTest {
	
	
		@Test
		public void intstream() {
			
			IntStream res = (IntStream)ForComprehensions.foreach1 (  c-> 
										c.mapAs$1(  IntStream.range(1,3)) 
										 .yield( (Vars1<Integer> v)-> v.$1() + 1));
			List<Integer> expected = Arrays.asList(2,3);
			
			
			
			assertThat(expected, equalTo( res.boxed().collect(Collectors.toList())));
		}
		@Test
		public void longstream() {
			
			
			
			LongStream res = ForComprehensions.foreach2 (  c-> 
										c.flatMapAs$1( LongStream.range(1l,30l))
										.mapAs$2((Vars2<Long,Long> v)-> LongStream.range(6l,10l))
										.yield(  v ->  (v.$1() * v.$2()) + 1l));
			
			
			
			List<Integer> expected = Arrays.asList(7, 8, 9, 10, 13, 15, 17, 19, 19, 22, 25, 28, 25, 29, 
										33, 37, 31, 36, 41, 46, 37, 43, 49, 55, 43, 50, 57, 64, 49, 57, 
										65, 73, 55, 64, 73, 82, 61, 71, 81, 91, 67, 78, 89, 100, 73, 85, 
										97, 109, 79, 92, 105, 118, 85, 99, 113, 127, 91, 106, 121, 136, 
										97, 113, 129, 145, 103, 120, 137, 154, 109, 127, 145, 163, 115, 
										134, 153, 172, 121, 141, 161, 181, 127, 148, 169, 190, 133, 155, 
										177, 199, 139, 162, 185, 208, 145, 169, 193, 217, 151, 176, 201, 
										226, 157, 183, 209, 235, 163, 190, 217, 244, 169, 197, 225, 253, 
										175, 204, 233, 262);
			
			
			
			assertThat(expected.stream().map(i->Long.valueOf(i)).collect(Collectors.toList()), equalTo( res.boxed().collect(Collectors.toList())));
		}
		@Test
		public void doubleStream() {
			
			
			DoubleStream res =ForComprehensions.foreach3 (  c-> 
										c.flatMapAs$1( DoubleStream.of(10.00,20.00))
										.flatMapAs$2((Vars3<Double,Double,Double> v)->DoubleStream.of(2.00,3.50))
										.mapAs$3(v->DoubleStream.of(25.50))
										.yield( v-> ( v.$1() * v.$2() * v.$3()) ));
			
			List<Double> expected = Arrays.asList(510.0, 892.5, 1020.0, 1785.0);
			
			
			
			assertThat(expected, equalTo( res.boxed().collect(Collectors.toList())));
		}
	
		@Test
		public void simpleLists() {
		
			
			Stream<Integer> res =ForComprehensions.foreach1 ( c-> 
																c.mapAs$1(Arrays.asList(1,2))
																.yield((Vars1<Integer> v) -> v.$1() +1));
			
			List<Integer> expected = Arrays.asList(2,3);
		
			assertThat(expected, equalTo( res.collect(Collectors.toList())));
			
		}
		
		@Test
		public void test5() {
			
			val some = some(1);
			Supplier<Option<Integer>> s = ()->some;
			List<Option<Integer>> list = Arrays.<Option<Integer>>asList(some(0), some(1),  none(),some(2), some(10));
			Stream<Option<Integer>> res =ForComprehensions.foreach1 (c-> c.mapAs$1(list)
															.filter((Vars1<Option<Integer>> v) -> v.$1().filter( it -> it > 1).isSome())
															.yield( v-> v.$1().map(it->it+3)));
				
				
			
			List<Option> expected = Arrays.asList( some(5), some(13));
			//println res
			assertThat(res.collect(Collectors.toList()), equalTo(expected));
		
	}
		
		@Test
		public void optionTest(){
			Option<Integer> one = Option.some(1);
			Option<Integer> empty = Option.none();
			BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
			
			Option result =  ForComprehensions.foreach2(c -> c.flatMapAs$1(one)
																.mapAs$2((Vars2<Integer,Integer> v) ->empty)
																.filter(v->v.$1()>2)
																.yield(v->{return f2.apply(v.$1(), 10);}));
			
			System.out.println(result);
			assertTrue(result.isNone());

		}
		@Test
		public void optionTestLessTyping(){
			Option<Integer> one = Option.some(1);
			Option<Integer> empty = Option.none();
			BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
			
			val result =  ForComprehensions
								.<Option<Integer>>foreach2(c -> c.flatMapAs$1(one)
																.mapAs$2((Vars2<Integer,Integer> v)->empty)
																.filter(v->v.$1()>2)
																.yield(v->{return f2.apply(v.$1(), 10);}));
			
			System.out.println(result);
			assertTrue(result.isNone());

		}
	
}
