package com.aol.cyclops.comprehensions;
import static com.aol.cyclops.comprehensions.ForComprehensions.foreachX;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.comprehensions.ComprehensionsModule.CustomForComprehension;
import com.aol.cyclops.comprehensions.ComprehensionsModule.MyComprehension;
import com.aol.cyclops.control.Do;
public class FreeFormTest {

	MyComprehension<Custom2,Custom2> comp = ForComprehensions.custom(Custom2.class);
	
	@Test
	public void freeForm(){
		List<Integer> list= Arrays.asList(1,2,3);
		Stream<Integer> stream = foreachX(c -> c.$("hello",list)
										.filter(()->c.<Integer>$("hello")<10)
										.yield(()-> c.<Integer>$("hello")+2));
		
		assertThat(Arrays.asList(3,4,5),equalTo(stream.collect(Collectors.toList())));
										
	}
	@Test
	public void freeFormDo(){
		List<Integer> list= Arrays.asList(1,2,3);
		Stream<Integer> stream = Do.add(list)
								.yield((Integer a)-> a +2).unwrap();
				
										
		
		assertThat(Arrays.asList(3,4,5),equalTo(stream.collect(Collectors.toList())));
										
	}
	@Test
	public void freeFormDoWithFilter(){
		List<Integer> list= Arrays.asList(1,2,3);
		Stream<Integer> stream = Do.add(list)
								.filter((Integer a) -> a>2)
								.yield((Integer a)-> a +2).unwrap();
				
										
		
		assertThat(Arrays.asList(5),equalTo(stream.collect(Collectors.toList())));
										
	}
	@Test
	public void freeFormDo2(){
		Stream<Integer> stream = Do.add(asList(20,30))
								   .withIterable((Integer i)->asList(1,2,3))
								   .yield((Integer a)-> (Integer b) -> a + b+2).unwrap();
		
		assertThat(stream.collect(Collectors.toList()),equalTo(Arrays.asList(23,24,25,33,34,35)));
			
	}
	@Test
	public void freeFormDo3(){
		Stream<Integer> stream = Do.add(asList(20,30))
								   .add(asList(1,2,3))
								   .yield((Integer a)-> (Integer b) -> a + b+2).unwrap();
		
		assertThat(stream.collect(Collectors.toList()),equalTo(Arrays.asList(23,24,25,33,34,35)));
			
	}
	
	@Test
	public void freeFormCustom(){
		
		List<Integer> list= Arrays.asList(1,2,3);
		Stream<Integer> stream = foreachX(Custom.class,  
									c-> c.myVar(list)
										.yield(()->c.myVar()+3)
									);
		
		assertThat(Arrays.asList(4,5,6),equalTo(stream.collect(Collectors.toList())));
										
	}
	
	@Test
	public void freeFormCustom2(){
			comp.foreach(c -> c.i(Arrays.asList(20,30))
								.j(Arrays.asList(1,2,3))
								.yield(() -> c.i() +c.j()));
	}
	
	static interface Custom extends CustomForComprehension<Stream<Integer>,Custom>{
		Integer myVar();
		Custom myVar(List<Integer> value);
	}
	static interface Custom2 extends CustomForComprehension<Stream<Integer>,Custom2>{
		Integer i();
		Custom2 i(List<Integer> value);
		Integer j();
		Custom2 j(List<Integer> value);
	}
	
}
