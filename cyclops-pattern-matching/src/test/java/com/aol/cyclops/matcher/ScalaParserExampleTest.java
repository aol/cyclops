package com.aol.cyclops.matcher;

import lombok.val;
import static com.aol.cyclops.matcher.Predicates.__;
import static com.aol.cyclops.matcher.Predicates.type;
import static com.aol.cyclops.matcher.Predicates.with;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.matcher.ScalaParserExample.Add;
import com.aol.cyclops.matcher.ScalaParserExample.Const;
import com.aol.cyclops.matcher.ScalaParserExample.Expression;
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testEval() {
		
		// 1 + 2 * X*X
		
		val expr = new Add(new Const(1), new Mult(new Const(2), new Mult(new X(), new X()))); 
		
		assertThat(parser.eval(expr, 3),is(19));
		
		val df = parser.derive(expr);
		assertThat(parser.eval(df, 3), is(12));
		val simple = parser.simplify(expr);
		System.out.println(simple);
	}	
	
	@Test
	public void testNesting(){
		val expr = new Add(new Const(1),new Mult(new Const(5),new Const(0)));
		assertThat(parser.deeplyNestedExample(expr),is(new Const(1)));
	}
	@Test
	public void testNestingFalse(){
		val expr = new Add(new Const(1),new Mult(new Const(5),new Const(10)));
		assertThat(parser.deeplyNestedExample(expr),is(new Const(-1)));
	}
	/**
	Matching.<Expression>_case().isType( (Add<Const,Mult> a)-> new Const(1)).with(__,type(Mult.class).with(__,new Const(0)))
	._case().isType( (Add<Mult,Const> a)-> new Const(0)).with(type(Mult.class).with(__,new Const(0)),__)
	._case().isType( (Add<Add,Const> a)-> new Const(0)).with(with(__,new Const(2)),__)
	//a.left.value+
	
	.unapply(e).orElse(e); **/

}
