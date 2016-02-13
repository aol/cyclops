package com.aol.cyclops.types.applicative.zipping;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.types.Unit;
import com.aol.cyclops.util.function.CurryVariance;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

import lombok.AllArgsConstructor;
@AllArgsConstructor
public class ZippingApplicativeBuilder<T,R, A extends ZippingApplicativable<R> >  {

		private final Unit unit;
		
		private ZippingApplicativable unit(Function fn){
			return (ZippingApplicativable)unit.unit(fn);
		}
		public ZippingApplicative<T,R,A> applicative(ZippingApplicativable<Function<? super T,? extends R>> fn){
			
			return ()->fn;
		}
		public  ZippingApplicative<T,R,A> applicative(Function<? super T,? extends R> fn){
			
			return applicative(unit(fn));
		}
		public <T2> ZippingApplicative2<T,T2,R,A> applicative2(ZippingApplicativable<Function<? super T,Function<? super T2,? extends R>>> fn){
			
			return ()->fn;
		}
		public <T2> ZippingApplicative2<T,T2,R,A> applicative2(Function<? super T,Function<? super T2,? extends R>> fn){
			
			return applicative2(unit(fn));
		}
		public <T2> ZippingApplicative2<T,T2,R,A> applicative2(BiFunction<? super T,? super T2,? extends R> fn){
			
			return applicative2(unit(CurryVariance.curry2(fn)));
		}
		public <T2,T3> ZippingApplicative3<T,T2,T3,R,A> applicative3(ZippingApplicativable<Function<? super T,Function<? super T2,Function<? super T3,? extends R>>>> fn){
			
			return ()->fn;
		}
		public <T2,T3> ZippingApplicative3<T,T2,T3,R,A> applicative3(Function<? super T,Function<? super T2,Function<? super T3,? extends R>>> fn){
			
			return applicative3(unit(fn));
		}
		public <T2,T3> ZippingApplicative3<T,T2,T3,R,A> applicative3(TriFunction<? super T,? super T2,? super T3,? extends R> fn){
			
			return applicative3(unit(CurryVariance.curry3(fn)));
		}
		public <T2,T3,T4> ZippingApplicative4<T,T2,T3,T4,R,A> applicative4(ZippingApplicativable<Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,? extends R>>>>> fn){
			
			return ()->fn;
		}
		public <T2,T3,T4> ZippingApplicative4<T,T2,T3,T4,R,A> applicative4(Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,? extends R>>>> fn){
			
			return applicative4(unit(fn));
		}
		public <T2,T3,T4> ZippingApplicative4<T,T2,T3,T4,R,A> applicative4(QuadFunction<? super T,? super T2,? super T3,? super T4,? extends R> fn){
			
			return applicative4(unit(CurryVariance.curry4(fn)));
		}
		public <T2,T3,T4,T5> ZippingApplicative5<T,T2,T3,T4,T5,R,A> applicative5(ZippingApplicativable<Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>>> fn){
			
			return ()->fn;
		}
		public <T2,T3,T4,T5> ZippingApplicative5<T,T2,T3,T4,T5,R,A> applicative5(Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>> fn){
			
			return applicative5(unit(fn));
		}
		public <T2,T3,T4,T5> ZippingApplicative5<T,T2,T3,T4,T5,R,A	> applicative5(QuintFunction<? super T,? super T2,? super T3,? super T4,? super T5,? extends R> fn){
			
			return applicative5(unit(CurryVariance.curry5(fn)));
		}
	
	
}
