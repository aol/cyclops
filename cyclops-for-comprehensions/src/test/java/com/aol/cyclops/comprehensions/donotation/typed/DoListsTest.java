package com.aol.cyclops.comprehensions.donotation.typed;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.monad.AnyM;
public class DoListsTest {
	
	@Test
	public void optionalListMultiReturn(){
		AnyM<Integer> anyM = Do.add(Optional.of(1))
									 .withStream(i->Stream.of(i,2))
									 .yield(a->b-> a+b);
		
		anyM.map(i->i+1);
		System.out.println(anyM);
	}

	
}
