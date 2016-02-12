package com.aol.cyclops.streams;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.SequenceM;

public class HeadTailTest {

	@Test
	public void headTailReplay(){
	
		SequenceM<String> helloWorld = AnyM.streamOf("hello","world","last").toSequence();
		HeadAndTail<String> headAndTail = helloWorld.headAndTail();
		 String head = headAndTail.head();
		 assertThat(head,equalTo("hello"));
		
		SequenceM<String> tail =  headAndTail.tail();
		assertThat(tail.headAndTail().head(),equalTo("world"));
		
	}
	@Test
	public void headTailOptional(){
	
		SequenceM<String> helloWorld = SequenceM.of();
		Optional<HeadAndTail<String>> headAndTail = helloWorld.headAndTailOptional();
		assertTrue(!headAndTail.isPresent());
		
	}
	@Test
	public void empty(){
	
		assertFalse(ListX.empty().headAndTail().headMaybe().isPresent());
		assertFalse(ListX.empty().headAndTail().headOptional().isPresent());
		assertTrue(ListX.empty().headAndTail().headStream().size()==0);
		
		
	}
}
