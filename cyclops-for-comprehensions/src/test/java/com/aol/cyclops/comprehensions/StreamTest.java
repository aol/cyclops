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
import java.util.stream.Stream;

import lombok.val;

import org.jooq.lambda.Seq;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.UntypedDo;
import com.aol.cyclops.comprehensions.donotation.typed.Do;

public class StreamTest {
	
	@Test
	public void arrayStream() {
		
		List<String> res = Do.add(new String[]{"hello world","hello"}) 
							.yield( v1->  v1 + "1")
							.toTraversable().toList();
		List<String> expected = Arrays.asList("hello world1", "hello1");
		
		
		
		assertThat(expected, equalTo( res));
	}
	@Test
	public void stringStream() {
		
		List<String> res = Do.add("hello world") 
							.yield( v-> ""+ v + "1").<String>traversable().toList();
		List<String> expected = Arrays.asList("h1", "e1", "l1", "l1", "o1",  " 1", "w1", "o1", "r1", 
				"l1", "d1");
		
		
		
		assertThat(expected,equalTo( res));
	}
	@Test
	public void stringStreamWithNull() {
		
		Seq<String> res =  Do.add(  "hello world") 
							.add((Iterable<String>)null)
							.yield( v1-> v2-> ""+ v1 + v2)
							.unwrap();
		List<String> expected = Arrays.asList();
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test @Ignore
	public void urlStream() throws MalformedURLException {
		val url = new URL("http://www.aol.com");
		Seq<String> res = Do.add (url) 
									 .yield( v1->  v1 + "New line!").unwrap();
		List<String> expected = Arrays.asList("h1", "e1", "l1", "l1", "o1",  " 1", "w1", "o1", "r1", 
				"l1", "d1");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void bufferedReaderStream() {
		
		Seq<String> res = Do.add(  new BufferedReader(new InputStreamReader(this.getClass().getClassLoader()
                               	.getResourceAsStream("input.file")))) 
                              .yield( v -> ""+ v + "*").unwrap();
		List<String> expected = Arrays.asList("line 1*","line 2*","line 3*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void urlStream2() {
		URL url =this.getClass().getClassLoader().getResource("input.file");
		Seq<String> res = Do.add (url) 
							.yield( v-> ""+ v+ "*").unwrap();
		List<String> expected = Arrays.asList("line 1*","line 2*","line 3*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void fileStream2() {
		URL url =this.getClass().getClassLoader().getResource("input.file");
		File file = new File(url.getFile());
		Seq<String> res = Do.add(  file) 
							 .yield( v-> ""+ v + "*").unwrap();
		List<String> expected = Arrays.asList("line 1*","line 2*","line 3*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void enumStream() {
		
		Seq<String> res = UntypedDo.add ( MyEnum.class) 
									 .yield( v -> ""+ v + "*");
		List<String> expected = Arrays.asList("FIRST*","SECOND*","THIRD*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void iterableStream() {
		
		Seq<String> res = Do.add(  new MyIterable()) 
							.yield( v->  v + "*").unwrap();
		List<String> expected = Arrays.asList("hello*","world*");
		
		
		
		assertThat(expected, equalTo( res.toList()));
	}
	@Test
	public void mapStream() {
		
		Map<String,Integer> m = new HashMap<>();
		m.put("hello",10);
		List<String> res = Do.add(m.entrySet().stream()) 
							.yield( v-> ""+ v + "*").toTraversable().toList();
		List<String> expected = Arrays.asList("hello=10*");
		
		
		
		
		assertThat(expected, equalTo( res));
	}
	
	static enum MyEnum{FIRST, SECOND, THIRD}
	
	static class MyIterable implements Iterable<String>{

		@Override
		public Iterator iterator() {
			return Arrays.asList("hello","world").iterator();
		}
		
	}
}
