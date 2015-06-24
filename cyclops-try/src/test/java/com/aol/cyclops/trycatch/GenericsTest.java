package com.aol.cyclops.trycatch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

public class GenericsTest {

	@Test
	public void doIt() {
		Try.catchExceptions(NullPointerException.class,IOException.class)
				.init(() -> new BufferedReader(new FileReader("file.txt")))
				.tryWithResources(this::read)
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
