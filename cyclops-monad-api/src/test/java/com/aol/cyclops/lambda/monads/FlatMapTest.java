package com.aol.cyclops.lambda.monads;
import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.junit.Test;
public class FlatMapTest {


	@Test
	public void flatMapCrossTypeNotCollectionUnwrap(){

		assertThat(anyM(Optional.of(1)).flatMapStream(i->Stream.of(i+2)).unwrap(),equalTo(Optional.of(Arrays.asList(3))));
	}
}
