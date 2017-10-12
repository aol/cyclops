package com.aol.cyclops2.react.lazy.sequence;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import cyclops.reactive.ReactiveSeq;
import com.aol.cyclops2.types.stream.HeadAndTail;
import static com.aol.cyclops2.react.lazy.DuplicationTest.of;
public class HeadTailTest {

	@Test
	public void headTailReplay(){
	
		ReactiveSeq<String> helloWorld = of("hello","world","last");
		HeadAndTail<String> headAndTail = helloWorld.headAndTail();
		 String head = headAndTail.head();
		 assertThat(head,equalTo("hello"));
		
		ReactiveSeq<String> tail =  headAndTail.tail();
		assertThat(tail.headAndTail().head(),equalTo("world"));
		
	}
	
}
