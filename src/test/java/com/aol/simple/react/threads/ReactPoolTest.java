package com.aol.simple.react.threads;

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

import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;

public class ReactPoolTest {

	
	@Test
	public void testReact(){
		
		ReactPool<EagerReact> pool = ReactPool.boundedPool(asList(new EagerReact(),new EagerReact()));
		List<String> result = pool.react( (er) -> er.react(()->"hello",()->"world").block() );
		assertThat(result.size(),is(2));
	}
	
	
	
	
	
	@Test
	public void testRoundRobin(){
		EagerReact react1 = mock(EagerReact.class);
		EagerReact react2 = mock(EagerReact.class);
		
		ReactPool<EagerReact> pool = ReactPool.boundedPool(asList(react1,react2));
		List<Supplier<String>> suppliers = Arrays.asList(()->"hello",()->"world" );
		pool.react( (er) -> er.react(suppliers));
		pool.react( (er) -> er.react(suppliers));
		
		
		verify(react1,times(1)).react(suppliers);
		verify(react2,times(1)).react(suppliers);
	}
	
	
	
	
	
	
	
	
	
	
	@Test
	public void testElastic(){
		
		ReactPool<LazyReact> pool = ReactPool.elasticPool(()->new LazyReact());
		List<String> result = pool.react( (er) -> er.react(()->"hello",()->"world").block() );
		assertThat(result.size(),is(2));
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
		EagerReact react1 = mock(EagerReact.class);
		EagerReact react2 = mock(EagerReact.class);
		EagerReact react3 = mock(EagerReact.class);
		
		ReactPool<EagerReact> pool = ReactPool.unboundedPool(asList(react1,react2));
		pool.populate(react3);
		List<Supplier<String>> suppliers = Arrays.asList( ()->"hello",()->"world" );
		pool.react( (er) -> er.react(suppliers));
		pool.react( (er) -> er.react(suppliers));
		pool.react( (er) -> er.react(suppliers));
		
		
		verify(react1,times(1)).react(suppliers);
		verify(react2,times(1)).react(suppliers);
		verify(react3,times(1)).react(suppliers);
		
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
