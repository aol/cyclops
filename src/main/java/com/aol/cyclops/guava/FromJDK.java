package com.aol.cyclops.guava;

import java.util.Optional;

public class FromJDK {

		public static <T> com.google.common.base.Optional<T> fromJDK(Optional<T> o){
			return com.google.common.base.Optional.fromNullable(o.orElse(null));
		}
}
