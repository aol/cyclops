package com.aol.cyclops.enableswitch;
import java.util.function.Function;

import java.util.function.Predicate;

import com.aol.cyclops.lambda.api.Comprehender;

/**
 * @author johnmcclean
 *
 *  Behaviour in cross-type flatMap is to create an empty instance for Disabled Switches, but always pass Enabled values on
 */
public class SwitchComprehender implements Comprehender<Switch>{

	@Override
	public Object filter(Switch t, Predicate p) {
		return t.filter(p);
	}

	@Override
	public Object map(Switch t, Function fn) {
		return t.map(fn);
	}

	@Override
	public Switch flatMap(Switch t, Function fn) {
		return t.flatMap(fn);
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Switch;
	}
	
	

	@Override
	public Switch of(Object o) {
		return Switch.enable(o);
	}

	@Override
	public Switch empty() {
		return Switch.disable(null);
	}

	@Override
	public Class getTargetClass() {
		return Switch.class;
	}
	@Override
	public Object handleReturnForCrossTypeFlatMap(Comprehender comp,Switch apply){
		return apply.matchType( c -> c.isType((Enabled e)-> comp.of(e.get()))
									 .isType( (Disabled d) -> comp.empty()));
	}

}
