package com.aol.cyclops.enableswitch;

import java.util.Objects;

import lombok.experimental.Delegate;

import com.googlecode.totallylazy.Either;
import com.googlecode.totallylazy.Right;
import com.googlecode.totallylazy.Tuple;

public class Enabled<F> extends Either<F, F> implements Switch<F>{

	
	public static void main(String[] args){
		Runnable r = ()->System.out.println("hello");
		
		new Enabled<Runnable>(r).enabled().forEach(runner  -> runner.run());
	}
    private static final long serialVersionUID = 1L;

    @Delegate
	Right<F,F> enabled;

    public Either<F,F> getDelegate(){
    	return enabled;
    }
    
    /**
     * Constructs a left.
     *
     * @param left The value of this Left
     */
    public Enabled(F enabled) {
       this.enabled =  (Right<F, F>) Either.right(enabled);
    }
    
    

   
    @Override
    public boolean equals(Object obj) {
        return (obj == this) || (obj instanceof Enabled && Objects.equals(enabled, ((Enabled<?>) obj).enabled));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enabled);
    }

    @Override
    public String toString() {
        return String.format("enabled(%s)", enabled );
    }
}
