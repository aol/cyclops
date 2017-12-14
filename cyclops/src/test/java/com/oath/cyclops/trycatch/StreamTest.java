package com.oath.cyclops.trycatch;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
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

import cyclops.control.Try;

public class StreamTest {

	@Test
	public void lines() throws URISyntaxException{
		Path myPath = Paths.get(ClassLoader.getSystemResource("input.file").toURI());
  assertThat(Try
               .withResources(() -> Files.lines(myPath),lines -> {
    lines.forEach(System.out::println);
    return "hello";
  },IOException.class).orElse(null),equalTo("hello"));
}
	@Test
	public void testTryWithResources(){


		assertThat(Try.withResources(()->new BufferedReader(new FileReader("file.txt")),
      this::read,FileNotFoundException.class,IOException.class).toFailedOptional().get(),instanceOf((Class)FileNotFoundException.class));




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
