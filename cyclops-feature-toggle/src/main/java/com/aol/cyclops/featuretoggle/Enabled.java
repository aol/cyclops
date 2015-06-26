package com.aol.cyclops.featuretoggle;

import java.util.Objects;

import lombok.Value;

/**
 * An enabled switch
 * 
 * <pre>
 * 
 * Switch&lt;Data&gt; data = Switch.enabled(data);
 * 
 * data.map(this::load);  //data will be loaded because Switch is of type Enabled
 * 
 * </pre>
 * @author johnmcclean
 *
 * @param <F> Type of value Enabled Switch holds
 */
@Value
public class Enabled<F> implements FeatureToggle<F>{

	
	
   

	private final F enabled;

    /* 
     *	@return
     * @see com.aol.cyclops.enableswitch.Switch#get()
     */
    public F get(){
    	return enabled;
    }
    
    /**
     * Constructs an Enabled Switch
     *
     * @param enabled The value of this Enabled Switch
     */
    public Enabled(F enabled) {
       this.enabled = enabled;
    }
    
    
    /* 
     * @param obj to check equality with
     * @return whether objects are equal
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return (obj == this) || (obj instanceof Enabled && Objects.equals(enabled, ((Enabled<?>) obj).enabled));
    }

    /* 
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(enabled);
    }

    /*  
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Enabled(%s)", enabled );
    }

	/* 
	 *	@return true - is Enabled
	 * @see com.aol.cyclops.enableswitch.Switch#isEnabled()
	 */
	@Override
	public final boolean isEnabled() {
		return true;
	}

	/* 
	 *	@return false - is not Disabled
	 * @see com.aol.cyclops.enableswitch.Switch#isDisabled()
	 */
	@Override
	public final boolean isDisabled() {
		
		return false;
	}
}
