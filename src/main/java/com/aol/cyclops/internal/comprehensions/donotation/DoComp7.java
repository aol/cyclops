
package com.aol.cyclops.internal.comprehensions.donotation;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.BaseStream;

import org.pcollections.PStack;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.util.function.CurryVariance;
import com.aol.cyclops.util.function.HeptFunction;
public class DoComp7<T1,T2,T3,T4,T5,T6,T7> extends DoComp{
		public DoComp7(PStack<Entry> assigned,Class orgType) {
			super(assigned,orgType);
			
		}
	
		

		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> reader(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Reader<?,? extends T8>>>>>>>> f){
			return new DoComp8<>(addToAssigned(f),getOrgType());
			
		}
		
		

		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> iterable(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Iterable<T8>>>>>>>> f){
			return new DoComp8<>(addToAssigned(f),getOrgType());
			
		}
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> publisher(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Publisher<T8>>>>>>>> f){
            return new DoComp8<>(addToAssigned(f),getOrgType());
            
        }
		
		
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> stream(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,BaseStream<T8,?>>>>>>>> f){
			return new DoComp8<>(addToAssigned(f),getOrgType());
			
		}
		


		
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> optional(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Optional<T8>>>>>>>> f){
			return new DoComp8<>(addToAssigned(f),getOrgType());
			
		}
		

		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> future(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,CompletableFuture<T8>>>>>>>> f){
			return new DoComp8<>(addToAssigned(f),getOrgType());
			
		}
		


	
		public <T8> DoComp8<T1,T2,T3,T4,T5,T6,T7,T8> anyM(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,AnyM<T8>>>>>>>> f){
			return new DoComp8<>(addToAssigned(f),getOrgType());
			
		}
		

		
		public <R> AnyMSeq<R> yield(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,Function<? super T5,Function<? super T6,Function<? super T7,? extends R>>>>>>> f){
			if(getOrgType()!=null)
				return new MonadWrapper<>(this.yieldInternal(f),this.getOrgType()).anyMSeq();
			else
				return AnyM.ofSeq(this.yieldInternal(f));
		}
		public <R> AnyMSeq<R> yield(HeptFunction<? super T1,? super T2,? super T3,? super T4,? super T5,? super T6,? super T7,? extends R> f){
            return this.yield(CurryVariance.curry7(f));
        }
		
		
		
		public  DoComp7<T1,T2,T3,T4,T5,T6,T7> filter(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Function<? super T7,Boolean>>>>>>> f){
			return new DoComp7<>(getAssigned().plus(getAssigned().size(),new Entry("$$internalGUARD"+getAssigned().size(),new Guard(f))),getOrgType());
		}
		
	}

