package com.aol.cyclops;

import static com.aol.cyclops.Matchables.optional;
import static com.aol.cyclops.control.Matchable.otherwise;
import static com.aol.cyclops.control.Matchable.then;
import static com.aol.cyclops.control.Matchable.when;
import static com.aol.cyclops.control.Try.success;
import static com.aol.cyclops.util.function.Predicates.__;
import static com.aol.cyclops.util.function.Predicates.any;
import static com.aol.cyclops.util.function.Predicates.eq;
import static com.aol.cyclops.util.function.Predicates.equal;
import static com.aol.cyclops.util.function.Predicates.in;
import static com.aol.cyclops.util.function.Predicates.instanceOf;
import static com.aol.cyclops.util.function.Predicates.not;
import static com.aol.cyclops.util.function.Predicates.some;
import static com.aol.cyclops.util.function.Predicates.equals;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.junit.Test;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Matchable.AutoCloseableMatchableIterable;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.data.async.QueueFactories;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.util.function.Predicates;

public class MatchablesTest {
    @Test
    public void cFuture(){
        
        Eval<Integer> result = Matchables.future(CompletableFuture.completedFuture(10))
                                         .matches(c-> 
                                                     c.is( when(some(10)), then(20)),  //success
                                                      
                                                     c->c.is(when(instanceOf(RuntimeException.class)), then(2)), //failure
                                                      
                                                     otherwise(3) //no match
                                                 );
        
        assertThat(result,equalTo(Eval.now(20)));
    
    
    }
    
