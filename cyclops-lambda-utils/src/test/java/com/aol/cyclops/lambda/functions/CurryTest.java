package com.aol.cyclops.lambda.functions;

import java.util.function.BiFunction;

import org.junit.Test;

import com.aol.cyclops.comprehensions.functions.Curry;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
public class CurryTest {

	@Test
	public void testBiFunc() {
		BiFunction<Integer,Integer,String> fn= (i,j) -> "" + (i+j) + "hello";
		assertThat(Curry.curry2(fn).apply(1).apply(2),equalTo("3hello"));
	}
	@Test
	public void testBiFuncInPlace() {
		
		assertThat(Curry.curry2((Integer i, Integer j) -> "" + (i+j) + "hello").apply(1).apply(2),equalTo("3hello"));
	}
	@Test
	public void testMethodRef() {
		
		assertThat(Curry.curry2(this::mult).apply(3).apply(2),equalTo(6));
	}
	@Test
	public void testMethodRef3() {
		
		assertThat(Curry.curry3(this::three).apply(3).apply(2).apply("three"),equalTo("three6"));
	}
	@Test
	public void testMethodRef4() {
		
		assertThat(Curry.curry4(this::four).apply(3).apply(2).apply("three").apply("4"),equalTo("three64"));
	}
	@Test
	public void testMethodRef5() {
		
		assertThat(Curry.curry5(this::five).apply(3).apply(2).apply("three").apply("4").apply(true),equalTo("three64true"));
	}
	@Test
	public void testMethodRef6() {
		
		assertThat(Curry.curry6(this::six).apply(3).apply(2).apply("three").apply("4").apply(true).apply(10),equalTo("three164true"));
	}
	@Test
	public void testMethodRef7() {
		
		assertThat(Curry.curry7(this::seven).apply(3).apply(2).apply("three").apply("4").apply(true).apply(10).apply("prefix"),equalTo("prefixthree164true"));
	}
	@Test
	public void testMethodRef8() {
		
		assertThat(Curry.curry8(this::eight).apply(3).apply(2).apply("three").apply("4").apply(true).apply(10).apply("prefix").apply(false),equalTo("falseprefixthree164true"));
	}
	
	public Integer mult(Integer a,Integer b){
		return a*b;
	}
	public String three(Integer a,Integer b,String name){
		return name + (a*b);
	}
	public String four(Integer a,Integer b,String name,String postfix){
		return name + (a*b) + postfix;
	}
	public String five(Integer a,Integer b,String name,String postfix,boolean append){
		return name + (a*b) + postfix +append;
	}
	public String six(Integer a,Integer b,String name,String postfix,boolean append,int num){
		return name + ((a*b)+num) + postfix +append;
	}
	public String seven(Integer a,Integer b,String name,String postfix,boolean append,int num , String start){
		return start +name + ((a*b)+num) + postfix +append;
	}
	public String eight(Integer a,Integer b,String name,String postfix,boolean append,int num , String start,boolean willBeFalse){
		return ""+willBeFalse+start +name + ((a*b)+num) + postfix +append;
	}

}
