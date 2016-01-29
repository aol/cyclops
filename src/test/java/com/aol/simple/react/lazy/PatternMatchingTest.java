
package com.aol.simple.react.lazy;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import com.aol.cyclops.objects.Decomposable;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class PatternMatchingTest {

	@Test
	public void test(){
		List<String> result = LazyFutureStream.of(1,2,3,4)
											  .capture(e->e.printStackTrace())
											  .patternMatch("",
													  	c->c.hasValuesWhere( (Integer i)->i%2==0 ).then(i->"even"),
													  	c->c.hasValuesWhere( (Integer i)->i%2!=0).then(i->"odd")
													  )
											  .toList();
		assertThat(result,equalTo(Arrays.asList("odd","even","odd","even")));
	}
	@Test
	public void test2(){
		List<String> result = LazyFutureStream.of(1,2,3,4)
											  .capture(e->e.printStackTrace())
											  
											  .patternMatch("n/a",c->c.hasValues(1).then(i->"one"))
											  
											  .toList();
		assertThat(result,equalTo(Arrays.asList("one","n/a","n/a","n/a")));
	}
	@Test
	public void decomposable(){
		List<String> result = LazyFutureStream.of(new MyCase(1,2),new MyCase(3,4))
											  .capture(e->e.printStackTrace())
											  .patternMatch("n/a",
													  c->c.hasValues(1,2).then(i->"one"),
													  c->c.hasValues(3,4).then(i->"two"),
													  c->c.hasValues(1,4).then(i->"three"),
													  c->c.hasValues(2,3).then(i->"four")
													  
													  
													  )
											  .toList();
		assertThat(result,equalTo(Arrays.asList("one","two")));
	}
	@Test
	public void pojo(){
		
		List<String> result = LazyFutureStream.of(new MyCase2(1,2),new MyCase2(3,4))
											  .capture(e->e.printStackTrace())
											  .patternMatch("n/a",
													  c->c.hasValues(1,2).then(i->"one"),
													  c->c.hasValues(3,4).then(i->"two"),
													  c->c.hasValues(5,6).then(i->"three")
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
