package com.aol.simple.react.async.future;

import static org.junit.Assert.*;
import io.netty.util.internal.chmv8.ForkJoinPool;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FastFutureTest {
	FastFuture<String> future;
	
	@Before
	public void setup(){
		future = new FastFuture<>();
		sizes = new ArrayList<>();
		sizes2 = new ArrayList<>();
		sizes3 = new ArrayList<>();
	}
	
	List<Integer> sizes;
	List<Integer> sizes2;
	List<Integer> sizes3;
	@Test
	public void testThenApplyAsync() {
		try{
			future = future.thenApplyAsync(String::toUpperCase, ForkJoinPool.commonPool())
						.peek(System.out::println);
			FastFuture<Integer> next = future.thenApply(s->s.length())
			.thenApply(l->{ sizes.add(l); return l;})
				.peek(System.out::println);
			
			
			next = next.thenApplyAsync(l->l+2, ForkJoinPool.commonPool())
					.peek(System.out::println)
			.thenApply(l->{ sizes2.add(l); return l;})
			.thenApplyAsync(l->l+2, ForkJoinPool.commonPool())
			.peek(System.out::println)
			
			.thenApply(l->{ sizes3.add(l); return l;});
			StringBuilder suffix = new StringBuilder();
			for(int i=0;i<100;i++){
				
				FastFuture f2 = next.build();
				System.out.println(f2.pipeline);
				f2.set("hello world" + suffix.toString());
				f2.join();
				FastFuture f3 = next.build();
				f3.set("hello world2"+ suffix.toString());
				f3.join();
				suffix.append(""+i);
			}
			for(int i=0;i<11;i++){
				assertFalse(sizes.contains(i));
			}
			for(int i=11;i<201;i++){
				assertTrue(sizes.contains(i));
			}
			for(int i=201;i<211;i++){
				assertFalse(sizes.contains(i));
			}
			System.out.println(sizes);
			System.out.println(sizes2);
			System.out.println(sizes3);
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	

}
