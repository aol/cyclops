package com.aol.cyclops.matcher;


import static com.aol.cyclops.matcher.Predicates.ANY;
import static org.jooq.lambda.tuple.Tuple.tuple;

import java.util.Objects;
import java.util.function.Predicate;

import lombok.Value;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jooq.lambda.tuple.Tuple;

import com.aol.cyclops.matcher.builders.Matching;

public class ScalaParserExample {

	//http://kerflyn.wordpress.com/2011/02/14/playing-with-scalas-pattern-matching/
	//See C# impl here :- https://github.com/dotnet/roslyn/issues/206
	public Integer eval(Expression expression, int xValue){
		
		return Matching.newCase().isType( (X x)-> xValue)
				.newCase().isType((Const c) -> c.getValue())
				.newCase().isType((Add a) ->  eval(a.getLeft(),xValue) + eval(a.getRight(),xValue))
				.newCase().isType( (Mult m) -> eval(m.getLeft(),xValue) * eval(m.getRight(),xValue))
				.newCase().isType( (Neg n) ->  -eval(n.getExpr(),xValue))
				.match(expression).orElse(1);
		
		
		
	}
	
	public Expression derive(Expression e){
		return Matching.newCase().isType((X x)-> new Const(1))
					.newCase().isType((Const c)-> new Const(0))
					.newCase().isType((Add a) -> new Add(derive(a.getLeft()),derive(a.getRight())))
					.newCase().isType( (Mult m) -> new Add(new Mult(derive(m.getLeft()), m.getRight()), new Mult(m.getLeft(), derive(m.getRight()))))
					.newCase().isType( (Neg n) -> new Neg(derive(n.getExpr())))
					.match(e).get();
					
	}
	
	
	Expression simplify(Expression e)
	{
	
		
		return Matching.<Expression>atomisedCase().isType( (Mult m)->new Const(0)).with(new Const(0),ANY())
						.atomisedCase().isType( (Mult m)->new Const(0)).with(ANY(),new Const(0))
						.atomisedCase().isType((Mult m)-> simplify(m.right)).with(new Const(1))
						.atomisedCase().isType( (Mult m) -> simplify(m.getLeft())).with(ANY(),new Const(1))
						.atomisedCase().isType( (Mult<Const,Const> m) -> new Const(m.left.value * m.right.value))
													.with(ANY(Const.class),ANY(Const.class))
						.atomisedCase().isType((Add a) -> simplify(a.right)).with(new Const(0),ANY())
						.atomisedCase().isType((Add a)-> simplify(a.left)).with(ANY(),new Const(0))
						.atomisedCase().isType( (Add<Const,Const> a) -> new Const(a.left.value + a.right.value)).with(ANY(Const.class), ANY(Const.class))
						.atomisedCase().isType( (Neg<Const> n) -> new Const(-n.expr.value)).with(new Neg<Const>(null),ANY(Const.class))
												
						
				.unapply(e).orElse(e);

	}
	
	//Records / case classes
	
	static abstract class  Expression implements Decomposable{ }
	final static class X extends Expression{ }
	@Value final static class Const extends Expression  implements Decomposable { int value; }
	@Value final static class Add<T extends Expression, R extends Expression> extends Expression implements Decomposable { T left; R right; }
	@Value final static class Mult<T extends Expression, R extends Expression> extends Expression  implements Decomposable { T left; R right; }
	@Value final static class Neg<T extends Expression> extends Expression  implements Decomposable { T expr; }
	
	
}

