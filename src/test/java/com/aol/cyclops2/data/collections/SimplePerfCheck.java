package com.aol.cyclops2.data.collections;

import java.util.ArrayList;
import java.util.List;

import cyclops.collections.immutable.LinkedListX;
import org.junit.Test;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import cyclops.collections.mutable.ListX;


//simple sanity check to make sure performance is in the ballpark not a proper benchmark!
public class SimplePerfCheck {
	@Test
	public void listInsert(){
		long start = System.currentTimeMillis();
		List<Integer> list = new ArrayList<>();
		for(int i=0;i<1_000_000;i++){
			list.add(1);
		}
		System.out.println("Javas List took " + (System.currentTimeMillis()- start));
		System.out.println(list.size());
		
		
	}
	@Test
	public void listXInsert(){
		long start = System.currentTimeMillis();
		ListX<Integer> list = ListX.empty();
		for(int i=0;i<1_000_000;i++){
			list.add(1);
		}
		System.out.println("ListX took " + (System.currentTimeMillis()- start));
		System.out.println(list.size());
		
		
	}
	@Test
	public void listXMapt(){
		
		ListX<Integer> list = ListX.empty();
		for(int i=0;i<1_000_000;i++){
			list.add(1);
		}
		long start = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("ListX  Map took " + (System.currentTimeMillis()- start));
		System.out.println(list.size());
		
		
	}
	@Test
	public void pstackInsert(){
		long start = System.currentTimeMillis();
		PStack<Integer> list = ConsPStack.empty();
		for(int i=0;i<1_000_000;i++){
			list = list.plus(1);
		}
		System.out.println("PCollections PStack  insert took " + (System.currentTimeMillis()- start));
		System.out.println(list.size());
		
		
	}
	@Test
	public void pstackXPrepend(){
		long start = System.currentTimeMillis();
		LinkedListX<Integer> list = LinkedListX.empty();
		for(int i=0;i<1_000_000;i++){
			list = list.plus(1);
		}
		System.out.println("LinkedListX  insert took " + (System.currentTimeMillis()- start));
		System.out.println(list.size());
		
		
	}
	/**
	@Test
	public void pStackXMap(){
		
		LinkedListX<Integer> list = LinkedListX.empty();
		for(int i=0;i<10_000;i++){
			list = list.plus(1);
		}
		long skip = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("LinkedListX  Map took " + (System.currentTimeMillis()- skip));
		System.out.println(list.size());
		
		
	}
	@Test
	public void pVectorXMapt(){
		
		VectorX<Integer> list = VectorX.empty();
		for(int i=0;i<1_000_000;i++){
			list = list.plus(1);
		}
		long skip = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("VectorX  Map took " + (System.currentTimeMillis()- skip));
		System.out.println(list.size());
		
		
	}
	@Test
	public void fjListMap(){
		
		fj.data.List<Integer> list = fj.data.List.list();
		for(int i=0;i<10_000;i++){
			list = list.cons(i);
		}
		long skip = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("FJ List  Map took " + (System.currentTimeMillis()- skip));
		System.out.println(list.length());
		
		
	}
	@Test
	public void fjList(){
		long skip = System.currentTimeMillis();
		fj.data.List<Integer> list = fj.data.List.list();
		for(int i=0;i<1_000_000;i++){
			list = list.cons(i);
		}
		
		
		System.out.println("FJ List  prepend took " + (System.currentTimeMillis()- skip));
		System.out.println(list.length());
		
		
	}
	@Test
	public void jsListPrepend(){
		long skip = System.currentTimeMillis();
		javaslang.collection.List<Integer> list = javaslang.collection.List.empty();
		for(int i=0;i<1_000_000;i++){
			list = list.prepend(i);
		}
		
		System.out.println("Javaslang List  prepend took " + (System.currentTimeMillis()- skip));
		System.out.println(list.length());
		
		
	}
	@Test
	public void jsListMap(){
		
		javaslang.collection.List<Integer> list = javaslang.collection.List.empty();
		for(int i=0;i<10_000;i++){
			list = list.prepend(i);
		}
		long skip = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("Javaslang List  Map took " + (System.currentTimeMillis()- skip));
		System.out.println(list.length());
		
		
	}
	**/
}
