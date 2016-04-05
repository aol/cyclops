
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
import com.aol.cyclops.util.function.HexFunction;


public class DoComp6<T1,T2,T3,T4,T5,T6> extends DoComp{
		public DoComp6(PStack<Entry> assigned, Class orgType) {
			super(assigned,orgType);
			
		}
	

		
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> reader(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Reader<?,? extends T7>>>>>>> f){
			return new DoComp7<>(addToAssigned(f),getOrgType());
			
		}

		
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> iterable(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Iterable<T7>>>>>>> f){
			return new DoComp7<>(addToAssigned(f),getOrgType());
			
		}
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> publisher(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Publisher<T7>>>>>>> f){
            return new DoComp7<>(addToAssigned(f),getOrgType());
            
        }
		
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> stream(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,BaseStream<T7,?>>>>>>> f){
			return new DoComp7<>(addToAssigned(f),getOrgType());
			
		}
		

		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> optional(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Optional<T7>>>>>>> f){
			return new DoComp7<>(addToAssigned(f),getOrgType());
			
		}
		
		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> future(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,CompletableFuture<T7>>>>>>> f){
			return new DoComp7<>(addToAssigned(f),getOrgType());
			
		}
		

		public <T7> DoComp7<T1,T2,T3,T4,T5,T6,T7> anyM(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,AnyM<T7>>>>>>> f){
			return new DoComp7<>(addToAssigned(f),getOrgType());
			
		}
		
		
		public <R> AnyMSeq<R> yield(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,Function<? super T5,Function<? super T6,? extends R>>>>>> f){
			if(getOrgType()!=null)
				return new MonadWrapper<>(this.yieldInternal(f),this.getOrgType()).anyMSeq();
			else
				return AnyM.ofSeq(this.yieldInternal(f));
		}
		public <R> AnyMSeq<R> yield6(HexFunction<? super T1,? super T2,? super T3,? super T4,? super T5,? super T6,? extends R> f){
            return this.yield(CurryVariance.curry6(f));
        }
		
		
		public  DoComp6<T1,T2,T3,T4,T5,T6> filter(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Function<? super T6,Boolean>>>>>> f){
			return new DoComp6<>(getAssigned().plus(getAssigned().size(),new Entry("$$internalGUARD"+getAssigned().size(),new Guard(f))),getOrgType());
		}
		
	}

