package com.aol.cyclops.value;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aol.cyclops.lambda.api.ReflectionCache;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;
import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.builders.MatchingInstance;
import com.aol.cyclops.matcher.builders.PatternMatcher;
import com.aol.cyclops.matcher.builders.SimplestCase;
import com.aol.cyclops.matcher.builders._Case;

public class AsStreamableValue {
	public static <T> StreamableValue<T> asValue(T toCoerce){
		return new CoercedStreamableValue<T>(toCoerce);
	}
	@lombok.Value
	public static class CoercedStreamableValue<T> implements StreamableValue<T>{
		private final T value;
		
		public T getMatchable(){
			return value;
		}
			
	}
}
