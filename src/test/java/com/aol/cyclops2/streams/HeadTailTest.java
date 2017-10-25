package com.aol.cyclops2.streams;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.types.stream.HeadAndTail;

public class HeadTailTest {

	@Test
	public void headTailReplay(){
	
		ReactiveSeq<String> helloWorld = AnyM.streamOf("hello","world","last").stream();
		HeadAndTail<String> headAndTail = helloWorld.headAndTail();
		 String head = headAndTail.head();
		 assertThat(head,equalTo("hello"));
		
		ReactiveSeq<String> tail =  headAndTail.tail();
		assertThat(tail.headAndTail().head(),equalTo("world"));
		
	}
	
	@Test
	public void empty(){
	
		assertFalse(ListX.empty().headAndTail().headMaybe().isPresent());
		assertFalse(ListX.empty().headAndTail().headOptional().isPresent());
		assertTrue(ListX.empty().headAndTail().headStream().size()==0);
		
		
	}
}
