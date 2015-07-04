package com.aol.cyclops.javaslang;


import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.junit.Test;

import javaslang.collection.List;

import com.aol.cyclops.comprehensions.donotation.typed.Do;

public class ComprehensionTest {

	@Test
	public void cfList(){
		CompletableFuture<String> future = CompletableFuture.supplyAsync(this::loadData);
		CompletableFuture<List<String>> results1 = Do.add(future)
 														.add(Javaslang.anyM(List.of("first","second")))
 														.yield( loadedData -> localData-> loadedData + ":" + localData )
 														.unwrap();
		
		System.out.println(results1.join());
	}
	private String loadData(){
		return "loaded";
	}
}
