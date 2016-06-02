package com.aol.cyclops.types.anyM;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;;

public class AnyMValueTest {

	@Test
	public void testFlatMapFirst() {
		List actualList = AnyM.fromMaybe(Maybe.just(10)).flatMapFirst(i->AnyM.fromList(ListX.of(i,20,30))).stream().toList();
		assertEquals(actualList.size(), 1);
		assertEquals(actualList.get(0), 10);
	}
	
	@Test
	public void testFlatMapFirst2() {
		List actualList = AnyM.fromMaybe(Maybe.just(-100)).flatMapFirst(i->AnyM.fromStream(ReactiveSeq.of(i,20,30))).stream().toList();
		assertEquals(actualList.size(), 1);
		assertEquals(actualList.get(0), -100);
	}
	
	@Test
	public void testFlatMapFirst3() {
		List actualList = AnyM.fromList(new ArrayList()).flatMapFirst(i -> AnyM.fromStream(ReactiveSeq.of(i,20,30))).stream().toList();
		assertEquals(actualList.size(), 0);
	}
}
