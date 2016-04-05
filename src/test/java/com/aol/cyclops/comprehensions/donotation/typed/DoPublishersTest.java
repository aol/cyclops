package com.aol.cyclops.comprehensions.donotation.typed;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.val;
import reactor.core.publisher.Flux;
public class DoPublishersTest {
	
    
    
    private int calculateTotal(int _,int i, int j, int k){
        return i*k + j;
        
    }
    @Test
    public void forComprehensionImpl(){
      
        ListX<Integer> list = For.publisher(Flux.just(1,2,3,4))
                               .optional(Optional::ofNullable)
                               .publisher(i->j-> loadBreakdown(j))
                               .publisher(i->j->k->Flux.just(10,20,30,40,50))
                               .yield4(this::calculateTotal)
                               .toListX();
       
    }
	private Flux<Integer> loadBreakdown(Integer integer) {
        return Flux.just(1);
    }
    @Test
	public void optionalListMultiReturn(){
		AnyM<Integer> anyM = For.optional(Optional.of(1))
									 .publisher(i->Flux.just(i,2))
									 .yield(a->b-> a+b);
		
		anyM.map(i->i+1);
		System.out.println(anyM);
	}

	@Test
	public void do2(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
						.publisher( d -> Flux.just(2.0))
						.yield( base -> bonus-> base*(1.0+bonus)).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do1(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
						.yield( base-> base+10).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(145.3));
	}
	
	
	@Test
	public void do3(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
						.publisher( d ->Flux.just(2.0))
						.publisher(d -> e->Flux.just(10.0))
						.yield(base ->bonus->woot -> base*(1.0+bonus)*woot).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
						.publisher(d->Flux.just(2.0))
						.publisher(d ->e ->Flux.just(10.0))
						.publisher( d -> e-> f->Flux.just(10.0))
						.yield( base -> bonus->woot -> f -> base*(1.0+bonus)*woot*f).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
						.publisher( d->Flux.just(2.0))
						.publisher( d-> e -> Flux.just(10.0))
						.publisher( d -> e -> f -> Flux.just(10.0))
						.publisher( d -> e -> f ->  g -> Flux.just(10.0) )
						.yield( base ->  bonus ->  woot ->  f -> g -> base*(1.0+bonus)*woot*f*g).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
								.publisher( d -> Flux.just(2.0))
								.publisher( d ->  e -> Flux.just(10.0))
								.publisher( d ->  e ->  f -> Flux.just(10.0))
								.publisher( d ->  e ->  f -> g -> Flux.just(10.0) )
								.publisher( d -> e ->  f ->  g -> h -> Flux.just(10.0) )
								.yield( base ->  bonus -> woot  ->  f -> g ->  h -> base*(1.0+bonus)*woot*f*g*h).stream();

		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
							.publisher( d -> Flux.just(2.0))
							.publisher( d -> e ->  Flux.just(10.0))
							.publisher( d -> e ->  f ->  Flux.just(10.0))
							.publisher( d -> e ->  f ->  g -> Flux.just(10.0) )
							.publisher( d -> e -> f ->  g ->  h-> Flux.just(10.0) )
							.publisher( d -> e -> f ->  g ->  h ->  i -> Flux.just(10.0) )
							.yield( base -> bonus -> woot ->  f -> g -> h -> i -> base*(1.0+bonus)*woot*f*g*h*i).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do9(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
							.publisher(d -> Flux.just(2.0))
							.publisher( d -> e ->  Flux.just(10.0))
							.publisher( d -> e ->  f -> Flux.just(10.0))
							.publisher( d -> e ->  f ->  g -> Flux.just(10.0) )
							.publisher( d -> e ->  f ->  g ->  h -> Flux.just(10.0) )
							.publisher( d -> e ->  f ->  g ->  h ->  i -> Flux.just(10.0) )
							.publisher( d -> e ->  f ->  g ->  h -> (Double i) -> (Double j) ->
											Flux.just(10.0) )
							.yield( base -> bonus ->  woot ->  f ->  g -> h -> i -> j -> base*(1.0+bonus)*woot*f*g*h*i*j).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
	
	
	
	@Test
	public void do2Just(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
								.publisher(d->Flux.just(2.0))
								.yield( base -> bonus -> base*(1.0+bonus)).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	
	
	@Test
	public void do3Just(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
								.publisher(d->Flux.just(2.0))
								.publisher(d->e->Flux.just(10.0))
								.yield((Double base)->(Double bonus)->(Double woot) -> base*(1.0+bonus)*woot).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4Just(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
							.publisher(d->Flux.just(2.0))
							.publisher( d -> e -> Flux.just(10.0))
							.publisher( d->e->f->Flux.just(10.0))
							.yield( base -> bonus -> woot ->  f -> base*(1.0+bonus)*woot*f).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5Just(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
							.publisher( d-> Flux.just(2.0))
							.publisher( d -> e -> Flux.just(10.0))
							.publisher( d -> e -> f -> Flux.just(10.0))
							.publisher(d ->  e -> f ->  g -> Flux.just(10.0) )
							.yield( base -> bonus -> woot  ->  f ->  g -> base*(1.0+bonus)*woot*f*g).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6Just(){
		Stream<Double> s = For.publisher( Flux.just(10.00,5.00,100.30))
							.publisher( d -> Flux.just(2.0))
							.publisher( d ->  e -> Flux.just(10.0))
							.publisher((Double d)->(Double e)->(Double f)->Flux.just(10.0))
							.publisher( (Double d)->(Double e)->(Double f)-> (Double g)-> Flux.just(10.0) )
							.publisher(d ->  e -> f ->  g ->  h ->  Flux.just(10.0) )
							.yield( base -> bonus -> woot  ->  f -> g -> h -> base*(1.0+bonus)*woot*f*g*h).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7Just(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
						.publisher( d -> Flux.just(2.0))
						.publisher( d -> e -> Flux.just(10.0))
						.publisher( d -> e -> f -> Flux.just(10.0))
						.publisher( d -> e -> f ->  g -> Flux.just(10.0) )
						.publisher( d -> e -> f ->  g -> h-> Flux.just(10.0) )
						.publisher(d ->  e -> f ->  g ->  h ->  i  ->Flux.just(10.0) )
						.yield( base ->  bonus ->  woot ->   f -> g ->  h ->  i -> base*(1.0+bonus)*woot*f*g*h*i).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do9Just(){
		Stream<Double> s = For.publisher(Flux.just(10.00,5.00,100.30))
								.publisher( d -> Flux.just(2.0))
								.publisher(  d ->  e -> Flux.just(10.0))
								.publisher(  d ->  e -> f ->  Flux.just(10.0))
								.publisher(  d ->  e -> f ->  g -> Flux.just(10.0) )
								.publisher(  d ->  e -> f ->  g ->  h -> Flux.just(10.0) )
								.publisher(  d ->  e -> f ->  g ->  h ->  i  -> Flux.just(10.0) )
								.publisher( d ->  e -> f ->  g ->  h ->  i  ->j->Flux.just(10.0) )
								.yield( base -> bonus -> woot  ->  f -> g -> h -> i -> j -> base*(1.0+bonus)*woot*f*g*h*i*j).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
	
	
}
