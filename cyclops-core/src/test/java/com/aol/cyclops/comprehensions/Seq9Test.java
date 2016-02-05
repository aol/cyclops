package com.aol.cyclops.comprehensions;

import java.util.stream.Collectors;

import org.jooq.lambda.Seq;
import org.junit.Test;

public class Seq9Test {

	@Test
	public void simple(){
		Seq.of("a","b","c").toList();
		Seq.of("a","b","c").collect(Collectors.toList());
	}
}
