package com.aol.cyclops.comprehensions.donotation.typed;

import java.util.function.Function;

import org.pcollections.PStack;

import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.comprehensions.donotation.typed.DoBuilderModule.Assignment;
import com.aol.cyclops.comprehensions.donotation.typed.DoBuilderModule.Entry;
import com.aol.cyclops.comprehensions.donotation.typed.DoBuilderModule.Guard;

public class DoComp8<T,T1,T2,T3,T4,T5,T6,T7> extends DoComp{
	public DoComp8(PStack<Entry> assigned, Class orgType) {
		super(assigned,orgType);
		
	}
	public  DoComp8<T,T1,T2,T3,T4,T5,T6,T7> filter(Function<T,Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<T7,Boolean>>>>>>>> f){
		return new DoComp8(getAssigned().plus(getAssigned().size(),new Entry("$$internalGUARD"+getAssigned().size(),new Guard(f))),getOrgType());
	}
	public <R> AnyM<R> yield(Function<T,Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<T7,? extends R>>>>>>>>  f){
		if(getOrgType()!=null)
			return new MonadWrapper(this.yieldInternal(f),this.getOrgType()).anyM();
		else
			return AnyM.ofMonad(this.yieldInternal(f));
	}
}