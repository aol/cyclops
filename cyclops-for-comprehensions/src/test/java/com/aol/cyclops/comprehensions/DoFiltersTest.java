package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.Do;
public class DoFiltersTest {
	
	@Test
	public void do2(){
		Stream<Double> s = Do.with(Arrays.asList(10.00,5.00,100.30))
						.and((Double d)->Arrays.asList(2.0))
						.filter((Double d)-> (Double e) -> e*d>10.00)
						.yield((Double base)->(Double bonus)-> base*(1.0+bonus));
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(330.9));
	}
	@Test
	public void do1(){
		Stream<Double> s = Do.with(Arrays.asList(10.00,5.00,100.30))
						.filter((Double d)-> d > 10.00)
						.yield((Double base)-> base+10);
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(110.3));
	}
	
	
	@Test
	public void do3(){
		Stream<Double> s = Do.with(Arrays.asList(10.00,5.00,100.30))
						.and((Double d)->Arrays.asList(2.0))
						.and((Double d)->(Double e)->Arrays.asList(10.0))
						.filter((Double d)-> (Double e) -> (Double f)-> (e*d*f)>10.00)
						.yield((Double base)->(Double bonus)->(Double woot) -> base*(1.0+bonus)*woot);
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4(){
		Stream<Double> s = Do.with(Arrays.asList(10.00,5.00,100.30))
						.and((Double d)->Arrays.asList(2.0))
						.and((Double d)->(Double e)->Arrays.asList(10.0))
						.and((Double d)->(Double e)->(Double f)->Arrays.asList(10.0))
						.filter((Double d)-> (Double e) -> (Double f)-> (Double g)->(e*d*f*g)>10.00)
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									base*(1.0+bonus)*woot*f);
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5(){
		Stream<Double> s = Do.with(Arrays.asList(10.00,5.00,100.30))
						.and((Double d)->Arrays.asList(2.0))
						.and((Double d)->(Double e)->Arrays.asList(10.0))
						.and((Double d)->(Double e)->(Double f)->Arrays.asList(10.0))
						.and( (Double d)->(Double e)->(Double f)-> (Double g)-> Arrays.asList(10.0) )
						.filter((Double d)-> (Double e) -> (Double f)-> (Double g)->
								(Double h)->
							(e*d*f*g*h)>10.00)
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->
									base*(1.0+bonus)*woot*f*g);
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6(){
		Stream<Double> s = Do.with(Arrays.asList(10.00,5.00,100.30))
						.and((Double d)->Arrays.asList(2.0))
						.and((Double d)->(Double e)->Arrays.asList(10.0))
						.and((Double d)->(Double e)->(Double f)->Arrays.asList(10.0))
						.and( (Double d)->(Double e)->(Double f)-> (Double g)-> Arrays.asList(10.0) )
						.and( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Arrays.asList(10.0) )
						.filter((Double d)-> (Double e) -> (Double f)-> (Double g)->
								(Double h)-> (Double i) ->
							(e*d*f*g*h*i)>10.00)
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->
									base*(1.0+bonus)*woot*f*g*h);
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7(){
		Stream<Double> s = Do.with(Arrays.asList(10.00,5.00,100.30))
						.and((Double d)->Arrays.asList(2.0))
						.and((Double d)->(Double e)->Arrays.asList(10.0))
						.and((Double d)->(Double e)->(Double f)->Arrays.asList(10.0))
						.and( (Double d)->(Double e)->(Double f)-> (Double g)-> Arrays.asList(10.0) )
						.and( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Arrays.asList(10.0) )
						.and( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) ->
											Arrays.asList(10.0) )
						.filter((Double d)-> (Double e) -> (Double f)-> (Double g)->
								(Double h)-> (Double i) -> (Double j) ->
							(e*d*f*g*h*i*j)>10.00)
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->(Double i)->
									base*(1.0+bonus)*woot*f*g*h*i);
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do8(){
		Stream<Double> s = Do.with(Arrays.asList(10.00,5.00,100.30))
						.and((Double d)->Arrays.asList(2.0))
						.and((Double d)->(Double e)->Arrays.asList(10.0))
						.and((Double d)->(Double e)->(Double f)->Arrays.asList(10.0))
						.and( (Double d)->(Double e)->(Double f)-> (Double g)-> Arrays.asList(10.0) )
						.and( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Arrays.asList(10.0) )
						.and( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) ->
											Arrays.asList(10.0) )
						.and( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) -> (Double j) ->
											Arrays.asList(10.0) )
						.filter((Double d)-> (Double e) -> (Double f)-> (Double g)->
								(Double h)-> (Double i) -> (Double j) -> (Double k) ->
							(e*d*f*g*h*i*j*k)>10.00)
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->(Double i)->(Double j)->
									base*(1.0+bonus)*woot*f*g*h*i*j);
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
	
	
	
}
