package com.aol.cyclops.featuretoggle;

import java.util.Objects;
import java.util.Optional;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.monads.AnyM;

import lombok.Value;

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
@Value
public class Disabled<F> implements FeatureToggle<F>{

	
	private final F disabled;
	


    /**
     * Constructs a left.
     *
     * @param disabled The value of this Left
     */
    public Disabled(F disabled) {
        this.disabled = disabled;
    }
    
    /**
	 * @return This monad, wrapped as AnyM
	 */
	public AnyM<F> anyM(){
		return AsAnyM.notTypeSafeAnyM(Optional.empty());
	}
	/**
	 * @return This monad, wrapped as AnyM of Disabled
	 */
	public AnyM<F> anyMDisabled(){
		return  AsAnyM.notTypeSafeAnyM(this);
	}
	/**
	 * @return This monad, wrapped as AnyM of Enabled
	 */
	public AnyM<F> anyMEnabled(){
		return anyM();
	}
	/**
	 * Create a new disabled switch
	 * 
	 * @param f switch value
	 * @return disabled switch
	 */
	public static <F> Disabled<F> of(F f){
		return new Disabled<F>(f);
	}
	/**
	 * Create a new disabled switch
	 * 
	 * @param f switch value
	 * @return disabled switch
	 */
	public static <F> AnyM<F> anyMOf(F f){
		return new Disabled<F>(f).anyM();
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