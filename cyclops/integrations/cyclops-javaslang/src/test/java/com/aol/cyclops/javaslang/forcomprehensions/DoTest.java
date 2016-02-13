package com.aol.cyclops.javaslang.forcomprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;
public class DoTest {
	
	
	@Test
	public void do2(){
		Stream<Double> s =  Do.addStream(Stream.of(10.00,5.00,100.30))
						.withStream(d->Stream.of(2.0))
						.yield( base -> bonus-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do2Add(){
		Stream<Double> s =  Do.addStream(Stream.of(10.00,5.00,100.30))
						.addStream(()->Stream.of(2.0))
						.yield( base -> bonus-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do3Add(){
		Stream<Double> s =  Do.addStream(Stream.of(10.00,5.00,100.30))
						.add(Arrays.asList(2.0))
						.addStream(()->Stream.of(3.0))
						.yield( base -> bonus-> v-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do4Add(){
		Stream<Double> s =  Do.addStream(Stream.of(10.00,5.00,100.30))
						.add(Arrays.asList(2.0))
						.add(Arrays.asList(2.0))
						.addStream(()->Stream.of(3.0))
						.yield( base -> bonus-> v-> v1->base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do5Add(){
		Stream<Double> s =  Do.addStream(Stream.of(10.00,5.00,100.30))
						.add(Arrays.asList(2.0))
						.add(Arrays.asList(2.0))
						.add(Arrays.asList(2.0))
						.addStream(()->Stream.of(3.0))
						.yield( base -> bonus-> v->v1->v2-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do6Add(){
		Stream<Double> s =  Do.addStream(Stream.of(10.00,5.00,100.30))
						.add(Arrays.asList(2.0))
						.add(Arrays.asList(2.0))
						.add(Arrays.asList(2.0))
						.add(Arrays.asList(2.0))
						.addStream(()->Stream.of(3.0))
						.yield( base -> bonus-> v->v1->v2->v3-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do7Add(){
		Stream<Double> s =  Do.addStream(Stream.of(10.00,5.00,100.30))
						.add(Arrays.asList(2.0))
						.add(Arrays.asList(2.0))
						.add(Arrays.asList(2.0))
						.add(Arrays.asList(2.0))
						.add(Arrays.asList(2.0))
						.addStream(()->Stream.of(3.0))
						.yield( base -> bonus-> v->v1->v2->v3->v4-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do1(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
						.yield( base -> base+10).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(145.3));
	}
	
	
	@Test
	public void do3(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
						.addStream(()->Stream.of(2.0))
						.withStream( d -> e ->Stream.of(10.0))
						.yield( base -> bonus -> woot  -> base*(1.0+bonus)*woot).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
							.withStream( d->Stream.of(2.0))
							.withStream( d -> e ->Stream.of(10.0))
							.withStream(  d ->  e ->  f -> Stream.of(10.0))
							.yield(base -> bonus -> woot ->  f -> base*(1.0+bonus)*woot*f).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
						.withStream( d ->Stream.of(2.0))
						.withStream( d -> e ->Stream.of(10.0))
						.withStream( d-> e -> f ->Stream.of(10.0))
						.withStream( d -> e -> f ->  g -> Stream.of(10.0) )
						.yield( base -> bonus->  woot  ->  f->
									g ->
									base*(1.0+bonus)*woot*f*g).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
						.withStream((Double d)->Stream.of(2.0))
						.withStream((Double d)->(Double e)->Stream.of(10.0))
						.withStream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Stream.of(10.0) )
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->
									base*(1.0+bonus)*woot*f*g*h).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
						.withStream((Double d)->Stream.of(2.0))
						.withStream((Double d)->(Double e)->Stream.of(10.0))
						.withStream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Stream.of(10.0) )
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) ->
											Stream.of(10.0) )
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->(Double i)->
									base*(1.0+bonus)*woot*f*g*h*i).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do9(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
						.withStream((Double d)->Stream.of(2.0))
						.withStream((Double d)->(Double e)->Stream.of(10.0))
						.withStream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Stream.of(10.0) )
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) ->
											Stream.of(10.0) )
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) -> (Double j) ->
											Stream.of(10.0) )
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->(Double i)->(Double j)->
									base*(1.0+bonus)*woot*f*g*h*i*j).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
	
	
	
	@Test
	public void do2Just(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
						.addStream(()->Stream.of(2.0))
						.yield((Double base)->(Double bonus)-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	
	
	@Test
	public void do3Just(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
							.addStream(()->Stream.of(2.0))
							.addStream(()->Stream.of(10.0))
							.yield( base -> bonus -> woot -> base*(1.0+bonus)*woot).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4Just(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
							.addStream(()->Stream.of(2.0))
							.withStream(d-> e ->Stream.of(10.0))
							.addStream(()->Stream.of(10.0))
							.yield( base -> bonus -> woot  -> f -> base*(1.0+bonus)*woot*f).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5Just(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
							.addStream(()->Stream.of(2.0))
							.withStream( d -> e->Stream.of(10.0))
							.withStream( d -> e-> f ->Stream.of(10.0))
							.addStream(()->Stream.of(10.0) )
							.yield(base -> bonus -> woot ->  f-> g -> base*(1.0+bonus)*woot*f*g)
							.unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6Just(){
		Stream<Double> s =  Do.addStream(Stream.of(10.00,5.00,100.30))
						.withStream((Double d)->Stream.of(2.0))
						.withStream((Double d)->(Double e)->Stream.of(10.0))
						.withStream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
						.addStream(()->Stream.of(10.0) )
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->
									base*(1.0+bonus)*woot*f*g*h).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7Just(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
							 .withStream((Double d)->Stream.of(2.0))
							 .withStream((Double d)->(Double e)->Stream.of(10.0))
							 .withStream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
							 .withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
							 .withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Stream.of(10.0) )
											.addStream(()->Stream.of(10.0) )
							.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->(Double i)->
									base*(1.0+bonus)*woot*f*g*h*i).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do9Just(){
		Stream<Double> s = Do.addStream(Stream.of(10.00,5.00,100.30))
						.withStream((Double d)->Stream.of(2.0))
						.withStream((Double d)->(Double e)->Stream.of(10.0))
						.withStream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Stream.of(10.0) )
						.withStream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) ->
											Stream.of(10.0) )
						.addStream(()->Stream.of(10.0) )
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->(Double i)->(Double j)->
									base*(1.0+bonus)*woot*f*g*h*i*j).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
}
