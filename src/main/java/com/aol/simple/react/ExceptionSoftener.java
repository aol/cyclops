package com.aol.simple.react;

import java.lang.reflect.Field;
import java.util.Optional;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

@Slf4j
class ExceptionSoftener {

	static enum singleton { factory;
		@Getter
	    private final ExceptionSoftener instance;
	    private singleton(){
	    	instance = new  ExceptionSoftener ();
	    }
	}
	
	private final Optional<Unsafe> unsafe = getUnsafe();
	
	void throwSoftenedException(final Exception e) {
		unsafe.ifPresent(u -> u.throwException(e));
	}
	
	private static Optional<Unsafe> getUnsafe() {

		try {

			final Field field = Unsafe.class.getDeclaredField("theUnsafe");

			field.setAccessible(true);

			return Optional.of((Unsafe) field.get(null));

		} catch (Exception ex) {

			log.error(ex.getMessage());

		}
		return Optional.empty();

	}
}
