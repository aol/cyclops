package com.aol.simple.react.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

@AllArgsConstructor
@Getter
@Wither
@Builder
public class MaxActive {

	private final int maxActive;
	private final int reduceTo;
	
	
	@AllArgsConstructor
	@Getter
	public enum defaultValue {
		factory(new MaxActive(70,30));
		private final MaxActive instance;
	}
}