    private String loadData(){
        return "";
    }
    private boolean validData(String data){
        return true;
    }
    private int save(String data){
        return 1;
    }
    private final  int IO_ERROR = -1;
    private final  int UNEXPECTED_RESULT = 0;
    private final  int SUCCESS = 1;
    @Test
    public void matchTest(){
       assertThat( Matchables.match(100)
                .matches(c->c.is(when(Predicates.greaterThan(50)), ()->"large"), ()->"small").get(),
                equalTo("large"));
    }
    @Test
    public void futurePatternMatching(){
        
        CompletableFuture<String> future = CompletableFuture.supplyAsync(this::loadData);
        Eval<Integer> result = Matchables.future(future)
                                         .matches(c-> 
                                                     c.is( when(this::validData), then(()->save(future.join()))),  //success
                                                      
                                                     c->c.is(when(instanceOf(IOException.class)), then(IO_ERROR)), //failure
                                                      
                                                     otherwise(UNEXPECTED_RESULT) //no match
                                                 );
        
        assertThat(result,equalTo(Eval.now(SUCCESS)));
    
    
    }
    @Test
    public void cFutureFail(){
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        cf.completeExceptionally(new RuntimeException());
        Eval<Integer> result = Matchables.future(cf)
                                         .matches(c-> c.is( when(some(10)), then(2)), c->c.is(when(instanceOf(RuntimeException.class)), then(2)),otherwise(3));
        
        assertThat(result,equalTo(Eval.now(2)));
    }
    @Test
    public void tryTest2(){
        Eval<Integer> result = Matchables.tryMatch(Try.success(1))
                                         .matches(c-> c.is( when(Maybe.just(1)), then(10)), c->c.is(when(instanceOf(RuntimeException.class)), then(2)),otherwise(3));
        
        assertThat(result,equalTo(Eval.now(10)));
    }
    @Test
    public void tryTest3(){
       
        Eval<Integer> result = Matchables.tryMatch(Try.success(1))
                                         .matches(c-> c.is( when(success(1)), then(10)), c->c.is(when(instanceOf(RuntimeException.class)), then(2)),otherwise(3));
        
        assertThat(result,equalTo(Eval.now(10)));
    }
    @Test
    public void tryTest(){
        Eval<Integer> result = Matchables.tryMatch(Try.success(1))
                                         .matches(c-> c.is( when(some(1)), then(10)), c->c.is(when(instanceOf(RuntimeException.class)), then(2)),otherwise(3));
        
        assertThat(result,equalTo(Eval.now(10)));
    }
    @Test
    public void tryFail(){
        Eval<Integer> result = Matchables.tryMatch(Try.failure(new RuntimeException()))
                                         .matches(c-> c.is( when(some(10)), then(2)), c->c.is(when(instanceOf(RuntimeException.class)), then(2)),otherwise(3));
        
        assertThat(result,equalTo(Eval.now(2)));
    }
    @Test
    public void future(){
        Eval<Integer> result = Matchables.future(FutureW.ofResult(1))
                                         .matches(c-> c.is( when(some(1)), then(10)), c->c.is(when(instanceOf(RuntimeException.class)), then(2)),otherwise(3));
        
        assertThat(result,equalTo(Eval.now(10)));
    }
    @Test
    public void futureFail(){
        Eval<Integer> result = Matchables.future(FutureW.ofError(new RuntimeException()))
                                         .matches(c-> c.is( when(some(10)), then(2)), c->c.is(when(instanceOf(RuntimeException.class)), then(2)),otherwise(3));
        
        assertThat(result,equalTo(Eval.now(2)));
    }
    Optional<Integer> serviceB(Object o){
        return Optional.of(2);
    }
    Integer serviceC(Object o){
        return 3;
    }
    @Test
    public void optional2(){
        Object args = null;
     
        optional(Optional.of(1)).visit(some-> some, 
                                        ()-> optional(serviceB(args)).visit(some->some,
                                                                        ()->serviceC(args)));
        
        Eval<Integer> result = Matchables.optional(Optional.of(1))
                                        .matches(c-> c.is( when(some(1)), then(2)), otherwise(3));
        
        assertThat(result,equalTo(Eval.now(2)));
    }
    @Test
    public void optionalEmpty(){
        Eval<Integer> result = Matchables.optional(Optional.empty())
                                        .matches(c-> c.is( when(some(1)), then(20)), otherwise(3));
        
        assertThat(result,equalTo(Eval.now(3)));
    }
    @Test
    public void maybe(){
        Eval<Integer> result = Matchables.maybe(Maybe.of(1))
                                        .matches(c-> c.is( when(Predicates.some(1)), then(2)), otherwise(3));
        
        assertThat(result,equalTo(Eval.now(2)));
    }
    @Test
    public void maybeEmpty(){
        Eval<Integer> result = Matchables.maybe(Maybe.none())
                                        .matches(c-> c.is( when(Predicates.some(10)), then(2)), otherwise(3));
        
        assertThat(result,equalTo(Eval.now(3)));
    }
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
    public void fileBr() throws FileNotFoundException{
        String file = ReactiveSeq.of("input.file")
                                 .map(getClass().getClassLoader()::getResource)
                                 .map(URL::getFile)
                                 .single();
        String result = Matchables.lines(new BufferedReader(new FileReader(new File(file))))
                                  .on$12___()
                                  .matches(c->c.is(when("hello","world"),then("correct")), otherwise("miss")).get();
        
        assertThat(result,equalTo("correct"));
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
        try( AutoCloseableMatchableIterable<String> matchable = Matchables.lines(new File(file))){
                               result =  matchable.on$12___()
                                                  .matches(c->c.is(when("hello","world"),then("correct")), otherwise("miss"))
                                                  .get();
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
    public void xor(){
        Xor<Exception, String> xor = Xor.primary("hello world");

        Eval<String> result = Matchables.xor(xor)
                                        .matches(c -> c.is(when(instanceOf(RuntimeException.class)), () -> "runtime"),
                                                 c -> c.is(when(equal("hello world")), () -> "hello back"), () -> "unknown");

        assertThat(result.get(), equalTo("hello back"));
            
    }
	@Test
	public void adapter(){
	    Adapter<Integer> adapter = QueueFactories.<Integer>unboundedQueue()
	                                                        .build();
	                                                         
	        String result =   Matchables.adapter(adapter)
	                                          .visit(queue->"we have a queue",topic->"we have a topic");
	        assertThat(result,equalTo("we have a queue"));
	        
	}
	@Test
    public void chars(){
      String result =   Matchables.chars("hello,world")
                                  .matches(c->c.has(when('h','e','l','l','o'), then("startsWith")), otherwise("miss"))
                                  .get();
      
      assertThat(result,equalTo("startsWith"));
    }
	@Test
    public void wordsSep(){
      String result =   Matchables.words("hello,world",",")
                                  .matches(c->c.has(when("hello","world","boo!"), then("incorrect")), otherwise("miss"))
                                  .get();
      
      assertThat(result,equalTo("miss"));
    }
	@Test
    public void words2Sep(){
        String result =   Matchables.words("hello,world",",")
                                    .matches(c->c.has(when("hello","world"), then("correct")), otherwise("miss"))
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
	
	@Test
	public void nonBlocking(){
	    assertThat(Matchables.blocking(new ManyToManyConcurrentArrayQueue(10))
	                                  .visit(c->"blocking", c->"not"),equalTo("not"));
	}
	@Test
    public void blocking(){
        assertThat(Matchables.blocking(new LinkedBlockingQueue(10))
                                      .visit(c->"blocking", c->"not"),equalTo("blocking"));
    }

}
