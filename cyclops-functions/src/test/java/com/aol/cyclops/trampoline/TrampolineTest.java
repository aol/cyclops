package com.aol.cyclops.trampoline;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import lombok.val;

import org.junit.Ignore;
import org.junit.Test;


public class TrampolineTest {

	@Test
	public void trampolineTest(){
		
		assertThat(loop(500000,10).result(),equalTo(446198426));
		
	}
	Trampoline<Integer> loop(int times,int sum){
		
		if(times==0)
			return Trampoline.done(sum);
		else
			return Trampoline.more(()->loop(times-1,sum+times));
	}
	
	@Test @Ignore
	public void trampolineTest1(){
		
		assertThat(loop1(500000,10),equalTo(446198426));
		
	}
	Integer loop1(int times,int sum){
		
		if(times==0)
			return sum;
		else
			return loop1(times-1,sum+times);
	}
	
	
	
	List results;
	@Test
	public void coroutine(){
		results = new ArrayList();
		Iterator<String> it = Arrays.asList("hello","world","end").iterator();
		val coroutine = new Trampoline[1];
		coroutine[0] = Trampoline.more( ()-> it.hasNext() ? print(it.next(),coroutine[0]) : Trampoline.done(0));
		withCoroutine(coroutine[0]);
		
		assertThat(results,equalTo(Arrays.asList(0,"hello",1,"world",2,"end",3,4)));
	}
	
	private Trampoline<Integer> print(Object next, Trampoline trampoline) {
		System.out.println(next);
		results.add(next);
		return trampoline;
	}
	public void withCoroutine(Trampoline coroutine){
		
		for(int i=0;i<5;i++){
				print(i,coroutine);
				if(!coroutine.complete())
					coroutine= coroutine.bounce();
				
		}
		
	}
}
