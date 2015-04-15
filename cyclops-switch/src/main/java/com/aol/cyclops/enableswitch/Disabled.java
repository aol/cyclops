package com.aol.cyclops.enableswitch;

import java.util.Objects;





public class Disabled<F> implements Switch<F>{

	
	private final F disabled;
	
	public static void main(String[] args){
		Runnable r = ()->System.out.println("hello");
		new Disabled<Runnable>(r).stream().forEach(runner  -> runner.run());
		
		
	}
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a left.
     *
     * @param left The value of this Left
     */
    public Disabled(F disabled) {
        this.disabled = disabled;
    }
    
      
    public F get(){
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

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isDisabled() {
		return true;
	}
}