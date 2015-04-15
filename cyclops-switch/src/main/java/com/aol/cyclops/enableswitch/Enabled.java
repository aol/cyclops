package com.aol.cyclops.enableswitch;

import java.util.Objects;

public class Enabled<F> implements Switch<F>{

	
	public static void main(String[] args){
		Runnable r = ()->System.out.println("hello");
		
		new Enabled<Runnable>(r).stream().forEach(runner  -> runner.run());
	}
    private static final long serialVersionUID = 1L;

    F enabled;

    public F get(){
    	return enabled;
    }
    
    /**
     * Constructs a left.
     *
     * @param left The value of this Left
     */
    public Enabled(F enabled) {
       this.enabled = enabled;
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

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isDisabled() {
		
		return false;
	}
}
