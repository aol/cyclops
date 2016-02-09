package com.aol.cyclops.control;

import org.junit.Test;

public class XorTest {

	private String concat(String a,String b){
		return a+b;
	}
	@Test
	public void test() {
		Xor<String,String> fail1 = Xor.secondary("failed1");
		String s = fail1.swap().ap2(this::concat).ap(Xor.secondary("failed2").swap()).get();
		System.out.println(s);
	}
	@Test
	public void test2() {
		
		Xor<String,String> fail1 = Xor.secondary("failed1");
		System.out.println(fail1);
		Xor<String,String> s = fail1.swap().ap2(this::concat).ap(Xor.<String,String>primary("success").swap()).toXorSecondary();
		System.out.println(s);
		System.out.println(s.swap().get());
	}

}
