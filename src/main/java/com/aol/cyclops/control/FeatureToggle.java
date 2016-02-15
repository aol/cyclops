package com.aol.cyclops.control;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.Applicativable;




/**
 * Switch interface for handling features that can be enabled or disabled.
 * 
 * @author johnmcclean
 *
 * @param <F>
 */
public interface FeatureToggle<F> extends Supplier<F>, Value<F>, Applicativable<F> {

	boolean isEnabled();
	boolean isDisabled();
	
	default <T> FeatureToggle<T> unit(T unit){
		return FeatureToggle.enable(unit);
	}
	<R> R visit(Function<? super F,? extends R> enabled, 
			Function<? super F, ? extends R> disabled);
	
	@Override
	default <R> FeatureToggle<R> patternMatch(R defaultValue,
			Function<CheckValues<F, R>, CheckValues<F, R>> case1) {
		
		return (FeatureToggle<R>)Applicativable.super.patternMatch(defaultValue, case1);
	}
	/**
	 * @return This monad, wrapped as AnyM
	 */
	public AnyM<F> anyM();
	/**
	 * @return This monad, wrapped as AnyM of Disabled
	 */
	public AnyM<F> anyMDisabled();
	/**
	 * @return This monad, wrapped as AnyM of Enabled
	 */
	public AnyM<F> anyMEnabled();
	F get();
	
	default ListX<F> unapply(){
		return ListX.of(get());
	}
	/**
	 * Create a new enabled switch
	 * 
	 * @param f switch value
	 * @return enabled switch
	 */
	public static <F> Enabled<F> enable(F f){
		return new Enabled<F>(f);
	}
	/**
	 * Create a new disabled switch
	 * 
	 * @param f switch value
	 * @return disabled switch
	 */
	public static <F> Disabled<F> disable(F f){
		return new Disabled<F>(f);
	}
	
	/**
	 * 
	 * 
	 * @param from Create a switch with the same type
	 * @param f but with this value (f)
	 * @return new switch
	 */
	public static <F> FeatureToggle<F> from(FeatureToggle<F> from, F f){
		if(from.isEnabled())
			return enable(f);
		return disable(f);
	}
	
	/**
	 * Flatten a nested Switch, maintaining top level enabled / disabled semantics
	 * 
	 * <pre>
	 * Enabled&lt;Enabled&lt;Disabled&gt;&gt; nested= Switch.enable(Switch.enable(Switch.disable(100)));
	 * </pre>
	 * 
	 * unwraps to enabled[100]
	 * 
	 * @return flattened switch
	 */
	default <X> FeatureToggle<X> flatten(){
		Optional s = Optional.of(get()).flatMap(x->{
			if(x instanceof FeatureToggle)
				return Optional.of(((FeatureToggle)x).flatten());
			else
				return Optional.of(FeatureToggle.from(this,x));
		});
		Object value = s.get();
		FeatureToggle<X> newSwitch = from((FeatureToggle<X>)this,((FeatureToggle<X>)value).get());
		return newSwitch;
	}
	
	/**
	 * Peek at current switch value
	 * 
	 * @param consumer Consumer to provide current value to
	 * @return This Switch
	 */
	default FeatureToggle<F> peek(Consumer<? super F> consumer){
		consumer.accept(get());
		return this;
	}
	
	/**
	 * @param map Create a new Switch with provided function
	 * @return switch from function
	 */
	default <X> FeatureToggle<X> flatMap(Function<? super F,? extends FeatureToggle<X>> map){
		if(isDisabled())
			return (FeatureToggle<X>)this;
		return map.apply(get());
	}
	
	
	
	/**
	 * @param map transform the value inside this Switch into new Switch object
	 * @return new Switch with transformed value
	 */
	default <X> FeatureToggle<X> map(Function<? super F,? extends X> map){
		if(isDisabled())
			return (FeatureToggle<X>)this;
		return enable(map.apply(get()));
	}
	
