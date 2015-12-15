package com.aol.cyclops.lambda.monads;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.monad.AnyM;
public class FlatMapTest {


	@Test
	public void flatMapCrossTypeNotCollectionUnwrap(){

		assertThat(AnyM.fromOptional(Optional.of(1)).flatMapStream(i->Stream.of(i+2)).unwrap(),equalTo(Optional.of(Arrays.asList(3))));
	}
}
