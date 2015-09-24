package com.aol.simple.react.async;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

import org.junit.Test;

public class BrownBagTest {

	public static void main(String[] args) throws IOException {
		 //importing the file from my argument

		        BufferedReader input = new BufferedReader(new FileReader("/Users/johnmcclean/repos/cyclops/readme.md"));
		        String line;

		        while ((line = input.readLine()) != null) {
		           System.out.println(line);


		        }
		        input.close();

		    }
	@Test
	public void presentation(){
		Stream.of(6,5,1,2).map(e->e*100)
						  .filter(e->e<551)
						  .forEach(System.out::println);
	}
}
