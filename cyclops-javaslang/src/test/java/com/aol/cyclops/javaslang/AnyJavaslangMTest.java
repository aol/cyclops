package com.aol.cyclops.javaslang;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javaslang.Lazy;
import javaslang.collection.Array;
import javaslang.collection.CharSeq;
import javaslang.collection.HashSet;
import javaslang.collection.List;
import javaslang.collection.Queue;
import javaslang.collection.Stack;
import javaslang.collection.Stream;
import javaslang.collection.Vector;
import javaslang.concurrent.Future;
import javaslang.control.Failure;
import javaslang.control.Left;
import javaslang.control.Option;
import javaslang.control.Right;
import javaslang.control.Success;
import javaslang.control.Try;
import javaslang.test.Arbitrary;
import javaslang.test.Gen;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.monad.AnyMonads;

public class AnyJavaslangMTest {

	@Test
	public void testToList(){
		
		assertThat(Javaslang.anyM(List.of(1,2,3)).toList(), equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void monadTest(){
		assertThat(Javaslang.anyMonad(Try.of(this::success))
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void tryTest(){
		assertThat(Javaslang.anyM(Try.of(this::success))
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test//(expected=javaslang.control.Failure.NonFatal.class)
	public void tryTestFailure(){
		
		Javaslang.anyM(new Failure(new RuntimeException()))
			.toSequence()
			.forEach(System.out::println);
		
	}
	@Test
	public void tryTestFailureProcess(){
		
		Exception e = new RuntimeException();
		assertThat(Javaslang.anyMFailure(new Failure(e))
				.toSequence()
				.toList(),equalTo(Arrays.asList(e)));
		
	}
	@Test
	public void whenSuccessFailureProcessDoesNothing(){
		
		assertThat(Javaslang.anyMFailure(new Success("hello world"))
											.toSequence()
											.toList(),equalTo(Arrays.asList()));
			
		
	}
	@Test
	public void tryFlatMapTest(){
		assertThat(Javaslang.anyM(Try.of(this::success))
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}

	private String success(){
		return "hello world";
		
	}
	private String exceptional(){
		
		throw new RuntimeException();
	}
	@Test
	public void eitherTest(){
		assertThat(Javaslang.anyM(new Right<Object,String>("hello world"))
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void eitherLeftTest(){
		assertThat(Javaslang.anyM(new Left<String,String>("hello world"))
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void eitherFlatMapTest(){
		assertThat(Javaslang.anyM(new Right<Object,String>("hello world"))
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void rightProjectionTest(){
		assertThat(Javaslang.anyM(new Right<Object,String>("hello world").right())
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void rightProjectionLeftTest(){
		assertThat(Javaslang.anyM(new Left<String,String>("hello world").right())
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void rightProjectionFlatMapTest(){
		assertThat(Javaslang.anyM(new Right<Object,String>("hello world").right())
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void leftProjectionTest(){
		assertThat(Javaslang.anyM(new Left<String,String>("hello world").right())
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList()));
	}
	
	@Test
	public void leftProjectionLeftTest(){
		assertThat(Javaslang.anyM(new Left<String,String>("hello world").left())
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	
	@Test
	public void leftProjectionLeftFlatMapTest(){
		assertThat(Javaslang.anyM(new Left<String,String>("hello world").left())
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	
	@Test
	public void optionTest(){
		assertThat(Javaslang.anyM(Option.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void optionFlatMapTest(){
		assertThat(Javaslang.anyM(Option.of("hello world"))
				.map(String::toUpperCase)
				.flatMapOptional(Optional::of)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void optionEmptyTest(){
		assertThat(Javaslang.anyM(Option.<String>none())
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void futureTest(){
		assertThat(Javaslang.anyMonad(Future.of(()->"hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void futureFlatMapTest(){
		assertThat(Javaslang.anyMonad(Future.of(()->"hello world"))
				.map(String::toUpperCase)
				.flatMapOptional(Optional::of)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	
	@Test
	public void lazyTest(){
		assertThat(Javaslang.anyMonad(Lazy.of(()->"hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void lazyFlatMapTest(){
		assertThat(Javaslang.anyMonad(Lazy.of(()->"hello world"))
				.map(String::toUpperCase)
				.flatMapOptional(Optional::of)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void streamTest(){
		assertThat(Javaslang.anyM(Stream.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test @Ignore
	public void arbritrayTest(){
		assertThat(Javaslang.anyM(Arbitrary.list(Gen.of("hello world").arbitrary()))
			//	.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void listTest(){
		assertThat(Javaslang.anyM(List.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void streamFlatMapTest(){
		assertThat(Javaslang.anyM(Stream.of("hello world"))
				.map(String::toUpperCase)
				.flatMap(i->Javaslang.anyM(List.of(i)))
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void streamFlatMapTestJDK(){
		assertThat(Javaslang.anyM(Stream.of("hello world"))
				.map(String::toUpperCase)
				.flatMap(i->AnyM.fromStream(java.util.stream.Stream.of(i)))
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void arrayTest(){
		assertThat(Javaslang.anyMonad(Array.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void charSeqTest(){
		assertThat(Javaslang.anyMonad(CharSeq.of("hello world"))
				.map(c->c.toString().toUpperCase().charAt(0))
				.toSequence()
				.join(),equalTo("HELLO WORLD"));
	}
	@Test
	public void hashsetTest(){
		assertThat(Javaslang.anyMonad(HashSet.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void queueTest(){
		assertThat(Javaslang.anyMonad(Queue.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void stackTest(){
		assertThat(Javaslang.anyMonad(Stack.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void vectorTest(){
		assertThat(Javaslang.anyMonad(Vector.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
}
