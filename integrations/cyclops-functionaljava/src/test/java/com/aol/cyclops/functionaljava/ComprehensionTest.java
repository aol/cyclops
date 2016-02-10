package com.aol.cyclops.functionaljava;


import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.typed.Do;

import fj.data.List;

public class ComprehensionTest {

	@Test
	public void cfList(){
		CompletableFuture<String> future = CompletableFuture.supplyAsync(this::loadData);
		CompletableFuture<List<String>> results1 = Do.add(future)
 														.add(FJ.anyM(List.list("first","second")))
 														.yield( loadedData -> localData-> loadedData + ":" + localData )
 														.unwrap();
		
		System.out.println(results1.join());
	}
	private String loadData(){
		return "loaded";
	}
}
