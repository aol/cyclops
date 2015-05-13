package com.aol.cyclops.monad;

import static com.aol.cyclops.comprehensions.ForComprehensions.foreach1;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Value;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.comprehensions.ForComprehensions;
import com.aol.cyclops.comprehensions.LessTypingForComprehension1.Vars1;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Vars2;
import com.aol.cyclops.comprehensions.LessTypingForComprehension3.Vars3;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.LiftableFunctor;
import com.aol.cyclops.lambda.monads.MonadWrapper;

public class FreeTest {
	@Test
	public void testFreeList2(){
		Free<Functor,Stream<Free.Return<Integer,Functor>>> result = ForComprehensions.foreach2(c -> 
						c.flatMapAs$1(lift(1))
						.mapAs$2((Vars2<Integer[],Integer[]> v)->lift(v.$1()))
						.yield(v-> v.$1()));
		
		System.out.println("Streaming the result!");
	//	result.result().forEach(System.out::println);
		System.out.println(result);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFreeList1(){
				
		Free<Functor,Stream<Free.Return<Integer,Functor>>> result = ForComprehensions.foreach1(c -> c.mapAs$1(lift(1,2,3))
										
										.yield(v-> printObject(v.$1())));
		
		System.out.println("Streaming the result!");
		result.result().forEach(System.out::println);
		//ints.forEach(System.out::println);
		
	}
	@Test
	public void testFreeOptional1(){
				
		Free<Functor,Optional<Free.Return<Integer,Functor>>> free =
							ForComprehensions.foreach1(c -> c.mapAs$1(liftOptional(1))
										.yield((Vars1<Integer> v)-> v.$1()+1));
		
		assertThat(free.result().get().result(),equalTo(2));
		
		
		
	}
	@Test
	public void testFreeOptionalNull(){
				
		Free<Functor,Optional<Free.Return<Integer,Functor>>> free =
							ForComprehensions.foreach1(c -> c.mapAs$1(liftOptional(null))
										.yield((Vars1<Integer> v)-> v.$1()+1));
		
		assertThat(free.result(),equalTo(Optional.empty()));
		
		
		
	}
	private Object printObject(Object i){
		System.out.println("i " + i);
		return i;
	}
	
	private Integer print(Integer i){
		System.out.println(i);
		return i;
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
	@Test @Ignore
	public void testList(){
		MonadWrapper.<Integer,Stream<Integer>>of(list(1,2,3))
					.<Stream<Integer>,Integer>flatMap(  a-> MonadWrapper.<Integer,Stream<Integer>>of(list(a,a*2).stream())
							.<Stream<Integer>,Integer>flatMap(b->  Stream.<Integer>of(1).map( c -> a+b+c)).unwrap()).<Stream>unwrap().forEach(System.out::println);
	}
	private List<Integer> list(Integer... is) {
		return Arrays.<Integer>asList(is);
	}
	private Free liftOptional(Object o) {
		if(o == null)
			return  Free.suspend(new MyFunctor(Optional.empty()));
		return Free.suspend(new MyFunctor(Optional.of(Free.ret(o))));
	}
	private Free lift(List is) {
		return Free.suspend(new MyFunctor(Stream.of(Free.ret(Arrays.asList(is)))));
	}
	private Free lift(Integer... is) {
		return Free.suspend(new MyFunctor(Stream.of(Free.ret(is))));
		//return Free.suspend(new MyFunctor(Free.ret(new MyFunctor(Stream.of(is)))));
		//return Free.suspend(new MyFunctor(Stream.of(Free.ret(is))));
	//	return Free.liftF(Stream.of(is));
		//return Free.liftF(new LiftableWrapper(Stream.of(is)));
	}
	@Value
	static class MyFunctor implements Functor{
		@Override
		public Functor map(Function fn) {
			Object result = Functor.super.map(fn);
			System.out.println("result " + result);
			return new  MyFunctor(result);
		}

		public <T> T unwrapToT(){
			if(functor instanceof Functor)
				return (T)((Functor) functor).unwrap();
			else
				return (T) functor;
		}
 		Object functor;
	}
	@Test
	public void test(){
		System.out.println(Free.liftF(new Box("banana")).flatMap(banana->Free.ret(banana+"-peel")));
	 System.out.println(Free.suspend(new Box(Free.ret(new Box("banana")))).flatMap(banana ->Free.ret("banana-peel")));	
	}
	@Test
	public void test2(){
		assertThat( foreach1(c -> c.mapAs$1(Box.liftF("banana"))
				
				.yield(v-> v.$1()+"-peel").toString()),containsString("banana-peel"));
	}
	@Value
	static class Box<A> implements LiftableFunctor<A,A,Box<A>>{
		A a;
		public static <A> Free<Functor<?>,A> liftF(A f){
			return Free.suspend(new Box(Free.ret(f)));
		//	return Free.suspend(new Box(f));
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
