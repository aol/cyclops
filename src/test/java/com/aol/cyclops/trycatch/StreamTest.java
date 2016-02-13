package com.aol.cyclops.trycatch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.aol.cyclops.control.Try;

public class StreamTest {

	@Test
	public void lines() throws URISyntaxException{
		Path myPath = Paths.get(ClassLoader.getSystemResource("input.file").toURI());
		assertThat(Try
		  .catchExceptions(IOException.class)
		  .init(() -> Files.lines(myPath))
		  .tryWithResources(lines -> {
			  lines.forEach(System.out::println);
			  return "hello";
		  }).get(),equalTo("hello"));
	}
	@Test
	public void testTryWithResources(){
		
		assertThat(Try.catchExceptions(FileNotFoundException.class,IOException.class)
				   .init(()->new BufferedReader(new FileReader("file.txt")))
				   .tryWithResources(this::read).toFailedOptional().get(),instanceOf((Class)FileNotFoundException.class));
		
		
										
		
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
}
