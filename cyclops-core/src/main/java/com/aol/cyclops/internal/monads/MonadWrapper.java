package com.aol.cyclops.internal.monads;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.control.SequenceM;
import com.aol.cyclops.types.Decomposable;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@AllArgsConstructor
public class MonadWrapper<MONAD,T> implements Monad<MONAD,T>, Decomposable{
	@Wither
	private final Object monad;
	private final Class orgType;
	
	public MonadWrapper(Object monad){
		this.monad = monad;
		orgType= monad.getClass();
	}
	
	public static <MONAD,T> Monad<MONAD,T>  of(Object of) {
		return new MonadWrapper(of);
		
	}
	public MONAD unwrap(){
		return (MONAD)monad;
	}
	@Override
	public <X> AnyM<X> anyM(){
		return new BaseAnyMImpl<X>((Monad)this,orgType);	
	}
	@Override
	public SequenceM<T>  sequence(){
		if(monad instanceof SequenceM)
			return ((SequenceM)monad);
		return SequenceM.fromStream(this.stream());
	}
}
