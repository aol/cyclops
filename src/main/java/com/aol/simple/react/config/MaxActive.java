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
	private final int parallelReduceBatchSize;
	
	
	@AllArgsConstructor
	@Getter
	public enum defaultValue {
		factory(new MaxActive(Runtime.getRuntime().availableProcessors()*2,Runtime.getRuntime().availableProcessors(),1000));
		private final MaxActive instance;
	}
}
