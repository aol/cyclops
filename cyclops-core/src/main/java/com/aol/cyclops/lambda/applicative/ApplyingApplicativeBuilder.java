package com.aol.cyclops.lambda.applicative;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.functions.QuadFunction;
import com.aol.cyclops.functions.QuintFunction;
import com.aol.cyclops.functions.TriFunction;
import com.aol.cyclops.functions.currying.CurryVariance;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.Unit;

import lombok.AllArgsConstructor;
@AllArgsConstructor
public class ApplyingApplicativeBuilder<T,R, A extends Applicativable<R> >  {

		
		private final Unit unit;
		private final Functor functor;
		
		private Applicativable unit(Function fn){
			return (Applicativable)unit.unit(fn);
		}
		public Applicative<T,R,A> applicative(Applicativable<Function<? super T,? extends R>> fn){
			
			return ()->fn;
		}
		public  Applicative<T,R,A> applicative(Function<? super T,? extends R> fn){
			
			return applicative(unit(fn));
		}
		public <T2> Applicative<T2,R,A> applicative2(Applicativable<Function<? super T,Function<? super T2,? extends R>>> fn){
			Applicative2<T,T2,R,A> app = ()->fn;
			return app.ap(functor);
			
		}
		public <T2> Applicative<T2,R,A> applicative2(Function<? super T,Function<? super T2,? extends R>> fn){
			
			return applicative2(unit(fn));
		}
		public <T2> Applicative<T2,R,A> applicative2(BiFunction<? super T,? super T2,? extends R> fn){
			
			return applicative2(unit(CurryVariance.curry2(fn)));
		}
		public <T2,T3> Applicative2<T2,T3,R,A> applicative3(Applicativable<Function<? super T,Function<? super T2,Function<? super T3,? extends R>>>> fn){
			Applicative3<T,T2,T3,R,A> app =  ()->fn;
			return app.ap(functor);
		}
		public <T2,T3> Applicative2<T2,T3,R,A> applicative3(Function<? super T,Function<? super T2,Function<? super T3,? extends R>>> fn){
			
			return applicative3(unit(fn));
		}
		public <T2,T3> Applicative2<T2,T3,R,A> applicative3(TriFunction<? super T,? super T2,? super T3,? extends R> fn){
			
			return applicative3(unit(CurryVariance.curry3(fn)));
		}
		public <T2,T3,T4> Applicative3<T2,T3,T4,R,A> applicative4(Applicativable<Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,? extends R>>>>> fn){
			Applicative4<T,T2,T3,T4,R,A> app =  ()->fn;
			return app.ap(functor);
		}
		public <T2,T3,T4> Applicative3<T2,T3,T4,R,A> applicative4(Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,? extends R>>>> fn){
			
			return applicative4(unit(fn));
		}
		public <T2,T3,T4> Applicative3<T2,T3,T4,R,A> applicative4(QuadFunction<? super T,? super T2,? super T3,? super T4,? extends R> fn){
			
			return applicative4(unit(CurryVariance.curry4(fn)));
		}
		public <T2,T3,T4,T5> Applicative4<T2,T3,T4,T5,R,A> applicative5(Applicativable<Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>>> fn){
			Applicative5<T,T2,T3,T4,T5,R,A> app =  ()->fn;
			return app.ap(functor);
		}
		public <T2,T3,T4,T5> Applicative4<T2,T3,T4,T5,R,A> applicative5(Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>> fn){
			
			return applicative5(unit(fn));
		}
		public <T2,T3,T4,T5> Applicative4<T2,T3,T4,T5,R,A	> applicative5(QuintFunction<? super T,? super T2,? super T3,? super T4,? super T5,? extends R> fn){
			
			return applicative5(unit(CurryVariance.curry5(fn)));
		}
	
}
