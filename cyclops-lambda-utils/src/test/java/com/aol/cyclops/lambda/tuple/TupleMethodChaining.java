package com.aol.cyclops.lambda.tuple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.cyclops.lambda.tuple.TupleMethodsTest.TwoParams;

public class TupleMethodChaining {
	@Test
	public void testConvert(){
		
		System.out.println(method1().call(this::method2));
		
	}
	public Tuple2<Integer,String> method1(){
		return Tuples.tuple(10,"hello");
	}
	
	public String method2(Integer number, String value){
		return "" + number + value;
	}
}
