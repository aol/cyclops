package com.aol.simple.react.stream.lazy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

/**
 * Configuration for incremental parallel reduction
 * 
 * batchSize and parallel indicates whether parallel reduction is enabled
 * 
 * @author johnmcclean
 *
 */
@Builder
@Wither
@Value
@AllArgsConstructor
public class ParallelReductionConfig {

	int batchSize;
	boolean parallel;
	
	public static final ParallelReductionConfig defaultValue = new ParallelReductionConfig(10,false);
}
