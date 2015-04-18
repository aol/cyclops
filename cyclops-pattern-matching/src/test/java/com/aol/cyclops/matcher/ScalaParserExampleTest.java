package com.aol.cyclops.matcher;

import lombok.val;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.matcher.ScalaParserExample.Add;
import com.aol.cyclops.matcher.ScalaParserExample.Const;
import com.aol.cyclops.matcher.ScalaParserExample.Mult;
import com.aol.cyclops.matcher.ScalaParserExample.Neg;
import com.aol.cyclops.matcher.ScalaParserExample.X;
import com.aol.cyclops.matcher.builders.Matching;
public class ScalaParserExampleTest {
	ScalaParserExample parser;
	@Before
	public void setUp() throws Exception {
		parser = new ScalaParserExample();
	}

	@Test
	public void testEval() {
		
		// 1 + 2 * X*X
		val expr = new Add(new Const(1), new Mult(new Const(2), new Mult(new X(), new X()))); 
		

		assertThat(parser.eval(expr, 3),is(19));
		
	}

}
