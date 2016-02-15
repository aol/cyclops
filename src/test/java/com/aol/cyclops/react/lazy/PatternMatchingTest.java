
package com.aol.cyclops.react.lazy;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.aol.cyclops.react.stream.traits.LazyFutureStream;
import com.aol.cyclops.types.Decomposable;

import lombok.AllArgsConstructor;

public class PatternMatchingTest {

	@Test
	public void test(){
		List<String> result = LazyFutureStream.of(1,2,3,4)
											  .capture(e->e.printStackTrace())
											  .patternMatch("",
													  	c->c.where(i->"even", (Integer i)->i%2==0 )
													  		.hasWhere(i->"odd", (Integer i)->i%2!=0)
													  )
											  .toList();
		assertThat(result,equalTo(Arrays.asList("odd","even","odd","even")));
	}
	@Test
	public void test2(){
		List<String> result = LazyFutureStream.of(1,2,3,4)
											  .capture(e->e.printStackTrace())
											  
											  .patternMatch("n/a",c->c.values(i->"one",1))
											  
											  .toList();
		assertThat(result,equalTo(Arrays.asList("one","n/a","n/a","n/a")));
	}
	@Test
	public void decomposable(){
		List<String> result = LazyFutureStream.of(new MyCase(1,2),new MyCase(3,4))
											  .capture(e->e.printStackTrace())
											  .patternMatch("n/a",
													  c->c.values(i->"one",1,2)
													  	  .has(i->"two",3,4)
													  	  .has(i->"three",1,4)
													  	   .has(i->"four",2,3)
													  
													  
													  )
											  .toList();
		assertThat(result,equalTo(Arrays.asList("one","two")));
	}
	@Test
	public void pojo(){
		
		List<String> result = LazyFutureStream.of(new MyCase2(1,2),new MyCase2(3,4))
											  .capture(e->e.printStackTrace())
											  .patternMatch("n/a",
													  c->c.values(i->"one",1,2)
													  .has(i->"two",3,4)
													  .has(i->"three",5,6)
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
