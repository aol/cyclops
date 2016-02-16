
package com.aol.cyclops.react.lazy;
import static com.aol.cyclops.control.Matchable.otherwise;
import static com.aol.cyclops.control.Matchable.then;
import static com.aol.cyclops.control.Matchable.when;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.util.function.Predicates;

import lombok.AllArgsConstructor;

public class PatternMatchingTest {

	@Test
	public void test(){
		List<String> result = LazyFutureStream.of(1,2,3,4)
											  .capture(e->e.printStackTrace())
											  .patternMatch(
													  	c->c.is(when((Integer i)->i%2==0 ),then("even"))
													  		.is(when((Integer i)->i%2!=0 ),then("odd"))
													  ,Matchable.otherwise(""))
											  .toList();
		assertThat(result,equalTo(Arrays.asList("odd","even","odd","even")));
	}
	@Test
	public void test2(){
		List<String> result = LazyFutureStream.of(1,2,3,4)
											  .capture(e->e.printStackTrace())
											  .patternMatch(c->c.is(when(1),then("one")),otherwise("n/a"))
											  .toList();
		assertThat(result,equalTo(Arrays.asList("one","n/a","n/a","n/a")));
	}
	@Test
	public void decomposable(){
		List<String> result = LazyFutureStream.of(new MyCase(1,2),new MyCase(3,4))
											  .capture(e->e.printStackTrace())
											  .patternMatch(
													  c->c.is( when(Predicates.type(MyCase.class).isGuard(1,2)), then("one") )
													   	   .is(when(Predicates.type(MyCase.class).isGuard(3,4)),then(()->"two"))
													   	   .is(when(Predicates.type(MyCase.class).isGuard(1,4)),then(()->"three"))
													   	   .is(when(Predicates.type(MyCase.class).isGuard(2,4)),then(()->"four"))
													   	,otherwise("n/a"))
											  .toList();
		assertThat(result,equalTo(Arrays.asList("one","two")));
	}
	@Test
	public void pojo(){
		
		List<String> result = LazyFutureStream.of(new MyCase2(1,2),new MyCase2(3,4))
											  .capture(e->e.printStackTrace())
											  .patternMatch(
													  c->c.is(when(Predicates.type(MyCase.class).isGuard(1,2)), then("one") )
													  	  .is(when(Predicates.type(MyCase.class).isGuard(3,4)),then(()->"two"))
													  	  .is(when(Predicates.type(MyCase.class).isGuard(5,6)),then(()->"three"))
													  	,otherwise("n/a")
													  )
											  .toList();
		assertThat(result,equalTo(Arrays.asList("one","two")));
	}
	@AllArgsConstructor
	static class MyCase implements Decomposable{
		int first;
		int second;
	}
	@AllArgsConstructor
	static class MyCase2 {
		int first;
		int second;
	}
}
