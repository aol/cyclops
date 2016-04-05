package com.aol.cyclops.react.lazy;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Vector;

import org.junit.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.react.base.BaseJDKStreamTest;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class JDKLazyStreamTest extends BaseJDKStreamTest{

	public <U> LazyFutureStream<U> of(U... array){
	 
		return LazyReact.parallelBuilder().from(Arrays.asList(array));
	}
	
	@Test
	public void testMapReduce2(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),is(1500));
	}
	@Test
	public void testMapReduceSeed2(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( 0,(acc,next) -> acc+next),is(1500));
	}
	
	
	@Test
	public void testMapReduceCombiner2(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( 0,
                (acc, next) -> acc+next,
                Integer::sum),is(1500));
	}
	@Test
	public void forEach() {
		Vector<Integer> list = new Vector<>();
		of(1,5,3,4,2).forEach(it-> list.add(it));
		assertThat(list,hasItem(1));
		assertThat(list,hasItem(2));
		assertThat(list,hasItem(3));
		assertThat(list,hasItem(4));
		assertThat(list,hasItem(5));
		
	}
}
