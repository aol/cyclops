package com.aol.cyclops.data.collections;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;


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
		PStackX<Integer> list = PStackX.empty();
		for(int i=0;i<1_000_000;i++){
			list = list.plus(1);
		}
		System.out.println("PStackX  insert took " + (System.currentTimeMillis()- start));
		System.out.println(list.size());
		
		
	}
	/**
	@Test
	public void pStackXMap(){
		
		PStackX<Integer> list = PStackX.empty();
		for(int i=0;i<10_000;i++){
			list = list.plus(1);
		}
		long start = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("PStackX  Map took " + (System.currentTimeMillis()- start));
		System.out.println(list.size());
		
		
	}
	@Test
	public void pVectorXMapt(){
		
		PVectorX<Integer> list = PVectorX.empty();
		for(int i=0;i<1_000_000;i++){
			list = list.plus(1);
		}
		long start = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("PVectorX  Map took " + (System.currentTimeMillis()- start));
		System.out.println(list.size());
		
		
	}
	@Test
	public void fjListMap(){
		
		fj.data.List<Integer> list = fj.data.List.list();
		for(int i=0;i<10_000;i++){
			list = list.cons(i);
		}
		long start = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("FJ List  Map took " + (System.currentTimeMillis()- start));
		System.out.println(list.length());
		
		
	}
	@Test
	public void fjList(){
		long start = System.currentTimeMillis();
		fj.data.List<Integer> list = fj.data.List.list();
		for(int i=0;i<1_000_000;i++){
			list = list.cons(i);
		}
		
		
		System.out.println("FJ List  prepend took " + (System.currentTimeMillis()- start));
		System.out.println(list.length());
		
		
	}
	@Test
	public void jsListPrepend(){
		long start = System.currentTimeMillis();
		javaslang.collection.List<Integer> list = javaslang.collection.List.empty();
		for(int i=0;i<1_000_000;i++){
			list = list.prepend(i);
		}
		
		System.out.println("Javaslang List  prepend took " + (System.currentTimeMillis()- start));
		System.out.println(list.length());
		
		
	}
	@Test
	public void jsListMap(){
		
		javaslang.collection.List<Integer> list = javaslang.collection.List.empty();
		for(int i=0;i<10_000;i++){
			list = list.prepend(i);
		}
		long start = System.currentTimeMillis();
		list = list.map(i->i+1);
		System.out.println("Javaslang List  Map took " + (System.currentTimeMillis()- start));
		System.out.println(list.length());
		
		
	}
	**/
}
