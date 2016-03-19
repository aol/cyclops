package com.aol.cyclops.internal.monads;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;

public class AnyMValueImpl<T> extends BaseAnyMImpl<T> implements AnyMValue<T> {
	
   public Xor<AnyMValue<T>,AnyMSeq<T>> matchable(){
        return Xor.secondary(this);
    }
	public AnyMValueImpl(Monad<T> monad, Class initialType) {
        super(monad, initialType);
       
    }
  
	private <T> AnyMValueImpl<T> with(Monad<T> anyM){
        
        return new AnyMValueImpl<T>(anyM,initialType);
    }
	
	private <T> AnyMValueImpl<T> with(AnyM<T> anyM){
		
		return (AnyMValueImpl<T>)anyM;
	}
	
	
	public <R> AnyMValue<R> flatMapFirst(Function<? super T, ? extends AnyM<? extends R>> fn) {
        return with(super.flatMapInternal(fn));

    }
	@Override
	public ReactiveSeq<T> reactiveSeq() {
		return stream();
	}

	@Override
	public T get() {
		return super.get();
	}

	
	@Override
	public <T> AnyMValue<T> emptyUnit() {
		return new AnyMValueImpl(monad.empty(),initialType);
	}
	public AnyMValue<List<T>> replicateM(int times){
        
        return monad.replicateM(times).anyMValue();      
    }
	
	@Override
	public AnyMValue<T> filter(Predicate<? super T> p) {
		return with(super.filterInternal(p));
	}

	@Override
	public AnyMValue<T> peek(Consumer<? super T> c) {
		return with(super.peekInternal(c));
	}

	
	@Override
	public AnyMValue<List<T>> aggregate(AnyM<T> next) {
		return (AnyMValue<List<T>>)super.aggregate(next);
	}

	@Override
	public <T> AnyMValue<T> unit(T value) {
		return AnyM.ofValue(monad.unit(value));
	}

	@Override
	public <T> AnyMValue<T> empty() {
	    return with(new AnyMValueImpl(monad.empty(),initialType));
	}

	

	@Override
	public <NT> ReactiveSeq<NT> toSequence(Function<? super T, ? extends Stream<? extends NT>> fn) {
		return super.toSequence(fn);
	}

	

	@Override
	public T getMatchable() {
		return get();
	}

	@Override
	public ReactiveSeq<T> stream() {
		return super.stream();
	}


	@Override
	public <R> AnyMValue<R> map(Function<? super T, ? extends R> fn) {
		return with(super.mapInternal(fn));
	}

	

	@Override
    public <R> AnyMValue<R> bind(Function<? super T, ?> fn) {
        
        return with(super.bindInternal(fn));
    }

    @Override
	public <T1> AnyMValue<T1> flatten() {
		return with(super.flattenInternal());
	}

	@Override
	public <R1, R> AnyMValue<R> forEach2(Function<? super T, ? extends AnyMValue<R1>> monad,
			                    Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
	    return AnyM.ofValue(For.anyM((AnyM<T>)this)
	                          .anyM(u -> monad.apply(u))
	                          .yield(yieldingFunction).unwrap());
	}

	@Override
	public <R1, R> AnyMValue<R> forEach2(Function<? super T, ? extends AnyMValue<R1>> monad,
			Function<? super T, Function<? super R1, Boolean>> filterFunction,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
	    return AnyM.ofValue(For.anyM((AnyM<T>)this)
                   .anyM(u -> monad.apply(u))
                .filter(filterFunction)
                .yield(yieldingFunction).unwrap());
		
	}

	@Override
	public <R1, R2, R> AnyMValue<R> forEach3(Function<? super T, ? extends AnyMValue<R1>> monad1,
			Function<? super T, Function<? super R1, ? extends AnyMValue<R2>>> monad2,
			Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {
	    
	    return AnyM.ofValue(For.anyM((AnyM<T>)this)
	                          .anyM(u -> monad1.apply(u))
	                          .anyM(a -> b -> monad2.apply(a).apply(b))
	                          .filter(filterFunction)
	                          .yield(yieldingFunction).unwrap());
	}

	@Override
	public <R1, R2, R> AnyMValue<R> forEach3(Function<? super T, ? extends AnyMValue<R1>> monad1,
			Function<? super T, Function<? super R1, ? extends AnyMValue<R2>>> monad2,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {
	    return AnyM.ofValue(For.anyM((AnyM<T>)this)
                .anyM(u -> monad1.apply(u))
                .anyM(a -> b -> monad2.apply(a).apply(b))
                .yield(yieldingFunction).unwrap());
	}

	@Override
	public <R> AnyMValue<R> flatMap(Function<? super T, ? extends AnyMValue<? extends R>> fn) {
		return with(super.flatMapInternal(fn));
	}

	

	

	

	@Override
	public <T> T unwrap() {
		return super.unwrap();
	}
	@Override
    public String toString() {
        return mkString();
    }
    @Override
    public int hashCode() {
       return Objects.hashCode(unwrap());
    }
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof AnyMValue))
            return false;
        AnyMValue v2 = (AnyMValue)obj;
        return this.toMaybe().equals(v2.toMaybe());
        
       
    }
    public <R> AnyMValue<R> applyM(AnyMValue<Function<? super T,? extends R>> fn){
        return monad.applyM(((AnyMValueImpl<Function<? super T,? extends R>>)fn).monad).anyMValue();
        
    }
	
    @Override
    public <NT> ReactiveSeq<NT> toSequence() {
        return super.toSequence();
    }
}
