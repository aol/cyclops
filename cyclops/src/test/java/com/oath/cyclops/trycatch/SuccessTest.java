package com.oath.cyclops.trycatch;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.control.Option;
import org.junit.Before;
import org.junit.Test;

import cyclops.control.Maybe;
import cyclops.control.Try;


public class SuccessTest {

	Try<Integer,FileNotFoundException> success;
	final Integer value = 10;


	public Try<String,FileNotFoundException> load(String filename){
		return Try.success("test-data");
	}

	public void process(){

		Try<String,FileNotFoundException> attempt = load("data");

		attempt.map(String::toUpperCase)
				.peek(System.out::println);

	}

	@Before
	public void setup(){
		success = Try.success(10);
	}



	@Test
	public void testGet() {
		assertThat(success.orElse(null),equalTo(value));
	}

	@Test
	public void testOf() {
		assertThat(value,not(nullValue()));
	}

	@Test
	public void testMap() {
		assertThat(success.map(x->x+1),equalTo(Try.success(value+1)));
	}

	@Test
	public void testFlatMap() {
		assertThat(success.flatMap(x->Try.success(x+1)),equalTo(Try.success(value+1)));
	}

	@Test
	public void testFilter() {
		assertThat(success.filter(x->x>5),equalTo(Maybe.of(value)));
	}
	@Test
	public void testFilterFail() {
		assertThat(success.filter(x->x>15),equalTo(Maybe.nothing()));
	}

	@Test
	public void testRecover() {
		assertThat(success.recover(e->20),equalTo(success));
	}
	@Test
	public void testRecoverSupplier() {
		assertThat(success.recover(()->20),equalTo(success));
	}

	@Test
	public void testRecoverWith() {
		assertThat(success.recoverFlatMap(e->Try.success(20)),equalTo(success));
	}

	@Test
	public void testRecoverFor() {
		Try<Integer,IOException> success = Try.success(20);
		assertThat(success.recoverFor(FileSystemException.class, e-> 10),equalTo(Try.success(20)));
		assertThat(success.recoverFor(FileNotFoundException.class, e-> 15),equalTo(Try.success(20)));
		assertThat(success.recoverFor(IOException.class,e->30),equalTo(success));
	}

	@Test
	public void testRecoverWithFor() {
		assertThat(success.recoverFlatMapFor(FileNotFoundException.class,e->Try.success(20)),equalTo(success));
	}
	@Test
	public void testFlatten() {

		assertThat(Try.<Try<Integer,FileNotFoundException>,FileNotFoundException>success(success)
				      .to(Try::flatten),equalTo(success));
	}
	@Test
	public void testFlattenFailure() {
		FileNotFoundException error = new FileNotFoundException();
		assertThat(Try.<Try<Integer,FileNotFoundException>,FileNotFoundException>success(Try.failure(error)).to(Try::flatten),equalTo(Try.failure(error)));
	}

	@Test
	public void testOrElse() {
		assertThat(success.orElse(30),equalTo(value));
	}

	@Test
	public void testOrElseGet() {
		assertThat(success.orElseGet(()->30),equalTo(value));
	}

	@Test
	public void testToOptional() {
		assertThat(success.toOptional(),equalTo(Optional.of(value)));
	}

	@Test
	public void testToStream() {
		assertThat(success.stream().collect(Collectors.toList()),
				equalTo(Stream.of(value).collect(Collectors.toList())));
	}

	@Test
	public void testIsSuccess() {
		assertTrue(success.isSuccess());
	}

	@Test
	public void testIsFailure() {
		assertFalse(success.isFailure());
	}
	Integer valueCaptured = null;
	@Test
	public void testForeach() {
		success.forEach(v -> valueCaptured = v);
		assertThat(valueCaptured,is(10));
	}
	Exception errorCaptured;
	@Test
	public void testOnFailConsumerOfX() {
		errorCaptured = null;
		success.onFail(e -> errorCaptured =e);
		assertThat(errorCaptured,is(nullValue()));
	}

	@Test
	public void testOnFailClassOfQsuperXConsumerOfX() {
		Try<Integer,IOException> success = Try.success(10);
		errorCaptured = null;
		success.onFail(FileNotFoundException.class,e -> errorCaptured =e);
		assertThat(errorCaptured,is(nullValue()));
	}



	@Test
	public void testToFailedOption() {

		assertThat(success.toFailedOption(),is(Option.none()));
	}

	@Test
	public void testToFailedStream() {
		assertThat(success.toFailedStream().findFirst(),is(Optional.empty()));
	}

	@Test
	public void testForeachFailed() {
		errorCaptured = null;
		success.forEachFailed(e -> errorCaptured =e);
		assertThat(errorCaptured,is(nullValue()));
	}


}
