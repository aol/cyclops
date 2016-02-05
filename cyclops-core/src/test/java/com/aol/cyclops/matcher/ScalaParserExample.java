package com.aol.cyclops.matcher;


import static com.aol.cyclops.matcher.Predicates.ANY;
import static com.aol.cyclops.matcher.Predicates.__;
import static com.aol.cyclops.matcher.Predicates.hasValues;
import static com.aol.cyclops.matcher.Predicates.type;

import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.recursive.Matchable;
import com.aol.cyclops.matcher.recursive.RecursiveMatcher;
import com.aol.cyclops.objects.Decomposable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

public class ScalaParserExample {

	//http://kerflyn.wordpress.com/2011/02/14/playing-with-scalas-pattern-matching/
	//See C# impl here :- https://github.com/dotnet/roslyn/issues/206
	//paper : http://lampwww.epfl.ch/~emir/written/MatchingObjectsWithPatterns-TR.pdf
	
	public Integer eval(Expression expression, int xValue){
		
		
		
		return Matching.whenIsType( (X x)-> xValue)
				.whenIsType((Const c) -> c.getValue())
				.whenIsType((Add a) ->   eval(a.getLeft(),xValue) + eval(a.getRight(),xValue))
				.whenIsType( (Mult m) -> eval(m.getLeft(),xValue) * eval(m.getRight(),xValue))
				.whenIsType( (Neg n) ->  -eval(n.getExpr(),xValue))
				.match(expression).orElse(1);
		
		
		
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	
	
	public Expression derive(Expression e){
	
		
		return Matching.whenIsType((X x)-> new Const(1))
					.whenIsType((Const c)-> new Const(0))
					.whenIsType((Add a) -> new Add(derive(a.getLeft()),derive(a.getRight())))
					.whenIsType( (Mult m) -> new Add(new Mult(derive(m.getLeft()), m.getRight()), new Mult(m.getLeft(), derive(m.getRight()))))
					.whenIsType( (Neg n) -> new Neg(derive(n.getExpr())))
					.match(e).get();
		
		
	}
	
	
	Expression simplify(Expression e)
	{

		
		return RecursiveMatcher.<Expression>when().isType( (Mult m)->new Const(0)).with(new Const(0),__)
						.whenIsType( (Mult m)->new Const(0)).with(__,new Const(0))
						.whenIsType((Mult m)-> simplify(m.right)).with(new Const(1))
						.whenIsType( (Mult m) -> simplify(m.getLeft())).with(__,new Const(1))
						.whenIsType( (Mult<Const,Const> m) -> new Const(m.left.value * m.right.value))
													.with(ANY(Const.class),ANY(Const.class))
						.whenIsType((Add a) -> simplify(a.right)).with(new Const(0),__)
						.whenIsType((Add a)-> simplify(a.left)).with(__,new Const(0))
					
						.whenIsType( (Add<Const,Const> a) -> new Const(a.left.value + a.right.value)).with(ANY(Const.class), ANY(Const.class))
						.whenIsType( (Neg<Const> n) -> new Const(-n.expr.value)).with(new Neg<Const>(null),ANY(Const.class))
						.apply(e).orElse(e);

	}
	
	
	
	public Expression deeplyNestedExample(Expression e){
		
		return RecursiveMatcher.<Expression>when().isType( (Add<Const,Mult> a)-> new Const(1))
									.with(__,type(Mult.class).hasValues(__,new Const(0)))
				.whenIsType( (Add<Mult,Const> a)-> new Const(0)).with(type(Mult.class).hasValues(__,new Const(0)),__)
				.whenIsType( (Add<Add,Const> a)-> new Const(-100)).with(hasValues(__,new Const(2)),__)
				
				
				.apply(e).orElse(new Const(-1));
			
	}
	
	//Sealed case classes
	
	@AllArgsConstructor(access=AccessLevel.PRIVATE) static abstract class  Expression implements Decomposable{}
	final static class X extends Expression{ }
	@Value final static class Const extends Expression  { int value; }
	@Value final static class Add<T extends Expression, R extends Expression> extends Expression { T left; R right; }
	@Value final static class Mult<T extends Expression, R extends Expression> extends Expression  { T left; R right; }
	@Value final static class Neg<T extends Expression> extends Expression { T expr; }
	
	
}

