package com.aol.cyclops.lambda.monads;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Value;
import lombok.val;
import lombok.experimental.Wither;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.lambda.api.AsGenericMonad;
public class MonadTest {

	@Test
	public void test() {
		val list = MonadWrapper.<Stream<Integer>,List<Integer>>of(Stream.of(Arrays.asList(1,3)))
				.flatMap(Collection::stream).unwrap()
				.map(i->i*2)
				.peek(System.out::println)
				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void testMixed() {
		
		List<Integer> list = MonadWrapper.<Stream<Integer>,List<Integer>>of(Stream.of(Arrays.asList(1,3),null))
				.bind(Optional::ofNullable)
				.map(i->i.size())
				.peek(System.out::println)
				.toList();
		assertThat(Arrays.asList(2),equalTo(list));
	}
	@Test
	public void testCycle(){
		assertThat(MonadWrapper.<Integer,Stream<Integer>>of(Stream.of(1,2,2))
											.cycle(3).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	}
	@Test
	public void testJoin(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,2)).map(b-> Stream.of(b)).flatten().toList(),equalTo(Arrays.asList(1,2,2)));
	}
	@Test
	public void testToSet(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,2)).toSet().size(),equalTo(2));
	}
	@Test
	public void testToList(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,3)).toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testToListFlatten(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,3,null)).bind(Optional::ofNullable).toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testToListOptional(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Optional.of(1)).toList(),equalTo(Arrays.asList(1)));
	}
	
	@Test
	public void testLift(){
		
		
		List<String> result = AsGenericMonad.<Stream<String>,String>asMonad(Stream.of("input.file"))
								.map(getClass().getClassLoader()::getResource)
							//	.peek(System.out::println)
								.map(URL::getFile)
								.<Stream<String>,String>liftAndbind(File::new)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	

}
