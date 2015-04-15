package com.aol.cyclops.enableswitch;
import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Option;

public interface Switch<F> {

	default boolean isEnabled(){
		return this.isRight();
	}
	boolean isRight();
	boolean isLeft();
	default boolean isDisabled(){
		return this.isLeft();
	}
	
	default Switch<F> flipTheSwitch(){
		Either<F,F> either = getDelegate().flip();
		if(either.isLeft())
			return new Disabled<F>(either.left());
		else
			return new Enabled<F>(either.right());
	}
	
	default Option<F> enabled(){
		return getDelegate().rightOption();
		
	}
	Either<F, F> getDelegate();
	
	
}
