package com.aol.cyclops.lambda.tuple;

import static com.aol.cyclops.lambda.tuple.Tuples.convert;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.function.Function;

import lombok.AllArgsConstructor;

import org.junit.Test;

public class TupleMethodsTest {
	@Test
	public void testConvert(){
		
		TwoParams p  = Tuples.tuple(10,"hello").convert().to(TwoParams.class);
		assertThat(p.num,equalTo(10));
		assertThat(p.value,equalTo("hello"));
		
	}
	@Test
	public void testAsStreams(){
		
		Tuples.tuple(Arrays.asList(10,20,30),Arrays.asList("hello","world")).asStreams().peek(o->System.out.println(o.getClass()))
							.forEach(System.out::println);
		
	}
	@Test
	public void testTo(){
		
		TwoParams p  = Tuples.tuple(10,"hello").to(TwoParams.class);
		assertThat(p.num,equalTo(10));
		assertThat(p.value,equalTo("hello"));
		
	}
	@Test
	public void testConvertStatic(){
		
		TwoParams p  = convert(Tuples.tuple(10,"hello")).to(TwoParams.class);
		assertThat(p.num,equalTo(10));
		assertThat(p.value,equalTo("hello"));
		
	}
	@Test
	public void testApply(){
		
		
		assertThat(Tuples.tuple(10).apply1(a-> a),
				equalTo(10));
		
		
	}
	@Test
	public void testApply2(){
		
		
		assertThat(Tuples.tuple(10,20).apply2(a->b-> a+b),
				equalTo(30));
		
		
	}
	@Test
	public void testApply3(){
		
		
		assertThat(Tuples.tuple(10,20,30).apply3(a->b->c-> a+b+c),
				equalTo(60));
		
		
	}
	@Test
	public void testApply4(){
		
		
		assertThat(Tuples.tuple(10,20,30,40).apply4(a->b->c->d-> a+b+c+d),
				equalTo(100));
		
		
	}
	@Test
	public void testApply5(){
		
		
		assertThat(Tuples.tuple(10,20,30,40,50).apply5(a->b->c->d->e-> a+b+c+d+e),
				equalTo(150));
		
		
	}
	@Test
	public void testApply6(){
		
		
		assertThat(Tuples.tuple(10,20,30,40,50,60).apply6(a->b->c->d->e->f-> a+b+c+d+e+f),
				equalTo(210));
		
		
	}
	@Test
	public void testApply7(){
		
		
		assertThat(Tuples.tuple(10,20,30,40,50,60,70).apply7(a->b->c->d->e->f->g-> a+b+c+d+e+f+g),
				equalTo(280));
		
		
	}
	@Test
	public void testApply8(){
		
		
		assertThat(Tuples.tuple(10,20,30,40,50,60,70,80).apply8(a->b->c->d->e->f->g->h-> a+b+c+d+e+f+g+h),
				equalTo(360));
		
		
	}
	@AllArgsConstructor
	static class TwoParams{
		int num;
		String value;
	}
}
