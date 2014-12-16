package com.aol.simple.react;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Status {
	private final int completed;
	private final int errors;
	private final int total;
	private final long elapsedNanos;
}
