package com.aol.cyclops.comprehensions.donotation.typed;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;


public class DoFiltersTest {
	static class Bean {
		public Integer getNumber(){
			return null;
		}
	}
	public Bean getCount(){
		return null;
	}
	@Test
	public void do2(){
		
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
							.withCollection( d-> asList(2.0))
							.filter(d-> e ->     (e*d)>10.00)
							.yield(base -> bonus->   base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(330.9));
	}
	private Object getPerson() {
		// TODO Auto-generated method stub
		return null;
	}
	@Test
	public void do1(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
							.filter( d-> d > 10.00)
							.yield(base -> base+10).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(110.3));
	}
	
	
	@Test
	public void do3(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
							.withCollection( d-> asList(2.0))
							.withCollection( d -> e->asList(10.0))
							.filter(d-> e -> f->    (e*d*f)>10.00)
							.yield(base -> bonus-> woot ->    base*(1.0+bonus)*woot).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
							.withCollection( d-> asList(2.0))
							.withCollection( d -> e->asList(10.0))
							.withCollection( d -> e -> f -> asList(10.0))
							.filter(d-> e -> f-> g->    (e*d*f*g)>10.00)
							.yield(base -> bonus-> woot ->  f->   base*(1.0+bonus)*woot*f).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5(){
		Stream<Double> s =  Do.add(asList(10.00,5.00,100.30))
								.withCollection( d-> asList(2.0))
								.withCollection( d -> e->asList(10.0))
								.withCollection( d -> e -> f -> asList(10.0))
								.withCollection( d -> e ->  f ->  g-> asList(10.0) )
								.filter(d-> e -> f-> g-> h->   (e*d*f*g*h)>10.00)
								.yield(base -> bonus-> woot ->  f-> g ->   base*(1.0+bonus)*woot*f*g).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
							.withCollection( d-> asList(2.0))
							.withCollection( d -> e->asList(10.0))
							.withCollection( d -> e -> f -> asList(10.0))
							.withCollection( d -> e ->  f ->  g-> asList(10.0) )
							.withCollection( d -> e ->  f -> g -> h->
									asList(10.0) )
							.filter(d-> e -> f-> g-> h-> i->  (e*d*f*g*h*i)>10.00)
							.yield(base -> bonus-> woot ->  f-> g -> h ->  base*(1.0+bonus)*woot*f*g*h).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
							.withCollection( d-> asList(2.0))
							.withCollection( d -> e->asList(10.0))
							.withCollection( d -> e -> f -> asList(10.0))
							.withCollection( d -> e ->  f ->  g-> asList(10.0) )
							.withCollection( d -> e ->  f -> g -> h->
												asList(10.0) )
							.withCollection(  d -> e -> f ->  g -> h ->  i -> asList(10.0) )
							.filter(d-> e -> f-> g-> h-> i-> j-> (e*d*f*g*h*i*j)>10.00)
							.yield(base -> bonus-> woot ->  f-> g -> h -> i-> base*(1.0+bonus)*woot*f*g*h*i).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do8(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
						.withCollection( d-> asList(2.0))
						.withCollection( d -> e->asList(10.0))
						.withCollection( d -> e -> f -> asList(10.0))
						.withCollection( d -> e ->  f ->  g-> asList(10.0) )
						.withCollection( d -> e ->  f -> g -> h->
											asList(10.0) )
						.withCollection(  d -> e -> f ->  g -> h ->  i -> asList(10.0) )
						.withCollection( d ->  e -> f ->  g ->  h ->  i ->  j -> asList(10.0) )
						.filter(d-> e -> f-> g-> h-> i-> j-> k->(e*d*f*g*h*i*j*k)>10.00)
						.yield(base -> bonus-> woot ->  f-> g -> h -> i-> j-> base*(1.0+bonus)*woot*f*g*h*i*j).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
	
	
	
}