	/**
	 * Filter this Switch. If current value does not meet criteria,
	 * a disabled Switch is returned
	 * 
	 * @param p Predicate to test for
	 * @return Filtered switch
	 */
	default FeatureToggle<F> filter(Predicate<F> p){
		if(isDisabled())
			return this;
		if(!p.test(get()))
			return FeatureToggle.disable(get());
		return this;
	}
	
	/**
	 * Iterate over value in switch (single value, so one iteration)
	 * @param consumer to provide value to.
	 */
	default void forEach(Consumer<? super F> consumer){
		if(isDisabled())
			return;
		consumer.accept(get());
	}
	/**
	 * @return transform this Switch into an enabled Switch
	 */
	default Enabled<F> enable(){
		return new Enabled<F>(get()); 
	}
	/**
	 * @return transform this Switch into a disabled Switch
	 */
	default Disabled<F> disable(){
		return new Disabled<F>(get()); 
	}
	/**
	 * @return flip this Switch
	 */
	default FeatureToggle<F> flip(){
		
		if(isEnabled())
			return disable(get());
		else
			return enable(get());
	}
	
	
	/**
	 * @return Optional.empty() if disabled, Optional containing current value if enabled
	 */
	default Optional<F> optional(){
		return stream().findFirst();	
	}
	
	/**
	 * @return emty Stream if disabled, Stream with current value if enabled.
	 */
	@Override
	default ReactiveSeq<F> stream(){
		if(isEnabled())
			return ReactiveSeq.of(get());
		else
			return ReactiveSeq.of();
	}
	@Override
	default Iterator<F> iterator() {
	
		return Applicativable.super.iterator();
	}
	
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
	@lombok.Value
	public static class Enabled<F> implements FeatureToggle<F>{

		
		
	   

		private final F enabled;
		/**
		 * @return This monad, wrapped as AnyM
		 */
		public AnyM<F> anyM(){
			return AnyM.ofValue(this);
		}
		/**
		 * @return This monad, wrapped as AnyM of Disabled
		 */
		public AnyM<F> anyMDisabled(){
			return  AnyM.ofMonad(Optional.empty());
		}
		/**
		 * @return This monad, wrapped as AnyM of Enabled
		 */
		public AnyM<F> anyMEnabled(){
			return anyM();
		}
		/**
		 * Create a new enabled switch
		 * 
		 * @param f switch value
		 * @return enabled switch
		 */
		public static <F> Enabled<F> of(F f){
			return new Enabled<F>(f);
		}
		/**
		 * Create a new enabled switch
		 * 
		 * @param f switch value
		 * @return enabled switch
		 */
		public static <F> AnyM<F> anyMOf(F f){
			return new Enabled<F>(f).anyM();
		}
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
	     Enabled(F enabled) {
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
		/* (non-Javadoc)
		 * @see com.aol.cyclops.featuretoggle.FeatureToggle#when(java.util.function.Function, java.util.function.Supplier)
		 */
		@Override
		public <R> R visit(Function<? super F, ? extends R> enabled, Function<? super F, ? extends R> disabled) {
			return enabled.apply(get());
		}
	}
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
		@lombok.Value
		public static class Disabled<F> implements FeatureToggle<F>{

			
			private final F disabled;
			


		    /**
		     * Constructs a left.
		     *
		     * @param disabled The value of this Left
		     */
		    Disabled(F disabled) {
		        this.disabled = disabled;
		    }
		    
		    /**
			 * @return This monad, wrapped as AnyM
			 */
			public AnyM<F> anyM(){
				return AnyM.fromOptional(Optional.empty());
			}
			/**
			 * @return This monad, wrapped as AnyM of Disabled
			 */
			public AnyM<F> anyMDisabled(){
				return AnyM.ofValue(this);
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
			/* (non-Javadoc)
			 * @see com.aol.cyclops.featuretoggle.FeatureToggle#when(java.util.function.Function, java.util.function.Supplier)
			 */
			@Override
			public <R> R visit(Function<? super F, ? extends R> enabled, Function<? super F, ? extends R> disabled) {
				return disabled.apply(get());
			}
		}
	
}
