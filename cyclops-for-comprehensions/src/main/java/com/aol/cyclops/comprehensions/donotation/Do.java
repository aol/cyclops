package com.aol.cyclops.comprehensions.donotation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
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

public class Do {
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
		public <T,R> R yield(Function<T,?> f){
			return this.yieldInternal(f);
		}
		public <T> DoComp1 filter(Function<T,Boolean> f){
			return new DoComp1(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}

		public <T> DoComp2 and(Function<T,?> f){
			return  new DoComp2(addToAssigned(f));
		}
		public  DoComp2 andJustAdd(Object o){	
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

		public <T,T1> DoComp3 and(Function<T,Function<T1,?>> f){
			return  new DoComp3(addToAssigned(f));
		}
		public  DoComp3 andJustAdd(Object o){	
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

		public <T,T1,T2> DoComp4 and(Function<T,Function<T1,Function<T2,?>>> f){
			return  new DoComp4(addToAssigned(f));
		}
		public  DoComp4 andJustAdd(Object o){	
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
		public <T,T1,T2,T3> DoComp5 and(Function<T,Function<T1,Function<T2,Function<T3,Boolean>>>> f){
			return  new DoComp5(addToAssigned(f));
		}
		public  DoComp5 andJustAdd(Object o){	
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
		public <T,T1,T2,T3,T4> DoComp6 and(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Boolean>>>>> f){
			return  new DoComp6(addToAssigned(f));
		}
		public  DoComp6 andJustAdd(Object o){	
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

		public <T,T1,T2,T3,T4,T5> DoComp7 and(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Boolean>>>>>> f){
			return  new DoComp7(addToAssigned(f));
		}
		public  DoComp7 andJustAdd(Object o){	
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

		public <T,T1,T2,T3,T4,T5,T6,T7> DoComp8 and(Function<T,Function<T1,Function<T2,Function<T3,Function<T4,Function<T5,Function<T6,Boolean>>>>>>> f){
			return  new DoComp8(addToAssigned(f));
		}
		public  DoComp8 andJustAdd(Object o){	
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
		/**
		public  DoComp assignand(Function f){
			return new DoComp(assigned.plus(assigned.size(),new Entry("$$monad"+assigned.size(),new Assignment(f))));
			
		}**/
	//	protected abstract <T extends DoComp> create(PStack stack);
		protected PStack<Entry> addToAssigned(Function f){
			return assigned.plus(assigned.size(),createEntry(f));
		}
		protected Entry createEntry(Function f){
			return new Entry("$$monad"+assigned.size(),new Assignment(f));
		}
		/**
		public DoComp filter(Function f){
			return new DoComp(assigned.plus(assigned.size(),new Entry("$$internalGUARD"+assigned.size(),new Guard(f))));
		}**/
		protected <T> T yieldInternal(Function f){
			return (T)ForComprehensions.foreachX(c->build(c,f));
		}
		
		 @SuppressWarnings({"rawtypes","unchecked"})
		private Object handleNext(Entry e,ComprehensionData c,List<String> assigned){
			if(e.getValue() instanceof Guard){
				
				final Function f = ((Guard)e.getValue()).getF();
				c.filter( ()-> {
									List<String>  newList = new ArrayList(assigned); 
									
									ClosedVar<Object> var = new ClosedVar<>(true);
									newList.stream().forEach(v-> var.set(f.apply(c.$(v)) )) ; 
										return var.get(); 
								}  );
				
			}
			else if(e.getValue() instanceof Assignment){
				
				final Function f = ((Assignment)e.getValue()).getF();
				c.$(e.getKey(), ()-> {
									List<Supplier<String>>  newList = new ArrayList(assigned); 
									
									ClosedVar<Object> var = new ClosedVar<>(true);
									newList.stream().forEach(v-> var.set(f.apply(c.$(v.get() )) )) ; 
										return var.get(); 
								}  );
				
			}
			else
				c.$(e.getKey(),e.getValue());
			
			return null;
		}
		private Object build(
				ComprehensionData c, Function f) {
			ClosedVar<List<String>> vars = new ClosedVar<>(new ArrayList());
			assigned.stream().forEach(e-> addToVar(e,vars,handleNext(e,c,vars.get())));
			ClosedVar var = new ClosedVar();
			return c.yield(()-> { vars.get().stream().forEach(e-> var.set(f.apply( c.$(e ) ))) ; return var.get(); }  );
			
		}
	
		private Object addToVar(Entry e,ClosedVar<List<String>> vars, Object handleNext) {
			if(!(e.getValue() instanceof Guard)){	
				vars.get().add(e.getKey());
			}
			return handleNext;
		}
	
	}
	public static  DoComp1 with(Supplier<Object> o){
		return new DoComp0(ConsPStack.empty()).add(o.get());
	}
	public static  DoComp1 with(Object o){
		return new DoComp0(ConsPStack.empty()).add(o);
	}
	
	
}
