package com.aol.cyclops.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;

import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
public class AsValueTest {

	@Test
	public void testAsValue() {
		List list = new ArrayList();
		AsValue.asValue(new Child(10,20)).unapply().forEach(i->list.add(i));
		assertThat(list,equalTo(Arrays.asList(10,20)));
	}

	@AllArgsConstructor
	static class Parent{
		int val;
	}
	@Value
	static class Child extends Parent{
		int nextVal;
		public Child(int val,int nextVal) { super(val); this.nextVal = nextVal;}
	}
}
