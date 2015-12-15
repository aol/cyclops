package com.aol.cyclops.invokedynamic;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
public class ExceptionSoftener2Test {

	@Test
	public void testSoftenBooleanSupplier() {
		assertThat(ExceptionSoftener.softenBooleanSupplier(()->true).getAsBoolean(),equalTo(true));
	}

	@Test(expected=IOException.class)
	public void testSoftenBooleanSupplierException() {
		ExceptionSoftener.softenBooleanSupplier(()->{throw new IOException();});
	
	}

	@Test
	public void testSoftenIntSupplier() {
		assertThat(ExceptionSoftener.softenIntSupplier(()->1).getAsInt(),equalTo(1));
	}
	@Test(expected=IOException.class)
	public void testSoftenIntSupplierException() {
		ExceptionSoftener.softenIntSupplier(()->{throw new IOException();});
	
	}

	@Test
	public void testSoftenLongSupplierCheckedLongSupplier() {
		assertThat(ExceptionSoftener.softenLongSupplier(()->1l).getAsLong(),equalTo(1l));
	}
	@Test(expected=IOException.class)
	public void testSoftenLongSupplierException() {
		ExceptionSoftener.softenLongSupplier(()->{throw new IOException();});
	
	}

	@Test
	public void testSoftenDoubleSupplier() {
		assertThat(ExceptionSoftener.softenDoubleSupplier(()->1d).getAsDouble(),equalTo(1d));
	}
	@Test(expected=IOException.class)
	public void testSoftenDpubleSupplierException() {
		ExceptionSoftener.softenDoubleSupplier(()->{throw new IOException();});
	
	}

	@Test
	public void testSoftenCheckedFunctionOfTR() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenIntFunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenLongFunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenDoubleFunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenLongToDoubleFunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenLongToIntFunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenIntToDoubleFunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenIntToLongFunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenDoubleToIntFunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenDoubleToLongFunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenCheckedBiFunctionOfT1T2R() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenCheckedPredicateOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenDoublePredicate() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenIntPredicateCheckedIntPredicate() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenIntPredicateCheckedLongPredicate() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenCheckedBiPredicateOfT1T2() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenCheckedConsumerOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenDoubleConsumer() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenIntConsumer() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenLongConsumer() {
		fail("Not yet implemented");
	}

	@Test
	public void testSoftenCheckedBiConsumerOfT1T2() {
		fail("Not yet implemented");
	}

}
