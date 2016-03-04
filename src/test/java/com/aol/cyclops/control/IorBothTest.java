package com.aol.cyclops.control;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

public class IorBothTest {

	Ior<FileNotFoundException,Integer> success;
	final Integer value = 10;
	
	
	public Ior<FileNotFoundException,String> load(String filename){
		return Ior.both(new FileNotFoundException(),"test-data");
	}
	
	public void process(){
		
		Ior<FileNotFoundException,String> attempt = load("data");
		
		attempt.map(String::toUpperCase)
				.peek(System.out::println);
	
	}
	
	@Before
	public void setup(){
		success = Ior.both(new FileNotFoundException(),10);
	}
	@Test
	public void bimap(){
	   
	    Ior<RuntimeException,Integer> mapped = success.bimap(e->new RuntimeException(), d->d+1);
	    assertThat(mapped.get(),equalTo(11));
	    assertThat(mapped.swap().get(),instanceOf(RuntimeException.class));
	}
	Throwable capT;
	int capInt=0;
	@Test
    public void bipeek(){
       capT =null;
       capInt=0;
         success.bipeek(e->capT=e, d->capInt=d);
        assertThat(capInt,equalTo(10));
        assertThat(capT,instanceOf(FileNotFoundException.class));
    }
	@Test
    public void bicast(){
        Ior<Throwable,Number> mapped = success.bicast(Throwable.class, Number.class);
        assertThat(mapped.get(),equalTo(10));
        assertThat(mapped.swap().get(),instanceOf(Throwable.class));
    }

	@Test
	public void testUnapply() {
		assertThat(success.unapply(),equalTo(Arrays.asList(value)));
	}

	@Test
	public void testGet() {
		assertThat(success.get(),equalTo(value));
	}

	@Test
	public void testOf() {
		assertThat(value,not(nullValue()));
	}

	@Test
	public void testMap() {
		assertThat(success.map(x->x+1).get(),equalTo(Ior.primary(value+1).get()));
	}

	@Test
	public void testFlatMap() {
		assertThat(success.flatMap(x->Ior.primary(x+1)).get(),equalTo(Ior.primary(value+1).get()));
	}

	@Test
	public void testFilter() {
		assertThat(success.filter(x->x>5),equalTo(Ior.primary(value)));
	}
	@Test
	public void testFilterFail() {
		assertThat(success.filter(x->x>15),equalTo(Ior.secondary(null)));
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
		assertTrue(success.isBoth());
		assertFalse(success.isPrimary());
	}

	@Test
	public void testIsFailure() {
		assertFalse(success.isSecondary());
	}
	Integer valueCaptured = null;
	@Test
	public void testForeach() {
		success.forEach(v -> valueCaptured = v);
		assertThat(valueCaptured,is(10));
	}
	Exception errorCaptured;
	

}
