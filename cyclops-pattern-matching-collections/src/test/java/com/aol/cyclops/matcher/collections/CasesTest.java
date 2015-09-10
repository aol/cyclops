package com.aol.cyclops.matcher.collections;

import static com.aol.cyclops.matcher.Predicates.__;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.Cases;
import com.aol.cyclops.matcher.CollectionMatcher;
import com.aol.cyclops.matcher.Extractors;

public class CasesTest {
	@Test
	public void andThenTest(){
		Case<Object,Set,Function<Object,Set>> cse = Case.of(input-> input instanceof Map, input-> ((Map)input).keySet());
		val cases = Cases.of(cse).map(c -> c.andThen((Cases)CollectionMatcher.whenIterable().allHoldNoType(__,2).thenExtract(Extractors.<Integer>get(1)).thenApply(i->i*2)
													.whenIterable().allHoldNoType(2,__).thenExtract(Extractors.<Integer>get(1)).thenApply(i->i*3).cases()));
	}
}
