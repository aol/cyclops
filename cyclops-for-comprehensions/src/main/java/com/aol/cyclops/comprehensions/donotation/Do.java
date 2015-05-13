package com.aol.cyclops.comprehensions.donotation;

import static com.aol.cyclops.comprehensions.donotation.Do.letters;
import static com.aol.cyclops.comprehensions.donotation.Do.Letters.a;
import static com.aol.cyclops.comprehensions.functions.Lambda.λ1;
import static com.aol.cyclops.comprehensions.functions.Lambda.λ2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.Value;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import com.aol.cyclops.comprehensions.ComprehensionData;
import com.aol.cyclops.comprehensions.ForComprehensions;
import com.aol.cyclops.lambda.utils.ClosedVar;

/**
 * Do notation for comprehensions
 * 
 * {@code 		Stream<Integer> stream = Do.withVars(letters)
								.assign(a, list)
								.filter(λ1((Integer a) -> a>2))
								.yield(λ1((Integer a)-> a +2) );
								
				Stream<Integer> stream = Do.withVars(Do.letters)
								   .assign(Do.Letters.a, Arrays.asList(20,30))
								   .assign(Do.Letters.b, Arrays.asList(1,2,3))
								   .yield(λ2((Integer a)-> (Integer b) -> a + b+2) );							
								
								
								}
 * 
 * @author johnmcclean
 *
 * @param <X>
 */
@Value
public class Do<X extends Vars> {
	private final Vars vars;
	
	PStack<Entry> assigned;
	public  Do<X> assign(Supplier<String> var,Object o){
		return new Do(vars,assigned.plus(assigned.size(),new Entry(var,o)));
		
	}
	@Value
	static class Entry{
		Supplier<String> key;
		Object value;
	}
	@Value
	static class Guard{
		
		Function<Vars,Boolean> f;
	}
	public Do<X> filter(Function f){
		return new Do(vars,assigned.plus(assigned.size(),new Entry(()->"$$internalGUARD"+assigned.size(),new Guard(f))));
	}
	public<T> T yield(Function f){
		return (T)ForComprehensions.foreachX(c->build(c,f));
	}
	
	private Object handleNext(Entry e,ComprehensionData c,List<Supplier<String>> assigned){
		if(e.getValue() instanceof Guard){
			
			final Function f = ((Guard)e.getValue()).getF();
			c.filter( ()-> { List<Supplier<String>>  newList = new ArrayList(assigned); 
								
								ClosedVar<Object> var = new ClosedVar<>(true);
								
							
							newList.stream().forEach(v-> var.set(f.apply(c.$(v.get() )) )) ; 
									return var.get(); 
							}  );
			
		}
		else
			c.$(e.getKey().get(),e.getValue());
		
		return null;
	}
	private Object build(
			ComprehensionData c, Function f) {
		ClosedVar<List<Supplier<String>>> vars = new ClosedVar<>(new ArrayList());
		assigned.stream().forEach(e-> addToVar(e,vars,handleNext(e,c,vars.get())));
		ClosedVar var = new ClosedVar();
		return c.yield(()-> { vars.get().stream().forEach(e-> var.set(f.apply( c.$(e.get() ) ))) ; return var.get(); }  );
		
	}

	private Object addToVar(Entry e,ClosedVar<List<Supplier<String>>> vars, Object handleNext) {
		if(!(e.getValue() instanceof Guard)){	
			vars.get().add(e.getKey());
		}
		return handleNext;
	}
	public static <X extends Vars> Do<X> withVars(X vars){
		return new Do(vars,ConsPStack.empty());
	}
	public static Letters letters = new Letters();
	public static class Letters implements Vars{
		public static final Supplier<String> a = ()->"a";
		public static final Supplier<String> b = ()->"b";
		public static final Supplier<String> c = ()->"c";
		public static final Supplier<String> d = ()->"d";
		public static final Supplier<String> e = ()->"e";
		public static final Supplier<String> f = ()->"f";
		public static final Supplier<String> g = ()->"g";
		public static final Supplier<String> h = ()->"h";
		public static final Supplier<String> i = ()->"i";
		public static final Supplier<String> j = ()->"j";
		public static final Supplier<String> k = ()->"k";
		public static final Supplier<String> l = ()->"l";
		public static final Supplier<String> m = ()->"m";
		public static final Supplier<String> n = ()->"n";
		public static final Supplier<String> o = ()->"o";
		public static final Supplier<String> p = ()->"p";
		public static final Supplier<String> q = ()->"q";
		public static final Supplier<String> r = ()->"r";
		public static final Supplier<String> s = ()->"s";
		public static final Supplier<String> t = ()->"t";
		public static final Supplier<String> u = ()->"u";
		public static final Supplier<String> v = ()->"v";
		public static final Supplier<String> w = ()->"w";
		public static final Supplier<String> x = ()->"x";
		public static final Supplier<String> y = ()->"y";
		public static final Supplier<String> z = ()->"z";
		
	}
	
}
