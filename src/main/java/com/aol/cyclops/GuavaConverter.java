package com.aol.cyclops;

import java.util.Optional;

public class GuavaConverter {

	static class Option{
		public static <T> com.google.common.base.Optional<T> fromJDK(Optional<T> o){
			return com.google.common.base.Optional.fromNullable(o.orElse(null));
		}
	}
	
}
