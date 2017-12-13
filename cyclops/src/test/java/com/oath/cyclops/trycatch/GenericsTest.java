package com.oath.cyclops.trycatch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import cyclops.control.Try;

public class GenericsTest {

	@Test
	public void doIt() {
    Try.CheckedSupplier<BufferedReader,FileNotFoundException> s  =() -> new BufferedReader(new FileReader("file.txt"));

		Try.withResources(() -> new BufferedReader(new FileReader("file.txt")),
      this::read,
      IOException.class,NullPointerException.class, FileNotFoundException.class)
				.onFail(NullPointerException.class, System.err::println)
				.onFail(System.err::println);
	}
  @Test
  public void doItSimple() {


    Try.withResources(() -> new BufferedReader(new FileReader("file.txt")),
      this::read)
      .onFail(NullPointerException.class, System.err::println)
      .onFail(System.err::println);
  }

	private String read(BufferedReader br) throws IOException {
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
