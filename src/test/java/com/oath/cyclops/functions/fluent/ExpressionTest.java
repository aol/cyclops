package com.oath.cyclops.functions.fluent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import cyclops.data.tuple.Tuple;
import org.junit.Test;

import cyclops.function.FluentFunctions;
public class ExpressionTest {

	@Test
	public void testExpression(){
		assertThat(FluentFunctions.expression(System.out::println)
					   .apply("hello"),equalTo("hello"));


	}

	public void print(String input) throws IOException{
		System.out.println(input);
	}

	@Test
	public void testCheckedExpression(){
		assertThat(FluentFunctions.checkedExpression(this::print)
				   .apply("hello"),equalTo("hello"));
	}

	public void withTwo(Integer a,Integer b){
		System.out.println(a+b);
	}
	@Test
	public void testBiExpression(){
		assertThat(FluentFunctions.expression(this::withTwo)
					   .apply(1,2),equalTo(Tuple.tuple(1,2)));


	}
	public void printTwo(String input1,String input2) throws IOException{
		System.out.println(input1);
		System.out.println(input2);
	}
	@Test
	public void testCheckedBiExpression(){
		assertThat(FluentFunctions.checkedExpression(this::printTwo)
					   .apply("hello","world"),equalTo(Tuple.tuple("hello","world")));


	}

}
