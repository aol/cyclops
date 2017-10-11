package cyclops.collectionx;

import java.util.Arrays;

import cyclops.companion.CyclopsCollectors;
import org.junit.Test;

import cyclops.collectionx.mutable.DequeX;
import cyclops.collectionx.mutable.ListX;
import cyclops.collectionx.mutable.QueueX;
import cyclops.collectionx.mutable.SetX;
import cyclops.collectionx.mutable.SortedSetX;

public class ExampleTest {

	@Test(expected=UnsupportedOperationException.class)
	public void immutable(){
	    
		 ListX.fromIterable(ListX.immutableCollector(), Arrays.asList(1,2,3,4))
		      .plus(5);
	}
	@Test
	public void list(){
		
			 
	   ListX.of(1,2,3)
			.map(i->i+2)
			.plus(5)
			.map(i->"hello" + i)
			.forEach(System.out::println);			
	}
	@Test
	public void listToSetX(){
		SetX<String> set = ListX.of(1,2,3)
								.flatMap(i->Arrays.asList(i+2,10))
								 .plus(5)
								 .map(i->"hello" + i).toSetX()
								 .collect(CyclopsCollectors.toSetX());
		
		set.printOut();
					
	}
	@Test
	public void listFlatMap(){
		ListX.of(1,2,3)
			.flatMap(i->Arrays.asList(i+2,10))
			.plus(5)
			.map(i->"hello" + i)
			.forEach(System.out::println);
			
	}
	@Test
	public void deque(){
		DequeX.of(1,2,3)
			.map(i->i+2)
			.plus(5)
			.map(i->"hello" + i)
			.forEach(System.out::println);
			
	}
	
	@Test
	public void set(){
		SetX.of(1,2,3)
			  .map(i->i+2)
		      .plus(5)
		      .map(i->"hello" + i)
		      .forEach(System.out::println);
	}
	@Test
	public void sortedSet(){
		SortedSetX.of(1,2,3)
			  .map(i->i+2)
		      .plus(5)
		      .map(i->"hello" + i)
		      .forEach(System.out::println);
	}
	@Test
	public void queue(){
		QueueX.of(1,2,3)
			  .map(i->i+2)
		      .plus(5)
		      .map(i->"hello" + i)
		      .forEach(System.out::println);
	}
}
