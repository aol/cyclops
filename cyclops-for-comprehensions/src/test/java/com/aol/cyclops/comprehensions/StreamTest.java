package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import lombok.val;

import org.jooq.lambda.Seq;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.comprehensions.LessTypingForComprehension1.Vars1;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Vars2;

public class StreamTest {

	@Test
	public void stringStream() {
		
		Seq<String> res = ForComprehensions.foreach1 (  c-> 
									c.mapAs$1(  "hello world") 
									 .yield( (Vars1<Character> v)-> ""+ v.$1() + "1"));
		List<String> expected = Arrays.asList("h1", "e1", "l1", "l1", "o1",  " 1", "w1", "o1", "r1", 
				"l1", "d1");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void stringStreamWithNull() {
		
		Seq<String> res = ForComprehensions.foreach2 (  c-> 
									c.flatMapAs$1(  "hello world") 
									.mapAs$2((Vars2<Character,Character> v)-> null)
									 .yield( v-> ""+ v.$1() + v.$2()));
		List<String> expected = Arrays.asList();
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test @Ignore
	public void urlStream() throws MalformedURLException {
		val url = new URL("http://www.aol.com");
		Seq<String> res = ForComprehensions.foreach1 (  c-> 
									c.mapAs$1(  url) 
									 .yield( (Vars1<Character> v)->  v.$1() + "New line!"));
		List<String> expected = Arrays.asList("h1", "e1", "l1", "l1", "o1",  " 1", "w1", "o1", "r1", 
				"l1", "d1");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	
}
