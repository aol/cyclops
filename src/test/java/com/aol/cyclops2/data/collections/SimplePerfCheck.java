package com.aol.cyclops2.data.collections;

import java.util.ArrayList;
import java.util.List;

import com.aol.cyclops2.data.collections.extensions.api.PStack;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.data.Seq;
import org.junit.Test;


import cyclops.collectionx.mutable.ListX;


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
		PStack<Integer> list = Seq.empty();
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
		
		LinkedListX<Integer> list = LinkedListX.zero();
		for(int i=0;i<10_000;i++){
			list = list.insertAt(1);
		}
		long skip = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("LinkedListX  Map took " + (System.currentTimeMillis()- skip));
		System.out.println(list.size());
		
		
	}
	@Test
	public void pVectorXMapt(){
		
		VectorX<Integer> list = VectorX.zero();
		for(int i=0;i<1_000_000;i++){
			list = list.insertAt(1);
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
		
		
		System.out.println("FJ List  prependAll took " + (System.currentTimeMillis()- skip));
		System.out.println(list.length());
		
		
	}
	@Test
	public void jsListPrepend(){
		long skip = System.currentTimeMillis();
		javaslang.toX.List<Integer> list = javaslang.toX.List.zero();
		for(int i=0;i<1_000_000;i++){
			list = list.prependAll(i);
		}
		
		System.out.println("Javaslang List  prependAll took " + (System.currentTimeMillis()- skip));
		System.out.println(list.length());
		
		
	}
	@Test
	public void jsListMap(){
		
		javaslang.toX.List<Integer> list = javaslang.toX.List.zero();
		for(int i=0;i<10_000;i++){
			list = list.prependAll(i);
		}
		long skip = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("Javaslang List  Map took " + (System.currentTimeMillis()- skip));
		System.out.println(list.length());
		
		
	}
	**/
}
