package com.aol.cyclops;

import static com.aol.cyclops.control.Matchable.otherwise;
import static com.aol.cyclops.control.Matchable.then;
import static com.aol.cyclops.control.Matchable.when;
import static com.aol.cyclops.util.function.Predicates.__;
import static com.aol.cyclops.util.function.Predicates.any;
import static com.aol.cyclops.util.function.Predicates.eq;
import static com.aol.cyclops.util.function.Predicates.in;
import static com.aol.cyclops.util.function.Predicates.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.val;

public class MatchablesTest {

	@Test
	public void test() {
		Matchables.headAndTail(Arrays.asList(1,2))
				 .matches(c->c.is(when(Maybe.of(1),ListX.of(2,3,4)),then("boo!"))
						 	  .is(when(t->t.equals(Maybe.of(1)),__),then("boohoo!"))
						 	   .isEmpty(then("oops!")), otherwise("hello"));
	}
	@Test
	public void throwable(){
	    Eval<String> result = Matchables.throwable(new IOException("hello"))
	                             .on$12__()
	                             .matches(c->c.is(when(IOException.class,"hello"), then("correct"))
	                                         .is(when(FileNotFoundException.class,"boo"),then("wrong")), 
	                                 otherwise("missing"));
	    
	    
	    assertThat(result,equalTo(Eval.now("correct")));
	}
	@Test
    public void url() throws MalformedURLException{
        System.out.println( Matchables.url(new URL("http://www.aol.com?q=hello"))
                                         .visit((protocol, host, port, path, query)-> protocol));
        System.out.println( Matchables.url(new URL("http://www.aol.com?q=hello"))
                                            .visit((protocol, host, port, path, query)-> host));
        System.out.println( Matchables.url(new URL("http://www.aol.com?q=hello"))
                                            .visit((protocol, host, port, path, query)-> port));
        System.out.println( Matchables.url(new URL("http://www.aol.com/path?q=hello"))
                                               .visit((protocol, host, port, path, query)-> path));
        System.out.println( Matchables.url(new URL("http://www.aol.com?q=hello"))
                                                .visit((protocol, host, port, path, query)-> query));
        
        
        Eval<String> url = Matchables.url(new URL("http://www.aol.com/path?q=hello"))
                                     .on$12_45()
                                     .matches(c->c.is(when("http","www.aol.com","/path","q=hello"), then("correct")),otherwise("miss"));
        
        assertThat(url,equalTo(Eval.now("correct")));
    }
	@Test
    public void urlPredicates() throws MalformedURLException{
        System.out.println( Matchables.url(new URL("http://www.aol.com?q=hello"))
                                         .visit((protocol, host, port, path, query)-> protocol));
        System.out.println( Matchables.url(new URL("http://www.aol.com?q=hello"))
                                            .visit((protocol, host, port, path, query)-> host));
        System.out.println( Matchables.url(new URL("http://www.aol.com?q=hello"))
                                            .visit((protocol, host, port, path, query)-> port));
        System.out.println( Matchables.url(new URL("http://www.aol.com/path?q=hello"))
                                               .visit((protocol, host, port, path, query)-> path));
        System.out.println( Matchables.url(new URL("http://www.aol.com?q=hello"))
                                                .visit((protocol, host, port, path, query)-> query));
  
        Eval<String> url = Matchables.url(new URL("http://www.aol.com/path?q=hello"))
                                     .on$12_45()
                                     .matches(c->c.is(when(eq("http"),in("www.aol.com","aol.com"),any(),not(eq("q=hello!"))), then("correct")),otherwise("miss"));
       
        assertThat(url,equalTo(Eval.now("correct")));
        
    }
	
	@Test
	public void file(){
	    String file = ReactiveSeq.of("input.file")
	                             .map(getClass().getClassLoader()::getResource)
	                             .map(URL::getFile)
	                             .single();
	    String result = Matchables.lines(new File(file))
	                              .on$12___()
	                              .matches(c->c.is(when("hello","world"),then("correct")), otherwise("miss")).get();
	    
	    assertThat(result,equalTo("correct"));
	}
	@Test
    public void fileAutoclose(){
        String file = ReactiveSeq.of("input.file")
                                 .map(getClass().getClassLoader()::getResource)
                                 .map(URL::getFile)
                                 .single();
        String result = null;
        try(val matchable = Matchables.lines(new File(file))){
                               result =  matchable.on$12___()
                                          .matches(c->c.is(when("hello","world"),then("correct")), otherwise("miss")).get();
                               throw new RuntimeException();
        } catch (Exception e) {
            
        }
        
        assertThat(result,equalTo("correct"));
    }
	
	@Test
	public void urlLines(){
	   URL url =  ReactiveSeq.of("input.file")
	                       .map(getClass().getClassLoader()::getResource)
	                       .single();
	   
	   String result = Matchables.lines(url)
	                             .on$12___()
	                             .matches(c->c.is(when("hello","world2"),then("incorrect"))
	                                          .is(when("hello","world"),then("correct")), otherwise("miss"))
	                             .get();
	   assertThat(result,equalTo("correct"));
	}
	@Test
	public void words(){
	  String result =   Matchables.words("hello world")
	                              .matches(c->c.has(when("hello","world","boo!"), then("incorrect")), otherwise("miss"))
	                              .get();
	  
	  assertThat(result,equalTo("miss"));
	}
	@Test
    public void words2(){
      String result =   Matchables.words("hello world")
                                  .matches(c->c.has(when("hello","world"), then("correct")), otherwise("miss"))
                                  .get();
      
      assertThat(result,equalTo("correct"));
    }
	@Test
    public void wordsPredicates() throws MalformedURLException{
      String result =   Matchables.words("hello world")
                                  .matches(c->c.has(when("hello","world"), then("correct")), otherwise("miss"))
                                  .get();
      
      assertThat(result,equalTo("correct"));
      
      Matchables.url(new URL("http://www.aol.com/path?q=hello"))
                .on$12_45()
                .matches(c->c.is(when(eq("http"),in("www.aol.com","aol.com"),any(),not(eq("q=hello!"))), then("correct")),otherwise("miss"));
    }

}
