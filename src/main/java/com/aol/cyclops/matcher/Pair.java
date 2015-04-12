
package com.aol.cyclops.matcher;

import java.io.Serializable;

import lombok.Getter;

class Pair<T, V> implements Serializable {

	private static final long serialVersionUID = 1L;
	@Getter
	private final T first;
	@Getter
	private final V second;

	public Pair(final T first, final V second) {
		this.first = first;
		this.second = second;
	}

}
