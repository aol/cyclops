package com.aol.cyclops.matcher;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.aol.cyclops.lambda.types.Decomposable;

import lombok.AllArgsConstructor;
import lombok.Value;

public class DecomposableTest {

	
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
