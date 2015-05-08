package com.aol.cyclops.comprehensions;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * For Comphrension for a single Monadic operation (map)
 * 
 * @author johnmcclean
 *
 * @param <MONAD> Monad or Collection / Map type (e.g. Stream<?> / Optional<?>/ Try<?> / List<?> / PList<?>, Map<?,?>, HashMap<?,?>)
 * @param <R>
 * @param <R_PARAM>
 */
public class ForComprehension1<MONAD,R,R_PARAM> {

	
	
	public static void main(String[] args){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
		Object result =  new ForComprehension1<Optional<?>,Optional<Integer>,Integer>()
							.<Integer>foreach(c -> c.mapAs$1(one)
															.filter(()->c.$1()>2)
															.yield(()->{return f2.apply(c.$1(), 10);}));
		System.out.println(result);
	}
	
	public <T1> R foreach(Function<Step1<MONAD,T1,R,R_PARAM>,R> fn){
		return Foreach.foreach(new ContextualExecutor<R,Foreach<R>>(new Foreach<R>()){
			public R execute(){
				return fn.apply(new ComphrensionData<>(this));
			}
		});
	}
	static interface Step1<MONAD,T1,R,R_PARAM>{
		/**
		 * Perform a map operation on the supplied Monad or Collection / Map
		 * 
		 * @param f Monad or Collection / Map to perform a map operation on
		 * @return Next step in the for comprehension builder
		 */
		public  Step2<MONAD,T1,R,R_PARAM> mapAs$1(MONAD f);
		
		/**
		 * @return current value of the operation bound as $1
		 */
		public T1 $1();
		
	}
	static interface Step2<MONAD,T1,R,R_PARAM>{
		
		/**
		 * Filter / guard against the current Monad
		 * 
		 * @param s Supplier that returns true / or false. The supplier can use bound variables ($1()) to access current state
		 * @return true if keep in / false if remove
		 */
		public  Step3<MONAD,T1,R,R_PARAM> filter(Supplier<Boolean> s);
		/**
		 * Trigger the for comprehension and yield a value for each terminal stage
		 * 
		 * @param s Supplier that returns a value. The supplier can use bound variables ($1()) to access current state
		 * @return Yielded variable
		 */
		public R yield(Supplier<R_PARAM> s);
		public void run(Runnable r);
		
	}
	static interface Step3<MONAD,T1,R,R_PARAM>{
		public R yield(Supplier<R_PARAM> s);
		
	}
	class ComphrensionData<MONAD,T1,R,R_PARAM> implements Step1<MONAD,T1,R,R_PARAM>, Step2<MONAD,T1,R,R_PARAM>,Step3<MONAD,T1,R,R_PARAM>{
		
		BaseComprehensionData data;
		
		
		public ComphrensionData(ContextualExecutor delegate) {
			super();
			data = new BaseComprehensionData(new ExecutionState(delegate, new State()));
		}
		
		public Step3<MONAD,T1,R,R_PARAM> filter(Supplier<Boolean> s){
			data.guardInternal(s);
			return this;
			
		}
		public void run(Runnable r){
			data.run(r);
		}
		
		public R yield(Supplier<R_PARAM> s){
			return data.yieldInternal(s);
			
		}
		public T1 $1(){
			return data.$Internal("_1");
		
		}
		
		public Step2<MONAD,T1,R,R_PARAM> mapAs$1(MONAD f){
			data.$Internal("_1", f);
			
			return this;
		}
		
	}
}
