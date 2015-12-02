package com.aol.cyclops.lambda.mixins;

import java.util.Map;

import lombok.Value;

import org.junit.Test;
import static org.hamcrest.Matchers.*;
import com.aol.cyclops.lambda.api.AsMappable;
import static org.junit.Assert.assertThat;

public class CoerceToMapTest {

	@Test
	public void testMap(){
		Map<String,?> map = AsMappable.asMappable(new MyEntity(10,"hello")).toMap();
		System.out.println(map);
		assertThat(map.get("num"),equalTo(10));
		assertThat(map.get("str"),equalTo("hello"));
	}
	@Test
	public void testMapNulls(){
		Map<String,?> map = AsMappable.asMappable(new MyEntity(10,null)).toMap();
		System.out.println(map);
		assertThat(map.get("num"),equalTo(10));
		assertThat(map.get("str"),nullValue());
	}
	@Value static class MyEntity { int num; String str;}
}
