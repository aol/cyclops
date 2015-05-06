package com.aol.cyclops.trycatch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.aol.cyclops.trycatch.Try;

public class TryTest {
 
	@Test
	public void test(){
		Try.runWithCatch(this::exceptional,IOException.class)
					.onFail(System.out::println);
	}
	
	@Test
	public void test2(){
		Try.withCatch(()-> exceptional2()).map(i->i+" woo!").onFail(System.out::println).toOptional().orElse("default");
		
		
		Try.catchExceptions(RuntimeException.class)
			.run(()-> exceptional2())
			.onFail(System.out::println)
			.map(i->i+"woo!")
			.toOptional()
			.orElse("hello world");
	}
	
	@Test
	public void test3(){
		
		Try.catchExceptions(IOException.class,FileNotFoundException.class)
				.init(()->new BufferedReader(new FileReader("file.txt")))
						.tryThis(this::read)
						.andFinally(BufferedReader::close);
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
