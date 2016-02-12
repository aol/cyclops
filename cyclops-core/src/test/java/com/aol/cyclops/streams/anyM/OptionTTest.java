package com.aol.cyclops.streams.anyM;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.monad.AnyM;

public class OptionTTest {

	
	@Test
	public void test() {
		OptionalT<Integer> optionT = OptionalT.of(AnyM.ofMonad(Stream.of(Optional.of(10))));
		System.out.println(optionT.map(num->"hello world"+num).unwrap().asSequence().firstValue());
	}


}
