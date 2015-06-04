package com.aol.cyclops.comprehensions.converters;

import lombok.val;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.pcollections.PStack;

import com.aol.cyclops.lambda.api.MonadicConverter;

public class MonadicConvertersTest {

	MonadicConverters converters;
	@Test
	public void testPriorityOrderingObjectToStreamShouldBeLast() {
		PStack<MonadicConverter> converters2 = converters.getConverters();
		assertThat(converters2.get(converters2.size()-1),instanceOf(ObjectToStreamConverter.class));
	}

}
