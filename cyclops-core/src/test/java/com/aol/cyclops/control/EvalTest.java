package com.aol.cyclops.control;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;
public class EvalTest {

	@Test
	public void now(){
		assertThat(Eval.now(1).map(i->i+2).get(),equalTo(3));
	}
	@Test
	public void later(){
		assertThat(Eval.later(()->1).map(i->i+2).get(),equalTo(3));
	}
	int count = 0;
	@Test
	public void laterCaches(){
		count = 0;
		Eval<Integer> eval = Eval.later(()->{ 
			count++;
			return 1;
		});
		eval.map(i->i+2).get();
		eval.map(i->i+2).get();
		assertThat(eval.map(i->i+2).get(),equalTo(3));
		assertThat(count,equalTo(1));
	}
	@Test
	public void always(){
		assertThat(Eval.always(()->1).map(i->i+2).get(),equalTo(3));
	}
	@Test
	public void alwaysDoesNotCache(){
		count = 0;
		Eval<Integer> eval = Eval.always(()->{ 
			count++;
			return 1;
		});
		eval.map(i->i+2).get();
		eval.map(i->i+2).get();
		assertThat(eval.map(i->i+2).get(),equalTo(3));
		assertThat(count,equalTo(3));
	}
	@Test
	public void nowFlatMap(){
		assertThat(Eval.now(1).map(i->i+2).flatMap(i->Eval.later(()->i*3)).get(),equalTo(9));
	}
	@Test
	public void laterFlatMap(){
		assertThat(Eval.later(()->1).map(i->i+2)
						.flatMap(i->Eval.now(i*3)).get(),equalTo(9));
	}
	@Test
	public void alwaysFlatMap(){
		assertThat(Eval.always(()->1).map(i->i+2)
						.flatMap(i->Eval.now(i*3)).get(),equalTo(9));
	}
	
	public int addOne(Integer i, Integer b){
		return i+b;
	}
	@Test
	public void ap(){
		
	//	System.out.println(Maybe.Applicatives.applicative(this::addOne).ap(Eval.now(10)).ap(Eval.now(30)).get());
	//	ListX.ZippingApplicatives.applicative(this::addOne);
		//ApplicativeBuilder.<Integer>of(t->Eval.now(t)).applicative2(this::addOne);
		System.out.println(Maybe.of(10).applicatives().applicative2(this::addOne).ap(Eval.now(20)).ap(Eval.now(30)).get());
		System.out.println(Maybe.of(10).applicatives().applicative2(this::addOne).ap(Optional.of(20)).ap(Eval.now(30)).get());
		System.out.println(Eval.now(10).applicatives().applicative2(this::addOne).ap(Eval.now(20)).ap(Maybe.of(30)).get());
		
	//	Eval.now(10).ap2(Maybe.applicativeBuilder().applicative2B(this::addOne).ap(Eval.now(20));
	}
}
