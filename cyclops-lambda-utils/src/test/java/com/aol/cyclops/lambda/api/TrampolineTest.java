package com.aol.cyclops.lambda.api;

import static org.junit.Assert.*;

import org.junit.Test;



public class TrampolineTest {

	@Test
	public void trampolineTest(){
		
		System.out.println(loop(5).result());
		
	}
	Trampoline<Integer> loop(int times){
		System.out.println(times);
		if(times==0)
			return Trampoline.done(100);
		else
			return Trampoline.more(()->loop(times-1));
	}
}
