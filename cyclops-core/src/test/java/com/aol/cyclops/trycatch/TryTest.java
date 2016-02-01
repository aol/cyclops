package com.aol.cyclops.trycatch;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

import org.junit.Test;

import com.aol.cyclops.lambda.tuple.PTuple2;
import com.aol.cyclops.lambda.tuple.PowerTuples;
public class TryTest {
 
	@Test(expected=IOException.class)
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
				   .init(()->PowerTuples.tuple(new BufferedReader(new FileReader("file.txt")),new FileReader("hello")))
				   .tryWithResources(this::read2);
		
	}
	
	private String read2(PTuple2<BufferedReader,FileReader> res) throws IOException{
		String line = res.v1().readLine();
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
