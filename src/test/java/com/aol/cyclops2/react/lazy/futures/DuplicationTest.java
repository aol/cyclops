package com.aol.cyclops2.react.lazy.futures;

import static com.aol.cyclops2.react.lazy.DuplicationTest.of;
import static org.junit.Assert.assertTrue;

import cyclops.stream.FutureStream;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple4;
import org.junit.Test;
public class DuplicationTest {
	@Test
	public void testDuplicate(){
		 Tuple2<FutureStream<Integer>, FutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().duplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
	}
	@Test
	public void testTriplicate(){
		 Tuple3<FutureStream<Integer>, FutureStream<Integer>, FutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().triplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
		 assertTrue(copies._3().anyMatch(i->i==2));
	}
	
	@Test
	public void testQuadriplicate(){
		 Tuple4<FutureStream<Integer>, FutureStream<Integer>, FutureStream<Integer>,FutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().quadruplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
		 assertTrue(copies._3().anyMatch(i->i==2));
		 assertTrue(copies._4().anyMatch(i->i==2));
	}

	@Test
	public void testDuplicateFilter(){
		 Tuple2<FutureStream<Integer>, FutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().duplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testTriplicateFilter(){
		Tuple3<FutureStream<Integer>, FutureStream<Integer>, FutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().triplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._3().filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateFilter(){
		 Tuple4<FutureStream<Integer>, FutureStream<Integer>, FutureStream<Integer>,FutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().quadruplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._3().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._4().filter(i->i%2==0).toList().size()==3);
	}
	@Test
	public void testDuplicateLimit(){
		 Tuple2<FutureStream<Integer>, FutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().duplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
	} 
	@Test
	public void testTriplicateLimit(){
		Tuple3<FutureStream<Integer>, FutureStream<Integer>, FutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().triplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
		 assertTrue(copies._3().limit(3).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateLimit(){
		 Tuple4<FutureStream<Integer>, FutureStream<Integer>, FutureStream<Integer>,FutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().quadruplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
		 assertTrue(copies._3().limit(3).toList().size()==3);
		 assertTrue(copies._4().limit(3).toList().size()==3);
	}
}
