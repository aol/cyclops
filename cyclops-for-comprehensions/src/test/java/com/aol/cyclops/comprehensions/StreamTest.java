package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.val;

import org.jooq.lambda.Seq;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.comprehensions.LessTypingForComprehension1.Vars1;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Vars2;

public class StreamTest {
	@Test
	public void arrayStream() {
		
		Seq<String> res = ForComprehensions.foreach1 (  c-> 
									c.mapAs$1(  new String[]{"hello world","hello"}) 
									 .yield( (Vars1<String> v)->  v.$1() + "1"));
		List<String> expected = Arrays.asList("hello world1", "hello1");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
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
	@Test
	public void bufferedReaderStream() {
		
		Seq<String> res = ForComprehensions.foreach1 (  c-> 
									c.mapAs$1(  new BufferedReader(new InputStreamReader(this.getClass().getClassLoader()
                                .getResourceAsStream("input.file")))) 
									 .yield( (Vars1<String> v)-> ""+ v.$1() + "*"));
		List<String> expected = Arrays.asList("line 1*","line 2*","line 3*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void urlStream2() {
		URL url =this.getClass().getClassLoader().getResource("input.file");
		Seq<String> res = ForComprehensions.foreach1 (  c-> 
									c.mapAs$1(  url) 
									 .yield( (Vars1<String> v)-> ""+ v.$1() + "*"));
		List<String> expected = Arrays.asList("line 1*","line 2*","line 3*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void fileStream2() {
		URL url =this.getClass().getClassLoader().getResource("input.file");
		File file = new File(url.getFile());
		Seq<String> res = ForComprehensions.foreach1 (  c-> 
									c.mapAs$1(  file) 
									 .yield( (Vars1<String> v)-> ""+ v.$1() + "*"));
		List<String> expected = Arrays.asList("line 1*","line 2*","line 3*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void enumStream() {
		
		Seq<String> res = ForComprehensions.foreach1 (  c-> 
									c.mapAs$1(  MyEnum.class) 
									 .yield( (Vars1<MyEnum> v)-> ""+ v.$1() + "*"));
		List<String> expected = Arrays.asList("FIRST*","SECOND*","THIRD*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void iterableStream() {
		
		Seq<String> res = ForComprehensions.foreach1 (  c-> 
									c.mapAs$1(  new MyIterable()) 
									 .yield( (Vars1<String> v)->  v.$1() + "*"));
		List<String> expected = Arrays.asList("hello*","world*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void mapStream() {
		
		Map<String,Integer> m = new HashMap<>();
		m.put("hello",10);
		Seq<String> res = ForComprehensions.foreach1 (  c-> 
									c.mapAs$1(  m) 
									 .yield( (Vars1<Map.Entry<String, Integer>> v)-> ""+ v.$1() + "*"));
		List<String> expected = Arrays.asList("hello=10*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	
	static enum MyEnum{FIRST, SECOND, THIRD}
	
	static class MyIterable implements Iterable{

		@Override
		public Iterator iterator() {
			return Arrays.asList("hello","world").iterator();
		}
		
	}
}
