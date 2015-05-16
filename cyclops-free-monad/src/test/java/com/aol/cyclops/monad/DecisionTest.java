package com.aol.cyclops.monad;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.function.Function;

import lombok.Value;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.Do;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.matcher.Matchable;
import com.aol.cyclops.monad.FreeTest.Box;

public class DecisionTest {

	@Test
	public void script(){
		//build a set of actions for get followed by put
		
		Object output =	Do.with(get("key"))
							.and((String a)->put("key",a))
							.yield((String a)->(String b) -> b);
							
		/**
		String input = ForComprehensions.foreach2(c -> c.flatMapAs$1(get("key"))
									.mapAs$2((Vars2<Object,Object> v) -> put("key",v.$1().toString()))
									.yield( v-> v.$2())).toString();
			**/
		
		System.out.println(output);
	//	assertThat(input,containsString("Free.Return(a=DecisionTest.Put(key=key, value=DecisionTest.Get(key=key, next=java.util.function"));
			
		
	}
	
	private Free<Action,Action> put(String key, String value){
		return Free.<Action,Action>liftF(new Put(key,value,new Action()));
		//return Free.suspend(new Box(Free.ret()));
	}
	private Free<Action,Void> get(String key){
		return Free.suspend(new Box(Free.ret(new Get(key,(Function)Function.identity()))));
	}
	private Free<Action,Void> delete(String key){
		return Free.suspend(new Box(Free.ret(new Delete(key,new Action()))));
	}
	static class Action implements Matchable, Functor<Action>{

		@Override
		public <R> Functor<R> map(Function<Action, R> fn) {
			return match(c -> 
							c.caseOf( (Put p) -> new Put(p.key,p.value,(Action)fn.apply(p.next)))
							.caseOf((Delete d) -> new Delete(d.key,(Action)fn.apply(d.next)))
							.caseOf((Get g) -> new Get(g.key,(Function)g.next.andThen(fn)))
			
					);
		} 
		
		
	}
	@Value
	static class Put extends Action  { String key; String value; Action next;}
	@Value
	static class Delete extends Action  { String key;Action next; }
	@Value
	static class Get extends Action { String key; Function<String,Action> next;}
	static class NoAction extends Action { }
	
	
	
}
