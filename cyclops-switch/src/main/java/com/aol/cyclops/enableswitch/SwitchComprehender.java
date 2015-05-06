package com.aol.cyclops.enableswitch;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.lambda.api.Comprehender;
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
	public Switch of() {
		return Switch.disable(null);
	}

}
