package com.aol.cyclops.streams.anyM;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.lambda.monads.transformers.OptionalT;
import com.aol.cyclops.monad.AnyM;

public class OptionTTest {

	
	@Test
	public void test() {
		OptionalT<Integer> optionT = new OptionalT<>(AnyM.ofMonad(Stream.of(Optional.of(10))));
		System.out.println(optionT.map(num->"hello world"+num).getRun().asSequence().firstValue());
	}



}
