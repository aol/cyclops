package com.aol.cyclops.invokedynamic;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.aol.cyclops.control.ExceptionSoftener;
public class ExceptionSoftener2Test {

	@Test
	public void testSoftenBooleanSupplier() {
		assertThat(ExceptionSoftener.softenBooleanSupplier(()->true).getAsBoolean(),equalTo(true));
	}

	@Test(expected=IOException.class)
	public void testSoftenBooleanSupplierException() {
		ExceptionSoftener.softenBooleanSupplier(()->{throw new IOException();}).getAsBoolean();
	
	}

	@Test
	public void testSoftenIntSupplier() {
		assertThat(ExceptionSoftener.softenIntSupplier(()->1).getAsInt(),equalTo(1));
	}
	@Test(expected=IOException.class)
	public void testSoftenIntSupplierException() {
		ExceptionSoftener.softenIntSupplier(()->{throw new IOException();}).getAsInt();
	
	}

	@Test
	public void testSoftenLongSupplierCheckedLongSupplier() {
		assertThat(ExceptionSoftener.softenLongSupplier(()->1l).getAsLong(),equalTo(1l));
	}
	@Test(expected=IOException.class)
	public void testSoftenLongSupplierException() {
		ExceptionSoftener.softenLongSupplier(()->{throw new IOException();}).getAsLong();
	
	}

	@Test
	public void testSoftenDoubleSupplier() {
		assertThat(ExceptionSoftener.softenDoubleSupplier(()->1d).getAsDouble(),equalTo(1d));
	}
	@Test(expected=IOException.class)
	public void testSoftenDpubleSupplierException() {
		ExceptionSoftener.softenDoubleSupplier(()->{throw new IOException();}).getAsDouble();
	
	}
	

	@Test
	public void testSoftenCheckedFunctionOfTR() {
		assertThat(ExceptionSoftener.softenFunction((Integer a)->a+1).apply(1),equalTo(2));
	}
	@Test(expected=IOException.class)
	public void testSoftenCheckedFunctionOfTRException() {
		ExceptionSoftener.softenFunction(this::load).apply("input");
	
	}
	public String load(String file) throws IOException{
		throw new IOException();
	}
	

	public void testSoftenIntFunction() {
		assertThat(ExceptionSoftener.softenIntFunction(a->a+1).apply(1),equalTo(2));
	}
	@Test(expected=IOException.class)
	public void testSoftenIntFunctionException() {
		ExceptionSoftener.softenIntFunction(this::loadId).apply(2);
	}
	public int loadId(int id) throws IOException{
		throw new IOException();
	}
	public long loadIdLong(long id) throws IOException{
		throw new IOException();
	}

	
	public void testSoftenLongFunction() {
		assertThat(ExceptionSoftener.softenLongFunction(a->a+1).apply(1),equalTo(2));
	}

	@Test(expected=IOException.class)
	public void testSoftenLongFunctionException() {
		ExceptionSoftener.softenLongFunction(this::loadIdLong).apply(2);
	}
	public double loadIdDouble(double id) throws IOException{
		throw new IOException();
	}
	
	public void testSoftenDoubleFunction() {
		assertThat(ExceptionSoftener.softenDoubleFunction(a->a+1).apply(1),equalTo(2));
	}
	@Test(expected=IOException.class)
	public void testSoftenDoubleFunctionException() {
		ExceptionSoftener.softenDoubleFunction(this::loadIdDouble).apply(2);
	}

	public void testSoftenLongToDoubleFunction() {
		assertThat(ExceptionSoftener.softenLongToDoubleFunction(a->a+1).applyAsDouble(1l),equalTo(2));
	}

	
	public void testSoftenLongToIntFunction() {
		assertThat(ExceptionSoftener.softenLongToIntFunction(a->(int)(a+1)).applyAsInt(1l),equalTo(2));
	}

	
	public void testSoftenIntToDoubleFunction() {
		assertThat(ExceptionSoftener.softenIntToDoubleFunction(a->a+1).applyAsDouble(1),equalTo(2d));
	}

	
	public void testSoftenIntToLongFunction() {
		assertThat(ExceptionSoftener.softenIntToLongFunction(a->a+1).applyAsLong(1),equalTo(2l));
	}

	
	public void testSoftenDoubleToIntFunction() {
		assertThat(ExceptionSoftener.softenDoubleToIntFunction(a->1).applyAsInt(1),equalTo(1));
	}

	
	public void testSoftenDoubleToLongFunction() {
		assertThat(ExceptionSoftener.softenDoubleToLongFunction(a->3l).applyAsLong(1),equalTo(3l));
	}
	public String loadDir(String file,String dir) throws IOException{
		throw new IOException();
	}
	@Test(expected=IOException.class)
	public void testSoftenCheckedBiFunctionOfT1T2R() {
		ExceptionSoftener.softenBiFunction(this::loadDir).apply("input","dir");
	}

	public boolean test(String input) throws IOException{
		throw new IOException();
	}
	@Test(expected=IOException.class)
	public void testSoftenCheckedPredicateOfT() {
		ExceptionSoftener.softenPredicate(this::test).test("input");
	}

	public boolean testDouble(double input) throws IOException{
		throw new IOException();
	}
	@Test(expected=IOException.class)
	public void testSoftenDoublePredicate() {
		ExceptionSoftener.softenDoublePredicate(this::testDouble).test(1d);
	}

	public boolean testInt(int input) throws IOException{
		throw new IOException();
	}
	@Test(expected=IOException.class)
	public void testSoftenIntPredicateCheckedIntPredicate() {
		ExceptionSoftener.softenIntPredicate(this::testInt).test(1);
	}
	public boolean testLong(long input) throws IOException{
		throw new IOException();
	}
	@Test(expected=IOException.class)
	public void testSoftenLongPredicate() {
		ExceptionSoftener.softenLongPredicate(this::testLong).test(1l);
	}

	public boolean testBiPredicate(long input,String input2) throws IOException{
		throw new IOException();
	}
	@Test(expected=IOException.class)
	public void testSoftenCheckedBiPredicateOfT1T2() {
		ExceptionSoftener.softenBiPredicate(this::testBiPredicate).test(1l,"hello");
	}
	public boolean testConsumer(long input) throws IOException{
		throw new IOException();
	}
	@Test(expected=IOException.class)
	public void testSoftenCheckedConsumerOfT() {
		ExceptionSoftener.softenConsumer(this::testConsumer).accept(1l);
	}
	public boolean testDoubleConsumer(double input) throws IOException{
		throw new IOException();
	}
	
	@Test(expected=IOException.class)
	public void testSoftenDoubleConsumer() {
		ExceptionSoftener.softenDoubleConsumer(this::testDoubleConsumer).accept(1d);
	}

	@Test(expected=IOException.class)
	public void testSoftenIntConsumer() {
		ExceptionSoftener.softenIntConsumer(this::testConsumer).accept(1);
	}

	@Test(expected=IOException.class)
	public void testSoftenLongConsumer() {
		ExceptionSoftener.softenLongConsumer(this::testConsumer).accept(1l);
	}
	public boolean testBiConsumer(long input,long input2) throws IOException{
		throw new IOException();
	}
	@Test(expected=IOException.class)
	public void testSoftenCheckedBiConsumerOfT1T2() {
		ExceptionSoftener.softenBiConsumer(this::testBiConsumer).accept(1l,2l);
	}

}
