package com.aol.simple.react.stream.lazy;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

@Builder
@Wither
@Value
public class ParallelReductionConfig {

	int batchSize;
	boolean parallel;
	
	public static final ParallelReductionConfig defaultValue = new ParallelReductionConfig(10,false);
}
