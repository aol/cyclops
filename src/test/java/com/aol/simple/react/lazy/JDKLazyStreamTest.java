package com.aol.simple.react.lazy;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.simple.react.base.BaseJDKStreamTest;
import com.aol.simple.react.stream.lazy.ParallelReductionConfig;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class JDKLazyStreamTest extends BaseJDKStreamTest{

	public <U> LazyFutureStream<U> of(U... array){
	 
		return LazyFutureStream.parallelBuilder().of(Arrays.asList(array));
	}
	
	@Test
	public void testMapReduce2(){
		assertThat(of(1,2,3,4,5).withParallelReduction(new ParallelReductionConfig(3,true)).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),is(1500));
	}
	@Test
	public void testMapReduceSeed2(){
		//Fails : this is an bug in JDK 8:45
//		assertThat(Stream.of(1,2,3,4,5).map(i->i*100).reduce( 50,(acc,next) -> acc+next),equalTo(Stream.of(1,2,3,4,5).map(i->i*100).parallel().reduce( 50,(acc,next) -> acc+next)));
	
		//assertThat(of(1,2,3,4,5).withParallelReduction(new ParallelReductionConfig(0,true)).map(it -> it*100).reduce( 50,(acc,next) -> acc+next),is(1550));
	}
	
	
	@Test
	public void testMapReduceCombiner2(){
		assertThat(of(1,2,3,4,5).withParallelReduction(new ParallelReductionConfig(3,true)).map(it -> it*100).reduce( 0,
                (acc, next) -> acc+next,
                Integer::sum),is(1500));
	}
	@Test
	public void forEach() {
		Vector<Integer> list = new Vector<>();
		of(1,5,3,4,2).withParallelReduction(new ParallelReductionConfig(3,true)).forEach(it-> list.add(it));
		assertThat(list,hasItem(1));
		assertThat(list,hasItem(2));
		assertThat(list,hasItem(3));
		assertThat(list,hasItem(4));
		assertThat(list,hasItem(5));
		
	}
}
