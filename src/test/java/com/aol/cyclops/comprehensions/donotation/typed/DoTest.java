package com.aol.cyclops.comprehensions.donotation.typed;

import static com.aol.cyclops.control.ReactiveSeq.range;
import static org.hamcrest.Matchers.equalTo;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.cyclops.control.For;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.anyM.AnyMSeq;

import lombok.val;
public class DoTest {
	
    @Test
    public void doGen2(){
       
        ReactiveSeq.range(1,10)
                   .forEach2(i->range(0, i), i->j->tuple(i,j));
                
        //  .forEach(System.out::println);
        
        
    }
	@Test
	public void doGen(){
	   AnyMSeq<Tuple2<Integer,Integer>> seq =  For.stream(ReactiveSeq.range(1,10))
	                                             .stream(i->ReactiveSeq.range(0, i))
	                                             .yield2((a,b)->Tuple.tuple(a,b));
	   
	   seq.forEach(System.out::println);
	
	    
	}
	@Test
	public void do2(){
		
		Stream<Double> s =  For.stream(Stream.of(10.00,5.00,100.30))
						.stream(d->Stream.of(2.0))
						.yield( base -> bonus-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do2iterable(){
		Stream<Double> s =  For.stream(Stream.of(10.00,5.00,100.30))
						.stream(a->Stream.of(2.0))
						.yield( base -> bonus-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do3iterable(){
		Stream<Double> s =  For.stream(Stream.of(10.00,5.00,100.30))
						.iterable(i->Arrays.asList(2.0))
						.stream(i->j->Stream.of(3.0))
						.yield( base -> bonus-> v-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do4iterable(){
		Stream<Double> s =  For.stream(Stream.of(10.00,5.00,100.30))
		                        .iterable(a->Arrays.asList(2.0))
		                        .iterable(a->b->Arrays.asList(2.0))
		                        .stream(a->b->c->Stream.of(3.0))
						.yield( base -> bonus-> v-> v1->base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do5iterable(){
		Stream<Double> s =  For.stream(Stream.of(10.00,5.00,100.30))
                		        .iterable(a->Arrays.asList(2.0))
                                .iterable(a->b->Arrays.asList(2.0))
                                .iterable(a->b->c->Arrays.asList(2.0))
                                .stream(a->b->c->d->Stream.of(3.0) )
						.yield( base -> bonus-> v->v1->v2-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do6iterable(){
		Stream<Double> s =  For.stream(Stream.of(10.00,5.00,100.30))
            		        .iterable(a->Arrays.asList(2.0))
                            .iterable(a->b->Arrays.asList(2.0))
                            .iterable(a->b->c->Arrays.asList(2.0))
                            .iterable(a->b->c->d->Arrays.asList(2.0))
                            .stream(a->b->c->d->e->Stream.of(3.0))
						.yield( base -> bonus-> v->v1->v2->v3-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do7iterable(){
		Stream<Double> s =  For.stream(Stream.of(10.00,5.00,100.30))
						.iterable(a->Arrays.asList(2.0))
						.iterable(a->b->Arrays.asList(2.0))
						.iterable(a->b->c->Arrays.asList(2.0))
						.iterable(a->b->c->d->Arrays.asList(2.0))
						.iterable(a->b->c->d->e->Arrays.asList(2.0))
						.stream(a->b->c->d->e->f->Stream.of(3.0))
						.yield( base -> bonus-> v->v1->v2->v3->v4-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	@Test
	public void do1(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
						.yield( base -> base+10).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(145.3));
	}
	
	
	@Test
	public void do3(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
						.stream(d->Stream.of(2.0))
						.stream( d -> e ->Stream.of(10.0))
						.yield( base -> bonus -> woot  -> base*(1.0+bonus)*woot).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
							.stream( d->Stream.of(2.0))
							.stream( d -> e ->Stream.of(10.0))
							.stream(  d ->  e ->  f -> Stream.of(10.0))
							.yield(base -> bonus -> woot ->  f -> base*(1.0+bonus)*woot*f).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
						.stream( d ->Stream.of(2.0))
						.stream( d -> e ->Stream.of(10.0))
						.stream( d-> e -> f ->Stream.of(10.0))
						.stream( d -> e -> f ->  g -> Stream.of(10.0) )
						.yield( base -> bonus->  woot  ->  f->
									g ->
									base*(1.0+bonus)*woot*f*g).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
						.stream((Double d)->Stream.of(2.0))
						.stream((Double d)->(Double e)->Stream.of(10.0))
						.stream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Stream.of(10.0) )
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->
									base*(1.0+bonus)*woot*f*g*h).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
						.stream((Double d)->Stream.of(2.0))
						.stream((Double d)->(Double e)->Stream.of(10.0))
						.stream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Stream.of(10.0) )
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) ->
											Stream.of(10.0) )
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->(Double i)->
									base*(1.0+bonus)*woot*f*g*h*i).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do9(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
						.stream((Double d)->Stream.of(2.0))
						.stream((Double d)->(Double e)->Stream.of(10.0))
						.stream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Stream.of(10.0) )
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) ->
											Stream.of(10.0) )
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) -> (Double j) ->
											Stream.of(10.0) )
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->(Double i)->(Double j)->
									base*(1.0+bonus)*woot*f*g*h*i*j).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
	
	
	
	@Test
	public void do2Just(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
						.stream(d->Stream.of(2.0))
						.yield((Double base)->(Double bonus)-> base*(1.0+bonus)).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345.9));
	}
	
	
	@Test
	public void do3Just(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
							.stream(d->Stream.of(2.0))
							.stream(d->e->Stream.of(10.0))
							.yield( base -> bonus -> woot -> base*(1.0+bonus)*woot).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459.0));
	}
	@Test
	public void do4Just(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
							.stream(d->Stream.of(2.0))
							.stream(d-> e ->Stream.of(10.0))
							.stream(d->e->f->Stream.of(10.0))
							.yield( base -> bonus -> woot  -> f -> base*(1.0+bonus)*woot*f).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590.0));
	}
	@Test
	public void do5Just(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
							.stream(d->Stream.of(2.0))
							.stream( d -> e->Stream.of(10.0))
							.stream( d -> e-> f ->Stream.of(10.0))
							.stream(d->e->f->g->Stream.of(10.0) )
							.yield(base -> bonus -> woot ->  f-> g -> base*(1.0+bonus)*woot*f*g)
							.unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900.0));
	}
	@Test
	public void do6Just(){
		Stream<Double> s =  For.stream(Stream.of(10.00,5.00,100.30))
						.stream((Double d)->Stream.of(2.0))
						.stream((Double d)->(Double e)->Stream.of(10.0))
						.stream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
						.stream( d->e->f->g->h-> Stream.of(10.0) )
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->
									base*(1.0+bonus)*woot*f*g*h).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(3459000.0));
	}
	@Test
	public void do7Just(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
							 .stream((Double d)->Stream.of(2.0))
							 .stream((Double d)->(Double e)->Stream.of(10.0))
							 .stream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
							 .stream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
							 .stream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Stream.of(10.0) )
											.stream(d->e->f->g->h->i->Stream.of(10.0) )
							.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->(Double i)->
									base*(1.0+bonus)*woot*f*g*h*i).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(34590000.0));
	}
	@Test
	public void do9Just(){
		Stream<Double> s = For.stream(Stream.of(10.00,5.00,100.30))
						.stream((Double d)->Stream.of(2.0))
						.stream((Double d)->(Double e)->Stream.of(10.0))
						.stream((Double d)->(Double e)->(Double f)->Stream.of(10.0))
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> Stream.of(10.0) )
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)->
											Stream.of(10.0) )
						.stream( (Double d)->(Double e)->(Double f)-> (Double g)-> (Double h)-> (Double i) ->
											Stream.of(10.0) )
						.stream(d->e->f->g->h->i->j->Stream.of(10.0) )
						.yield((Double base)->(Double bonus)->(Double woot) -> (Double f)->
									(Double g)->(Double h)->(Double i)->(Double j)->
									base*(1.0+bonus)*woot*f*g*h*i*j).unwrap();
		
		val total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(345900000.0));
	}
}
