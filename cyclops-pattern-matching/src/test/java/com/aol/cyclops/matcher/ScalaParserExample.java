package com.aol.cyclops.matcher;


import static com.aol.cyclops.matcher.Predicates.ANY;
import static com.aol.cyclops.matcher.Predicates.__;
import static com.aol.cyclops.matcher.Predicates.type;
import static com.aol.cyclops.matcher.Predicates.with;

import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.val;

import com.aol.cyclops.lambda.api.Decomposable;
import com.aol.cyclops.matcher.ScalaParserExample.Mult;
import com.aol.cyclops.matcher.builders.Matching;

public class ScalaParserExample {

	//http://kerflyn.wordpress.com/2011/02/14/playing-with-scalas-pattern-matching/
	//See C# impl here :- https://github.com/dotnet/roslyn/issues/206
	//paper : http://lampwww.epfl.ch/~emir/written/MatchingObjectsWithPatterns-TR.pdf
	
	public Integer eval(Expression expression, int xValue){
		
		return Matching.when().isType( (X x)-> xValue)
				.when().isType((Const c) -> c.getValue())
				.when().isType((Add a) ->   eval(a.getLeft(),xValue) + eval(a.getRight(),xValue))
				.when().isType( (Mult m) -> eval(m.getLeft(),xValue) * eval(m.getRight(),xValue))
				.when().isType( (Neg n) ->  -eval(n.getExpr(),xValue))
				.match(expression).orElse(1);
		
		
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	
	
	public Expression derive(Expression e){
	
		
		return Matching.when().isType((X x)-> new Const(1))
					.when().isType((Const c)-> new Const(0))
					.when().isType((Add a) -> new Add(derive(a.getLeft()),derive(a.getRight())))
					.when().isType( (Mult m) -> new Add(new Mult(derive(m.getLeft()), m.getRight()), new Mult(m.getLeft(), derive(m.getRight()))))
					.when().isType( (Neg n) -> new Neg(derive(n.getExpr())))
					.match(e).get();
		
		
	}
	
	
	Expression simplify(Expression e)
	{

		
		return Matching.<Expression>whenValues().isType( (Mult m)->new Const(0)).with(new Const(0),__)
						.whenValues().isType( (Mult m)->new Const(0)).with(__,new Const(0))
						.whenValues().isType((Mult m)-> simplify(m.right)).with(new Const(1))
						.whenValues().isType( (Mult m) -> simplify(m.getLeft())).with(__,new Const(1))
						.whenValues().isType( (Mult<Const,Const> m) -> new Const(m.left.value * m.right.value))
													.with(ANY(Const.class),ANY(Const.class))
						.whenValues().isType((Add a) -> simplify(a.right)).with(new Const(0),__)
						.whenValues().isType((Add a)-> simplify(a.left)).with(__,new Const(0))
					
						.whenValues().isType( (Add<Const,Const> a) -> new Const(a.left.value + a.right.value)).with(ANY(Const.class), ANY(Const.class))
						.whenValues().isType( (Neg<Const> n) -> new Const(-n.expr.value)).with(new Neg<Const>(null),ANY(Const.class))
												
						
				.apply(e).orElse(e);

	}
	
	
	
	public Expression deeplyNestedExample(Expression e){
		
		return Matching.<Expression>whenValues().isType( (Add<Const,Mult> a)-> new Const(1))
									.with(__,type(Mult.class).with(__,new Const(0)))
				.whenValues().isType( (Add<Mult,Const> a)-> new Const(0)).with(type(Mult.class).with(__,new Const(0)),__)
				.whenValues().isType( (Add<Add,Const> a)-> new Const(-100)).with(with(__,new Const(2)),__)
				
				
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

