package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher.Predicates.ANY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;

import org.junit.Test;

import com.aol.cyclops.matcher.builders.Matching;

public class DecomposableTest {
	@Test
	public void allValues(){
		assertThat(Matching._case().allValues(1,ANY(),2).thenApply(l->"case1")
			._case().allValues(1,3,2).thenApply(l->"case2")
			._case().bothTrue((Integer i)->i==1,(String s)->s.length()>0)
					.thenExtract(Extractors.<Integer,String>toTuple2())
					.thenApply(t->t.v1+t.v2)
			.unapply(new DecomposableObject(1,"hello",2)).get(),is("case1"));
		
		
	}
	
	
	@Test
	public void testDefaultMethod(){
		assertThat(new DefaultDecomposable(1,"hello",2).unapply(),is(new DecomposableObject(1,"hello",2).unapply()));
	}
	@AllArgsConstructor
	static class DecomposableObject implements Decomposable{
		private final int num;
		private final String name;
		private final int num2;
		@Override
		public List<? extends Object> unapply() {
			return Arrays.asList(num,name,num2);
		}
		
	}
	
	@Value static final class DefaultDecomposable implements Decomposable{ int num; String name; int num2;}
}
