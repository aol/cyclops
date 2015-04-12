package com.aol.cyclops.matcher;


import static com.aol.cyclops.matcher.Matchers.typeMatcher;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

public class ScalaParserExample {

	//http://kerflyn.wordpress.com/2011/02/14/playing-with-scalas-pattern-matching/
	public Integer eval(Expression expression, int xValue){

		
		return Matching.inCaseOfType( (X x)-> xValue)
			.inCaseOfType((Const c) -> c.getValue())
			.inCaseOfType((Add a) ->  eval(a.getLeft(),xValue) + eval(a.getRight(),xValue))
			.inCaseOfType( (Mult m) -> eval(m.getLeft(),xValue) * eval(m.getRight(),xValue))
			.inCaseOfType( (Neg n) ->  -eval(n.getExpr(),xValue))
			.match(expression).orElse(1);
		
		
	}
	
	
	
	static class Expression{ }
	
	static class X extends Expression{ }
	
	@AllArgsConstructor
	@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
	@Getter
	static class Const extends Expression{
		int value;
		
	}
	@AllArgsConstructor
	@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
	@Getter
	static class Add extends Expression{
		Expression left;
		Expression right;
		
	}
	
	@AllArgsConstructor
	@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
	@Getter
	static class Mult extends Expression{
		Expression left;
		Expression right;
		
	}
	@AllArgsConstructor (access=AccessLevel.PROTECTED) 
	@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
	@Getter
	static class Neg extends Expression{
		Expression expr;
		
		
	}
}

