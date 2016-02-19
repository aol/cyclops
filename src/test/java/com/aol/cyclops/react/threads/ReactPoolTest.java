package com.aol.cyclops.react.threads;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.SimpleReact;

public class ReactPoolTest {

	
	@Test
	public void testReact(){
		
		ReactPool<SimpleReact> pool = ReactPool.boundedPool(asList(new SimpleReact(),new SimpleReact()));
		List<String> result = pool.react( (er) -> er.react(()->"hello",()->"world").block() );
		assertThat(result.size(),is(2));
	}
	
	
	
	
	
	@Test
	public void testRoundRobin(){
		SimpleReact react1 = mock(SimpleReact.class);
		SimpleReact react2 = mock(SimpleReact.class);
		
		ReactPool<SimpleReact> pool = ReactPool.boundedPool(asList(react1,react2));
		List<Supplier<String>> suppliers = Arrays.asList(()->"hello",()->"world" );
		pool.react( (er) -> er.reactCollection(suppliers));
		pool.react( (er) -> er.reactCollection(suppliers));
		
		
		verify(react1,times(1)).reactCollection(suppliers);
		verify(react2,times(1)).reactCollection(suppliers);
	}
	
	
	
	
	
	
	
	
	
	
	@Test
	public void testElastic(){
		for(int i=0;i<1000;i++){
			ReactPool<LazyReact> pool = ReactPool.elasticPool(()->new LazyReact());
			List<String> result = pool.react( (er) -> er.react(()->"hello",()->"world").block() );
			assertThat(result.size(),is(2));
		}
	}
	@Test
	public void testUnbounded(){
		
		ReactPool<LazyReact> pool = ReactPool.unboundedPool(asList(new LazyReact(),new LazyReact()));
		List<String> result = pool.react( (er) -> er.react(()->"hello",()->"world").block() );
		pool.populate(new LazyReact());
		assertThat(result.size(),is(2));
	}
	@Test
	public void testUnboundedRoundRobin(){
		SimpleReact react1 = mock(SimpleReact.class);
		SimpleReact react2 = mock(SimpleReact.class);
		SimpleReact react3 = mock(SimpleReact.class);
		
		ReactPool<SimpleReact> pool = ReactPool.unboundedPool(asList(react1,react2));
		pool.populate(react3);
		List<Supplier<String>> suppliers = Arrays.asList( ()->"hello",()->"world" );
		pool.react( (er) -> er.reactCollection(suppliers));
		pool.react( (er) -> er.reactCollection(suppliers));
		pool.react( (er) -> er.reactCollection(suppliers));
		
		
		verify(react1,times(1)).reactCollection(suppliers);
		verify(react2,times(1)).reactCollection(suppliers);
		verify(react3,times(1)).reactCollection(suppliers);
		
	}
	
	@Test
	public void testSyncrhonous(){
		
		ReactPool<SimpleReact> pool = ReactPool.syncrhonousPool();
		new SimpleReact().react( ()->populate(pool));
		List<String> result = pool.react( (sr) -> sr.react(()->"hello",()->"world").peek(System.out::println).block() );
		assertThat(result.size(),is(2));
	}
	
	private boolean populate(ReactPool pool){
		pool.populate(new SimpleReact());
		return true;
	}
}
