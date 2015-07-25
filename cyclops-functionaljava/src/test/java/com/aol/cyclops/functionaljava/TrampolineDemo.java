package com.aol.cyclops.functionaljava;

import java.util.List;

import org.junit.Test;

import fj.control.Trampoline;

public class TrampolineDemo {
	@Test
	public  void trampolineDemo(){
		
		
		List<String> list = FJ.anyM(FJ.Trampoline.suspend(() -> Trampoline.pure("hello world")))
								.map(String::toUpperCase)
								.asSequence()
								.toList();
		
		
		System.out.println(list);
		
		
	}
	
	
}
