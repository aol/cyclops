package com.aol.cyclops;

import static com.aol.cyclops.control.Matchable.Otherwise;
import static com.aol.cyclops.control.Matchable.then;
import static com.aol.cyclops.control.Matchable.When;
import static com.aol.cyclops.util.function.Predicates.__;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public class MatchablesTest {

	@Test
	public void test() {
		Matchables.headAndTail(Arrays.asList(1,2))
				 .matches(c->c.is(When(Maybe.of(1),ListX.of(2,3,4)),then("boo!"))
						 	  .is(When(t->t.equals(Maybe.of(1)),__),then("boohoo!"))
						 	   .isEmpty(then("oops!")), Otherwise("hello"));
	}

}
