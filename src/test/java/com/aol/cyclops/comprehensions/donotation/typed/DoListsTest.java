package com.aol.cyclops.comprehensions.donotation.typed;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.val;
public class DoListsTest {
	
    @Test
    public void forComprehension(){
        
        List<Integer> deals = Arrays.asList(1,2,3,null,4);
        List<Integer> costPerUnit = Arrays.asList(10,20,30,40,50);
        List<Integer> results = new ArrayList<>();
        for(int i=0;i<deals.size();i++){
            if(deals.get(i)!=null){
                List<Integer> iterableitionalBreakdown = loadBreakdown(deals.get(i));
                for(int j=0;j<iterableitionalBreakdown.size();j++){
                    for(int k=0;k<costPerUnit.size();k++){
                        results.add(deals.get(i)*costPerUnit.get(k) + iterableitionalBreakdown.get(j));
                    } 
                }
            }
            
        }
        
        
    }
    
    private int calculateTotal(int _,int i, int j, int k){
        return i*k + j;
        
    }
    @Test
    public void forComprehensionImpl(){
       
        ListX<Integer> list = For.iterable(Arrays.asList(1,2,3,null,4))
                               .optional(Optional::ofNullable)
                               .iterable(i->j-> loadBreakdown(j))
                               .iterable(i->j->k->Arrays.asList(10,20,30,40,50))
                               .yield4(this::calculateTotal)
                               .toListX();
       
    }
	private List<Integer> loadBreakdown(Integer integer) {
        return Arrays.asList(1);
    }
    @Test
	public void optionalListMultiReturn(){
		AnyM<Integer> anyM = For.optional(Optional.of(1))
									 .stream(i->Stream.of(i,2))
									 .yield(a->b-> a+b);
		
		anyM.map(i->i+1);
		System.out.println(anyM);
	}

	@Test
	public void do2(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
						.iterable( d -> asList(2.0))
						.yield( base -> bonus-> base*(1.0+bonus)).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do1(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
						.yield( base-> base+10).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(145.3));
	}
	
	
	@Test
	public void do3(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
						.iterable( d ->asList(2.0))
						.iterable(d -> e->asList(10.0))
						.yield(base ->bonus->woot -> base*(1.0+bonus)*woot).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
						.iterable(d->asList(2.0))
						.iterable(d ->e ->asList(10.0))
						.iterable( d -> e-> f->asList(10.0))
						.yield( base -> bonus->woot -> f -> base*(1.0+bonus)*woot*f).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
						.iterable( d->asList(2.0))
						.iterable( d-> e -> asList(10.0))
						.iterable( d -> e -> f -> asList(10.0))
						.iterable( d -> e -> f ->  g -> asList(10.0) )
						.yield( base ->  bonus ->  woot ->  f -> g -> base*(1.0+bonus)*woot*f*g).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
								.iterable( d -> asList(2.0))
								.iterable( d ->  e -> asList(10.0))
								.iterable( d ->  e ->  f -> asList(10.0))
								.iterable( d ->  e ->  f -> g -> asList(10.0) )
								.iterable( d -> e ->  f ->  g -> h -> asList(10.0) )
								.yield( base ->  bonus -> woot  ->  f -> g ->  h -> base*(1.0+bonus)*woot*f*g*h).stream();

		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
							.iterable( d -> asList(2.0))
							.iterable( d -> e ->  asList(10.0))
							.iterable( d -> e ->  f ->  asList(10.0))
							.iterable( d -> e ->  f ->  g -> asList(10.0) )
							.iterable( d -> e -> f ->  g ->  h-> asList(10.0) )
							.iterable( d -> e -> f ->  g ->  h ->  i -> asList(10.0) )
							.yield( base -> bonus -> woot ->  f -> g -> h -> i -> base*(1.0+bonus)*woot*f*g*h*i).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do9(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
							.iterable(d -> asList(2.0))
							.iterable( d -> e ->  asList(10.0))
							.iterable( d -> e ->  f -> asList(10.0))
							.iterable( d -> e ->  f ->  g -> asList(10.0) )
							.iterable( d -> e ->  f ->  g ->  h -> asList(10.0) )
							.iterable( d -> e ->  f ->  g ->  h ->  i -> asList(10.0) )
							.iterable( d -> e ->  f ->  g ->  h -> (Double i) -> (Double j) ->
											Arrays.asList(10.0) )
							.yield( base -> bonus ->  woot ->  f ->  g -> h -> i -> j -> base*(1.0+bonus)*woot*f*g*h*i*j).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
	
	
	
	@Test
	public void do2Just(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
								.iterable(d->asList(2.0))
								.yield( base -> bonus -> base*(1.0+bonus)).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	
	
	@Test
	public void do3Just(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
								.iterable(d->asList(2.0))
								.iterable(d->e->asList(10.0))
								.yield((Double base)->(Double bonus)->(Double woot) -> base*(1.0+bonus)*woot).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4Just(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
							.iterable(d->Arrays.asList(2.0))
							.iterable( d -> e -> asList(10.0))
							.iterable( d->e->f->asList(10.0))
							.yield( base -> bonus -> woot ->  f -> base*(1.0+bonus)*woot*f).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5Just(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
							.iterable( d-> asList(2.0))
							.iterable( d -> e -> asList(10.0))
							.iterable( d -> e -> f -> asList(10.0))
							.iterable(d ->  e -> f ->  g -> asList(10.0) )
							.yield( base -> bonus -> woot  ->  f ->  g -> base*(1.0+bonus)*woot*f*g).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6Just(){
		Stream<Double> s = For.iterable( asList(10.00,5.00,100.30))
							.iterable( d -> asList(2.0))
							.iterable( d ->  e -> asList(10.0))
							.iterable((Double d)->(Double e)->(Double f)->Arrays.asList(10.0))
							.iterable( (Double d)->(Double e)->(Double f)-> (Double g)-> Arrays.asList(10.0) )
							.iterable(d ->  e -> f ->  g ->  h ->  asList(10.0) )
							.yield( base -> bonus -> woot  ->  f -> g -> h -> base*(1.0+bonus)*woot*f*g*h).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7Just(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
						.iterable( d -> asList(2.0))
						.iterable( d -> e -> asList(10.0))
						.iterable( d -> e -> f -> asList(10.0))
						.iterable( d -> e -> f ->  g -> asList(10.0) )
						.iterable( d -> e -> f ->  g -> h-> asList(10.0) )
						.iterable(d ->  e -> f ->  g ->  h ->  i  ->asList(10.0) )
						.yield( base ->  bonus ->  woot ->   f -> g ->  h ->  i -> base*(1.0+bonus)*woot*f*g*h*i).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do9Just(){
		Stream<Double> s = For.iterable(asList(10.00,5.00,100.30))
								.iterable( d -> asList(2.0))
								.iterable(  d ->  e -> asList(10.0))
								.iterable(  d ->  e -> f ->  asList(10.0))
								.iterable(  d ->  e -> f ->  g -> asList(10.0) )
								.iterable(  d ->  e -> f ->  g ->  h -> asList(10.0) )
								.iterable(  d ->  e -> f ->  g ->  h ->  i  -> asList(10.0) )
								.iterable( d ->  e -> f ->  g ->  h ->  i  ->j->Arrays.asList(10.0) )
								.yield( base -> bonus -> woot  ->  f -> g -> h -> i -> j -> base*(1.0+bonus)*woot*f*g*h*i*j).stream();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
	
	
}
