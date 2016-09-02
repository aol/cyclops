package com.aol.cyclops.lambda.mixins;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

import com.aol.cyclops.types.mixins.AsMappable;

import lombok.Value;

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
