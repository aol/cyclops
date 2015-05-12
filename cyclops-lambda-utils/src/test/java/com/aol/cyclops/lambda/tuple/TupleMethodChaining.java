package com.aol.cyclops.lambda.tuple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TupleMethodChaining {
	@Test
	public void testConvertT1(){
		
		assertThat(methodTuple1().call(this::method3),equalTo("10"));
		
	}
	@Test
	public void testConvertT2(){
		
		assertThat(methodTuple2().call(this::method2),equalTo("10hello"));
		
	}
	@Test
	public void testConvertT3(){
		
		assertThat(methodTuple3().call(this::method3params),equalTo("10hello3"));
		
	}
	@Test
	public void testConvert2(){
		methodTuple2().<Tuple1<Integer>>filter(t->t.v1()==0).call(this::method3);
		
	}
	@Test
	public void testConvert2Async(){
		methodTuple2().<Tuple1<Integer>>filter(t->t.v1()==0).callAsync(this::method3).join();
		
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
}
