package com.aol.cyclops.functions.collections.extensions;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.SequenceM;

public abstract class AbstractOrderDependentCollectionXTest extends AbstractCollectionXTest {
	@Test
	public void headTailReplay() {

		CollectionX<String> helloWorld = of("hello", "world", "last");
		HeadAndTail<String> headAndTail = helloWorld.headAndTail();
		String head = headAndTail.head();
		assertThat(head, equalTo("hello"));

		SequenceM<String> tail = headAndTail.tail();
		assertThat(tail.headAndTail().head(), equalTo("world"));

	} 
	@Test
    public void testScanLeftStringConcat() {
        assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
        		is(4));
    }
	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
	}
	@Test
	public void testReverse() {

		assertThat(of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
	}

	@Test
	public void testFoldRight() {
		Supplier<CollectionX<String>> s = () -> of("a", "b", "c");

		assertTrue(s.get().foldRight("", String::concat).contains("a"));
		assertTrue(s.get().foldRight("", String::concat).contains("b"));
		assertTrue(s.get().foldRight("", String::concat).contains("c"));
		assertEquals(3, (int) s.get().map(str -> str.length()).foldRight(0, (t, u) -> u + t));
	}

	@Test
	public void testFoldLeft() {
		for (int i = 0; i < 100; i++) {
			Supplier<CollectionX<String>> s = () -> of("a", "b", "c");

			assertTrue(s.get().reduce("", String::concat).contains("a"));
			assertTrue(s.get().reduce("", String::concat).contains("b"));
			assertTrue(s.get().reduce("", String::concat).contains("c"));

			assertEquals(3, (int) s.get().map(str -> str.length()).foldLeft(0, (u, t) -> u + t));

			assertEquals(3, (int) s.get().map(str -> str.length()).foldRight(0, (t, u) -> u + t));
		}
	}

}
