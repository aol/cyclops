package com.aol.cyclops.comprehensions.converters;

import lombok.val;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class MonadicConvertersTest {

	MonadicConverters converters;
	@Test
	public void testPriorityOrderingObjectToStreamShouldBeLast() {
		val converters = converters.getConverters();
		assertThat(converters.get(converters.size()-1),instanceOf(ObjectToStreamConverter.class));
	}

}
