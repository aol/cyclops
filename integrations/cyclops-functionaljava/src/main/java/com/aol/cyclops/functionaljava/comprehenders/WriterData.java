package com.aol.cyclops.functionaljava.comprehenders;

import lombok.Value;
import fj.Monoid;
@Value
public class WriterData<W, A> {
	private final A val;
	private final W logValue;
    private final Monoid<W> monoid;

}
