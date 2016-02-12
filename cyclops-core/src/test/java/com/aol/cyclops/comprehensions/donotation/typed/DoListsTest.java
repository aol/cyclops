package com.aol.cyclops.comprehensions.donotation.typed;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.Do;
import com.aol.cyclops.control.AnyM;

import lombok.val;
public class DoListsTest {
	
	@Test
	public void optionalListMultiReturn(){
		AnyM<Integer> anyM = Do.add(Optional.of(1))
									 .withStream(i->Stream.of(i,2))
									 .yield(a->b-> a+b);
		
		anyM.map(i->i+1);
		System.out.println(anyM);
	}

	@Test
	public void do2(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
						.withCollection( d -> asList(2.0))
						.yield( base -> bonus-> base*(1.0+bonus)).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do1(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
						.yield( base-> base+10).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(145.3));
	}
	
	
	@Test
	public void do3(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
						.withCollection( d ->asList(2.0))
						.withCollection(d -> e->asList(10.0))
						.yield(base ->bonus->woot -> base*(1.0+bonus)*woot).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
						.withCollection(d->asList(2.0))
						.withCollection(d ->e ->asList(10.0))
						.withCollection( d -> e-> f->asList(10.0))
						.yield( base -> bonus->woot -> f -> base*(1.0+bonus)*woot*f).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
						.withCollection( d->asList(2.0))
						.withCollection( d-> e -> asList(10.0))
						.withCollection( d -> e -> f -> asList(10.0))
						.withCollection( d -> e -> f ->  g -> asList(10.0) )
						.yield( base ->  bonus ->  woot ->  f -> g -> base*(1.0+bonus)*woot*f*g).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
								.withCollection( d -> asList(2.0))
								.withCollection( d ->  e -> asList(10.0))
								.withCollection( d ->  e ->  f -> asList(10.0))
								.withCollection( d ->  e ->  f -> g -> asList(10.0) )
								.withCollection( d -> e ->  f ->  g -> h -> asList(10.0) )
								.yield( base ->  bonus -> woot  ->  f -> g ->  h -> base*(1.0+bonus)*woot*f*g*h).toSequence();

		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
							.withCollection( d -> asList(2.0))
							.withCollection( d -> e ->  asList(10.0))
							.withCollection( d -> e ->  f ->  asList(10.0))
							.withCollection( d -> e ->  f ->  g -> asList(10.0) )
							.withCollection( d -> e -> f ->  g ->  h-> asList(10.0) )
							.withCollection( d -> e -> f ->  g ->  h ->  i -> asList(10.0) )
							.yield( base -> bonus -> woot ->  f -> g -> h -> i -> base*(1.0+bonus)*woot*f*g*h*i).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do9(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
							.withCollection(d -> asList(2.0))
							.withCollection( d -> e ->  asList(10.0))
							.withCollection( d -> e ->  f -> asList(10.0))
							.withCollection( d -> e ->  f ->  g -> asList(10.0) )
							.withCollection( d -> e ->  f ->  g ->  h -> asList(10.0) )
							.withCollection( d -> e ->  f ->  g ->  h ->  i -> asList(10.0) )
							.withCollection( d -> e ->  f ->  g ->  h -> (Double i) -> (Double j) ->
											Arrays.asList(10.0) )
							.yield( base -> bonus ->  woot ->  f ->  g -> h -> i -> j -> base*(1.0+bonus)*woot*f*g*h*i*j).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
	
	
	
	@Test
	public void do2Just(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
								.add(asList(2.0))
								.yield( base -> bonus -> base*(1.0+bonus)).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	
	
	@Test
	public void do3Just(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
								.add(asList(2.0))
								.add(asList(10.0))
								.yield((Double base)->(Double bonus)->(Double woot) -> base*(1.0+bonus)*woot).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4Just(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
							.add(Arrays.asList(2.0))
							.withCollection( d -> e -> asList(10.0))
							.add(asList(10.0))
							.yield( base -> bonus -> woot ->  f -> base*(1.0+bonus)*woot*f).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5Just(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
							.add(asList(2.0))
							.withCollection( d -> e -> asList(10.0))
							.withCollection( d -> e -> f -> asList(10.0))
							.add( asList(10.0) )
							.yield( base -> bonus -> woot  ->  f ->  g -> base*(1.0+bonus)*woot*f*g).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6Just(){
		Stream<Double> s = Do.add( asList(10.00,5.00,100.30))
							.withCollection( d -> asList(2.0))
							.withCollection( d ->  e -> asList(10.0))
							.withCollection((Double d)->(Double e)->(Double f)->Arrays.asList(10.0))
							.withCollection( (Double d)->(Double e)->(Double f)-> (Double g)-> Arrays.asList(10.0) )
							.add( asList(10.0) )
							.yield( base -> bonus -> woot  ->  f -> g -> h -> base*(1.0+bonus)*woot*f*g*h).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7Just(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
						.withCollection( d -> asList(2.0))
						.withCollection( d -> e -> asList(10.0))
						.withCollection( d -> e -> f -> asList(10.0))
						.withCollection( d -> e -> f ->  g -> asList(10.0) )
						.withCollection( d -> e -> f ->  g -> h-> asList(10.0) )
						.add(asList(10.0) )
						.yield( base ->  bonus ->  woot ->   f -> g ->  h ->  i -> base*(1.0+bonus)*woot*f*g*h*i).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do9Just(){
		Stream<Double> s = Do.add(asList(10.00,5.00,100.30))
								.withCollection( d -> asList(2.0))
								.withCollection(  d ->  e -> asList(10.0))
								.withCollection(  d ->  e -> f ->  asList(10.0))
								.withCollection(  d ->  e -> f ->  g -> asList(10.0) )
								.withCollection(  d ->  e -> f ->  g ->  h -> asList(10.0) )
								.withCollection(  d ->  e -> f ->  g ->  h ->  i  -> asList(10.0) )
								.add(Arrays.asList(10.0) )
								.yield( base -> bonus -> woot  ->  f -> g -> h -> i -> j -> base*(1.0+bonus)*woot*f*g*h*i*j).toSequence();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
	
	
}
