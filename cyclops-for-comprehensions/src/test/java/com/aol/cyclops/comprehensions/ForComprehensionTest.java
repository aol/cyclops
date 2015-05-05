package com.aol.cyclops.comprehensions;


import static com.aol.cyclops.comprehensions.LessTypingForComprehension3.foreach;
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

import fj.data.Option;


	
	
public class ForComprehensionTest {
	
	
		@Test
		public void intstream() {
			
			IntStream res = (IntStream)foreach (  c-> 
										c.flatMapAs$1(  IntStream.range(1,3)) 
										 .yield( ()-> c.<Integer>$1() + 1));
			List<Integer> expected = Arrays.asList(2,3);
			
			
			
			assertThat(expected, equalTo( res.boxed().collect(Collectors.toList())));
		}
		@Test
		public void longstream() {
			val comp =  new ForComprehension2<LongStream,LongStream,Long>();
			
			
			LongStream res = comp.<Long,Long>foreach (  c-> 
										c.flatMapAs$1( LongStream.range(1l,30l))
										.mapAs$2(()->LongStream.range(6l,10l))
										.yield( ()-> ( c.$1() * c.$2()) + 1l));
			
			
			
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
			val comp =  new ForComprehension3<DoubleStream,DoubleStream,Double>();
			
			DoubleStream res = comp.<Double,Double,Double>foreach (  c-> 
										c.flatMapAs$1( DoubleStream.of(10.00,20.00))
										.flatMapAs$2(()->DoubleStream.of(2.00,3.50))
										.mapAs$3(()->DoubleStream.of(25.50))
										.yield( ()-> ( c.$1() * c.$2() * c.$3()) ));
			List<Double> expected = Arrays.asList(510.0, 892.5, 1020.0, 1785.0);
			
			
			
			assertThat(expected, equalTo( res.boxed().collect(Collectors.toList())));
		}
	
		@Test
		public void simpleLists() {
			val comp =  new ForComprehension1<List,Stream<Integer>,Integer>();
			
			Stream<Integer> res =comp.<Integer>foreach ( c-> 
						c.mapAs$1(Arrays.asList(1,2))
						 .yield(()-> c.$1() +1));
			
			List<Integer> expected = Arrays.asList(2,3);
		
			assertThat(expected, equalTo( res.collect(Collectors.toList())));
			
		}
		/**
		@Test
		void test1() {
			def res = foreach {
				a { 1.to(2) }
				b { 1.to(1) }
				yield {
					[a, b]
				}
			}
	//		def expected = [[1, 3], [1, 4], [2, 3], [2, 4]]
			def expected = [[1, 1], [2, 1]]
			assertTrue(expected == res.toJList())
		}
		**/
		/**
		
		@Test
		void test1() {
			def res = foreach {
				a { 1.to(2) }
				b { 1.to(1) }
				yield {
					[a, b]
				}
			}
	//		def expected = [[1, 3], [1, 4], [2, 3], [2, 4]]
			def expected = [[1, 1], [2, 1]]
			assertTrue(expected == res.toJList())
		}
	
		@Test
		void test2() {
			def res = foreach {
				a { 1.to(2) }
				b { a.to(2) }
				yield {
					[a, b]
				}
			}
			def expected = [[1, 1], [1, 2], [2, 2]]
			def actual = res.toJList()
			assertTrue(expected == actual)
		}
	
		@Test
		void test3() {
			def res = foreach {
				a { 1.to(2) }
				guard {
					a == 2
				}
				yield {
					a
				}
			}
			def expected = [2]
			assertTrue(expected == res.toJList())
		}
	
		@Test
		void test4() {
			def res = foreach {
				a { 1.to(2) }
				b { 3.to(4) }
				guard {
					a == 2 && b == 3
				}
				c { 5.to(6) }
				guard { c == 5 }
				yield {
					[a, b, c]
				}
			}
			def expected = [[2, 3, 5]]
			def actual = res.toJList()
			assertTrue(actual == expected)
		}
	**/
		@Test
		public void test5() {
			val comp =  new ForComprehension1<List<Option<Integer>>,Stream<Option<Integer>>,Option<Integer>>();
			val some = some(1);
			Supplier<Option<Integer>> s = ()->some;
			List<Option<Integer>> list = Arrays.<Option<Integer>>asList(some(0), some(1),  none(),some(2), some(10));
			Stream<Option<Integer>> res =comp
							.<Option<Integer>>foreach(c-> c.mapAs$1(list)
															.filter(()-> c.$1().filter( it -> it > 1).isSome())
															.yield( ()-> c.$1().map(it->it+3)));
				
				
			
			List<Option> expected = Arrays.asList( some(5), some(13));
			//println res
			assertThat(res.collect(Collectors.toList()), equalTo(expected));
		
	}
		
		@Test
		public void optionTest(){
			Option<Integer> one = Option.some(1);
			Option<Integer> empty = Option.none();
			BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
			
			val result =  new ForComprehension2<Option<?>,Option<Integer>,Integer>()
								.<Integer,Integer>foreach(c -> c.flatMapAs$1(one)
																.mapAs$2(empty)
																.filter(()->c.$1()>2)
																.yield(()->{return f2.apply(c.$1(), 10);}));
			
			System.out.println(result);
			assertTrue(result.isNone());

		}
		@Test
		public void optionTestLessTyping(){
			Option<Integer> one = Option.some(1);
			Option<Integer> empty = Option.none();
			BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
			
			val result =  ForComprehensions
								.<Integer,Option<Integer>>foreach1(c -> c.flatMapAs$1(one)
																.mapAs$2(empty)
																.filter(()->c.<Integer>$1()>2)
																.yield(()->{return f2.apply(c.$1(), 10);}));
			
			System.out.println(result);
			assertTrue(result.isNone());

		}
	
}
