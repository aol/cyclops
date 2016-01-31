package com.aol.cyclops.lambda.api;

import static org.junit.Assert.*;

import java.util.stream.Stream;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import com.aol.cyclops.sequence.Monoid;

public class AsGenericMonoidTest {

	@Test
	public void testAsMonoidFj() {
		fj.Monoid m = fj.Monoid.monoid((Integer a) -> (Integer b) -> a+b,0);
		Monoid<Integer> sum = AsGenericMonoid.asMonoid(m);
		
		assertThat(sum.reduce(Stream.of(1,2,3)),equalTo(6));
	}

}
