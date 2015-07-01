package com.aol.cyclops.comprehensions.donotation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Value;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import com.aol.cyclops.comprehensions.ComprehensionData;
import com.aol.cyclops.comprehensions.ForComprehensions;
import com.aol.cyclops.lambda.utils.Mutable;

/**
 * Do notation for comprehensions
 * 
 * <pre>{@code 		Stream<Integer> stream = Do.add(list)
								.filter((Integer a) -> a>2)
								.yield((Integer a)-> a +2 );
								
				Stream<Integer> stream = Do.add(Arrays.asList(20,30))
								   .add(Arrays.asList(1,2,3))
								   .yield((Integer a)-> (Integer b) -> a + b+2);							
								
								
								}</pre>
 * 
 * @author johnmcclean
 *
 * 
 */

public class UntypedDo {
	@Value
	static class Entry{
		String key;
		Object value;
	}
	@Value
	static class Assignment{
	
		Function f;
	}
	@Value
	static class Guard{
		
		Function f;
	}
	public static class DoComp0 extends DoComp{
		public DoComp0(PStack<Entry> assigned) {
			super(assigned);
			
		}
		public  DoComp1 add(Object o){	
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
			
		}
		
	}
	public static class DoComp1 extends DoComp{
		public DoComp1(PStack<Entry> assigned) {
			super(assigned);
			
		}
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. add 2 to every element in a list
		 * 
		 * <pre>{@code   Do.add(list)
					  .yield((Integer a)-> a +2 );
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <T,R> R yield(Function<T,?> f){
			return this.yieldInternal(f);
		}
		
		public <T> DoComp1 filter(Function<T,Boolean> f){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}

		public <T> DoComp2 with(Function<T,?> f){
			return  new DoComp2(addToAssigned(f));
		}
		public  DoComp2 add(Object o){	
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
		public  DoComp2 add(Supplier o){	
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
	}
	public static class DoComp2 extends DoComp{
		public DoComp2(PStack<Entry> assigned) {
			super(assigned);
			
		}
		public <T,T1,R> R yield(Function<T,Function<T1,?>> f){
			return this.yieldInternal(f);
		}
		
		
		public <T,T1> DoComp2 filter(Function<T,Function<T1,Boolean>> f){
			return new DoComp2(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}

		public <T,T1> DoComp3 with(Function<T,Function<T1,?>> f){
			return  new DoComp3(addToAssigned(f));
		}
		public  DoComp3 add(Object o){	
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
		public  DoComp3 add(Supplier o){	
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
	}
	public static class DoComp3 extends DoComp{
		public DoComp3(PStack<Entry> assigned) {
			super(assigned);
			
		}
		public <T,T1,T2> DoComp3 filter(Function<T,Function<T1,Function<T2,Boolean>>> f){
			return new DoComp3(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}
		
		public <T,T1,T2,R> R yield(Function<T,Function<T1,Function<T2,?>>> f){
			return this.yieldInternal(f);
		}
		

		public <T,T1,T2> DoComp4 with(Function<T,Function<T1,Function<T2,?>>> f){
			return  new DoComp4(addToAssigned(f));
		}
		public  DoComp4 add(Object o){	
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
		public  DoComp4 add(Supplier o){	
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
	}
	public static class DoComp4 extends DoComp{
		public DoComp4(PStack<Entry> assigned) {
			super(assigned);
			
		}
		public <T,T1,T2,T3> DoComp4 filter(Function<T,Function<T1,Function<T2,Function<T3,Boolean>>>> f){
			return new DoComp4(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}
		public <T,T1,T2,T3,R> R yield(Function<T,Function<T1,Function<T2,Function<T3,?>>>> f){
			return this.yieldInternal(f);
		}
		
		public <T,T1,T2,T3> DoComp5 with(Function<T,Function<T1,Function<T2,Function<T3,?>>>> f){
			return  new DoComp5(addToAssigned(f));
		}
		public  DoComp5 add(Object o){	
			return new DoComp5(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
		public  DoComp5 add(Supplier o){	
			return new DoComp5(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
	}
	public static class DoComp5 extends DoComp{
		public DoComp5(PStack<Entry> assigned) {
			super(assigned);
			
		}
		public <T,T1,T2,T3,T4> DoComp5 filter(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Boolean>>>>> f){
			return new DoComp5(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}
		public <T,T1,T2,T3,T4,R> R yield(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,?>>>>> f){
			return this.yieldInternal(f);
		}
		
		public <T,T1,T2,T3,T4> DoComp6 with(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,?>>>>> f){
			return  new DoComp6(addToAssigned(f));
		}
		public  DoComp6 add(Object o){	
			return new DoComp6(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
		public  DoComp6 add(Supplier o){	
			return new DoComp6(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
	}
	public static class DoComp6 extends DoComp{
		public DoComp6(PStack<Entry> assigned) {
			super(assigned);
			
		}
		public <T,T1,T2,T3,T4,T5> DoComp6 filter(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Boolean>>>>>> f){
			return new DoComp6(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}
		public <T,T1,T2,T3,T4,T5,R> R yield(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,?>>>>>>  f){
			return this.yieldInternal(f);
		}
		
		public <T,T1,T2,T3,T4,T5> DoComp7 with(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,?>>>>>> f){
			return  new DoComp7(addToAssigned(f));
		}
		public  DoComp7 add(Object o){	
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
		public  DoComp7 add(Supplier o){	
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
	}
	public static class DoComp7 extends DoComp{
		public DoComp7(PStack<Entry> assigned) {
			super(assigned);
			
		}
		public <T,T1,T2,T3,T4,T5,T6> DoComp7 filter(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Boolean>>>>>>> f){
			return new DoComp7(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}
		public <T,T1,T2,T3,T4,T5,T6,R> R yield(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,?>>>>>>>  f){
			return this.yieldInternal(f);
		}
		

		public <T,T1,T2,T3,T4,T5,T6,T7> DoComp8 with(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,?>>>>>>> f){
			return  new DoComp8(addToAssigned(f));
		}
		public  DoComp8 add(Object o){	
			return new DoComp8(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
		public  DoComp8 add(Supplier o){	
			return new DoComp8(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),o)));
		}
	}
	public static class DoComp8 extends DoComp{
		public DoComp8(PStack<Entry> assigned) {
			super(assigned);
			
		}
		public <T,T1,T2,T3,T4,T5,T6,T7> DoComp8 filter(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,Boolean>>>>>>>> f){
			return new DoComp8(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}
		public <T,T1,T2,T3,T4,T5,T6,T7,R> R yield(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Function<T7,?>>>>>>>>  f){
			return this.yieldInternal(f);
		}
	}
	
	@AllArgsConstructor
	public abstract static class DoComp {
		
		PStack<Entry> assigned;
		
		protected PStack<Entry> addToAssigned(Function f){
			return assigned.plus(assigned.size(),createEntry(f));
		}
		protected Entry createEntry(Function f){
			return new Entry("$$monad"+assigned.size(),new Assignment(f));
		}
		
		protected <T> T yieldInternal(Function f){
			return (T)ForComprehensions.foreachX(c->build(c,f));
		}
		
		 @SuppressWarnings({"rawtypes","unchecked"})
		private Object handleNext(Entry e,ComprehensionData c,List<String> assigned){
			 List<String>  newList = new ArrayList(assigned); 
			if(e.getValue() instanceof Guard){
				
				final Function f = ((Guard)e.getValue()).getF();
				c.filter( ()-> {
							
							return unwrapNestedFunction(c, f, newList);
								
								}  );
				
			}
			else if(e.getValue() instanceof Assignment){
				
				final Function f = ((Assignment)e.getValue()).getF();
				c.$(e.getKey(), ()-> {
									
									return unwrapNestedFunction(c, f, newList);
								
								}  );
				
			}
			else
				c.$(e.getKey(),e.getValue());
			
			return null;
		}
		private Object build(
				ComprehensionData c, Function f) {
			Mutable<List<String>> vars = new Mutable<>(new ArrayList());
			assigned.stream().forEach(e-> addToVar(e,vars,handleNext(e,c,vars.get())));
			Mutable<Object> var = new Mutable<>(f);
			
			return c.yield(()-> { 
				return unwrapNestedFunction(c, f, vars.get());
				
		}  );
			
		}
		private Object unwrapNestedFunction(ComprehensionData c, Function f,
				List<String> vars) {
			Function next = f;
			Object result = null;
			for(String e : vars){
				
					result = next.apply(c.$(e ));
				if(result instanceof Function){
					next = ((Function)result);
				}
				
			}

			return result;
		}
	
		private Object addToVar(Entry e,Mutable<List<String>> vars, Object handleNext) {
			if(!(e.getValue() instanceof Guard)){	
				vars.get().add(e.getKey());
			}
			return handleNext;
		}
	
	}
	/**
	 * Start a for comprehension from a Supplier
	 * 
	 * Supplier#get will be called immediately
	 * 
	 *  If  supplied type is a Monad Cyclops knows about (@see com.aol.cyclops.lambda.api.Comprehender) it will be used directly
	 *  Otherwise an attempt will be made to lift the type to a Monadic form (@see com.aol.cyclops.lambda.api.MonadicConverter)
	 *
	 * 
	 * @param o Supplier that generates Object to use
	 * @return Next stage in the step builder
	 */
	public static  DoComp1 add(Supplier<Object> o){
		return new DoComp0(ConsPStack.empty()).add(o.get());
	}
	/**
	 * Build a for comprehension from supplied type
	 * If type is a Monad Cyclops knows about (@see com.aol.cyclops.lambda.api.Comprehender) it will be used directly
	 * Otherwise an attempt will be made to lift the type to a Monadic form (@see com.aol.cyclops.lambda.api.MonadicConverter)
	 * 
	 * @param o Object to use
	 * @return Next stage in for comprehension step builder
	 */
	public static  DoComp1 add(Object o){
		return new DoComp0(ConsPStack.empty()).add(o);
	}
	
	
}
