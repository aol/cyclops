package com.aol.simple.react.completablefuture;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.junit.Test;

import rx.Observable;

import com.aol.simple.react.SimpleReact;
import com.aol.simple.react.predicates.Predicates;


public class RxJavaConversionTest {
	volatile int count=0;
	volatile int savedCalled =0;
	
	@Test
	public void rxConversion() throws InterruptedException, ExecutionException{
		//conversion of RxJava code here :- http://blog.danlew.net/2014/09/22/grokking-rxjava-part-2/
		
		
		
		List<String> titles = new SimpleReact().reactToCollection(query("Hello, world!").get())
								.<String>then(url -> getTitle(url))
								.filter(Objects::nonNull)
								.filter(Predicates.take(5))
								.peek(title -> saveTitle(title) )
								.peek(System.out::println)
								.block();
								
	   assertThat(titles.size(),is(5));
	   assertThat(savedCalled,is(5));
	  
	}
	@Test
	public void rxConversionTestSkip() throws InterruptedException, ExecutionException{
		List<String> titles = new SimpleReact().reactToCollection(query("Hello, world!").get())
								.<String>then(url -> getTitle(url))
								.filter(Objects::nonNull)
								.filter(Predicates.skip(5))
								.peek(title -> saveTitle(title) )
								.peek(System.out::println)
								.block();
								
	   assertThat(titles.size(),is(4));
	   assertThat(savedCalled,is(4));
	  
	}
	@Test
	public void rxConversionTestSample() throws InterruptedException, ExecutionException{
		List<String> titles = new SimpleReact().reactToCollection(query("Hello, world!").get())
								.<String>then(url -> getTitle(url))
								.filter(Objects::nonNull)
								.filter(Predicates.sample(2))
								.peek(title -> saveTitle(title) )
								.peek(System.out::println)
								.block();
								
	   assertThat(titles.size(),is(4));
	   assertThat(savedCalled,is(4));
	  
	}
	
	
	
	private synchronized void saveTitle(String title) {
		savedCalled++;
	}


	private String getTitle(String url) {
		
		return url.substring(url.lastIndexOf('/'));
	}


	
	
	private CompletableFuture<List<String>> query(String string) {
		CompletableFuture future = new CompletableFuture();
		future.complete(Arrays.asList("http://blog.danlew.net/2014/09/22/grokking-rxjava-part-2",
				"http://blog.danlew.net/2014/09/30/grokking-rxjava-part-3",
				"http://blog.danlew.net/2014/09/30/grokking-rxjava-part-3",
				"http://blog.danlew.net/2014/09/30/grokking-rxjava-part-3",
				"http://blog.danlew.net/2014/09/30/grokking-rxjava-part-3",
				"http://blog.danlew.net/2014/09/30/grokking-rxjava-part-3",
				"http://blog.danlew.net/2014/09/30/grokking-rxjava-part-3",
				"http://blog.danlew.net/2014/09/30/grokking-rxjava-part-3",
				"http://blog.danlew.net/2014/09/30/grokking-rxjava-part-3"));
		return future;
	}
	/** Original RxJava example
	public void rx(){
		queryRx("Hello, world!")
	    .flatMap(urls -> Observable.from(urls))
	    .flatMap(url -> getTitleRx(url))
	    .filter(title -> title != null)
	    .take(5)
	    .doOnNext(title -> saveTitleRx(title))
	    .subscribe(title -> System.out.println(title));
	}
	private Observable<String> saveTitleRx(String title) {
		// TODO Auto-generated method stub
		return null;
	}
	private Observable<String> getTitleRx(String url) {
		// TODO Auto-generated method stub
		return null;
	}
	private Observable<List<String>> queryRx(String string) {
		// TODO Auto-generated method stub
		return null;
	}
	**/
}
