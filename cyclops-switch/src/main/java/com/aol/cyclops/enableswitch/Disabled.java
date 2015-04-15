package com.aol.cyclops.enableswitch;

import java.util.Objects;

import lombok.experimental.Delegate;

import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Left;

public class Disabled<F> extends Either<F, F> implements Switch<F>{

	@Delegate
	Left<F,F> disabled;
	
	public static void main(String[] args){
		Runnable r = ()->System.out.println("hello");
		new Disabled<Runnable>(r).enabled().forEach(runner  -> runner.run());
		
		new Disabled<Runnable>(r).flip();
	}
    private static final long serialVersionUID = 1L;

    

    /**
     * Constructs a left.
     *
     * @param left The value of this Left
     */
    public Disabled(F disabled) {
        this.disabled = (Left<F, F>) Either.left(disabled);
    }
    
    
    public Either<F,F> getDelegate(){
    	return disabled;
    }
    

   

    @Override
    public boolean equals(Object obj) {
        return (obj == this) || (obj instanceof Disabled && Objects.equals(disabled, ((Disabled<?>) obj).disabled));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(disabled);
    }

    @Override
    public String toString() {
        return String.format("Disabled(%s)", disabled );
    }
}