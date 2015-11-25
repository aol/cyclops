package com.aol.cyclops.javaslang.forcomprehensions;


import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javaslang.collection.List;
import lombok.Value;

import org.junit.Test;


public class ComprehensionTest {

	@Test
	public void cfList(){
		
		CompletableFuture<String> future = CompletableFuture.supplyAsync(this::loadData);
		CompletableFuture<List<String>> results1 = Do.add(future)
 														.monad(List.ofAll("first","second"))
 														.yield( loadedData -> localData-> loadedData + ":" + localData )
 														.unwrap();
		
		System.out.println(results1.join());
		
	}
	private String loadData(){
		return "loaded";
	}
	/**
	 * 
def prepareCappuccino(): Try[Cappuccino] = for {
  ground <- Try(grind("arabica beans"))
  water <- Try(heatWater(Water(25)))
  espresso <- Try(brew(ground, water))
  foam <- Try(frothMilk("milk"))
} yield combine(espresso, foam)
	 */
	@Test
	public void futureTest(){
		
		CompletableFuture<String> result = 	Do.add(grind("arabica beans"))
							  				 .add(heatWater(new Water(25)))
							  				 .withCompletableFuture(ground -> water -> brew(ground,water))
							  				 .add(frothMilk("milk"))
							  				 .yield(ground ->water -> espresso->foam-> combine(espresso,foam))
							  				 .unwrap();
		
		System.out.println(result.join());
	}
	
	
	
	CompletableFuture<String> grind(String beans) {
		 return CompletableFuture.completedFuture("ground coffee of "+ beans);
	}

	CompletableFuture<Water> heatWater(Water water){
		 return CompletableFuture.supplyAsync((Supplier) ()->water.withTemperature(85));
		  
	}

	CompletableFuture<String> frothMilk(String milk) {
		 return CompletableFuture.completedFuture("frothed " + milk);
	}

	CompletableFuture<String>	brew(String coffee, Water heatedWater){
		  return CompletableFuture.completedFuture("espresso");
	}
	String combine(String espresso ,String frothedMilk) {
		return "cappuccino";
	}
}
