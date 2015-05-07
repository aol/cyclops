package com.aol.cyclops.trycatch;

import java.io.BufferedReader;
import static org.junit.Assert.assertThat;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
public class TryTest {
 
	@Test(expected=IOException.class)
	public void test(){
		Try.runWithCatch(this::exceptional,IOException.class)
					.onFail(System.out::println).get();
	}
	
	@Test
	public void test2(){
		assertThat(Try.withCatch(()-> exceptional2())
						.map(i->i+" woo!")
						.onFail(System.out::println)
						.toOptional()
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
		String line = res.v1.readLine();
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
