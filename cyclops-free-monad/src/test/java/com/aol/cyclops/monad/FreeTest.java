package com.aol.cyclops.monad;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Value;

import org.junit.Test;

import com.aol.cyclops.comprehensions.ForComprehensions;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Vars2;
import com.aol.cyclops.comprehensions.LessTypingForComprehension3.Vars3;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.LiftableFunctor;
import com.aol.cyclops.lambda.monads.MonadWrapper;

public class FreeTest {
	@Test
	public void testFreeList2(){
		Stream<Integer> ints = ForComprehensions.foreach2(c -> 
						c.flatMapAs$1(lift(1,2,3))
						.mapAs$2((Vars2<Integer,Integer> v)->lift(v.$1(),v.$1()*2))
						.yield(v-> v.$1()));
		
		ints.forEach(System.out::println);
		
	}
	@Test
	public void testFreeList1(){
		Stream<Integer> ints = ForComprehensions.foreach1(c -> c.mapAs$1(lift(1,2,3))
										
										.yield(v-> v.$1()));
		
		ints.forEach(System.out::println);
		
	}
	@Test
	public void testFreeList(){
		ForComprehensions.foreach3(c -> c.flatMapAs$1(lift(1,2,3))
										.flatMapAs$2((Vars3<Integer,Integer,Integer> v)->lift(v.$1(),2*v.$1()))
										.mapAs$3(v->lift())
										.yield(v-> v.$1()+v.$2()+v.$3()));
		
	/**	MonadWrapper.<Integer,Stream<Integer>>of(lift(1,2,3))
					.<Stream<Integer>,Integer>flatMap(  a-> MonadWrapper.<Integer,Stream<Integer>>of(lift(a,a*2))
							.<Stream<Integer>,Integer>flatMap(b->  lift(1).map( c -> a+b+c)).unwrap()).<Stream>unwrap().forEach(System.out::println);**/
	}
	@Test
	public void testList(){
		MonadWrapper.<Integer,Stream<Integer>>of(list(1,2,3))
					.<Stream<Integer>,Integer>flatMap(  a-> MonadWrapper.<Integer,Stream<Integer>>of(list(a,a*2).stream())
							.<Stream<Integer>,Integer>flatMap(b->  Stream.<Integer>of(1).map( c -> a+b+c)).unwrap()).<Stream>unwrap().forEach(System.out::println);
	}
	private List<Integer> list(Integer... is) {
		return Arrays.<Integer>asList(is);
	}
	private Free lift(Integer... is) {
		
		return Free.suspend(new MyFunctor((Free.ret(Stream.of(is)))));
	//	return Free.liftF(Stream.of(is));
		//return Free.liftF(new LiftableWrapper(Stream.of(is)));
	}
	@Value
	static class MyFunctor implements Functor{
		Object functor;
	}
	@Test
	public void test(){
		//Free.liftF(new Box("banana"));
	 Free.suspend(new Box(Free.ret(new Box("banana")))).flatMap(banana ->Free.ret("banana-peel"));	
	}
	@Value
	static class Box<A> implements LiftableFunctor<A,A,Box<A>>{
		A a;
		public static <A> Free<Functor<?>,A> liftF(Functor<A> f){
			return Free.suspend(new Box(Free.ret(f)));
		}
		public <B> Box<B> map(Function<A,B> fn){
			return new Box(fn.apply(a));
		}
		/**@Override
		public <B> Box<B> of(B current) {
			return new Box(current);
		}**/
		/**
		@Override
		public  Box<A> of(A current) {
			return new Box(current);
		}
		**/

	}
}
