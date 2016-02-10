package com.aol.cyclops.guava;


import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.junit.Test;

import javaslang.collection.List;

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.guava.Guava;
import com.google.common.collect.FluentIterable;

public class ComprehensionTest {

	@Test
	public void cfList(){
		
		CompletableFuture<String> future = CompletableFuture.supplyAsync(this::loadData);
		CompletableFuture<List<String>> results1 = Do.add(future)
 														.add(Guava.anyM(FluentIterable.of(new String[]{"first","second"})))
 														.yield( loadedData -> localData-> loadedData + ":" + localData )
 														.unwrap();
		
		System.out.println(results1.join());
		
	}
	private String loadData(){
		return "loaded";
	}
}
