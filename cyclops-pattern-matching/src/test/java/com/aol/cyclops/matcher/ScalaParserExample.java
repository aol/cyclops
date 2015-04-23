package com.aol.cyclops.matcher;


import static com.aol.cyclops.matcher.Predicates.ANY;
import static com.aol.cyclops.matcher.ScalaParserExample.Matchable.value;
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
	
		
		return Matching.atomisedCase().isType( (Mult m)->new Const(0)).andAllValues(new Const(0),ANY())
												
												
						.atomisedCase().isType( (Mult m)->new Const(0)).andAllValues(ANY(),new Const(0))
												
												
						.atomisedCase().recordMembers(mult,new Const(1))
												.thenApply((Mult m)-> simplify(m.right))
						
						.atomisedCase().recordMembers(mult,ANY(),new Const(1))
												.thenApply( (Mult m) -> simplify(m.getLeft()))
												
						.atomisedCase().bothTrue(ANY(Const.class), ANY(Const.class))
												.thenExtract( (Mult<Const,Const> m) -> tuple(m.left.value,m.right.value)   )
												.thenApply( t -> new Const(t.v1 * t.v2))						
					/**	.atomisedCase().recordMembers(multConst,ANY(Const.class),ANY(Const.class))
												.thenApply( (Mult<Const,Const> m) -> new Const(m.left.value * m.right.value))
						**/						
						.atomisedCase().recordMembers(add,new Const(0),ANY())
												.thenApply((Add a) -> simplify(a.right))
												
						.atomisedCase().recordMembers(add, ANY(),new Const(0))
												.thenApply((Add a)-> simplify(a.left))
						.atomisedCase().bothTrue(ANY(Const.class), ANY(Const.class))
												.thenExtract( (Add<Const,Const> a )-> tuple(a.left.value,a.right.value))
												.thenApply( t -> new Const(t.v1+t.v2))
						.atomisedCase().recordMembers(new Neg<Const>(null),ANY(Const.class))
											.thenApply( (Neg<Const> n) -> new Const(-n.expr.value))		
						
				.match(e).get();
				/**
//				.isMult(Const(0), *): return Const(0);
	    case Mult(*, Const(0)): return Const(0);
	    case Mult(Const(1), var x): return Simplify(x);
	    case Mult(var x, Const(1)): return Simplify(x);
	    case Mult(Const(var l), Const(var r)): return Const(l*r);
	    case Add(Const(0), var x): return Simplify(x);
	    case Add(var x, Const(0)): return Simplify(x);
	    case Add(Const(var l), Const(var r)): return Const(l+r);
	    case Neg(Const(var k)): return Const(-k);
	    default: return e;
	  }**/
	}
	
	//Records / case classes
	
	static abstract class  Expression{ }
	static class X extends Expression{ }
	@Value  static class Const extends Expression  implements Decomposable { int value; }
	@Value static class Add<T extends Expression, R extends Expression> extends Expression implements Decomposable { T left; R right; }
	@Value static class Mult<T extends Expression, R extends Expression> extends Expression  implements Decomposable { T left; R right; }
	@Value static class Neg<T extends Expression> extends Expression  implements Decomposable { T expr; }
	
	
}

