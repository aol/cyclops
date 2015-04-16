package com.aol.cyclops.enableswitch;

import java.util.Objects;

/**
 * An disabled switch
 * 
 * <pre>
 * 
 * Switch&lt;Data&gt; data = Switch.disabled(data);
 * 
 * data.map(this::load);  //data will NOT be loaded because Switch is of type Disabled
 * 
 * </pre>
 * @author johnmcclean
 *
 * @param <F> Type of value Enabled Switch holds
 */
public class Disabled<F> implements Switch<F>{

	
	private final F disabled;
	


    /**
     * Constructs a left.
     *
     * @param left The value of this Left
     */
    public Disabled(F disabled) {
        this.disabled = disabled;
    }
    
      
    /* 
     *	@return value of this Disabled
     * @see com.aol.cyclops.enableswitch.Switch#get()
     */
    public F get(){
    	return disabled;
    }
   

    /* 
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return (obj == this) || (obj instanceof Disabled && Objects.equals(disabled, ((Disabled<?>) obj).disabled));
    }

    /* 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(disabled);
    }

    /* 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Disabled(%s)", disabled );
    }

	/* 
	 *	@return false - is NOT enabled
	 * @see com.aol.cyclops.enableswitch.Switch#isEnabled()
	 */
	@Override
	public final boolean isEnabled() {
		return false;
	}

	/* 
	 *	@return true - is Disabled
	 * @see com.aol.cyclops.enableswitch.Switch#isDisabled()
	 */
	@Override
	public final boolean isDisabled() {
		return true;
	}
}