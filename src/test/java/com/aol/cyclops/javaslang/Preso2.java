package com.aol.cyclops.javaslang;

import static com.googlecode.totallylazy.collections.PersistentList.constructors.list;
import static com.googlecode.totallylazy.collections.PersistentMap.constructors.map;

import java.util.Date;
import java.util.Iterator;

import lombok.val;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.junit.Test;

import com.googlecode.totallylazy.Trampoline;


public class Preso2 {

	@Test
	public void coroutine(){
		Iterator<String> it = list("hello","world","end").iterator();
		val coroutine = new Trampoline[1];
		coroutine[0] = Trampoline.more( ()-> it.hasNext() ? print(it.next(),coroutine[0]) : Trampoline.done(0));
		withCoroutine(coroutine[0]);
	}
	
	private Trampoline<Integer> print(String next, Trampoline trampoline) {
		System.out.println(next);
		return trampoline;
	}
	public void withCoroutine(Trampoline coroutine){
		
		for(int i=0;i<10;i++){
				System.out.println(i);		
				if(!coroutine.done())
					coroutine= coroutine.next();
				
		}
		
	}
	
	
	@Test
	public void trampolineTest(){
		
		loop(5).get();
		
	}
	Trampoline<Integer> loop(int times){
		System.out.println(times);
		if(times==0)
			return Trampoline.done(100);
		else
			return Trampoline.more(()->loop(times-1));
	}
	
	
	
	@Test
	public void recursionTest(){
		
		loop(5);
		
		
	}
	Integer recLoop(int times){
		System.out.println(times);
		if(times==0)
			return 100;
		else
			return recLoop(times-1);
	}
	
	@Test
	public void totallyLazy(){
		
		
		val list = list(10l,30l,40l);
		val map = map(1l,"hello",2l,"world");
		
		list.append(50l).forEach(System.out::println);
		
		map.insert(100l, "new").entrySet().forEach(System.out::println);
		
		
	}
	
	@Test
	public void tuple(){
		
		
		val tuple = Tuple.tuple("hello",1l,new Date());
		
		System.out.println(tuple.v1());
		System.out.println(tuple.v2());
		System.out.println(tuple.v3());
		
		tuple.map((s,l,d) -> "world").toLowerCase();
		
		Tuple3<String, Long, Date> tuple2 = Tuple.tuple("hello",1l,new Date());
		
		
		
		
	}
}
