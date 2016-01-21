package com.aol.cyclops.javaslang.reactivestreams.reactivestream.tests;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;
public class StreamTest {
	 public static <U> ReactiveStream<U> of(U... array){
		 return ReactiveStream.of(array);
	 }

	@Test
	public void testAnyMatch(){
		assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
	}
	@Test
	public void testAllMatch(){
		assertThat(of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
	}
	@Test
	public void testNoneMatch(){
		assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),equalTo(true));
	}
	
	
	@Test
	public void testAnyMatchFalse(){
		assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(8)),equalTo(false));
	}
	@Test
	public void testAllMatchFalse(){
		assertThat(of(1,2,3,4,5).allMatch(it-> it<0 && it >6),equalTo(false));
	}
	@Test
	public void testFlatMap(){
		assertThat(of( asList("1","10"), asList("2"),asList("3"),asList("4")).flatMapStream( list -> list.stream() ).collect(Collectors.toList() 
						),hasItem("10"));
	}
	
	@Test
	public void testMapReduce(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),equalTo(1500));
	}
	
	@Test
	public void testDistinct(){
		assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()).size(),equalTo(2));
		assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()),hasItem(1));
		assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()),hasItem(2));
	}
	
	
	@Test
	public void sorted() {
		assertThat(of(1,5,3,4,2).sorted().collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void sortedComparator() {
		assertThat(of(1,5,3,4,2).sorted((t1,t2) -> t2-t1).collect(Collectors.toList()),equalTo(Arrays.asList(5,4,3,2,1)));
	}
	@Test
	public void forEach() {
		List<Integer> list = new ArrayList<>();
		of(1,5,3,4,2).forEach(it-> list.add(it));
		assertThat(list,hasItem(1));
		assertThat(list,hasItem(2));
		assertThat(list,hasItem(3));
		assertThat(list,hasItem(4));
		assertThat(list,hasItem(5));
		
	}
	
	
	

	
	@Test
	public void collectSBB(){
		
		List<Integer> list = of(1,2,3,4,5).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		assertThat(list.size(),equalTo(5));
	}
	@Test
	public void collect(){
		assertThat(of(1,2,3,4,5).collect(Collectors.toList()).size(),equalTo(5));
		assertThat(of(1,1,1,2).collect(Collectors.toSet()).size(),equalTo(2));
	}
	@Test
	public void testFilter(){
		assertThat(of(1,1,1,2).filter(it -> it==1).collect(Collectors.toList()).size(),equalTo(3));
	}
	@Test
	public void testMap(){
		assertThat(of(1).map(it->it+100).collect(Collectors.toList()).get(0),equalTo(101));
	}
	Object val;
	@Test
	public void testPeek(){
		val = null;
		of(1).map(it->it+100).peek(it -> val=it).collect(Collectors.toList());
		assertThat(val,equalTo(101));
	}
		


}