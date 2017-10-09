package com.aol.cyclops2.control;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.control.Ior;
import cyclops.control.Option;
import org.junit.Before;
import org.junit.Test;

public class IorPrimaryTest {

	Ior<FileNotFoundException,Integer> success;
	final Integer value = 10;
	
	
	public Ior<FileNotFoundException,String> load(String filename){
		return Ior.primary("test-data");
	}
	
	public void process(){
		
		Ior<FileNotFoundException,String> attempt = load("data");
		
		attempt.map(String::toUpperCase)
				.peek(System.out::println);
	
	}
	
	@Before
	public void setup(){
		success = Ior.primary(10);
	}
	@Test
    public void bimap(){
       
        Ior<RuntimeException,Integer> mapped = success.bimap(e->new RuntimeException(), d->d+1);
        assertThat(mapped.get(),equalTo(Option.some(11)));
        assertTrue(mapped.isPrimary());
    }
    Throwable capT;
    int capInt=0;
    @Test
    public void bipeek(){
       capT =null;
       capInt=0;
         success.bipeek(e->capT=e, d->capInt=d);
        assertThat(capInt,equalTo(10));
        assertThat(capT,nullValue());
    }
    @Test
    public void bicast(){
        Ior<Throwable,Number> mapped = success.bicast(Throwable.class, Number.class);
        assertThat(mapped.get(),equalTo(Option.some(10)));
        assertTrue(mapped.isPrimary());
    }



	@Test
	public void testGet() {
		assertThat(success.get(),equalTo(Option.some(value)));
	}

	@Test
	public void testOf() {
		assertThat(value,not(nullValue()));
	}

	@Test
	public void testMap() {
		assertThat(success.map(x->x+1),equalTo(Ior.primary(value+1)));
	}

	@Test
	public void testFlatMap() {
		assertThat(success.flatMap(x->Ior.primary(x+1)),equalTo(Ior.primary(value+1)));
	}

	@Test
	public void testFilter() {
		assertThat(success.filter(x->x>5),equalTo(Option.some(value)));
	}
	@Test
	public void testFilterFail() {
		assertThat(success.filter(x->x>15),equalTo(Option.none()));
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
		assertTrue(success.isPrimary());
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
