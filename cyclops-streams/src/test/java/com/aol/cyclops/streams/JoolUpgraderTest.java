package com.aol.cyclops.streams;

import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.junit.Test;

public class JoolUpgraderTest {

	
	@Test
	public void test(){
		
		Seq s = Seq.of(1,2,3);
		
		Stream.of(s.getClass().getMethods())
				.filter(m-> m.getReturnType().equals(Seq.class))
				.forEach(m-> {
					System.out.println(m);
				});
		
	}
}

