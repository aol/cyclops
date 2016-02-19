package com.aol.cyclops.react.lazy.futures;

import static com.aol.cyclops.types.futurestream.LazyFutureStream.of;
import static org.junit.Assert.assertTrue;

import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Test;

import com.aol.cyclops.types.futurestream.LazyFutureStream;
public class DuplicationTest {
	@Test
	public void testDuplicate(){
		 Tuple2<LazyFutureStream<Integer>, LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().duplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
	}
	@Test
	public void testTriplicate(){
		 Tuple3<LazyFutureStream<Integer>, LazyFutureStream<Integer>, LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().triplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
		 assertTrue(copies.v3.anyMatch(i->i==2));
	}
	
	@Test
	public void testQuadriplicate(){
		 Tuple4<LazyFutureStream<Integer>, LazyFutureStream<Integer>, LazyFutureStream<Integer>,LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().quadruplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
		 assertTrue(copies.v3.anyMatch(i->i==2));
		 assertTrue(copies.v4.anyMatch(i->i==2));
	}

	@Test
	public void testDuplicateFilter(){
		 Tuple2<LazyFutureStream<Integer>, LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().duplicate();
		 assertTrue(copies.v1.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v2.filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testTriplicateFilter(){
		Tuple3<LazyFutureStream<Integer>, LazyFutureStream<Integer>, LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().triplicate();
		 assertTrue(copies.v1.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v2.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v3.filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateFilter(){
		 Tuple4<LazyFutureStream<Integer>, LazyFutureStream<Integer>, LazyFutureStream<Integer>,LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().quadruplicate();
		 assertTrue(copies.v1.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v2.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v3.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v4.filter(i->i%2==0).toList().size()==3);
	}
	@Test
	public void testDuplicateLimit(){
		 Tuple2<LazyFutureStream<Integer>, LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().duplicate();
		 assertTrue(copies.v1.limit(3).toList().size()==3);
		 assertTrue(copies.v2.limit(3).toList().size()==3);
	} 
	@Test
	public void testTriplicateLimit(){
		Tuple3<LazyFutureStream<Integer>, LazyFutureStream<Integer>, LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().triplicate();
		 assertTrue(copies.v1.limit(3).toList().size()==3);
		 assertTrue(copies.v2.limit(3).toList().size()==3);
		 assertTrue(copies.v3.limit(3).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateLimit(){
		 Tuple4<LazyFutureStream<Integer>, LazyFutureStream<Integer>, LazyFutureStream<Integer>,LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().quadruplicate();
		 assertTrue(copies.v1.limit(3).toList().size()==3);
		 assertTrue(copies.v2.limit(3).toList().size()==3);
		 assertTrue(copies.v3.limit(3).toList().size()==3);
		 assertTrue(copies.v4.limit(3).toList().size()==3);
	}
}
