package com.aol.cyclops.trycatch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

public class FailureTest {

	Failure<Integer,FileNotFoundException> failure;
	FileNotFoundException error = new FileNotFoundException();
	@Before
	public void setup(){
		failure = Failure.of(error);
	}
	@Test
	public void testUnapply() {
		assertThat(failure.unapply(),equalTo(Arrays.asList(error)));
	}

	@Test
	public void testOf() {
		assertNotNull(failure);
	}

	@Test(expected=FileNotFoundException.class)
	public void testGet() {
		failure.get();
	}

	@Test
	public void testMap() {
		assertThat(failure.map(x->x+1),equalTo(failure));
	}

	@Test
	public void testFlatMap() {
		assertThat(failure.flatMap(x->Success.of(10)),equalTo(failure));
	}

	@Test
	public void testFilter() {
		assertThat(failure.filter(x->x==10),equalTo(Optional.empty()));
	}

	@Test
	public void testRecoverWithFor() {
		assertThat(failure.recoverWithFor(FileNotFoundException.class, e-> Success.of(10)),equalTo(Success.of(10)));
	}
	@Test
	public void testRecoverWithForSubclass() {
		Failure<Integer,IOException> failure = Failure.of(error);
		assertThat(failure.recoverWithFor(FileSystemException.class, e-> Success.of(10)),equalTo(Failure.of(error)));
		assertThat(failure.recoverWithFor(FileNotFoundException.class, e-> Success.of(10)),equalTo(Success.of(10)));
		
	}
	
	@Test
	public void testRecoverWithForIgnore() {
		assertThat(failure.recoverWithFor((Class)RuntimeException.class, e-> Success.of(10)),equalTo(failure));
	}

	@Test
	public void testRecoverFor() {
		assertThat(failure.recoverFor(FileNotFoundException.class, e-> 10),equalTo(Success.of(10)));
	}
	@Test
	public void testRecoverForInherited() {
		Failure<Integer,IOException> failure = Failure.of(error);
		assertThat(failure.recoverFor(FileSystemException.class, e-> 10),equalTo(Failure.of(error)));
		assertThat(failure.recoverFor(FileNotFoundException.class, e-> 10),equalTo(Success.of(10)));
		
	}
	@Test
	public void testRecoverForIgnore() {
		assertThat(failure.recoverFor((Class)RuntimeException.class, e->10),equalTo(failure));
	}


	@Test
	public void testRecover() {
		assertThat(failure.recover(e-> 10),equalTo(Success.of(10)));
	}

	@Test
	public void testRecoverWith() {
		assertThat(failure.recoverWith(e-> Success.of(10)),equalTo(Success.of(10)));
	}

	@Test
	public void testFlatten() {
		assertThat(failure.flatten(),equalTo(failure));
	}

	@Test
	public void testOrElse() {
		assertThat(failure.orElse(10),equalTo(10));
	}

	@Test
	public void testOrElseGet() {
		assertThat(failure.orElseGet(()->10),equalTo(10));
	}

	@Test
	public void testToOptional() {
		assertThat(failure.toOptional(),equalTo(Optional.empty()));
	}

	@Test
	public void testToStream() {
		assertThat(failure.stream().collect(Collectors.toList()),
				equalTo(Stream.of().collect(Collectors.toList())));
	}

	@Test
	public void testIsSuccess() {
		assertThat(failure.isSuccess(),equalTo(false));
	}

	@Test
	public void testIsFailure() {
		assertThat(failure.isFailure(),equalTo(true));
	}

	Integer value = null;
	@Test
	public void testForeach() {
		
		failure.forEach(v -> value = v);
		assertThat(value,is(nullValue()));
	}

	Exception errorCaptured;
	@Test
	public void testOnFailConsumerOfX() {
		errorCaptured = null;
		failure.onFail(e -> errorCaptured =e);
		assertThat(error,equalTo(errorCaptured));
	}

	@Test
	public void testOnFailClassOfQextendsXConsumerOfX() {
		errorCaptured = null;
		failure.onFail(FileNotFoundException.class, e -> errorCaptured =e);
		assertThat(error,equalTo(errorCaptured));
	}
	@Test
	public void testOnFailClassOfQextendsXConsumerOfXInherited() {
		Failure<Integer,IOException> failure =  Failure.of(error);
		errorCaptured = null;
		failure.onFail(FileNotFoundException.class, e -> errorCaptured =e);
		assertThat(error,equalTo(errorCaptured));
	}
	@Test
	public void testOnFailClassOfQextendsXConsumerOfXIgnored() {
		errorCaptured = null;
		failure.onFail((Class)RuntimeException.class, e -> errorCaptured =e);
		assertThat(errorCaptured,is(nullValue()));
	}

	@Test(expected=FileNotFoundException.class)
	public void testThrowException() {
		failure.throwException();
	}

	@Test
	public void testToFailedOptional() {
		assertThat(failure.toFailedOptional(),equalTo(Optional.of(error)));
	}

	@Test
	public void testToFailedStream() {
		assertThat(failure.toFailedStream().collect(Collectors.toList()),
				equalTo(Stream.of(error).collect(Collectors.toList())));
	}

	@Test
	public void testForeachFailed() {
		errorCaptured = null;
		failure.forEachFailed(e -> errorCaptured =e);
		assertThat(error,equalTo(errorCaptured));
	}

	

}
