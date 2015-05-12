package com.aol.cyclops.lambda.tuple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.cyclops.comprehensions.functions.QuintFunction;

public class AppyAsyncTest {
	@Test
	public void testConvertT1(){
		
		assertThat(methodTuple1().callAsync(this::method3).join(),equalTo("10"));
		
	}
	@Test
	public void testConvertT2(){
		
		assertThat(methodTuple2().callAsync(this::method2).join(),equalTo("10hello"));
		
	}
	@Test
	public void testConvertT3(){
		
		assertThat(methodTuple3().callAsync(this::method3params).join(),equalTo("10hello3"));
		
	}
	@Test
	public void testConvertT4(){
		
		assertThat(Tuple4.of(1,2,3,4).callAsync(this::method4params).join(),equalTo(10));
		
	}
	@Test
	public void testConvertT5(){
		
		assertThat(Tuples.tuple(1,2,3,4,5).callAsync(this::method5params).join(),equalTo(15));
		
	}
	@Test
	public void testConvertT6(){
		
		assertThat(Tuples.tuple(1,2,3,4,5,6).callAsync(this::method6params).join(),equalTo(21));
		
	}
	@Test
	public void testConvertT7(){
		
		assertThat(Tuples.tuple(1,2,3,4,5,6,7).callAsync(this::method7params).join(),equalTo(28));
		
	}
	@Test
	public void testConvertT8(){
		
		assertThat(Tuples.tuple(1,2,3,4,5,6,7,8).callAsync(this::method8params).join(),equalTo(36));
		
	}
	
	public Tuple1<Integer> methodTuple1(){
		return Tuples.tuple(10);
	}
	public Tuple2<Integer,String> methodTuple2(){
		return Tuples.tuple(10,"hello");
	}
	public Tuple3<Integer,String,Integer> methodTuple3(){
		return Tuples.tuple(10,"hello",3);
	}
	public String method3params(Integer number, String value,Integer num2){
		return "" + number + value + num2;
	}
	public String method2(Integer number, String value){
		return "" + number + value;
	}
	public String method3(Integer number){
		return "" + number;
	}
	public int method4params(int one,int two,int three, int four){
		return one+two+three+four;
	}
	public int method5params(int one,int two,int three, int four,int five){
		return one+two+three+four+five;
	}
	public int method6params(int one,int two,int three, int four,int five,int six){
		return one+two+three+four+five+six;
	}
	public int method7params(int one,int two,int three, int four,int five,int six,int seven){
		return one+two+three+four+five+six+seven;
	}
	public int method8params(int one,int two,int three, int four,int five,int six,int seven,int eight){
		return one+two+three+four+five+six+seven + eight;
	}
}
