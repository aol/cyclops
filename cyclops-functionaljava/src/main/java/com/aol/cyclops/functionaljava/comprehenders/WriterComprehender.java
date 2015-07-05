package com.aol.cyclops.functionaljava.comprehenders;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.streams.StreamUtils;

import fj.Monoid;
import fj.Semigroup;
import fj.data.Option;
import fj.data.Writer;

public class WriterComprehender implements Comprehender<Writer>{
	
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, Writer apply) {
		return apply.value();
	}

	@Override
	public Object map(Writer t, Function fn) {
		return t.map(r->fn.apply(r));
	}

	@Override
	public Object flatMap(Writer t, Function fn) {
		
		return t.flatMap(r->fn.apply(r));
	}

	@Override
	public Writer of(Object o) {
		if(o instanceof WriterData){
			WriterData data =(WriterData)o;
			return Writer.unit(data.getVal(), data.getLogValue(), data.getMonoid());
		}
		else if(o instanceof Iterable){
			List l = (List) StreamUtils.stream((Iterable)o).collect(Collectors.toList());
			if(l.size()==1)
				return Writer.unit(l.get(0));
			if(l.size()==2){
				Monoid m = (l.get(0) instanceof Monoid) ? (Monoid)l.get(0) : (Monoid)l.get(1);
				Object a = (l.get(0) instanceof Monoid) ? l.get(1) : l.get(0);
				return Writer.unit(a,m);
			}
			if(l.size()==3){
				Object a = l.get(0);
				Object w = l.get(1);
				Monoid m = (Monoid)l.get(2);
				return Writer.unit(a,w,m);
				
			}
				
		}
		
			return Writer.unit(o);
	}

	@Override
	public Writer empty() {
		return Writer.unit(Option.none(), Monoid.monoid(Semigroup.semigroup((a,b)->Option.none()),Option.none()));
	}

	@Override
	public Class getTargetClass() {
		return Writer.class;
	}

}
