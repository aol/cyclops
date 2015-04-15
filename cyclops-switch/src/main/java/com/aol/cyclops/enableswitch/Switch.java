package com.aol.cyclops.enableswitch;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Switch interface for handling features that can be enabled or disabled.
 * 
 * @author johnmcclean
 *
 * @param <F>
 */
public interface Switch<F> {

	boolean isEnabled();
	boolean isDisabled();
	
	F get();
	
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
	public static <F> Switch<F> from(Switch<F> from, F f){
		if(from.isEnabled())
			return enable(f);
		return disable(f);
	}
	
	/**
	 * Flatten a nested Switch
	 * 
	 * <pre>
	 * Enabled&lt;Enabled&lt;Disabled&gt;&gt; nested= Switch.enable(Switch.enable(Switch.disable(100)));
	 * </pre>
	 * 
	 * unwraps to disabled[100]
	 * 
	 * @return flattened switch
	 */
	default <X> Optional<Switch<X>> flatten(){
		Optional s = Optional.of(get()).flatMap(x->{
			if(x instanceof Switch)
				return ((Switch)x).flatten();
			else
				return Optional.of(Switch.from(this,x));
		});
		return (Optional<Switch<X>>)s;
	}
	
	/**
	 * Peek at current switch value
	 * 
	 * @param consumer Consumer to provide current value to
	 * @return This Switch
	 */
	default Switch<F> peek(Consumer<F> consumer){
		consumer.accept(get());
		return this;
	}
	
	/**
	 * @param map Create a new Switch with provided function
	 * @return switch from function
	 */
	default <X> Switch<X> flatMap(Function<F,Switch<X>> map){
		if(isDisabled())
			return (Switch<X>)this;
		return map.apply(get());
	}
	
	
	
	/**
	 * @param map transform the value inside this Switch into new Switch object
	 * @return new Switch with transformed value
	 */
	default <X> Switch<X> map(Function<F,X> map){
		if(isDisabled())
			return (Switch<X>)this;
		return enable(map.apply(get()));
	}
	
	/**
	 * Filter this Switch. If current value does not meet criteria,
	 * a disabled Switch is returned
	 * 
	 * @param p Predicate to test for
	 * @return Filtered switch
	 */
	default Switch<F> filter(Predicate<F> p){
		if(isDisabled())
			return this;
		if(!p.test(get()))
			return Switch.disable(get());
		return this;
	}
	
	/**
	 * Iterate over value in switch (single value, so one iteration)
	 * @param consumer to provide value to.
	 */
	default void forEach(Consumer<F> consumer){
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
	default Switch<F> flip(){
		
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
	default Stream<F> stream(){
		if(isEnabled())
			return Stream.of(get());
		else
			return Stream.of();
	}
	
	
}
