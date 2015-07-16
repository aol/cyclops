package com.aol.cyclops.functionaljava;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.lambda.monads.AnyMonads;
import com.sun.net.httpserver.Authenticator.Failure;
import com.sun.net.httpserver.Authenticator.Success;

import fj.Monoid;
import fj.P1;
import fj.Try;
import fj.control.Trampoline;
import fj.data.Either;
import fj.data.IOFunctions;
import fj.data.IterableW;
import fj.data.List;
import fj.data.Option;
import fj.data.Reader;
import fj.data.State;
import fj.data.Stream;
import fj.data.Validation;
import fj.data.Writer;

public class AnyFunctionalJavaMTest {


	private String success(){
		return "hello world";
		
	}
	private String exceptional(){
		
		throw new RuntimeException();
	}
	@Test
	public void eitherTest(){
		assertThat(FJ.anyM(Either.right("hello world"))
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void eitherLeftTest(){
		assertThat(FJ.anyM(Either.<String,String>left("hello world"))
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void eitherFlatMapTest(){
		assertThat(FJ.anyM(Either.right("hello world"))
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void rightProjectionTest(){
		assertThat(FJ.anyM(Either.right("hello world").right())
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void rightProjectionLeftTest(){
		assertThat(FJ.anyM(Either.<String,String>left("hello world").right())
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void rightProjectionFlatMapTest(){
		assertThat(FJ.anyM(Either.right("hello world").right())
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void leftProjectionTest(){
		assertThat(FJ.anyM(Either.<String,String>left("hello world").right())
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList()));
	}
	
	@Test
	public void leftProjectionLeftTest(){
		assertThat(FJ.anyM(Either.<String,String>left("hello world").left())
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	
	@Test
	public void leftProjectionLeftFlatMapTest(){
		assertThat(FJ.anyM(Either.<String,String>left("hello world").left())
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	
	@Test
	public void optionTest(){
		assertThat(FJ.anyM(Option.some("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void optionFlatMapTest(){
		assertThat(FJ.anyM(Option.some("hello world"))
				.map(String::toUpperCase)
				.flatMapOptional(Optional::of)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void optionEmptyTest(){
		assertThat(FJ.anyM(Option.<String>none())
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void streamTest(){
		assertThat(FJ.anyM(Stream.stream("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	
	@Test
	public void listTest(){
		assertThat(FJ.anyM(List.list("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void iterableWTest(){
		assertThat(FJ.anyM(IterableW.wrap(Arrays.asList("hello world")))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void streamFlatMapTest(){
		assertThat(FJ.anyM(Stream.stream("hello world"))
				.map(String::toUpperCase)
				.flatMap(i->FJ.anyM(Stream.stream(i)))
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void streamFlatMapTestJDK(){
		assertThat(FJ.anyM(Stream.stream("hello world"))
				.map(String::toUpperCase)
				.flatMap(i->AnyMonads.anyM(java.util.stream.Stream.of(i)))
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	public String finalStage(){
		return "hello world";
	}
	@Test
	public void trampolineTest(){
		assertThat(FJ.anyM(Trampoline.suspend(new P1<Trampoline<String>>() {
					public Trampoline<String> _1(){
						return Trampoline.pure(finalStage());
					}
				}))
				.map(String::toUpperCase)
				.asSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void readerTest(){
		
		assertThat(FJ.unwrapReader(FJ.anyM(Reader.unit( (Integer a) -> "hello "+a ))
						.map(String::toUpperCase)).f(10),equalTo("HELLO 10"));
	}
	
	
	@Test
	public void validateTest(){
		assertThat(FJ.anyM(Validation.success(success()))
			.map(String::toUpperCase)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	
	@Test//(expected=javaslang.control.Failure.NonFatal.class)
	public void validationTestFailure(){
		
		FJ.anyM(Validation.fail(new RuntimeException()))
			.toSequence()
			.forEach(System.out::println);
		
	}
	
	@Test
	public void validateTestFailureProcess(){
		
		Exception e = new RuntimeException();
		assertThat(FJ.anyM(Validation.fail(e))
				.toSequence()
				.toList(),equalTo(Arrays.asList()));
		
	}
	
	@Test
	public void tryFlatMapTest(){
		assertThat(FJ.anyM(Validation.success(success()))
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test @Ignore
	public void writerFailingTest(){
		//even with types the same Writer isn't fully typesafe
		
		//failing FJ code
		Writer.unit("lower", "", Monoid.stringMonoid).map(a->a.length());
		
		
		//see -> can map from String to Int, breaking it
		System.out.println(FJ.anyM(Writer.unit("lower", "", Monoid.stringMonoid))
				.map(a->a.length()).<Writer<String,String>>unwrap().value());
				
		
	}
	@Test 
	public void writerUpperCaseTest(){
		
		
	
		assertThat(FJ.anyM(Writer.unit("lower", "", Monoid.stringMonoid))
				.map(String::toUpperCase).<Writer<String,String>>unwrap().value(),equalTo("LOWER"));
				
		
	}
	@Test 
	public void writerFlatMapTest(){
		
		
	
		assertThat(FJ.anyM(Writer.unit("lower", "", Monoid.stringMonoid))
				.flatMap(a->FJ.anyMValue(Writer.unit("hello",Monoid.stringMonoid)))
				.<Writer<String,String>>unwrap().value(),equalTo("hello"));
				
		
	}
	
	@Test
	public void stateTest(){
	
		
		assertThat(FJ.unwrapState(FJ.anyM(State.constant("hello"))
			.map(String::toUpperCase)).run("")._2()
				,equalTo("HELLO"));
	}
	@Test
	public void ioTest() throws IOException{
		
		
		FJ.unwrapIO( 
				FJ.anyM(IOFunctions.lazy(a->{ System.out.println("hello world"); return a;}))
				.map(a-> {System.out.println("hello world2"); return a;})   ).run();
		
	}
}
