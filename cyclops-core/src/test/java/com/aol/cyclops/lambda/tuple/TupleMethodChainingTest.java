package com.aol.cyclops.lambda.tuple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TupleMethodChainingTest {
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
	public void testConvertT4(){
		
		assertThat(PTuple4.of(1,2,3,4).call(this::method4params),equalTo(10));
		
	}
	@Test
	public void testConvertT5(){
		
		assertThat(PowerTuples.tuple(1,2,3,4,5).call(this::method5params),equalTo(15));
		
	}
	@Test
	public void testConvertT6(){
		
		assertThat(PowerTuples.tuple(1,2,3,4,5,6).call(this::method6params),equalTo(21));
		
	}
	@Test
	public void testConvertT7(){
		
		assertThat(PowerTuples.tuple(1,2,3,4,5,6,7).call(this::method7params),equalTo(28));
		
	}
	@Test
	public void testConvertT8(){
		
		assertThat(PowerTuples.tuple(1,2,3,4,5,6,7,8).call(this::method8params),equalTo(36));
		
	}
	@Test
	public void testConvert2(){
		methodTuple2().<PTuple1<Integer>>filter(t->t.v1()==0).call(this::method3);
		
	}
	@Test
	public void testConvert2Async(){
		methodTuple2().<PTuple1<Integer>>filter(t->t.v1()==0).callAsync(this::method3).join();
		
	}
	public PTuple1<Integer> methodTuple1(){
		return PowerTuples.tuple(10);
	}
	public PTuple2<Integer,String> methodTuple2(){
		return PowerTuples.tuple(10,"hello");
	}
	public PTuple3<Integer,String,Integer> methodTuple3(){
		return PowerTuples.tuple(10,"hello",3);
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
