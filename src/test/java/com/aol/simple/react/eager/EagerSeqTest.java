package com.aol.simple.react.eager;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

import com.aol.simple.react.base.BaseSeqTest;
import com.aol.simple.react.stream.eager.EagerFutureStream;

public class EagerSeqTest extends BaseSeqTest {
 
	@Override
	protected <U> EagerFutureStream<U> of(U... array) {
		return EagerFutureStream.parallel(array);
	}

	@Test
	public void testOfType() {
		assertTrue(
				of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList().containsAll(asList(1, 2, 3)));
		assertTrue( of(1, "a", 2, "b", 3, null)
				.ofType(Serializable.class).toList().containsAll(asList(1, "a", 2, "b", 3)));
	}

	@Test
	public void testCastPast() {
		assertTrue(
				of(1, "a", 2, "b", 3, null).capture(e -> e.printStackTrace())
						.cast(Serializable.class).toList().containsAll(
								asList(1, "a", 2, "b", 3, null)));

	}

}
