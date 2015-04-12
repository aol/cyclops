package com.aol.cyclops.matcher;


import static com.aol.cyclops.matcher.Matchers.typeMatcher;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

public class ScalaParserExample {

	//http://kerflyn.wordpress.com/2011/02/14/playing-with-scalas-pattern-matching/
	public Integer eval(Expression expression, int xValue){
		
		PatternMatcher match = new PatternMatcher();
		
		match.inCaseOf(typeMatcher(X.class), (X x)-> xValue)
			.inCaseOf(typeMatcher(Const.class), (Const c) -> c.getValue())
			.inCaseOf(typeMatcher(Add.class), (Add a) ->  eval(a.getLeft(),xValue) + eval(a.getRight(),xValue))
			.inCaseOf(typeMatcher(Mult.class), (Mult m) -> eval(m.getLeft(),xValue) * eval(m.getRight(),xValue))
			.inCaseOf(typeMatcher(Neg.class), (Neg n) ->  -eval(n.getExpr(),xValue));
		
		return (Integer)match.match(expression).orElse(1);
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

