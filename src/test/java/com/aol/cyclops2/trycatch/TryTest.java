package com.aol.cyclops2.trycatch;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import org.junit.Test;

import cyclops.control.Try;
public class TryTest {
 
	@Test
	public void test(){
		Try<Integer,RuntimeException> tryM;
		
		Try.runWithCatch(this::exceptional,IOException.class)
					.onFail(System.out::println).get();
	}

	private Consumer<IOException> extractFromMemoryCace() {
		// TODO Auto-generated method stub
		return null;
	}
	@Test
	public void ongoing(){
        Try<Object, Throwable> t = Try.success(2, RuntimeException.class).map(a -> {
            throw new RuntimeException();});
            System.out.println(t.toString());

            assertTrue(Try.success(2, RuntimeException.class)
                    .map(i -> {
                        throw new RuntimeException();
                    }).isFailure());

        };

    public int throwsEx() throws Exception{
	    return 0;
    }
    @Test
    public void testExcept(){

        assertThat(Try.withCatch(()-> throwsEx())
                .map(i->i+" woo!")
                .onFail(System.out::println)
                .flatMap(e->Try.withCatch(()-> throwsEx()))
                .orElse(1),is(0));

        Try.withCatch(() -> throwsEx())
                .onFail(e -> {})
                .flatMap(v -> Try.withCatch(()-> throwsEx()));
    }

    @Test
	public void test2(){
		assertThat(Try.withCatch(()-> exceptional2())
						.map(i->i+" woo!")
						.onFail(System.out::println)
						
						.orElse("default"),is("hello world woo!"));
		
		
	}
	
	@Test
	public void catchExceptonsWithRun(){
		assertThat(Try.catchExceptions(RuntimeException.class)
			.run(()-> exceptional2())
			.onFail(System.out::println)
			.map(i->i+"woo!")
			.toOptional()
			.orElse("hello world"),is("nullwoo!"));
	}
	
	@Test
	public void testTryWithResources(){
		
		assertThat(Try.catchExceptions(FileNotFoundException.class,IOException.class)
				   .init(()->new BufferedReader(new FileReader("file.txt")))
				   .tryWithResources(this::read).toFailedOptional().get(),instanceOf((Class)FileNotFoundException.class));
		
		
										
		
	}
	
	public void testMultipleResources(){
		
		Try t2 = Try.catchExceptions(FileNotFoundException.class,IOException.class)
				   .init(()->Tuple.tuple(new BufferedReader(new FileReader("file.txt")),new FileReader("hello")))
				   .tryWithResources(this::read2);
		
	}
	
	private String read2(Tuple2<BufferedReader,FileReader> res) throws IOException{
		String line = res._1().readLine();
		return null;
	}
	private String read(BufferedReader br) throws IOException{
		StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }
        String everything = sb.toString();
        return everything;
	}
	
	private void exceptional() throws IOException{
		throw new IOException();
	}
	private String exceptional2() throws RuntimeException{
		return "hello world";
	}
}
