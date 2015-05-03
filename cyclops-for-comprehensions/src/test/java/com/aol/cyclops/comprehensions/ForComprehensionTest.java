package com.aol.cyclops.comprehensions;


import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
	
	
public class ForComprehensionTest {
	
		@Test
		public void simple() {
			
			IntStream res = (IntStream)ForComprehension.foreach (  c-> {
				return c.$1(  IntStream.range(1,3)) 
				.yield( ()-> c.<Integer>$1() + 1);
				
			});
			List<Integer> expected = Arrays.asList(2,3);
			//def actual = []
			//res.forEach { n -> actual << n } as Consumer
			
			System.out.println(res);
			assertThat(expected, equalTo( res.boxed().collect(Collectors.toList())));
		}
	/**
		@Test
		void simpleLists() {
			def res = foreach {
				a {
					[1, 2]
				}
				yield {
					a + 1
				}
			}
			def expected = [2, 3]
			def actual = res
			assertTrue(expected == actual)
		}
	
		@Test
		void simpleListsWithShift() {
			def res = foreach {
				a << [1, 2]
				yield {
					a + 1
				}
			}
			def expected = [2, 3]
			def actual = res
			assertTrue(expected == actual)
		}
	
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
	
		@Test
		void test5() {
			def res = foreach {
				a << [some(0), some(1), some(2), none(), some(10)]
				guard {
					a.filter {
						it > 1
					}.isSome()
				}
				yield {
					a.map { it + 3 }
				}
			}
			def expected = [ some(5), some(13)]
			println res
			assertTrue (res == expected)
		}
	}
	**/
}
