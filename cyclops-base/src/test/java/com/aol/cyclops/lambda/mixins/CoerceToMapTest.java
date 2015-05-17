package com.aol.cyclops.lambda.mixins;

import java.util.Map;

import lombok.Value;

import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import com.aol.cyclops.lambda.api.AsMap;
import static org.junit.Assert.assertThat;

public class CoerceToMapTest {

	@Test
	public void testMap(){
		Map<String,?> map = AsMap.asMap(new MyEntity(10,"hello"));
		System.out.println(map);
		assertThat(map.get("num"),equalTo(10));
		assertThat(map.get("str"),equalTo("hello"));
	}
	@Value static class MyEntity { int num; String str;}
}
